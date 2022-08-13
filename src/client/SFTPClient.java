package client;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

import utils.Utils;

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

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    public List<String> getLogHistory() {
        return logHistory;
    }

    /**
     * Runs the client, continuously accepting input, sending it to the server and
     * awaiting responses until an exception is thrown.
     */
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

    /**
     * Sends a command call to the server, awaits a response, and then logs it.
     * 
     * @param cmd the string form of the desired command call
     * @throws Exception if the server could not be written to or read from
     */
    public void evalCommand(String cmd) throws Exception {
        writeToServer(cmd);
        String commandRes = readFromServer();
        logMessage(commandRes);

        if (cmd.equals("done")) {
            closeConnection();
        }
    }

    /**
     * Attempts to connect to the prescribed server, including opening input and
     * output streams to it. Closes any connections that were made and quits the
     * program if the connection fails.
     */
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
            System.exit(0);
        }
    }

    private void connectToKeyboardStream() {
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Closes connections to the server, and the client socket.
     */
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

    /**
     * Polls the user's keyboard InputStream until a newline is reached.
     * 
     * @return the input received from the user
     * @throws Exception if input from the user could not be read
     */
    private String readUserInput() throws Exception {
        try {
            System.out.print("> ");
            return inFromUser.readLine();
        } catch (Exception e) {
            logMessage("Could not read user keyboard input");
            throw e;
        }
    }

    /**
     * Polls the DataInputStream from the connectected server for characters, until
     * a terminating '\0' is reached. This is a blocking method.
     * 
     * @return the message received from the server
     * @throws Exception if input from the server could not be read
     */
    private String readFromServer() throws Exception {
        try {
            StringBuilder serverRes = new StringBuilder();
            char c = (char) inFromServer.read();
            while (c != '\0') {
                serverRes.append(c);
                c = (char) inFromServer.read();
            }
            return serverRes.toString();
        } catch (Exception e) {
            logMessage("Could not read server response from " + HOSTNAME + ":" + PORT);
            throw e;
        }
    }

    /**
     * Sends a message to the currently connected server. A terminating newline is
     * appended to the message if it is missing.
     * 
     * @throws Exception if the server could not be written to
     */
    private void writeToServer(String msg) throws Exception {
        try {
            msg = Utils.appendIfMissing(msg, "\n");
            outToServer.writeBytes(msg);
        } catch (Exception e) {
            logMessage("Could not write to server " + HOSTNAME + ":" + PORT);
            throw e;
        }
    }

    private void logMessage(String msg) {
        logHistory.add(msg);
        Utils.logMessage(msg);
    }
}
