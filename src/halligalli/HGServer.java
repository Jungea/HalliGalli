package halligalli;

/*
 * 작성자: 정은애
 * 할리갈리 게임 서버
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
	int n = 0; // timer에서 사용하는 변수
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

			System.out.println("할리갈리 서버가 시작되었습니다.");

			waitingRoomMng = new WManager(16);
			mng[0] = new Manager(4, new Table());
			mng[1] = new Manager(4, new Table());
			mng[2] = new Manager(4, new Table());

			while (true) {

				Player p = new Player(ss.accept());

				if (waitingRoomMng.addI.isEmpty()) { // 대기실 인원초과
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
						System.out.println("서버에 인원이 가득찼습니다.");

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					waitingRoomMng.add(p);

					p.start();
					System.out.println("서버연결 성공  \n( 접속자 수 = " + waitingRoomMng.enterNum() + "명 )");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void endGame(int mngId) { // 게임 종료 후 초기화 타이머
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
					mng[mngId].sendToAll("NOTI " + n + "초 후 초기화");
					n--;
				}
			}
		};
		timer.schedule(task, 1000, 1000);
	}

	public void initGame(int mngId) { // 그래픽 초기화
		for (int i = 0; i < 4; i++)
			mng[mngId].player[i].initPlayer();
		mng[mngId].readyCount = 0;

		mng[mngId].sendToAll("INIT");
		mng[mngId].sendToAll("NOTI 레디하시오!");
	}

	public void newGame(int mngId) { // 게임 변수 초기화
		mng[mngId].initMng();
		Deck d = new Deck(); // 56장의 카드 덱 생성
		d.shuffle();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 14; j++) // 14장씩 딜
				mng[mngId].player[i].addPlayerCard(d.deal());

	}

	/* sendTo, sendToAll 참고 : http://mudchobo.tistory.com/2 */

	class WManager { // 대기실 매니저
		int playerSize;
		Player[] player;
		List<Integer> addI = new LinkedList<>(); // no할당

		public WManager(int playerSize) {
			this.playerSize = playerSize;
			player = new Player[playerSize];
			for (int i = 0; i < playerSize; i++)
				addI.add(i);
		}

		public int enterNum() { // 대기실 입장 인원
			return playerSize - addI.size();
		}

		public void add(Player p) { // 대기실 입장(connect)
			int id = addI.remove(0);
			player[id] = p;
			p.no = id;
		}

		public void remove(Player p) { // 플레이어 게임 종료 (해당 NO 반납)
			player[p.no] = null;
			addI.add(p.no);
			Collections.sort(addI);
		}

		public void sendTo(int playerNo, String msg) // 플레이어 playerNo 에게 메시지를 전달.
		{
			player[playerNo].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 메시지 전달
		{
			int size = enterNum();
			for (int i = 0, j = 0; j < size; i++)
				if (player[i] != null) {
					sendTo(i, msg);
					j++;
				}

		}

		public void update() { // 대기실 새로운 사람 입장(connect)
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

	// 게임방의 매니저 클래스
	class Manager {
		Table table;
		int playerSize;
		Player[] player;
		List<Integer> addI = new LinkedList<>(); // id
		int readyCount = 0; // 레디한 인원 수
		boolean[] dead = new boolean[4]; // 죽으면 true, 살면 false
		int nowPlayer = 0; // 현재 플레이어
		boolean bellClick = false; // 벨 클릭 상태
		int pNum = 1; // 1은 waitingRoom 2는 gameRoom

		public Manager(int playerSize, Table table) {
			this.playerSize = playerSize;
			player = new Player[playerSize];
			for (int i = 0; i < playerSize; i++)
				addI.add(i);

			this.table = table;
		}

		public void initMng() { // 게임 변수 초기화
			table = new Table();
			dead = new boolean[4];
			nowPlayer = 0;
			bellClick = false;
		}

		public int enterNum() { // 게임방 입장 인원
			return playerSize - addI.size();
		}

		public void add(Player p) { // 게임방 입장
			int id = addI.remove(0);
			player[id] = p;
			p.playerId = id;
			p.table = table;
		}

		public void remove(Player p) { // 게임방 나가기 (ID 반납)
			addI.add(p.playerId);
			Collections.sort(addI);
			player[p.playerId] = null;
		}

		public void sendTo(int playerID, String msg) // 플레이어 playerID 에게 메시지를 전달.
		{
			player[playerID].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 메시지 전달
		{
			int size = enterNum();
			for (int i = 0, j = 0; j < size; i++) {
				if (player[i] != null) {
					sendTo(i, msg);
					j++;
				}
			}
		}

		public void updatePlayer() { // 게임방에 플레이어 업데이트

			sendToAll("NEW ");
			for (int i = 0; i < playerSize; i++) {
				if (player[i] == null)
					sendToAll("null");
				else
					sendToAll(player[i].playerId + "/" + player[i].name);

			}

		}

		// 게임 용 메소드
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

		public void die(int playerId) { // 죽은사람으로 설정
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

		private List<Card> list = new LinkedList<>(); // 카드 리스트

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

		public void initPlayer() { // 플레이어 게임변수 초기화
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
			return list.remove(0);
		}

		public void showPlayerCards() { // 전체 카드 리스트 출력
			System.out.println(list);
		}

		public int size() { // 남은 카드 개수
			return list.size();
		}

		public void run() {
			try {
				String command = input.readLine();
				if (command.startsWith("CONNECT")) { // 연결
					name = command.substring(8);
					output.println("NO " + no);
					waitingRoomMng.update();
				}
				while ((command = input.readLine()) != null) {
					if (pNum == 1) { // 대기실
						if (command.startsWith("WCHAT")) { // 플레이어가 채팅을 입력
							waitingRoomMng.sendToAll(command);

						} else if (command.startsWith("ENTER")) { // 입장버튼 클릭
							int i = -1;

							if (command.length() > 5) // 방버튼을 클릭
								i = command.charAt(6) - 48;
							else { // 입장 클릭
								for (i = 0; i < 3; i++)
									if (!(mng[i].enterNum() == 4))
										break;
							}

							mngId = i;

							if (mngId == -1)
								output.println("NOTI 방이 가득 찼습니다.");
							else {
								System.out.println(i + "번방 입장");
								mng[i].add(this);
								mngId = i;
								output.println("ENTER 성공");
								pNum = 2;
								waitingRoomMng.sendToAll("ENTER /" + i + "/" + mng[i].enterNum());

								output.println("START " + mngId + " " + playerId);
								mng[mngId].sendToAll("NOTI " + name + " 입장");
								output.println("NOTI 현재 준비 완료 : " + mng[mngId].readyCount);
								output.println("PRINT 다른 경기자를 기다립니다.");
								mng[mngId].updatePlayer();
							}

							continue;
						}

					} else { // pNum==2 게임방

						if (command.startsWith("READY")) {
							if (!ready) {
								ready = true;
								++(mng[mngId].readyCount);
								mng[mngId].sendToAll("NOTI player" + playerId + " 준비 완료!");
								mng[mngId].sendToAll("NOTI 현재 준비 완료 : " + mng[mngId].readyCount);

								if (mng[mngId].readyCount == 4) { // 4명이 되면 시작
									newGame(mngId);
									mng[mngId].sendToAll("PRINT 게임을 시작합니다.");
									mng[mngId].sendToAll("NOTI 게임을 시작합니다.");
									mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
								}
							} else { // 이미 레디(레디해제)
								ready = false;
								--(mng[mngId].readyCount);
								mng[mngId].sendToAll("NOTI player" + playerId + " 준비 해제!");
								mng[mngId].sendToAll("NOTI 현재 준비 완료 : " + mng[mngId].readyCount);
							}

						} else if (command.startsWith("TURN")) { // 카드 뒤집음.
							if (!mng[mngId].dead[playerId] && size() > 0) {
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 카드를 뒤집었습니다.");

								Card removeCard = removePlayerCard();
								table.addTableCard(removeCard, playerId);
								mng[mngId].sendToAll(
										"REPAINT /" + mng[mngId].nowPlayer + "/" + removeCard + "/" + list.size());

								if (list.size() == 0) { // 남은 카드 개수 0개 죽음.
									mng[mngId].die(mng[mngId].nowPlayer);
									mng[mngId].sendToAll("DIE " + mng[mngId].nowPlayer);
									mng[mngId].sendToAll("NOTI player" + mng[mngId].nowPlayer + " 게임오버.");

									if (mng[mngId].aliveCount() == 1) { // 남은 사람 1명
										mng[mngId].sendToAll("WIN " + mng[mngId].winner());
										mng[mngId].sendToAll(
												"NOTI --------- [[승리]] player" + mng[mngId].winner() + " ---------");
										continue;
									}
								}
								mng[mngId].nextPlayer();

								mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
								mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
							}
						} else if (command.startsWith("BELL")) {

							if (mng[mngId].bellClick) { // 이미 누가 벨을 클릭했을 때 (종치기 성공/종치기 실패)
								output.println("늦었습니다.");

							} else {
								mng[mngId].bellClick = true;
								mng[mngId].sendToAll("PRINT player" + playerId + "이 종침.");

								if (!table.sumFive()) { // 종치기 실패(과일이 다섯개가 아니면)

									mng[mngId].sendToAll("PRINT player" + playerId + " 종치기 실패.");
									mng[mngId].sendToAll("NOTI player" + playerId + " 종치기 실패.");

									if (list.size() < mng[mngId].aliveCount()) { // 나눠줄 카드가 부족할 때
										mng[mngId].die(playerId);
										mng[mngId].sendToAll("DIE " + playerId);
										mng[mngId].sendToAll("NOTI player" + playerId + " 게임오버.");

										if (mng[mngId].aliveCount() == 1) {
											mng[mngId].sendToAll("WIN " + mng[mngId].winner());
											mng[mngId].sendToAll("NOTI --------- [[승리]] player" + mng[mngId].winner()
													+ " ---------");
										}

										else if (playerId == mng[mngId].nowPlayer) { // 내 차례인데 종치기 실패하여 남은 카드가 없을 때
											mng[mngId].nextPlayer();
											mng[mngId].sendToAll("NOW " + mng[mngId].nowPlayer);
											mng[mngId].sendToAll("PRINT player" + mng[mngId].nowPlayer + " 차례입니다.");
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

								}
								mng[mngId].bellClick = false;
							}

						} else if (command.startsWith("CHAT")) { // 플레이어가 채팅을 입력하였을 때
							mng[mngId].sendToAll(command);
						} else if (command.startsWith("NOTI")) { // 채팅 창에 표시되는 게임 진행 정보
							mng[mngId].sendToAll(command);
						} else if (command.startsWith("END")) { // 게임 종료(초기화 타이머 시작)
							endGame(mngId);

						} else if (command.startsWith("EXIT")) { // 퇴장
							mng[mngId].sendToAll("NOTI player" + playerId + " 퇴장");
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
					System.out.println("할리갈리 서버 접속을 종료합니다. \n( 접속자 수 = " + waitingRoomMng.enterNum() + "명 )");
					waitingRoomMng.update();
				} catch (Exception e) {
				}
			}
		}
	}

}

//테이블의 정보를 담은 클래스
class Table {
	private List<Card> list = new LinkedList<>(); // 플레이어가 넘긴 카드 리스트
	Card[] playerCard = new Card[4]; // 현재 보이는 카드

	// 테이블에 카드 추가(플레이어가 TURN)
	public void addTableCard(Card c, int playerId) {
		list.add(0, c);
		playerCard[playerId] = c;
	}

	// 테이블에 카드 제거(플레이어 BELL)
	public Card removeTableCard() {
		for (int i = 0; i < 4; i++) { // 보이는 카드 초기화
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
