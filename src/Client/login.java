package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class login extends JFrame implements ActionListener {
    private JButton jb=new JButton("登录");
    private JPanel jpl=new JPanel();
    JLabel label_welcome=new JLabel("Mini Wechat");
    JLabel label_name=new JLabel("用户名:");
    JLabel label_pw=new JLabel("密码:");


    private JTextField jtf=new JTextField(15);
    private JPasswordField jpf=new JPasswordField(15);
    public login(){
        this.setBounds(400,250,400,300);
        this.add(jpl);
        this.setTitle("Log in");
        jpl.setBackground(Color.cyan);
        //jpl.setForeground(Color.cyan);

        jpl.setLayout(null);
        jpl.add(jtf);  jtf.setBounds(150,80,150,25);
        jpl.add(jpf);  jpf.setBounds(150,120,150,25);
        jpl.add(jb);   jb.setBounds(170,170,70,30);
        jb.addActionListener(this);

        jpl.add(label_welcome);
        //label_welcome.
        label_welcome.setFont(new Font("TimesRoman",Font.BOLD,25));
        label_welcome.setBounds(120,20,150,20);
        //label_welcome.setForeground(Color.);
        jpl.add(label_name);
        label_name.setBounds(100,80,50,25);
        jpl.add(label_pw);
        label_pw.setBounds(100,120,50,25);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
        boolean flag=false;
        for(Users o:Users.uList){
            //System.out.println(o.name);
            if(jtf.getText().equals(o.name)){
                flag=true;
                if(String.valueOf(jpf.getPassword()).equals(o.password)){
                    System.out.println(o.name+"登录成功！");
                    try {
                        new client1(o);
                        this.dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    System.out.println(o.name+"密码错误，登陆失败！");
                    JOptionPane.showMessageDialog(null, "密码输入错误",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if(!flag){
        JOptionPane.showMessageDialog(null,
                "该用户不存在，请重新输入",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new users_init();
        //System.out.println("ss");
        new login().setVisible(true);
    }
}
