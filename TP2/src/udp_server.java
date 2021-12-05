import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class udp_server {
    public static void main (String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(5252);
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);

        ds.receive(dp);

        ListarFicheiro lf = new ListarFicheiro(dp);



       /* DatagramPacket senddp = new DatagramPacket(send, 5,
                dp.getAddress(), dp.getPort());

        DatagramSocket socket = new DatagramSocket();

        socket.connect(dp.getAddress(), dp.getPort());
        socket.send(senddp);*/
    }
}
