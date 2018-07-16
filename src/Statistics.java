import java.util.LinkedHashMap;

public class Statistics {
	private int filesUploaded, activeUers;
	private long dataUploaded;
	LinkedHashMap<String, ClientDetail> clientDetails;

	public int getActiveUers() {
		return activeUers;
	}

	public void addActiveUers() {
		this.activeUers += 1;
	}

	public void removeActiveUers() {
		this.activeUers -= 1;
	}

	public Statistics() {
		filesUploaded = (int) (dataUploaded = 0);
		clientDetails = new LinkedHashMap<String, ClientDetail>();
	}

	public int getFilesUploaded() {
		return filesUploaded;
	}

	public void addFilesUploaded() {
		this.filesUploaded += 1;
	}

	public long getDataUploaded() {
		return dataUploaded;
	}

	public void addDataUploaded(long l) {
		this.dataUploaded += l;
	}

	public LinkedHashMap<String, ClientDetail> getClientDetails() {
		return clientDetails;
	}

	public int getActiveClients() {
		int count = 0;
		for (ClientDetail clientDetail : clientDetails.values()) {
			if (clientDetail.isOnline())
				count++;
		}
		return count;
	}
}
