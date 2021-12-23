import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;



public class udp_server {

    public static void main (String[] args) throws IOException {
        String TEST_DIR = System.getProperty("user.dir");
        DatagramSocket ds = new DatagramSocket(6000);
        String str_dir;
        if(args[0].equals("."))
            str_dir = TEST_DIR;
        else
            str_dir = TEST_DIR+"/"+args[0];

        File dir = new File(str_dir);

        if(!dir.exists()){ dir.mkdirs(); }
        InetAddress ip = InetAddress.getLocalHost();
        int password = Integer.parseInt(args[2]);
        Cabecalho c = new Cabecalho((byte) 1, -1, password);
        try {
            Pacote.pedeListaFicheiros(ds, c, ip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ListarFicheiro lf_A = new ListarFicheiro(str_dir);
        lf_A.atualizaListaFicheiro();

        //ListarFicheiro lf_B = new ListarFicheiro(str_dir);
        ListarFicheiro lf_B = new ListarFicheiro();
        int aux = 1;
        List<Integer> seqs = new ArrayList<>();
        int maxcount = 0;
        for(int i = 0; i < aux ; i+=1){
            try {
                ds.setSoTimeout(100); // 10 milissegundos
                c = Pacote.recebePacoteListaFicheiros(ds,lf_B,i-1);
                if(c == null)
                    return;
                aux = c.getHash(); // get numero de pacotes
                seqs.add(c.getSeq());
            }catch (SocketTimeoutException ste){
                try {
                    maxcount +=1;
                    i-=1;
                    if(maxcount == 20)
                            throw new IOException("Ligação Instável");
                    InetSocketAddress isa = new InetSocketAddress(ip,5252);
                   // Pacote.pedePacotesEmFalta(ds,seqs,isa,aux);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        Set<String> send = lf_A.checkDiff(lf_B);
        Set<String> req = lf_B.checkDiff(lf_A);

        System.out.println("Requested" + req.toString());
        System.out.println("Sending" + send.toString());

        ds.close();

    }
}

