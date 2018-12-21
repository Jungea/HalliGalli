package halligalli;

import java.awt.BorderLayout;
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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class WaitingRoom extends JPanel {
	MainFrame client;

	JPanel roomPanel;
	JButton[] room = new JButton[3];

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

	public WaitingRoom(MainFrame client) {
		this.client = client;

		setLayout(null);

		//
		roomPanel = new JPanel();
		roomPanel.setLayout(new GridLayout(3, 1));
		room[0] = new JButton("방제목: 방1        인원: " + roomCount[0] + "/4");
		room[0].addActionListener(e -> client.output.println("ENTER 0"));
		roomPanel.add(room[0]);
		room[1] = new JButton("방제목: 방2        인원: " + roomCount[0] + "/4");
		room[1].addActionListener(e -> client.output.println("ENTER 1"));
		roomPanel.add(room[1]);
		room[2] = new JButton("방제목: 방3        인원: " + roomCount[0] + "/4");
		room[2].addActionListener(e -> client.output.println("ENTER 2"));
		roomPanel.add(room[2]);
		roomPanel.setBounds(30, 30, 310, 200);

		///
		JPanel chatJP = new JPanel();
		chatJP.setLayout(new BorderLayout());
		waitChatArea = new JTextArea(1, 1);
		waitChatArea.setEditable(false);
		waitChatArea.setText("NAME을 입력하고 CONNECTE 버튼을 누르시오.");
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
					if (chatting.length() > 20)
						chatting = chatting.substring(0, 20);

					chatInput.setText("");
					waitChatArea.append("[나] >>> " + chatting + "\n");
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
//		rButtonPanel.add(makeRoomButton);
		enterButton = new JButton("입장");
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
					if (nameTF.getText().trim().length() > 2) {
						client.socket = new Socket("localhost", client.ip);
						client.input = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
						client.output = new PrintWriter(client.socket.getOutputStream(), true);
						new Thread(client).start();
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

		add(roomPanel);
		add(chatJP);
		add(rButtonPanel);
		add(connectPanel);
//

	}

}
