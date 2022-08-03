package server;

import java.io.*;
import java.net.*;
import java.util.List;

public class SFTPServer {
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 6789;
	private static final String SERVER_PROTOCOL = "RFC 913 SFTP";

	private static List<User> users = Utils.readUserDb();
	private static boolean isRunning = true;

	public static void main(String argv[]) throws Exception {
		ServerSocket welcomeSocket = new ServerSocket(PORT);
		System.out.println("Server started on " + HOSTNAME + " port " + PORT + " [Protocol: " + SERVER_PROTOCOL + "]");

		// Listen for connection and send back welcome msg
		Socket connectionSocket = welcomeSocket.accept();
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		outToClient.writeBytes(makeResponse(SERVER_PROTOCOL + " Server", ResponseCode.Success));

		while (isRunning) {
			// Get command call from client
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			String commandCall = inFromClient.readLine();
			// Check which commmand was supplied and perform action
			String commandRes = callCommand(commandCall);
			// Return command result to client
			outToClient.writeBytes(commandRes);
		}
	}

	private static String callCommand(String commandCall) {
		if (commandCall == null) return "";

		List<String> commandArgs = Utils.splitString(commandCall, "\\s+");
		String commandName = commandArgs.remove(0); // first value in call is just the command name

		switch (commandName) {
			case "user":
				if (userExists(commandArgs.get(0))) {
					return makeResponse(commandArgs.get(0) + " logged in", ResponseCode.LoggedIn);
				} else {
					return makeResponse("Invalid user-id, try again", ResponseCode.Error);
				}
			case "done":
				return makeResponse("Closing connection", ResponseCode.Success);
			default:
				return makeResponse("Could not call command", ResponseCode.Error);
		}
	}

	private static String makeResponse(String msg, ResponseCode responseCode) {
		return responseCode.toString() + msg + "\n";
	}

	private static boolean userExists(String userId) {
		for (User user : users) {
			if (user.getId().equals(userId)) {
				return true;
			}
		}
		return false;
	}
}
