import com.sun.istack.internal.Nullable;

public class FileDetail {
	private String name;
	private String path;
	private String timestamp;
	private Long filesize;

	public FileDetail(String name, String path, String timestamp, Long filesize) {
		this.name = name;
		this.path = path;
		this.timestamp = timestamp;
		this.filesize = filesize;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getName() {
		return name;
	}

	public Long getFilesize() {
		return filesize;
	}

	public String getPath() {
		return path;
	}

}
