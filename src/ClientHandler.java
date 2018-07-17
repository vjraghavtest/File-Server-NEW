import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientHandler extends Thread {
	Socket socket = null;
	ClientDetail detail = null;
	static Logger logger = null;
	static FileHandler fileHandler = null;

	public ClientHandler(ClientDetail detail) {
		this.detail = detail;
		this.socket = detail.getSocket();
	}

	public String gettimestamp() {
		return new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SSS").format(new java.util.Date());
	}

	public void run() {
		try {
			logger = Logger.getLogger(this.getName());
			fileHandler = FileServer.fileHandler;
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);

			logger.fine("In thread " + this.getName());
			System.out.println("Thread strated for client " + detail.getName());

			int bufferSize = 1024 * 1024;
			String home = "C:\\Users\\Administrator\\Desktop\\";
			while (!FileServer.exit) {
				// initialization block
				try {
					PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
					BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
					byte[] buffer = new byte[bufferSize];
					// System.out.println("Init success");
					logger.info("Reading from ip stream");
					// System.out.println("Receiving file details as string");
					inputStream.read(buffer);
					String data = new String(buffer);
					data = data.substring(0, data.lastIndexOf('|'));
					logger.fine("Formatted data" + data);
					// System.out.println(fileDetail);

					// handling data
					if (data.startsWith("INFO")) {
						logger.fine("Data was INFO Command");
						String fileDetail = data.substring(4);
						logger.info("File details after extraction " + fileDetail);
						// sending file

						// getting data from file details
						// string tokenizer
						logger.info("Extracting data");
						StringTokenizer stringTokenizer = new StringTokenizer(fileDetail, ",");
						HashMap<String, String> file = new HashMap<String, String>();
						while (stringTokenizer.hasMoreTokens()) {
							StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken(), ":");
							String tmp1 = stringTokenizer2.nextToken();
							String tmp2 = stringTokenizer2.nextToken();
							file.put(tmp1, tmp2);
							logger.info(tmp1 + "----" + tmp2 + "----");
						}
						logger.fine("File details received");
						// System.out.println("File Details received");
						// System.out.println("details received ");

						// new path
						String owner = file.get("owner");
						logger.info("Owner " + owner);
						String timestamp = gettimestamp();
						String filename = timestamp + "-" + file.get("name");
						logger.info("File name " + filename);
						String path = "C:\\Users\\Administrator\\Desktop\\";
						path += owner + "\\" + filename;
						logger.info("New path " + path);

						// creating folder
						logger.info("Creating folder");
						new File(home + owner).mkdirs();
						logger.fine("Folder created");
						// System.out.println("New Path-" + path);

						// creating output path
						logger.info("Creating op stream");
						BufferedOutputStream outputStream = new BufferedOutputStream(
								new FileOutputStream(new File(path)));

						// Sending ACK
						logger.info("ACK OBJ");
						printWriter.println("ACK OBJ");
						printWriter.flush();
						logger.fine("ACK OBJ SENT");

						// getting file size

						// Receiving file
						long filesize = Long.parseLong(file.get("filesize"));
						long remainingBytes = filesize;
						logger.fine("File size " + filesize);
						logger.info("Receiving file");
						try {
							while (true) {
								// logger.info("Reading from stream");
								int bytesRead = inputStream.read(buffer, 0, (int) Math.min(bufferSize, remainingBytes));

								// logger.info(bytesRead + " bytes received");
								if (bytesRead < 0 || remainingBytes <= 0)
									break;
								else
									remainingBytes = remainingBytes - bytesRead;

								// logger.info(remainingBytes + " bytes
								// remaining");
								outputStream.write(buffer, 0, bytesRead);
								// logger.info(bytesRead + " bytes written to
								// file");
							}
						} catch (Exception e) {
							// e.printStackTrace();
							logger.warning(e.getMessage());
							logger.info("Data transfered " + (filesize - remainingBytes));
							FileServer.disconnectClient(detail);
							FileServer.statistics.addDataUploaded(filesize - remainingBytes);
							break;
						}
						outputStream.flush();

						logger.info("File received successfully.Data transferred is " + filesize);
						FileServer.statistics.addDataUploaded(filesize);

						// verification using checksum
						// sending ACK TO Client
						logger.info("Comparing checksum");
						if (Checksum.getChecksum(path).equals(file.get("checksum"))) {
							logger.info("File both are same");
							FileServer.statistics.addFilesUploaded();
							// success message to client
							printWriter.println("SUCCESS " + path);
							logger.info("Writing file data into file log");
							FileLogbook.getInstance().writeLog(timestamp, file.get("name"), owner);
							logger.fine("File details written into file log");
							System.out.println("File received successfully from " + detail.getName());
							// adding to details
							FileDetail fileDetail2 = new FileDetail(file.get("name"), path, timestamp);
							detail.getFiles().add(fileDetail2);

						} else {
							// Sending failure response
							logger.info("File are not same");
							System.out.println("File transfer Incomplete from " + detail.getName());
							printWriter.println("TRANSFER FAILED");
						}

						printWriter.flush();
						logger.fine("File transfer message sent to client");
						FileServer.printStatistics();
						outputStream.close();
						logger.fine("File stream closed");
					} else if (data.equals("LIST")) {

						// retrive file details
						logger.info("Command was " + data);
						ArrayList<FileDetail> files = detail.getFiles();

						// parse it as string (JSON format)
						StringBuffer fileDetails = new StringBuffer();
						for (FileDetail fileData : files) {
							fileDetails.append(fileData.getName() + "<" + fileData.getPath() + "|");
						}
						logger.info("Data is " + fileDetails);

						// send to client
						printWriter.println(fileDetails.toString());
						printWriter.flush();

						logger.fine("File details sent to client " + detail.getName());
						System.out.println("Data sent to client");

					} else if (data.equals("END")) {
						logger.info("Command was " + data);
						System.out.println("Client " + detail.getName() + " requested for end connection");
						logger.fine("Client " + detail.getName() + " requested for end connection");
						FileServer.disconnectClient(detail);
						socket.close();
						logger.fine("Socket closed");
						break;
					} else {
						// System.out.println("Invalid request:" + data);
						logger.info("Received echo");
						logger.info("Sending echo");
						printWriter.println("ECHO");
						printWriter.flush();
						logger.fine("ECHO sent");
					}

				} catch (Exception e) {
					logger.warning(e.getMessage());
					FileServer.disconnectClient(detail);
					detail.setOnline(false);
					break;
				}
			}
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
	}
}
