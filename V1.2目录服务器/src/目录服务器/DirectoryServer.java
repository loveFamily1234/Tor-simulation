package 目录服务器;

import util.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DirectoryServer {
	private ServerSocket server;
	private Socket client;
	private StringBuffer sb = new StringBuffer();

	public static void main(String[] args) {
		new DirectoryServer();
	}

	public DirectoryServer() {
		boolean flag = true;
		try {
			server = new ServerSocket(8800); // 端口
			System.out.println("目录服务器正在等待客户端连接......");
			while (flag) {
				client = server.accept(); // 阻塞
				new ServerThread(client).start();
			}
			server.close();
		} catch (Exception e) {
		}
	}

	private class ServerThread extends Thread {
		private Socket clientThread;
		private PrintStream out;

		public ServerThread(Socket client) {
			this.clientThread = client;
			System.out.println("目录服务器与客户端(" + clientThread.getInetAddress().getHostAddress() + ")成功建立连接，开始通讯！！");
		}

		/*
		 * 冒泡排序函数，升序
		 */
		public void sort(int[] arr) {
			for (int i = 0; i < arr.length - 1; i++) {
				for (int j = 0; j < arr.length - 1 - i; j++) {
					if (arr[j] > arr[j + 1]) {
						int temp;
						temp = arr[j];
						arr[j] = arr[j + 1];
						arr[j + 1] = temp;
					}
				}
			}
		}

		// 发送路由节点信息文件
		public void sendFiles() {
			int length = 0;
			Socket socket = null;
			byte[] sendByte = null;
			DataOutputStream dout = null;
			FileInputStream fin = null;
			try {
				String path = "C:\\ip";
				File files = new File(path);
				String[] files_Name = files.list();
				out = new PrintStream(clientThread.getOutputStream());
				int number = 2;// 此处从所有节点信息中任意选取m个文件传到客户端
				out.println(number);
				System.out.println("路由节点总数为：" + files_Name.length);
				File temp = null;
				int[] position = new GenerateRandomArray().randomArray(0, files_Name.length - 1, number);
				sort(position);// 冒泡排序，升序
				int count = 0;
				for (int i = 0; i < files_Name.length; i++) {
					if (i == position[count]) {
						if (path.endsWith(File.separator)) {
							temp = new File(path + files_Name[i]);
						} else {
							temp = new File(path + File.separator + files_Name[i]);
						}
						if (temp.isFile()) {
							fin = new FileInputStream(temp);
							socket = new Socket();
							socket.connect(new InetSocketAddress(clientThread.getInetAddress().getHostAddress(), 33456), 10 * 1000);
							dout = new DataOutputStream(socket.getOutputStream());
							sendByte = new byte[1024];
							dout.writeUTF(temp.getName());
							while ((length = fin.read(sendByte, 0, sendByte.length)) != -1) {
								dout.write(sendByte, 0, length);
								dout.flush();
							}
							socket.close();
						}
						if (count < position.length - 1) {
							count++;
						} else {
							break;// 结束循环
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("发送完毕......................");
		}

		public void run() {
			try {
				String ip = clientThread.getInetAddress().getHostAddress();
				sb.append(ip + ";");
				System.out.println(ip + "已上线！");
				System.out.println("此时所有上线IP：" + sb.toString());
				String receiveInfo = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(clientThread.getInputStream()));
				receiveInfo = in.readLine();
				String[] infos = receiveInfo.split("分隔符");
				String IP = infos[0];// 获取上线电脑的IP地址
				if (!IP.equals("client")) {//若此时是中继服务器
					// 写入文件
					File file;
					FileWriter writer;
					try {
						file = new File("C:/ip/" + IP + ";.txt");
						if (!file.exists()) { // 文件不存在则创建文件，先创建目录
							File dir = new File(file.getParent());
							dir.mkdirs();
							file.createNewFile();
						}
						/* 若文件已存在，则在写入信息前，需要清空文件内容 */
						writer = new FileWriter(file);
						writer.write("你好");// 此处应该写入密钥
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {// 若此时是客户端
					/*
					 * 此处有两种机制： 第一种：从目录服务器随机选择m个路由IP地址。
					 * 第二种：将目录服务器的所有路由IP信息都发送给客户端，由客户端随机产生三个路由IP。 但是，此时，网络延迟会加大。
					 */
					sendFiles();
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			InetAddress address = null;
			try {
				/*
				 * 超时判断：设置超时时间300秒，中断连接或者300秒内客户端无回应信息则认为中断
				 * 缺点便是必须把超时时间设置很长，否则客户端待机也认为超时处理，无法判断是否端口
				 */
				clientThread.setSoTimeout(300000);
				// out = new PrintStream(clientThread.getOutputStream());
				address = clientThread.getInetAddress();
				// out.println(ipInfo);//将所有IP地址传送给客户端请求者
				clientThread.close();
			} catch (Exception e) {
				// 如果异常是连接异常，则输出连接断开
				if (e.getMessage() == "Connection reset") {
					System.out.println("客户端(" + address.getHostAddress() + ")已断开连接！！");
				}
			}
		}
	}
}
