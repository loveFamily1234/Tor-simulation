package tor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class UI extends JFrame implements ActionListener{
	private JPanel jpanel_1 = new JPanel();
	private JPanel jpanel_2 = new JPanel();
	private JPanel jpanel_3 = new JPanel();
	private JPanel jpanel_4 = new JPanel();
	private JLabel jlabel_sendMessage = new JLabel("������Ҫ���͵���Ϣ��");
	private JLabel jlabel_sendIP = new JLabel("��������շ���IP��ַ��");
	private JLabel jlabel_show = new JLabel("��ʾ���յ���Ϣ");
	private JTextField jtextField_sendMessage = new JTextField(10);
	private JTextField jtextField_sendIP = new JTextField(10);
	private JButton jbutton = new JButton("����");
	private JTextArea jtextArea = new JTextArea(10, 20);
	private JScrollPane jscrollPane = new JScrollPane(jtextArea);
	private Box box=Box.createVerticalBox();//��������Box����
	
	private TorLogic torLogic = new TorLogic();
	
	public JTextArea getJtextArea() {
		return jtextArea;
	}

	public void setJtextArea(JTextArea jtextArea) {
		this.jtextArea = jtextArea;
	}

	/*
	 * main����
	 */
	public static void main(String[] args) {
		TorLogic torLogic = new TorLogic();
		UI ui = new UI(torLogic);
		torLogic.server(ui);
	}
	
	public UI(TorLogic tor){
		torLogic = tor;
		jpanel_1.add(jlabel_sendMessage);
		jpanel_1.add(jtextField_sendMessage);
		jpanel_2.add(jlabel_sendIP);
		jpanel_2.add(jtextField_sendIP);
		jpanel_3.add(jbutton);
		jpanel_4.add(jlabel_show);
		jtextArea.setLineWrap(true);//�Զ�����
		jpanel_4.add(jscrollPane);
		this.add(box);
		box.add(Box.createVerticalGlue());//��Ӵ�ֱ��ˮ��Ҳ����jb3��box2�ļ��
		box.add(jpanel_1);
		box.add(jpanel_2);
		box.add(jpanel_3);
		box.add(jpanel_4);
		box.add(Box.createVerticalGlue());//��Ӵ�ֱ��ˮ��Ҳ����jb3��box2�ļ��
		
		//��Ӽ���
		jbutton.addActionListener(this);
		
		this.setTitle("�ͻ���");
		this.setBounds(350,100,600,500);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public void actionPerformed(ActionEvent e) {
		String sendMessage = jtextField_sendMessage.getText();
		String sendIP = jtextField_sendIP.getText();
		torLogic.client(sendMessage,sendIP);
	}
	
}
