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

	// ImageIcon[] cardImage; //카드 앞면 이미지
	ImageIcon cardBackImage; // 카드 뒷면 이미지
	ImageIcon bellImage; // 벨 이미지
	ImageIcon[] playerCardImage; // 플레이어 카드 이미지

	JLabel[] playerName; // 플레이어 이름(보드에서)
	JLabel[] playerCardNum; // 플레이어 남은 카드
	String[] userName; // 방에 들어온 유저 이름

	public HG_Board() {

		setLayout(null);
		playerCardImage = new ImageIcon[4];
		playerName = new JLabel[4];
		playerCardNum = new JLabel[4];
		userName = new String[4];

		cardBackImage = new ImageIcon("image/CardBack.jpg");
		bellImage = new ImageIcon("image/Bell.png");
		for (int i = 0; i < 4; i++) // 플레이어 카드를 뒷면으로
			playerCardImage[i] = cardBackImage;

		// 글자
		playerName[0] = new JLabel("Player1");
		playerName[0].setBounds(1, 1, 50, 15);
		add(playerName[0]);
		playerCardNum[0] = new JLabel("0장");
		playerCardNum[0].setBounds(50, 1, 70, 15);
		add(playerCardNum[0]);
		playerName[1] = new JLabel("Player2");
		playerName[1].setBounds(250, 1, 50, 15);
		add(playerName[1]);
		playerCardNum[1] = new JLabel("0장");
		playerCardNum[1].setBounds(300, 1, 70, 15);
		add(playerCardNum[1]);
		playerName[2] = new JLabel("Player3");
		playerName[2].setBounds(250, 300, 50, 15);
		add(playerName[2]);
		playerCardNum[2] = new JLabel("0장");
		playerCardNum[2].setBounds(300, 300, 70, 15);
		add(playerCardNum[2]);
		playerName[3] = new JLabel("Player4");
		playerName[3].setBounds(1, 300, 50, 15);
		add(playerName[3]);
		playerCardNum[3] = new JLabel("0장");
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

	JLabel gameInfo_Label = new JLabel("[정보알림]"); // 왼쪽 위 게임 정보 알림

	HG_Board board = new HG_Board(); // 할리갈리 판 생성

	JTextField ipAddress_TF = new JTextField(); // ip 주소 입력 상자
	JTextField name_TF = new JTextField(" "); // 사용자 이름(닉네임) 입력 상자
	JButton connectButton = new JButton("연결"); // 연결 버튼
	JButton readyButton = new JButton("Ready"); // 준비 버튼

	JLabel playerInfo_La = new JLabel("참여 인원");
	JButton turnButton = new JButton("카드 뒤집기"); // 카드 뒤집기 버튼
	JButton bellButton = new JButton("종치기"); // 종치기 버튼

	JTextArea chatArea_TA = new JTextArea(); // 메세지가 띄워질 공간
	JScrollPane scrollPane = new JScrollPane(chatArea_TA); // 스크롤
	JTextField sendMsg_TF = new JTextField(); // 메세지 보낼 필드

	public HGClient() {
		setTitle("Halli Galli");
		Container container = getContentPane();
		container.setLayout(null); // 절대 경로로 레이아웃 설정

		gameInfo_Label.setBounds(20, 20, 480, 30); // 정보알림 위치,크기
		gameInfo_Label.setFont(new Font("굴림", Font.BOLD, 20));
		container.add(gameInfo_Label);

		board.setBounds(20, 65, 480, 480); //// JPanel을 상속받기 때문에 setBounds를 사용가능
		board.setBackground(Color.CYAN);
		container.add(board);
		
//		readyButton.setBounds(520, 30, 250, 70);
//		readyButton.setFont(new Font("바탕", Font.BOLD, 50));
//		add(readyButton);
//		대기실 연결 기능 추가 후 사용
		
		//서버 연결, 게임 준비
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new GridLayout(3,2));
		tempPanel.add(new Label("서버주소 : ",2));
		tempPanel.add(ipAddress_TF);
		tempPanel.add(new Label("이름 : ", 2));
		tempPanel.add(name_TF);
		tempPanel.add(connectButton);
		tempPanel.add(readyButton);
		readyButton.setEnabled(false);
		tempPanel.setBounds(520,30,250,70);
		add(tempPanel);

		//현재 게임방에 들어와있는 유저목록, 게임 버튼
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(playerInfo_La, "North");
		JPanel p1Buttons = new JPanel();  // 버튼만
		p1Buttons.add(turnButton);
		p1Buttons.add(bellButton);
		turnButton.setEnabled(false);
		bellButton.setEnabled(false);
		p1.add(p1Buttons, "South");
		p1.setBounds(520, 110, 250, 180);
		p1.setBackground(Color.yellow);
		p1Buttons.setBackground(Color.yellow);
		add(p1);
		
		//채팅기능
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
