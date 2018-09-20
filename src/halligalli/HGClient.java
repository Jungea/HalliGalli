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

	JLabel gameInfo_Label = new JLabel("[정보알림]"); // 왼쪽 위 게임 정보 알림

	JPanel board = new JPanel();// 할리갈리 판 생성

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
