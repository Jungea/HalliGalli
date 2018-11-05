package halligalli;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class MiniHGClient extends Thread {
	int playerId;
	private JFrame frame;
	private JPanel panel;
	private JLabel message;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	JLabel[] pName; // 플레이어 이름
	JLabel[] pCardNum; // 남은 카드 개수
	JLabel[] pCard; // 보이는카드

	JButton bellButton = new JButton("Bell");
	JButton turnButton = new JButton("Turn");

	public MiniHGClient() throws UnknownHostException, IOException {

		socket = new Socket("localhost", 8881);

		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);

		frame = new JFrame();
		frame.setSize(350, 400);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		turnButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		turnButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				output.println(e);

			}
		});
		bellButton.setFont(new Font("Dialog", Font.PLAIN, 30));
		buttonPanel.add(turnButton);
		buttonPanel.add(bellButton);

		JPanel totalCardPanel = new JPanel();
		totalCardPanel.setLayout(new GridLayout(2, 2));
		JPanel[] cardPanel = new JPanel[4];

		EtchedBorder eborder = new EtchedBorder(EtchedBorder.RAISED);
		pName = new JLabel[4];
		pCardNum = new JLabel[4];
		pCard = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardPanel[i] = new JPanel();
			pName[i] = new JLabel("player" + i);
			pCardNum[i] = new JLabel("0장");

			pCard[i] = new JLabel();
			pCard[i].setFont(new Font("Dialog", Font.BOLD, 50));

			cardPanel[i].add(pName[i]);
			cardPanel[i].add(pCardNum[i]);
			cardPanel[i].add(pCard[i]);
			cardPanel[i].setBorder(eborder);
			totalCardPanel.add(cardPanel[i]);
		}

		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		message = new JLabel("[정보알림]"); // 왼쪽 위 게임 정보 알림
		// message.setFont(new Font("Dialog", Font.PLAIN, 20));
		jp.add(message, "North");
		jp.add(buttonPanel, "South");
		jp.add(totalCardPanel, "Center");

		frame.add(jp);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	public void run() {
		String response;

		try {
			response = input.readLine();

			if (response.startsWith("START")) {
				playerId = response.charAt(6) - 48;
				message.setText("경기가 시작됩니다.");
				frame.setTitle("경기자 player" + playerId);
			}

			while (true) {
				response = input.readLine();
				if (response.startsWith("PRINT")) {
					message.setText(response.substring(6));
				}

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		MiniHGClient client = new MiniHGClient();
		client.start();

	}

}