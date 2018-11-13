package halligalli;

/*
 * 문제
 * 1. 나의 턴일 때 BELL을 싫패하여 카드가 없을 때
 * 2. BELL 실패시 카드를 나눠주고 남는 개수가 */

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
	boolean bellCount = false;

	public void nextPlayer() { // 다음 플레이어
		do {
			nowPlayer = (nowPlayer + 1) % 4;
		} while (dead[nowPlayer]);
	}

	public void die(int playerId) {
		dead[playerId] = true;
	}

	public static void main(String[] args) throws Exception {

		MiniHGServer server = new MiniHGServer();
		server.startServer();
	}

	public void startServer() throws Exception {
		// TODO Auto-generated method stub
		ServerSocket ss = new ServerSocket(8885);
		;
		try {

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
					player[i].start();
				}
				System.out.println("페어가 만들어 졌습니다. ");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ss.close();
		}

	}

	class Messenger {

		Player[] player = new Player[4];
		int addI = 0;

		public void add(Player p) {
			player[addI++] = p;
		}

		public Socket getSocket(int i) // 소켓을 가져온다.
		{
			System.out.println(player[i]);
			return player[i].getSocket();
		}

		public void sendTo(int playerId, String msg) // i번 플레이어에게 메시지를 전달.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 보내는 메시지
		{
			for (int i = 0; i < player.length; i++) {
				sendTo(i, msg);
			}
		}

		public void sendToCard(int myId, int otherId) // i번 플레이어에게 카드 전달.
		{
			player[otherId].addPlayerCard(player[myId].removePlayerCard());
			sendToAll("REPAINT /" + otherId + "/" + player[otherId].size());
		}

		public void sendToCardOther(int myId) // 종치기 실패(다른 플레이어에게 카드 전달)
		{
			for (int i = 0; i < player.length && i < player[myId].size(); i++) {
				if (i != myId && !dead[i])
					sendToCard(myId, i);
			}
		}

		public void bellRepaint() {
			for (int i = 0; i < player.length; i++) {
				sendToAll("REPAINT /" + i + "//" + player[i].size());
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

		public Socket getSocket() {
			System.out.println("b");
			return socket;
		}

		@Override
		public String toString() {
			return "{ player" + playerId + "입니다} \n";
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

		public int size() {
			return list.size();
		}

		public void run() {
			output.println("PRINT 모든 경기자가 연결되었습니다.");
			try {
				String command;

				getMsg().sendToAll("NOW " + nowPlayer);
				getMsg().sendToAll("PRINT player" + nowPlayer + " 차례입니다."); // info
				if (nowPlayer == playerId)
					getMsg().sendToAll("NOTI player" + nowPlayer + " 차례입니다."); // noti

				while ((command = input.readLine()) != null) {

					if (command.startsWith("TURN")) {
						getMsg().sendToAll("PRINT player" + nowPlayer + " 카드를 뒤집었습니다.");
						getMsg().sendToAll("NOTI player" + nowPlayer + " 카드를 뒤집었습니다.");

						Card removeCard = removePlayerCard();
						table.addTableCard(removeCard);
						getMsg().sendToAll("REPAINT /" + nowPlayer + "/" + removeCard + "/" + list.size());

						if (list.size() == 0)
							die(nowPlayer);
						nextPlayer();

						getMsg().sendToAll("NOW " + nowPlayer);
						getMsg().sendToAll("PRINT player" + nowPlayer + " 차례입니다.");
						getMsg().sendToAll("NOTI player" + nowPlayer + " 차례입니다.");

					} else if (command.startsWith("BELL")) {
						if (bellCount) {
							output.println("늦었습니다.");
						} else {
							bellCount = true;
							int bellPlayerId = (command.charAt(5) - 48);
							getMsg().sendToAll("PRINT player" + bellPlayerId + "이 종침.");
							getMsg().sendToAll("NOTI player" + bellPlayerId + "이 종침.");

							if (!table.sumFive()) {
								getMsg().sendToAll("PRINT player" + bellPlayerId + " 종치기 실패.");
								getMsg().sendToAll("NOTI player" + bellPlayerId + " 종치기 실패.");

								if (list.size() < 4)
									die(bellPlayerId);
								getMsg().sendToCardOther(playerId);
								getMsg().sendToAll("REPAINT /" + bellPlayerId + "/" + list.size());
							}

							else {
								while (table.size() > 0)
									addPlayerCard(table.removeTableCard());
								getMsg().sendToAll("PRINT player" + bellPlayerId + " 종치기 성공.");
								getMsg().sendToAll("NOTI player" + bellPlayerId + " 종치기 성공.");

								getMsg().bellRepaint();
							}
							bellCount = false;
						}

					} else if (command.startsWith("CHAT")) {
						getMsg().sendToAll(command);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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

	public int size() {
		return list.size();
	}

	public boolean sumFive() {
		int[] sum = new int[4];
		for (int i = 0; i < 4 && i < list.size(); i++) {
			switch (list.get(i).getFruit()) {
			case "딸기":
				sum[0] += list.get(i).getNumber();
				break;
			case "바나":
				sum[1] += list.get(i).getNumber();
				break;
			case "라임":
				sum[2] += list.get(i).getNumber();
				break;
			case "자두":
				sum[3] += list.get(i).getNumber();
				break;
			}
		}

		for (int i = 0; i < 4; i++) {
			if (sum[i] == 5)
				return true;
		}

		return false;
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