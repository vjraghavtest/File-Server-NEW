import com.sun.istack.internal.Nullable;

public class FileDetail {
	private String name;
	private String path;
	private String timestamp;
	public FileDetail(String name,String path, String timestamp) {
		this.name = name;
		this.path = path;
		this.timestamp = timestamp;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public String getName() {
		return name;
	}
	public String getPath() {
		return path;
	}
	
}
