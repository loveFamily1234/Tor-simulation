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
        	 * 向目录服务器注册信息
        	 */
        	Socket ds = new Socket("10.3.1.201", 8800);// 连接目录服务器，上传本机ip和密钥信息。
        	ds.setSoTimeout(50000);
			System.out.println("正在连接目录服务器......");
			String hostIP = InetAddress.getLocalHost().getHostAddress();// 获取本地ip地址
			String transferInfo = "";// 要传输的信息
			transferInfo = hostIP + "分隔符" ;
			PrintStream outToDS = new PrintStream(ds.getOutputStream());
			outToDS.println(transferInfo);  
			ds.close();
			
            server = new ServerSocket(8000);    //端口  
            System.out.println("中继服务器正在等待客户端连接......");  
            while (flag) {  
                client = server.accept();   //阻塞  
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
            System.out.println("中继服务器与客户端("+clientThread.getInetAddress().getHostAddress()+")成功建立连接，开始通讯！！");  
        }  
   
        public void run() {  
        	/*
        	 * 接收客户端发送的数据
        	 */
        	String in_info = null;
        	String out_info = null;
            try {  
                /* 
                 * 超时判断：设置超时时间300秒，中断连接或者300秒内客户端无回应信息则认为中断 
                 * 缺点便是必须把超时时间设置很长，否则客户端待机也认为超时处理，无法判断是否端口 
                 */  
                clientThread.setSoTimeout(300000);  
                out = new PrintStream(clientThread.getOutputStream());  
                in = new BufferedReader(new InputStreamReader(clientThread.getInputStream()));  
                in_info = in.readLine(); //阻塞  
                String[] in_s = in_info.split("分隔符");
                StringBuffer sb = new StringBuffer();
                	for(int i=0;i<in_s.length-1;i++){
                		sb.append(in_s[i]+"分隔符");
                	}
                	out_info = sb.toString();
                    System.out.println("客户端("+clientThread.getInetAddress().getHostAddress()+")说：" + in_info);  
                    out.println("中继服务器成功接收到您的信息！！");
                    
                    /*
                     * 转发客户端发送的数据
                     */
                    try {  
                        System.out.println("正在连接下一个服务器......");  
                        server = new Socket(in_s[in_s.length-1], 8000);    //阻塞  
                        server.setSoTimeout(50000);
                        PrintStream out2 = new PrintStream(server.getOutputStream());
                        System.out.println("成功连接到下一个服务器，开始通讯！！");  
                        out2.println(out_info);  //阻塞  
                        //判断是否与主机断开  
                        if (isConnected()) {  
                            System.out.println("转发成功！！");   
                        } else {  
                            System.out.println("转发失败！！");  
                            System.out.println("与服务器断开连接！！");  
//                            server.close();  
                        }  
                      server.close();
                    } catch (Exception e) {  
                        System.out.println(e.getMessage());  
                    }
                client.close();  
            } catch (Exception e) {  
                //如果异常是连接异常，则输出连接断开  
                if (e.getMessage() == "Connection reset") {  
                    System.out.println("客户端("+clientThread.getInetAddress().getHostAddress()+")已断开连接！！");  
                }  
            }  
        } 
        //判断服务器是否断开的方法，  
        //通过OutputStream发送一段测试数据，如果发送失败就表示远端已经断开连接  
        //但会与正常的传送干扰，所以用sendUrgenData  
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