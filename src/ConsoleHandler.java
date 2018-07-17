import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleHandler extends Thread {
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Logger log = Logger.getLogger(this.getClass().getName());
		log.addHandler(FileServer.fileHandler);
		log.setLevel(Level.ALL);
		log.info("In console handler thread");
		while (true) {
			try {
				// reading ip
				System.out.println(
						"\n1.View file details by timestamp\n2.View file details by user\n3.View by file size(Decending)\n4.View top 5 uploaders\n5.File catagory by file size\n6.Exit\nEnter choice number:-");
				String cmd = reader.readLine();
				log.fine("Readed console cmd " + cmd);
				int choice = Integer.parseInt(cmd);
				// display by timestamp
				if (choice == 1) {
					String[][] details = FileLogbook.getInstance().readByTimestamp();
					log.fine("Data fetched from file");
					System.out.println("Total no. of files received is " + details.length);

					if (details.length == 0) {
						log.fine("No Data found to display");
						continue;
					}

					// further detail
					System.out.println("Enter 1 to view full details");
					cmd = reader.readLine();
					log.info("Console cmd " + cmd);
					if (Integer.parseInt(cmd) == 1) {
						System.out.println(
								"Timestamp\t\tFilename\t\t\tUsername\tFilesize\n-------------------------------------------------------------------------------");
						for (String[] record : details) {
							// for(String data:record){
							// System.out.printf("%-30s ",data);
							// }
							System.out.printf("%s %-35s %-8s %-10d\n", record[0], record[1], record[2],
									Long.parseLong(record[3]));
						}
					}
					log.fine("Data retrived and printed to console");
				} else if (choice == 2) {
					LinkedHashMap<String, ArrayList<FileDetail>> details = FileLogbook.getInstance().readByUser();
					log.fine("Info fetched from file based on username");
					if (details.isEmpty()) {
						System.out.println("No details found");
						log.fine("No data found from file");
						continue;
					}
					System.out.println("Username  -  No. of files uploaded");
					for (String username : details.keySet()) {
						// System.out.println(username+" - "
						// +details.get(username).size());
						System.out.printf("%-15s - %d\n", username, details.get(username).size());
					}
					log.fine("Details printed to console");
					System.out.println("Enter the username to view full details");
					String name = reader.readLine();
					log.info("Name readed from console " + name);
					if (details.containsKey(name)) {
						log.info("Name found");
						ArrayList<FileDetail> userDetail = details.get(name);
						System.out.println(
								"Filename\t\t\t\tTimestamp\t\tFilesize\n-------------------------------------------------------------------------------");
						for (FileDetail fileDetail : userDetail) {
							// System.out.println(fileDetail.getName()+"\t"+fileDetail.getTimestamp());
							System.out.printf("%-35s %-10s %d\n", fileDetail.getName(), fileDetail.getTimestamp(),
									fileDetail.getFilesize());
						}
						log.fine("User details printed");
					} else {
						System.out.println("Invalid Name");
						log.fine("User details not found");
					}
				} else if (choice == 3) {
					// reading details based on file size
					log.info("Retriving data from file");
					String[][] data = FileLogbook.getInstance().readByFilesize();
					log.fine("Data retrived from file");
					if (data.length == 0) {
						log.fine("No data to display");
						System.out.println("No file details available");
						continue;
					}
					System.out.println(
							"Timestamp\t\tFilename\t\t\tUsername\tSize(Mb)\n-------------------------------------------------------------------------------");
					for (String[] record : data) {
						System.out.printf("%s %-35s %-10s %-5.2f\n", record[0], record[1], record[2],
								Double.parseDouble(record[3]) / 1000000d);
					}
					log.fine("Data extracted and displayed");
				} else if (choice == 4) {
					// Top 5 users
					String[][] users = FileLogbook.getInstance().top5UserList();
					System.out.println(
							"Username\tNo.of files uploaded\n-------------------------------------------------------------------------------");
					for (int i = 0; i < 5; i++) {
						if (users[i][0] == null)
							break;
						System.out.printf("%-15s - %s\n", users[i][0], users[i][1]);
					}
					
					log.fine("Data extracted and printed to console");
				} else if (choice == 5) {
					//file catagory
					int[] data=FileLogbook.getInstance().countFilesBySize();
					log.fine("Data parsed from file");
					System.out.println("-------------------------------------------------------------------------------");
					System.out.println(data[0]+" files received below 1mb size");
					System.out.println(data[1]+" files received between 1mb to 100mb size");
					System.out.println(data[2]+" files received above 100mb size");
					System.out.println("-------------------------------------------------------------------------------");
					log.fine("Data printed to console");
				} else if (choice == 6) {
					log.info("Shutting down server");
					FileServer.endServer();
					// System.exit(0);
					log.fine("Server stopped");
					System.out.println("Server stopped");
					break;
				}
			} catch (IOException | NumberFormatException e) {
				System.out.println("Invalid input");
				// log.warning(e.getMessage());
				log.log(Level.WARNING, "Invalid input", e);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception", e);

			}
		}
	}
}
