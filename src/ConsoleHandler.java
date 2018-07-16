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
				System.out.println("1.View file details by timestamp\n2.View file details by user\n3.Exit\nEnter choice number:-");
				String cmd = reader.readLine();
				log.fine("Readed console cmd " + cmd);
				int choice=Integer.parseInt(cmd);
				//display by timestamp
				if (choice == 1) {
					ArrayList<ArrayList<String>> details = FileLogbook.getInstance().readByTimestamp();
					log.fine("Data fetched from file");
					System.out.println("Total no. of files received is " + details.size());
					cmd = reader.readLine();
					log.info("Console cmd "+cmd);
					//further detail
					System.out.println("Enter 1 to view full details");
					if (Integer.parseInt(cmd) == 1) {
						System.out.println("Timestamp\tFilename\tUsername");
						for(ArrayList<String> record:details){
							for(String data:record){
								System.out.print(data+"\t");
							}
							System.out.println();
						}
					}
					log.fine("Data retrived and printed to console");
				} else if(choice==2){
					LinkedHashMap<String, ArrayList<FileDetail>> details=FileLogbook.getInstance().readByUser();
					log.fine("Info fetched from file based on username");
					System.out.println("Username  -  No. of files uploaded");
					for(String username:details.keySet()){
						System.out.println(username+" - " +details.get(username).size());
					}
					log.fine("Details printed to console");
					System.out.println("Enter the username to view full details");
					String name=reader.readLine();
					log.info("Name readed from console "+name);
					if(details.containsKey(name)){
						log.info("Name found");
						ArrayList<FileDetail> userDetail=details.get(name);
						System.out.println("Filename\tTimestamp");
						for(FileDetail fileDetail:userDetail){
							System.out.println(fileDetail.getName()+"\t"+fileDetail.getTimestamp());
						}
						log.fine("User details printed");
					}else{
						System.out.println("Invalid Name");
						log.fine("User details not found");
					}
				}else if (choice==3) {
					FileServer.endServer();
					System.exit(0);
					break;
				}
			} catch (IOException | NumberFormatException e) {
				System.out.println("Invalid input");
				log.warning(e.getMessage());
			} catch (Exception e) {
				log.severe(e.getMessage());
			}
		}
	}
}
