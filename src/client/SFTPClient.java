package client;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

import server.Utils;

public class SFTPClient {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 6789;

    private Socket clientSocket;
    private BufferedReader inFromUser;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private List<String> serverResHistory;

    public SFTPClient() {
        serverResHistory = new ArrayList<String>();
        // Attempt to connect to user keyboard stream/server
        connectToKeyboardStream();
        connectToServer();
    }

    public static void main(String argv[]) {
        SFTPClient sftpClient = new SFTPClient();
        sftpClient.run();
    }

    public void run() {
        while (!isClosed()) {
            try {
                String cmd = readUserInput();
                evalCommand(cmd);
            } catch (Exception e) {
                // e.printStackTrace();
                closeConnection();
            }
        }
    }

    public void evalCommand(String cmd) throws Exception {
        boolean writeWasSuccessful = writeToServer(cmd + "\n");
        if (writeWasSuccessful) {
            String commandRes = readFromServer();
            logMessage(commandRes);
        }

        if (cmd.equals("done")) {
            closeConnection();
        }
    }

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    public List<String> getServerResHistory() {
        return serverResHistory;
    }

    private void connectToServer() {
        try {
            // Attempt to connect to server
            clientSocket = new Socket(HOSTNAME, PORT);
            logMessage("Successfully connected to " + HOSTNAME + " on port " + PORT);
            // Open the stream that the server is sending to the client
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Create stream to send input to server
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            // Get and print server welcome message
            String welcomeMessage = readFromServer();
            logMessage(welcomeMessage);
        } catch (Exception e) {
            logMessage("Could not connect to " + HOSTNAME + ":" + PORT);
            e.printStackTrace();
            closeConnection();
        }
    }

    private void connectToKeyboardStream() {
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
    }

    private void closeConnection() {
        try {
            clientSocket.close();
            // inFromUser.close();
            inFromServer.close();
            outToServer.close();
        } catch (Exception e) {
            logMessage("Could not close connection to " + HOSTNAME + ":" + PORT + " gracefully");
        }
    }

    private String readUserInput() throws Exception {
        try {
            System.out.print("> ");
            return inFromUser.readLine();
        } catch (Exception e) {
            logMessage("Could not read user keyboard input");
            throw e;
        }
    }

    private String readFromServer() throws Exception {
        try {
            String serverRes = inFromServer.readLine();
            if (serverRes == null) throw new NullPointerException();
            return serverRes;
        } catch (Exception e) {
            logMessage("Could not read server response from " + HOSTNAME + ":" + PORT);
            throw e;
        }
    }

    private boolean writeToServer(String cmd) throws Exception {
        String argValidityMsg = validateArgs(cmd);
        if (argValidityMsg != null) {
            logMessage(argValidityMsg);
            return false;
        }
        try {
            outToServer.writeBytes(cmd);
            return true;
        } catch (Exception e) {
            logMessage("Could not write to server " + HOSTNAME + ":" + PORT);
            throw e;
        }
    }

    private String validateArgs(String cmd) {
        List<String> commandArgs = Utils.splitString(cmd, "\\s+");
        String commandName = commandArgs.remove(0); // first value in call is just the command name

        if (commandArgs.size() <= 1)
            return null;
        switch (commandName) {
            case "user":
                return "ERROR: Invalid Arguments\nUsage: USER user-id";
            case "acct":
                return "ERROR: Invalid Arguments\nUsage: ACCT account";
            case "pass":
                return "ERROR: Invalid Arguments\nUsage: PASS password";
            case "type":
                return "ERROR: Invalid Arguments\nUsage: TYPE { A | B | C }";
            default:
                return null;
        }
    }

    private void logMessage(String msg) {
        System.out.println(msg);
        serverResHistory.add(msg);
    }
}
