import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
		String owner = null, filename = null;
		HashMap<String, String> file = null;
		int bytesRead = 0;
		int filesize = 0;
		int bufferSize = 8 * 1024;
		int remainingBytes = 0;
		byte[] buffer = null;
		String tmp1, tmp2;

		// initialization block
		try {
			printWriter = new PrintWriter(socket.getOutputStream());
			inputStream = new BufferedInputStream(socket.getInputStream());
			System.out.println("Init success");
		} catch (IOException e1) {
			System.out.println("Client is disconnected");
			FileServer.statistics.removeActiveUers();
		}

		while (true) {
			// receiving file details as object
			try {
				buffer = new byte[bufferSize];
				// System.out.println("Receiving file details as obj");
				try {
					inputStream.read(buffer);
					fileDetail = new String(buffer);

					// string tokenizer
					try {
						StringTokenizer stringTokenizer = new StringTokenizer(fileDetail, ",");
						file = new HashMap<String, String>();
						while (stringTokenizer.hasMoreTokens()) {
							StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken(), ":");
							tmp1 = stringTokenizer2.nextToken();
							tmp2 = stringTokenizer2.nextToken();
							file.put(tmp1, tmp2);
							System.out.println(tmp1 + "----" + tmp2 + "----");
						}
					} catch (Exception e) {
//						System.out.println("Invalid details");
						System.out.println("Client is disconnected");
						FileServer.statistics.removeActiveUers();
//						e.printStackTrace();
						break;
					}

				} catch (Exception e) {
					System.out.println(detail.getName() + " is disconnected");
					detail.setOnline(false);
					FileServer.statistics.removeActiveUers();
					break;
					// e.printStackTrace();
				}
				System.out.println("File Details received");
				System.out.println("details received ");

				// new path
				owner = file.get("owner");
				filename = gettimestamp() + "-" + file.get("name");
				System.out.println(owner + "----" + filename);
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
				filesize = (int) Long.parseLong(file.get("filesize"));
				remainingBytes = filesize;
				System.out.println("Receiving file");
				try {
					while (true) {
						// System.out.println("Reading from stream");
						bytesRead = inputStream.read(buffer, 0, Math.min(bufferSize, remainingBytes));
						
						// System.out.println(bytesRead + " bytes received");
						if (bytesRead < 0 || remainingBytes <= 0)
							break;
						else
							remainingBytes = remainingBytes - bytesRead;

						// System.out.println(remainingBytes + " bytes remaining");
						outputStream.write(buffer, 0, bytesRead);
						// System.out.println(bytesRead + " bytes written to file");
					}
				} catch (Exception e) {
//					e.printStackTrace();
					FileServer.statistics.addDataUploaded(filesize-remainingBytes);
					System.out.println("Client is disconnected");
					FileServer.statistics.removeActiveUers();
					break;
				}

				outputStream.flush();
				System.out.println("File received success fully");
				FileServer.statistics.addDataUploaded(filesize);
				// verification using checksum
				// sending ACK TO Client
				if (Checksum.getChecksum(path).equals(file.get("checksum"))) {
					System.out.println("File both are same");
					FileServer.statistics.addFilesUploaded();
					// success message to client
					printWriter.println("SUCCESS " + path);

				} else {
					// Sending failure response
					System.out.println("File are not same");
					System.out.println("File transfer Incomplete");
					printWriter.println("TRANSFER FAILED");
				}

				printWriter.flush();
				System.out.println("File transfer message sent to client");
				FileServer.printStatistics();
				outputStream.close();
				// }
			} catch (IOException e) {
				System.out.println("Client is disconnected");
				FileServer.statistics.removeActiveUers();
				e.printStackTrace();
			}

			// clearing buffer
			buffer = null;

		}

	}
}
