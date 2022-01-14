## HalliGalli

<img src="https://img.shields.io/badge/Java-red" /> <img src="https://img.shields.io/badge/Socket-lightgrey" /> <img src="https://img.shields.io/badge/AWT-yellow" /> <img src="https://img.shields.io/badge/Swing-brightgreen" />

> 4인 할리갈리 게임

할리갈리는 규칙이 간단하고 친구들과 종종 할 만큼 재미있어 프로젝트 주제로 선택하였습니다.  
강의 교재인 [어서와 Java는 처음이지!]에서 
- Card, Deck, Player 클래스를 사용한 카드 게임 예시
- 처음이나 끝 데이터의 삽입이나 삭제에서 성능이 좋다는 LinkedList
- Socket을 이용한 Server=Client 연결하는 Tic-Tac-Toe 네트워크 게임 예시  

에서 필요한 내용을 조합하여 할리갈리를 만들었습니다.

## 프로젝트 설치 & 실행

1. git clone https://github.com/Jungea/HalliGalli.git
2. 이클립스 실행
3. File > import > General > Existing Projects into Workspace
4. [서버 실행]
src/halligalli/HGServer.java 파일 우클릭 > Run As > Java Application
5. [클라이언트 실행]
src/halligalli/MainFrame.java 파일 우클릭 > Runn As > Java Application
6. [서버-클라이언트 연결]
서버 IP와 이름 입력 후 CONNECT 버튼 클릭

이후 [게임진행](https://github.com/Jungea/HalliGalli/wiki/%EA%B2%8C%EC%9E%84-%EC%A7%84%ED%96%89)은 위키 참고

## 구조
<img src="https://user-images.githubusercontent.com/33142199/149461081-72cb4c0f-2765-4189-a634-2e5653aeefbe.jpg" alt="전체그림" width="600px" height="350px">

자세한 [클래스설명](https://github.com/Jungea/HalliGalli/wiki/%ED%81%B4%EB%9E%98%EC%8A%A4-%EC%84%A4%EB%AA%85)은 위키 참고

## Socket(Server-Client)
<img src="https://user-images.githubusercontent.com/33142199/149462208-de4dac5f-a296-400b-aab9-5e25ade087bd.png" alt="server-client" width="400px" height="200px">

[Server]
```
ServerSocket server = new ServerSocket(port);  //연결 요청 전용 소켓 생성
Socket serverSocket = server.accept();  //클라이언트와 연결되면 새로운 Socket 객체 생성 & 반환
```
[Client]
```
Socket clientSocket = new Socket(ip, port);
```
[공통]
```
BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));;
PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
```
<img src="https://user-images.githubusercontent.com/33142199/98620630-fe89e700-2348-11eb-8787-fba95a43b93b.png" alt="통신" width="550px" height="200px">

### 프로토콜
| SERVER | CLIENT |
|--|--|
| ![image](https://user-images.githubusercontent.com/33142199/149471938-7d953d5e-aa8e-4299-8f18-baac8ba81705.png) | ![image](https://user-images.githubusercontent.com/33142199/149472077-fed791c7-8a3f-4594-a48c-fa4be6610bcc.png) |

## 화면 구성
### [대기실]
<img src="https://user-images.githubusercontent.com/33142199/98503510-0b023700-2298-11eb-9dc4-49ee36029483.jpg" alt="대기실" width="500px" height="350px">

 1. 게임방 목록 : 방만들기 버튼으로 생성된 게임방 목록(4개의 방이 보이며 위 아래 버튼으로 목록 이동이 가능하다.), 게임방 버튼을 누를 경우 해당 게임방으로 화면이 바뀐다.
 2. 연결된 플레이어 목록
 3. 방만들기 : 팝업창이 뜨로 방이름을 입력하여 확인 버튼을 누르면 게임방이 생성된다.   
입장 : 자리가 있는 방에 자동으로 입장되고, 게임방으로 화면이 바뀐다.
 4. 채팅 : 대기실에서 채팅한 내용이 표시된다.
 5. 연결 : 서버의 IP와 2~6자의 NAME을 입력하고 CONNECT버튼을 클릭 시 서버와 연결 된다.

### [게임방]
<img src="https://user-images.githubusercontent.com/33142199/98503668-74824580-2298-11eb-9817-7afc6775a376.jpg" alt="게임방" width="500px" height="350px">

 1. 정보알림창
 2. READY : 게임 준비가 완료된 경우   
나가기 : 게임을 대기중 일 경우 대기실로 화면이 전환된다.(단. 레디 버튼을 누른 경우 나가기 불가능)
 3. 해당 게임방 입장 인원 목록
 4. Turn : 카드 뒤집기, 자기 차례가 될 경우 활성화   
Bell : 카드에 그려진 같은 종류의 과일이 5개가 되었을 경우
 5. 채팅창 : 해당 게임방 
 6. 게임 진행 보드 : 플레이어의 NAME, 남은 카드 개수 뒤집힌 카드를 보여준다.

## 기타
:movie_camera: [시연영상](https://youtu.be/BeUBzrD1Pag)

Manager class 참고 http://mudchobo.tistory.com/2
