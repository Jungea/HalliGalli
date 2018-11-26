package halligalli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
import java.util.Timer;
import java.util.TimerTask;

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
	int n;
	Timer timer;
	TimerTask task;
	ImageIcon[][] cardImg; // ī�� �̹��� ����� ImageIcon
	ImageIcon cardBackImg; // ī�� �޸� ImageIcon
	ImageIcon emptyImg;
//	ImageIcon bellImg;

	int playerId;
	// int cardNum = 14;
	private JLabel info;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	JPanel[] cardPanel;
	JLabel[] pName; // �÷��̾� �̸�
	JLabel[] pCardNum; // ���� ī�� ����
	JLabel[] pCard = new JLabel[4]; // ImageIcon�� ������ label

	EtchedBorder eb = new EtchedBorder(EtchedBorder.RAISED);
	LineBorder lb = new LineBorder(Color.YELLOW, 3);

	JButton readyButton = new JButton("READY");
	JButton exitButton = new JButton("������");

	JTextArea userArea;

	JButton bellButton = new JButton("Bell");
	JButton turnButton = new JButton("Turn");

	JTextArea chatArea;
	JScrollPane sp;

	public MiniHGClient() throws UnknownHostException, IOException {

		socket = new Socket("localhost", 8886);

		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);

		setSize(920, 720);
		setLayout(new GridLayout());
		ImageIcon backgroundImg = new ImageIcon("Image/Background.png");
		JPanel background = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(backgroundImg.getImage(), 0, 0, null);
				setOpaque(false); // �׸��� ǥ���ϰ� ����,�����ϰ� ����
				super.paintComponent(g);
				this.repaint();
			}
		};
		add(background);
		background.setLayout(null);

		cardImg = new ImageIcon[4][5];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 5; j++) {
				cardImg[i][j] = new ImageIcon("Image/" + i + j + ".png");
			}
		}
		cardBackImg = new ImageIcon("Image/CardBack.png");
		emptyImg = new ImageIcon();

		// -------------------------��ư �г�(����)------------------------------//

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

		// -------------------------��ư �г�(�÷���)------------------------------//

		JPanel buttonPane2 = new JPanel();
		buttonPane2.setLayout(new GridLayout(1, 2));
		readyButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		readyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				output.println("READY " + playerId);
//						readyButton.setEnabled(false);
			}
		});
		exitButton.setFont(new Font("Dialog", Font.PLAIN, 30));
//		exitButton.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// TODO Auto-generated method stub
//				client.changeRoom("waitingRoom");
//			}
//		});
		buttonPane2.add(readyButton);
		buttonPane2.add(exitButton);

		// -------------------------����------------------------------//

		userArea = new JTextArea(1, 1);
		userArea.setEditable(false);
		userArea.setFont(new Font("Dialog", Font.BOLD, 15));

		// -------------------------���� �г�------------------------------//

		JPanel totalCardPanel = new JPanel();
		totalCardPanel.setLayout(new GridLayout(2, 2));
		cardPanel = new JPanel[4];

		pName = new JLabel[4];
		pCardNum = new JLabel[4];
		pCard = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardPanel[i] = new JPanel();
			pName[i] = new JLabel("player" + i);
			pName[i].setForeground(Color.WHITE);
			pCardNum[i] = new JLabel("14��");
			pCardNum[i].setForeground(Color.WHITE);
			pCard[i] = new JLabel(cardBackImg);

			cardPanel[i].add(pName[i]);
			cardPanel[i].add(pCardNum[i]);
			cardPanel[i].add(pCard[i]);
			cardPanel[i].setBorder(eb);
			totalCardPanel.add(cardPanel[i]);

			cardPanel[i].setOpaque(false);
		}
		totalCardPanel.setBackground(new Color(0x55000000, true));

		JPanel gameJp = new JPanel();
		gameJp.setLayout(null);
		info = new JLabel("[�����˸�]"); // ���� �� ���� ���� �˸�
		info.setFont(new Font("Dialog", Font.BOLD, 20));
		info.setForeground(Color.WHITE);
		gameJp.add(info);
		gameJp.add(totalCardPanel);
		gameJp.setOpaque(false);

		info.setBounds(0, 0, 490, 20);
		totalCardPanel.setBounds(0, 35, 480, 600);
		gameJp.setBounds(30, 20, 480, 635);

		// -------------------------ä�� �г�------------------------------//

		JPanel userJP = new JPanel();
		userJP.setLayout(new BorderLayout());
		chatArea = new JTextArea(1, 1);
		chatArea.setEditable(false);
		chatArea.setFont(new Font("Dialog", Font.BOLD, 15));
		sp = new JScrollPane(chatArea);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// �ʿ信���ؼ� ������ �������� ��ũ�� �ٰ� �����
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// ���� ��ũ���� �ȸ����

		userJP.add(sp, "Center");
		JTextField chatInput = new JTextField("");
		chatArea.setFont(new Font("Dialog", Font.BOLD, 15));
		chatArea.setForeground(Color.WHITE);
		chatInput.setForeground(Color.WHITE);
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
					chatArea.append("[��] >>> " + chatting + "\n");
					output.println("CHAT " + playerId + " " + chatting);

				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		chatArea.setOpaque(false);
		chatInput.setOpaque(false);
		sp.setOpaque(false);
		sp.getViewport().setOpaque(false);
		// userJP.setBackground(new Color(0x60ffffff, true));
		userJP.setBackground(new Color(0x55000000, true));

		userJP.setBounds(560, 355, 310, 298);

		userJP.add(chatInput, "South");
		buttonPane2.setBounds(560, 20, 310, 50);

		userArea.setBounds(560, 95, 310, 180);

		buttonPanel.setBounds(560, 285, 310, 50);

		background.add(gameJp);
		background.add(buttonPane2);
		background.add(userArea);
		background.add(buttonPanel);
		background.add(userJP);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

	}

	public void newGame() {
		n = 5;
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (n == 0) {
					timer.cancel();
					output.println("NEWGAME");
				} else {
					output.println("NOTI " + n + "�� �� ����");
					n--;
				}
			}
		};
		timer.schedule(task, 1000, 1000);

	}

	public void newTurnTimer() {
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String numStr = pCardNum[playerId].getText();
				int num = Integer.parseInt(numStr.substring(0, numStr.length() - 1));
				if (num > 0)
					output.println("TURN " + playerId);
				turnButton.setEnabled(false);
			}
		};

		timer.schedule(task, 3000);
	}

	public void run() {
		String response;

		try {
			response = input.readLine();

			if (response.startsWith("START")) {
				playerId = response.charAt(6) - 48;
				info.setText("��� �غ� ���Դϴ�.");
				setTitle("����� player" + playerId);
			}

			while ((response = input.readLine()) != null) {
				if (response.startsWith("NOW")) {
					if (response.charAt(4) - 48 == playerId) {
						turnButton.setEnabled(true);
						System.out.println(playerId + ">> ������.");
						newTurnTimer();
					}
					cardPanel[response.charAt(4) - 48].setBorder(lb);
				} else if (response.startsWith("PRINT")) {
					info.setText(response.substring(6));
					if (response.endsWith("ī�带 ���������ϴ�.")) {
						cardPanel[response.charAt(12) - 48].setBorder(eb);
						if (playerId == (response.charAt(12) - 48)) {
							turnButton.setEnabled(false);
							timer.cancel();
						}
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
						pCardNum[Integer.parseInt(s[1])].setText(s[3] + "��");
					} else {
						pCardNum[Integer.parseInt(s[1])].setText(s[2] + "��");
					}
				} else if (response.startsWith("CHAT")) {
					int chatId = response.charAt(5) - 48;
					if (chatId != playerId)
						chatArea.append("player" + chatId + " >>>" + response.substring(7) + "\n");
					sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
				} else if (response.startsWith("NOTI")) {
					chatArea.append(response.substring(5) + "\n");
					sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
				} else if (response.startsWith("DIE")) {
					if (response.charAt(4) - 48 == playerId) {
						info.setText("���ӿ���");
						turnButton.setEnabled(false);
						bellButton.setEnabled(false);
					}
					cardPanel[response.charAt(4) - 48].setBorder(eb);
				} else if (response.startsWith("WIN")) {
					if (response.charAt(4) - 48 == playerId) {
						timer.cancel();
						info.setText("WIN!!");
						turnButton.setEnabled(false);
						bellButton.setEnabled(false);
						newGame();
					} else
						info.setText("player" + (response.charAt(4) - 48) + " �¸�");
					cardPanel[response.charAt(4) - 48].setBorder(eb);
					for (int i = 0; i < 4; i++)
						if (i == (response.charAt(4) - 48))
							pCard[response.charAt(4) - 48].setIcon(cardBackImg);
						else
							pCard[i].setIcon(emptyImg);
				} else if (response.startsWith("NEWGAME")) {
					for (int i = 0; i < 4; i++) {
						pCardNum[i].setText("14��");
						pCard[i].setIcon(cardBackImg);
					}
					bellButton.setEnabled(true);
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