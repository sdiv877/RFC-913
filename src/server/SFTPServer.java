package server;

import java.io.*;
import java.net.*;
import java.util.List;

public class SFTPServer {
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 6789;
	private static final String SERVER_PROTOCOL = "RFC 913 SFTP";
	private static final String WELCOME_MSG = Utils.makeResponse(SERVER_PROTOCOL + " Server", ResponseCode.Success);
	private static final String CLOSED_MSG = Utils.makeResponse("Closing connection", ResponseCode.Success);

	ServerSocket welcomeSocket;
	private int numActiveClients;
	private int numAttemptedClients;
	private List<User> users = Utils.readUserDb();

	public static void main(String argv[]) throws Exception {
		SFTPServer sftpServer = new SFTPServer();
		sftpServer.run();
	}

	public SFTPServer() {
		init();
	}

	private void init() {
		try {
			welcomeSocket = new ServerSocket(PORT);
			welcomeSocket.setReuseAddress(true);
			Utils.logMessage("Server started on " + HOSTNAME + " port " + PORT + " [Protocol: " + SERVER_PROTOCOL + "]");
		} catch (Exception e) {
			Utils.logMessage("Could not start server on " + HOSTNAME + " port " + PORT);
			e.printStackTrace();
		}
	}

	public void run() {
		// poll the welcomeSocket
		while (true) {
			Socket incomingClientSocket;
			try {
				// accept incoming connection
				incomingClientSocket = welcomeSocket.accept();
				// instantiate ClientHandler with new connection and run on new thread
				SFTPClientHandler clientHandler = new SFTPClientHandler(incomingClientSocket);
				new Thread(clientHandler).start();
				Utils.logMessage("New client connected with id: " + clientHandler.getId() + " (total: " + numActiveClients + ")");
			} catch (Exception e) {
				Utils.logMessage("Could not connect to incoming client socket");
			}
		}
	}

	private String callCommand(String commandCall) {
		List<String> commandArgs = Utils.splitString(commandCall, "\\s+");
		String commandName = commandArgs.remove(0); // first value in call is just the command name

		switch (commandName) {
			case "user":
				if (userExists(commandArgs.get(0))) {
					return Utils.makeResponse(commandArgs.get(0) + " logged in", ResponseCode.LoggedIn);
				} else {
					return Utils.makeResponse("Invalid user-id, try again", ResponseCode.Error);
				}
			case "done":
				return Utils.makeResponse("Closing connection", ResponseCode.Success);
			default:
				return Utils.makeResponse("Could not call command", ResponseCode.Error);
		}
	}

	private boolean userExists(String userId) {
		for (User user : users) {
			if (user.getId().equals(userId)) {
				return true;
			}
		}
		return false;
	}

	private class SFTPClientHandler implements Runnable {
		private int id;
		private Socket clientSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;

		public SFTPClientHandler(Socket clientSocket) {
			init(clientSocket);
		}

		private void init(Socket clientSocket) {
			numActiveClients++;
			this.id = ++numAttemptedClients;
			this.clientSocket = clientSocket;
			try {
				inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outToClient = new DataOutputStream(clientSocket.getOutputStream());
				writeToClient(WELCOME_MSG);
			} catch (Exception e) {
				Utils.logMessage("Could not open streams to client " + id);
				e.printStackTrace();
			}
		}

		public void run() {
			while (!isClosed()) {
				// Get command call from client
				String commandCall = readFromClient();
				// Check which commmand was supplied and perform action (SFTPServer call)
				if (commandCall != null && !isClosed()) {
					// Get result of command and return it to client
					String commandRes = callCommand(commandCall);
					writeToClient(commandRes);
					// Close this client handler if it was requested
					if (commandRes.equals(CLOSED_MSG)) {
						closeConnection();
					}
				}
			}
		}

		public int getId() {
			return this.id;
		}

		public boolean isClosed() {
			return clientSocket.isClosed();
		}

		private String readFromClient() {
			try {
				return inFromClient.readLine();
			} catch (Exception e) {
				// Utils.logMessage("Could not read response from client " + id);
				// e.printStackTrace();
				closeConnection();
			}
			return "";
		}

		private void writeToClient(String s) {
			try {
				outToClient.writeBytes(s);
			} catch (Exception e) {
				// Utils.logMessage("Could not write \"" + s + "\" to client " + id);
				// e.printStackTrace();
				closeConnection();
			}
		}

		private void closeConnection() {
			try {
				numActiveClients--;
				clientSocket.close();
				inFromClient.close();
				outToClient.close();
				Utils.logMessage("Client " + id + " disconnected (total: " + numActiveClients + ")");
			} catch (Exception e) {
				Utils.logMessage("Failed to close connection to client " + id);
			}
		}
	}
}
