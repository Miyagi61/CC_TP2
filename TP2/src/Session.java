import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class SessionSocket implements Runnable {
    private DatagramSocket ds;
    private String[] args;

    private final String TEST_DIR = "/home/miyagi/Desktop/ffsync";

    SessionSocket(DatagramSocket s,String[] args) {
        this.ds = s;
        this.args = new String[args.length];
        for(int i = 0; i < args.length ; i++){
            this.args[i] = args[i];
        }
    }

    public void run() {
        if(args[0].equals("start")){
            byte[] buf = new byte[800];
            DatagramPacket dp = new DatagramPacket(buf, 800);

            try {
                ds.receive(dp);
                DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
                Cabecalho c = new Cabecalho(din);
                ListarFicheiro lf = new ListarFicheiro(din);
                din.close();
                lf.atualizaListaFicheiro();
                ByteManager list_files = lf.upSerialize();

                for(int i = 1; i <= list_files.getCount() ; i++)
                    Pacote.enviaPacoteListaFicheiros(ds,list_files,i,dp.getSocketAddress());

                while(true){
                    try {
                        Pacote.trataACKL(ds,list_files);
                    }catch (SocketTimeoutException e){
                        break;
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }





            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{

            String str_dir;
            if(args[0].equals("."))
                str_dir = TEST_DIR;
            else
                str_dir = TEST_DIR+"/"+args[0];

            File dir = new File(str_dir);

            if(!dir.exists()){ dir.mkdirs(); }

            Cabecalho c = new Cabecalho((byte) 1, -1, 123);
            try {
                Pacote.pedeListaFicheiros(ds, c, str_dir, InetAddress.getByName(args[1]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ListarFicheiro lf_A = new ListarFicheiro(str_dir);
            lf_A.atualizaListaFicheiro();

            ListarFicheiro lf_B = new ListarFicheiro(str_dir);
            int aux = 1;
            List<Integer> seqs = new ArrayList<>();
            for(int i = 0; i < aux ; i++){
                try {
                    ds.setSoTimeout(10); // 10 milissegundos
                    c = Pacote.recebePacoteListaFicheiros(ds,lf_B);
                    aux = c.getHash(); // get numero de pacotes
                    seqs.add(c.getSeq());
                }catch (SocketTimeoutException ste){
                    try {
                        Pacote.pedePacotesEmFaltaLF(ds,seqs,lf_B.origem,aux);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Set<String> req = lf_A.checkDiff(lf_B);
            Set<String> send = lf_B.checkDiff(lf_A);




            ds.close();
        }

    }
}

class Session{
    public static void main(String[] str) throws IOException {
        DatagramSocket ds = new DatagramSocket(5252);
        if((str.length == 1 && str[0].equals("start")) || str.length == 2) {
            Thread ss = new Thread(new SessionSocket(ds, str));
            ss.start();
        }
        else{
            System.out.println("Argumentos inválidos");
        }
    }
}
