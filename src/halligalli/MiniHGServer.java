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
		System.out.println("미니 할리갈리 서버가 시작되었습니다.");

		while (true) {
			Player[] player = new Player[4];
			for (int i = 0; i < 4; i++)
				player[i] = new Player(ss.accept(), i);

			for (int i = 0; i < 4; i++)
				player[i].start();

			System.out.println("페어가 만들어 졌습니다. ");

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
			output.println("PRINT 다른 경기자를 기다립니다.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("연결이 끊어졌습니다 . " + e);
		}
	}

	public void run() {
		output.println("PRINT 모든 경기자가 연결되었습니다. ");
	}
}

class Card { // 카드 한 장을 표현하는 클래스 Card
	private int number; // 카드 번호
	private String fruit; // 과일 모양

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