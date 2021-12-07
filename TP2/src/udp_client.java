import org.codehaus.groovy.util.ByteArrayIterator;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class udp_client
{
    public static void main(String[] args) throws IOException
    {
        /*   // Constructor to create a datagram socket
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
/home/miyagi/Desktop
        // getSOTimeout() method
        System.out.println("SO Timeout : " + socket.getSoTimeout());
    */
        String pasta = "/home/miyagi/Desktop";
        InetAddress ip = InetAddress.getLocalHost();
        DatagramSocket cs = new DatagramSocket();
        Cabecalho c =  new Cabecalho((byte) 1,-1,123);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bao);
        c.serialize(out);
        out.writeUTF(pasta);
        DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),ip,5252);
        cs.send(pastaP);

        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        cs.receive(dp);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
        ListarFicheiro lf = new ListarFicheiro(pasta);
        lf.carregarDP(in,0);

   }

}





