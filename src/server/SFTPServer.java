package server;

import java.net.*;

import utils.Utils;

public class SFTPServer {
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 6789;
	private static final String SERVER_PROTOCOL = "RFC 913 SFTP";

	ServerSocket welcomeSocket;
	private int numAttemptedClients;

	public static void main(String argv[]) throws Exception {
		SFTPServer sftpServer = new SFTPServer();
		sftpServer.run();
	}

	public SFTPServer() {
		try {
			welcomeSocket = new ServerSocket(PORT);
			welcomeSocket.setReuseAddress(true);
			Utils.logMessage("Server started on " + HOSTNAME + " port " + PORT + " [Protocol: " + SERVER_PROTOCOL + "]");
		} catch (Exception e) {
			Utils.logMessage("Could not start server on " + HOSTNAME + " port " + PORT);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static String getServerProtocol() {
		return SERVER_PROTOCOL;
	}

	public void run() {
		// poll the welcomeSocket
		while (true) {
			try {
				// accept incoming connection
				Socket incomingClientSocket = welcomeSocket.accept();
				numAttemptedClients++;
				// instantiate ClientHandler with new connection and run on new thread
				SFTPClientWorker clientHandler = new SFTPClientWorker(incomingClientSocket, numAttemptedClients);
				new Thread(clientHandler).start();
				Utils.logMessage("New client connected with id: " + clientHandler.getId());
			} catch (Exception e) {
				Utils.logMessage("Could not connect to incoming client socket");
			}
		}
	}
}
