import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Sairam on 2/25/2015.
 */
public class PeerWindow extends JFrame {
    private JButton signUpButton;
    private JTextField displayMessage;
    private JButton loginButton;
    private JEditorPane displayConversations;
    private JButton connectToThemButton;
    private JTextField sendMessage;
    private JButton sendMessageToHim;
    private JPanel displayFriends;
    public JPanel mainPanel;
    private JList chatFriends;
    private JList totalUsersOnDB;
    private JList totalFriends;
    private JButton addAsFriendButton;
    private JButton refreshButton;
    private JButton refreshButton1;
    private JButton makeGroupButton;
    private Socket client;
    private DataInputStream fromServer = null;
    private DataOutputStream toServer = null;
    boolean isLoggedIn = false;
    String username;
    HashMap<String, Socket> ipToSocket = new HashMap<String, Socket>();
    HashMap<String, String> nameToIp = new HashMap<String, String>();
    HashMap<String, ArrayList<String>> groupToName = new HashMap<String, ArrayList<String>>();


    public PeerWindow(final Socket client, final DataInputStream fromServer, final DataOutputStream toServer, ServerSocket server, final int port2, final int diff) {

        setButton(false);
        final Driver driver = new Driver(client, fromServer, toServer);
        this.client = client;
        this.fromServer = fromServer;
        this.toServer = toServer;
        final PeerWindow pw = this;
        updateUsersList();
        new _Server(server, ipToSocket, nameToIp, displayConversations, chatFriends, displayMessage, this).start();
        totalFriends.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        totalUsersOnDB.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loginButton.getText() == "Login") {
                    // String ok = new String();
                    Login login = new Login(driver, pw);
                    login.setSize(300, 300);
                    login.setContentPane(login.mainPanel);
                    login.setLocationRelativeTo(null);
                    login.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    login.setVisible(true);
                } else {
                    //
                    isLoggedIn = false;
                    try {
                        toServer.writeInt(4);
                        pw.displayMessage.setText(username + " LoggedOut Successfully!");
                        loginButton.setText("Login");
                        setButton(false);


                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }


            }
        });
        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignUp signUp = new SignUp(driver, pw);
                signUp.setSize(300, 300);
                signUp.setContentPane(signUp.mainPanel);
                signUp.setLocationRelativeTo(null);
                signUp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                signUp.setTitle("Create Account");
                signUp.setVisible(true);

            }
        });
        addAsFriendButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String friend = (String) totalUsersOnDB.getSelectedValue();
                    toServer.writeInt(7);
                    toServer.writeUTF(friend);
                    if (fromServer.readBoolean()) {
                        //added friend
                        updateTotalFriendsList();
                        displayMessage.setText(friend + " is added as your friend !");
                    } else {
                        if (!fromServer.readBoolean()) {
                            displayMessage.setText(friend + " is already your friend !");

                        } else {
                            displayMessage.setText(" U can't add yourself as friend !'");

                        }

                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        connectToThemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String friend = (String) totalFriends.getSelectedValue();
                String details[] = friend.split("\\(");
                if (details[1].charAt(0) == 'O') {
                    displayMessage.setText(details[0] + " is offline now !");

                } else {
                    String ip = "";
                    for (int i = 0; i < details[1].length() - 1; i++) {
                        ip += Character.toString(details[1].charAt(i));
                    }
                    System.out.println("connecting to " + ip + " at port " + (8001));
                    try {

                        Socket c = new Socket(ip, 8001); //
                        new PureClient(c, details[0]).start();

                        DataOutputStream _toServer = new DataOutputStream(c.getOutputStream());
                        _toServer.writeBoolean(false); //not group
                        _toServer.writeUTF(username);  //send the username to server
                        // System.out.println("Connected ! Sent  username ");

                        nameToIp.put(details[0], ip);
                        ipToSocket.put(ip,c );
                        updateChatFriendsList();


                        displayMessage.setText("Connected to " + details[0] + " successfully !");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }


            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTotalFriendsList();
            }
        });
        sendMessageToHim.addActionListener(new ActionListener() { //multiple guys send .group chat
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indices = chatFriends.getSelectedIndices();

                for (int i = 0; i < indices.length; i++) {
                    String name = (String) chatFriends.getModel().getElementAt(indices[i]);
                    System.out.println("Name : " + name);
                    if (!name.contains(":->")) { //not a group
                        Socket socket = ipToSocket.get(nameToIp.get(name));
                        String _txt = sendMessage.getText();
                        if (socket.isConnected() && !socket.isOutputShutdown()) {

                            try {
                                DataOutputStream _toServer = new DataOutputStream(socket.getOutputStream());
                                _toServer.writeBoolean(false); //not a group message
                                _toServer.writeUTF(_txt);
                                System.out.println("Data Sent :) to " + name);
                                displayConversations.setText(displayConversations.getText() + "Me to " + name + " :-> " + _txt + "\n");
                                sendMessage.setText("");
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                        } else {
                            displayMessage.setText("Message can't be sent to " + name + " coz He is offline !");
                            displayConversations.setText(displayConversations.getText() + "Me to " + name + " :-> " + _txt + " ( NOT SENT)  \n");
                            sendMessage.setText("");
                        }

                    } else { //This name is group
                        String[] tmp=name.split(">");
                        String groupName=tmp[1];

                        ArrayList<String> members=groupToName.get(groupName);
                        for(int j=0;j<members.size();j++){
                            Socket s=ipToSocket.get(nameToIp.get(members.get(j)));
                            try {
                                DataOutputStream out=new DataOutputStream(s.getOutputStream());
                                out.writeBoolean(true);
                                out.writeUTF(groupName);
                                out.writeUTF(username);
                                out.writeUTF(sendMessage.getText());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                        }
                        displayConversations.setText(displayConversations.getText() + "Me to Group " + groupName + " :-> " + sendMessage.getText() + "\n");
                        sendMessage.setText("");



                    }
                }
            }
        });
        refreshButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUsersList();
            }
        });
        makeGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int[] cnt = chatFriends.getSelectedIndices();
                if(cnt.length==0){
                    displayMessage.setText("Please select atleast one guy to group chat !");
                    return;
                }
                ArrayList<String> g = new ArrayList<String>();
                String msg = "";
                for (int i = 0; i < cnt.length; i++) {
                    g.add((String) chatFriends.getModel().getElementAt(cnt[i]));
                    msg += ", " + (String) chatFriends.getModel().getElementAt(cnt[i]);

                }
                msg = "Enter the name of group of" + msg;
                String name = JOptionPane.showInputDialog(msg);
                //Socket s=new Socket()
                for (int i=0;i<g.size();i++){
                    try {
                        Socket s=new Socket(nameToIp.get(g.get(i)),8001);
                        DataOutputStream out=new DataOutputStream(s.getOutputStream());
                        out.writeBoolean(true);
                        out.writeUTF(name);
                        out.writeUTF(username);
                        toServer.writeInt(8);
                        out.writeUTF(fromServer.readUTF());
                        out.writeInt(g.size());
                        for(int j=0;j<g.size();j++){
                            out.writeUTF(g.get(j));
                            out.writeUTF(nameToIp.get(g.get(j)));
                        }
                        out.writeUTF(username);

                        out.close();
                        s.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }


                groupToName.put(name, g);
                updateChatFriendsList();
            }
        });
    }

    private void updateChatFriendsList() {
        DefaultListModel listModel = new DefaultListModel();
        Iterator it = nameToIp.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) it.next();
            listModel.addElement(entry.getKey());

        }
        it = groupToName.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) it.next();
            listModel.addElement("Group:->" + entry.getKey());

        }
        chatFriends.setModel(listModel);
    }

    public void updateTotalFriendsList() {
        try {
            toServer.writeInt(3); //get Online First
            int count = fromServer.readInt();
            DefaultListModel<String> listModel = new DefaultListModel<String>();
            while (count-- != 0) {
                //totalFriends.
                listModel.addElement(fromServer.readUTF() + "(" + fromServer.readUTF() + ")");

            }

            toServer.writeInt(6);  // NOT online friends!
            count = fromServer.readInt();

            while (count-- != 0) {
                //totalFriends.
                listModel.addElement(fromServer.readUTF() + "(OFFLINE)");

            }
            totalFriends.setModel(listModel);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateUsersList() {
        try {
            toServer.writeInt(5);
            int count = fromServer.readInt();
            DefaultListModel<String> listModel = new DefaultListModel<String>();
            while (count-- != 0) {
                //totalFriends.
                listModel.addElement(fromServer.readUTF());

            }
            totalUsersOnDB.setModel(listModel);
            //limit to select only one user at a time


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void setLoggedIn(String username) {
        displayMessage.setText("You have Logged in as : " + username);
        this.username = username;
        // isLoggedIn=true;
        loginButton.setText(username + " wanna Logout? ");
        setButton(true);


        //this.setTitle(username+"'s chat ");

    }

    public void accountCreated(String message) {
        displayMessage.setText(message);
        updateUsersList();

    }


    public void setButton(Boolean isActive) {
        isLoggedIn = isActive;
        addAsFriendButton.setEnabled(isActive);
        connectToThemButton.setEnabled(isActive);
        sendMessageToHim.setEnabled(isActive);
        refreshButton.setEnabled(isActive);
        refreshButton1.setEnabled(isActive);
        signUpButton.setEnabled(!isActive);

        if (isActive) {
            //logged in
            updateTotalFriendsList();
            // updateOnlineFriendsList();
        } else {
            //loggeed out
            totalFriends.setModel(new DefaultListModel());
            chatFriends.setModel(new DefaultListModel());

        }


    }

    public static class _Server extends Thread {
        ServerSocket server;
        HashMap<String, Socket> ipToSocket;
        HashMap<String, String> nameToIp;
        JEditorPane displayConversations;
        JList chatFriends;
        JTextField displayMessage;
        PeerWindow peerWindow;

        _Server(ServerSocket server, HashMap<String, Socket> ipToSocket, HashMap<String, String> nameToIp, JEditorPane displayConversations, JList chatFriends, JTextField displayMessage, PeerWindow peerWindow) {
            this.server = server;
            this.ipToSocket = ipToSocket;
            this.nameToIp = nameToIp;
            this.displayConversations = displayConversations;
            this.chatFriends = chatFriends;
            this.displayMessage = displayMessage;
            //System.out.println("Into server ..");
            this.peerWindow = peerWindow;


        }

        public void run() {
            while (true) {
                try {
                    System.out.println("waiting for clients..");
                    Socket client = server.accept(); //
                    System.out.println(client.getInetAddress().getHostAddress() + " : got connected !");

                    new _Client(client, ipToSocket, nameToIp, displayConversations, chatFriends, displayMessage, peerWindow).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class _Client extends Thread {
        Socket client;
        HashMap<String, Socket> ipToSocket;
        HashMap<String, String> nameToIp;
        DataInputStream fromServer;
        JEditorPane displayConversations;
        JList chatFriends;
        JTextField displayMessage;
        PeerWindow peerWindow;

        _Client(Socket client, HashMap<String, Socket> ipToSocket, HashMap<String, String> nameToIp, JEditorPane displayConversations, JList chatFriends, JTextField displayMessage, PeerWindow peerWindow) {
            this.client = client;
            this.ipToSocket = ipToSocket;
            this.nameToIp = nameToIp;
            this.displayConversations = displayConversations;
            this.chatFriends = chatFriends;
            this.displayMessage = displayMessage;
            this.peerWindow = peerWindow;
            try {
                fromServer = new DataInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {
                String ip = client.getInetAddress().getHostAddress();
                boolean isGroup = fromServer.readBoolean();
                if (!isGroup) {
                    String username = fromServer.readUTF();
                    ipToSocket.put(ip, client);
                    nameToIp.put(username, ip);
                    peerWindow.updateChatFriendsList();
                    displayMessage.setText("Connection recieved from : " + username + " ! Happy chatting :)");
                    while (client.isConnected()) {
                        boolean isGroupMessage=fromServer.readBoolean();
                        String txt=displayConversations.getText();
                        if(!isGroupMessage) {
                            String msg = fromServer.readUTF();
                            txt=txt+ username + " to me :-> " + msg + " \n ";

                        }
                        else {
                            String groupName=fromServer.readUTF();
                            String sender=fromServer.readUTF();
                            String message=fromServer.readUTF();
                            txt=txt+sender +" to "+groupName+":-> "+message+ "\n";



                        }
                        displayConversations.setText(txt);


                    }
                } else {
                    String groupName = fromServer.readUTF();
                    peerWindow.groupToName.put(groupName, new ArrayList<String>());
                    String n=fromServer.readUTF();
                    String ipTmp=fromServer.readUTF();


                    peerWindow.groupToName.get(groupName).add(n);

                    peerWindow.nameToIp.put(n, ipTmp);
                    if (!peerWindow.ipToSocket.containsKey(ipTmp)) {
                        Socket tmp = new Socket(ipTmp, 8001);
                        DataOutputStream out = new DataOutputStream(tmp.getOutputStream());
                        out.writeBoolean(false);
                        out.writeUTF(peerWindow.username);
                        peerWindow.ipToSocket.put(ipTmp, tmp);
                    }
                    System.out.println(n + "is connected ");
                    int cnt = fromServer.readInt();
                    for (int i = 0; i < cnt; i++) {
                        n = fromServer.readUTF();
                        ipTmp = fromServer.readUTF();
                        if(n.equals(peerWindow.username)){
                            continue;
                        }
                        peerWindow.groupToName.get(groupName).add(n);

                        peerWindow.nameToIp.put(n, ipTmp);
                        if (!peerWindow.ipToSocket.containsKey(ipTmp)) {
                            Socket tmp = new Socket(ipTmp, 8001);
                            DataOutputStream out = new DataOutputStream(tmp.getOutputStream());
                            out.writeBoolean(false);
                            out.writeUTF(peerWindow.username);
                            peerWindow.ipToSocket.put(ipTmp, tmp);
                        }
                        System.out.println(n + "is connected ");
                    }
                    displayMessage.setText("Connection recieved from GROUP : " + groupName + " ! Happy chatting :)");
                    peerWindow.updateChatFriendsList();

                }
            } catch (IOException e) {
                e.printStackTrace();
                //System.out.println("client : "+client.getInetAddress().getHostAddress());
            }

        }
    }

    private class PureClient extends Thread {
        Socket client;
        String username;
        DataInputStream fromServer;

        public PureClient(Socket client, String username) {
            this.client = client;
            this.username = username;
            try {
                fromServer = new DataInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {
                while (client.isConnected()) {
                    boolean isGroupMessage=fromServer.readBoolean();
                    String txt=displayConversations.getText();
                    if(!isGroupMessage) {
                        String msg = fromServer.readUTF();
                        txt=txt+ username + " to me :-> " + msg + " \n ";

                    }
                    else {
                        String groupName=fromServer.readUTF();
                        String sender=fromServer.readUTF();
                        String message=fromServer.readUTF();
                        txt=txt+sender +" to "+groupName+":-> "+message+ "\n";



                    }
                    displayConversations.setText(txt);


                }
            } catch (IOException ex) {
                System.out.println(username + " has been Disconnected !");
            }


        }
    }
}

