import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class udp_client
{
    public static void main(String[] args) throws IOException
    {
        if(args.length == 1 && args[0].equals("start")){
            DatagramSocket ds = new DatagramSocket(5252);
            byte[] buf = new byte[800];
            DatagramPacket dp = new DatagramPacket(buf, 800);

            ds.receive(dp);
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
            Cabecalho c = new Cabecalho(din);
            ListarFicheiro lf = new ListarFicheiro(din);
            din.close();
            lf.atualizaListaFicheiro();

            ByteManager list_files = lf.upSerialize();

            for(int i = 0; i < list_files.getCount() ; i++)
                Pacote.enviaPacoteListaFicheiros(ds,list_files,i,dp.getSocketAddress());

        }else {
            String pasta = "/home/miyagi/Desktop";
            InetAddress ip = InetAddress.getLocalHost();
            //InetAddress ip = InetAddress.getByName(args[1]);
            //String pasta = args[0];
            DatagramSocket cs = new DatagramSocket();
            Cabecalho c = new Cabecalho((byte) 1, -1, 123);

            Pacote.pedeListaFicheiros(cs, c, pasta, ip);

            ListarFicheiro lf = new ListarFicheiro(pasta);
            int aux = 1;
            List<Integer> seqs = new ArrayList<>();
            for(int i = 0; i < aux ; i++){
                c = Pacote.recebePacoteListaFicheiros(cs,lf);
                aux = c.getHash(); // get numero de pacotes
                seqs.add(c.getSeq());
            }

            System.out.println(lf.list.toString());
            cs.close();
        }

   }

}





