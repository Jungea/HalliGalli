package halligalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class MiniHGServer {

	Manager[] mng = new Manager[3];
	int mngNum = 0;

	public static void main(String[] args) {

		MiniHGServer server = new MiniHGServer();
		server.startServer();
	}

	public void serverInit() {

	}

	public void startServer() {
		// TODO Auto-generated method stub
		ServerSocket ss;
		try {
			ss = new ServerSocket(8885);

			System.out.println("�̴� �Ҹ����� ������ ���۵Ǿ����ϴ�.");

			mng[0] = new Manager(4, new Table());

			while (true) {

				Player p = new Player(ss.accept(), mngNum);
				mng[mngNum].add(p);

				if (mng[mngNum].addI.isEmpty()) {
					mngNum++;
					mng[mngNum] = new Manager(4, new Table());
				}

				p.start();

				System.out.println("�÷��̾� ����");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void newGame(int mngId) {
		mng[mngId].initMng();
		Deck d = new Deck(); // 56���� ī�� �� ����
		d.shuffle();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 14; j++) // 14�徿 ��
				mng[mngId].player[i].addPlayerCard(d.deal());

		mng[mngId].sendToAll("NEWGAME");

	}

	public void initGame(int mngId) {
		for (int i = 0; i < 4; i++)
			mng[mngId].player[i].initPlayer();
		mng[mngId].readyCount = 0;

		mng[mngId].sendToAll("NOTI �����Ͻÿ�!");
	}

	// �ش� ���ӹ��� �Ŵ��� Ŭ����
	class Manager {
		Table table;
		int playerSize;
		Player[] player;
		Stack<Integer> addI = new Stack<>();
		int readyCount = 0; // ������ �ο�
		boolean[] dead = new boolean[4]; // ������ true/ ��� false
		int nowPlayer = 0; // ���� �÷��̾�
		boolean bellClick = false; // �� Ŭ�� ����

		public Manager(int playerSize, Table table) {
			this.playerSize = playerSize;
			player = new Player[playerSize];
			for (int i = playerSize - 1; i >= 0; i--)
				addI.add(i);

			this.table = table;
		}

		public void initMng() {
			table = new Table();

			dead = new boolean[4];
			nowPlayer = 0;
			bellClick = false;
		}

		public void add(Player p) {
			int id = addI.pop();
			player[id] = p;
			p.playerId = id;
			p.table = table;
		}

		public void sendTo(int playerId, String msg) // �÷��̾� playerId ���� �޽����� ����.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // ��� �÷��̾�� �޽��� ����
		{
			for (int i = 0; i < playerSize; i++) {
				if (player[i] != null)
					sendTo(i, msg);
			}
		}

		//// ���� �� �޼ҵ�
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
	}

	// �÷��̾��� ������ ��� Ŭ����
	class Player extends Thread {
		Table table;
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		int playerId; // �ش� �÷��̾� ��ȣ
		int mngId; // �ش� ���ӹ� �Ŵ��� ��ü ��ȣ
		boolean ready;

		private LinkedList<Card> list = new LinkedList<Card>(); // ī�� ����Ʈ

		public Player(Socket socket, int mngNum) {
			this.socket = socket;
			this.mngId = mngNum;

			try {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("������ ���������ϴ� . " + e);
			}
		}

		public void initPlayer() {
			list = new LinkedList<Card>();
			ready = false;
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

		public int size() { // ���� ī�� ����
			return list.size();
		}

		/*
		 * NOW : �����÷��̾� / PRINT : �����˸� ���̺� ǥ��/ NOTI : ���� ����������� ä��â�� ǥ�� / TURN : �÷��̾
		 * TURN��ư�� Ŭ�� / REPAINT : Ŭ���̾��� ī�峪 ���� ī�� ���� ��ǥ�� / DIE : �÷��̾� ���� / WIN : �÷��̾� �̱�
		 * / BELL : �÷��̾ BELL��ư�� Ŭ�� / CHAT : ä���� �������� ä��â�� ǥ�� /
		 */

		public void run() {
			output.println("START " + mngId + " " + this.playerId);
			output.println("PRINT �ٸ� ����ڸ� ��ٸ��ϴ�.");
//			output.println("PRINT ��� ����ڰ� ����Ǿ����ϴ�.");
			try {
				String command;

				while ((command = input.readLine()) != null) {
					if (command.startsWith("READY")) {
						if (!ready) {
							ready = true;
							++(mng[mngId].readyCount);
							mng[mngId].sendToAll("NOTI player" + playerId + " �غ� �Ϸ�!");
							mng[mngId].sendToAll("NOTI ���� �غ� �Ϸ� : " + mng[mngId].readyCount);

							if (mng[mngId].readyCount == 4) {
								newGame(mngId);
								mng[mngId].sendToAll("PRINT ������ �����մϴ�.");
								mng[mngId].sendToAll("NOTI ������ �����մϴ�.");
								mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
								mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " �����Դϴ�.");
							}
						} else {
							ready = false;
							--(mng[mngId].readyCount);
							mng[mngId].sendToAll("NOTI player" + playerId + " �غ� ����!");
							mng[mngId].sendToAll("NOTI ���� �غ� �Ϸ� : " + mng[mngId].readyCount);
						}

					} else if (command.startsWith("TURN")) {
						if (!mng[mngId].dead[playerId] && size() > 0) {
							mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " ī�带 ���������ϴ�.");
							mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " ī�带 ���������ϴ�.");

							Card removeCard = removePlayerCard();
							table.addTableCard(removeCard, playerId);
							mng[mngId].sendToAll(
									"REPAINT /" + mng[mngId].nowPlayer + "/" + removeCard + "/" + list.size());

							if (list.size() == 0) { // ���� ī�� ���� 0��
								mng[mngId].die(mng[mngId].nowPlayer);
								mng[mngId].sendToAll("DIE " + mng[mngId].nowPlayer);
								mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " ���ӿ���.");

								if (mng[mngId].aliveCount() == 1) { // ���� ��� 1��
									mng[mngId].sendToAll("WIN " + mng[mngId].winner());
									mng[mngId].sendToAll("NOTI -----[[�¸�]] player" + mng[mngId].winner() + " -----");
									continue;
								}
							}
							mng[mngId].nextPlayer();

							mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
							mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
							mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " �����Դϴ�.");
						}
					} else if (command.startsWith("BELL")) {

						if (mng[mngId].bellClick) { // �̹� ���� ���� Ŭ������ �� (��ġ�� ����/��ġ�� ����)
							output.println("�ʾ����ϴ�.");

						} else {
							mng[mngId].bellClick = true;
							mng[mngId].sendToAll("PRINT player" + playerId + "�� ��ħ.");
							mng[mngId].sendToAll("NOTI player" + playerId + "�� ��ħ.");

							if (!table.sumFive(/* mng[mngId] */)) { // ��ġ�� ����(������ �ټ����� �ƴϸ�)

								mng[mngId].sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
								mng[mngId].sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

								if (list.size() < mng[mngId].aliveCount()) { // ������ ī�尡 ������ ��
									mng[mngId].die(playerId);
									mng[mngId].sendToAll("DIE " + playerId);
									mng[mngId].sendToAll("NOTI player" + playerId + " ���ӿ���.");

									if (mng[mngId].aliveCount() == 1) {
										mng[mngId].sendToAll("WIN " + mng[mngId].winner());
										mng[mngId]
												.sendToAll("NOTI -----[[�¸�]] player" + mng[mngId].winner() + " -----");
									}

									else if (playerId == mng[mngId].nowPlayer) { // �� �����ε� ��ġ�� �����Ͽ� ���� ī�尡 ���� ��
										mng[mngId].nextPlayer();
										mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
										mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
										mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " �����Դϴ�.");
									}

								}

								mng[mngId].sendToCardOther(playerId);
								mng[mngId].sendToAll("REPAINT /" + playerId + "/" + list.size());

								if (mng[mngId].dead[playerId] && table.playerCard[playerId] == null) // ���̺� �ѱ� ī�尡 ����
																										// �׾���
																										// ��
									mng[mngId].sendToAll("REPAINT /" + playerId + "//" + size());
							}

							else { // ��ġ�� ����
								while (table.size() > 0) // ���̺��� ��� ī�� �÷��̾��
									addPlayerCard(table.removeTableCard());

								mng[mngId].sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
								mng[mngId].sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

								mng[mngId].bellRepaint(); // �÷��̾� ���� ����

								// ��ġ�� ������ �÷��̾���� �̾ ����
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " ī�带 ���������ϴ�.");
								mng[mngId].nowPlayer = playerId;
								mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
								mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " �����Դϴ�.");

							}
							mng[mngId].bellClick = false;
						}

					} else if (command.startsWith("CHAT")) { // �÷��̾ ä���� �Է��Ͽ��� ��
						mng[mngId].sendToAll(command);
					} else if (command.startsWith("NOTI")) {
						mng[mngId].sendToAll(command);
					} else if (command.startsWith("NEWGAME")) {
						initGame(mngId);

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