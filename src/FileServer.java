import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

public class FileServer {
	public static int PORT = 5555;
	public static Statistics statistics;

	public static void printStatistics() {
		System.out.println("---------------------Statistics---------------------");
		System.out.println("No.of users active:-" + statistics.getActiveUers());
		System.out.println("No.of files uploaded:-" + statistics.getFilesUploaded());
		System.out.println("Uploaded data in bytes:-" + statistics.getDataUploaded());
		System.out.println("----------------------------------------------------");
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Socket socket = null;
		BufferedReader reader = null;
		String name = null;
		ClientDetail detail = null;
		PrintWriter printWriter = null;
		// start server
		System.out.println("Starting server");
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server started");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// creating client details
		statistics = new Statistics();
		LinkedHashMap<String, ClientDetail> clientDetails = statistics.getClientDetails();

		// wait for connection
		while (true) {
			try {
				System.out.println("Waiting for connection");
				socket = serverSocket.accept();
				System.out.println("Client connected");
				statistics.addActiveUers();
				printStatistics();
				// receiving name
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// System.out.println("Receiving name");
				name = reader.readLine();
				System.out.println("Client " + name + " is connected");

				// sending ack to client
				printWriter = new PrintWriter(socket.getOutputStream());
				printWriter.println("ACK");
				printWriter.flush();

				// checking for existing details
				if (clientDetails.containsKey(name)) {

					// existing user
					System.out.println("Existing user");

					// replacing socket
					detail = clientDetails.get(name);
					detail.setSocket(socket);
					detail.setOnline(true);
					System.out.println("Socket replaced");
				} else {

					// new user
					System.out.println("New user");

					// creating details
					detail = new ClientDetail(name, socket, true);
					clientDetails.put(name, detail);
					System.out.println("Details added");
				}
				// stating thread
				System.out.println("Starting thread to handle client");

				new ClientHandler(detail).start();
				
				System.out.println("Thread started");

			} catch (IOException e) {
				System.out.println("Client is disconnected");
				statistics.removeActiveUers();
//				e.printStackTrace();
			}
		}

	}

}
