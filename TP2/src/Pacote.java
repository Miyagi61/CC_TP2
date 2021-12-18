import java.io.*;
import java.net.*;
import java.util.List;

public class Pacote {

    static void enviaPacoteErro(DatagramSocket ds, int seq, SocketAddress sa) throws IOException {
        Cabecalho c = new Cabecalho((byte)5,seq,0);
        byte[] res = c.outputToByte();
        DatagramPacket newP = new DatagramPacket(res, res.length,sa);
        ds.send(newP);
    }

    static void pedeListaFicheiros(DatagramSocket ds, Cabecalho c, InetAddress ip) throws IOException{
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bao);
        c.serialize(out);
        DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),ip,5252);
        ds.send(pastaP);
    }

    static void enviaPacoteListaFicheiros(DatagramSocket ds, ByteManager list_files, int seq, SocketAddress sa) throws IOException {
        byte[] aux = list_files.getL_array(seq-1);
        Cabecalho c = new Cabecalho((byte)0,seq, list_files.getCount());
        byte[] res = ByteManager.concatByteArray(c.outputToByte(),aux);
        DatagramPacket newP = new DatagramPacket(res, res.length,sa);
        ds.send(newP);
    }

    static Cabecalho recebePacoteListaFicheiros(DatagramSocket cs,ListarFicheiro lf, int tipo) throws IOException {
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        cs.receive(dp);
        lf.origem = dp.getSocketAddress();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
        Cabecalho c = new Cabecalho(in);
        if(checkErro(c)){
            return null;
        }
        lf.carregarDP(in, tipo);

        return c; // get tamanho
    }

    static void pedePacotesEmFaltaLF(DatagramSocket cs, List<Integer> seqs, SocketAddress sa, int size) throws IOException{
        for(int i = 1; i <= size; i++){
            if(!seqs.contains(i)){
                Cabecalho c = new Cabecalho((byte)4, i,size); // NOACKL
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bao);
                c.serialize(out);
                DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),sa);
                cs.send(pastaP);
            }
        }
    }

    static void trataACKL(DatagramSocket ds, ByteManager bm ) throws IOException{
        Par<Cabecalho,SocketAddress> info = receive(ds);
        Pacote.enviaPacoteListaFicheiros(ds,bm,info.getFst().getSeq(),info.getSnd());
    }

    static Par<Cabecalho,SocketAddress> receive(DatagramSocket ds) throws IOException{
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        ds.setSoTimeout(100); // timeout é importante
        ds.receive(dp);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
        return new Par<>(new Cabecalho(din),dp.getSocketAddress());
    }

    static Triplo<Cabecalho,SocketAddress,DataInputStream> receiveDIN(DatagramSocket ds) throws IOException{
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        ds.setSoTimeout(100); // timeout é importante
        ds.receive(dp);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
        return new Triplo<>(new Cabecalho(din),dp.getSocketAddress(),din);
    }

    private static boolean checkErro(Cabecalho c){
        if(c.getTipo() != 5){
            return false;
        }
        else
        switch (c.getSeq()){
            case 0 : System.out.println("Passowrd errada"); break;
        }
        return true;
    }
}
