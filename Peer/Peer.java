import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Peer {
    public static String AuthServer;
    public static int port=8000;  //this port for auth server
    public  static int port2=8001; //this port for peers
    public static int diff=1;

    public static void main(String[] args) {
        if(port2==8002) diff=-1;
        DataInputStream fromServer=null;
        DataOutputStream toServer=null;
        try {
            //JOptionPane.showInputDialog()
            AuthServer= JOptionPane.showInputDialog("Enter the IP address of server : ");
            Socket client = new Socket(AuthServer, port);
            fromServer=new DataInputStream(client.getInputStream());
            toServer=new DataOutputStream(client.getOutputStream());
            System.out.println("Connected to Server !");
            ServerSocket server = new ServerSocket(port2); //running the server


            System.out.println("Server started at port : "+port2);
            PeerWindow peerWindow = new PeerWindow(client,fromServer,toServer,server,port2,diff);
            peerWindow.setSize(600,500);
           // peerWindow.pack();
            peerWindow.setLocationRelativeTo(null);
            peerWindow.setContentPane(peerWindow.mainPanel);
            peerWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            peerWindow.setTitle(" Chat Window ");
            peerWindow.setVisible(true);
          //  System.out.println("window displayed !");
        } catch (IOException e) {
            System.out.println("Host Address not found :( Try Again !!");
        }


    }

}
