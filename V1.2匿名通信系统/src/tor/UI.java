package tor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class UI extends JFrame implements ActionListener{
	private JPanel jpanel_1 = new JPanel();
	private JPanel jpanel_2 = new JPanel();
	private JPanel jpanel_3 = new JPanel();
	private JPanel jpanel_4 = new JPanel();
	private JLabel jlabel_sendMessage = new JLabel("请输入要发送的信息：");
	private JLabel jlabel_sendIP = new JLabel("请输入接收方的IP地址：");
	private JLabel jlabel_show = new JLabel("显示接收的信息");
	private JTextField jtextField_sendMessage = new JTextField(10);
	private JTextField jtextField_sendIP = new JTextField(10);
	private JButton jbutton = new JButton("发送");
	private JTextArea jtextArea = new JTextArea(10, 20);
	private JScrollPane jscrollPane = new JScrollPane(jtextArea);
	private Box box=Box.createVerticalBox();//创建纵向Box容器
	
	private TorLogic torLogic = new TorLogic();
	
	public JTextArea getJtextArea() {
		return jtextArea;
	}

	public void setJtextArea(JTextArea jtextArea) {
		this.jtextArea = jtextArea;
	}

	/*
	 * main方法
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
		jtextArea.setLineWrap(true);//自动换行
		jpanel_4.add(jscrollPane);
		this.add(box);
		box.add(Box.createVerticalGlue());//添加垂直胶水，也就是jb3与box2的间隔
		box.add(jpanel_1);
		box.add(jpanel_2);
		box.add(jpanel_3);
		box.add(jpanel_4);
		box.add(Box.createVerticalGlue());//添加垂直胶水，也就是jb3与box2的间隔
		
		//添加监听
		jbutton.addActionListener(this);
		
		this.setTitle("客户端");
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
