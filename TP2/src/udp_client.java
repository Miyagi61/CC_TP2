import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        DatagramSocket cs = new DatagramSocket(12321);

        ListarFicheiro lf = new ListarFicheiro("../");
        lf.atualizaListaFicheiro();

        byte[] dp = lf.outputToByte();
        InetAddress ip = InetAddress.getLocalHost();
        DatagramPacket ps = new DatagramPacket(dp,dp.length,ip,5252);
        cs.send(ps);

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

class Cabecalho implements Serializable {
    byte tipo;
    int length;
    int seq;


    Cabecalho(int length, byte tipo){
        Random rn = new Random();
        this.length = length;
        this.seq=0;
        this.tipo = tipo;
    }

    Cabecalho(byte tipo, int length, int seq, byte[] key){
        this.length = length;
        this.seq=seq;
        this.tipo = tipo;
    }

    Cabecalho(Cabecalho cb){
        this.length = cb.getLength();
        this.seq = cb.getSeq();
        this.tipo = cb.getTipo();
    }

    int getLength(){
        return this.length;
    }

    int getSeq(){
        return this.seq;
    }

    byte getTipo() { return this.tipo; }

    void serialize(DataOutputStream out) throws IOException {
        out.write(tipo);
        out.writeInt(length);
        out.writeInt(seq);
    }

    Cabecalho deserialize(DataInputStream in) throws IOException {
        byte b = in.readByte();
        int comp = in.readInt();
        int sq = in.readInt();
        byte[] k = in.readNBytes(6);
        return new Cabecalho(b,comp,sq,k);
    }
}

