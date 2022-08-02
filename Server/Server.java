import java.io.*;
import java.net.*;

class Server {
	private static final int PORT = 6789;

	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;

		ServerSocket welcomeSocket = new ServerSocket(PORT);
		System.out.println("Server started on port " + PORT);

		while (true) {
			Socket connectionSocket = welcomeSocket.accept();

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			clientSentence = inFromClient.readLine();

			capitalizedSentence = clientSentence.toUpperCase() + '\n';

			outToClient.writeBytes(capitalizedSentence);
		}
	}
}
