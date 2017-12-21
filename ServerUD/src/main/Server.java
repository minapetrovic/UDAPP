package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import klijenti.Klijent;
import klijenti.Klijenti;

public class Server {
	public static LinkedList<Klijent> sviKlijenti = new LinkedList<>();
	public static ObjectOutputStream listToUsers;
	public static ObjectInputStream usersToList;

	public static void main(String[] args) {
		load();
		ServerSocket serverSocet = null;
		Socket socket = null;
		try {
			serverSocet = new ServerSocket(44115);

			while (true) {
				System.out.println("Waiting for connection...");
				socket = serverSocet.accept();
				System.out.println("Connection has been established!");

				Klijenti newClient = new Klijenti(socket);
				newClient.start();

			}
		} catch (IOException e) {
			System.err.println("Communication error occurred!");
		}

	}

	public static void update() {
		try {
			listToUsers = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("users.out")));
			for (Klijent klijent : sviKlijenti) {
				listToUsers.writeObject(klijent);
			}
			listToUsers.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void load() {
		try {
			usersToList = new ObjectInputStream(new BufferedInputStream(new FileInputStream("users.out")));
			try {
				while (true) {
					Klijent k = (Klijent) (usersToList.readObject());
					sviKlijenti.add(k);
				}
			} catch (EOFException e) {
				usersToList.close();

			} catch (ClassNotFoundException e) {
				System.out.println("Nije pronadjena klasa!");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
