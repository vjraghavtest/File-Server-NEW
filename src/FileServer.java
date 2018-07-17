import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.sun.istack.internal.Nullable;

public class FileServer {
	public static int PORT = 5555;
	public static Statistics statistics;
	public static FileHandler fileHandler = null;
	public static ServerSocket serverSocket = null;
	static boolean exit = false;

	public static void disconnectClient(@Nullable ClientDetail detail) {
		if (exit)
			return;
		if (detail != null)
			System.out.println("Client " + detail.getName() + " is disconnected");
		else
			System.out.println("Client is disconnected");
		statistics.removeActiveUers();
		printStatistics();
	}

	public static void printStatistics() {
		System.out.println("---------------------Statistics---------------------");
		System.out.println("No.of users active:-" + statistics.getActiveUers());
		System.out.println("No.of files uploaded:-" + statistics.getFilesUploaded());
		System.out.println("Uploaded data in bytes:-" + statistics.getDataUploaded());
		System.out.println("----------------------------------------------------");
	}

	static void prepareFile(String path) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(new File(path), true);
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		bufferedOutputStream.write(
				"-----------------------------------------------------------------------------------\n".getBytes());
		bufferedOutputStream.close();
	}

	static void endServer() throws Exception {
		exit = true;
		serverSocket.close();
		for (ClientDetail detail : statistics.getClientDetails().values()) {
			detail.getSocket().close();
		}
	}

	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		Logger log = null;
		try {
			String path = "C:\\Users\\Administrator\\Desktop\\"
					+ new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) + "-server-log.log";
			prepareFile(path);
			fileHandler = new FileHandler(path, true);
			fileHandler.setFormatter(new SimpleFormatter());
			log = Logger.getLogger(FileServer.class.getName());
			log.addHandler(fileHandler);
			log.setLevel(Level.ALL);
		} catch (SecurityException | IOException e1) {
			e1.printStackTrace();
		}
		// start server
		System.out.println("Starting server");
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server started");
			log.fine("Server started");

			// console handler
			log.info("Starting thread for console reader");
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.start();
			log.fine("Console handler Thread " + consoleHandler.getName() + " started");

		} catch (IOException e) {
			System.out.println("Error while starting server.Please close other pprogram using PORT " + PORT);
			log.warning("Error while starting server.Please close other pprogram using PORT " + PORT);
		}

		// creating client details
		statistics = new Statistics();
		LinkedHashMap<String, ClientDetail> clientDetails = statistics.getClientDetails();

		// wait for connection
		while (!exit && serverSocket != null) {
			try {
				// System.out.println("Waiting for connection");
				log.info("Waiting for connection");
				Socket socket = serverSocket.accept();
				System.out.println("Client connected");
				log.info("Client connected " + socket.toString());
				
				statistics.addActiveUers();
				printStatistics();

				// receiving name
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				try {
					socket.setSoTimeout(30000);
					log.info("Reading name");
					String name = reader.readLine();
					log.fine("Name readed Name-" + name);
					System.out.println("Client " + name + " is connected");
					socket.setSoTimeout(0);
					// sending ack to client
					log.info("Sending ACK for name to " + socket.toString());
					PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
					printWriter.println("ACK");
					printWriter.flush();
					log.fine("ACK sent to " + socket.toString());

					// checking for existing details
					log.info("Checking for existing details");
					ClientDetail detail = null;
					if (clientDetails.containsKey(name)) {
						log.info("Existing details found " + socket.toString());
						// existing user
						// replacing socket
						log.info("Replacing details");
						detail = clientDetails.get(name);
						detail.setSocket(socket);
						detail.setOnline(true);
						log.fine("Socket replaced " + socket.toString());
					} else {

						// new user
						log.info("New user " + socket.toString());
						// creating details
						detail = new ClientDetail(name, socket, true);
						clientDetails.put(name, detail);
						log.fine("Details added");
					}
					// stating thread
					log.info("Thread starting for " + socket.toString());
					ClientHandler clientHandler = new ClientHandler(detail);
					clientHandler.start();
					log.fine("Thread started for client " + name + " " + socket.toString() + " Thread name "
							+ clientHandler.getName());
				} catch (Exception e) {
					log.severe(e.getMessage());
					disconnectClient(null);
					continue;
				}

			} catch (IOException e) {
				log.severe(e.getMessage());
				disconnectClient(null);
			}
		}

	}

}
