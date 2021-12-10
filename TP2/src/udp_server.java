import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

public class udp_server {
    public static void main (String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(6000);
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);

        ds.receive(dp);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
        Cabecalho c = new Cabecalho(din);
        ListarFicheiro lf = new ListarFicheiro(din);
        din.close();
        lf.atualizaListaFicheiro();

        ByteManager list_files = lf.upSerialize();

        for(int i = 1; i <= list_files.getCount() ; i++)
            Pacote.enviaPacoteListaFicheiros(ds,list_files,i,dp.getSocketAddress());


        ds.close();

    }
}

