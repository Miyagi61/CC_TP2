import javax.sound.sampled.Port;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;


public class FileHandler implements Serializable,Runnable {
    boolean send;  //se for true a thread vai enviar informação, senao vai receber
    private SocketAddress destino; //no caso de ser para enviar é p/onde envia os packets, se for receber é de onde recebe
    private File file;
    private boolean syncronized; //saber se já chegaram os pacotes todos
    private Set<Par<Cabecalho,byte[]>> pacotes; //cada pacote tem o seu cabeçalho e os bytes do corpo
                                                //e o set está ordenado pelos números de sequência do cabeçalho
                                                //o campo hash do cabeçalho é o tamanho do pacote neste tipo

    public FileHandler(){
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=false;
    }

    public  FileHandler(File file, SocketAddress dest, boolean send){
        this.send=send;
        this.file=file;
        this.destino=dest;
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=false;
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
        //syncronized=true;
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
        File f = new File("TP2/src/test.txt");
        boolean b = args[0].equals("true"); //true é send false é recebe
        SocketAddress sa = new InetSocketAddress(5250);
        Thread t = new Thread(new FileHandler(f, sa , b));
        t.start();
    }

    public void run_send(){
        try {

            DatagramSocket ds = new DatagramSocket();  //De onde a nova thread envia o ficheiro

            FileInputStream fis = new FileInputStream(file);
            int nr_pacotes_expected = (int)Math.floorDiv(file.length(),791) + 1;
            for (long i = 0 ; i < nr_pacotes_expected; i++) {

                byte[] pacote = new byte[791];
                int read_bytes = fis.read(pacote);
                Cabecalho cb = new Cabecalho((byte)1,(int)i, read_bytes); //read bytes devido o ultimo ter tamanho dif

                //System.out.println(read_bytes);

                pacotes.add(new Par<>(cb,pacote));
                byte[] res = ByteManager.concatByteArray(cb.outputToByte(),pacote);
                DatagramPacket newP = new DatagramPacket(res,res.length,destino);
                ds.send(newP); //envia para o SocketAdress destino
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void run_receive(){
        try {
            DatagramSocket ds =new DatagramSocket(destino);
            int nr_pacotes_expected = (int)Math.floorDiv(file.length(),791) + 1;

            for (long i = 0; i < nr_pacotes_expected ; i++) {

                byte[] buffer=new byte[800];
                DatagramPacket dp = new DatagramPacket(buffer,800);
                ds.receive(dp);
                ByteBuffer bb = ByteBuffer.wrap(dp.getData());

                byte tipo = bb.get();   //recolher os campos do cabeçalho do datagrampacket
                int seq = bb.getInt();
                int resto = bb.getInt(); //resto é a quantidade de bytes que o array ainda tem (dados)

                Cabecalho cb = new Cabecalho(tipo,seq,resto);
                byte[] pacote = new byte[resto];
                bb.get(pacote);
                pacotes.add(new Par<>(cb,pacote));
            }

            if(pacotes.size() < nr_pacotes_expected) {  // caso para noacks
                System.out.println("Faltam pacotes: " +pacotes.size()+"/"+nr_pacotes_expected);

            }
            else{
                System.out.println("Todos os pacotes foram recebidos: Syncronized");
                syncronized=true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (send) run_send();
        else run_receive();
    }
}

class ComparaPacotes implements  Comparator<Par<Cabecalho,byte[]>>{

    @Override
    public int compare(Par<Cabecalho, byte[]> o1, Par<Cabecalho, byte[]> o2) {
        return Integer.compare(o1.getFst().getSeq(), o2.getFst().getSeq());
    }
}
