package 채팅서버;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Clinet extends JFrame implements ActionListener{
	
	//네트워크를 위한 자원 변수
	private Socket socket;
	private String ip ; //127.0.0.1
	private int port;
	private String id;
	
	private InputStream in;
	private OutputStream out;
	private DataInputStream din;
	private DataOutputStream dout;
	
	
	
	//Login  GUI 변수이다.
	private JFrame frame;
	
	private JFrame Main_GUI = new JFrame();
	private JFrame Login_GUI = new JFrame();
	private JTextField IpText;
	private JTextField PortText;
	private JTextField IdText;
	private JButton loginBtn = new JButton("접 속");
	
	
	//Main GUI 변수이다.
	private JPanel contentPane;
	private JTextField mainText;
	private JLabel lblNewLabel = new JLabel("쪽지 보내기");
	
	
	private JButton InputRoomBtn = new JButton("채팅방 참여");
	private JButton MakeRoomBtn = new JButton("방 만들기");
	private JButton SentBtn = new JButton("메세지 보내기");
	private JButton MessgeBtn = new JButton("쪽지 보내기");
	
	private JTextArea ChatTextArea = new JTextArea();
	private JList UserList = new JList();
	private JList RoomList = new JList();
	
	
	//그외 변수들
	Vector user_list = new Vector();
	Vector room_list = new Vector();
	StringTokenizer st ;
	private String My_Room;// 내가 현재 접속중인 방이름
	
	
	Clinet(){
		Login();//로그인창 화면 구성 메소드이다.
		main_init();
		
		start();
	}
	
	private  void start() {
		loginBtn.addActionListener(this);
		InputRoomBtn.addActionListener(this);
		MakeRoomBtn.addActionListener(this);
		SentBtn.addActionListener(this);
		MessgeBtn.addActionListener(this);
	}
	
	private void main_init() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 509, 562);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		lblNewLabel.setFont(new Font("굴림", Font.BOLD, 14));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(22, 10, 104, 24);
		contentPane.add(lblNewLabel);
		
		
		UserList.setBounds(22, 46, 104, 107);
		contentPane.add(UserList);
		
		
		MessgeBtn.setBounds(22, 175, 104, 23);
		contentPane.add(MessgeBtn);
		
		JLabel lblNewLabel_1 = new JLabel("\uCC44 \uD305 \uBC29 \uBAA9\uB85D");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 14));
		lblNewLabel_1.setBounds(22, 224, 104, 24);
		contentPane.add(lblNewLabel_1);
		
		
		RoomList.setBounds(22, 255, 104, 148);
		contentPane.add(RoomList);
		
		
		InputRoomBtn.setBounds(22, 413, 97, 23);
		contentPane.add(InputRoomBtn);
		
		
		MakeRoomBtn.setBounds(22, 446, 97, 23);
		contentPane.add(MakeRoomBtn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(170, 42, 272, 361);
		contentPane.add(scrollPane);
		
		
		scrollPane.setViewportView(ChatTextArea);
		
		mainText = new JTextField();
		mainText.setBounds(168, 433, 197, 21);
		contentPane.add(mainText);
		mainText.setColumns(10);
		
		
		
		SentBtn.setBounds(365, 432, 97, 23);
		contentPane.add(SentBtn);
		
		this.setVisible(true);
		
	}
	
	
	
	private void Network() {
		
		try {
			socket= new Socket(ip,port);
			
			if(socket != null) { // 정상적으로 소켓이 연결이 되었을 때
				Connection();
			}
			
			
		}catch(UnknownHostException e) {
			e.printStackTrace();
		}catch(Exception e2) {
			e2.printStackTrace();
		}
	}
	
	private void Connection() {// 실제 연결부분
		
		
		try {
			
			in = socket.getInputStream();
			din = new DataInputStream(in);	
			out = socket.getOutputStream();
			dout = new DataOutputStream(out);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//Stream 설정 끝
		
		//처음 접속시에 ID 전송
		Send_Message(id);
		
		user_list.add(id);
		UserList.setListData(user_list);
		
		
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
			
				
				while(true) {// 서버로부터 계속해서 메세지를 받기 위한 무한 반복문이다.
					String msg;
					
					try {// 메세지 수신
					
						msg = din.readUTF();
					
						inmessage(msg);
						
					}catch (IOException e) {
						
					}
					
				}
				
			}
			
		});
		th.start();
	}
	
	private void inmessage(String str) {//서버로부터 들어오는 모든 메세지
		
		st = new StringTokenizer(str,"/");
		
		String protocol = st.nextToken();
		String Message = st.nextToken();
		
		
		
		
		if(protocol.equals("NewUser")) {//새로운 유저에 대한 리스트 추가
			user_list.add(Message);
			UserList.setListData(user_list);
			
		}
		
		// 이지 존재한 사람
		else if(protocol.equals("OldUser")) {
			System.out.println("프로토콜  :"+protocol);
			System.out.println("Message :"+Message);
			
			user_list.add(Message);
			UserList.setListData(user_list);
			
		}
		
		else if(protocol.equals("CreateRoom" )) {
			My_Room=Message;
		}
		
		else if(protocol.equals(("CreateRoomFail"))) {
			JOptionPane.showMessageDialog(null, "방만들기 실패","알림",JOptionPane.ERROR_MESSAGE	);
		}
		
		else if(protocol.equals("NewRoom")) {//새로운 방이 만들어졌을때
			room_list.add(Message);
			RoomList.setListData(room_list);
		}
		else if(protocol.equals("Chatting")) {
			String msg = st.nextToken();
			ChatTextArea.append(Message+": "+msg+"\n");
			
			
		}
		else if(protocol.equals("OldRoom")) {
			room_list.add(Message);
			RoomList.setListData(room_list);
		}
		
		else if(protocol.equals("JoinRoom")) {
			JOptionPane.showMessageDialog(null, "채팅방에 입장했습니다.","알림",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(protocol.equals("user_list_update")) {
			System.out.println("유저 목록 업데이트가 되었습니다.");
			RoomList.setListData(room_list);
		}
		
	}
	
	private void Send_Message(String str) {// 서버에게 메세지를 보내는 부분
		
		try {
			dout.writeUTF(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void Login() {
		
		Login_GUI.setBounds(100, 100, 316, 357);
		Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Login_GUI.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server IP");
		lblNewLabel.setBounds(12, 91, 57, 15);
		Login_GUI.getContentPane().add(lblNewLabel);
		
		JLabel lblServerPort = new JLabel("Server Port");
		lblServerPort.setBounds(12, 137, 94, 15);
		Login_GUI.getContentPane().add(lblServerPort);
		
		JLabel lblId = new JLabel("ID");
		lblId.setBounds(12, 184, 57, 15);
		Login_GUI.getContentPane().add(lblId);
		
		IpText = new JTextField();
		IpText.setBounds(116, 88, 116, 21);
		Login_GUI.getContentPane().add(IpText);
		IpText.setColumns(10);
		
		
		PortText = new JTextField();
		PortText.setColumns(10);
		PortText.setBounds(116, 134, 116, 21);
		Login_GUI.getContentPane().add(PortText);
		
		IdText = new JTextField();
		IdText.setColumns(10);
		IdText.setBounds(116, 181, 116, 21);
		Login_GUI.getContentPane().add(IdText);
		
		
		loginBtn.setBounds(38, 249, 194, 23);
		Login_GUI.getContentPane().add(loginBtn);
		Login_GUI.setVisible(true);
		
	}
	
	
	public static void main(String[] args) {
		new Clinet();
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == loginBtn) {
		
			
			ip=IpText.getText().trim();
			port = Integer.parseInt(PortText.getText().trim());
			id = IdText.getText();
			Network();
			Send_Message("NewOld/"+ip);
		}
		
		
		
		else if(e.getSource() == InputRoomBtn) {
			String joinRoom = (String)RoomList.getSelectedValue();
			Send_Message("JoinRoom/"+joinRoom);
		}
		
		
		
		else if(e.getSource() == MakeRoomBtn) {// 유저가 방을 만든다고 한다면
			String roomname = JOptionPane.showInputDialog("방 이름");
			if(roomname != null) {
				Send_Message("CreateRoom/"+roomname);
			}
			
		}
		
		
		else if(e.getSource() == SentBtn) {
		
			Send_Message("Chatting/"+My_Room+"/"+mainText.getText());
			mainText.setText("");
			
			//현재 채팅방
			
		}
		
		else if(e.getSource() == MessgeBtn) {
		
			
			String user = (String)UserList.getSelectedValue();
			String note = JOptionPane.showInputDialog("보낼 메세지");
			
			if(note != null) {
				Send_Message("Note/"+user+"/"+note);
			}
			
		}
		
	}

	

}
