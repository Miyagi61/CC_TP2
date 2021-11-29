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

class Cabecalho implements Serializable {
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

    Cabecalho(byte tipo, int length, int seq, byte[] key){
        this.key = key;
        this.length = length;
        this.seq=seq;
        this.tipo = tipo;
    }

    Cabecalho(Cabecalho cb){
        this.key = cb.getKey();
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

    byte[] getKey(){
        return this.key;
    }

    byte getTipo() { return this.tipo; }

    void serialize(DataOutputStream out) throws IOException {
        out.write(tipo);
        out.writeInt(length);
        out.writeInt(seq);
        out.write(key);
    }

    Cabecalho deserialize(DataInputStream in) throws IOException {
        byte b = in.readByte();
        int comp = in.readInt();
        int sq = in.readInt();
        byte[] k = in.readNBytes(6);
        return new Cabecalho(b,comp,sq,k);
    }
}

class ListarFicheiro {
    HashMap<String,Double> list;

    void getListaFicheiro(String dir){
        File[] listaF = new File(dir).listFiles();

        for(File l : listaF){
            if(!l.isDirectory()){
                String nome = l.getName();
                Double size = (double)l.length();
                list.put(nome,size);
            }
        }
    }

    void serialize(DataOutputStream out) throws IOException {
        for(Map.Entry<String,Double> file : list.entrySet()){
            out.writeUTF(file.getKey());
            out.writeUTF("#");
            out.writeDouble(file.getValue());
            out.writeUTF("#");
        }
    }

    Map<String,Double> deserialize(DataInputStream in) {
        return  null;
    }


}

