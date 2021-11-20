import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;

public class udp_client
{
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

        // isBound() method
        System.out.println("IsBound : " + socket.isBound());

        // isConnected() method
        System.out.println("isConnected : " + socket.isConnected());

        // getInetAddress() method
        System.out.println("InetAddress : " + socket.getInetAddress());

        // getPort() method
        System.out.println("Port : " + socket.getPort());

        // getRemoteSocketAddress() method
        System.out.println("Remote socket address : " +
                socket.getRemoteSocketAddress());

        // getLocalSocketAddress() method
        System.out.println("Local socket address : " +
                socket.getLocalSocketAddress());

        // send() method
        socket.send(dp);
        System.out.println("...packet sent successfully....");

        // receive() method
        socket.receive(dptorec);
        System.out.println("Received packet data : " +
                new String(dptorec.getData()));

        // getLocalPort() method
        System.out.println("Local Port : " + socket.getLocalPort());

        // getLocalAddress() method
        System.out.println("Local Address : " + socket.getLocalAddress());

        // setSOTimeout() method
        socket.setSoTimeout(50);

        // getSOTimeout() method
        System.out.println("SO Timeout : " + socket.getSoTimeout());
    }

}
