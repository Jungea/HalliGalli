package halligalli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class HGClient extends JFrame {

	JLabel gameInfo_Label = new JLabel("[�����˸�]"); // ���� �� ���� ���� �˸�

	JPanel board = new JPanel();// �Ҹ����� �� ����

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
