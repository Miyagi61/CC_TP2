import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

        // getSOTimeout() method
        System.out.println("SO Timeout : " + socket.getSoTimeout());
    */
    Cabecalho c = new Cabecalho(800, (byte) 0);
    }

}

class DynamicByteArray
{
    private List<byte[]> L_array;
    private int count;
    private int sizeofarray;
    private int byteSize;
    //creating a constructor of the class that initializes the values
    public DynamicByteArray(int byteSize)
    {
        L_array = new ArrayList<>();
        count = 0;
        sizeofarray = 1;
        this.byteSize = byteSize;
    }
    //creating a function that appends an element at the end of the array
    public void addElement()
    {
        Random rs = new Random();
        byte[] array = new byte[this.byteSize];
        rs.nextBytes(array);
        L_array.add(array);
        count++;
    }
}

class Socket{
    Socket(int port) throws IOException{
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getLocalHost();

    }
}

class Cabecalho{
    byte tipo;
    int length;
    int seq;
    byte[] key;


    Cabecalho(int length, byte tipo){
        Random rn = new Random();
        this.key = new byte[6];
        rn.nextBytes(this.key);
        this.length = length;
        this.seq=0;
        this.tipo = tipo;
    }

    Cabecalho(Cabecalho cb){
        this.key = cb.getKey();
        this.length = cb.getLength();
        this.seq = cb.getSeq();
    }

    int getLength(){
        return this.length;
    }

    int getSeq(){
        return this.seq;
    }

    byte[] getKey(){
        return this.key;
    }

    byte getTipo() { return this.tipo; }
}
