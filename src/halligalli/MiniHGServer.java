package halligalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class MiniHGServer {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerSocket ss = new ServerSocket(8881);
		System.out.println("�̴� �Ҹ����� ������ ���۵Ǿ����ϴ�.");

		while (true) {
			Player[] player = new Player[4];
			for (int i = 0; i < 4; i++)
				player[i] = new Player(ss.accept(), i);

			for (int i = 0; i < 4; i++)
				player[i].start();

			System.out.println("�� ����� �����ϴ�. ");

		}

	}
}

class Player extends Thread {
	Socket socket;
	BufferedReader input;
	PrintWriter output;
	int playerId;

	private LinkedList<Card> list = new LinkedList<Card>();

	public Player(Socket socket, int playerId) {
		this.socket = socket;
		this.playerId = playerId;

		try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
			output.println("START " + this.playerId);
			output.println("PRINT �ٸ� ����ڸ� ��ٸ��ϴ�.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("������ ���������ϴ� . " + e);
		}
	}

	public void run() {
		output.println("PRINT ��� ����ڰ� ����Ǿ����ϴ�. ");
	}
}

class Card { // ī�� �� ���� ǥ���ϴ� Ŭ���� Card
	private int number; // ī�� ��ȣ
	private String fruit; // ���� ���

	public Card(String fruit, int number) {
		this.fruit = fruit;
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public String getFruit() {
		return fruit;
	}

	public String toString() {
		return fruit + " " + number;
	}
}