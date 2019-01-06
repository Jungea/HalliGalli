package halligalli;

/*
 * 작성자: 정은애
 * 할리갈리 게임 대기실 Panel
 */

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class WaitingRoom extends JPanel {
	MainFrame client;

	String inputName;

	List<Room> roomList = new ArrayList<>(); // 방 리스트
	int page = 0; // 사용자가 보고있는 방 페이지 번호
	JPanel roomPanel;

	JTextArea waitChatArea;
	JScrollPane waitSp;

	JTextArea userArea;

	JButton enterButton;
	JButton makeRoomButton;

	JPanel connectPanel;
	JTextField ipTF;
	JTextField nameTF;
	JButton connectButton;

	String[] roomTitle = new String[3];
	int[] roomCount = new int[3];

	public void roomRepaint() {
		roomPanel.removeAll();
		for (int j = 0; j < 4; j++) {
			if (j + (4 * page) < roomList.size())
				roomPanel.add(roomList.get(j + (4 * page)).getButton());
			else
				roomPanel.add(new JLabel(""));
		}

		roomPanel.revalidate();
		roomPanel.repaint();
	}

	public Room find(int roomNum) {
		for (int k = 0; k < roomList.size(); k++)
			if (roomList.get(k).roomNum == roomNum)
				return roomList.get(k);

		return null;
	}

	public WaitingRoom(MainFrame client) {
		this.client = client;

		setLayout(null);

		JPanel total = new JPanel();
		total.setLayout(new BorderLayout());

		roomPanel = new JPanel();
		roomPanel.setLayout(new GridLayout(2, 2));
		roomPanel.setBorder(new LineBorder(Color.BLACK, 1));

		JPanel movePanel = new JPanel();
		movePanel.setLayout(new GridLayout(2, 2));
		JButton up = new JButton("∧");
		up.addActionListener(e -> {
			if (page != 0)
				page--;
			roomRepaint();
		});
		up.setBorderPainted(false);
		up.setContentAreaFilled(false);
		JButton down = new JButton("∨");
		down.addActionListener(e -> {
			if ((roomList.size() - 1) / 4 != page)
				page++;
			roomRepaint();
		});
		down.setBorderPainted(false);
		down.setContentAreaFilled(false);
		movePanel.add(up);
		movePanel.add(down);

		total.add(roomPanel, "Center");
		total.add(movePanel, "East");

		total.setBounds(30, 30, 330, 200);

		///
		JPanel chatJP = new JPanel();
		chatJP.setLayout(new BorderLayout());
		waitChatArea = new JTextArea(1, 1);
		waitChatArea.setEditable(false);
		waitChatArea.setText("NAME을 입력하고 CONNECTE 버튼을 누르시오. \n");
		waitChatArea.setFont(new Font("Dialog", Font.PLAIN, 13));
		waitSp = new JScrollPane(waitChatArea);
		waitSp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// 필요에의해서 내용이 많아지면 스크롤 바가 생긴다
		waitSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// 가로 스크롤은 안만든다

		chatJP.add(waitSp, "Center");
		JTextField chatInput = new JTextField("");
		waitChatArea.setFont(new Font("Dialog", Font.PLAIN, 13));
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
					waitChatArea.append("  [나] : " + chatting + "\n");
					client.output.println("WCHAT /" + client.name + "/" + chatting);

				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		chatJP.setBounds(30, 250, 310, 180);

		chatJP.add(chatInput, "South");

		//
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new BorderLayout());
		JLabel userHeader = new JLabel("     no    |    name");
		userArea = new JTextArea();
		JScrollPane userSP = new JScrollPane(userArea);
		userPanel.add(userHeader, "North");
		userPanel.add(userSP, "Center");
		userPanel.setBounds(380, 30, 270, 200);
		add(userPanel);

		//

		JPanel rButtonPanel = new JPanel();
		rButtonPanel.setLayout(new GridLayout(1, 2));
		makeRoomButton = new JButton("방만들기");
		makeRoomButton.setEnabled(false);
		makeRoomButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				inputName = JOptionPane.showInputDialog("방 제목을 입력하세요.");
				client.output.println("CREATE " + inputName);

			}
		});
		rButtonPanel.add(makeRoomButton);
		enterButton = new JButton("입장");
		enterButton.setEnabled(false);
		enterButton.addActionListener(e -> client.output.println("ENTER"));
		rButtonPanel.add(enterButton);
		rButtonPanel.setBounds(380, 250, 270, 40);

		//
		connectPanel = new JPanel();
		connectPanel.setLayout(new GridLayout(3, 1));
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 2));
		p1.add(new JLabel("IP : ", 0));
		ipTF = new JTextField();
		p1.add(ipTF);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1, 2));
		p2.add(new JLabel("NAME : ", 0));
		nameTF = new JTextField();
		p2.add(nameTF);
		connectPanel.add(p1);
		connectPanel.add(p2);

		connectButton = new JButton("CONNECT");
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					if (nameTF.getText().length() > 2) {
						client.socket = new Socket("localhost", client.ip);
						client.input = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
						client.output = new PrintWriter(client.socket.getOutputStream(), true);
						new Thread(client).start();
						if (nameTF.getText().length() > 6)
							nameTF.setText(nameTF.getText().substring(0, 7));
						client.name = nameTF.getText();
						client.output.println("CONNECT " + client.name);
						nameTF.setText("");
						nameTF.setEnabled(false);
						connectButton.setEnabled(false);
					}
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		connectPanel.add(connectButton);

		connectPanel.setBounds(380, 320, 270, 110);
//

		add(total);
		add(chatJP);
		add(rButtonPanel);
		add(connectPanel);
//

	}

}

//방 정보를 담는 클래스
class Room {
	int roomNum; // 방번호
	String roomName; // 방 제목
	int enterNum; // 들어온 인원
	JButton button; // 버튼

	public Room(int roomNum, String roomName) {
		this.roomNum = roomNum;
		this.roomName = roomName;
		enterNum = 0;
		button = new JButton("<html><body><p align=\"center\">" + roomNum + "번방</p><p>" + roomName
				+ "</p> <p align=\"right\">" + enterNum + "/4</p></body></html>");
	}

	public Room(int roomNum, String roomName, int enterNum) {
		this.roomNum = roomNum;
		this.roomName = roomName;
		this.enterNum = enterNum;
		button = new JButton("<html><body><p align=\"center\">" + roomNum + "번방</p><p>" + roomName
				+ "</p> <p align=\"right\">" + enterNum + "/4</p></body></html>");
	}

	public void enterPlayer() { // 플레이어 입장
		button.setText("<html><body><p align=\"center\">" + roomNum + "번방</p><p>" + roomName
				+ "</p> <p align=\"right\">" + (++enterNum) + "/4</p></body></html>");
	}

	public JButton getButton() {
		return button;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof Room == false)
			return false;
		Room r = (Room) obj;
		return this.roomNum == r.roomNum && Objects.equals(this.roomName, r.roomName) && this.enterNum == r.enterNum
				&& Objects.equals(this.button.getText(), r.button.getText());
	}

	@Override
	public String toString() {
		return "Room [roomNum=" + roomNum + ", roomName=" + roomName + ", enterNum=" + enterNum + ", button="
				+ button.getText() + "]";
	}

}
