package halligalli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class HG_Board extends JPanel {

	// ImageIcon[] cardImage; //ī�� �ո� �̹���
	ImageIcon cardBackImage; // ī�� �޸� �̹���
	ImageIcon bellImage; // �� �̹���
	ImageIcon[] playerCardImage; // �÷��̾� ī�� �̹���

	JLabel[] playerName; // �÷��̾� �̸�(���忡��)
	JLabel[] playerCardNum; // �÷��̾� ���� ī��
	String[] userName; // �濡 ���� ���� �̸�

	public HG_Board() {

		setLayout(null);
		playerCardImage = new ImageIcon[4];
		playerName = new JLabel[4];
		playerCardNum = new JLabel[4];
		userName = new String[4];

		cardBackImage = new ImageIcon("image/CardBack.jpg");
		bellImage = new ImageIcon("image/Bell.png");
		for (int i = 0; i < 4; i++) // �÷��̾� ī�带 �޸�����
			playerCardImage[i] = cardBackImage;

		// ����
		playerName[0] = new JLabel("Player1");
		playerName[0].setBounds(1, 1, 50, 15);
		add(playerName[0]);
		playerCardNum[0] = new JLabel("0��");
		playerCardNum[0].setBounds(50, 1, 70, 15);
		add(playerCardNum[0]);
		playerName[1] = new JLabel("Player2");
		playerName[1].setBounds(250, 1, 50, 15);
		add(playerName[1]);
		playerCardNum[1] = new JLabel("0��");
		playerCardNum[1].setBounds(300, 1, 70, 15);
		add(playerCardNum[1]);
		playerName[2] = new JLabel("Player3");
		playerName[2].setBounds(250, 300, 50, 15);
		add(playerName[2]);
		playerCardNum[2] = new JLabel("0��");
		playerCardNum[2].setBounds(300, 300, 70, 15);
		add(playerCardNum[2]);
		playerName[3] = new JLabel("Player4");
		playerName[3].setBounds(1, 300, 50, 15);
		add(playerName[3]);
		playerCardNum[3] = new JLabel("0��");
		playerCardNum[3].setBounds(50, 300, 70, 15);
		add(playerCardNum[3]);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		playerCardImage[0].paintIcon(this, g, 10, 20);
		playerCardImage[1].paintIcon(this, g, 250, 20);
		playerCardImage[2].paintIcon(this, g, 10, 320);
		playerCardImage[3].paintIcon(this, g, 250, 320);
		bellImage.paintIcon(this, g, 180, 180);
	}
}

public class HGClient extends JFrame {

	JLabel gameInfo_Label = new JLabel("[�����˸�]"); // ���� �� ���� ���� �˸�

	HG_Board board = new HG_Board(); // �Ҹ����� �� ����

	JTextField ipAddress_TF = new JTextField(); // ip �ּ� �Է� ����
	JTextField name_TF = new JTextField(" "); // ����� �̸�(�г���) �Է� ����
	JButton connectButton = new JButton("����"); // ���� ��ư
	JButton readyButton = new JButton("Ready"); // �غ� ��ư

	JLabel playerInfo_La = new JLabel("���� �ο�");
	JButton turnButton = new JButton("ī�� ������"); // ī�� ������ ��ư
	JButton bellButton = new JButton("��ġ��"); // ��ġ�� ��ư

	JTextArea chatArea_TA = new JTextArea(); // �޼����� ����� ����
	JScrollPane scrollPane = new JScrollPane(chatArea_TA); // ��ũ��
	JTextField sendMsg_TF = new JTextField(); // �޼��� ���� �ʵ�

	public HGClient() {
		setTitle("Halli Galli");
		Container container = getContentPane();
		container.setLayout(null); // ���� ��η� ���̾ƿ� ����

		gameInfo_Label.setBounds(20, 20, 480, 30); // �����˸� ��ġ,ũ��
		gameInfo_Label.setFont(new Font("����", Font.BOLD, 20));
		container.add(gameInfo_Label);

		board.setBounds(20, 65, 480, 480); //// JPanel�� ��ӹޱ� ������ setBounds�� ��밡��
		board.setBackground(Color.CYAN);
		container.add(board);
		
//		readyButton.setBounds(520, 30, 250, 70);
//		readyButton.setFont(new Font("����", Font.BOLD, 50));
//		add(readyButton);
//		���� ���� ��� �߰� �� ���
		
		//���� ����, ���� �غ�
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new GridLayout(3,2));
		tempPanel.add(new Label("�����ּ� : ",2));
		tempPanel.add(ipAddress_TF);
		tempPanel.add(new Label("�̸� : ", 2));
		tempPanel.add(name_TF);
		tempPanel.add(connectButton);
		tempPanel.add(readyButton);
		readyButton.setEnabled(false);
		tempPanel.setBounds(520,30,250,70);
		add(tempPanel);

		//���� ���ӹ濡 �����ִ� �������, ���� ��ư
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(playerInfo_La, "North");
		JPanel p1Buttons = new JPanel();  // ��ư��
		p1Buttons.add(turnButton);
		p1Buttons.add(bellButton);
		turnButton.setEnabled(false);
		bellButton.setEnabled(false);
		p1.add(p1Buttons, "South");
		p1.setBounds(520, 110, 250, 180);
		p1.setBackground(Color.yellow);
		p1Buttons.setBackground(Color.yellow);
		add(p1);
		
		//ä�ñ��
		JPanel chattingPanel = new JPanel();
		chattingPanel.setLayout(new BorderLayout());
		chattingPanel.add(scrollPane, "Center");
		chattingPanel.add(sendMsg_TF, "South");
		chattingPanel.setBounds(520, 300, 250, 245);
		add(chattingPanel);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		HGClient client = new HGClient();
		client.setSize(810, 610);
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setVisible(true);
	}

}
