package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

import utils.Utils;

public class SFTPServer {
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 6789;
	private static final String SERVER_PROTOCOL = "RFC 913 SFTP";

	ServerSocket welcomeSocket;
	private int numActiveClients;
	private int numAttemptedClients;
	private List<User> users;

	public static void main(String argv[]) throws Exception {
		SFTPServer sftpServer = new SFTPServer();
		sftpServer.run();
	}

	public SFTPServer() {
		users = FileSystem.readUsers();
		start();
	}

	public void run() {
		// poll the welcomeSocket
		while (true) {
			Socket incomingClientSocket;
			try {
				// accept incoming connection
				incomingClientSocket = welcomeSocket.accept();
				// instantiate ClientHandler with new connection and run on new thread
				SFTPClientWorker clientHandler = new SFTPClientWorker(incomingClientSocket);
				new Thread(clientHandler).start();
				logMessage("New client connected with id: " + clientHandler.getId() + " (total active: " + numActiveClients + ")");
			} catch (Exception e) {
				logMessage("Could not connect to incoming client socket");
			}
		}
	}
	
	private void start() {
		try {
			welcomeSocket = new ServerSocket(PORT);
			welcomeSocket.setReuseAddress(true);
			logMessage("Server started on " + HOSTNAME + " port " + PORT + " [Protocol: " + SERVER_PROTOCOL + "]");
		} catch (Exception e) {
			logMessage("Could not start server on " + HOSTNAME + " port " + PORT);
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private String validateArgs(String commandName, List<String> commandArgs) {
        if (commandArgs.size() <= 1) return null;

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
	
	private User getUser(String userId) {
		for (User user : users) {
			if (user.getId().equals(userId)) {
				return user;
			}
		}
		return null;
	}

    private static String makeResponse(String msg, ResponseCode responseCode) {
		return responseCode.toString() + msg + '\0';
    }

	private static void logMessage(String msg) {
		System.out.println(msg);
	}

	private class SFTPClientWorker implements Runnable {
		private int id;
		private Socket clientSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;
		private User selectedUser;
		private boolean isLoggedIn;
		private String selectedAccount;

		public SFTPClientWorker(Socket clientSocket) {
			init(clientSocket);
		}

		private void init(Socket clientSocket) {
			numActiveClients++;
			this.id = ++numAttemptedClients;
			this.clientSocket = clientSocket;
			try {
				inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outToClient = new DataOutputStream(clientSocket.getOutputStream());
				String welcomeMsg = makeResponse(SERVER_PROTOCOL + " Server", ResponseCode.Success);
				writeToClient(welcomeMsg);
			} catch (Exception e) {
				logMessage("Could not open streams from client " + id);
				e.printStackTrace();
			}
		}

		public void run() {
			while (!isClosed()) {
				try {
					// Get command call from client
					String commandCall = readFromClient();
					// Check which commmand was supplied and perform action (SFTPServer call)
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

		public int getId() {
			return this.id;
		}

		public boolean isClosed() {
			return clientSocket.isClosed();
		}

		private String readFromClient() throws Exception {
			return inFromClient.readLine();
		}

		private void writeToClient(String s) throws Exception {
			outToClient.writeBytes(s);
			outToClient.flush();
		}

		private String callCommand(String commandCall) {
			ArrayList<String> commandArgs = Utils.splitString(commandCall, "\\s+");
			String commandName = commandArgs.remove(0); // first value in call is just the command name

			String argValidityMessage = validateArgs(commandName, commandArgs);
			if (argValidityMessage != null) {
				return makeResponse(argValidityMessage, ResponseCode.None);
			}

			switch (commandName) {
				case "user":
					selectedUser = getUser(commandArgs.get(0));
					if (selectedUser != null) {
						if (selectedUser.requiresAccount() || selectedUser.requiresPassword()) {
							return makeResponse("User-id valid, send account and password", ResponseCode.Success);
						} else {
							isLoggedIn = true;
							return makeResponse(selectedUser.getId() + " logged in", ResponseCode.LoggedIn);
						}
					} else {
						return makeResponse("Invalid user-id, try again", ResponseCode.Error);
					}
				case "acct":
					if (selectedUser.containsAccount(commandArgs.get(0))) {
						selectedAccount = commandArgs.get(0);
						if (selectedUser.requiresPassword() && !isLoggedIn) {
							return makeResponse("Account valid, send password", ResponseCode.Success);
						} else {
							isLoggedIn = true;
							return makeResponse("Account valid, logged-in", ResponseCode.LoggedIn);
						}
					} else {
						selectedAccount = null;
						return makeResponse("Invalid account, try again", ResponseCode.Error);
					}
				case "pass":
					if (selectedUser.getPassword().equals(commandArgs.get(0))) {
						isLoggedIn = true;
						if (selectedUser.requiresAccount() && selectedAccount == null) {
							return makeResponse("Send account", ResponseCode.Success);
						} else {
							return makeResponse("Logged in", ResponseCode.LoggedIn);
						}
					} else {
						return makeResponse("Wrong password, try again", ResponseCode.Error);
					}
				case "type":
					switch (commandArgs.get(0)) {
						case "a":
							return makeResponse("Using Ascii mode", ResponseCode.Success);
						case "b":
							return makeResponse("Using Binary mode", ResponseCode.Success);
						case "c":
							return makeResponse("Using Continuous mode", ResponseCode.Success);
						default:
							return makeResponse("Type not valid", ResponseCode.Error);
					}
				case "list":
					switch (commandArgs.get(0)) {
						case "f":
							return makeResponse(selectedUser.getId() + "/\n" + FileSystem.readDir("resources/home/user1"), ResponseCode.Success);
						case "v":
							return "v\0";
						default:
							return makeResponse("Argument error", ResponseCode.Error);
					}
				case "done":
					return makeResponse("Closing connection", ResponseCode.Success);
				default:
					return makeResponse("Could not call command", ResponseCode.Error);
			}
		}

		private void closeConnection() {
			try {
				numActiveClients--;
				clientSocket.close();
				inFromClient.close();
				outToClient.close();
				logMessage("Client " + id + " disconnected (total active: " + numActiveClients + ")");
			} catch (Exception e) {
				logMessage("Failed to close connection to client " + id);
			}
		}
	}
}
