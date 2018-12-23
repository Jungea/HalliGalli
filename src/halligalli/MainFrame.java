package halligalli;

/*
 * 작성자: 정은애
 * 할리갈리 게임 Frame
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

import javax.swing.JFrame;

public class MainFrame extends JFrame implements Runnable {
	// TODO Auto-generated catch block
	int ip = 8885;
	int no;
	String name;
	Socket socket;
	BufferedReader input;
	PrintWriter output;

	public WaitingRoom wR = null;
	public GameRoom gR = null;

	int pNum = 1;

	public void changeRoom(String panelName) {
		setResizable(false);
		if (panelName.equals("gR")) {
			getContentPane().removeAll();
			getContentPane().add(gR);
			setSize(920, 720);
			revalidate();
			repaint();
		} else {
			getContentPane().removeAll();
			getContentPane().add(wR);
			setSize(700, 500);
			revalidate();
			repaint();
		}
	}

	public MainFrame() throws IOException {

		setSize(700, 500);

		wR = new WaitingRoom(this);
		gR = new GameRoom(this);

		add(wR);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void run() {
		// TODO Auto-generated catch block
		String response;

		try {
			response = input.readLine();

			if (response.startsWith("NO")) {
				no = Integer.parseInt(response.substring(3));
				setTitle("no: " + no + "/ name: " + name);
			}

			while ((response = input.readLine()) != null) {
				if (pNum == 1) {
					if (response.startsWith("WNEW")) {
						wR.userArea.setText("");
						String[] r = response.split("/");
						int size = Integer.parseInt(r[1]);
						for (int i = 0; i < size; i++)
							wR.userArea.append(input.readLine() + "\n");
						response = input.readLine();
						r = response.split("/");
						for (int i = 0; i < 3; i++) {
							wR.room[i].setText("방제목: 방" + i + "        인원: " + r[i] + "/4");
							if (Integer.parseInt(r[i]) == 4)
								wR.room[i].setEnabled(false);
							else
								wR.room[i].setEnabled(true);

						}

					} else if (response.startsWith("WCHAT")) {
						String[] s = response.split("/");

						if (!Objects.equals(name, s[1]))
							wR.waitChatArea.append("  " + s[1] + " : " + s[2] + "\n");
						wR.waitChatArea.setCaretPosition(wR.waitChatArea.getDocument().getLength());
					} else if (response.startsWith("NOTI")) {
						wR.waitChatArea.append(response.substring(5) + "\n");
						wR.waitChatArea.setCaretPosition(wR.waitChatArea.getDocument().getLength());
					} else if (response.startsWith("ENTER")) {
						if (response.endsWith("성공")) {
							gR.chatArea.setText("");
							changeRoom("gR");
							pNum = 2;
							setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
							continue;
						}
						String[] r = response.split("/");
						int roomId = Integer.parseInt(r[1]);
						wR.room[roomId].setText("방제목: 방" + roomId + "        인원: " + r[2] + "/4");
						if (Integer.parseInt(r[2]) == 4)
							wR.room[roomId].setEnabled(false);
						else
							wR.room[roomId].setEnabled(true);

					}
				}

				else {
					if (response.startsWith("START")) {
						gR.roomNum = response.charAt(6) - 48;
						gR.playerId = response.charAt(8) - 48;
						gR.info.setText("경기 준비 중입니다.");
						setTitle(gR.roomNum + "번방 p" + gR.playerId + " " + name);
					} else if (response.startsWith("NEW")) {
						gR.userArea.setText("");
						String[] r;
						for (int i = 0; i < 4; i++) {
							response = input.readLine();
							if (response.equals("null")) {
								gR.pName[i].setText("player" + i);
								continue;
							} else {
								r = response.split("/");
								gR.Name[i] = r[1];
								gR.userArea.append("           " + r[0] + "      |      " + r[1] + "\n");
								gR.pName[i].setText("( " + i + " ) " + r[1]);
							}
						}
					} else if (response.startsWith("NOW")) {
						if (response.charAt(4) - 48 == gR.playerId) {
							gR.turnButton.setEnabled(true);
							gR.newTurnTimer();
						}
						gR.cardPanel[response.charAt(4) - 48].setBorder(gR.lb);
					} else if (response.startsWith("PRINT")) {
						gR.info.setText(response.substring(6));
						if (response.endsWith("게임을 시작합니다.")) {
							gR.bellButton.setEnabled(true);
							gR.readyButton.setEnabled(false);
						}
						if (response.endsWith("카드를 뒤집었습니다.")) {
							gR.cardPanel[response.charAt(12) - 48].setBorder(gR.eb);
							if (gR.playerId == (response.charAt(12) - 48)) {
								gR.turnButton.setEnabled(false);
								gR.timer.cancel();
							}
						}

					} else if (response.startsWith("REPAINT")) {
						String[] s = response.split("/");
						if (s.length == 4) {
							if (s[2].length() > 0)
								gR.pCard[Integer.parseInt(s[1])]
										.setIcon(gR.cardImg[s[2].charAt(0) - 48][s[2].charAt(2) - 49]);
							else {
								if (!s[3].equals("0"))
									gR.pCard[Integer.parseInt(s[1])].setIcon(gR.cardBackImg);
								else
									gR.pCard[Integer.parseInt(s[1])].setIcon(gR.emptyImg);
							}
							gR.pCardNum[Integer.parseInt(s[1])].setText("   " + s[3] + "장");
						} else {
							gR.pCardNum[Integer.parseInt(s[1])].setText("   " + s[2] + "장");
						}
					} else if (response.startsWith("CHAT")) {
						String[] s = response.split("/");
						if (!Objects.equals(name, s[1]))
							gR.chatArea.append("  " + s[1] + " : " + s[2] + "\n");
						gR.chatArea.setCaretPosition(gR.chatArea.getDocument().getLength());
					} else if (response.startsWith("NOTI")) {

						gR.chatArea.append(response.substring(5) + "\n");
						gR.chatArea.setCaretPosition(gR.chatArea.getDocument().getLength());

						if (response.endsWith("레디하시오!")) {
							gR.readyButton.setEnabled(true);
							gR.exitButton.setEnabled(true);
						} else if (response.length() > 11 && response.charAt(11) - 48 == gR.playerId) {
							if (response.endsWith("준비 완료!"))
								gR.exitButton.setEnabled(false);
							else if (response.endsWith("준비 해제!"))
								gR.exitButton.setEnabled(true);
						}
						if (response.endsWith("퇴장")) {
							int i = response.charAt(11) - 48;
							gR.pName[i].setText("player" + i);
							if (i == gR.playerId) {
								pNum = 1;
								setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							}

						}
					} else if (response.startsWith("DIE")) {
						if (response.charAt(4) - 48 == gR.playerId) {
							gR.info.setText("게임오버");
							gR.turnButton.setEnabled(false);
							gR.bellButton.setEnabled(false);
						}
						gR.cardPanel[response.charAt(4) - 48].setBorder(gR.eb);
					} else if (response.startsWith("WIN")) {
						if (response.charAt(4) - 48 == gR.playerId) {
							gR.timer.cancel();
							gR.info.setText("WIN!!");
							gR.turnButton.setEnabled(false);
							gR.bellButton.setEnabled(false);
							output.println("END");
						} else
							gR.info.setText("player" + (response.charAt(4) - 48) + " 승리");
						gR.cardPanel[response.charAt(4) - 48].setBorder(gR.eb);
						for (int i = 0; i < 4; i++)
							if (i == (response.charAt(4) - 48))
								gR.pCard[response.charAt(4) - 48].setIcon(gR.cardBackImg);
							else
								gR.pCard[i].setIcon(gR.emptyImg);
					} else if (response.startsWith("INIT")) {
						for (int i = 0; i < 4; i++) {
							gR.pCardNum[i].setText("   14장");
							gR.pCard[i].setIcon(gR.cardBackImg);
						}

					}
				}

			}
		} catch (

		IOException e) {
			// TODO Auto-generated catch block
			wR.waitChatArea.append("서버 접속 실패 \n 잠시후 시도해 주세요.");
//			System.exit(0);
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated constructor stub

//		MainFrame mainFrame = new MainFrame();
		Thread client = new Thread(new MainFrame());
	}
}
