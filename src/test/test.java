package test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Printer implements ActionListener {          //ActionListener是一个接口，必须被重写函数
    public void actionPerformed(ActionEvent e){ //这里的函数名不能变
        System.out.println("Hello");            //这里是事件处理函数
    }
}

class MyFrame1 extends JFrame {
    private JButton jbt = new JButton("打印");
    private JPanel  jpl = new JPanel();
    public MyFrame1(){
        Printer p = new Printer();
        jbt.addActionListener(p);          //绑定:必须将事件的发出者和事件的处理者
        //对象绑定起来
        jpl.add(jbt);
        this.add(jpl);
        this.setSize(400,300);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}



public class test {
    public static void main(String[] args) {
        new MyFrame1();
    }
}
