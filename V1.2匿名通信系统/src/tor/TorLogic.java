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
			System.out.println("�������ӷ�����......");
			Socket ds = new Socket("10.3.1.201", 8800);// ����Ŀ¼���������ϴ�����ip����Կ��Ϣ��һ��Ҫ�����뷢����Ϣ��Ŀ��IP֮ǰ�ͽ�������
			String hostIP = InetAddress.getLocalHost().getHostAddress();// ��ȡ����ip��ַ
			String transferInfo = "";// Ҫ�������Ϣ
			transferInfo = "client" + "�ָ���";
			PrintStream outToDS = new PrintStream(ds.getOutputStream());
			outToDS.println(transferInfo);
			BufferedReader in1 = new BufferedReader(new InputStreamReader(ds.getInputStream()));
			int sum = 0;
			sum = Integer.parseInt(in1.readLine());// Ŀ¼�������ܵ�·�ɽڵ���Ŀ
			ds.close();
			System.out.println("Ŀ¼���������͹�����·�ɽڵ���Ϊ��" + sum);
			ServerSocket server = new ServerSocket(33456);
			while (true) {
				try {
					System.out.println("��ʼ����������");
					Socket socket = server.accept();
					System.out.println("������");
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
			System.out.println("����·�ɽڵ���Ϣ�ļ�������ϣ�");
			String ipInfo = null;
			// �ӱ������ѡ������·�ɼ�����Ϣ
			File f = new File("F:/ip");
			String[] files = f.list();// �����ļ���
			int max = files.length;// �����ļ��ĸ���
			StringBuffer sbIP = new StringBuffer();
			int randomArray[] = new GenerateRandomArray().randomArray(0, max - 1, 2);
			int count = 0;
			for (int i = 0; i < randomArray.length; i++) {
				int randomNum = randomArray[i];// ��i�������
				String[] strs = files[randomNum].split(";");
				String getIP = strs[0];// ���IP��ַ
				if (!getIP.equals(hostIP) && !getIP.equals(destinationIP)) {// ��ȡ��·�ɽڵ��IP�Ȳ����ڿͻ���IP���ֲ����ڽ��ն�IP
					System.out.println(getIP);
					sbIP.append(getIP + ";");
					count++;
					if (count == 2) {
						break;
					}
				}

			}
			ipInfo = sbIP.toString();// �����ȡ������IP��ַ
			System.out.println("�ӱ��ػ�ȡ��������IPΪ��" + ipInfo);

			String ips[] = null;
			ips = ipInfo.split(";");// ����·��IP
			String information = "";
			information = message + "�ָ���" + destinationIP + "�ָ���" + ips[1];
			System.out.println("OP���͸���ڽڵ����ϢΪ��" + information);
			client = new Socket(ips[0], 8000); // ����
			System.out.println("�ɹ����ӵ�����������ʼͨѶ����");
			PrintStream out = new PrintStream(client.getOutputStream());
			out.println(information); // ����
			BufferedReader in2 = new BufferedReader(new InputStreamReader(client.getInputStream()));
			// �ж��Ƿ��������Ͽ�
			if (isConnected()) {
				System.out.println("���ͳɹ�����");
				System.out.println("��������" + in2.readLine());
			} else {
				System.out.println("����ʧ�ܣ���");
				System.out.println("��������Ͽ����ӣ���");
			}
			client.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// ����ļ����������ļ�
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
			delAllFiles(path);//����ļ����е������ļ�
			(new File(path)).mkdirs();// ����ļ��в����ڣ��������ļ���
			din = new DataInputStream(socket.getInputStream());
			File file = new File(path + File.separator + din.readUTF());
			if (!file.exists()) { // �ļ��������򴴽��ļ����ȴ���Ŀ¼
				file.createNewFile();
			}
			fout = new FileOutputStream(file);
			inputByte = new byte[1024];
			System.out.println("��ʼ��������...");
			while (true) {
				if (din != null) {
					length = din.read(inputByte, 0, inputByte.length);
				}
				if (length == -1) {
					break;
				}
				System.out.println("�ļ�����Ϊ��" + length);
				fout.write(inputByte, 0, length);
				fout.flush();
			}
			count++;
			System.out.println("��" + count + "���ļ���ȡ����");
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
	 * ��Ϊ�������˵ķ���
	 */
	public void server(UI ui) {
		boolean flag = true;
		try {
			server = new ServerSocket(8000); // �˿�
			System.out.println("�ͻ������ڵȴ�������Ϣ......");
			while (flag) {
				client = server.accept(); // ����
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
			System.out.println("������ͻ���(" + clientThread.getInetAddress().getHostAddress() + ")�ɹ��������ӣ���ʼͨѶ����");
		}

		public void run() {
			/*
			 * ���տͻ��˷��͵�����
			 */
			String message = null;
			try {
				/*
				 * ��ʱ�жϣ����ó�ʱʱ��300�룬�ж����ӻ���300���ڿͻ����޻�Ӧ��Ϣ����Ϊ�ж�
				 * ȱ����Ǳ���ѳ�ʱʱ�����úܳ�������ͻ��˴���Ҳ��Ϊ��ʱ�����޷��ж��Ƿ�˿�
				 */
				clientThread.setSoTimeout(300000);
				in = new BufferedReader(new InputStreamReader(clientThread.getInputStream()));
				message = in.readLine(); // ���� //��ÿͻ��˷��͵���Ϣ
				message = message.split("�ָ���")[0];
				System.out.println("�ͻ���(" + clientThread.getInetAddress().getHostAddress() + ")˵��" + message);

				ui.getJtextArea().setText(message);// ���ı�����ʾ���յ���Ϣ

				clientThread.close();
			} catch (Exception e) {
				// ����쳣�������쳣����������ӶϿ�
				if (e.getMessage() == "Connection reset") {
					System.out.println("�ͻ���(" + clientThread.getInetAddress().getHostAddress() + ")�ѶϿ����ӣ���");
				}
			}
		}
	}

	// �жϷ������Ƿ�Ͽ��ķ�����
	// ͨ��OutputStream����һ�β������ݣ��������ʧ�ܾͱ�ʾԶ���Ѿ��Ͽ�����
	// �����������Ĵ��͸��ţ�������sendUrgenData
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