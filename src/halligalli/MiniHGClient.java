package halligalli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

public class MiniHGClient extends JFrame implements Runnable {
	int playerId;
	int cardNum = 14;
	private JLabel message;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	JPanel[] cardPanel;
	JLabel[] pName; // �÷��̾� �̸�
	JLabel[] pCardNum; // ���� ī�� ����
	JLabel[] pCard; // ���̴�ī��
	EtchedBorder eb = new EtchedBorder(EtchedBorder.RAISED);
	LineBorder lb = new LineBorder(Color.YELLOW, 3);

	JButton bellButton = new JButton("Bell");
	JButton turnButton = new JButton("Turn");

	JTextArea chatArea;

	public MiniHGClient() throws UnknownHostException, IOException {

		socket = new Socket("localhost", 8888);

		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);

		setSize(600, 400);
		Container ct = getContentPane();
		ct.setLayout(new GridLayout(1, 2));

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
				output.println("BELL " + playerId);
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
			pCardNum[i] = new JLabel(cardNum + "��");

			pCard[i] = new JLabel();
			pCard[i].setFont(new Font("Dialog", Font.BOLD, 50));

			cardPanel[i].add(pName[i]);
			cardPanel[i].add(pCardNum[i]);
			cardPanel[i].add(pCard[i]);
			cardPanel[i].setBorder(eb);
			totalCardPanel.add(cardPanel[i]);
		}

		JPanel gameJp = new JPanel();
		gameJp.setLayout(new BorderLayout());
		message = new JLabel("[�����˸�]"); // ���� �� ���� ���� �˸�
		// message.setFont(new Font("Dialog", Font.PLAIN, 20));
		gameJp.add(message, "North");
		gameJp.add(buttonPanel, "South");
		gameJp.add(totalCardPanel, "Center");

		JPanel userJP = new JPanel();
		userJP.setLayout(new BorderLayout());
		chatArea = new JTextArea(1, 1);
		chatArea.setEditable(false);
		JScrollPane sp = new JScrollPane(chatArea);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// �ʿ信���ؼ� ������ �������� ��ũ�� �ٰ� �����
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// ���� ��ũ���� �ȸ����

		userJP.add(sp, "Center");
		JTextField chatInput = new JTextField("");
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
					if (chatting.length() > 20) {
						chatting = chatting.substring(0, 20);

						chatInput.setText("");
						output.println("CHAT " + playerId + " " + chatting);
					}
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		userJP.add(chatInput, "South");

		ct.add(gameJp);
		ct.add(userJP);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

	}

	public void run() {
		String response;

		try {
			response = input.readLine();

			if (response.startsWith("START")) {
				playerId = response.charAt(6) - 48;
				message.setText("��Ⱑ ���۵˴ϴ�.");
				setTitle("����� player" + playerId);
			}

			while ((response = input.readLine()) != null) {
				if (response.startsWith("NOW")) {
					if (response.charAt(4) - 48 == playerId) {
						turnButton.setEnabled(true);
						System.out.println(playerId + ">> ������.");
					}
					cardPanel[response.charAt(4) - 48].setBorder(lb);
				} else if (response.startsWith("PRINT")) {
					message.setText(response.substring(6));
					if (response.endsWith("ī�带 ���������ϴ�."))
						cardPanel[response.charAt(12) - 48].setBorder(eb);

				} else if (response.startsWith("REPAINT")) {
					String[] s = response.split("/");
					if (s.length == 4) {
						pCard[Integer.parseInt(s[1])].setText(s[2]);
						// cardNum=Integer.parseInt(s[3]);
						pCardNum[Integer.parseInt(s[1])].setText(s[3] + "��");
					} else {
						pCardNum[Integer.parseInt(s[1])].setText(s[2] + "��");
					}
				} else if (response.startsWith("CHAT")) {
					chatArea.setText(response.charAt(5) - 48 + " >>" + response.substring(7));
				} else if (response.startsWith("NOTI")) {
					chatArea.append(response.substring(5) + "\n");
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		Thread client = new Thread(new MiniHGClient());
		client.start();

	}

}