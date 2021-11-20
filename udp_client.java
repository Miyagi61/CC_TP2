import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

public class udp_client
{
    class Socket{
        Socket(int port) throws IOException {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getLocalHost();
        }
    }
    class Cabecalho{
        int length;
        int seq;
        byte key;
        Cabecalho(int length, byte key){
            this.key = key;
            this.length = length;
            this.seq=0;
        }
        Cabecalho(int length, byte key, int seq){
            this.key = key;
            this.length = length;
            this.seq=seq;
        }


    }
    public static void main(String[] args) throws IOException
    {
        // Constructor to create a datagram socket
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getLocalHost();
        int port = 5252;
        String inputString = "HELLO";
        byte[] buf = inputString.getBytes();
        byte[] buf1 = new byte[5];
        DatagramPacket dp = new DatagramPacket(buf, 5, address, port);
        DatagramPacket dptorec = new DatagramPacket(buf1, 5);

        // connect() method
        socket.connect(address, port);

        // send() method
        socket.send(dp);
        System.out.println("...packet sent successfully....");

        // receive() method
        socket.receive(dptorec);
        System.out.println("Received packet data : " +
                new String(dptorec.getData()));

        // setSOTimeout() method
        socket.setSoTimeout(50);

        // getSOTimeout() method
        System.out.println("SO Timeout : " + socket.getSoTimeout());
    }

}
