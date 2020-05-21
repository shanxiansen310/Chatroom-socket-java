package Server;
import Client.users_init;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;



public class server_connect extends JFrame implements Runnable, ActionListener {
    ArrayList<ChatThread> al=new ArrayList<ChatThread>();
    ArrayList<InterestThread> itList=new ArrayList<InterestThread>();
    ArrayList<FileToClient> fileToClientList=new ArrayList<FileToClient>();

    private JTextField jtf=new JTextField();
    private JButton jb_sendAll=new JButton("群发系统消息");
    private JButton jb_disconnect=new JButton("强制用户下线");
    private JLabel label=new JLabel("服务器端日志" +
            "                                                          已上线用户");
    private JTextArea jta=new JTextArea();
    private List userlist= new List();
    private JPanel jpl=new JPanel();
    private ArrayList<String> user_arr=new ArrayList<String>();
    public boolean isFound=false;
    public String dis_name;
    public static int port_num=6666;

    String fileName;
    String filePath;



    //private boolean isAcceptFile=false;

    public server_connect() throws Exception {
        try { // 使用Windows的界面风格
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle("Server");
        this.add(jta,BorderLayout.CENTER);
        this.setSize(400,600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.add(userlist,BorderLayout.EAST);
        this.add(label,BorderLayout.NORTH);
        this.add(jpl,BorderLayout.SOUTH);
        jpl.add(jb_sendAll,BorderLayout.SOUTH);
        jpl.add(jb_disconnect,BorderLayout.SOUTH);
        
        jb_sendAll.addActionListener(this);
        jb_disconnect.addActionListener(this);

        //this.add(log);
        //this.setVisible(true);
        new Thread(this).start();
        new Interest().start();
        new FileThreadFromClient().start();
        //new ToClient().start();
    }



    public void run(){
        try{

            ServerSocket ss=new ServerSocket(9999);
            //ServerSocket socket_interest=new ServerSocket(9997);


//            System.out.println("111");
            while (true){

                //关于兴趣小组,这个要放前面！！！
//                Socket s_interest=socket_interest.accept();
//                InterestThread it=new InterestThread(s_interest);
//                itList.add(it);
//                it.start();

                Socket s=ss.accept();

                //jta.append("Someone have connected!\n");
                ChatThread ct=new ChatThread(s);
                System.out.println("one thread connected.");
                al.add(ct);
                ct.start();

                /**下线检测的线程*/
                DisconnectThread dt=new DisconnectThread(s);
                dt.start();


            }
        }catch (Exception e){}
    }

    /**文件传输线程*/
    class FileThreadFromClient extends Thread{
        private Socket socket_file=null;
        private BufferedInputStream bis;
        private BufferedOutputStream bos;
        private String name_sender;
        private String group_title;

        @Override
        public void run(){
            try {
                ServerSocket serverSocket=new ServerSocket(7777);

                while (true){
                    socket_file=serverSocket.accept();
                    System.out.println("FileThreadFromClient 已连接！");

                    BufferedReader br=new BufferedReader(new InputStreamReader(
                            socket_file.getInputStream()));
                    //读取传输来的文件名
                    String string=br.readLine();
                    String[] strings=string.split("#");
                    fileName=strings[1];
                    name_sender=strings[0];
                    group_title=strings[2];
                    filePath="C:\\Users\\35378\\Desktop\\Study" +
                            "\\作业\\计网\\计网实验-socket\\服务器文件\\"+fileName;

                    bis=new BufferedInputStream(socket_file.getInputStream());
                    bos=new BufferedOutputStream(new FileOutputStream(filePath));

                    byte[] bytes=new byte[1024];
                    int len=0;
                    System.out.println("开始下载...");
                    while((len=bis.read(bytes))!=-1){
                        bos.write(bytes,0,len);
                        bos.flush();
                    }
                    System.out.println("下载成功！！！");
                    socket_file.close();
                    bos.close();
                    br.close();
                    System.out.println("▼准备发送文件给其他客户端");


                    new newFileSend(filePath).start();
                    //告诉客户端可以开始接收文件啦
                    for(InterestThread it:itList){
                        it.ps.println("开始#"+name_sender+"#"+group_title);
                    }


                    System.out.println("newFileSend:"+filePath);
//                    for (FileToClient o:fileToClientList){
//                        //o.start();
//                        o.isAcceptFile=true;
//                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class newFileSend extends Thread{
        private Socket socket_file=null;
        private BufferedInputStream bis;
        private BufferedOutputStream bos;
        private String address;

        public newFileSend(String fileAddress){
            this.address=fileAddress;
        }

        @Override
        public void run(){
            try {
                ServerSocket serverSocket = new ServerSocket(port_num++);

                while (true) {
                    System.out.println("newFileSend 正在连接！");
                    socket_file = serverSocket.accept();
                    System.out.println("newFileSend 已连接！");

                    //就发送一次文件名，用于确定格式，并不是从本机地址获取
                    PrintWriter printWriter=new PrintWriter(socket_file.getOutputStream());
                    String[] strings=address.split("\\\\|/");
                    String File_name=strings[strings.length-1];
                    printWriter.println(File_name);
                    printWriter.flush();

                    bis=new BufferedInputStream(new FileInputStream(address));
                    bos=new BufferedOutputStream(socket_file.getOutputStream());

                    //服务器端向客户端发送数据
                    byte[] dataByte=new byte[1024];  //大小其实随意，因为是循环，别太大就好
                    int len=0;
                    while((len=bis.read(dataByte))!=-1){
                        bos.write(dataByte,0,len);
                    }
                    bos.close();

                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if (socket_file!=null){
                        socket_file.close();
                    }
                    if(bis!=null){
                        bis.close();
                    }
                }catch (Exception e){}
            }
        }
    }
    //专门用于接收FileToClient
    class ToClient extends Thread{
        //Socket s_fileToC=null;
        public void run(){
            try{
                ServerSocket socket_toClient =new ServerSocket(9996);
                while (true){
                    System.out.println("ToClient starts");
                    Socket s_fileToC=socket_toClient.accept();

                    FileToClient ftc=new FileToClient(s_fileToC);
                    fileToClientList.add(ftc);
                    System.out.println("一个fileToClient加入!");
                    ftc.start();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class FileToClient extends Thread{
        private Socket socketToC=null;
        private BufferedOutputStream bos=null;
        private BufferedInputStream bis=null;
        boolean isAcceptFile=false;

        public FileToClient(Socket socketToC) throws IOException {
            this.socketToC=socketToC;
//            bos=new BufferedOutputStream(socketToC.getOutputStream());
//            bis=new BufferedInputStream(new FileInputStream(filePath));
            System.out.println("FileToClient 启动");
        }

        public void run(){

            while (true){
                //用于唤醒这个线程，不加点输出的运行不了
                try{
                    Thread.sleep(5000);
                }catch (Exception e){}
                System.out.print("");

                if(isAcceptFile){
                    try {
                        //传输文件名，用于确定格式(不是文件地址！！！)
                        PrintWriter pw=new PrintWriter(socketToC.getOutputStream());
                        pw.println(fileName);
                        pw.flush();
                        //pw.close();

                        //文件的传输流,filePath和fileName仅由FileFromClient控制
                        System.out.println("toClient filePath:"+filePath);
                        bis=new BufferedInputStream(new FileInputStream(filePath));
                        bos=new BufferedOutputStream(socketToC.getOutputStream());

                        //服务器端向客户端发送数据
                        byte[] dataByte=new byte[1024];
                        int len=0;

                        System.out.println("databyte:");
                        for (FileToClient o:fileToClientList){
                            while((len=bis.read(dataByte))!=-1){
                                o.bos.write(dataByte,0,len);
                                System.out.print(dataByte);
                            }
                            o.bos.close();
                            o.bis.close();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
//                    finally {
//                        try {
//                            if (socketToC!=null){
//                                socketToC.close();
//                            }
//                            if(bis!=null){
//                                bis.close();
//                            }
//                        }catch (Exception e){}
//                    }

                }
                isAcceptFile=false;
            }
        }
    }

    public void actionPerformed(ActionEvent e){
        if (e.getSource()==jb_sendAll){
            String str=JOptionPane.showInputDialog("请输入群发的系统消息：");
            str="★★★系统消息:"+str;
            for(ChatThread ct:al){
                ct.ps.println(str);
            }
            System.out.println("已发送系统消息！");
        }
        else if (e.getSource()==jb_disconnect){
            //while(!isFound){
            dis_name=JOptionPane.showInputDialog("请输入您想要强制下线的用户的名称:");
            for(String s:user_arr){
                if(s.equals(dis_name)){
                    isFound=true;

                }
            }
            System.out.println("disname:"+dis_name);
            if(!isFound){
                JOptionPane.showMessageDialog(null, "此用户未上线或无此用户", "Error", JOptionPane.ERROR_MESSAGE);
            }
            System.out.println("isFound:"+isFound);
        }
    }

    class DisconnectThread extends Thread {
        BufferedReader br;
        PrintStream ps;
        public DisconnectThread(Socket s) throws IOException {
            System.out.println("DisconnectThread starts!");
            br=new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps=new PrintStream(s.getOutputStream());
        }
        public void run() {
            while(true){
                /**不知道多线程这里有什么毛病，必须要加一句输出语句才能正常执行*/
                System.out.print("");
                try {
                    Thread.sleep(2000);
                }catch (Exception e){}
                if(isFound){
                    for(ChatThread ct:al){
                        ct.ps.println("forcedlogout#"+dis_name);
                    }
                    System.out.println("forcedlogout#"+dis_name);
                    jta.append("forcedlogout#"+dis_name+"\n");
                    isFound=false;
                    for(int i=0;i<al.size();i++){
                        if(al.get(i).nickName.equals(dis_name)){
                            al.remove(al.get(i));
                            i--;
                        }
                    }
                }
            }
        }
    }

    //专门用来接收InterestGroup线程
    class Interest extends Thread{
        public void run(){
            ServerSocket socket_interest= null;
            try {
                socket_interest = new ServerSocket(9997);
//            System.out.println("111");
                while (true){
                    Socket s_interest=socket_interest.accept();
                    InterestThread it=new InterestThread(s_interest);
                    itList.add(it);
                    it.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class InterestThread extends Thread{
        BufferedReader br;
        PrintStream ps;

        /**表示已连接上的线程*/
        public InterestThread(Socket s) throws Exception{
            br=new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps=new PrintStream(s.getOutputStream());
        }

        public void run(){
            System.out.println("InterestThread starts!");
            while (true){
                try{
                    String str=br.readLine();
                    jta.append(str+'\n');
                    for (InterestThread it:itList){
                        it.ps.println(str);
                    }
                }catch (Exception e){}
            }
        }

    }

    //该线程接收客户端发送的内容并发送给其他客户端
    class ChatThread extends Thread{
        BufferedReader br;
        PrintStream ps;
        String nickName;
        /**表示已连接上的线程*/
        public ChatThread(Socket s) throws Exception{
            br=new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps=new PrintStream(s.getOutputStream());
        }

        public void run(){
            System.out.println("one thread starts.");
            while (true){
//                System.out.println(count++);
//                System.out.println("nickName:"+nickName+"\t\tdisname:"+dis_name);
//                if((isFound)&&(nickName==dis_name)){
//                    ps.println("forcedlogout#"+dis_name);
//                    isFound=false;
//                }
                try{
                    String str=br.readLine();  //这句话其实能使在没有新语句读入时程序停止
                    System.out.println("br.readline()="+str);
                    jta.append(str+"\n");

                    /**在al中删除退出程序的用户*/
                    if(str.startsWith("SIGNOUT#")){
                        System.out.println("signout执行");
                        String[] strs1=str.split("#");
                        nickName=strs1[1];
                        str="LOGIN#";
                        /**list遍历中有remove必须要这么搞！！！*/
                        for(int i=0;i<al.size();i++){
                            if(al.get(i).nickName.equals(nickName)){
                                al.remove(al.get(i));
                                i--;
                            }
                        }
                        for(ChatThread ct:al){
                            str=str+ct.nickName+"#";
                        }
                    }

                    /**处理登录信息，将登录的用户加入到数组中*/
                    else if(str.startsWith("LOGIN#")){
                        String[] strs=str.split("#");
                        nickName=strs[1];
                        str="LOGIN#";
                        for(ChatThread ct:al){
                            str=str+ct.nickName+"#";
                        }
                    }

                    /**处理聊天信息*/
                    for(ChatThread ct:al){
                        ct.ps.println(str);
                    }
                    System.out.println("已发送str至所有用户");

                    str="LOGIN#";
                    for(ChatThread ct:al){
                        System.out.print(ct.nickName+"   ");
                        str=str+ct.nickName+"#";
                    }
                    System.out.println("★"+str);

                    /**用于服务器端的用户显示*/
                    String[] strs2=str.split("#");
                    userlist.removeAll();
                    for(int i=1;i<strs2.length;i++){
                        userlist.add(strs2[i]);
                        user_arr.add(strs2[i]);
                        System.out.println("userlist加入"+strs2[i]);
                    }

                    System.out.println();

                }catch (Exception e){}
            }
        }
    }



    public static void main(String[] args) throws Exception {
        //new users_init();
        new server_connect().setVisible(true);
    }
}



