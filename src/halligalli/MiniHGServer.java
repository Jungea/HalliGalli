package halligalli;

/*
 * ����
 * 1. ���� ���� �� BELL�� �����Ͽ� ī�尡 ���� ��
 * 2. BELL ���н� ī�带 �����ְ� ���� ������ */

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
	boolean[] dead = new boolean[4]; // �׾�����
	int nowPlayer = 0;
	ArrayList<Messenger> msgList = new ArrayList<Messenger>();
	int msgNum = 0;
	Messenger msg = new Messenger();
	boolean bellCount = false;

	public void nextPlayer() { // ���� �÷��̾�
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

			System.out.println("�̴� �Ҹ����� ������ ���۵Ǿ����ϴ�.");

			while (true) {

				Table table = new Table();
				Deck d = new Deck(); // 56���� ī�� �� ����
				d.shuffle();

				Player[] player = new Player[4];
				for (int i = 0; i < 4; i++) {
					player[i] = new Player(table, ss.accept(), i, msgNum);
					msg.add(player[i]);
					for (int j = 0; j < 14; j++) // 14�徿 ��
						player[i].addPlayerCard(d.deal());
				}
				msgNum++;
				msgList.add(msg);

				for (int i = 0; i < 4; i++) {
					player[i].start();
				}
				System.out.println("�� ����� �����ϴ�. ");

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

		public Socket getSocket(int i) // ������ �����´�.
		{
			System.out.println(player[i]);
			return player[i].getSocket();
		}

		public void sendTo(int playerId, String msg) // i�� �÷��̾�� �޽����� ����.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // ��� �÷��̾�� ������ �޽���
		{
			for (int i = 0; i < player.length; i++) {
				sendTo(i, msg);
			}
		}

		public void sendToCard(int myId, int otherId) // i�� �÷��̾�� ī�� ����.
		{
			player[otherId].addPlayerCard(player[myId].removePlayerCard());
			sendToAll("REPAINT /" + otherId + "/" + player[otherId].size());
		}

		public void sendToCardOther(int myId) // ��ġ�� ����(�ٸ� �÷��̾�� ī�� ����)
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
				output.println("PRINT �ٸ� ����ڸ� ��ٸ��ϴ�.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("������ ���������ϴ� . " + e);
			}
		}

		public Socket getSocket() {
			System.out.println("b");
			return socket;
		}

		@Override
		public String toString() {
			return "{ player" + playerId + "�Դϴ�} \n";
		}

		// �÷��̾� ī�� �߰�(Bell����/�ٸ� �÷��̾��� Bell����)
		public void addPlayerCard(Card c) {
			list.addLast(c);
		}

		// �÷��̾� ī�� ����(Turn/Bell����)
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
			output.println("PRINT ��� ����ڰ� ����Ǿ����ϴ�.");
			try {
				String command;

				getMsg().sendToAll("NOW " + nowPlayer);
				getMsg().sendToAll("PRINT player" + nowPlayer + " �����Դϴ�."); // info
				if (nowPlayer == playerId)
					getMsg().sendToAll("NOTI player" + nowPlayer + " �����Դϴ�."); // noti

				while ((command = input.readLine()) != null) {

					if (command.startsWith("TURN")) {
						getMsg().sendToAll("PRINT player" + nowPlayer + " ī�带 ���������ϴ�.");
						getMsg().sendToAll("NOTI player" + nowPlayer + " ī�带 ���������ϴ�.");

						Card removeCard = removePlayerCard();
						table.addTableCard(removeCard);
						getMsg().sendToAll("REPAINT /" + nowPlayer + "/" + removeCard + "/" + list.size());

						if (list.size() == 0)
							die(nowPlayer);
						nextPlayer();

						getMsg().sendToAll("NOW " + nowPlayer);
						getMsg().sendToAll("PRINT player" + nowPlayer + " �����Դϴ�.");
						getMsg().sendToAll("NOTI player" + nowPlayer + " �����Դϴ�.");

					} else if (command.startsWith("BELL")) {
						if (bellCount) {
							output.println("�ʾ����ϴ�.");
						} else {
							bellCount = true;
							int bellPlayerId = (command.charAt(5) - 48);
							getMsg().sendToAll("PRINT player" + bellPlayerId + "�� ��ħ.");
							getMsg().sendToAll("NOTI player" + bellPlayerId + "�� ��ħ.");

							if (!table.sumFive()) {
								getMsg().sendToAll("PRINT player" + bellPlayerId + " ��ġ�� ����.");
								getMsg().sendToAll("NOTI player" + bellPlayerId + " ��ġ�� ����.");

								if (list.size() < 4)
									die(bellPlayerId);
								getMsg().sendToCardOther(playerId);
								getMsg().sendToAll("REPAINT /" + bellPlayerId + "/" + list.size());
							}

							else {
								while (table.size() > 0)
									addPlayerCard(table.removeTableCard());
								getMsg().sendToAll("PRINT player" + bellPlayerId + " ��ġ�� ����.");
								getMsg().sendToAll("NOTI player" + bellPlayerId + " ��ġ�� ����.");

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

	// ���̺� ī�� �߰�(�÷��̾ TURN)
	public void addTableCard(Card c) {
		list.addFirst(c);
	}

	// ���̺� ī�� ����(�÷��̾� BELL)
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
			case "����":
				sum[0] += list.get(i).getNumber();
				break;
			case "�ٳ�":
				sum[1] += list.get(i).getNumber();
				break;
			case "����":
				sum[2] += list.get(i).getNumber();
				break;
			case "�ڵ�":
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