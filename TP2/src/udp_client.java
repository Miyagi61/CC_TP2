import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class udp_client
{
    static final int pass = 123;
    public static void main(String[] args) throws IOException
    {

        DatagramSocket ds = new DatagramSocket(5252);
        byte[] buf = new byte[800];
        DatagramPacket dp = new DatagramPacket(buf, 800);

        try {
            ds.receive(dp);
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(),dp.getOffset(),dp.getLength()));
            Cabecalho c = new Cabecalho(din);
            if(c.getHash() != pass){
                Pacote.enviaPacoteErro(ds,0,dp.getSocketAddress());
                throw new Exception("ERRROUUUUU MISERÁVEUUUU");
            }
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
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

   }

}





