import java.net.Socket;
import java.util.ArrayList;

public class ClientDetail {
	private String name;
	private boolean isOnline;
	private Socket socket;
	private ArrayList<FileDetail> files;
	public ClientDetail(String name, Socket socket,boolean isOnline) {
		super();
		this.name = name;
		this.socket = socket;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public ArrayList<FileDetail> getFiles() {
		return files;
	}
	public void setFiles(ArrayList<FileDetail> files) {
		this.files = files;
	}
}
