import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class udp_client
{
    static final int pass = 123;
    public static void main(String[] args) throws IOException
    {
        String TEST_DIR = System.getProperty("user.dir");
        DatagramSocket ds = new DatagramSocket(5252);
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);
        String str_dir;
        if(args[0].equals("."))
            str_dir = TEST_DIR;
        else
            str_dir = TEST_DIR+"/"+args[0];

        File dir = new File(str_dir);

        if(!dir.exists()) {
            if (!dir.mkdirs())
                System.out.println("Something went wrong");
                return;
        }
        try {
            ds.receive(dp);
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
            Cabecalho c = new Cabecalho(din);
            if(c.getHash() != pass){
                Pacote.enviaPacoteErro(ds,0,dp.getSocketAddress());
                throw new Exception("ERRROUUUUU MISERÁVEUUUU");
            }
            ListarFicheiro lf = new ListarFicheiro(str_dir);
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
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

   }

}





