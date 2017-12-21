package klijenti;

import java.io.Serializable;
import java.util.LinkedList;

public class Klijent implements Serializable {
	String username = "";
	String password = "";
	LinkedList<String> uploadFiles = new LinkedList<>();

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LinkedList<String> getUploadFiles() {
		return uploadFiles;
	}

	public void setUploadFiles(LinkedList<String> uploadFiles) {
		this.uploadFiles = uploadFiles;
	}
}
