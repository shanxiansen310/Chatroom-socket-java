package Client;

import java.util.ArrayList;
class Users{
    public static ArrayList <Users> uList=new ArrayList<Users>();

    String name;
    String password;   //采用default就可以保护了
    ArrayList <String> myFriend=new ArrayList<String>();
    boolean inMathGroup=false;
    boolean inEngGroup=false;
}

public class users_init {
    private static boolean isCall=false;

    public users_init(){
        if(!isCall){
            Users u1=new Users();
            u1.name="Shan"; u1.password="123";
            u1.myFriend.add("He");  u1.myFriend.add("Khan"); u1.myFriend.add("Vicious");
            u1.inEngGroup=true; u1.inMathGroup=true;

            Users u2=new Users();
            u2.name="He"; u2.password="123";
            u2.myFriend.add("Shan"); u2.myFriend.add("Khan"); u2.myFriend.add("Vicious");
            u2.inEngGroup=true;

            Users u3=new Users();
            u3.name="Khan"; u3.password="123";
            u3.myFriend.add("Shan"); u3.myFriend.add("He"); u3.myFriend.add("Vicious");
            u3.inEngGroup=true; u3.inMathGroup=true;

            Users u4=new Users();
            u4.name="Vicious"; u4.password="123";
            u4.myFriend.add("Shan"); u4.myFriend.add("He"); u4.myFriend.add("Khan");
            u4.inMathGroup=true;

            Users.uList.add(u1);
            Users.uList.add(u2);
            Users.uList.add(u3);
            Users.uList.add(u4);

            for(Users o:Users.uList){
                System.out.println(o.name);
            }
        }
        isCall=true;
    }
    //ArrayList <Users> uList=new ArrayList<Users>();
    public static void main(String[] args) throws Exception {
        new users_init();
    }
}
