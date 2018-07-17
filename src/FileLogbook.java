import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
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
	public String[][] readByTimestamp() throws IOException {

		// creating output obj
		ArrayList<String[]> log = new ArrayList<String[]>();
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
			log.add(record.toArray(new String[record.size()]));
		}
		bufferedReader.close();

		// returning op obj
		return log.toArray(new String[log.size()][]);
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
			long filesize = Long.parseLong(stringTokenizer.nextToken());
			// String filepath = stringTokenizer.nextToken();
			// System.out.println(timestamp + " " + filename + " " + username);

			FileDetail detail = new FileDetail(filename, null, timestamp, filesize);
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

	// public String[][] readByFilesize() throws IOException {
	// // Get full list of user
	// String[][] details = readByTimestamp();
	//
	// // throw exception if no element found
	// if (details.length == 0)
	// throw new NoSuchElementException();
	//
	// // sort based on file size
	// for (int i = 0; i < details.length; i++) {
	// for (int j = i + 1; j < details.length; j++) {
	// if (Long.parseLong(details[i][3]) < Long.parseLong(details[j][3])) {
	// String[] tmp = details[i];
	// details[i] = details[j];
	// details[j] = tmp;
	// }
	// }
	// }
	//
	// // return as array
	// return details;
	// }

	/*
	 * Read by file size
	 * 
	 * @return String[][] Detailed list sorted based on filesize (Decending
	 * order)
	 */
	public String[][] readByFilesize() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		LinkedList<String[]> log = new LinkedList<String[]>();
		// reads until EOF
		while (true) {
			// read line
			String msg = reader.readLine();
			if (msg == null)
				break;
			// parse using stringTokenizer
			StringTokenizer stringTokenizer = new StringTokenizer(msg, "|");
			String[] record = new String[4];
			int count = 0;
			// store it in array
			while (stringTokenizer.hasMoreTokens()) {
				record[count++] = stringTokenizer.nextToken();
			}

			// run loop log size times
			int i;
			for (i = 0; i < log.size(); i++) {
				// compare element i with record
				if (Long.parseLong(record[3]) >= Long.parseLong(log.get(i)[3])) {
					// if new size greater or equal than old size then break
					break;
				}
			}
			// add at i th index
			log.add(i, record);

			// return as array
		}
		return log.toArray(new String[log.size()][]);
	}

	/*
	 * top5UserList
	 * 
	 * @return String[][] Array of details about top users
	 */
	public String[][] top5UserList() throws IOException {
		String[][] list = new String[6][2];

		// retrive details
		LinkedHashMap<String, ArrayList<FileDetail>> details = readByUser();

		// retrive all names
		String[] nameList = details.keySet().toArray(new String[details.keySet().size()]);

		// sort by file count
		for (int i = 0; i < nameList.length; i++) {
			int pos = 5;
			list[pos][0] = nameList[i];
			list[pos][1] = Integer.toString(details.get(nameList[i]).size());
			for (int j = 4; j >= 0; j--) {
				if (list[j][1] == null || Integer.parseInt(list[pos][1]) > Integer.parseInt(list[j][1])) {
					String[] tmp = list[j];
					list[j] = list[pos];
					list[pos] = tmp;
					pos = j;
				}
			}
		}

		// return array
		return list;
	}

	/*
	 * countFilebySize
	 * 
	 * @return int[] represents no of files based on file size
	 */
	public int[] countFilesBySize() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
		int[] count = new int[3];
		while (true) {

			// read until EOF
			String msg = reader.readLine();
			if (msg == null)
				break;

			// parse and get file size
			StringTokenizer stringTokenizer = new StringTokenizer(msg, "|");
			for (int i = 0; i < 3; i++) {
				stringTokenizer.nextToken();
			}
			long size = Long.parseLong(stringTokenizer.nextToken());

			// catagerise based on file size
			if (size < 1000000L) {
				count[0]++;
			} else if (size < 100000000L) {
				count[1]++;
			} else {
				count[2]++;
			}
		}
		reader.close();

		// return as array
		return count;

	}
}
