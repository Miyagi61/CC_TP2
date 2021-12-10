import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.List;

public class Pacote {

    void sendPacote(DatagramSocket ds){

    }

    static void pedeListaFicheiros(DatagramSocket ds, Cabecalho c, String pasta, InetAddress ip) throws IOException{
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bao);
        c.serialize(out);
        out.writeUTF(pasta);
        DatagramPacket pastaP = new DatagramPacket(bao.toByteArray(), bao.size(),ip,6000);
        ds.send(pastaP);
    }

    static void enviaPacoteListaFicheiros(DatagramSocket ds, ByteManager list_files, int seq, SocketAddress sa) throws IOException {
        byte[] aux = list_files.getL_array(seq);
        Cabecalho c = new Cabecalho((byte)0,seq, list_files.getCount());
        byte[] res = ByteManager.concatByteArray(c.outputToByte(),aux);
        DatagramPacket newP = new DatagramPacket(res, res.length,sa);
        ds.send(newP);
    }

    static Cabecalho recebePacoteListaFicheiros(DatagramSocket cs,ListarFicheiro lf) throws IOException {
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        cs.receive(dp);
        lf.origem = dp.getSocketAddress();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
        Cabecalho c = new Cabecalho(in);
        lf.carregarDP(in, 0);

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
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        ds.setSoTimeout(100); // timeout é importante
        ds.receive(dp);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
        Cabecalho c = new Cabecalho(din);
        Pacote.enviaPacoteListaFicheiros(ds,bm,c.getSeq(),dp.getSocketAddress());
    }
}
