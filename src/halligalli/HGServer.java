package halligalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// �ڹ� å�� ���� ���� ���� Ȱ�� - ���� ���� 1

public class HGServer {
	public static void main(String[] args) throws IOException {
		System.out.println("�Ҹ����� ���� ������ �������Դϴ�.");
		int clientId = 0;
		ServerSocket ss = new ServerSocket(9101); // ��������
		try {
			while (true) {

				try {
					clientId++;
					Translator t = new Translator(ss.accept(), clientId);
					t.start();
					// Ŭ���̾�Ʈ�� ����Ǹ� ���ο� ������ ����(�����δ� �� �������� ��ȣ��ȭ)
				} finally {
					ss.close();
				}
			}

		} finally {
			ss.close();
		}
	}

	private static class Translator extends Thread {
		private Socket socket; // accept�� Ŭ���̾�Ʈ���� �����������
		private int myId;

		public Translator(Socket socket, int clientId) {
			this.socket = socket;
			this.myId = clientId;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// ������ ������
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // �������� ������

				out.println("�ȳ��ϼ���? Ŭ���̾�Ʈ" + myId + " �Դϴ�.");

				while (true) {
					String input = in.readLine();
					if (input == null)
						break;
				}
			} catch (IOException e) {
				System.out.println("Ŭ���̾�Ʈ" + myId + " ó�� ���� " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("���� ���� ���� " + e);
				}
				System.out.println("Ŭ���̾�Ʈ" + myId + " ó�� ����");
			}

		}
	}

}
