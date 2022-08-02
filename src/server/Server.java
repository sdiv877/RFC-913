package server;

import java.io.*;
import java.net.*;

public class Server {
	private static final String HOST = "localhost";
	private static final int PORT = 6789;
	private static final String SERVER_PROTOCOL = "RFC 913 SFTP";

	public static void main(String argv[]) throws Exception {
		
		ServerSocket welcomeSocket = new ServerSocket(PORT);
		System.out.println("Server started on " + HOST + " port " + PORT);
		System.out.println("[Protocol: " + SERVER_PROTOCOL + "]");
		
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			outToClient.writeBytes("+" + SERVER_PROTOCOL + " Server" + "\n");
		}
	}
}
