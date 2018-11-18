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
	boolean[] dead = new boolean[4]; // ������ true/ ��� false
	int nowPlayer = 0;
	ArrayList<Manager> mngList = new ArrayList<Manager>();
	int mngNum = 0;
	Manager mng = new Manager();
	boolean bellClick = false;

	public void nextPlayer() { // ���� �÷��̾�� ����
		do {
			nowPlayer = (nowPlayer + 1) % 4;
		} while (dead[nowPlayer]);
	}

	public void die(int playerId) { // �����������
		dead[playerId] = true;
	}

	public int aliveCount() { // ���� ����
		int sum = 0;
		for (int i = 0; i < dead.length; i++)
			if (!dead[i])
				sum++;
		return sum;
	}

	public int winner() { // aliveCount()�� 1�� �� ���ڿ��� ���(���� id) ����
		for (int i = 0; i < dead.length; i++)
			if (!dead[i])
				return i;

		return -1;
	}

	public static void main(String[] args) {

		MiniHGServer server = new MiniHGServer();
		server.startServer();
	}

	public void startServer() {
		// TODO Auto-generated method stub
		ServerSocket ss;
		try {
			ss = new ServerSocket(8886);

			System.out.println("�̴� �Ҹ����� ������ ���۵Ǿ����ϴ�.");

			while (true) {

				Table table = new Table();
				Deck d = new Deck(); // 56���� ī�� �� ����
				d.shuffle();

				Player[] player = new Player[4];
				for (int i = 0; i < 4; i++) {
					player[i] = new Player(table, ss.accept(), i, mngNum);
					mng.add(player[i]);
					for (int j = 0; j < 14; j++) // 14�徿 ��
						player[i].addPlayerCard(d.deal());
				}
				mngNum++;
				mngList.add(mng);

				for (int i = 0; i < 4; i++) {
					System.out.print("�÷��̾�" + i + "�� ī�� ��� = ");
					player[i].showPlayerCards(); // �÷��̾��� ī�� ���
					player[i].start();
				}
				System.out.println("�� ����� �����ϴ�. ");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// �÷��̾�� �ൿ�� ���ִ� Ŭ����
	class Manager {

		Player[] player = new Player[4];
		int addI = 0;

		public void add(Player p) {
			player[addI++] = p;
		}

		public void sendTo(int playerId, String msg) // �÷��̾� playerId ���� �޽����� ����.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // ��� �÷��̾�� �޽��� ����
		{
			for (int i = 0; i < player.length; i++) {
				sendTo(i, msg);
			}
		}

		public void sendToCard(int myId, int otherId) // �÷��̾� otherId ���� ī�� ����.
		{
			player[otherId].addPlayerCard(player[myId].removePlayerCard());
			sendToAll("REPAINT /" + otherId + "/" + player[otherId].size());
		}

		public void sendToCardOther(int myId) // ��ġ�� ����(�ٸ� �÷��̾�� ī�� ����)
		{
			for (int i = 0; i < player.length; i++) {
				if (i != myId && !dead[i]) // ���� �ƴ� �� ������Ը�
					sendToCard(myId, i);
				if (player[myId].size() == 0) // �� ���� ī�� ����������
					break;
			}
		}

		public void bellRepaint() { // ��ġ�� �����Ͽ� ī�� ����
			for (int i = 0; i < player.length; i++) {
				sendToAll("REPAINT /" + i + "//" + player[i].size());
			}
		}
	}

	// �÷��̾��� ������ ��� Ŭ����
	class Player extends Thread {
		Table table;
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		int playerId; // �ش� �÷��̾� ��ȣ
		int mngId; // �ش� ���ӹ� �Ŵ��� ��ü ��ȣ

		private LinkedList<Card> list = new LinkedList<Card>(); // ī�� ����Ʈ

		public Player(Table table, Socket socket, int playerId, int mngNum) {
			this.table = table;
			this.socket = socket;
			this.playerId = playerId;
			this.mngId = mngNum;

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

		@Override
		public String toString() {
			return "{ player" + playerId + "�Դϴ�} \n";
		}

		// �÷��̾� ī�� �߰�(Bell����/�ٸ� �÷��̾��� Bell����)
		public void addPlayerCard(Card c) {
			list.add(c);
		}

		// �÷��̾� ī�� ����(Turn/Bell����)
		public Card removePlayerCard() {
			return list.remove();
		}

		public void showPlayerCards() { // ��ü ī�� ����Ʈ ���
			System.out.println(list);
		}

		public Manager getMng() {
			return mngList.get(mngId);
		}

		public int size() { // ���� ī�� ����
			return list.size();
		}

		/*
		 * NOW : �����÷��̾� / PRINT : �����˸� ���̺� ǥ��/ NOTI : ���� ����������� ä��â�� ǥ�� / TURN : �÷��̾
		 * TURN��ư�� Ŭ�� / REPAINT : Ŭ���̾��� ī�峪 ���� ī�� ���� ��ǥ�� / DIE : �÷��̾� ���� / WIN : �÷��̾� �̱�
		 * / BELL : �÷��̾ BELL��ư�� Ŭ�� / CHAT : ä���� �������� ä��â�� ǥ�� /
		 */

		public void run() {
			output.println("PRINT ��� ����ڰ� ����Ǿ����ϴ�.");
			try {
				String command;

				getMng().sendToAll("NOW " + nowPlayer);
				getMng().sendToAll("PRINT player" + nowPlayer + " �����Դϴ�.");
				if (nowPlayer == playerId)
					getMng().sendToAll("NOTI player" + nowPlayer + " �����Դϴ�.");

				while ((command = input.readLine()) != null) {

					if (command.startsWith("TURN")) {
						getMng().sendToAll("PRINT player" + nowPlayer + " ī�带 ���������ϴ�.");
						getMng().sendToAll("NOTI player" + nowPlayer + " ī�带 ���������ϴ�.");

						Card removeCard = removePlayerCard();
						table.addTableCard(removeCard, playerId);
						getMng().sendToAll("REPAINT /" + nowPlayer + "/" + removeCard + "/" + list.size());

						if (list.size() == 0) { // ���� ī�� ���� 0��
							die(nowPlayer);
							getMng().sendToAll("DIE " + nowPlayer);
							getMng().sendToAll("NOTI player" + nowPlayer + " ���ӿ���.");

							if (aliveCount() == 1) { // ���� ��� 1��
								getMng().sendToAll("WIN " + winner());
								getMng().sendToAll("NOTI -----[[�¸�]] player" + winner() + " -----");
							}
						}
						nextPlayer();

						getMng().sendToAll("NOW " + nowPlayer);
						getMng().sendToAll("PRINT player" + nowPlayer + " �����Դϴ�.");
						getMng().sendToAll("NOTI player" + nowPlayer + " �����Դϴ�.");

					} else if (command.startsWith("BELL")) {

						if (bellClick) { // �̹� ���� ���� Ŭ������ �� (��ġ�� ����/��ġ�� ����)
							output.println("�ʾ����ϴ�.");

						} else {
							bellClick = true;
							getMng().sendToAll("PRINT player" + playerId + "�� ��ħ.");
							getMng().sendToAll("NOTI player" + playerId + "�� ��ħ.");

							if (!table.sumFive(/* getmng() */)) { // ��ġ�� ����(������ �ټ����� �ƴϸ�)

								getMng().sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
								getMng().sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

								if (list.size() < aliveCount()) { // ������ ī�尡 ������ ��
									die(playerId);
									getMng().sendToAll("DIE " + playerId);
									getMng().sendToAll("NOTI player" + playerId + " ���ӿ���.");

									if (aliveCount() == 1) {
										getMng().sendToAll("WIN " + winner());
										getMng().sendToAll("NOTI -----[[�¸�]] player" + winner() + " -----");
									}

									else if (playerId == nowPlayer) { // �� �����ε� ��ġ�� �����Ͽ� ���� ī�尡 ���� ��
										nextPlayer();
										getMng().sendToAll("NOW " + nowPlayer);
										getMng().sendToAll("PRINT player" + nowPlayer + " �����Դϴ�.");
										getMng().sendToAll("NOTI player" + nowPlayer + " �����Դϴ�.");
									}

								}

								getMng().sendToCardOther(playerId);
								getMng().sendToAll("REPAINT /" + playerId + "/" + list.size());

								if (dead[playerId] && table.playerCard[playerId] == null) // ���̺� �ѱ� ī�尡 ���� �׾��� ��
									getMng().sendToAll("REPAINT /" + playerId + "//" + size());
							}

							else { // ��ġ�� ����
								while (table.size() > 0) // ���̺��� ��� ī�� �÷��̾��
									addPlayerCard(table.removeTableCard());

								getMng().sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
								getMng().sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

								getMng().bellRepaint(); // �÷��̾� ���� ����

								// ��ġ�� ������ �÷��̾���� �̾ ����
								getMng().sendToAll("PRINT player" + nowPlayer + " ī�带 ���������ϴ�.");
								nowPlayer = playerId;
								getMng().sendToAll("NOW " + nowPlayer);
								getMng().sendToAll("PRINT player" + nowPlayer + " �����Դϴ�.");
								getMng().sendToAll("NOTI player" + nowPlayer + " �����Դϴ�.");

							}
							bellClick = false;
						}

					} else if (command.startsWith("CHAT")) { // �÷��̾ ä���� �Է��Ͽ��� ��
						getMng().sendToAll(command);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

//���̺��� ������ ���� Ŭ����
class Table {
	private LinkedList<Card> list = new LinkedList<Card>(); // �÷��̾ �ѱ� ī�� ����Ʈ
	Card[] playerCard = new Card[4]; // ���� ���̴� ī��

	// ���̺� ī�� �߰�(�÷��̾ TURN)
	public void addTableCard(Card c, int playerId) {
		list.addFirst(c);
		playerCard[playerId] = c;
	}

	// ���̺� ī�� ����(�÷��̾� BELL)
	public Card removeTableCard() {
		for (int i = 0; i < 4; i++) { // ���̴� ī�� �ʱ�ȭ
			playerCard[i] = null;
		}
		return list.remove();
	}

	public void showTableList() {
		System.out.println("TABLE : " + list);
	}

	public int size() {
		return list.size();
	}

	public boolean sumFive(/* Manager mng */) { // ������ �ټ������� Ȯ���� �ִ� �޼ҵ�
		int[] sum = new int[4]; // �� ������ �� ��

		for (int i = 0; i < 4; i++)
			if (playerCard[i] != null) // ������ ī�常
				sum[playerCard[i].getFruit()] += playerCard[i].getNumber();

//		mng.sendToAll("NOTI " + Arrays.toString(sum));
		for (int i = 0; i < 4; i++) {
			if (sum[i] == 5)
				return true;
		}

		return false;
	}
}

class Card { // ī�� �� ���� ǥ���ϴ� Ŭ���� Card
	private int fruit; // ���� ���(0~3)
	private int number; // ī�� ��ȣ(0~4)

	public Card(int fruit, int number) {
		this.fruit = fruit;
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public int getFruit() {
		return fruit;
	}

	public String toString() {
		return fruit + " " + number;
	}

}

class Deck { // ī�� 56���� �ٷ�� ���� ǥ���ϴ� Ŭ���� Deck
	private LinkedList<Card> deck = new LinkedList<Card>();
	private int[] count = { 5, 3, 3, 2, 1 };

	// ī�带 �����Ͽ� ���� ����
	public Deck() {
		for (int i = 0; i < 4; i++)
			for (int j = 1, k = 0; k < 5; j++) {
				deck.add(new Card(i, k + 1));
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