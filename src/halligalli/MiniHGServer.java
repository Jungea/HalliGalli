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
		System.out.println("�̴� �Ҹ����� ������ ���۵Ǿ����ϴ�.");

		while (true) {
			Table table = new Table();
			Deck d = new Deck(); // 56���� ī�� �� ����
			d.shuffle();

			Player[] player = new Player[4];
			for (int i = 0; i < 4; i++) {
				player[i] = new Player(ss.accept(), i);
				for (int j = 0; j < 14; j++) // 14�徿 ��
					player[i].setPlayerCard(d.deal());
			}

			for (int i = 0; i < 4; i++) {
				System.out.print("�÷��̾�" + i + "�� ī�� ��� = ");
				player[i].showCards(); // �÷��̾��� ī�� ���
				player[i].start();
			}

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

class Deck { // ī�� 56���� �ٷ�� ���� ǥ���ϴ� Ŭ���� Deck
	private LinkedList<Card> deck = new LinkedList<Card>();
	private String[] fruit = { "����", "�ٳ�", "����", "�ڵ�" };
	private int[] count = { 5, 3, 3, 2, 1 };

	// ī�带 �����Ͽ� ���� ����
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

	// ī�带 ����
	public void shuffle() {
		Collections.shuffle(deck);
	}

	// ���� �� �� ī�带 �����Ͽ� ����
	public Card deal() {
		return deck.remove(0);
	}
}