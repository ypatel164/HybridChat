import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class Driver {
    public Socket server;
    DataInputStream fromServer;
    DataOutputStream toServer;


    Driver(Socket server,DataInputStream fromServer,DataOutputStream toServer){
        this.server=server;
        this.fromServer=fromServer;
        this.toServer=toServer;



    }
}
