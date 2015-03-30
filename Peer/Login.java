import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Login extends JFrame {
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JButton loginButton;
    private JTextArea Message;
    private JLabel username;
    private JLabel password;
    public JPanel mainPanel;
    Socket client;

    public Login(final Driver driver,final PeerWindow pw) {

        //this.client=client;
        final Login win=this;

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username=usernameInput.getText();
                char[] pass=passwordInput.getPassword();
                String password=new String(pass);
                try {
                    driver.toServer.writeInt(2);  //request for logIn
                    driver.toServer.writeUTF(username);
                    driver.toServer.writeUTF(password);


                     boolean res=driver.fromServer.readBoolean();
                    if(res){
                       // driver.isLogged=true;

                        Message.setText("Connected Successfully ");
                        pw.setLoggedIn(username);
                        win.dispose();

                    }
                    else{
                        //driver.isLogged=false;
                        Message.setText(" Either username or password Incorrect");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }



            }
        });
    }
}
