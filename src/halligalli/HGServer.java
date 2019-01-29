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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HGServer {
	WManager waitingRoomMng;

	List<Integer> emptyRoomI = new LinkedList<>(); // ������ ���ȣ
	int r = 0; // ���ȣ�� �� á�� ��

	List<Manager> mng = new LinkedList<>(); // �� ����

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
			ss = new ServerSocket(8883);

			System.out.println("�Ҹ����� ������ ���۵Ǿ����ϴ�.");

			waitingRoomMng = new WManager(16);

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

	public Manager getManager(int id) { // �ش� ���ȣ�� �Ŵ��� ã�� �޼ҵ�
		for (int k = 0; k < mng.size(); k++) {
			if (mng.get(k).managerId == id) {
				return mng.get(k);
			}
		}

		return null;
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
					getManager(mngId).sendToAll("NOTI " + n + "�� �� �ʱ�ȭ");
					n--;
				}
			}
		};
		timer.schedule(task, 1000, 1000);
	}

	public void initGame(int mngId) { // �׷��� �ʱ�ȭ
		for (int i = 0; i < 4; i++)
			getManager(mngId).player[i].initPlayer();
		getManager(mngId).readyCount = 0;

		getManager(mngId).sendToAll("INIT");
		getManager(mngId).sendToAll("NOTI �����Ͻÿ�!");
	}

	public void newGame(int mngId) { // ���� ���� �ʱ�ȭ
		getManager(mngId).initMng();
		Deck d = new Deck(); // 56���� ī�� �� ����
		d.shuffle();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 14; j++) // 14�徿 ��
				getManager(mngId).player[i].addPlayerCard(d.deal());

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
			sendToAll(Integer.toString(mng.size()));
			for (int i = 0; i < mng.size(); i++) {
				sendToAll(mng.get(i).managerId + "/" + mng.get(i).roomName + "/" + mng.get(i).enterNum());
			}

		}

		public void inviteUpdate(int no) { // �ʴ��ư ����Ʈ
			int size = enterNum();
			sendToAll("LIST /" + size);
			for (int i = 0, j = 0; j < size; i++) {
				if (player[i] != null) {
					sendTo(no, "      " + player[i].no + "     |    " + player[i].name);
					j++;
				}
			}
		}
	}

	// ���ӹ��� �Ŵ��� Ŭ����
	class Manager {
		int managerId;
		String roomName;
		Table table;
		int playerSize;
		Player[] player;
		List<Integer> addI = new LinkedList<>(); // id
		int readyCount = 0; // ������ �ο� ��
		boolean[] dead = new boolean[4]; // ������ true, ��� false
		int nowPlayer = 0; // ���� �÷��̾�
		boolean bellClick = false; // �� Ŭ�� ����
		int pNum = 1; // 1�� waitingRoom 2�� gameRoom

		public Manager(int managerId, int playerSize, Table table) {
			this.managerId = managerId;
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

						} else if (command.startsWith("CREATE")) { // �� ����
							int index = r++;
							if (emptyRoomI.size() != 0) { // ������ �� ��ȣ ����
								index = emptyRoomI.remove(0);
								r--;
							}
							Manager m = new Manager(index, 4, new Table());
							m.roomName = command.substring(7);
							mng.add(m);
							waitingRoomMng.sendToAll("CREATE /" + no + "/" + index + "/" + m.roomName);
						}

						else if (command.startsWith("ENTER")) { // �����ư Ŭ��
							int i = -1;
							if (command.length() > 5) // ���ư�� Ŭ��(+������ư)
								mngId = Integer.parseInt(command.substring(6));
							else { // ���� Ŭ��

								for (i = 0; i < mng.size(); i++)
									if (!(mng.get(i).enterNum() == 4))
										break;
								if (i != mng.size())
									mngId = mng.get(i).managerId;
							}

							if (i == mng.size())
								output.println("NOTI ���� ���� á���ϴ�.");
							else {
								System.out.println(mngId + "���� ����");
								getManager(mngId).add(this);
								output.println("ENTER ����");
								pNum = 2;
								waitingRoomMng.sendToAll("ENTER /" + mngId + "/" + getManager(mngId).enterNum());

								output.println("START " + mngId + " " + playerId);
								getManager(mngId).sendToAll("NOTI " + name + " ����");
								output.println("NOTI ���� �غ� �Ϸ� : " + getManager(mngId).readyCount);
								output.println("PRINT �ٸ� ����ڸ� ��ٸ��ϴ�.");
								getManager(mngId).updatePlayer();
							}

							continue;
						}

					} else { // pNum==2 ���ӹ�

						if (command.startsWith("READY")) {
							if (!ready) {
								ready = true;
								++(getManager(mngId).readyCount);
								getManager(mngId).sendToAll("NOTI player" + playerId + " �غ� �Ϸ�!");
								getManager(mngId).sendToAll("NOTI ���� �غ� �Ϸ� : " + getManager(mngId).readyCount);

								if (getManager(mngId).readyCount == 4) { // 4���� �Ǹ� ����
									newGame(mngId);
									getManager(mngId).sendToAll("PRINT ������ �����մϴ�.");
									getManager(mngId).sendToAll("NOTI ������ �����մϴ�.");
									getManager(mngId).sendToAll("NOW " + getManager(mngId).nowPlayer);
									getManager(mngId)
											.sendToAll("PRINT player" + getManager(mngId).nowPlayer + " �����Դϴ�.");
								}
							} else { // �̹� ����(��������)
								ready = false;
								--(getManager(mngId).readyCount);
								getManager(mngId).sendToAll("NOTI player" + playerId + " �غ� ����!");
								getManager(mngId).sendToAll("NOTI ���� �غ� �Ϸ� : " + getManager(mngId).readyCount);
							}

						} else if (command.startsWith("TURN")) { // ī�� ������.
							if (!getManager(mngId).dead[playerId] && size() > 0) {
								getManager(mngId)
										.sendToAll("PRINT player" + getManager(mngId).nowPlayer + " ī�带 ���������ϴ�.");

								Card removeCard = removePlayerCard();
								table.addTableCard(removeCard, playerId);
								getManager(mngId).sendToAll("REPAINT /" + getManager(mngId).nowPlayer + "/" + removeCard
										+ "/" + list.size());

								if (list.size() == 0) { // ���� ī�� ���� 0�� ����.
									getManager(mngId).die(getManager(mngId).nowPlayer);
									getManager(mngId).sendToAll("DIE " + getManager(mngId).nowPlayer);
									getManager(mngId).sendToAll("NOTI player" + getManager(mngId).nowPlayer + " ���ӿ���.");

									if (getManager(mngId).aliveCount() == 1) { // ���� ��� 1��
										getManager(mngId).sendToAll("WIN " + getManager(mngId).winner());
										getManager(mngId).sendToAll("NOTI --------- [[�¸�]] player"
												+ getManager(mngId).winner() + " ---------");
										continue;
									}
								}
								getManager(mngId).nextPlayer();

								getManager(mngId).sendToAll("NOW " + getManager(mngId).nowPlayer);
								getManager(mngId).sendToAll("PRINT player" + getManager(mngId).nowPlayer + " �����Դϴ�.");
							}
						} else if (command.startsWith("BELL")) {

							if (getManager(mngId).bellClick) { // �̹� ���� ���� Ŭ������ �� (��ġ�� ����/��ġ�� ����)
								output.println("�ʾ����ϴ�.");

							} else {
								getManager(mngId).bellClick = true;
								getManager(mngId).sendToAll("PRINT player" + playerId + "�� ��ħ.");

								if (!table.sumFive()) { // ��ġ�� ����(������ �ټ����� �ƴϸ�)

									getManager(mngId).sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
									getManager(mngId).sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

									if (list.size() < getManager(mngId).aliveCount()) { // ������ ī�尡 ������ ��
										getManager(mngId).die(playerId);
										getManager(mngId).sendToAll("DIE " + playerId);
										getManager(mngId).sendToAll("NOTI player" + playerId + " ���ӿ���.");

										if (getManager(mngId).aliveCount() == 1) {
											getManager(mngId).sendToAll("WIN " + getManager(mngId).winner());
											getManager(mngId).sendToAll("NOTI --------- [[�¸�]] player"
													+ getManager(mngId).winner() + " ---------");
										}

										else if (playerId == getManager(mngId).nowPlayer) { // �� �����ε� ��ġ�� �����Ͽ� ���� ī�尡 ����
																							// ��
											getManager(mngId).nextPlayer();
											getManager(mngId).sendToAll("NOW " + getManager(mngId).nowPlayer);
											getManager(mngId).sendToAll(
													"PRINT player" + getManager(mngId).nowPlayer + " �����Դϴ�.");
										}

									}

									getManager(mngId).sendToCardOther(playerId);
									getManager(mngId).sendToAll("REPAINT /" + playerId + "/" + list.size());

									if (getManager(mngId).dead[playerId] && table.playerCard[playerId] == null) // ���̺�
																												// �ѱ�
										// ī�尡 ���� �׾��� ��
										getManager(mngId).sendToAll("REPAINT /" + playerId + "//" + size());
								}

								else { // ��ġ�� ����
									while (table.size() > 0) // ���̺��� ��� ī�� �÷��̾��
										addPlayerCard(table.removeTableCard());

									getManager(mngId).sendToAll("PRINT player" + playerId + " ��ġ�� ����.");
									getManager(mngId).sendToAll("NOTI player" + playerId + " ��ġ�� ����.");

									getManager(mngId).bellRepaint(); // �÷��̾� ���� ����

									// ��ġ�� ������ �÷��̾���� �̾ ����
									getManager(mngId)
											.sendToAll("PRINT player" + getManager(mngId).nowPlayer + " ī�带 ���������ϴ�.");
									getManager(mngId).nowPlayer = playerId;
									getManager(mngId).sendToAll("NOW " + getManager(mngId).nowPlayer);
									getManager(mngId)
											.sendToAll("PRINT player" + getManager(mngId).nowPlayer + " �����Դϴ�.");

								}
								getManager(mngId).bellClick = false;
							}

						} else if (command.startsWith("CHAT")) { // �÷��̾ ä���� �Է��Ͽ��� ��
							getManager(mngId).sendToAll(command);
						} else if (command.startsWith("NOTI")) { // ä�� â�� ǥ�õǴ� ���� ���� ����
							getManager(mngId).sendToAll(command);
						} else if (command.startsWith("END")) { // ���� ����(�ʱ�ȭ Ÿ�̸� ����)
							endGame(mngId);

						} else if (command.startsWith("EXIT")) { // ����
							getManager(mngId).sendToAll("NOTI player" + playerId + " ����");
							getManager(mngId).remove(this);
							getManager(mngId).updatePlayer();
							if (getManager(mngId).enterNum() == 0) {
								emptyRoomI.add(getManager(mngId).managerId);
								Collections.sort(emptyRoomI);
								mng.remove(getManager(mngId));
							}
							mngId = -1;
							pNum = 1;
							waitingRoomMng.update();
						} else if (command.startsWith("LIST")) {
							waitingRoomMng.inviteUpdate(no);
						} else if (command.startsWith("INVITE")) {
							String[] s = command.split("\\|");
							System.out.println(Arrays.toString(s));
							int yourNo = Integer.parseInt(s[2].trim());

							if (waitingRoomMng.player[yourNo] != null)
								if (!waitingRoomMng.player[yourNo].name.equals(name))
									if (!waitingRoomMng.player[yourNo].ready)
										waitingRoomMng.sendTo(yourNo,
												"IM " + mngId + "����  �÷��̾� " + name + "�� �ʴ븦 ���մϴ�.");

							System.out
									.println(yourNo + "IM " + mngId + "����  no:" + no + " " + name + "�÷��̾ �ʴ븦 ���մϴ�.");
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
