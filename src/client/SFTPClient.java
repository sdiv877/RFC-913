package client;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class SFTPClient {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 6789;

    private Socket clientSocket;
    private BufferedReader inFromUser;
    private DataInputStream inFromServer;
    private DataOutputStream outToServer;
    private List<String> logHistory;

    public SFTPClient() {
        logHistory = new ArrayList<String>();
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
        writeToServer(cmd + "\n");
        String commandRes = readFromServer();
        logMessage(commandRes);

        if (cmd.equals("done")) {
            closeConnection();
        }
    }

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    public List<String> getLogHistory() {
        return logHistory;
    }

    private void connectToServer() {
        try {
            // Attempt to connect to server
            clientSocket = new Socket(HOSTNAME, PORT);
            logMessage("Successfully connected to " + HOSTNAME + " on port " + PORT);
            // Open the stream that the server is sending to the client
            inFromServer = new DataInputStream(clientSocket.getInputStream());
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
            StringBuilder serverRes = new StringBuilder();
            char c = (char)inFromServer.read();
            while (c != '\0') {
                serverRes.append(c);
                c = (char)inFromServer.read();
            }
            return serverRes.toString();
        } catch (Exception e) {
            logMessage("Could not read server response from " + HOSTNAME + ":" + PORT);
            throw e;
        }
    }

    private void writeToServer(String cmd) throws Exception {
        try {
            outToServer.writeBytes(cmd);
        } catch (Exception e) {
            logMessage("Could not write to server " + HOSTNAME + ":" + PORT);
            throw e;
        }
    }

    private void logMessage(String msg) {
        System.out.println(msg);
        logHistory.add(msg);
    }
}
