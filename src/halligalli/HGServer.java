package halligalli;

/*
 * �ۼ���: ������
 * �Ҹ����� ���� ����
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HGServer {
	WManager waitingRoomMng;
	Manager[] mng = new Manager[3];
	int n = 0; // timer���� ����ϴ� ����
	Timer timer;
	TimerTask task;

	public static void main(String[] args) {

		HGServer server = new HGServer();
		server.startServer();
	}

	public void startServer() {
		// TODO Auto-generated method stub
		ServerSocket ss;
		try {
			ss = new ServerSocket(8885);

			System.out.println("�Ҹ����� ������ ���۵Ǿ����ϴ�.");

			waitingRoomMng = new WManager(16);
			mng[0] = new Manager(4, new Table());
			mng[1] = new Manager(4, new Table());
			mng[2] = new Manager(4, new Table());

			while (true) {

				Player p = new Player(ss.accept());

				if (waitingRoomMng.addI.isEmpty()) { // ���� �ο��ʰ�
					try {
						if (p.input != null)
							p.input.close();
						if (p.output != null)
							p.output.close();
						if (p.socket != null)
							p.socket.close();
						p.input = null;
						p.output = null;
						p.socket = null;
						System.out.println("������ �ο��� ����á���ϴ�.");

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					waitingRoomMng.add(p);

					p.start();
					System.out.println("�������� ����  \n( ������ �� = " + waitingRoomMng.enterNum() + "�� )");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void endGame(int mngId) { // ���� ���� �� �ʱ�ȭ Ÿ�̸�
		n = 5;
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (n == 0) {
					timer.cancel();
					initGame(mngId);
				} else {
					mng[mngId].sendToAll("NOTI " + n + "�� �� �ʱ�ȭ");
					n--;
				}
			}
		};
		timer.schedule(task, 1000, 1000);
	}

	public void initGame(int mngId) { // �׷��� �ʱ�ȭ
		for (int i = 0; i < 4; i++)
			mng[mngId].player[i].initPlayer();
		mng[mngId].readyCount = 0;

		mng[mngId].sendToAll("INIT");
		mng[mngId].sendToAll("NOTI �����Ͻÿ�!");
	}

	public void newGame(int mngId) { // ���� ���� �ʱ�ȭ
		mng[mngId].initMng();
		Deck d = new Deck(); // 56���� ī�� �� ����
		d.shuffle();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 14; j++) // 14�徿 ��
				mng[mngId].player[i].addPlayerCard(d.deal());

	}

	/* sendTo, sendToAll ���� : http://mudchobo.tistory.com/2 */

	class WManager { // ���� �Ŵ���
		int playerSize;
		Player[] player;
		List<Integer> addI = new LinkedList<>(); // no�Ҵ�

		public WManager(int playerSize) {
			this.playerSize = playerSize;
			player = new Player[playerSize];
			for (int i = 0; i < playerSize; i++)
				addI.add(i);
		}

		public int enterNum() { // ���� ���� �ο�
			return playerSize - addI.size();
		}

		public void add(Player p) { // ���� ����(connect)
			int id = addI.remove(0);
			player[id] = p;
			p.no = id;
		}

		public void remove(Player p) { // �÷��̾� ���� ���� (�ش� NO �ݳ�)
			player[p.no] = null;
			addI.add(p.no);
			Collections.sort(addI);
		}

		public void sendTo(int playerNo, String msg) // �÷��̾� playerNo ���� �޽����� ����.
		{
			player[playerNo].output.println(msg);
		}

		public void sendToAll(String msg) // ��� �÷��̾�� �޽��� ����
		{
			int size = enterNum();
			for (int i = 0, j = 0; j < size; i++)
				if (player[i] != null) {
					sendTo(i, msg);
					j++;
				}

		}

		public void update() { // ���� ���ο� ��� ����(connect)
			int size = enterNum();

			sendToAll("WNEW /" + size);
			for (int i = 0, j = 0; j < size; i++) {
				if (player[i] != null) {
					sendToAll("      " + player[i].no + "     |    " + player[i].name);
					j++;
				}
			}
			sendToAll(mng[0].enterNum() + "/" + mng[1].enterNum() + "/" + mng[2].enterNum());

		}
	}

	// ���ӹ��� �Ŵ��� Ŭ����
	class Manager {
		Table table;
		int playerSize;
		Player[] player;
		List<Integer> addI = new LinkedList<>(); // id
		int readyCount = 0; // ������ �ο� ��
		boolean[] dead = new boolean[4]; // ������ true, ��� false
		int nowPlayer = 0; // ���� �÷��̾�
		boolean bellClick = false; // �� Ŭ�� ����
		int pNum = 1; // 1�� waitingRoom 2�� gameRoom

		public Manager(int playerSize, Table table) {
			this.playerSize = playerSize;
			player = new Player[playerSize];
			for (int i = 0; i < playerSize; i++)
				addI.add(i);

			this.table = table;
		}

		public void initMng() { // ���� ���� �ʱ�ȭ
			table = new Table();
			dead = new boolean[4];
			nowPlayer = 0;
			bellClick = false;
		}

		public int enterNum() { // ���ӹ� ���� �ο�
			return playerSize - addI.size();
		}

		public void add(Player p) { // ���ӹ� ����
			int id = addI.remove(0);
			player[id] = p;
			p.playerId = id;
			p.table = table;
		}

		public void remove(Player p) { // ���ӹ� ������ (ID �ݳ�)
			addI.add(p.playerId);
			Collections.sort(addI);
			player[p.playerId] = null;
		}

		public void sendTo(int playerID, String msg) // �÷��̾� playerID ���� �޽����� ����.
		{
			player[playerID].output.println(msg);
		}

		public void sendToAll(String msg) // ��� �÷��̾�� �޽��� ����
		{
			int size = enterNum();
			for (int i = 0, j = 0; j < size; i++) {
				if (player[i] != null) {
					sendTo(i, msg);
					j++;
				}
			}
		}

		public void updatePlayer() { // ���ӹ濡 �÷��̾� ������Ʈ

			sendToAll("NEW ");
			for (int i = 0; i < playerSize; i++) {
				if (player[i] == null)
					sendToAll("null");
				else
					sendToAll(player[i].playerId + "/" + player[i].name);

			}

		}

		// ���� �� �޼ҵ�
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

		public void die(int playerId) { // ����������� ����
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
		int mngId = -1; // �ش� ���ӹ� �Ŵ��� ��ü ��ȣ
		boolean ready;
		int pNum = 1;
		int no;
		String name;

		private List<Card> list = new LinkedList<>(); // ī�� ����Ʈ

		public Player(Socket socket) {
			this.socket = socket;

			try {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("������ ���������ϴ� . " + e);
			}
		}

		public void initPlayer() { // �÷��̾� ���Ӻ��� �ʱ�ȭ
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
			return list.remove(0);
		}

		public void showPlayerCards() { // ��ü ī�� ����Ʈ ���
			System.out.println(list);
		}

		public int size() { // ���� ī�� ����
			return list.size();
		}

		public void run() {
			try {
				String command = input.readLine();
				if (command.startsWith("CONNECT")) { // ����
					name = command.substring(8);
					output.println("NO " + no);
					waitingRoomMng.update();
				}
				while ((command = input.readLine()) != null) {
					if (pNum == 1) { // ����
						if (command.startsWith("WCHAT")) { // �÷��̾ ä���� �Է�
							waitingRoomMng.sendToAll(command);

						} else if (command.startsWith("ENTER")) { // �����ư Ŭ��
							int i = -1;

							if (command.length() > 5) // ���ư�� Ŭ��
								i = command.charAt(6) - 48;
							else { // ���� Ŭ��
								for (i = 0; i < 3; i++)
									if (!(mng[i].enterNum() == 4))
										break;
							}

							mngId = i;

							if (mngId == -1)
								output.println("NOTI ���� ���� á���ϴ�.");
							else {
								System.out.println(i + "���� ����");
								mng[i].add(this);
								mngId = i;
								output.println("ENTER ����");
								pNum = 2;
								waitingRoomMng.sendToAll("ENTER /" + i + "/" + mng[i].enterNum());

								output.println("START " + mngId + " " + playerId);
								mng[mngId].sendToAll("NOTI " + name + " ����");
								output.println("NOTI ���� �غ� �Ϸ� : " + mng[mngId].readyCount);
								output.println("PRINT �ٸ� ����ڸ� ��ٸ��ϴ�.");
								mng[mngId].updatePlayer();
							}

							continue;
						}

					} else { // pNum==2 ���ӹ�

						if (command.startsWith("READY")) {
							if (!ready) {
								ready = true;
								++(mng[mngId].readyCount);
								mng[mngId].sendToAll("NOTI player" + playerId + " �غ� �Ϸ�!");
								mng[mngId].sendToAll("NOTI ���� �غ� �Ϸ� : " + mng[mngId].readyCount);

								if (mng[mngId].readyCount == 4) { // 4���� �Ǹ� ����
									newGame(mngId);
									mng[mngId].sendToAll("PRINT ������ �����մϴ�.");
									mng[mngId].sendToAll("NOTI ������ �����մϴ�.");
									mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
								}
							} else { // �̹� ����(��������)
								ready = false;
								--(mng[mngId].readyCount);
								mng[mngId].sendToAll("NOTI player" + playerId + " �غ� ����!");
								mng[mngId].sendToAll("NOTI ���� �غ� �Ϸ� : " + mng[mngId].readyCount);
							}

						} else if (command.startsWith("TURN")) { // ī�� ������.
							if (!mng[mngId].dead[playerId] && size() > 0) {
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " ī�带 ���������ϴ�.");

								Card removeCard = removePlayerCard();
								table.addTableCard(removeCard, playerId);
								mng[mngId].sendToAll(
										"REPAINT /" + mng[mngId].nowPlayer + "/" + removeCard + "/" + list.size());

								if (list.size() == 0) { // ���� ī�� ���� 0�� ����.
									mng[mngId].die(mng[mngId].nowPlayer);
									mng[mngId].sendToAll("DIE " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " ���ӿ���.");

									if (mng[mngId].aliveCount() == 1) { // ���� ��� 1��
										mng[mngId].sendToAll("WIN " + mng[mngId].winner());
										mng[mngId].sendToAll(
												"NOTI --------- [[�¸�]] player" + mng[mngId].winner() + " ---------");
										continue;
									}
								}
								mng[mngId].nextPlayer();

								mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
							}
						} else if (command.startsWith("BELL")) {

							if (mng[mngId].bellClick) { // �̹� ���� ���� Ŭ������ �� (��ġ�� ����/��ġ�� ����)
								output.println("�ʾ����ϴ�.");

							} else {
								mng[mngId].bellClick = true;
								mng[mngId].sendToAll("PRINT player" + playerId + "�� ��ħ.");

								if (!table.sumFive()) { // ��ġ�� ����(������ �ټ����� �ƴϸ�)

									mng[mngId].sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
									mng[mngId].sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

									if (list.size() < mng[mngId].aliveCount()) { // ������ ī�尡 ������ ��
										mng[mngId].die(playerId);
										mng[mngId].sendToAll("DIE " + playerId);
										mng[mngId].sendToAll("NOTI player" + playerId + " ���ӿ���.");

										if (mng[mngId].aliveCount() == 1) {
											mng[mngId].sendToAll("WIN " + mng[mngId].winner());
											mng[mngId].sendToAll("NOTI --------- [[�¸�]] player" + mng[mngId].winner()
													+ " ---------");
										}

										else if (playerId == mng[mngId].nowPlayer) { // �� �����ε� ��ġ�� �����Ͽ� ���� ī�尡 ���� ��
											mng[mngId].nextPlayer();
											mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
											mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " �����Դϴ�.");
										}

									}

									mng[mngId].sendToCardOther(playerId);
									mng[mngId].sendToAll("REPAINT /" + playerId + "/" + list.size());

									if (mng[mngId].dead[playerId] && table.playerCard[playerId] == null) // ���̺� �ѱ�
																											// ī�尡
																											// ����
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

								}
								mng[mngId].bellClick = false;
							}

						} else if (command.startsWith("CHAT")) { // �÷��̾ ä���� �Է��Ͽ��� ��
							mng[mngId].sendToAll(command);
						} else if (command.startsWith("NOTI")) { // ä�� â�� ǥ�õǴ� ���� ���� ����
							mng[mngId].sendToAll(command);
						} else if (command.startsWith("END")) { // ���� ����(�ʱ�ȭ Ÿ�̸� ����)
							endGame(mngId);

						} else if (command.startsWith("EXIT")) { // ����
							mng[mngId].sendToAll("NOTI player" + playerId + " ����");
							mng[mngId].remove(this);
							mng[mngId].updatePlayer();
							mngId = -1;
							pNum = 1;
							waitingRoomMng.update();
						}
					}

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
			} finally {
				try {
					waitingRoomMng.remove(this);
					if (input != null)
						input.close();

					if (output != null)
						output.close();

					if (socket != null)
						socket.close();

					input = null;
					output = null;
					socket = null;
					System.out.println("�Ҹ����� ���� ������ �����մϴ�. \n( ������ �� = " + waitingRoomMng.enterNum() + "�� )");
					waitingRoomMng.update();
				} catch (Exception e) {
				}
			}
		}
	}

}

//���̺��� ������ ���� Ŭ����
class Table {
	private List<Card> list = new LinkedList<>(); // �÷��̾ �ѱ� ī�� ����Ʈ
	Card[] playerCard = new Card[4]; // ���� ���̴� ī��

	// ���̺� ī�� �߰�(�÷��̾ TURN)
	public void addTableCard(Card c, int playerId) {
		list.add(0, c);
		playerCard[playerId] = c;
	}

	// ���̺� ī�� ����(�÷��̾� BELL)
	public Card removeTableCard() {
		for (int i = 0; i < 4; i++) { // ���̴� ī�� �ʱ�ȭ
			playerCard[i] = null;
		}
		return list.remove(0);
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
