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

public class FileServer {
	public static int PORT = 5555;
	public static Statistics statistics;
	public static FileHandler fileHandler = null;
	public static ServerSocket serverSocket = null;
	static boolean exit = false;

	/**
	 * Print message on console as "Client user is disconnected" ,reduce the no.
	 * of active users by 1 and setOnline to false to that client details and
	 * call printStatistics method
	 * 
	 * @param detail
	 *            ClientDetail object holds connected client information
	 */
	public static void disconnectClient(ClientDetail detail) {
		if (exit)
			return;

		// Print client disconnected message
		if (detail != null) {
			System.out.println("Client " + detail.getName() + " is disconnected");
			detail.setOnline(false);
		} else
			System.out.println("Client is disconnected");

		// reduce the count of active users by 1 and print statistics
		statistics.removeActiveUers();
		printStatistics();
	}

	/**
	 * Return path of folder server located in user home directory depend on
	 * operating system.If folder not exists then creates the folder
	 * 
	 * @return String denoting absolute path to server folder
	 */
	public static String getHomeDir() {
		String home = System.getProperty("user.home") + System.getProperty("file.separator") + "Server"
				+ System.getProperty("file.separator");
		if (!new File(home).exists())
			new File(home).mkdirs();
		return home;
	}

	/**
	 * Print no.of active users,no.of files uploaded and data transfered from
	 * server started in this session
	 */
	public static void printStatistics() {
		System.out.println("---------------------Statistics---------------------");
		System.out.println("No.of users active:-" + statistics.getActiveUers());
		System.out.println("No.of files uploaded:-" + statistics.getFilesUploaded());
		System.out.println("Uploaded data in bytes:-" + statistics.getDataUploaded());
		System.out.println("----------------------------------------------------");
	}

	/**
	 * Print an line in the log file to distinguise server startup in log file
	 * 
	 * @param path
	 *            Path to the log file
	 * @throws IOException
	 *             Throws when IOException raised while opening or writing into
	 *             file
	 */
	static void prepareFile(String path) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(new File(path), true);
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		bufferedOutputStream.write(
				"-----------------------------------------------------------------------------------\n".getBytes());
		bufferedOutputStream.close();
	}

	/**
	 * Disconnect all the client connected to server by closing all connected
	 * socket and by setting boolean exit variable to true.This prevent all the
	 * loops from running
	 * 
	 */
	static void endServer() throws Exception {
		exit = true;
		serverSocket.close();
		for (ClientDetail detail : statistics.getClientDetails().values()) {
			detail.getSocket().close();
		}
	}

	/**
	 * Main method of the server handles logging ,server startup procedures,
	 * acception connection until server ends and responsible to receive name
	 * and sending ACK to client
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Reseting logmanager to prevent printing log into console
		LogManager.getLogManager().reset();
		// log file preparation
		Logger log = null;
		try {
			// setting path for log file
			String path = getHomeDir() + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
					+ "-server-log.log";
			prepareFile(path);
			fileHandler = new FileHandler(path, true);
			fileHandler.setFormatter(new SimpleFormatter());
			log = Logger.getLogger(FileServer.class.getName());
			log.addHandler(fileHandler);
			log.setLevel(Level.ALL);
		} catch (SecurityException | IOException e1) {
			System.out.println("Error opening log file");
			e1.printStackTrace();
		}

		// start server
		System.out.println("Starting server");
		try {

			// create server socket
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
			log.log(Level.WARNING, "Error while starting server.Please close other pprogram using PORT " + PORT, e);
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

				// Increment no.of active user count by 1
				statistics.addActiveUers();
				printStatistics();

				// receiving name
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				try {
					// setting socket timeout to 30s
					socket.setSoTimeout(30000);
					log.info("Reading name");
					String name = reader.readLine();
					log.fine("Name readed Name-" + name);
					System.out.println("Client " + name + " is connected");

					// resetting socket timeout to 5m
					socket.setSoTimeout(300000);

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
					log.log(Level.SEVERE, "Client disconnected", e);
					disconnectClient(null);
					continue;
				}

			} catch (IOException e) {
				log.log(Level.SEVERE, "Client disconnected", e);
				disconnectClient(null);
			}
		}

	}

}
