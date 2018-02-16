package server;
import java.net.*;  
import java.io.*;  
   
public class RelayServer {  
   
    private ServerSocket server;  
    private Socket client;  
   
    public RelayServer() {  
        boolean flag = true;  
        try {
        	/*
        	 * ��Ŀ¼������ע����Ϣ
        	 */
        	Socket ds = new Socket("10.3.1.201", 8800);// ����Ŀ¼���������ϴ�����ip����Կ��Ϣ��
        	ds.setSoTimeout(50000);
			System.out.println("��������Ŀ¼������......");
			String hostIP = InetAddress.getLocalHost().getHostAddress();// ��ȡ����ip��ַ
			String transferInfo = "";// Ҫ�������Ϣ
			transferInfo = hostIP + "�ָ���" ;
			PrintStream outToDS = new PrintStream(ds.getOutputStream());
			outToDS.println(transferInfo);  
			ds.close();
			
            server = new ServerSocket(8000);    //�˿�  
            System.out.println("�м̷��������ڵȴ��ͻ�������......");  
            while (flag) {  
                client = server.accept();   //����  
                new ServerThread(client).start();  
            }  
            server.close();  
        } catch (Exception e) {  
        }  
    }  
   
    public static void main(String[] args) {  
        new RelayServer();  
    }  
   
    private class ServerThread extends Thread {  
   
        private Socket clientThread; 
        private Socket server;
        private PrintStream out;  
        private BufferedReader in;  
   
        public ServerThread(Socket client) {  
            this.clientThread = client;  
            System.out.println("�м̷�������ͻ���("+clientThread.getInetAddress().getHostAddress()+")�ɹ��������ӣ���ʼͨѶ����");  
        }  
   
        public void run() {  
        	/*
        	 * ���տͻ��˷��͵�����
        	 */
        	String in_info = null;
        	String out_info = null;
            try {  
                /* 
                 * ��ʱ�жϣ����ó�ʱʱ��300�룬�ж����ӻ���300���ڿͻ����޻�Ӧ��Ϣ����Ϊ�ж� 
                 * ȱ����Ǳ���ѳ�ʱʱ�����úܳ�������ͻ��˴���Ҳ��Ϊ��ʱ�����޷��ж��Ƿ�˿� 
                 */  
                clientThread.setSoTimeout(300000);  
                out = new PrintStream(clientThread.getOutputStream());  
                in = new BufferedReader(new InputStreamReader(clientThread.getInputStream()));  
                in_info = in.readLine(); //����  
                String[] in_s = in_info.split("�ָ���");
                StringBuffer sb = new StringBuffer();
                	for(int i=0;i<in_s.length-1;i++){
                		sb.append(in_s[i]+"�ָ���");
                	}
                	out_info = sb.toString();
                    System.out.println("�ͻ���("+clientThread.getInetAddress().getHostAddress()+")˵��" + in_info);  
                    out.println("�м̷������ɹ����յ�������Ϣ����");
                    
                    /*
                     * ת���ͻ��˷��͵�����
                     */
                    try {  
                        System.out.println("����������һ��������......");  
                        server = new Socket(in_s[in_s.length-1], 8000);    //����  
                        server.setSoTimeout(50000);
                        PrintStream out2 = new PrintStream(server.getOutputStream());
                        System.out.println("�ɹ����ӵ���һ������������ʼͨѶ����");  
                        out2.println(out_info);  //����  
                        //�ж��Ƿ��������Ͽ�  
                        if (isConnected()) {  
                            System.out.println("ת���ɹ�����");   
                        } else {  
                            System.out.println("ת��ʧ�ܣ���");  
                            System.out.println("��������Ͽ����ӣ���");  
//                            server.close();  
                        }  
                      server.close();
                    } catch (Exception e) {  
                        System.out.println(e.getMessage());  
                    }
                client.close();  
            } catch (Exception e) {  
                //����쳣�������쳣����������ӶϿ�  
                if (e.getMessage() == "Connection reset") {  
                    System.out.println("�ͻ���("+clientThread.getInetAddress().getHostAddress()+")�ѶϿ����ӣ���");  
                }  
            }  
        } 
        //�жϷ������Ƿ�Ͽ��ķ�����  
        //ͨ��OutputStream����һ�β������ݣ��������ʧ�ܾͱ�ʾԶ���Ѿ��Ͽ�����  
        //�����������Ĵ��͸��ţ�������sendUrgenData  
        public boolean isConnected() {  
            try {  
                server.sendUrgentData(0xFF);  
                return true;  
            } catch (Exception e) {  
                e.printStackTrace();  
                return false;  
            }  
        }  
    }  
}  