package Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;

public class client2 extends JFrame implements ActionListener,Runnable {
    private JTextField jtf=new JTextField();
    private JTextArea jta=new JTextArea();
    private PrintStream ps=null;
    private BufferedReader br=null;
    private String nickName;
    private List userlist=new List();

    public client2() throws Exception{
        this.add(jtf, BorderLayout.SOUTH);
        this.add(jta,BorderLayout.CENTER);
        this.add(userlist,BorderLayout.EAST);

        //jtf.addActionListener((ActionListener)this);
        jtf.addActionListener(this);

        this.setLocation(500,200);
        this.setSize(400,400);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        jtf.setFont(new Font("黑体",Font.BOLD,20));
        nickName=JOptionPane.showInputDialog("请输入昵称");
        this.setTitle(nickName);

        Socket s=new Socket("127.0.0.1",9999);
        ps=new PrintStream(s.getOutputStream());  //向服务器端输出
        br=new BufferedReader(new InputStreamReader(s.getInputStream()));
        ps.println("LOGIN#"+nickName);
        new Thread(this).start();  //每个客户端都要开一条线程

        /**传递用户下线的信息*/
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ps.println("SIGNOUT#"+nickName);
                System.out.println("nmsl");
            }
        });
    }
    /**这里是针对Runnable的一个线程函数，用于打印从服务器端得到的信息*/
    public void run(){
        while(true){
            try{
                String str=br.readLine();
                //如果不是login类信息，那么就是聊天信息
                if(!str.startsWith("LOGIN#")){
                    jta.append(str+'\n');
                }else{    //?????
                    String[] strs=str.split("#");
                    userlist.removeAll();
                    for(int i=1;i<strs.length;i++){
                        userlist.add(strs[i]);
                    }
                }
            }catch (Exception e){}
        }
    }

    public void actionPerformed(ActionEvent e){
        ps.println(nickName+" says:"+jtf.getText());
        jtf.setText(null);
    }

    public static void main(String[] args) throws Exception{
        new client2().setVisible(true);

    }

}
