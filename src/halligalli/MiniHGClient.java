package halligalli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

public class MiniHGClient extends JFrame implements Runnable {
	ImageIcon[][] cardImg; // 카드 이미지 저장된 ImageIcon
	ImageIcon cardBackImg; // 카드 뒷면 ImageIcon
	ImageIcon emptyImg;
//	ImageIcon bellImg;

	int playerId;
	// int cardNum = 14;
	private JLabel info;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	JPanel[] cardPanel;
	JLabel[] pName; // 플레이어 이름
	JLabel[] pCardNum; // 남은 카드 개수
	JLabel[] pCard = new JLabel[4]; // ImageIcon을 보여줄 label

	EtchedBorder eb = new EtchedBorder(EtchedBorder.RAISED);
	LineBorder lb = new LineBorder(Color.YELLOW, 3);

	JButton bellButton = new JButton("Bell");
	JButton turnButton = new JButton("Turn");

	JTextArea chatArea;
	JScrollPane sp;

	public MiniHGClient() throws UnknownHostException, IOException {

		socket = new Socket("localhost", 8886);

		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);

		setSize(900, 700);
		Container ct = getContentPane();
		ct.setLayout(new GridLayout());
//		JLabel background = new JLabel(new ImageIcon("Image/Background.png"));
//		add(background);
//		background.setLayout(new GridLayout(1, 2));

		cardImg = new ImageIcon[4][5];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 5; j++) {
				cardImg[i][j] = new ImageIcon("Image/" + i + j + ".png");
			}
		}
		cardBackImg = new ImageIcon("Image/CardBack.png");
		emptyImg = new ImageIcon();

		// -------------------------버튼 패널------------------------------//

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		turnButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		turnButton.setEnabled(false);
		turnButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				output.println("TURN " + playerId);
				turnButton.setEnabled(false);
			}
		});
		bellButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		bellButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				output.println("BELL " + playerId);
			}
		});
		buttonPanel.add(turnButton);
		buttonPanel.add(bellButton);

		// -------------------------게임 패널------------------------------//

		JPanel totalCardPanel = new JPanel();
		totalCardPanel.setLayout(new GridLayout(2, 2));
		cardPanel = new JPanel[4];

		pName = new JLabel[4];
		pCardNum = new JLabel[4];
		pCard = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardPanel[i] = new JPanel();
			pName[i] = new JLabel("player" + i);
//			pName[i].setForeground(Color.WHITE);
			pCardNum[i] = new JLabel("14장");
//			pCardNum[i].setForeground(Color.WHITE);
			pCard[i] = new JLabel(cardBackImg);

			cardPanel[i].add(pName[i]);
			cardPanel[i].add(pCardNum[i]);
			cardPanel[i].add(pCard[i]);
			cardPanel[i].setBorder(eb);
			totalCardPanel.add(cardPanel[i]);

//			cardPanel[i].setOpaque(false);
		}
//		totalCardPanel.setBackground(new Color(0x55000000, true));

		JPanel gameJp = new JPanel();
		gameJp.setLayout(new BorderLayout());
		info = new JLabel("[정보알림]"); // 왼쪽 위 게임 정보 알림
		info.setFont(new Font("Dialog", Font.BOLD, 20));
//		info.setForeground(Color.WHITE);
		gameJp.add(info, "North");
		gameJp.add(buttonPanel, "South");
		gameJp.add(totalCardPanel, "Center");
//		gameJp.setOpaque(false);

		// -------------------------채팅 패널------------------------------//

		JPanel userJP = new JPanel();
		userJP.setLayout(new BorderLayout());
		chatArea = new JTextArea(1, 1);
		chatArea.setEditable(false);
		sp = new JScrollPane(chatArea);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// 필요에의해서 내용이 많아지면 스크롤 바가 생긴다
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// 가로 스크롤은 안만든다

		userJP.add(sp, "Center");
		JTextField chatInput = new JTextField("");
		chatInput.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String chatting = chatInput.getText();
					if (chatting.length() == 0)
						return;
					if (chatting.length() > 20)
						chatting = chatting.substring(0, 20);

					chatInput.setText("");
					chatArea.append("[나] >>> " + chatting + "\n");
					output.println("CHAT " + playerId + " " + chatting);

				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		userJP.add(chatInput, "South");

		ct.add(gameJp);
		ct.add(userJP);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

	}

	public void run() {
		String response;

		try {
			response = input.readLine();

			if (response.startsWith("START")) {
				playerId = response.charAt(6) - 48;
				info.setText("경기가 시작됩니다.");
				setTitle("경기자 player" + playerId);
			}

			while ((response = input.readLine()) != null) {
				if (response.startsWith("NOW")) {
					if (response.charAt(4) - 48 == playerId) {
						turnButton.setEnabled(true);
						System.out.println(playerId + ">> 내차례.");
					}
					cardPanel[response.charAt(4) - 48].setBorder(lb);
				} else if (response.startsWith("PRINT")) {
					info.setText(response.substring(6));
					if (response.endsWith("카드를 뒤집었습니다.")) {
						cardPanel[response.charAt(12) - 48].setBorder(eb);
						if (playerId == (response.charAt(12) - 48))
							turnButton.setEnabled(false);
					}

				} else if (response.startsWith("REPAINT")) {
					String[] s = response.split("/");
					if (s.length == 4) {
						if (s[2].length() > 0)
							pCard[Integer.parseInt(s[1])].setIcon(cardImg[s[2].charAt(0) - 48][s[2].charAt(2) - 49]);
						else {
							if (!s[3].equals("0"))
								pCard[Integer.parseInt(s[1])].setIcon(cardBackImg);
							else
								pCard[Integer.parseInt(s[1])].setIcon(emptyImg);
						}
						pCardNum[Integer.parseInt(s[1])].setText(s[3] + "장");
					} else {
						pCardNum[Integer.parseInt(s[1])].setText(s[2] + "장");
					}
				} else if (response.startsWith("CHAT")) {
					int chatId = response.charAt(5) - 48;
					sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
					if (chatId != playerId)
						chatArea.append("player" + chatId + " >>>" + response.substring(7) + "\n");
				} else if (response.startsWith("NOTI")) {
					chatArea.append(response.substring(5) + "\n");
					sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
				} else if (response.startsWith("DIE")) {
					if (response.charAt(4) - 48 == playerId) {
						info.setText("게임오버");
						turnButton.setEnabled(false);
						bellButton.setEnabled(false);
					}
					cardPanel[response.charAt(4) - 48].setBorder(eb);
				} else if (response.startsWith("WIN")) {
					if (response.charAt(4) - 48 == playerId) {
						info.setText("WIN!!");
						turnButton.setEnabled(false);
						bellButton.setEnabled(false);
					} else
						info.setText("player" + (response.charAt(4) - 48) + " 승리");

					for (int i = 0; i < 4; i++)
						if (i == (response.charAt(4) - 48))
							pCard[response.charAt(4) - 48].setIcon(cardBackImg);
						else
							pCard[i].setIcon(emptyImg);

				}

			}

		} catch (

		IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		Thread client = new Thread(new MiniHGClient());
		client.start();

	}

}