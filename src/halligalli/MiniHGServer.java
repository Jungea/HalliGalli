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
	boolean[] dead = new boolean[4]; // 죽으면 true/ 살면 false
	int nowPlayer = 0;
	ArrayList<Manager> mngList = new ArrayList<Manager>();
	int mngNum = 0;
	Manager mng = new Manager();
	boolean bellClick = false;

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

	public static void main(String[] args) {

		MiniHGServer server = new MiniHGServer();
		server.startServer();
	}

	public void startServer() {
		// TODO Auto-generated method stub
		ServerSocket ss;
		try {
			ss = new ServerSocket(8886);

			System.out.println("미니 할리갈리 서버가 시작되었습니다.");

			while (true) {

				Table table = new Table();
				Deck d = new Deck(); // 56장의 카드 덱 생성
				d.shuffle();

				Player[] player = new Player[4];
				for (int i = 0; i < 4; i++) {
					player[i] = new Player(table, ss.accept(), i, mngNum);
					mng.add(player[i]);
					for (int j = 0; j < 14; j++) // 14장씩 딜
						player[i].addPlayerCard(d.deal());
				}
				mngNum++;
				mngList.add(mng);

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

	// 플레이어간의 행동을 해주는 클래스
	class Manager {

		Player[] player = new Player[4];
		int addI = 0;

		public void add(Player p) {
			player[addI++] = p;
		}

		public void sendTo(int playerId, String msg) // 플레이어 playerId 에게 메시지를 전달.
		{
			player[playerId].output.println(msg);
		}

		public void sendToAll(String msg) // 모든 플레이어에게 메시지 전달
		{
			for (int i = 0; i < player.length; i++) {
				sendTo(i, msg);
			}
		}

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
	}

	// 플레이어의 정보가 담긴 클래스
	class Player extends Thread {
		Table table;
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		int playerId; // 해당 플레이어 번호
		int mngId; // 해당 게임방 매니저 객체 번호

		private LinkedList<Card> list = new LinkedList<Card>(); // 카드 리스트

		public Player(Table table, Socket socket, int playerId, int mngNum) {
			this.table = table;
			this.socket = socket;
			this.playerId = playerId;
			this.mngId = mngNum;

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

		public Manager getMng() {
			return mngList.get(mngId);
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
			output.println("PRINT 모든 경기자가 연결되었습니다.");
			try {
				String command;

				getMng().sendToAll("NOW " + nowPlayer);
				getMng().sendToAll("PRINT player" + nowPlayer + " 차례입니다.");
				if (nowPlayer == playerId)
					getMng().sendToAll("NOTI player" + nowPlayer + " 차례입니다.");

				while ((command = input.readLine()) != null) {

					if (command.startsWith("TURN")) {
						getMng().sendToAll("PRINT player" + nowPlayer + " 카드를 뒤집었습니다.");
						getMng().sendToAll("NOTI player" + nowPlayer + " 카드를 뒤집었습니다.");

						Card removeCard = removePlayerCard();
						table.addTableCard(removeCard, playerId);
						getMng().sendToAll("REPAINT /" + nowPlayer + "/" + removeCard + "/" + list.size());

						if (list.size() == 0) { // 남은 카드 개수 0개
							die(nowPlayer);
							getMng().sendToAll("DIE " + nowPlayer);
							getMng().sendToAll("NOTI player" + nowPlayer + " 게임오버.");

							if (aliveCount() == 1) { // 남은 사람 1명
								getMng().sendToAll("WIN " + winner());
								getMng().sendToAll("NOTI -----[[승리]] player" + winner() + " -----");
							}
						}
						nextPlayer();

						getMng().sendToAll("NOW " + nowPlayer);
						getMng().sendToAll("PRINT player" + nowPlayer + " 차례입니다.");
						getMng().sendToAll("NOTI player" + nowPlayer + " 차례입니다.");

					} else if (command.startsWith("BELL")) {

						if (bellClick) { // 이미 누가 벨을 클릭했을 때 (종치기 성공/종치기 실패)
							output.println("늦었습니다.");

						} else {
							bellClick = true;
							getMng().sendToAll("PRINT player" + playerId + "이 종침.");
							getMng().sendToAll("NOTI player" + playerId + "이 종침.");

							if (!table.sumFive(/* getmng() */)) { // 종치기 실패(과일이 다섯개가 아니면)

								getMng().sendToAll("PRINT player" + playerId + " 종치기 실패.");
								getMng().sendToAll("NOTI player" + playerId + " 종치기 실패.");

								if (list.size() < aliveCount()) { // 나눠줄 카드가 부족할 때
									die(playerId);
									getMng().sendToAll("DIE " + playerId);
									getMng().sendToAll("NOTI player" + playerId + " 게임오버.");

									if (aliveCount() == 1) {
										getMng().sendToAll("WIN " + winner());
										getMng().sendToAll("NOTI -----[[승리]] player" + winner() + " -----");
									}

									else if (playerId == nowPlayer) { // 내 차례인데 종치기 실패하여 남은 카드가 없을 때
										nextPlayer();
										getMng().sendToAll("NOW " + nowPlayer);
										getMng().sendToAll("PRINT player" + nowPlayer + " 차례입니다.");
										getMng().sendToAll("NOTI player" + nowPlayer + " 차례입니다.");
									}

								}

								getMng().sendToCardOther(playerId);
								getMng().sendToAll("REPAINT /" + playerId + "/" + list.size());

								if (dead[playerId] && table.playerCard[playerId] == null) // 테이블에 넘긴 카드가 없고 죽었을 때
									getMng().sendToAll("REPAINT /" + playerId + "//" + size());
							}

							else { // 종치기 성공
								while (table.size() > 0) // 테이블의 모든 카드 플레이어에게
									addPlayerCard(table.removeTableCard());

								getMng().sendToAll("PRINT player" + playerId + " 종치기 성공.");
								getMng().sendToAll("NOTI player" + playerId + " 종치기 성공.");

								getMng().bellRepaint(); // 플레이어 상태 갱신

								// 종치기 성공한 플레이어부터 이어서 시작
								getMng().sendToAll("PRINT player" + nowPlayer + " 카드를 뒤집었습니다.");
								nowPlayer = playerId;
								getMng().sendToAll("NOW " + nowPlayer);
								getMng().sendToAll("PRINT player" + nowPlayer + " 차례입니다.");
								getMng().sendToAll("NOTI player" + nowPlayer + " 차례입니다.");

							}
							bellClick = false;
						}

					} else if (command.startsWith("CHAT")) { // 플레이어가 채팅을 입력하였을 때
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