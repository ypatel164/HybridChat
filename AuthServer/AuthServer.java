
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.sql.*;

public class AuthServer {
    public static int port = 8000;
    public static Queue<Client> QClient = new LinkedList<Client>();
    public static Connection c = null;
    public static Statement stmt = null;

    public static void main(String[] args) {

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:dataBase.db");
            stmt = c.createStatement();
            String sql="CREATE  TABLE  IF NOT EXISTS user"+
                    " (ID int IDENTITY(1,1) NOT NULL," +
                    " username varchar(50) NOT NULL ," +
                    "password varchar(50) NOT NULL," +

                    "isActive  boolean NOT NULL," +
                    "IP varchar(50) NULL, " +
                    "PRIMARY KEY(ID))";
            stmt.executeUpdate(sql);
            sql="CREATE TABLE IF NOT EXISTS  friend" +
                    "(ID int IDENTITY(1,1) NOT NULL," +
                    "U_ID int NOT NULL," +

                    "P_ID int NOT NULL," +
                    "PRIMARY KEY(ID)" +
                    "FOREIGN KEY(P_ID) REFERENCES user(ID))";
            stmt.executeUpdate(sql);

            ServerSocket server = new ServerSocket(port);
            System.out.println("Server has started at port : " + port);
            while (true) {
                Socket client = server.accept();
                System.out.println(client.getInetAddress().getHostAddress() + " : " + " is connected !");
                QClient.add(new Client(client));
                new Client(client).start();

            }


        } catch (IOException ex) {
            System.out.println("Server can't start at port " + port);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //close stmt and Connection


    }

    public static class Client extends Thread {
        Socket client;
        DataInputStream fromClient;
        DataOutputStream toClient;
        int idOfUser;

        Client(Socket client) throws IOException {
            this.client = client;
            fromClient = new DataInputStream(client.getInputStream());
            toClient = new DataOutputStream(client.getOutputStream());


        }

        public void run() {
            while (client.isConnected()) {
               // System.out.println(client.isClosed());

                try {
                    int choice = fromClient.readInt();
                    if (choice == 1) { //SighNup
                        String username = fromClient.readUTF();
                        String password = fromClient.readUTF();
                        String sql;
                        sql = "SELECT COUNT(*) AS cnt  FROM user WHERE username='" + username + "';";
                        ResultSet rs = stmt.executeQuery(sql);
                        if (rs.next()) {  // Do i need this if stmt
                            int cnt = rs.getInt("cnt");
                            if (cnt != 0)
                                toClient.writeBoolean(false);
                            else {
                                sql = "INSERT INTO user " +
                                        "values((select ifnull(max(ID),0) from user)+1,'" + username + "','" + password + "',0,null); ";
                                stmt.executeUpdate(sql);
                                toClient.writeBoolean(true);

                            }

                        } else {


                        }
                        rs.close();
                    } else if (choice == 2) {  //login
                        String username = fromClient.readUTF();
                        String password = fromClient.readUTF();
                        String ipAddress = client.getInetAddress().getHostAddress();
                        String sql;
                        sql = "SELECT * from user WHERE username='" + username + "' and password='" + password + "'; ";
                        ResultSet rs = stmt.executeQuery(sql);
                        if (rs.next()) {  // The login credentials are correct !
                            int id = rs.getInt("ID");
                            idOfUser=id;
                            //Update the login field of that user
                            sql = "UPDATE user SET isActive=1,IP='" + ipAddress + "' where ID=" + id + ";";
                            stmt.executeUpdate(sql);
                            //now return his friends list
                            //sql = "SELECT user.username,user.ipAddress from user,friend where friend.P_id=id and user.id=id and user.isActive=true";

                            System.out.println("client : "+ipAddress +" : got logged in ");
                            toClient.writeBoolean(true);
                           // toClient.writeUTF(username);
                        } else { //login credentials are Incorrect
                            toClient.writeBoolean(false);
                        }
                        rs.close();


                    } else if (choice == 3) { //get online friends list
                        //String username=from
                        String sql;
                        //now return his friends list
                        sql = "SELECT username,IP from user,friend where friend.U_ID="+idOfUser+" and friend.P_ID =user.ID and user.isActive=1";

                        ResultSet friendsRS = stmt.executeQuery(sql);
                        ArrayList<String> names = new ArrayList<String>(); //usernames
                        ArrayList<String> ip = new ArrayList<String>(); //Ip addresses
                        while (friendsRS.next()) {
                            names.add(friendsRS.getString("username"));
                            ip.add(friendsRS.getString("IP"));


                        }
                        friendsRS.close();

                        toClient.writeInt(names.size());  //Man u definatly need this
                        for (int i = 0; i < names.size(); i++) {
                            toClient.writeUTF(names.get(i));
                            toClient.writeUTF(ip.get(i));
                        }

                    }
                    else if(choice==4){  //logout

                        String sql;
                        sql="UPDATE user SET isActive=0 where ID="+idOfUser+";";
                        stmt.executeUpdate(sql);
                        System.out.println("client : "+client.getInetAddress().getHostAddress()+" : got Logged out !");
                        System.out.flush();

                    }
                    else if(choice==5){ //get all users
                        String sql="SELECT username from user";
                        ResultSet resultSet=stmt.executeQuery(sql);
                        ArrayList<String> lst=new ArrayList<String>();
                        while(resultSet.next()){
                            lst.add(resultSet.getString("username"));

                        }
                        toClient.writeInt(lst.size());
                        for(int i=0;i<lst.size();i++){
                            toClient.writeUTF(lst.get(i));

                        }

                    }
                    else if(choice==6){  //get Offline friends s

                        String sql="SELECT username from user,friend where user.ID=friend.P_ID and user.isActive=0 and friend.U_ID="+idOfUser+";";
                        ResultSet resultSet=stmt.executeQuery(sql);
                        ArrayList<String> lst=new ArrayList<String>();
                        while(resultSet.next()){
                            lst.add(resultSet.getString("username"));

                        }
                        toClient.writeInt(lst.size());
                        for(int i=0;i<lst.size();i++){
                            toClient.writeUTF(lst.get(i));

                        }

                    }
                    else if(choice==7){ //Add friend  ! one friend at each time
                        String friendName=fromClient.readUTF();
                        int idOfFriend;
                        String sql="SELECT count(*) AS cnt  from friend where U_ID="+idOfUser+" and P_ID= (SELECT ID from user where username='"+friendName+"');";
                       // sql="Select count(*) as cnt from friend;";
                        ResultSet resultSet=stmt.executeQuery(sql);
                        //System.out.println(resultSet.getInt("cnt"));
                        if(resultSet.getInt("cnt") == 0){
                            sql="Select ID from user where username='"+friendName+"';";
                            resultSet=stmt.executeQuery(sql);
                            if(resultSet.getInt("ID")==idOfUser){
                                toClient.writeBoolean(false);
                                toClient.writeBoolean(true);  //U can't add urselves as friend

                            }
                            else {
                                sql = "INSERT INTO friend VALUES ( (SELECT ifnull(MAX(ID),0) from friend)+1," + idOfUser + ",(SELECT ID from user where " +
                                        " username='" + friendName + "') );";
                                stmt.executeUpdate(sql);
                                toClient.writeBoolean(true);
                            }
                            resultSet.close();


                        }
                        else{
                            toClient.writeBoolean(false);
                            toClient.writeBoolean(false); //already exists as friend
                        }




                    }
                    else if(choice==8){

                        toClient.writeUTF(client.getInetAddress().getHostAddress());



                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    String sql;
                    sql="UPDATE user SET isActive=0 where ID="+idOfUser+";";
                    try {
                        stmt.executeUpdate(sql);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }


                    System.out.println("client "+client.getInetAddress().getHostAddress()+" is  disconnected ");
                    break;
                }
            }
        }
    }
}
