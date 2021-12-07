import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class udp_server {
    public static void main (String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(5252);
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);

        ds.receive(dp);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
        Cabecalho c = new Cabecalho(din);
        ListarFicheiro lf = new ListarFicheiro(din);
        din.close();
        lf.atualizaListaFicheiro();

        byte[] list_files = lf.upSerialize();
        DatagramPacket newP = new DatagramPacket(list_files, list_files.length,dp.getSocketAddress());
        ds.send(newP);

    }
}

