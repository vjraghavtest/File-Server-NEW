import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class FileLogbook {
	private static FileLogbook logbook;
	private String path;

	private FileLogbook() throws IOException {
		path = "C:\\Users\\Administrator\\Desktop\\file-log.txt";
	}

	// method that returns object
	public static FileLogbook getInstance() throws IOException {
		if (logbook == null) {
			logbook = new FileLogbook();
		}
		return logbook;
	}

	// writing data
	public void writeLog(String timestamp, String filename, String username, long filesize) throws IOException {
		// preparing data
		String data = timestamp + "|" + filename + "|" + username + "|" + filesize + "\n";
		// "|"+filepath +
		// Creating op writer
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path), true));
		// writing data
		bufferedWriter.write(data);
		bufferedWriter.flush();
		// closing stream
		bufferedWriter.close();
	}

	// Read by timestamp
	public ArrayList<ArrayList<String>> readByTimestamp() throws IOException {

		// creating output obj
		ArrayList<ArrayList<String>> log = new ArrayList<ArrayList<String>>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
		while (true) {

			// reading from file
			String msg = bufferedReader.readLine();
			if (msg == null)
				break;

			// Extracting data
			StringTokenizer stringTokenizer = new StringTokenizer(msg, "|");
			ArrayList<String> record = new ArrayList<String>();

			// Adding into arraylist
			while (stringTokenizer.hasMoreTokens()) {
				record.add(stringTokenizer.nextToken());
			}
			log.add(record);
		}
		bufferedReader.close();

		// returning op obj
		return log;
	}

	// Read by user
	public LinkedHashMap<String, ArrayList<FileDetail>> readByUser() throws IOException {

		// creating output obj
		LinkedHashMap<String, ArrayList<FileDetail>> log = new LinkedHashMap<String, ArrayList<FileDetail>>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
		while (true) {

			// reading from file
			String msg = bufferedReader.readLine();
			if (msg == null)
				break;

			// Extracting data
			StringTokenizer stringTokenizer = new StringTokenizer(msg, "|");
			String timestamp = stringTokenizer.nextToken();
			String filename = stringTokenizer.nextToken();
			String username = stringTokenizer.nextToken();
			long filesize=Long.parseLong(stringTokenizer.nextToken());
			// String filepath = stringTokenizer.nextToken();
			// System.out.println(timestamp + " " + filename + " " + username);

			FileDetail detail = new FileDetail(filename, null, timestamp,filesize);
			ArrayList<FileDetail> list = null;
			if (log.containsKey(username)) {
				list = log.get(username);
			} else {
				list = new ArrayList<FileDetail>();
			}

			// Adding into arraylist
			list.add(detail);
			log.put(username, list);
		}
		bufferedReader.close();
		// returning op obj
		return log;
	}

}
