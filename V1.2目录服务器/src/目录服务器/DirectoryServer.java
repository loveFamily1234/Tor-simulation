package Ŀ¼������;

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
			server = new ServerSocket(8800); // �˿�
			System.out.println("Ŀ¼���������ڵȴ��ͻ�������......");
			while (flag) {
				client = server.accept(); // ����
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
			System.out.println("Ŀ¼��������ͻ���(" + clientThread.getInetAddress().getHostAddress() + ")�ɹ��������ӣ���ʼͨѶ����");
		}

		/*
		 * ð��������������
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

		// ����·�ɽڵ���Ϣ�ļ�
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
				int number = 2;// �˴������нڵ���Ϣ������ѡȡm���ļ������ͻ���
				out.println(number);
				System.out.println("·�ɽڵ�����Ϊ��" + files_Name.length);
				File temp = null;
				int[] position = new GenerateRandomArray().randomArray(0, files_Name.length - 1, number);
				sort(position);// ð����������
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
							break;// ����ѭ��
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("�������......................");
		}

		public void run() {
			try {
				String ip = clientThread.getInetAddress().getHostAddress();
				sb.append(ip + ";");
				System.out.println(ip + "�����ߣ�");
				System.out.println("��ʱ��������IP��" + sb.toString());
				String receiveInfo = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(clientThread.getInputStream()));
				receiveInfo = in.readLine();
				String[] infos = receiveInfo.split("�ָ���");
				String IP = infos[0];// ��ȡ���ߵ��Ե�IP��ַ
				if (!IP.equals("client")) {//����ʱ���м̷�����
					// д���ļ�
					File file;
					FileWriter writer;
					try {
						file = new File("C:/ip/" + IP + ";.txt");
						if (!file.exists()) { // �ļ��������򴴽��ļ����ȴ���Ŀ¼
							File dir = new File(file.getParent());
							dir.mkdirs();
							file.createNewFile();
						}
						/* ���ļ��Ѵ��ڣ�����д����Ϣǰ����Ҫ����ļ����� */
						writer = new FileWriter(file);
						writer.write("���");// �˴�Ӧ��д����Կ
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {// ����ʱ�ǿͻ���
					/*
					 * �˴������ֻ��ƣ� ��һ�֣���Ŀ¼���������ѡ��m��·��IP��ַ��
					 * �ڶ��֣���Ŀ¼������������·��IP��Ϣ�����͸��ͻ��ˣ��ɿͻ��������������·��IP�� ���ǣ���ʱ�������ӳٻ�Ӵ�
					 */
					sendFiles();
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			InetAddress address = null;
			try {
				/*
				 * ��ʱ�жϣ����ó�ʱʱ��300�룬�ж����ӻ���300���ڿͻ����޻�Ӧ��Ϣ����Ϊ�ж�
				 * ȱ����Ǳ���ѳ�ʱʱ�����úܳ�������ͻ��˴���Ҳ��Ϊ��ʱ�����޷��ж��Ƿ�˿�
				 */
				clientThread.setSoTimeout(300000);
				// out = new PrintStream(clientThread.getOutputStream());
				address = clientThread.getInetAddress();
				// out.println(ipInfo);//������IP��ַ���͸��ͻ���������
				clientThread.close();
			} catch (Exception e) {
				// ����쳣�������쳣����������ӶϿ�
				if (e.getMessage() == "Connection reset") {
					System.out.println("�ͻ���(" + address.getHostAddress() + ")�ѶϿ����ӣ���");
				}
			}
		}
	}
}
