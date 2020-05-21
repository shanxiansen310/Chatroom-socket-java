package Client;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Flow;

import Server.server_connect;

import static Client.Users.uList;


class InterestGroup extends JFrame implements Runnable {
    private String groupTitle;
    private JTextPane jTextPane=new JTextPane();
    private StyledDocument doc=null;
    private JScrollPane scrollPane=new JScrollPane(jTextPane);
    private Box box=Box.createVerticalBox();
    private Box box_function=Box.createHorizontalBox();
    private Box box_text=Box.createHorizontalBox();
    private JLabel jLabel_fontSize=new JLabel("字体大小");
    private JLabel jLabel_fontColor=new JLabel("字体颜色");
    private JTextField jtf_text=new JTextField(28);

    private JButton jbt_emoji=new JButton("表情");
    private JButton jbt_image=new JButton("图片");
    private JButton jbt_sendfile=new JButton("文件");
    private JButton jbt_quitGroup=new JButton("退出小组");
    private JButton jbt_announce=new JButton("通告");
    private JLabel jLabel_groups=new JLabel("小组成员:");
    private JComboBox fontSize=null,fontColor=null;
    private StyledDocument styledDocument=null;
    //小组成员显示
    private JTextPane jtp_groups=new JTextPane();
    private ArrayList<Users> userList= uList;

    private JPanel jPanel=new JPanel();  //最大的画布
    private JPanel jPanel_east=new JPanel();
    private JPanel jPanel_announce=new JPanel();
    private JPanel jPanel_center=new JPanel();
    private JPanel jPanel_quitGroup=new JPanel();
    private JPanel jPanel_south=new JPanel();
    private JPanel jpl_function=new JPanel();
    private JPanel jpl_text=new JPanel();
    private Component blackJTextPane() {
        JTextPane pane = new JTextPane();
        pane.setBackground(Color.BLACK);
        pane.setForeground(Color.WHITE);
        pane.setText("Here is example text");
        return pane;
    }
    private Users users=null;

    private PrintStream ps=null;
    private BufferedReader br=null;

    private ArrayList<JButton> jButtonList=new ArrayList<JButton>();

    public static int portNum=6666;

    public InterestGroup(String groupTitle,Users users) throws Exception {
        this.users=users;
        this.groupTitle=groupTitle;
        try { // 使用Windows的界面风格
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle("▼"+groupTitle+"小组▼"+"★"+users.name);
        this.add(jPanel);
        this.setBounds(200,200,500,600);
        jPanel.setLayout(new BorderLayout());

        Socket s=new Socket("127.0.0.1",9997);
        ps=new PrintStream(s.getOutputStream());     //向服务器端输出
        br=new BufferedReader(new InputStreamReader(s.getInputStream()));
        new Thread(this).start();


        /**主界面右侧设计*/
        jPanel.add(jPanel_east,BorderLayout.EAST);
        jPanel_east.setLayout(new BorderLayout());
        jPanel_east.add(jPanel_announce,BorderLayout.NORTH);
        jPanel_announce.setLayout(new BorderLayout());
        jPanel_announce.add(jbt_announce,BorderLayout.NORTH);
        jbt_announce.setForeground(Color.red); jbt_announce.setBackground(Color.black);
        jbt_announce.setPreferredSize(new Dimension(100,50));
        jbt_announce.setFont(new Font("宋体",Font.BOLD,20));
        jbt_announce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Announce();
            }
        });

        jPanel_announce.add(jLabel_groups,BorderLayout.SOUTH);
        jLabel_groups.setForeground(Color.white); jPanel_announce.setBackground(Color.black);
        //jLabel_groups.setPreferredSize(new Dimension(100,50));
        jLabel_groups.setFont(new Font("宋体",Font.PLAIN,20));

        jPanel_east.add(jPanel_quitGroup,BorderLayout.SOUTH);
        jPanel_east.add(jtp_groups,BorderLayout.CENTER);
        jPanel_quitGroup.setLayout(new BorderLayout());
        jPanel_quitGroup.setPreferredSize(new Dimension(100,100));
        jPanel_quitGroup.add(jbt_quitGroup,BorderLayout.NORTH);
        jPanel_quitGroup.setBackground(Color.black);
        jbt_quitGroup.setBackground(Color.red);
        jbt_quitGroup.setForeground(Color.WHITE);
        jbt_quitGroup.setPreferredSize(new Dimension(100,40));
        jbt_quitGroup.setFont(new Font("宋体",Font.BOLD,15));
        jbt_quitGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = {"确定","取消"};
                int response=JOptionPane.showOptionDialog(jbt_quitGroup, "您确定退出吗？"
                        , "提示",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if(response==0)
                {
                    users.inMathGroup=false;
                    ps.println("QuitGroup#"+groupTitle+"#"+users.name);
                }

            }
        });


        jPanel.add(jPanel_center,BorderLayout.CENTER);
        //小组成员显示
        jPanel_center.add(scrollPane);
        jtp_groups.setEditable(false);
        jPanel_center.setBackground(Color.blue);
        scrollPane.setPreferredSize(new Dimension(380,507));
        jtp_groups.setBackground(Color.black);


        for(int i=0;i<userList.size();i++){
            if(userList.get(i).inMathGroup==true&&groupTitle.equals("数学"))
                addColoredText(jtp_groups,userList.get(i).name+"\n",Color.white,15);
            if(userList.get(i).inEngGroup==true&&groupTitle.equals("英语"))
                addColoredText(jtp_groups,userList.get(i).name+"\n",Color.white,15);
        }

        /**SOUTH发送框设计*/
        jPanel.add(jPanel_south,BorderLayout.SOUTH);
        jPanel_south.setLayout(new BorderLayout());

        jPanel_south.add(jpl_function,BorderLayout.NORTH);
        jpl_function.setLayout(new GridLayout(1,7,10,5));
        jpl_function.add(jLabel_fontSize);
        jLabel_fontSize.setForeground(Color.cyan);
        jpl_function.setBackground(Color.black);
        String[] fSize={"12","14","16","18","20","22"};
        fontSize=new JComboBox(fSize);
        jpl_function.add(fontSize);
        fontSize.setForeground(Color.white);
        fontSize.setBackground(Color.black);
        jpl_function.add(jLabel_fontColor); jLabel_fontColor.setForeground(Color.cyan);
        String[] fColor={"红色","黑色","蓝色","绿色","黄色"};
        fontColor=new JComboBox(fColor);
        fontColor.setBackground(Color.black);
        fontColor.setForeground(Color.white);
        jpl_function.add(fontColor);

        jpl_function.add(jbt_emoji);
        jbt_emoji.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new EmojiFrame();
            }
        });
        jpl_function.add(jbt_image);
        jbt_image.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser=new JFileChooser();
                int ok=fileChooser.showOpenDialog(jbt_image);
                if (ok != JFileChooser.APPROVE_OPTION) return;
                String ImagePath=fileChooser.getSelectedFile().getPath();
                System.out.println(ImagePath);

                ps.println("Image#"+ImagePath+"#"+users.name+"#"+groupTitle);


            }
        });
        jpl_function.add(jbt_sendfile);
        jbt_sendfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser=new JFileChooser("C:\\Users\\35378\\Desktop\\Study\\作业\\计网");
                int ok=fileChooser.showOpenDialog(jbt_image);
                if (ok != JFileChooser.APPROVE_OPTION) return;

                String FilePath=fileChooser.getSelectedFile().getPath();
                //点击触发文件传输的线程 c to s
                try {
                    new FileThreadClientToS(FilePath,users.name).start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        jPanel_south.add(jtf_text,BorderLayout.SOUTH);

        jtf_text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String str=jtf_text.getText();
                str=users.name+" says:"+str;
                //addColoredText(jTextPane,str+"\n",getColor(),getFontSize());
                ps.println(groupTitle+"#"+str);
                jtf_text.setText(null);
            }
        });

        /**主界面JTextPane*/
        jPanel.add(scrollPane);
        jTextPane.setEditable(false);
        //addColoredText(jTextPane,"nmsl",Color.blue, Integer.parseInt((String) fontSize.getSelectedItem()));
        //System.out.println(Integer.parseInt((String) fontSize.getSelectedItem()));
        //jTextPane.setCaretPosition(1);

        //new FileThreadFromServer().start();

        //new newFileReceive().start();
        this.setVisible(true);
    }

    public void run(){
        System.out.println("小组线程开启！");
        while (true){
            try{
                String str=br.readLine();
                System.out.println("读入字符:"+str);
                System.out.println("读入字符前两位:"+str.substring(0,2));
                //System.out.println(groupTitle);
                //System.out.println(str.substring(0,2).equals(groupTitle));
                if(str.substring(0,2).equals(groupTitle)){
                    String[] strs=str.split("#");
                    addColoredText(jTextPane,strs[1]+'\n',getColor(),getFontSize());
                }
                else if(str.startsWith("Emoji#")){    //表情默认为提前安装到了每个客户端
                    System.out.println("Emoji starts!");
                    String[] str_set=str.split("#");
                    if(str_set[3].equals(groupTitle)) {
                        String EmojiPath = "C:\\Users\\35378\\Pictures\\素材\\" + str_set[1] + ".jpg";
                        JLabel jLabelEmoji = new JLabel();
                        ImageLabel(jLabelEmoji, EmojiPath, 20, 20);
                        addColoredText(jTextPane, str_set[2] + " send a Emoji:", getColor(), getFontSize());
                        jTextPane.setCaretPosition(doc.getLength());
                        jTextPane.insertComponent(jLabelEmoji);
                        addColoredText(jTextPane, "\n", getColor(), getFontSize());
                    }
                }else if(str.startsWith("Image#")){
                    System.out.println("image starts!");
                    String[] str_img=str.split("#");

                    if(str_img[3].equals(groupTitle)){
                        JLabel jLabelImage=new JLabel();
                        addColoredText(jTextPane,str_img[2]+" send a image:",getColor(),getFontSize());
                        jTextPane.setCaretPosition(doc.getLength());
                        ImageLabel(jLabelImage,str_img[1],40,40);
                        jTextPane.insertComponent(jLabelImage);
                        addColoredText(jTextPane,"\n",getColor(),getFontSize());
                    }
                }else if (str.startsWith("QuitGroup#")){
                    System.out.println(str);
                    String[] str_qt=str.split("#");
                    for (Users u:userList){
                        if (u.name.equals(str_qt[2])&&str_qt[1].equals("英语")){
                            u.inEngGroup=false;
                        }
                        else if(u.name.equals(str_qt[2])&&str_qt[1].equals("数学")){
                            u.inMathGroup=false;
                        }
                    }

                    if (str_qt[1].equals(groupTitle)){
                        jtp_groups.setText(null);

                        for(int i=0;i<userList.size();i++){
                            if(userList.get(i).inMathGroup==true&&groupTitle.equals("数学"))
                                addColoredText(jtp_groups,userList.get(i).name+"\n",Color.white,15);
                            if(userList.get(i).inEngGroup==true&&groupTitle.equals("英语"))
                                addColoredText(jtp_groups,userList.get(i).name+"\n",Color.white,15);
                        }
                        if(str_qt[2].equals(users.name)) {
                            this.dispose();
                        }
                    }
                }else if(str.startsWith("开始")){
                    System.out.println("execute newFileReceive");
                    String[] strings=str.split("#");
                    String sendName=strings[1];
                    String sendGroup=strings[2];

                    if (!(sendName.equals(users.name))&&groupTitle.equals(sendGroup)) {
                        Object[] options = {"接收","拒绝"};
                        int response=JOptionPane.showOptionDialog(jbt_quitGroup, sendName+" 给您发送了一个文件"
                                , "文件传输",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if(response==0)
                        {
                            new newFileReceive().start();
                        }
                    }
                }

            }catch (Exception e){}
        }
    }

    //文件传输  c to s
    class FileThreadClientToS extends Thread{
        BufferedInputStream bis=null;
        BufferedOutputStream bos=null;
        Socket s=null;
        String ClientName;

        String FilePath=null;
        public FileThreadClientToS(String FilePath,String name) {
            this.FilePath=FilePath;
            this.ClientName=name;
        }

        public void run(){
            try {
                //ServerSocket serverSocket=new ServerSocket(7777);

                s=new Socket("127.0.0.1",7777);
                //System.out.println("服务器端已连接:"+s.getInetAddress());

                //就发送一次文件名，用于确定格式，并不是从本机地址获取
                PrintWriter printWriter=new PrintWriter(s.getOutputStream());
                String[] strings=FilePath.split("\\\\|/");
                String File_name=strings[strings.length-1];
                File_name=ClientName+"#"+File_name+"#"+groupTitle;
                printWriter.println(File_name);
                printWriter.flush();

                bis=new BufferedInputStream(new FileInputStream(FilePath));
                bos=new BufferedOutputStream(s.getOutputStream());


                //客户端向服务器端发送数据
                byte[] dataByte=new byte[1024];  //大小其实随意，因为是循环，别太大就好
                int len=0;
                while((len=bis.read(dataByte))!=-1){
                    bos.write(dataByte,0,len);
                    bos.flush();
                }
                bos.close();


//                String str_read=br.readLine();
//                System.out.println(str_read);


            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (s!=null){
                        s.close();
                    }
                    if(bis!=null){
                        bis.close();
                    }
                }catch (Exception e){}
            }
        }

    }

    //文件接收  s to c
    class FileThreadFromServer extends Thread{
        BufferedInputStream bis=null;
        BufferedOutputStream bos=null;
        Socket socketFromS=null;

        String fileNameStoC=null;
        String filePathStoC=null;

        public FileThreadFromServer(){
            System.out.println("FileThreadFromServer已启动！");
        }

        public void run(){

                try {
                    socketFromS = new Socket("127.0.0.1", 9996);
                    while (true) {
                        try{
                            Thread.sleep(3000);
                        }catch (Exception e){}

                        BufferedReader br = new BufferedReader
                                (new InputStreamReader(socketFromS.getInputStream()));
                        fileNameStoC = br.readLine();
                        fileNameStoC = users.name + "-" + fileNameStoC;


                        filePathStoC = "C:\\Users\\35378\\Desktop\\Study" +
                                "\\作业\\计网\\计网实验-socket\\客户端文件\\" + fileNameStoC;
                        System.out.println("fileNameStoC:" + fileNameStoC);

                        bis = new BufferedInputStream(socketFromS.getInputStream());
                        bos = new BufferedOutputStream(new FileOutputStream(filePathStoC));

                        byte[] bytes = new byte[1024];
                        int len = 0;
                        System.out.println("开始下载...");
                        long begin=System.currentTimeMillis();
                        while ((len = bis.read(bytes)) != -1||len!=0) {
                            bos.write(bytes, 0, len);

                            long end=System.currentTimeMillis();
                            System.out.print((end-begin));
                            if((begin-end)>1000)
                                break;
                        }
                        System.out.println("下载成功！！！");
                        //br.close();
                    }
                } catch (Exception e) {}
            }
        }

    class newFileReceive extends Thread{
        private Socket socket_file=null;
        private BufferedInputStream bis;
        private BufferedOutputStream bos;


        String myFileName;
        String myFilePath;

        @Override
        public void run(){
            try{
                socket_file=new Socket("127.0.0.1",portNum++);

                BufferedReader br=new BufferedReader(new InputStreamReader(
                        socket_file.getInputStream()));
                myFileName=br.readLine();
                myFilePath="C:\\Users\\35378\\Desktop\\Study" +
                        "\\作业\\计网\\计网实验-socket\\客户端文件\\"+users.name+"--"+myFileName;

                bis=new BufferedInputStream(socket_file.getInputStream());
                bos=new BufferedOutputStream(new FileOutputStream(myFilePath));

                byte[] bytes=new byte[1024];
                int len=0;
                System.out.println("开始下载...");
                while((len=bis.read(bytes))!=-1){
                    bos.write(bytes,0,len);
                }
                System.out.println("下载成功！！！");

                JOptionPane.showMessageDialog(null,
                        "文件已存放至您的电脑中! 下面是存放地址\n"+myFilePath);

                socket_file.close();
                bos.close();
                br.close();

            }catch (Exception e){}
        }

    }



    public int getFontSize()
    {
        return Integer.parseInt((String) fontSize.getSelectedItem());
    }
    public Color getColor(){
        String color=(String)fontColor.getSelectedItem();
        if(color=="红色"){
            return Color.red;
        }else if (color=="黑色"){
            return Color.black;
        }else if (color=="蓝色"){
            return Color.blue;
        }else if (color=="绿色"){
            return Color.green;
        }
        else
            return Color.yellow;

    }

    public void ImageLabel(JLabel jlb,String path,int width,int height) {
        ImageIcon image = new ImageIcon(path);
        // image.setImage(image.getImage().getScaledInstance(width, height,Image.SCALE_DEFAULT));
        Image img = image.getImage();
        img = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        image.setImage(img);
        jlb.setIcon(image);
        jlb.setSize(width, height);
    }
    public void ImageButton(JButton jb,String path,int width,int height) {
        ImageIcon image = new ImageIcon(path);
        // image.setImage(image.getImage().getScaledInstance(width, height,Image.SCALE_DEFAULT));
        Image img = image.getImage();
        img = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        image.setImage(img);
        jb.setIcon(image);
        jb.setSize(width, height);
    }

    class EmojiFrame extends JFrame{
        private JPanel jPanelEmoji=new JPanel();
        String emojiPath;

        public EmojiFrame(){
            this.setTitle("表情");
            this.setSize(80,180);
            this.add(jPanelEmoji);
            jPanelEmoji.setLayout(new GridLayout(3,3,0,0));
            final int[] key=new int[10];

            for(int i=1;i<=9;i++){
                JButton jButtonEmoji=new JButton();
                String x=String.valueOf(i);
                emojiPath="C:\\Users\\35378\\Pictures\\素材\\"+x+".jpg";
                ImageButton(jButtonEmoji,emojiPath,30,30);
                //jButtonEmoji.setPreferredSize(new Dimension(20,20));
                jPanelEmoji.add(jButtonEmoji);
                key[i]=i;
                jButtonEmoji.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ps.println("Emoji#"+x+"#"+users.name+"#"+groupTitle);
                        System.out.print("Emoji#"+x+"#"+users.name+"#"+groupTitle);
                    }
                });
            }

            this.setVisible(true);
            this.setLocationRelativeTo(jbt_emoji);
        }
    }

    public void addColoredText(JTextPane pane, String text, Color color,int fontsize) {
        doc = pane.getStyledDocument();

        Style style = pane.addStyle("Color Style", null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontSize(style,fontsize);
        try {
            doc.insertString(doc.getLength(), text, style);
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}

class Announce extends JFrame{
    private JPanel jPanel=new JPanel();
    private JPanel jPanel_north=new JPanel();
    private JPanel jPanel_south=new JPanel();
    private JPanel jPanel_center=new JPanel();
    private JLabel jLabel=new JLabel("组内通告");
    private JTextArea jTextArea=new JTextArea();
    private JTextField jTextField=new JTextField(22);
    private JLabel jLabel_reset=new JLabel("修改通告:");

    public Announce(){
        this.setTitle("组内通告！！！");
        //this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setBounds(500,300,300,200);
        this.setVisible(true);

        this.add(jPanel);
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jPanel_north,BorderLayout.NORTH);
        jPanel_north.add(jLabel);
        jPanel_north.setBackground(Color.black);
        jLabel.setForeground(Color.RED);
        jLabel.setFont(new Font("宋体",Font.BOLD,20));
        jPanel.add(jPanel_center,BorderLayout.CENTER);
        jPanel_center.add(jTextArea);
        jPanel_center.setBackground(Color.black);
        jTextArea.setPreferredSize(new Dimension(285,122));
        jTextArea.setEditable(false);
        jTextArea.setBackground(Color.black);
        jTextArea.setForeground(Color.white);
        jTextArea.setFont(new Font("黑体",Font.BOLD,15));

        jPanel.add(jPanel_south,BorderLayout.SOUTH);
        jPanel_south.setLayout(new BorderLayout());
        jPanel_south.setBackground(Color.white);
        jLabel_reset.setForeground(Color.blue);
        jPanel_south.add(jTextField,BorderLayout.EAST);
        jPanel_south.add(jLabel_reset,BorderLayout.WEST);

        String s="欢迎大家来到本小组！";
        jTextArea.append(s);
        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jTextArea.setText(null);
                jTextArea.append(jTextField.getText());
                jTextField.setText(null);
            }
        });
    }
}

public class client1 extends JFrame implements ActionListener,Runnable {
    private JTextField jtf=new JTextField();
    private JTextArea jta=new JTextArea();
    private JButton jb1=new JButton();
    private JButton jb2=new JButton();
    private JTextPane jTextPane=new JTextPane();
    private JPanel jPanel_east=new JPanel();
    private JPanel jPanel_interest=new JPanel();
    private JLabel jl_interest=new JLabel("兴趣小组");
    private JButton jb_Mathgroup=new JButton("数学小组");
    private JButton jb_Englishgroup=new JButton("英语小组");

    private PrintStream ps=null;
    private BufferedReader br=null;
    private String nickName;
    private List userlist=new List();
    private JPanel jpl_userlist=new JPanel();
    private Users users;
    private JLabel jl_onlineUsers=new JLabel("已上线用户");
    private JPanel jpl_onlineusers=new JPanel();

    public JPanel friendList(){
        JPanel fl=new JPanel();
        JLabel title=new JLabel("好友列表");
        fl.setLayout(new GridLayout(4,1,5,20));
        String[] labels=new String[users.myFriend.size()];
        for(int i=0;i<labels.length;i++){
            labels[i]= users.myFriend.get(i);
        }
        fl.add(title);
        for(int i=0;i<3;i++){
            JButton jbt=new JButton(labels[i]);
            jbt.setForeground(Color.RED);
            jbt.addActionListener(this);
            fl.add(jbt);
        }
        return fl;
    }

    public client1(Users u) throws Exception{
        try { // 使用Windows的界面风格
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        users=u;

        this.add(jtf, BorderLayout.SOUTH);
        this.add(jta,BorderLayout.CENTER);
        this.add(jPanel_east,BorderLayout.EAST);
        jPanel_east.setLayout(new BorderLayout());
        jPanel_east.add(jpl_userlist,BorderLayout.CENTER);
        jPanel_east.add(jpl_onlineusers,BorderLayout.NORTH);
        jPanel_east.setPreferredSize(new Dimension(120,100));

        jpl_userlist.add(userlist);
        jpl_onlineusers.add(jl_onlineUsers);
        //userlist.setSize(50,200);

        /**兴趣小组*/
        jPanel_interest.setPreferredSize(new Dimension(120,150));
        jPanel_east.add(jPanel_interest,BorderLayout.SOUTH);
        jPanel_interest.setLayout(new GridLayout(3,1,10,15));
        jPanel_interest.add(jl_interest);
        jl_interest.setFont(new Font("宋体",Font.PLAIN,15));
        jPanel_interest.add(jb_Mathgroup);
        jb_Mathgroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(users.inMathGroup==false){
                    Object[] options = {"确定","取消"};
                    int response=JOptionPane.showOptionDialog(jb_Mathgroup, "您尚未加入数学兴趣小组，是否加入？"
                            , "提示",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if(response==0)
                    {
                        users.inMathGroup=true;
                        ps.println("QuitGroup#"+"数学"+"#"+users.name);
//                        for (Users u:uList) {
//                            if (u.name.equals(users.name)) {
//                                u.inMathGroup = true;
//                            }
//                        }
                        try{
                            new InterestGroup("数学",users);
                        }catch (Exception ex){}
                    }
                }
                else {
                    try {
                        new InterestGroup("数学",users);
                    }catch (Exception ex){}

                }
            }
        });
        jb_Englishgroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(users.inEngGroup==false){
                    Object[] options = {"确定","取消"};
                    int response=JOptionPane.showOptionDialog(jb_Englishgroup, "您尚未加入数学兴趣小组，是否加入？", "提示",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if(response==0)
                    {
                        users.inEngGroup=true;
                        ps.println("QuitGroup#"+"英语"+"#"+users.name);
//                        for (Users u:uList) {
//                            if (u.name.equals(users.name)) {
//                                u.inEngGroup = true;
//                            }
//                        }
                        try {
                            new InterestGroup("英语",users);
                        }catch (Exception ex){}

                    }
                }
                else {
                    try{
                        new InterestGroup("英语",users);
                    }catch (Exception ex){}
                }
            }
        });
        jPanel_interest.add(jb_Englishgroup);


        //左边的按钮
        this.add(friendList(),BorderLayout.WEST);


        //jtf.addActionListener((ActionListener)this);
        jtf.addActionListener(this);

        this.setLocation(200,200);
        this.setSize(550,600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);

        jtf.setFont(new Font("黑体",Font.BOLD,20));
        nickName=u.name;
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
                System.out.println("窗口关闭！");
            }
            @Override
            public void windowClosed(WindowEvent e){
                ps.println("SIGNOUT#"+nickName);
                System.out.println("窗口关闭！");
            }
        });
    }

    /**这里是针对Runnable的一个线程函数，用于打印从服务器端得到的信息*/
    public void run(){
        while(true){
            try{
                String str=br.readLine();
                if(str.startsWith("LOGIN#")){
                    String[] strs=str.split("#");
                    userlist.removeAll();
                    for(int i=1;i<strs.length;i++){
                        userlist.add(strs[i]);
                    }
                }
                else if(str.startsWith("msgto#")){
                    String[] str_list=str.split("#");
                    if(str_list[1].equals(users.name)||str_list[3].equals(users.name)){
                        jta.setSelectedTextColor(Color.red);
                        jta.select(0,10);
                        jta.append(str_list[3]+" says to "+str_list[1]
                                +":"+str_list[2]+'\n');
                    }
                }
                else if(str.startsWith("forcedlogout#")){
                    String[] strs_list=str.split("#");
                    if(strs_list[1].equals(users.name)){
                        this.dispose();
                        System.exit(0);
                    }
                }
                else if (str.startsWith("QuitGroup#")){
                    String[] str_qt=str.split("#");
                    for (Users u:uList){
                        if (u.name.equals(str_qt[2])&&str_qt[1].equals("英语")){
                            u.inEngGroup=true;
                        }
                        else if(u.name.equals(str_qt[2])&&str_qt[1].equals("数学")){
                            u.inMathGroup=true;
                        }
                    }
                }

                else{
                    jta.append(str+'\n');
                }
            }catch (Exception e){}
        }
    }

    public void actionPerformed(ActionEvent e) {
        //这是点击左边的好友按钮的事件
        if(e.getSource()!=jtf) {
            jtf.setText(null);
            JButton jb = (JButton) e.getSource();
            jtf.setText("msgto#"+jb.getText()+"#:");
        }
        else{
            if(jtf.getText().startsWith("msgto#")){
                //到时候分割的话[1]是私聊对象，[3]是发送者
                ps.println(jtf.getText()+"#"+users.name);
            }
            else {
                ps.println(nickName + " says:" + jtf.getText());
            }
            jtf.setText(null);  /**实现按Enter发送后清空输入框*/
        }
    }

    public static void main(String[] args) throws Exception{
        //new client1().setVisible(true);
        new users_init();
        //new InterestGroup("数学",users);
        new Announce();
    }

}
