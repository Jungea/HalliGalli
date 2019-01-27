package halligalli;

/*
 * �ۼ���: ������
 * �Ҹ����� ���� ���ӹ� Panel
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
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

public class GameRoom extends JPanel {
	public inviteList i;

	Timer timer;
	TimerTask task;
	ImageIcon[][] cardImg; // ī�� �̹��� ����� ImageIcon
	ImageIcon cardBackImg; // ī�� �޸� ImageIcon
	ImageIcon emptyImg;
//		ImageIcon bellImg;

	int playerId;
	String[] Name = new String[4];
	int roomNum;
	// int cardNum = 14;
	JLabel info;

	JPanel[] cardPanel;
	JLabel[] pName; // �÷��̾� �̸�
	JLabel[] pCardNum; // ���� ī�� ����
	JLabel[] pCard = new JLabel[4]; // ImageIcon�� ������ label

	EtchedBorder eb = new EtchedBorder(EtchedBorder.RAISED);
	LineBorder lb = new LineBorder(Color.YELLOW, 3);

	JButton readyButton = new JButton("READY");
	JButton exitButton = new JButton("������");

	JTextArea userArea;
	JButton inviteButton;

	JButton bellButton = new JButton("Bell");
	JButton turnButton = new JButton("Turn");

	JTextArea chatArea;
	JScrollPane sp;

	MainFrame client;
	JButton backButton;

	public GameRoom(MainFrame client) {
		this.client = client;
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
				client.output.println("TURN " + playerId);
				turnButton.setEnabled(false);
			}
		});
		bellButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		bellButton.setEnabled(false);
		bellButton.addActionListener(e -> client.output.println("BELL " + playerId));
		buttonPanel.add(turnButton);
		buttonPanel.add(bellButton);

		// -------------------------��ư �г�(�÷���)------------------------------//

		JPanel buttonPane2 = new JPanel();
		buttonPane2.setLayout(new GridLayout(1, 2));
		readyButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		readyButton.addActionListener(e -> client.output.println("READY " + playerId));

		exitButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				client.output.println("EXIT");
				client.changeRoom("wR");
				client.setTitle("no: " + client.no + "/ name: " + client.name);
				client.wR.waitChatArea.setText("");
			}
		});
		buttonPane2.add(readyButton);
		buttonPane2.add(exitButton);

		// -------------------------����------------------------------//

		JPanel userPanel = new JPanel();
		userPanel.setBackground(new Color(0x55000000, true));
		userPanel.setLayout(new BorderLayout());
		JLabel userHeader = new JLabel("   player     |     name");
		userHeader.setFont(new Font("Dialog", Font.BOLD, 18));
		userHeader.setForeground(Color.WHITE);
		userHeader.setOpaque(false);
		userArea = new JTextArea(1, 1);
		userArea.setEditable(false);
		userArea.setFont(new Font("Dialog", Font.BOLD, 18));
		inviteButton = new JButton("�ʴ�");
///
		final GameRoom a = this;
		inviteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				i = new inviteList(a);
				client.output.println("LIST");
			}
		});
		userPanel.add(userHeader, "North");
		userPanel.add(userArea, "Center");
		userPanel.add(inviteButton, "South");

		// -------------------------���� �г�------------------------------//

		JPanel totalCardPanel = new JPanel();
		totalCardPanel.setLayout(new GridLayout(2, 2));
		cardPanel = new JPanel[4];

		pName = new JLabel[4];
		pCardNum = new JLabel[4];
		pCard = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardPanel[i] = new JPanel();
			cardPanel[i].setLayout(null);

			pName[i] = new JLabel("player" + i, 0);
			pName[i].setForeground(Color.WHITE);
			pCardNum[i] = new JLabel("14��", 0);
			pCardNum[i].setForeground(Color.WHITE);
			pCard[i] = new JLabel(cardBackImg);

			pName[i].setBounds(50, 10, 110, 30);
			cardPanel[i].add(pName[i]);
			pCardNum[i].setBounds(160, 10, 60, 30);
			cardPanel[i].add(pCardNum[i]);
			pCard[i].setBounds(0, 25, 240, 270);
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
					if (chatting.length() > 15)
						chatting = chatting.substring(0, 16);

					chatInput.setText("");
					chatArea.append("  [��] : " + chatting + "\n");
					client.output.println("CHAT /" + client.name + "/" + chatting);

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

		userPanel.setBounds(560, 95, 310, 180);

		buttonPanel.setBounds(560, 285, 310, 50);

		background.add(gameJp);
		background.add(buttonPane2);
		background.add(userPanel);
		background.add(buttonPanel);
		background.add(userJP);

	}

	public void newTurnTimer() {
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String numStr = pCardNum[playerId].getText().trim();
				int num = Integer.parseInt(numStr.substring(0, numStr.length() - 1));
				if (num > 0)
					client.output.println("TURN " + playerId);
				turnButton.setEnabled(false);
			}
		};

		timer.schedule(task, 7000);
	}
}

class inviteList extends JFrame {
	GameRoom g = null;
	JPanel invitePanel;
	List<String> inviteL = new ArrayList<>();
	JButton[] inviteButton = new JButton[8];
	int invitePage = 0;

	public inviteList(GameRoom g) {
		this.g = g;
		setSize(500, 500);
		Dimension screen1 = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension screen2 = getSize();
		int xpos = (int) (screen1.getWidth() / 2 - screen2.getWidth() / 2);
		int ypos = (int) (screen1.getHeight() / 2 - screen2.getHeight() / 2);
		setLocation(xpos, ypos);

		JPanel total = new JPanel();
		total.setLayout(new BorderLayout());

		invitePanel = new JPanel();
		invitePanel.setLayout(new GridLayout(8, 1));
		invitePanel.setBorder(new LineBorder(Color.BLACK, 1));

		for (int j = 0; j < 8; j++) {
			inviteButton[j] = new JButton();
			final int jj = j;
			inviteButton[j].addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					System.out.println(g.roomNum + "/" + inviteButton[jj].getText());
				}
			});
		}

		inviteRepaint();

		JPanel movePanel = new JPanel();
		movePanel.setLayout(new GridLayout(2, 2));
		JButton up = new JButton("��");
		up.addActionListener(e -> {
			if (invitePage != 0)
				invitePage--;
			inviteRepaint();
		});
		up.setBorderPainted(false);
		up.setContentAreaFilled(false);
		JButton down = new JButton("��");
		down.addActionListener(e -> {
			if ((inviteL.size() - 1) / 8 != invitePage)
				invitePage++;
			inviteRepaint();
		});
		down.setBorderPainted(false);
		down.setContentAreaFilled(false);
		movePanel.add(up);
		movePanel.add(down);

		total.add(invitePanel, "Center");
		total.add(movePanel, "East");

		add(total);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public void inviteRepaint() {
		invitePanel.removeAll();
		for (int j = 0; j < 8; j++) {
			if (j + (8 * invitePage) < inviteL.size()) {
				inviteButton[j].setText(inviteL.get(j + (8 * invitePage)));
				invitePanel.add(inviteButton[j]);
			} else
				invitePanel.add(new JLabel(""));
		}

		invitePanel.revalidate();
		invitePanel.repaint();
	}
}
