package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import fs.FileSystem;
import fs.User;
import utils.Utils;

/**
 * Represents the connection of a given SFTPServer instance to a specific client.
 * Intended for use with a Thread.
 */
public class SFTPClientWorker implements Runnable {
    private static final List<String> ZERO_ARG_CMDS = Arrays.asList("done", "send", "stop");
    private static final List<String> ONE_OR_TWO_ARG_CMDS = Arrays.asList("list");
    private static final List<String> TWO_ARG_CMDS = Arrays.asList("stor");
    private static final List<String> RESTRICTED_CMDS = Arrays.asList("type", "list", "kill", "name",
            "tobe", "retr", "send", "stop", "stor", "size");
    private static final List<String> ALL_CMDS = Arrays.asList("user", "acct", "pass", "type", "list",
            "cdir", "kill", "name", "tobe", "done", "retr", "send", "stop", "stor", "size");

    private int id;
    private Socket clientSocket;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String transferType;
    private User selectedUser;
    private String selectedAccount;
    private boolean passwordProvided;
    private String currentDir;
    private String pendingDirChange;
    private String pendingFileToRename;
    private String pendingFileToRetrieve;
    private PendingStorFile pendingStorFile;

    public SFTPClientWorker(Socket clientSocket, int id, boolean serverAvailable) {
        this.id = id;
        this.clientSocket = clientSocket;
        this.transferType = "b";
        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            if (serverAvailable) {
                // if server is available, send positive welcome message
                writeToClient(makeResponse(SFTPServer.getServerProtocol() + " Server Online", ResponseCode.Success));
            } else {
                // if the server is unavailable, send error message and close connection
                writeToClient(makeResponse(SFTPServer.getServerProtocol() + " Server Unavailable", ResponseCode.Error));
                closeConnection();
            }
        } catch (Exception e) {
            Utils.logMessage("Could not open streams from client " + id);
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    /**
     * @return true of the Client has selected a valid User, account and password.
     */
    public boolean isLoggedIn() {
        return isUserSelected() && isAccountSelected() && isPasswordProvided();
    }

    /**
     * @return true if the Client has selected a User (does not imply they are
     *         logged in).
     */
    public boolean isUserSelected() {
        return this.selectedUser != null;
    }

    public boolean isAccountSelected() {
        return this.selectedAccount != null || !this.selectedUser.requiresAccount();
    }

    public boolean isPasswordProvided() {
        return passwordProvided || !this.selectedUser.requiresPassword();
    }

    public boolean isPendingDirChange() {
        return this.pendingDirChange != null;
    }

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    /**
     * Continuously polls the connection to the client's InputStream, attempts to
     * call the relevant server command, and sends the response back to the client.
     */
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
                // Utils.logMessage("Could not read/write to client " + id);
                // e.printStackTrace();
                closeConnection();
            }
        }
    }

    /**
     * Closes all connections to the client.
     */
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

    /**
     * Clears any information associated with the clients current user session.
     * (selectedUser, currentDir, pendingStorFile etc.)
     */
    private void clearUserState() {
        selectedUser = null;
        selectedAccount = null;
        passwordProvided = false;
        currentDir = null;
        pendingDirChange = null;
        pendingFileToRename = null;
        pendingFileToRetrieve = null;
        pendingStorFile = null;
    }

    /**
     * Polls the InputStream from the client for characters, until
     * a newline is reached. This is a blocking method.
     * 
     * @return the message received from the client
     * @throws Exception if input from the client could not be read
     */
    private String readFromClient() throws Exception {
        return inFromClient.readLine();
    }

    /**
     * Writes a message to the client output buffer and flushes it. To ensure the
     * client is aware when the message is finished, use makeResponse() which
     * appropriately appends a null terminating character to the message.
     * 
     * @throws Exception if the client could not be written to
     */
    private void writeToClient(String s) throws Exception {
        outToClient.writeBytes(s);
        outToClient.flush();
    }

    /**
     * @return true if the user is logged or the command does not required
     *         authentication
     */
    private boolean callIsAuthorized(String commandName) {
        return isLoggedIn() || !RESTRICTED_CMDS.contains(commandName);
    }

    /**
     * Verifies that a command matching commmandName exists, and that the number of
     * args provided is within bounds for that command.
     * 
     * @return whether the command and it's number of args is valid
     */
    private boolean callIsValid(String commandName, int numArgs) {
        boolean commandExists = ALL_CMDS.contains(commandName);
        boolean twoArgCmdsOutOfBounds = (TWO_ARG_CMDS.contains(commandName) && (numArgs > 2 || numArgs < 2));
        boolean oneOrTwoArgCmdsOutOfBounds = (ONE_OR_TWO_ARG_CMDS.contains(commandName)
                && (numArgs < 1 || numArgs > 2));
        boolean zeroArgCmdsOutOfBounds = (ZERO_ARG_CMDS.contains(commandName) && numArgs > 0);
        boolean remainingCmdsOutOfBounds = (!TWO_ARG_CMDS.contains(commandName) && !ZERO_ARG_CMDS.contains(commandName)
                && !ONE_OR_TWO_ARG_CMDS.contains(commandName) && (numArgs != 1));

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
                return "ERROR: Invalid Command\r\nAvailable Commands: \"USER\", \"ACCT\", \"PASS\", \"TYPE\", \"LIST\","
                        + " \"CDIR\", \"KILL\", \"NAME\", \"TOBE\", \"DONE\", \"RETR\", \"SEND\", \"STOP\", \"STOR\", \"SIZE\"";
        }
    }

    /**
     * Calls a string that represents a server command call and returns the result
     * of the call. May also return errors from lack of authorization or invalid
     * arguments.
     * 
     * @param commandCall
     * @return results of the command call or a meaningful error
     */
    private String callCommand(String commandCall) {
        ArrayList<String> commandArgs = Utils.splitString(commandCall, "\\s+");
        String commandName = commandArgs.remove(0); // first value in call is just the command name

        if (!callIsAuthorized(commandName)) {
            return makeResponse("Please log in first", ResponseCode.Error);
        }
        if (!callIsValid(commandName, commandArgs.size())) {
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
        User foundUser = FileSystem.getUser(userId);
        if (foundUser != null) {
            // clear state on user switch to stop new user from accessing old information
            clearUserState();
            // update selectedUser and cdir to the root dir of the new user
            selectedUser = foundUser;
            currentDir = selectedUser.getRootDir();
            // distinguish between accounts that need accounts/passwords and ones that don't
            if (selectedUser.requiresAccount() || selectedUser.requiresPassword()) {
                return makeResponse("User-id valid, send account and password", ResponseCode.Success);
            } else {
                return makeResponse(selectedUser.getId() + " logged in", ResponseCode.LoggedIn);
            }
        } else {
            return makeResponse("Invalid user-id, try again", ResponseCode.Error);
        }
    }

    private String acct(String accountName) {
        // guard against trying to select an account without specifying a user
        // or trying to select an account when a user doesn't have any
        if (!isUserSelected()) {
            return makeResponse("Please select a user first", ResponseCode.Error);
        } else if (!selectedUser.requiresAccount()) {
            return makeResponse("User is not associated with any accounts", ResponseCode.Error);
        }
        if (selectedUser.containsAccount(accountName)) {
            // update selectedAccount
            selectedAccount = accountName;
            // handle users that need a password along with their account
            if (selectedUser.requiresPassword() && !isLoggedIn()) {
                return makeResponse("Account valid, send password", ResponseCode.Success);
            } else {
                // if there were any dir changes that took place prior to login
                // handle them now
                if (isPendingDirChange()) {
                    String loginResponse = makeResponseString("Account valid, logged-in\n", ResponseCode.LoggedIn);
                    loginResponse += cdir(pendingDirChange);
                    pendingDirChange = null;
                    return loginResponse;
                } else { // otherwise send normal login acknowledgement
                    return makeResponse("Account valid, logged-in", ResponseCode.LoggedIn);
                }
            }
        } else {
            selectedAccount = null;
            return makeResponse("Invalid account, try again", ResponseCode.Error);
        }
    }

    private String pass(String password) {
        // guard against trying to use a password when no selectedUser, or when
        // selectedUser has no pasword
        if (!isUserSelected()) {
            return makeResponse("Please select a user first", ResponseCode.Error);
        } else if (!selectedUser.requiresPassword()) {
            return makeResponse("No password required", ResponseCode.Error);
        }
        if (selectedUser.getPassword().equals(password)) {
            // signal the user has provided a correct password
            passwordProvided = true;
            // handle users that need an account along with their password
            if (selectedUser.requiresAccount() && !isAccountSelected()) {
                return makeResponse("Send account", ResponseCode.Success);
            } else {
                // if there were any dir changes that took place prior to login
                // handle them now
                if (isPendingDirChange()) {
                    String loginResponse = makeResponseString("Logged in\n", ResponseCode.LoggedIn);
                    loginResponse += cdir(pendingDirChange);
                    pendingDirChange = null;
                    return loginResponse;
                } else { // otherwise send normal login acknowledgement
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
        // append the extra arg to list a subdir, if given
        if (commandArgs.size() > 1) {
            selectedListDir += commandArgs.get(1);
        }
        if (!FileSystem.pathExists(selectedListDir)) {
            return makeResponse("Can't list directory because: " + selectedListDir + " does not exist",
                    ResponseCode.Error);
        } else if (FileSystem.pathIsFile(selectedListDir)) {
            return makeResponse("Can't list directory because: " + selectedListDir + " is not a directory",
                    ResponseCode.Error);
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
            return makeResponse("Can't connect to directory because: " + selectedDir  + " does not exist", ResponseCode.Error);
        } else if (FileSystem.pathIsFile(selectedDir)) {
            return makeResponse("Can't list directory because: " + selectedDir  + " is not a directory", ResponseCode.Error);
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

    private String kill(String pathName) {
        String selectedPath = Utils.appendIfMissing(currentDir, "/") + pathName;
        if (!FileSystem.pathExists(selectedPath)) {
            return makeResponse("Not deleted because " + selectedPath + " does not exist", ResponseCode.Error);
        } else if (FileSystem.pathIsDirectory(selectedPath)) {
            return makeResponse("Not deleted because " + selectedPath + " is a directory", ResponseCode.Error);
        }
        FileSystem.deletePath(selectedPath);
        return makeResponse(selectedPath + " deleted", ResponseCode.Success);
    }

    private String name(String fileName) {
        String selectedFile = Utils.appendIfMissing(currentDir, "/") + fileName;
        if (!FileSystem.pathExists(selectedFile)) {
            return makeResponse("Can't find " + selectedFile, ResponseCode.Error);
        }
        pendingFileToRename = selectedFile;
        return makeResponse("File exists", ResponseCode.Success);
    }

    private String tobe(String fileName) {
        if (pendingFileToRename == null) {
            return makeResponse("Please select a file to rename first", ResponseCode.Error);
        }
        String renamedFile = Utils.appendIfMissing(currentDir, "/") + fileName;
        if (FileSystem.pathExists(renamedFile)) {
            pendingFileToRename = null;
            return makeResponse("File wasn't renamed because " + renamedFile + " already exists", ResponseCode.Error);
        }
        FileSystem.renameFile(pendingFileToRename, renamedFile);
        String tempPendingFileToRename = pendingFileToRename;
        pendingFileToRename = null;
        return makeResponse(tempPendingFileToRename + " renamed to " + renamedFile, ResponseCode.Success);
    }

    private String done() {
        return makeResponse("Closing connection", ResponseCode.Success);
    }

    private String retr(String fileName) {
        String selectedFile = Utils.appendIfMissing(currentDir, "/") + fileName;
        if (!FileSystem.pathExists(selectedFile)) {
            pendingFileToRetrieve = null;
            return makeResponse("File doesn't exist", ResponseCode.Error);
        } else if (FileSystem.pathIsDirectory(selectedFile)) {
            pendingFileToRetrieve = null;
            return makeResponse("Specifier is not a file", ResponseCode.Error);
        }
        pendingFileToRetrieve = selectedFile;
        return makeResponse(FileSystem.getFileTransferSize(selectedFile) + " bytes will be sent", ResponseCode.Success);
    }

    private String send() {
        if (pendingFileToRetrieve == null) {
            return makeResponse("Please select a file to retrieve first", ResponseCode.Error);
        }
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
            pendingStorFile = null;
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
            if (pendingStorFile == null) {
                return makeResponse("Please select a file name to store at first", ResponseCode.Error);
            }
            writeToClient(makeResponse("Ok, waiting for file", ResponseCode.Success));
            pendingStorFile.setMaxBytes(maxBytes);
            pendingStorFile.setBytesToWrite(inFromClient.readLine());
            pendingStorFile.setTransferType(transferType);
            FileSystem.writeFile(pendingStorFile);
        } catch (Exception e) {
            // e.printStackTrace();
            pendingStorFile = null;
            return makeResponse("Couldn't save " + e.getLocalizedMessage(), ResponseCode.Error);
        }
        String savedFile = pendingStorFile.getFilePath();
        pendingStorFile = null;
        return makeResponse("Saved " + savedFile, ResponseCode.Success);
    }

    private static String makeResponse(String msg, ResponseCode responseCode) {
        return makeResponseString(msg, responseCode) + '\0';
    }

    private static String makeResponseString(String msg, ResponseCode responseCode) {
        return responseCode.toString() + msg;
    }
}
