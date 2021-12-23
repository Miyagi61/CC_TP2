import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.time.Instant;
import java.time.Duration;
import java.util.logging.*;

public class FileHandler implements Serializable,Runnable {
    boolean send;  //se for true a thread vai enviar informação, senao vai receber
    private SocketAddress destino; //no caso de ser para enviar é p/onde envia os packets, se for receber é de onde recebe
    private File file;
    private boolean syncronized; //saber se já chegaram os pacotes todos
    private Set<Par<Cabecalho,byte[]>> pacotes; //cada pacote tem o seu cabeçalho e os bytes do corpo
                                                //e o set está ordenado pelos números de sequência do cabeçalho
                                                //o campo hash do cabeçalho é o tamanho do pacote neste tipo
    private int port;
    private Double tam_file;

     private final static  Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public FileHandler(){
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=false;
    }

    public  FileHandler(File file, SocketAddress dest, boolean send, int port, Double tam_file){
        this.send=send;
        this.file=file;
        this.destino=dest;
        Comparator<Par<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=false;
        this.port = port;
        this.tam_file = tam_file;
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

    void juntaPacotes() throws FileNotFoundException,IOException{ //Pacotes já estão ordenados por ser treeset
        if(file.exists() && !file.delete()){
            throw new IOException("Erro a eliminar ficheiro");
        }
        if(!file.createNewFile()){
            throw new IOException("Erro a criar ficheiro");
        }
        OutputStream os = new FileOutputStream(file);

        for(Par<Cabecalho,byte[]> data : pacotes){
            os.write(data.getSnd());
        }
        os.flush();
        os.close();
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

                pacotes.add(new Par<>(cb,pacote));
                byte[] res = ByteManager.concatByteArray(cb.outputToByte(),pacote);
                DatagramPacket newP = new DatagramPacket(res,res.length,destino);
                ds.send(newP); //envia para o SocketAdress destino
            }
            ds.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void run_receive(){
        try {
            DatagramSocket datS = new DatagramSocket(port);
            long start = System.currentTimeMillis();
            int nr_pacotes_expected = (int)Math.floorDiv(tam_file.longValue(),791) + 1; // este valor tem de ser recebido nao faz sentido estar aqui
            for (long i = 0; i < nr_pacotes_expected ; i++) {
                Triplo<Cabecalho,byte[],SocketAddress> pac = Pacote.recebePacoteDados(datS);
                if(pac != null)
                    pacotes.add(new Par<>(pac.getFst(),pac.getSnd()));
                else{
                    nr_pacotes_expected--;
                }
            }
            if(pacotes.size() < nr_pacotes_expected) {  // caso para noacks
                System.out.println("Faltam pacotes: " +pacotes.size()+"/"+nr_pacotes_expected);
                this.pedePacotesEmFalta(datS);
            }
            System.out.println("Todos os pacotes foram recebidos: Synchronized");

            juntaPacotes(); // junta pacotes e cria ficheiro
            long end = System.currentTimeMillis();
            long res = end - start;
            double Debito = tam_file/((double)res/1000);
            logr.log(Level.INFO,"The debt is "+Debito);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void pedePacotesEmFalta(DatagramSocket ds) throws IOException {
        int maxcount = 0;
        int last = -1;
        List<Integer> seqs = new ArrayList<>();
        for(Par<Cabecalho,byte[]> idx : pacotes){
            while(idx.getFst().getSeq() != last+1){
                seqs.add(last++);
            }
            last = idx.getFst().getSeq();
        }
        while (!seqs.isEmpty()){
            Pacote.pedePacotesEmFaltaD(ds,seqs,destino);
            for(Integer i : seqs){
                Triplo<Cabecalho,byte[],SocketAddress> rec = Pacote.recebePacoteDados(ds);
                if(seqs.contains(rec.getFst().getSeq())){
                    pacotes.add(new Par<>(rec.getFst(),rec.getSnd()));
                    seqs.remove(rec.getFst().getSeq());
                }
            }
            maxcount +=1;
            if(maxcount == 10)
                throw new IOException("Ligação Instável");
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
