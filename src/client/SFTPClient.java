package client;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class SFTPClient {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 6789;

    private boolean isConnected;
    private Socket clientSocket;
    private BufferedReader inFromUser;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private List<String> serverResHistory;

    public SFTPClient() {
        serverResHistory = new ArrayList<String>();
        // Open stream from user's keyboard to client
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        // Attempt to connect to server
        isConnected = connectToServer();
    }

    public static void main(String argv[]) {
        SFTPClient sftpClient = new SFTPClient();
        sftpClient.run();
    }

    public void run() {
        while (isConnected) {
            String cmd = readUserInput();
            evalCommand(cmd);
        }
    }

    public void evalCommand(String cmd) {
        sendToServer(cmd + "\n");
        String commandRes = readFromServer();
        logMessage(commandRes);

        if (cmd.equals("done")) {
            this.closeConnection();
        }
    }
    
    public void closeConnection() {
        try {
            // TODO: Close on server side as well
            isConnected = false;
            inFromUser.close();
            inFromServer.close();
            outToServer.close();
            clientSocket.close();
        } catch (Exception e) {
            logMessage("Could not close connection to " + HOSTNAME + ":" + PORT);
        }
    }
    
    public List<String> getServerResHistory() {
        return serverResHistory;
    }

    private boolean connectToServer() {
        try {
            // Attempt to connect to server
            clientSocket = new Socket(HOSTNAME, PORT);
            logMessage("Successfully connected to " + HOSTNAME + " on port " + PORT);
            // Open the stream that the server is sending to the client
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Get and print server welcome message
            String welcomeMessage = readFromServer();
            logMessage(welcomeMessage);
            // Create stream to send input to server
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            return true;
        } catch (Exception e) {
            logMessage("Could not connect to " + HOSTNAME + " on port " + PORT);
            e.printStackTrace();
            System.exit(0);
            return false;
        }
    }

    private String readUserInput() {
        try {
            System.out.print("> ");
            return inFromUser.readLine();
        } catch (Exception e) {
            logMessage("Could not read user input");
            e.printStackTrace();
            System.exit(0);
        }
        return "";
    }

    private void sendToServer(String s) {
        try {
            outToServer.writeBytes(s);
        } catch (Exception e) {
            logMessage("Could not write " + s + "to " + HOSTNAME + ":" + PORT);
            e.printStackTrace();
        }
    }

    private String readFromServer() {
        try {
            return inFromServer.readLine();
        } catch (Exception e) {
            logMessage("Could not read server response from " + HOSTNAME + ":" + PORT);
            e.printStackTrace();
        }
        return "";
    }

    private void logMessage(String msg) {
        System.out.println(msg);
        serverResHistory.add(msg);
    }
}
