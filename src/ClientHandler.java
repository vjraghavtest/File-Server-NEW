import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ClientHandler extends Thread {
	Socket socket = null;
	ClientDetail detail = null;;

	public ClientHandler(ClientDetail detail) {
		this.detail = detail;
		this.socket = detail.getSocket();
	}

	public String gettimestamp() {
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
	}

	public void run() {
		System.out.println("Thread strated for client " + detail.getName());

		String fileDetail = null;
		PrintWriter printWriter = null;
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		String owner = null, filename = null, data = null;
		HashMap<String, String> file = null;
		int bytesRead = 0;
		long filesize = 0;
		int bufferSize = 1024 * 1024;
		long remainingBytes = 0;
		byte[] buffer = null;
		String tmp1, tmp2;

		while (true) {
			// initialization block
			try {
				printWriter = new PrintWriter(socket.getOutputStream());
				inputStream = new BufferedInputStream(socket.getInputStream());
				buffer = new byte[bufferSize];
				// System.out.println("Init success");
				
				//sending ack
				System.out.println("Sending ack");
				printWriter.println("Hello");
				System.out.println("Ack sent");
				printWriter.flush();
				
				// System.out.println("Receiving file details as string");
				inputStream.read(buffer);
				data = new String(buffer);
				data = data.substring(0, data.lastIndexOf('|'));
				// System.out.println(fileDetail);

				// handling data
				if (data.startsWith("INFO")) {
					fileDetail = data.substring(4);

					// sending file

					// getting data from file details
					// string tokenizer
					StringTokenizer stringTokenizer = new StringTokenizer(fileDetail, ",");
					file = new HashMap<String, String>();
					while (stringTokenizer.hasMoreTokens()) {
						StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken(), ":");
						tmp1 = stringTokenizer2.nextToken();
						tmp2 = stringTokenizer2.nextToken();
						file.put(tmp1, tmp2);
						// System.out.println(tmp1 + "----" + tmp2 +
						// "----");
					}

					System.out.println("File Details received");
					System.out.println("details received ");

					// new path
					owner = file.get("owner");
					filename = gettimestamp() + "-" + file.get("name");
					// System.out.println(owner + "----" + filename);
					String path = "C:\\Users\\Administrator\\Desktop\\";
					path += owner + "\\" + filename;

					// creating folder
					new File("C:\\Users\\Administrator\\Desktop\\" + owner).mkdirs();
					System.out.println("New Path-" + path);

					// creating output path
					outputStream = new BufferedOutputStream(new FileOutputStream(new File(path)));

					// Sending ACK
					printWriter.println("ACK OBJ");
					printWriter.flush();
					System.out.println("ACK OBJ SENT");

					// getting file size

					// Receiving file
					System.out.println(file.get("filesize") + "--hi");
					filesize = Long.parseLong(file.get("filesize"));
					remainingBytes = filesize;
					System.out.println("Receiving file");
					try {
						while (true) {
							// System.out.println("Reading from stream");
							bytesRead = inputStream.read(buffer, 0, (int) Math.min(bufferSize, remainingBytes));

							// System.out.println(bytesRead + " bytes
							// received");
							if (bytesRead < 0 || remainingBytes <= 0)
								break;
							else
								remainingBytes = remainingBytes - bytesRead;

							// System.out.println(remainingBytes + " bytes
							// remaining");
							outputStream.write(buffer, 0, bytesRead);
							// System.out.println(bytesRead + " bytes written to
							// file");
						}
					} catch (Exception e) {
						// e.printStackTrace();
						FileServer.disconnectClient(detail.getName());
						FileServer.statistics.addDataUploaded(filesize - remainingBytes);
						// System.out.println("Client is disconnected");
						// FileServer.statistics.removeActiveUers();
						break;
					}

					outputStream.flush();
					System.out.println("File received success fully");
					FileServer.statistics.addDataUploaded(filesize);

					// verification using checksum
					// sending ACK TO Client
					System.out.println("Comparing checksum");
					if (Checksum.getChecksum(path).equals(file.get("checksum"))) {
						System.out.println("File both are same");
						FileServer.statistics.addFilesUploaded();
						// success message to client
						printWriter.println("SUCCESS " + path);

						// adding to details
						FileDetail fileDetail2 = new FileDetail(filename, path);
						detail.getFiles().add(fileDetail2);

					} else {
						// Sending failure response
						System.out.println("File are not same");
						System.out.println("File transfer Incomplete");
						printWriter.println("TRANSFER FAILED");
					}
					
					printWriter.flush();
					System.out.println("Reading ack");
					inputStream=null;
					new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
//					inputStream.read(buffer);
					System.out.println(new String(buffer));
					System.out.println("File transfer message sent to client");
//					int tmp = 0;
//					outputStream.write(tmp);
//					System.out.println("tmp"+tmp);
					FileServer.printStatistics();
					outputStream.close();

				} else if (data.equals("INFO")) {
					// retrive file details
					
					// parse it as string (JSON format)
					// send to client
<<<<<<< HEAD
					printWriter.println(fileDetails.toString());
					printWriter.flush();
					
					System.out.println("Data sent to client");
					if(inputStream.read()==1){
						System.out.println("OK");
					}
=======
>>>>>>> parent of 7514e70... LIST feautre
				} else if (data.equals("END")) {
					System.out.println("Client " + detail.getName() + " requested for end connection");
					FileServer.disconnectClient(detail.getName());
					socket.close();
					break;
				} else {
					System.out.println("Invalid request:" + data);
				}

			} catch (Exception e) {
				FileServer.disconnectClient(detail.getName());
				// System.out.println(detail.getName() + " is
				// disconnected");
				detail.setOnline(false);
				// FileServer.statistics.removeActiveUers();
				break;
				// e.printStackTrace();
			}

			// clearing buffer
			buffer = null;

		}

	}
}
