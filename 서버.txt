package 채팅서버;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

//ActionListener
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;//ActionListener

public class Server extends JFrame implements ActionListener {
	
	private JPanel contentPane;
	private JTextField textField;
	JTextArea textArea = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane();
	JButton Start_Btn = new JButton("서버 실행");
	JButton End_Btn = new JButton("서버 중지");
	
	
	//Network 자원
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc =new Vector();
	private Vector room_vc = new Vector();
	private StringTokenizer st;
	
	Server(){// 생성자
		
		init();// 화면 생성 메소드
		start();
	}
	
	private void start() {
		Start_Btn.addActionListener(this); 
		End_Btn.addActionListener(this);
	}
	
	
	
	private void init() {//화면 구성
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 440, 420);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(22, 10, 382, 274);
		contentPane.add(scrollPane);
		
		
		scrollPane.setViewportView(textArea);
		
		JLabel PortLa = new JLabel("포트번호");
		PortLa.setBounds(22, 315, 67, 15);
		contentPane.add(PortLa);
		
		textField = new JTextField();
		textField.setBounds(86, 312, 318, 21);
		contentPane.add(textField);
		textField.setColumns(10);
		
		
		Start_Btn.setBounds(22, 343, 179, 23);
		contentPane.add(Start_Btn);
		Start_Btn.addActionListener(null);
		
		End_Btn.setBounds(239, 343, 165, 23);
		contentPane.add(End_Btn);
		
		this.setVisible(true);
		
	}
	
	
	private void Server_start() {
		try {
			server_socket = new ServerSocket(port);
	

		}catch(IOException e){
			
		}
		
		if(server_socket != null)// 정상적으로 포트가 열렸을 경우
		{
			Connection();
		}
		
	}
	
	private void Connection() {
		//1가지의 스레드에서 1가지의 일만 처리할 수 있다.
		
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(true) {
				
				try {
					
					textArea.append("사용자 접속 대기중\n");
					socket = server_socket.accept();//사용자 접속 대기 무한대기
					textArea.append("사용자 접속!!!\n");
					
					UserInfo user = new UserInfo(socket);
					user.start();//상속 받은 쓰레드이기때문에 사용간으한 쓰레드 함수이다.ㅣ
					
					
					
				} catch (IOException e) {	
					e.printStackTrace();
				}
				
			}//while문 끝
				
			}
		});
		
		th.start();

	}
	
	
	
	
	public static void main(String[] args) {
		new Server();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource() == Start_Btn) {
			
			System.out.println("서버 시작");
			port = Integer.parseInt(textField.getText());
			Server_start();
			
		}
		else if(e.getSource() == End_Btn) {
			
			System.out.println("서버 종료");
			
		}
		
	}//액션 이벤트 끝
	
	class UserInfo extends Thread{
		
		private OutputStream out;
		private DataOutputStream dout;
		private InputStream in;
		private DataInputStream din;
		
		private Socket user_socket;
		private String Nickname = "";
		
		private boolean RoomCh =true;
		
		UserInfo(Socket socket){//생성자 매소드
			this.user_socket = socket;
			
			UserNetWork();
			
			
		}
		
		private void UserNetWork() {
			
			try {
				
				in = user_socket.getInputStream();
				din = new DataInputStream(in);
				out = user_socket.getOutputStream();
				dout = new DataOutputStream(out);
				
				Nickname = din.readUTF();
				textArea.append(Nickname+" : 사용자 접속!\n");
				
				BroadCast("NewUser/"+Nickname); //현재 접속된 유저정보를 접속중인 유저들에게 정보를 전달
				
				for(int i = 0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);
					System.out.println("OldUser/"+u.Nickname);
					send_Message("OldUser/"+u.Nickname); //새로운
				}	
				
				//현재 방목록을 새로운 유저에게 알려준다.
				for(int i = 0; i<room_vc.size(); i++) {
					RoomInfo  r = (RoomInfo)room_vc.elementAt(i);
					send_Message("OldRoom/"+r.Room_name);
					
				}
				
				BroadCast("user_list_update/ok");
				
				
				
				user_vc.add(this);// 사용자에게 접속을 알리고 자기 자신을 백터에 집어 넣는다.
						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	
		
	public void run() {// 클라이언트로부터 받은 매세지를 개별적으로
			
		while(true) {
				
			try {
				String msg = din.readUTF();
				textArea.append(Nickname+"사용자로부터 들어온 메세지"+msg+"\n");
				InMessage(msg);
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}//run 끝
	
	private void InMessage(String str) {//클라이언트로부터 들어오는 메세지 처리
		st = new StringTokenizer(str,"/");
		String protocol = st.nextToken();
		String message = st.nextToken();
		
		System.out.println("프로토콜: "+protocol);
		System.out.println("메세지"+ message);
		
		if(protocol.equals("NOTE")) {
			System.out.println(message);
		}
		
		
		if(protocol.equals("CreateRoom")) {// 온메세지가 메세지이면
			
			//1.현재 같은 방이 존재하는지 확인한다. 채팅방
			for(int i= 0; i<room_vc.size(); i++) {
				RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				if(r.Room_name.equals(message)) {//만들고자 하는 방이름이 존재 했을 때 사용자에게 알려서 못만들게 한다.
					send_Message("CreateRoomFail/ok");
					RoomCh = false;
					break;
				}else {
					
				}
			}// for문 끝
			
			if(RoomCh) {
				
				RoomInfo new_room = new RoomInfo(message,this);
				room_vc.add(new_room);//전체 방 백터에 방을 추가하고 사용자들에게 알려준다.
				send_Message("CreateRoom/"+message);
				
				BroadCast("NewRoom/"+message);
			}
			
			RoomCh =true;
		}
		
		
		
		else if(protocol.equals("Chatting")) {// 채팅 프로토콜
			String msg = st.nextToken();
			
			for(int i =0; i< room_vc.size(); i++) {
				RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				
				if(r.Room_name.equals(message)) {// 해당 방을 찾았을 때
					
					r.BroadCast_Room("Chatting/"+Nickname+"/"+msg);
				}
			}
		}
		
		else if(protocol.equals("JoinRoom")) {
			for(int i=0; i<room_vc.size(); i++) {
				RoomInfo r = (RoomInfo)room_vc.elementAt(i);
				if(r.Room_name.equals(message)) {
					//방에 사용자를 추가 시켜준다.
					r.Add_User(this);
					send_Message("JoinRoom/ok");
					
				}
			}
		}
		
		
	}
	
	
	
	
	
		
		private void BroadCast(String str) {
			for(int i=0; i<user_vc.size(); i++) {
				UserInfo u = (UserInfo)user_vc.elementAt(i);
				u.send_Message(str);
			}
			
		}
		
		
		private void send_Message(String str) {
			try {
				dout.writeUTF(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	class RoomInfo{
		private String Room_name;
		private Vector Room_user_vc = new Vector();
		
		RoomInfo(String str, UserInfo u){
			this.Room_name = str; // 방이름
			this.Room_user_vc.add(u);// 유저 추가
			
		}
		
		public void BroadCast_Room(String str){// 현재 방의 모든 사람에게 알린다.
			for(int i =0; i<Room_user_vc.size(); i++) {
				UserInfo u = (UserInfo) Room_user_vc.elementAt(i);
				u.send_Message(str);
				
			}
		}
		
		
		private void Add_User(UserInfo u) {
			this.Room_user_vc.add(u);
		}
		
	}//RoomInfo 클래스 마지막

}
