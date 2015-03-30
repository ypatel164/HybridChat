import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class SignUp extends  JFrame{
    private JTextField usernameInput;
    private JButton createAccountButton;
    private JTextArea message;
    private JPasswordField passwordInput;
    private JPanel username;
    private JLabel password;
    public JPanel mainPanel;

    public SignUp(final Driver driver,final PeerWindow pw) {
        final SignUp win=this;

        createAccountButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String username=usernameInput.getText();
                char[] pass=passwordInput.getPassword();
                String password=new String(pass);
                try {
                    driver.toServer.writeInt(1);  //request for signup
                    driver.toServer.writeUTF(username);
                    driver.toServer.writeUTF(password);
                    boolean res=driver.fromServer.readBoolean();
                    if(res){

                        pw.accountCreated("Account of " + username+ " created Successfully !");
                        win.dispose();

                    }
                    else{

                        message.setText(" Account with username "+username +" already exists :(");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }
}
