package client;

import java.io.*;
import java.net.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 6789;

    public static void main(String argv[]) throws Exception {
        Socket clientSocket;
        BufferedReader inFromServer;

        try {
            clientSocket = new Socket(HOST, PORT);
            System.out.println("Successfully connected to " + HOST + " on port " + PORT);
            // Open the stream that the server is sending to the client
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Get and print server welcome message
            String welcomeMessage = inFromServer.readLine();
            System.out.println(welcomeMessage);
        } catch (Exception e) {
            System.out.println("Could not connect to " + HOST + " on port " + PORT);
            e.printStackTrace();
            return;
        }
        
        System.out.print("> ");
        // BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        // String sentence = inFromUser.readLine();

        clientSocket.close();
    }
}
