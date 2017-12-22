package klijenti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.LinkedList;

import main.Server;

public class Klijenti extends Thread {

	public String username = null;
	public String password;
	public static OutputStream output;
	public static BufferedReader clientInput = null;
	public static PrintStream clientOutput = null;
	public static Socket socket = null;

	public Klijenti(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		int num;
		try {
			System.out.println("Handling new connection from" + socket.getInetAddress() + ":" + socket.getPort());
			clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = socket.getOutputStream();
			clientOutput = new PrintStream(socket.getOutputStream());
			clientOutput.println("Communication has been established!");
			boolean value = true;
			while (value) {
				num = Integer.parseInt(clientInput.readLine().toString());
				switch (num) {
				case 0:
					register();
					break;
				case 1:
					login();
					break;
				case 2:
					logout();
					break;
				case 3:
					upload();
					break;
				case 4:
					download();
					break;
				case 5:
					list();
					break;
				case 6:
					value = false;
					System.out.println("User disconnected!");
					break;
				default:
					clientOutput.println("Error!");
					break;
				}
			}
			socket.close();
		} catch (NumberFormatException e) {
			System.out.println("Entry not valid!");
		} catch (IOException e) {
			System.out.println("Client has forcefully terminated the application.");
		}
	}

	private void list() {
		LinkedList<String> kljucevi = new LinkedList<>();
		for (int i = 0; i < Server.sviKlijenti.size(); i++) {
			if (Server.sviKlijenti.get(i).username.equals(this.username)) {
				kljucevi = Server.sviKlijenti.get(i).uploadFiles;
			}
		}

		for (int i = 0; i < kljucevi.size(); i++) {
			clientOutput.println(kljucevi.get(i));
		}
		clientOutput.println("-END-");
	}

	private void download() {
		try {
			String key = clientInput.readLine();
			if (key.equals(">>QUIT")) {
				return;
			}

			for (int i = 0; i < Server.sviKlijenti.size(); i++) {
				for (int j = 0; j < Server.sviKlijenti.get(i).uploadFiles.size(); j++) {
					if (!Server.sviKlijenti.get(i).uploadFiles.contains(key)) {
						clientOutput.println("Invalid key.");
					}
				}
			}

			boolean state = false;
			for (int i = 0; i < Server.sviKlijenti.size(); i++) {
				for (int j = 0; j < Server.sviKlijenti.get(i).uploadFiles.size(); j++) {
					if (Server.sviKlijenti.get(i).uploadFiles.get(j).equals(key)) {
						clientOutput.println("Valid key.");
						state = true;
						break;
					}
				}
			}

			if (state == true) {
				// clientOutput.println("Here is your requested file " + key + ".txt!");
				byte[] buffer = new byte[1024];
				File file = new File(key + ".txt");
				if (file.exists()) {
					RandomAccessFile randomAccessFile = new RandomAccessFile(key + ".txt", "r");
					int n;
					while (true) {
						n = randomAccessFile.read(buffer);
						if (n == -1) {
							break;
						}
						output.write(buffer, 0, n);
					}
					randomAccessFile.close();

				} else {
					System.out.println("Error!");
				}
			}
		} catch (IOException e) {
			System.out.println("Error while downloading file!");
		}
	}

	private void upload() {
		String entry;
		String key = null;
		boolean valid = true;
		while (valid) {
			key = getKey();
			for (int i = 0; i < Server.sviKlijenti.size(); i++) {
				if (!Server.sviKlijenti.get(i).uploadFiles.contains(key)) {
					valid = false;
					break;
				}
			}
		}

		try {
			entry = clientInput.readLine();
			if (entry.equals(">>QUIT")) {
				System.out.println("Client has forecefully canceled upload.");
				return;
			}
			clientOutput.println(key);
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(key + ".txt")));
			p.println(entry);
			p.close();

			for (int i = 0; i < Server.sviKlijenti.size(); i++) {
				if (Server.sviKlijenti.get(i).username.equals(this.username)) {
					Server.sviKlijenti.get(i).uploadFiles.add(key);
					Server.update();
				}
			}

		} catch (IOException e) {
			System.err.println("Error while uploading file!");
		}
	}

	private String getKey() {
		String primaryText = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom random = new SecureRandom();
		StringBuilder stringBuilder = new StringBuilder(10);
		for (int i = 0; i < 10; i++) {
			stringBuilder.append(primaryText.charAt(random.nextInt(primaryText.length())));
		}
		return stringBuilder.toString();
	}

	private void logout() {

		this.username = null;
		// this.password = null;
		clientOutput.println("User successfully logged out.");
	}

	private void login() {
		try {
			String userpass;
			userpass = clientInput.readLine();
			String[] userpassNiz = userpass.split(",");
			String username = userpassNiz[0];
			String password = userpassNiz[1];

			for (int i = 0; i < Server.sviKlijenti.size(); i++) {
				if (Server.sviKlijenti.get(i).username.equals(username)
						&& Server.sviKlijenti.get(i).password.equals(password)) {
					clientOutput.println("Login successful!");
					this.username = username;
					// this.password = password;
					return;
				}
			}
			clientOutput.println("This user does not exist!");
		} catch (IOException e) {
			System.out.println("Error while login.");
		}
	}

	private void register() {
		try {
			String userpass = clientInput.readLine();
			String[] userpassNiz = userpass.split(",");
			String username = userpassNiz[0];
			String password = userpassNiz[1];

			for (int i = 0; i < Server.sviKlijenti.size(); i++) {
				if (Server.sviKlijenti.get(i).equals(username)) {
					clientOutput.println("Username already exists! Enter another username.");
					return;
				}
			}
			clientOutput.println("New user created!");
			Klijent k = new Klijent();
			k.setUsername(username);
			k.setPassword(password);
			Server.sviKlijenti.add(k);
			Server.update();
		} catch (IOException e) {
			System.out.println("Error while registration!");
		}

	}
}
