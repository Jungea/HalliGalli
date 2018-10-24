package halligalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// 자바 책의 영어 번역 서버 활용 - 서버 연습 1

public class HGServer {
	public static void main(String[] args) throws IOException {
		System.out.println("할리갈리 게임 서버가 실행중입니다.");
		int clientId = 0;
		ServerSocket ss = new ServerSocket(9101); // 서버소켓
		try {
			while (true) {

				try {
					clientId++;
					Translator t = new Translator(ss.accept(), clientId);
					t.start();
					// 클라이언트와 연결되면 새로운 소켓을 생성(앞으로는 이 소켓으로 상호대화)
				} finally {
					ss.close();
				}
			}

		} finally {
			ss.close();
		}
	}

	private static class Translator extends Thread {
		private Socket socket; // accept한 클라이언트와의 서버연결소켓
		private int myId;

		public Translator(Socket socket, int clientId) {
			this.socket = socket;
			this.myId = clientId;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// 서버에 들어오는
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // 서버에서 나가는

				out.println("안녕하세요? 클라이언트" + myId + " 입니다.");

				while (true) {
					String input = in.readLine();
					if (input == null)
						break;
				}
			} catch (IOException e) {
				System.out.println("클라이언트" + myId + " 처리 실패 " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("소켓 종류 오류 " + e);
				}
				System.out.println("클라이언트" + myId + " 처리 종료");
			}

		}
	}

}
