# HalliGalli

> 4명의 플레이어로 하는 할리갈리 게임

### 프로그램 실행
1.  HGServer를  실행하고 MainFrmae(최대 16개)을  실행한다.
2.  서버의 IP와  이름을  입력하고 CONNECT버튼을  클릭한다.
3.  방만들기  버튼을  클릭하고  방  이름을  입력한  후  확이  버튼을  누른다. (이미  방이  생성된  경우에는  입장을  클릭하거나  방을  클릭해  게임방  안으로  들어갈  수  있다.)
4.  레디  버튼을  클릭해 4명이  모두  레디  상태가  되면  게임이  시작된다. (레디한  경우  나가기  버튼  비활성화)
5.  자신의  차례일  경우 TRUN버튼이  활성화된다. 아닐  경우는  카드에  그려진  과일이 5개가  되면 BELL버튼을  클릭한다.
6.  BELL버튼  실패  시  다른  플레이어들에게  카드를  한  장씩  준다. 성공  시  여태까지  테이블에  뒤집힌  모든  카드를  가져온다.
7.  남은  카드의  개수가 0개가  되면  해당  플레이어는  죽게  된다.
8.  남은  사람이 1명이  되면  남은  사람이  승리자가  된다.
9.  5초후  초기화가  되고  다시  레디  버튼을  눌러  새  게임을  시작하거나  나가기  버튼을  눌러  대기실로  이동한다.
10.  다른  방에서  게임을  진행하던가 X버튼을  눌러  프로그램을  종료한다.


### 이용 기술
* AWT(Abstract Windows Toolkit) & 스윙(Swing)
* Socket


## 시스템 구성
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

## 약속된 프로토콜
### [SERVER]
| 프로토콜 | 내용 |
|--|--|
| CONNECT | NAME |
| WCHAT | 대기실 채팅 |
| CREATE | 방 만들기 |
| ENTER | 입장버튼 |
| READY | 레디버튼 |
| TURN | TURN버튼 |
| BELL | BELL버튼 |
| CHAT | 게임방 채팅 |
| NOTI | 게임진행 공지 |
| END | 게임종료 |
| EXIT | 퇴장 |


### [CLIENT]

| 프로토콜 | 내용 |
|--|--|
| NO | NO |
| WNEW | 대기실 입장 |
| WCHAT | 대기실 채팅 |
| CREATE | 방생성 |
| ENTER | 방입장 |
| START | 게임시작 |
| NEW | 게임방 입장 |
| NOW | 자기 턴 |
| PRINT | 정보알림 표시 |
| REPAINT | 게임보드 업데이트 |
| CHAT | 게임방 채팅 |
| NOTI | 공지 |
| DIE | 죽음 |
| WIN | 승리 |
| INIT | 초기화|

## 기타
<img src="https://user-images.githubusercontent.com/33142199/98620630-fe89e700-2348-11eb-8787-fba95a43b93b.png" alt="통신" width="550px" height="200px">
<img src="https://user-images.githubusercontent.com/33142199/98620915-8a037800-2349-11eb-8b8a-6e6771438ceb.png" alt="전체그림" width="600px" height="350px">

시연 영상  https://youtu.be/BeUBzrD1Pag   
Manager class 참고 http://mudchobo.tistory.com/2
