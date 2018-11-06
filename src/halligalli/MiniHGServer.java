package halligalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;

class Table {
	private LinkedList<Card> list = new LinkedList<Card>();

	public void setTableCard(Card c) {
		list.addFirst(c);
	}

	public Card getTableCard() {
		return list.removeFirst();
	}
}

public class MiniHGServer {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerSocket ss = new ServerSocket(8881);
		System.out.println("미니 할리갈리 서버가 시작되었습니다.");

		while (true) {
			Table table = new Table();
			Deck d = new Deck(); // 56장의 카드 덱 생성
			d.shuffle();

			Player[] player = new Player[4];
			for (int i = 0; i < 4; i++) {
				player[i] = new Player(ss.accept(), i);
				for (int j = 0; j < 14; j++) // 14장씩 딜
					player[i].setPlayerCard(d.deal());
			}

			for (int i = 0; i < 4; i++) {
				System.out.print("플레이어" + i + "의 카드 목록 = ");
				player[i].showCards(); // 플레이어의 카드 목록
				player[i].start();
			}

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

	public void setPlayerCard(Card c) {
		list.addLast(c);
	}

	public Card getPlayerCard() {
		return list.removeFirst();
	}

	public void showCards() {
		System.out.println(list);
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

class Deck { // 카드 56장을 다루는 덱을 표현하는 클래스 Deck
	private LinkedList<Card> deck = new LinkedList<Card>();
	private String[] fruit = { "딸기", "바나", "라임", "자두" };
	private int[] count = { 5, 3, 3, 2, 1 };

	// 카드를 생성하여 덱에 넣음
	public Deck() {
		for (int i = 0; i < 4; i++)
			for (int j = 1, k = 0; k < 5; j++) {
				deck.add(new Card(fruit[i], k + 1));
				if (j == count[k]) {
					j = 0;
					k++;
				}
			}
	}

	// 카드를 섞음
	public void shuffle() {
		Collections.shuffle(deck);
	}

	// 덱의 맨 앞 카드를 제거하여 리턴
	public Card deal() {
		return deck.remove(0);
	}
}