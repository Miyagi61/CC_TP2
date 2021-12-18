import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;


public class FileHandler implements Serializable,Runnable {
    private SocketAddress destino;
    private File file;
    private boolean syncronized; //saber se já chegaram os pacotes todos
    private Set<Par<Cabecalho,byte[]>> pacotes;   //cada pacote tem o seu cabeçalho e os bytes do corpo
                                                        //e o set está ordenado pelos números de sequência do cabeçalho


    public FileHandler(){
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=true;
    }

    public  FileHandler(File file, SocketAddress dest){
        this.file=file;
        this.destino=dest;
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=true;
    }


    public int getNr_pacotes() {
        return pacotes.size();
    }

    public List<byte[]> getPacotes() {
        List<byte[]> nova = new ArrayList<byte[]>();
        for(Par pacote: pacotes)
            nova.add((byte[]) pacote.getSnd());
        return nova;
    }

    public boolean isSyncronized() {
        return syncronized;
    }

    public void setSyncronized(boolean syncronized) {
        this.syncronized = syncronized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileHandler that = (FileHandler) o;
        return syncronized == that.syncronized && pacotes.containsAll(that.pacotes);
    }

    boolean checkIfPacotesIguais(List<byte[]> a, List<byte[]> b){
        if(a.size()==b.size()){
            int i;
            for(i=0; i<a.size() && Arrays.equals(a.get(i),b.get(i)); i++);
            if(i<a.size()) return false;
            else return true;
        }
        return false;
    }

    public FileHandler(DataInputStream din) throws IOException {                    //lê uma input stream
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes();  //inicia o comparador
        this.pacotes = new TreeSet<>(comparador);                                   //inicia o set
        syncronized=true;
        while(din.available()>0) {          // enquanto houver data
            Cabecalho cb = new Cabecalho(din);   //cria um cabeçalho e atua conforme o seu tipo
            switch (cb.tipo) {
                case 0:
                    break;
                case 1:
                    byte[] pacote = din.readNBytes(791);  //lê os bytes restantes para um pacote
                    Par<Cabecalho,byte[]> par = new Par<>(cb,pacote); //cria um mapEntry
                    pacotes.add(par);  //adiciona-o ao set dos pacotes
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
            }
        }
        din.close();
    }

    File juntaPacotes() throws FileNotFoundException,IOException{ //Pacotes já estão ordenados por ser treeset
        File file = new File("xpto");
        FileOutputStream fos = new FileOutputStream(file);
        if(syncronized){
            for(Par<Cabecalho,byte[]> me: pacotes){
                fos.write(me.getSnd());
            }
        }
        return file;
    }


    public static void main(String[] args) throws IOException {
        File f = new File("test.txt");
        DatagramSocket ds = new DatagramSocket();
        Thread t = new Thread(new FileHandler(f,ds.getLocalSocketAddress()));
        t.start();

    }

    @Override
    public void run() {
        try {

            DatagramSocket ds = new DatagramSocket();  //De onde a nova thread envia o ficheiro

            FileInputStream fis = new FileInputStream(file);
            for (long i = 0, len = file.length() / 791; i < len; i++) {

                byte[] pacote = new byte[791];
                Cabecalho cb = new Cabecalho((byte)1,(int)i, 123);
                int read_bytes = fis.read(pacote);

                System.out.println(read_bytes);

                pacotes.add(new Par<>(cb,pacote));

                byte[] res = ByteManager.concatByteArray(cb.outputToByte(),pacote);
                DatagramPacket newP = new DatagramPacket(res, res.length,destino);
                ds.send(newP);
                // do something with the 8 bytes
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class ComparaPacotes implements  Comparator<Par<Cabecalho,byte[]>>{

    @Override
    public int compare(Par<Cabecalho, byte[]> o1, Par<Cabecalho, byte[]> o2) {
        return Integer.compare(o1.getFst().getSeq(), o2.getFst().getSeq());
    }
}
