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
import java.util.Timer;
import java.util.TimerTask;

public class HGServer {
	WManager waitingRoomMng;
	Manager[] mng = new Manager[3];
	int mngNum = 0;
	int n = 0;
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
			ss = new ServerSocket(8881);

			System.out.println("미니 할리갈리 서버가 시작되었습니다.");

			waitingRoomMng = new WManager(15);
			mng[0] = new Manager(4, new Table());
			mng[1] = new Manager(4, new Table());
			mng[2] = new Manager(4, new Table());

			while (true) {

				Player p = new Player(ss.accept());
				waitingRoomMng.add(p);

				p.start();

				System.out.println("플레이어 입장");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void endGame(int mngId) {
		n = 5;
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (n == 0) {
					timer.cancel();
					newGame(mngId);
				} else {
					mng[mngId].sendToAll("NOTI " + n + "초 후 시작");
					n--;
				}
			}
		};
		timer.schedule(task, 1000, 1000);
	}

	public void newGame(int mngId) {
		mng[mngId].initMng();
		Deck d = new Deck(); // 56장의 카드 덱 생성
		d.shuffle();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 14; j++) // 14장씩 딜
				mng[mngId].player[i].addPlayerCard(d.deal());

		mng[mngId].sendToAll("INIT");

	}

	public void initGame(int mngId) {
		for (int i = 0; i < 4; i++)
			mng[mngId].player[i].initPlayer();
		mng[mngId].readyCount = 0;

		mng[mngId].sendToAll("NOTI 레디하시오!");
	}

	// 해당 게임방의 매니저 클래스
	class Manager {
		Table table;
		int playerSize;
		Player[] player;
		Stack<Integer> addI = new Stack<>();
		int readyCount = 0; // 레디한 인원
		boolean[] dead = new boolean[4]; // 죽으면 true/ 살면 false
		int nowPlayer = 0; // 현재 플레이어
		boolean bellClick = false; // 벨 클릭 상태
		int pNum = 1; // 1은 waitingRoom 2는 gameRoom

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

		public int enterNum() {
			return playerSize - addI.size();
		}

		public void add(Player p) { // 1이면 no에 추가 2면 playerId에 추가
			int id = addI.pop();
			player[id] = p;
			p.playerId = id;
			p.table = table;
		}

		public void sendTo(int playerId, String msg) // 플레이어 playerId 에게 메시지를 전달.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 메시지 전달
		{
			int size = enterNum();
			for (int i = 0, j = 0; i < size; i++, j++) {
				if (player[j] == null)
					i--;
				else
					sendTo(j, msg);
			}
		}

		public void updatePlayer() {
			int size = enterNum();

			sendToAll("NEW " + size);
			for (int i = 0, j = 0; i < size; i++, j++) {
				if (player[j] == null)
					i--;
				else
					sendToAll(player[j].playerId + "/" + player[j].name);
			}

		}

		//// 게임 용 메소드
		public void sendToCard(int myId, int otherId) // 플레이어 otherId 에게 카드 전달.
		{
			player[otherId].addPlayerCard(player[myId].removePlayerCard());
			sendToAll("REPAINT /" + otherId + "/" + player[otherId].size());
		}

		public void sendToCardOther(int myId) // 종치기 실패(다른 플레이어에게 카드 전달)
		{
			for (int i = 0; i < player.length; i++) {
				if (i != myId && !dead[i]) // 내가 아닌 산 사람에게만
					sendToCard(myId, i);
				if (player[myId].size() == 0) // 내 남은 카드 개수까지만
					break;
			}
		}

		public void bellRepaint() { // 종치기 성공하여 카드 리셋
			for (int i = 0; i < player.length; i++) {
				sendToAll("REPAINT /" + i + "//" + player[i].size());
			}
		}

		public void nextPlayer() { // 다음 플레이어로 지정
			do {
				nowPlayer = (nowPlayer + 1) % 4;
			} while (dead[nowPlayer]);
		}

		public void die(int playerId) { // 죽은사람으로
			dead[playerId] = true;
		}

		public int aliveCount() { // 산사람 숫자
			int sum = 0;
			for (int i = 0; i < dead.length; i++)
				if (!dead[i])
					sum++;
			return sum;
		}

		public int winner() { // aliveCount()가 1일 때 승자에게 사용(산사람 id) 리턴
			for (int i = 0; i < dead.length; i++)
				if (!dead[i])
					return i;

			return -1;
		}

	}

	// 플레이어의 정보가 담긴 클래스
	class Player extends Thread {
		Table table;
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		int playerId; // 해당 플레이어 번호
		int mngId = -1; // 해당 게임방 매니저 객체 번호
		boolean ready;
		int pNum = 1;

		int no;
		String name;

		private LinkedList<Card> list = new LinkedList<Card>(); // 카드 리스트

		public Player(Socket socket) {
			this.socket = socket;

			try {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("연결이 끊어졌습니다 . " + e);
			}
		}

		public void initPlayer() {
			list = new LinkedList<Card>();
			ready = false;
		}

		@Override
		public String toString() {
			return "{ player" + playerId + "입니다} \n";
		}

		// 플레이어 카드 추가(Bell성공/다른 플레이어의 Bell실패)
		public void addPlayerCard(Card c) {
			list.add(c);
		}

		// 플레이어 카드 제거(Turn/Bell실패)
		public Card removePlayerCard() {
			return list.remove();
		}

		public void showPlayerCards() { // 전체 카드 리스트 출력
			System.out.println(list);
		}

		public int size() { // 남은 카드 개수
			return list.size();
		}

		/*
		 * NOW : 현재플레이어 / PRINT : 정보알림 레이블에 표시/ NOTI : 게임 진행사항으로 채팅창에 표시 / TURN : 플레이어가
		 * TURN버튼을 클릭 / REPAINT : 클라이어의 카드나 남은 카드 개수 재표시 / DIE : 플레이어 죽음 / WIN : 플레이어 이김
		 * / BELL : 플레이어가 BELL버튼을 클릭 / CHAT : 채팅한 내용으로 채팅창에 표시 /
		 */

		public void run() {
			try {
				String command = input.readLine();
				if (command.startsWith("CONNECT")) {
					name = command.substring(8);
					output.println("NO " + no);
					waitingRoomMng.updatePlayer(no);
					waitingRoomMng.updateRoom(no);
				}
				while ((command = input.readLine()) != null) {
					if (pNum == 1) {
						if (command.startsWith("WCHAT")) { // 플레이어가 채팅을 입력하였을 때
							waitingRoomMng.sendToAll(command);
						} else if (command.startsWith("ENTER")) {
							for (int i = 0; i < 3; i++) {
								if (!(mng[i].enterNum() == 4)) {
									System.out.println(i);
									mng[i].add(this);
									mngId = i;
									output.println("ENTER 성공");
									pNum = 2;
									waitingRoomMng.sendToAll("ENTER /" + i + "/" + mng[i].enterNum());

									output.println("START " + mngId + " " + playerId);
									mng[mngId].sendToAll("NOTI player" + playerId + " 입장");
									output.println("PRINT 다른 경기자를 기다립니다.");
									mng[mngId].updatePlayer();
									break;
								}
							}
							if (mngId == -1)
								output.println("NOTI 방이 가득 찼습니다.");
							continue;
						}

					} else { // pNum==2

						if (command.startsWith("READY")) {
							if (!ready) {
								ready = true;
								++(mng[mngId].readyCount);
								mng[mngId].sendToAll("NOTI player" + playerId + " 준비 완료!");
								mng[mngId].sendToAll("NOTI 현재 준비 완료 : " + mng[mngId].readyCount);

								if (mng[mngId].readyCount == 4) {
									newGame(mngId);
									mng[mngId].sendToAll("PRINT 게임을 시작합니다.");
									mng[mngId].sendToAll("NOTI 게임을 시작합니다.");
									mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
									mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 차례입니다.");
								}
							} else {
								ready = false;
								--(mng[mngId].readyCount);
								mng[mngId].sendToAll("NOTI player" + playerId + " 준비 해제!");
								mng[mngId].sendToAll("NOTI 현재 준비 완료 : " + mng[mngId].readyCount);
							}

						} else if (command.startsWith("TURN")) {
							if (!mng[mngId].dead[playerId] && size() > 0) {
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 카드를 뒤집었습니다.");
								mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 카드를 뒤집었습니다.");

								Card removeCard = removePlayerCard();
								table.addTableCard(removeCard, playerId);
								mng[mngId].sendToAll(
										"REPAINT /" + mng[mngId].nowPlayer + "/" + removeCard + "/" + list.size());

								if (list.size() == 0) { // 남은 카드 개수 0개
									mng[mngId].die(mng[mngId].nowPlayer);
									mng[mngId].sendToAll("DIE " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 게임오버.");

									if (mng[mngId].aliveCount() == 1) { // 남은 사람 1명
										mng[mngId].sendToAll("WIN " + mng[mngId].winner());
										mng[mngId]
												.sendToAll("NOTI -----[[승리]] player" + mng[mngId].winner() + " -----");
										continue;
									}
								}
								mng[mngId].nextPlayer();

								mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
								mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 차례입니다.");
							}
						} else if (command.startsWith("BELL")) {

							if (mng[mngId].bellClick) { // 이미 누가 벨을 클릭했을 때 (종치기 성공/종치기 실패)
								output.println("늦었습니다.");

							} else {
								mng[mngId].bellClick = true;
								mng[mngId].sendToAll("PRINT player" + playerId + "이 종침.");
								mng[mngId].sendToAll("NOTI player" + playerId + "이 종침.");

								if (!table.sumFive(/* mng[mngId] */)) { // 종치기 실패(과일이 다섯개가 아니면)

									mng[mngId].sendToAll("PRINT player" + playerId + " 종치기 실패.");
									mng[mngId].sendToAll("NOTI player" + playerId + " 종치기 실패.");

									if (list.size() < mng[mngId].aliveCount()) { // 나눠줄 카드가 부족할 때
										mng[mngId].die(playerId);
										mng[mngId].sendToAll("DIE " + playerId);
										mng[mngId].sendToAll("NOTI player" + playerId + " 게임오버.");

										if (mng[mngId].aliveCount() == 1) {
											mng[mngId].sendToAll("WIN " + mng[mngId].winner());
											mng[mngId].sendToAll(
													"NOTI -----[[승리]] player" + mng[mngId].winner() + " -----");
										}

										else if (playerId == mng[mngId].nowPlayer) { // 내 차례인데 종치기 실패하여 남은 카드가 없을 때
											mng[mngId].nextPlayer();
											mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
											mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
											mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 차례입니다.");
										}

									}

									mng[mngId].sendToCardOther(playerId);
									mng[mngId].sendToAll("REPAINT /" + playerId + "/" + list.size());

									if (mng[mngId].dead[playerId] && table.playerCard[playerId] == null) // 테이블에 넘긴
																											// 카드가
																											// 없고
																											// 죽었을
																											// 때
										mng[mngId].sendToAll("REPAINT /" + playerId + "//" + size());
								}

								else { // 종치기 성공
									while (table.size() > 0) // 테이블의 모든 카드 플레이어에게
										addPlayerCard(table.removeTableCard());

									mng[mngId].sendToAll("PRINT player" + playerId + " 종치기 성공.");
									mng[mngId].sendToAll("NOTI player" + playerId + " 종치기 성공.");

									mng[mngId].bellRepaint(); // 플레이어 상태 갱신

									// 종치기 성공한 플레이어부터 이어서 시작
									mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 카드를 뒤집었습니다.");
									mng[mngId].nowPlayer = playerId;
									mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
									mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 차례입니다.");

								}
								mng[mngId].bellClick = false;
							}

						} else if (command.startsWith("CHAT")) { // 플레이어가 채팅을 입력하였을 때
							mng[mngId].sendToAll(command);
						} else if (command.startsWith("NOTI")) {
							mng[mngId].sendToAll(command);
						} else if (command.startsWith("END")) {
							endGame(mngId);

						} else if (command.startsWith("EXIT")) {

						}
					}

				}

			} catch (

			IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class WManager {
		int playerSize;
		Player[] player;
		Stack<Integer> addI = new Stack<>();
		int readyCount = 0; // 레디한 인원
		boolean[] dead = new boolean[4]; // 죽으면 true/ 살면 false
		int nowPlayer = 0; // 현재 플레이어
		boolean bellClick = false; // 벨 클릭 상태
		int pNum = 1; // 1은 waitingRoom 2는 gameRoom

		public WManager(int playerSize) {
			this.playerSize = playerSize;
			player = new Player[playerSize];
			for (int i = playerSize - 1; i >= 0; i--)
				addI.add(i);
		}

		public void initMng() {
			dead = new boolean[4];
			nowPlayer = 0;
			bellClick = false;
		}

		public int enterNum() {
			return playerSize - addI.size();
		}

		public void add(Player p) {
			int id = addI.pop();
			player[id] = p;
			p.no = id;
		}

		public void sendTo(int playerId, String msg) // 플레이어 playerId 에게 메시지를 전달.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 메시지 전달
		{
			int size = enterNum();
			for (int i = 0, j = 0; i < size; i++, j++) {
				if (player[j] == null)
					i--;
				else
					sendTo(j, msg);
			}
		}

		public void updatePlayer(int no) {
			int size = enterNum();

			sendToAll("WNEW /" + no + "/" + size);
			for (int i = 0, j = 0; i < size; i++, j++) {
				if (player[j] == null)
					i--;
				else
					sendToAll("      " + player[j].no + "    |    " + player[j].name);
			}

		}

		public void updateRoom(int no) {
			sendTo(no, mng[0].enterNum() + "/" + mng[1].enterNum() + "/" + mng[2].enterNum());
		}
	}
}

//테이블의 정보를 담은 클래스
class Table {
	private LinkedList<Card> list = new LinkedList<Card>(); // 플레이어가 넘긴 카드 리스트
	Card[] playerCard = new Card[4]; // 현재 보이는 카드

	// 테이블에 카드 추가(플레이어가 TURN)
	public void addTableCard(Card c, int playerId) {
		list.addFirst(c);
		playerCard[playerId] = c;
	}

	// 테이블에 카드 제거(플레이어 BELL)
	public Card removeTableCard() {
		for (int i = 0; i < 4; i++) { // 보이는 카드 초기화
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

	public boolean sumFive(/* Manager mng */) { // 과일이 다섯개인지 확인해 주는 메소드
		int[] sum = new int[4]; // 각 과일의 총 합

		for (int i = 0; i < 4; i++)
			if (playerCard[i] != null) // 뒤집은 카드만
				sum[playerCard[i].getFruit()] += playerCard[i].getNumber();

//		mng.sendToAll("NOTI " + Arrays.toString(sum));
		for (int i = 0; i < 4; i++) {
			if (sum[i] == 5)
				return true;
		}

		return false;
	}
}

class Card { // 카드 한 장을 표현하는 클래스 Card
	private int fruit; // 과일 모양(0~3)
	private int number; // 카드 번호(0~4)

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

class Deck { // 카드 56장을 다루는 덱을 표현하는 클래스 Deck
	private LinkedList<Card> deck = new LinkedList<Card>();
	private int[] count = { 5, 3, 3, 2, 1 };

	// 카드를 생성하여 덱에 넣음
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

	// 카드를 섞음
	public void shuffle() {
		Collections.shuffle(deck);
	}

	// 덱의 맨 앞 카드를 제거하여 리턴
	public Card deal() {
		return deck.remove(0);
	}
}
