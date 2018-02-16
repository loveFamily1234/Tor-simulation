package tor;

import util.GenerateRandomArray;
import java.net.*;
import java.io.*;

public class TorLogic {

	private Socket client;
	private ServerSocket server;
	static int count = 0;

	public void client(String message, String destinationIP) {
		try {
			System.out.println("正在连接服务器......");
			Socket ds = new Socket("10.3.1.201", 8800);// 连接目录服务器，上传本机ip和密钥信息。一定要在输入发送消息和目的IP之前就建立连接
			String hostIP = InetAddress.getLocalHost().getHostAddress();// 获取本地ip地址
			String transferInfo = "";// 要传输的信息
			transferInfo = "client" + "分隔符";
			PrintStream outToDS = new PrintStream(ds.getOutputStream());
			outToDS.println(transferInfo);
			BufferedReader in1 = new BufferedReader(new InputStreamReader(ds.getInputStream()));
			int sum = 0;
			sum = Integer.parseInt(in1.readLine());// 目录服务器总的路由节点数目
			ds.close();
			System.out.println("目录服务器发送过来的路由节点数为：" + sum);
			ServerSocket server = new ServerSocket(33456);
			while (true) {
				try {
					System.out.println("开始监听。。。");
					Socket socket = server.accept();
					System.out.println("有链接");
					receiveFile(socket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (count == sum) {
					count = 0;
					break;

				}
			}
			server.close();
			System.out.println("所有路由节点信息文件下载完毕！");
			String ipInfo = null;
			// 从本地随机选择三个路由几点信息
			File f = new File("F:/ip");
			String[] files = f.list();// 所有文件名
			int max = files.length;// 所有文件的个数
			StringBuffer sbIP = new StringBuffer();
			int randomArray[] = new GenerateRandomArray().randomArray(0, max - 1, 2);
			int count = 0;
			for (int i = 0; i < randomArray.length; i++) {
				int randomNum = randomArray[i];// 第i个随机数
				String[] strs = files[randomNum].split(";");
				String getIP = strs[0];// 获得IP地址
				if (!getIP.equals(hostIP) && !getIP.equals(destinationIP)) {// 获取的路由节点的IP既不等于客户端IP，又不等于接收端IP
					System.out.println(getIP);
					sbIP.append(getIP + ";");
					count++;
					if (count == 2) {
						break;
					}
				}

			}
			ipInfo = sbIP.toString();// 随机获取的三个IP地址
			System.out.println("从本地获取到的两个IP为：" + ipInfo);

			String ips[] = null;
			ips = ipInfo.split(";");// 三个路由IP
			String information = "";
			information = message + "分隔符" + destinationIP + "分隔符" + ips[1];
			System.out.println("OP发送给入口节点的消息为：" + information);
			client = new Socket(ips[0], 8000); // 阻塞
			System.out.println("成功连接到服务器，开始通讯！！");
			PrintStream out = new PrintStream(client.getOutputStream());
			out.println(information); // 阻塞
			BufferedReader in2 = new BufferedReader(new InputStreamReader(client.getInputStream()));
			// 判断是否与主机断开
			if (isConnected()) {
				System.out.println("发送成功！！");
				System.out.println("服务器：" + in2.readLine());
			} else {
				System.out.println("发送失败！！");
				System.out.println("与服务器断开连接！！");
			}
			client.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// 清空文件夹里所有文件
	public  boolean delAllFiles(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
		}
		return true;
	}

	public void receiveFile(Socket socket) {
		byte[] inputByte = null;
		int length = 0;
		DataInputStream din = null;
		FileOutputStream fout = null;
		try {
			String path = "F:\\ip";
			delAllFiles(path);//清空文件夹中的所有文件
			(new File(path)).mkdirs();// 如果文件夹不存在，则建立新文件夹
			din = new DataInputStream(socket.getInputStream());
			File file = new File(path + File.separator + din.readUTF());
			if (!file.exists()) { // 文件不存在则创建文件，先创建目录
				file.createNewFile();
			}
			fout = new FileOutputStream(file);
			inputByte = new byte[1024];
			System.out.println("开始接收数据...");
			while (true) {
				if (din != null) {
					length = din.read(inputByte, 0, inputByte.length);
				}
				if (length == -1) {
					break;
				}
				System.out.println("文件长度为：" + length);
				fout.write(inputByte, 0, length);
				fout.flush();
			}
			count++;
			System.out.println("第" + count + "个文件读取结束");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/*
	 * 作为服务器端的方法
	 */
	public void server(UI ui) {
		boolean flag = true;
		try {
			server = new ServerSocket(8000); // 端口
			System.out.println("客户端正在等待接收消息......");
			while (flag) {
				client = server.accept(); // 阻塞
				new ServerThread(client, ui).start();
			}
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ServerThread extends Thread {
		private Socket clientThread;
		private BufferedReader in;
		private UI ui;

		public ServerThread(Socket client, UI ui2) {
			clientThread = client;
			ui = ui2;
			System.out.println("主机与客户端(" + clientThread.getInetAddress().getHostAddress() + ")成功建立连接，开始通讯！！");
		}

		public void run() {
			/*
			 * 接收客户端发送的数据
			 */
			String message = null;
			try {
				/*
				 * 超时判断：设置超时时间300秒，中断连接或者300秒内客户端无回应信息则认为中断
				 * 缺点便是必须把超时时间设置很长，否则客户端待机也认为超时处理，无法判断是否端口
				 */
				clientThread.setSoTimeout(300000);
				in = new BufferedReader(new InputStreamReader(clientThread.getInputStream()));
				message = in.readLine(); // 阻塞 //获得客户端发送的消息
				message = message.split("分隔符")[0];
				System.out.println("客户端(" + clientThread.getInetAddress().getHostAddress() + ")说：" + message);

				ui.getJtextArea().setText(message);// 在文本区显示接收的信息

				clientThread.close();
			} catch (Exception e) {
				// 如果异常是连接异常，则输出连接断开
				if (e.getMessage() == "Connection reset") {
					System.out.println("客户端(" + clientThread.getInetAddress().getHostAddress() + ")已断开连接！！");
				}
			}
		}
	}

	// 判断服务器是否断开的方法，
	// 通过OutputStream发送一段测试数据，如果发送失败就表示远端已经断开连接
	// 但会与正常的传送干扰，所以用sendUrgenData
	public boolean isConnected() {
		try {
			client.sendUrgentData(0xFF);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}