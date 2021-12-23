import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

public class Pacote {
    public static final int port = 80;
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
        DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),ip,port);
        ds.send(pastaP);
    }

    static void enviaPacoteFicheirosAReceber(DatagramSocket ds, ByteManager files, int seq, SocketAddress sa) throws  IOException{
        byte[] aux = files.getL_array(seq);
        Cabecalho c = new Cabecalho((byte)6,seq, files.getCount());
        byte[] res = ByteManager.concatByteArray(c.outputToByte(),aux);
        DatagramPacket newP = new DatagramPacket(res, res.length,sa);
        ds.send(newP);
    }

    static void enviaPacoteListaFicheiros(DatagramSocket ds, ByteManager list_files, int seq, SocketAddress sa) throws IOException {
        byte[] aux = list_files.getL_array(seq-1);
        Cabecalho c = new Cabecalho((byte)0,seq, list_files.getCount());
        byte[] res = ByteManager.concatByteArray(c.outputToByte(),aux);
        DatagramPacket newP = new DatagramPacket(res, res.length,sa);
        ds.send(newP);
    }

    static Triplo<Cabecalho,byte[],SocketAddress> recebePacoteDados(DatagramSocket ds) throws IOException {
        byte[] buffer=new byte[800];
        DatagramPacket dp = new DatagramPacket(buffer,800);
        ds.setSoTimeout(100);
        ds.receive(dp);
        ByteBuffer bb = ByteBuffer.wrap(dp.getData());

        byte tipo = bb.get();   //recolher os campos do cabeçalho do datagrampacket
        int seq = bb.getInt();
        int resto = bb.getInt(); //resto é a quantidade de bytes que o array ainda tem (dados)

        Cabecalho cb = new Cabecalho(tipo,seq,resto);
        if(resto <= 0)
            return null;
        byte[] pacote = new byte[resto];
        bb.get(pacote);
        return new Triplo<>(cb,pacote,dp.getSocketAddress());
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

    static Cabecalho recebePacoteFicheirosAEnviar(DatagramSocket cs, Set<String> send) throws IOException {
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        cs.receive(dp);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
        Cabecalho c = new Cabecalho(in);
        if(checkErro(c)){
            return null;
        }
        try{
            while(true) { // está a ler até dar EOF exception
                send.add(in.readUTF());
                in.readUTF(); // ler o " "
            }
        }catch (EOFException e){  }

        return c; // get tamanho
    }

    static void pedePacotesEmFalta(DatagramSocket cs, List<Integer> seqs, SocketAddress sa, int size, int tipo) throws IOException{
        for(int i = 1; i <= size; i++){
            if(!seqs.contains(i)){
                Cabecalho c = new Cabecalho((byte)tipo, i,size); // NOACKL
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bao);
                c.serialize(out);
                DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),sa);
                cs.send(pastaP);
            }
        }
    }

    static void pedePacotesEmFaltaD(DatagramSocket cs, List<Integer> seqs, SocketAddress sa) throws IOException{
        for(Integer i : seqs){
            Cabecalho c = new Cabecalho((byte)3, i,0); // NOACKL
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bao);
            c.serialize(out);
            DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),sa);
            cs.send(pastaP);
        }
    }

    static void trataACKL(DatagramSocket ds, ByteManager bm ) throws IOException{
        Par<Cabecalho,SocketAddress> info = receive(ds);
        switch (info.getFst().getTipo()) {
            case 0 -> Pacote.enviaPacoteListaFicheiros(ds, bm, info.getFst().getSeq(), info.getSnd());
            case 6 -> Pacote.enviaPacoteFicheirosAReceber(ds, bm, info.getFst().getSeq(), info.getSnd());
        }
    }

    static Par<Cabecalho,SocketAddress> receive(DatagramSocket ds) throws IOException{
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        ds.setSoTimeout(2000); // timeout é importante
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
