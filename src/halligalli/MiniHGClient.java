package halligalli;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.border.LineBorder;

public class MiniHGClient extends Thread {
	int playerId; // 플레이어 번호
	int cardNum = 14; // 남은 카드 개수 값
	private JFrame frame;
	private JLabel message; // 알림창 레이블
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	JPanel[] cardPanel; // pName, pCardNum, pCard의 panel
	JLabel[] pName; // 플레이어 이름 레이블
	JLabel[] pCardNum; // 남은 카드 개수 레이블
	JLabel[] pCard; // 보이는카드 레이블
	EtchedBorder eb = new EtchedBorder(EtchedBorder.RAISED);
	LineBorder lb = new LineBorder(Color.YELLOW, 3); // 현재 차례 강조

	JButton bellButton = new JButton("Bell");
	JButton turnButton = new JButton("Turn");

	public MiniHGClient() throws UnknownHostException, IOException {

		socket = new Socket("localhost", 8885);

		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);

		frame = new JFrame();
		frame.setSize(350, 400);

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
				output.println(e);

			}
		});
		buttonPanel.add(turnButton);
		buttonPanel.add(bellButton);

		JPanel totalCardPanel = new JPanel();
		totalCardPanel.setLayout(new GridLayout(2, 2));
		cardPanel = new JPanel[4];

		pName = new JLabel[4];
		pCardNum = new JLabel[4];
		pCard = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardPanel[i] = new JPanel();
			pName[i] = new JLabel("player" + i);
			pCardNum[i] = new JLabel(cardNum + "장");

			pCard[i] = new JLabel();
			pCard[i].setFont(new Font("Dialog", Font.BOLD, 50));

			cardPanel[i].add(pName[i]);
			cardPanel[i].add(pCardNum[i]);
			cardPanel[i].add(pCard[i]);
			cardPanel[i].setBorder(eb);
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

			while ((response = input.readLine()) != null) {
				if (response.startsWith("NOW")) {
					if (response.charAt(4) - 48 == playerId) {
						turnButton.setEnabled(true);
						System.out.println(playerId + ">> 내차례.");
					}
					cardPanel[response.charAt(4) - 48].setBorder(lb);
				} else if (response.startsWith("PRINT")) {
					message.setText(response.substring(6));
					if (response.endsWith("카드를 뒤집었습니다."))
						cardPanel[response.charAt(12) - 48].setBorder(eb);

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