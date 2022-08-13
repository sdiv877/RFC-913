package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import utils.Utils;

public class SFTPClientWorker implements Runnable {
	private static final List<String> ZERO_ARG_CMDS = Arrays.asList("done", "send", "stop");
	private static final List<String> ONE_OR_TWO_ARG_CMDS = Arrays.asList("list");
	private static final List<String> TWO_ARG_CMDS = Arrays.asList("stor");
	private static final List<String> ALL_CMDS = Arrays.asList("user", "acct", "pass", "type", "list",
			"cdir", "kill", "name", "tobe", "done", "retr", "send", "stop", "stor", "size");
	private static final List<String> RESTRICTED_CMDS = Arrays.asList("type", "list", "kill", "name",
			"tobe", "retr", "send", "stop", "stor", "size");

    private int id;
    private Socket clientSocket;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String transferType;
    private User selectedUser;
    private boolean loggedIn;
    private String selectedAccount;
    private String currentDir;
    private String pendingDirChange;
    private String pendingFileToRename;
    private String pendingFileToRetrieve;
    private PendingStorFile pendingStorFile;

    public SFTPClientWorker(Socket clientSocket, int id) {
        this.id = id;
        this.clientSocket = clientSocket;
        this.transferType = "b";
        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            String welcomeMsg = makeResponse(SFTPServer.getServerProtocol() + " Server", ResponseCode.Success);
            writeToClient(welcomeMsg);
        } catch (Exception e) {
            Utils.logMessage("Could not open streams from client " + id);
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    /**
     * Returns true of the Client has selected a valid User and password.
     */
    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * Returns true if the Client has selected a User (does not imply they are
     * logged in).
     */
    public boolean isUserSelected() {
        return this.selectedUser != null;
    }

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    public void run() {
        while (!isClosed()) {
            try {
                // Get command call from client
                String commandCall = readFromClient();
                // Check which commmand was supplied and perform it
                String commandRes = callCommand(commandCall);
                // Send the result back to the client
                writeToClient(commandRes);
            } catch (Exception e) { // If we fail to communicate with the client, close the connection
                // Utils.Utils.logMessage("Could not read/write to client " + id);
                // e.printStackTrace();
                closeConnection();
            }
        }
    }

    public void closeConnection() {
        try {
            clientSocket.close();
            inFromClient.close();
            outToClient.close();
            Utils.logMessage("Client " + id + " disconnected");
        } catch (Exception e) {
            Utils.logMessage("Failed to close connection to client " + id);
        }
    }

    private String readFromClient() throws Exception {
        return inFromClient.readLine();
    }

    private void writeToClient(String s) throws Exception {
        outToClient.writeBytes(s);
        outToClient.flush();
    }

    private boolean callIsAuthorized(String commandName) {
        return loggedIn || !RESTRICTED_CMDS.contains(commandName);
    }

    private boolean argsAreValid(String commandName, List<String> commandArgs) {
        boolean commandExists = ALL_CMDS.contains(commandName);
        boolean twoArgCmdsOutOfBounds = (TWO_ARG_CMDS.contains(commandName)
                && (commandArgs.size() > 2 || commandArgs.size() < 2));
        boolean oneOrTwoArgCmdsOutOfBounds = (ONE_OR_TWO_ARG_CMDS.contains(commandName)
                && (commandArgs.size() < 1 || commandArgs.size() > 2));
        boolean zeroArgCmdsOutOfBounds = (ZERO_ARG_CMDS.contains(commandName) && commandArgs.size() > 0);
        boolean remainingCmdsOutOfBounds = (!TWO_ARG_CMDS.contains(commandName)
                && !ZERO_ARG_CMDS.contains(commandName)
                && !ONE_OR_TWO_ARG_CMDS.contains(commandName) && (commandArgs.size() != 1));

        return (commandExists && !twoArgCmdsOutOfBounds && !oneOrTwoArgCmdsOutOfBounds &&
                !zeroArgCmdsOutOfBounds && !remainingCmdsOutOfBounds);
    }

    private String getArgError(String commandName) {
        switch (commandName) {
            case "user":
                return "ERROR: Invalid Arguments\nUsage: USER user-id";
            case "acct":
                return "ERROR: Invalid Arguments\nUsage: ACCT account";
            case "pass":
                return "ERROR: Invalid Arguments\nUsage: PASS password";
            case "type":
                return "ERROR: Invalid Arguments\nUsage: TYPE { A | B | C }";
            case "list":
                return "ERROR: Invalid Arguments\nUsage: LIST { F | V } directory-path";
            case "cdir":
                return "ERROR: Invalid Arguments\nUsage: CDIR new-directory";
            case "kill":
                return "ERROR: Invalid Arguments\nUsage: KILL file-spec";
            case "name":
                return "ERROR: Invalid Arguments\nUsage: NAME old-file-spec";
            case "tobe":
                return "ERROR: Invalid Arguments\nUsage: TOBE new-file-spec";
            case "done":
                return "ERROR: Invalid Arguments\nUsage: DONE";
            case "retr":
                return "ERROR: Invalid Arguments\nUsage: RETR file-spec";
            case "send":
                return "ERROR: Invalid Arguments\nUsage: SEND";
            case "stop":
                return "ERROR: Invalid Arguments\nUsage: STOP";
            case "stor":
                return "ERROR: Invalid Arguments\nUsage: STOR { NEW | OLD | APP } file-spec";
            case "size":
                return "ERROR: Invalid Arguments\nUsage: SIZE number-of-bytes-in-file";
            default:
                return "ERROR: Invalid Command\r\nAvailable Commands: \"USER\", \"ACCT\", \"PASS\", \"TYPE\", \"LIST\"," + 
                " \"CDIR\", \"KILL\", \"NAME\", \"TOBE\", \"DONE\", \"RETR\", \"SEND\", \"STOP\", \"STOR\", \"SIZE\"";
        }
    }

    private String callCommand(String commandCall) {
        ArrayList<String> commandArgs = Utils.splitString(commandCall, "\\s+");
        String commandName = commandArgs.remove(0); // first value in call is just the command name

        if (!callIsAuthorized(commandName)) {
            return makeResponse("Please log in first", ResponseCode.Error);
        }
        if (!argsAreValid(commandName, commandArgs)) {
            return makeResponse(getArgError(commandName), ResponseCode.None);
        }

        switch (commandName) {
            case "user":
                return user(commandArgs.get(0));
            case "acct":
                return acct(commandArgs.get(0));
            case "pass":
                return pass(commandArgs.get(0));
            case "type":
                return type(commandArgs.get(0));
            case "list":
                return list(commandArgs);
            case "cdir":
                return cdir(commandArgs.get(0));
            case "kill":
                return kill(commandArgs.get(0));
            case "name":
                return name(commandArgs.get(0));
            case "tobe":
                return tobe(commandArgs.get(0));
            case "done":
                return done();
            case "retr":
                return retr(commandArgs.get(0));
            case "send":
                return send();
            case "stop":
                return stop();
            case "stor":
                return stor(commandArgs.get(0), commandArgs.get(1));
            case "size":
                return size(Integer.valueOf(commandArgs.get(0)));
            default:
                throw new IllegalArgumentException();
        }
    }

    private String user(String userId) {
        selectedUser = FileSystem.getUser(userId);
        if (selectedUser != null) {
            currentDir = selectedUser.getRootDir();
            if (selectedUser.requiresAccount() || selectedUser.requiresPassword()) {
                return makeResponse("User-id valid, send account and password", ResponseCode.Success);
            } else {
                loggedIn = true;
                return makeResponse(selectedUser.getId() + " logged in", ResponseCode.LoggedIn);
            }
        } else {
            return makeResponse("Invalid user-id, try again", ResponseCode.Error);
        }
    }

    private String acct(String accountName) {
        if (!selectedUser.requiresAccount()) {
            return makeResponse("No account required", ResponseCode.Error);
        }
        if (selectedUser.containsAccount(accountName)) {
            selectedAccount = accountName;
            currentDir = selectedUser.getRootDir();
            if (selectedUser.requiresPassword() && !loggedIn) {
                return makeResponse("Account valid, send password", ResponseCode.Success);
            } else {
                loggedIn = true;
                if (pendingDirChange != null) {
                    String loginResponse = makeResponseString("Account valid, logged-in\n", ResponseCode.LoggedIn);
                    loginResponse += cdir(pendingDirChange);
                    pendingDirChange = null;
                    return loginResponse;
                } else {
                    return makeResponse("Account valid, logged-in", ResponseCode.LoggedIn);
                }
            }
        } else {
            selectedAccount = null;
            return makeResponse("Invalid account, try again", ResponseCode.Error);
        }
    }

    private String pass(String password) {
        if (!selectedUser.requiresPassword()) {
            return makeResponse("No password required", ResponseCode.Error);
        }
        if (selectedUser.getPassword().equals(password)) {
            loggedIn = true;
            if (selectedUser.requiresAccount() && selectedAccount == null) {
                return makeResponse("Send account", ResponseCode.Success);
            } else {
                if (pendingDirChange != null) {
                    String loginResponse = makeResponseString("Logged in\n", ResponseCode.LoggedIn);
                    loginResponse += cdir(pendingDirChange);
                    pendingDirChange = null;
                    return loginResponse;
                } else {
                    currentDir = selectedUser.getRootDir();
                    return makeResponse("Logged in", ResponseCode.LoggedIn);
                }
            }
        } else {
            return makeResponse("Wrong password, try again", ResponseCode.Error);
        }
    }

    private String type(String selectedType) {
        switch (selectedType) {
            case "a":
                this.transferType = selectedType;
                return makeResponse("Using Ascii mode", ResponseCode.Success);
            case "b":
                this.transferType = selectedType;
                return makeResponse("Using Binary mode", ResponseCode.Success);
            case "c":
                this.transferType = selectedType;
                return makeResponse("Using Continuous mode", ResponseCode.Success);
            default:
                return makeResponse("Type not valid", ResponseCode.Error);
        }
    }

    private String list(List<String> commandArgs) {
        String selectedListDir = currentDir;
        if (commandArgs.size() > 1) {
            selectedListDir += commandArgs.get(1);
        }
        if (!FileSystem.pathExists(selectedListDir)) {
            return makeResponse("Cant list directory because: " + selectedListDir + " does not exist", ResponseCode.Error);
        } else if (FileSystem.pathIsFile(selectedListDir)) {
            return makeResponse("Cant list directory because: " + selectedListDir + " is not a directory", ResponseCode.Error); 
        }
        switch (commandArgs.get(0)) {
            case "f":
                return makeResponse(FileSystem.readDir(selectedListDir), ResponseCode.Success);
            case "v":
                return makeResponse(FileSystem.readDirVerbose(selectedListDir), ResponseCode.Success);
            default:
                return makeResponse("Argument error", ResponseCode.Error);
        }
    }

    private String cdir(String destDir) {
        // signal the client must select a user before dir can be resolved
        if (!isUserSelected()) {
            return makeResponse("Please select a user first", ResponseCode.Error);
        }
        // resolve selected directory
        String selectedDir;
        if (destDir.equals("/")) {
            selectedDir = selectedUser.getRootDir();
        } else if (destDir.startsWith("/")) {
            selectedDir = selectedUser.getId() + destDir;
        } else {
            selectedDir = Utils.appendIfMissing(currentDir, "/");
            selectedDir += destDir;
        }
        // validate dir can be navigated to
        if (!FileSystem.pathExists(selectedDir)) {
            return makeResponse("Cant connect to directory because: " + selectedDir  + " does not exist", ResponseCode.Error);
        } else if (FileSystem.pathIsFile(selectedDir)) {
            return makeResponse("Cant list directory because: " + selectedDir  + " is not a directory", ResponseCode.Error);
        }
        // signal the client must be logged in before cdir can happen
        if (!isLoggedIn()) {
            pendingDirChange = destDir;
            return makeResponse("Directory exists, send account/password", ResponseCode.Success);
        }
        // change current directory
        currentDir = selectedDir;
        return makeResponse("Changed working dir to " + currentDir, ResponseCode.LoggedIn);
    }

    private String kill(String fileName) {
        String selectedFile = currentDir + fileName;
        if (!FileSystem.pathExists(selectedFile)) {
            return makeResponse("Not deleted because " + selectedFile + " does not exist", ResponseCode.Error);
        }
        FileSystem.deleteFile(selectedFile);
        return makeResponse(selectedFile + " deleted", ResponseCode.Success);
    }

    private String name(String fileName) {
        String selectedFile = currentDir + fileName;
        if (!FileSystem.pathExists(selectedFile)) {
            return makeResponse("Can't find " + selectedFile, ResponseCode.Error);
        }
        pendingFileToRename = selectedFile;
        return makeResponse("File exists", ResponseCode.Success);
    }

    private String tobe(String fileName) {
        String renamedFile = currentDir + fileName;
        if (FileSystem.pathExists(renamedFile)) {
            return makeResponse("File wasn't renamed because " + renamedFile + " already exists", ResponseCode.Error);
        }
        FileSystem.renameFile(pendingFileToRename, renamedFile);
        return makeResponse(pendingFileToRename + " renamed to " + renamedFile, ResponseCode.Success);
    }

    private String done() {
        return makeResponse("Closing connection", ResponseCode.Success);
    }

    private String retr(String fileName) {
        String selectedFile = currentDir + fileName;
        if (!FileSystem.pathExists(selectedFile)) {
            return makeResponse("File doesn't exist", ResponseCode.Error);
        } else if (FileSystem.pathIsDirectory(selectedFile)) {
            return makeResponse("Specifier is not a file", ResponseCode.Error);
        }
        pendingFileToRetrieve = selectedFile;
        return makeResponse(FileSystem.getFileSize(selectedFile) + " bytes will be sent", ResponseCode.Success);
    }

    private String send() {
        String dataToSend = FileSystem.readFile(pendingFileToRetrieve);
        pendingFileToRetrieve = null;
        return makeResponse(dataToSend, ResponseCode.None);
    }

    private String stop() {
        pendingFileToRetrieve = null;
        return makeResponse("File will not be sent", ResponseCode.Success);
    }

    private String stor(String mode, String fileName) {
        if (!fileName.contains(".")) {
            return makeResponse("Specifier is not a file", ResponseCode.Error);
        }
        String selectedFile = Utils.appendIfMissing(currentDir, "/") + fileName;
        switch (mode) {
            case "new":
                if (!FileSystem.pathExists(selectedFile)) {
                    pendingStorFile = new PendingStorFile(selectedFile, mode);
                    return makeResponse("File does not exist, will create new file", ResponseCode.Success);
                }
                selectedFile = FileSystem.getUniqueFileName(fileName, currentDir);
                pendingStorFile = new PendingStorFile(selectedFile, mode);
                return makeResponse("File exists, will create new generation of file", ResponseCode.Success);
            case "old":
                pendingStorFile = new PendingStorFile(selectedFile, mode);
                if (!FileSystem.pathExists(selectedFile)) {
                    return makeResponse("Will create new file", ResponseCode.Success);
                }
                return makeResponse("Will write over old file", ResponseCode.Success);
            case "app":
                if (!FileSystem.pathExists(selectedFile)) {
                    pendingStorFile = new PendingStorFile(selectedFile, "new");
                    return makeResponse("Will create new file", ResponseCode.Success);
                }
                pendingStorFile = new PendingStorFile(selectedFile, mode);
                return makeResponse("Will append to file", ResponseCode.Success);
            default:
                throw new IllegalArgumentException();
        }
    }

    private String size(int maxBytes) {
        try {
            writeToClient(makeResponse("Ok, waiting for file", ResponseCode.Success));
            pendingStorFile.setMaxBytes(maxBytes);
            pendingStorFile.setBytesToWrite(inFromClient.readLine());
            pendingStorFile.setTransferType(transferType);
            FileSystem.writeFile(pendingStorFile);
        } catch (Exception e) {
            // e.printStackTrace();
            return makeResponse("Couldn't save " + e.getLocalizedMessage(), ResponseCode.Error);
        }

        return makeResponse("Saved " + pendingStorFile.getFilePath(), ResponseCode.Success);
    }

    private static String makeResponse(String msg, ResponseCode responseCode) {
		return makeResponseString(msg, responseCode) + '\0';
	}

	private static String makeResponseString(String msg, ResponseCode responseCode) {
		return responseCode.toString() + msg;
	}
}