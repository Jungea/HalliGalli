package halligalli;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
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

public class MiniHGClient {

	private JFrame frame;
	private JPanel panel;
	private JLabel message;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	JLabel[] pName; // 플레이어 이름
	JLabel[] pCardNum; // 남은 카드 개수
	JLabel[] pCard; // 보이는카드

	JButton bell = new JButton("Bell");
	JButton turnCard = new JButton("Turn");

	public MiniHGClient() throws UnknownHostException, IOException {

		socket = new Socket("localhost", 9000);

		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);

		frame = new JFrame();
		frame.setTitle("Mini Halli Galli");
		frame.setSize(350, 400);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		turnCard.setFont(new Font("Dialog", Font.PLAIN, 30));
		bell.setFont(new Font("Dialog", Font.PLAIN, 30));
		buttonPanel.add(turnCard);
		buttonPanel.add(bell);

		JPanel totalCardPanel = new JPanel();
		totalCardPanel.setLayout(new GridLayout(2, 2));
		JPanel[] cardPanel = new JPanel[4];

		EtchedBorder eborder = new EtchedBorder(EtchedBorder.RAISED);
		pName = new JLabel[4];
		pCardNum = new JLabel[4];
		pCard = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardPanel[i] = new JPanel();
			pName[i] = new JLabel("player" + (i + 1));
			pCardNum[i] = new JLabel("0장");

			pCard[i] = new JLabel("레몬" + i);
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
		message.setFont(new Font("Dialog", Font.PLAIN, 30));
		jp.add(message, "North");
		jp.add(buttonPanel, "South");
		jp.add(totalCardPanel, "Center");

		frame.add(jp);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		MiniHGClient client = new MiniHGClient();

	}

}
