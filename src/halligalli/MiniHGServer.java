package halligalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class MiniHGServer {
	boolean[] dead = new boolean[4]; // 죽었는지
	int nowPlayer = 0;
	ArrayList<Messenger> msgList = new ArrayList<Messenger>();
	int msgNum = 0;
	Messenger msg = new Messenger();

	public void nextPlayer() { // 다음 플레이어
		do {
			nowPlayer = (nowPlayer + 1) % 4;
		} while (dead[nowPlayer]);
	}

	public void die(int playerId) {
		dead[playerId] = true;
	}

	public static void main(String[] args) {

		MiniHGServer server = new MiniHGServer();
		server.startServer();
	}

	public void startServer() {
		// TODO Auto-generated method stub
		ServerSocket ss;
		try {
			ss = new ServerSocket(8885);

			System.out.println("미니 할리갈리 서버가 시작되었습니다.");

			while (true) {

				Table table = new Table();
				Deck d = new Deck(); // 56장의 카드 덱 생성
				d.shuffle();

				Player[] player = new Player[4];
				for (int i = 0; i < 4; i++) {
					player[i] = new Player(table, ss.accept(), i, msgNum);
					msg.add(player[i]);
					for (int j = 0; j < 14; j++) // 14장씩 딜
						player[i].addPlayerCard(d.deal());
				}
				msgNum++;
				msgList.add(msg);

				for (int i = 0; i < 4; i++) {
					System.out.print("플레이어" + i + "의 카드 목록 = ");
					player[i].showPlayerCards(); // 플레이어의 카드 목록
					player[i].start();
				}
				System.out.println("페어가 만들어 졌습니다. ");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class Messenger {

		Player[] player = new Player[4];
		int addI = 0;

		public void add(Player p) {
			player[addI++] = p;
		}

		public void sendTo(int playerId, String msg) // 플레이어 playerId에게 메시지를 전달.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 보내는 메시지
		{
			for (int i = 0; i < player.length; i++) {
				sendTo(i, msg);
			}
		}

	}

	class Player extends Thread {
		Table table;
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		int playerId;
		int msgId;

		private LinkedList<Card> list = new LinkedList<Card>();

		public Player(Table table, Socket socket, int playerId, int msgNum) {
			this.table = table;
			this.socket = socket;
			this.playerId = playerId;
			this.msgId = msgNum;

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

		// 플레이어 카드 추가(Bell성공/다른 플레이어의 Bell실패)
		public void addPlayerCard(Card c) {
			list.addLast(c);
		}

		// 플레이어 카드 제거(Turn/Bell실패)
		public Card removePlayerCard() {
			return list.removeFirst();
		}

		public void showPlayerCards() {
			System.out.println(list);
		}

		public Messenger getMsg() {
			return msgList.get(msgId);
		}

		public void run() {
			output.println("PRINT 모든 경기자가 연결되었습니다. ");
			try {
				String command;

				getMsg().sendToAll("NOW " + nowPlayer);
				getMsg().sendToAll("PRINT 플레이어" + nowPlayer + "차례입니다.");
				System.out.println(playerId + ">> 플레이어" + nowPlayer + " 차례.");
				while ((command = input.readLine()) != null) {

					if (command.startsWith("TURN")) {
						System.out.println(playerId + ">> plyaer" + nowPlayer + "카드 뒤집음.");
						getMsg().sendToAll("PRINT player" + nowPlayer + " 카드를 뒤집었습니다.");

						System.out.println(playerId + ">> 다음차례넘김");
						nextPlayer();

						getMsg().sendToAll("NOW " + nowPlayer);
						getMsg().sendToAll("PRINT player" + nowPlayer + " 차례입니다.");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class Table {
	private LinkedList<Card> list = new LinkedList<Card>();

	// 테이블에 카드 추가(플레이어가 TURN)
	public void addTableCard(Card c) {
		list.addFirst(c);
	}

	// 테이블에 카드 제거(플레이어 BELL)
	public Card removeTableCard() {
		return list.removeFirst();
	}

	public void showTableList() {
		System.out.println("TABLE : " + list);
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