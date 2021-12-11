import java.io.*;
import java.util.*;


public class FileHandler implements Serializable {
    private boolean syncronized; //saber se já chegaram os pacotes todos
    private Set<Map.Entry<Cabecalho,byte[]>> pacotes;   //cada pacote tem o seu cabeçalho e os bytes do corpo
                                                        //e o set está ordenado pelos números de sequência do cabeçalho


    public FileHandler(){
        Comparator<Map.Entry<Cabecalho,byte[]>> comparador = new ComparaPacotes(); //comprador segundo seq do cabeçalho
        this.pacotes = new TreeSet<>(comparador);
        this.syncronized=true;
    }


    public int getNr_pacotes() {
        return pacotes.size();
    }

    public List<byte[]> getPacotes() {
        List<byte[]> nova = new ArrayList<byte[]>();
        for(Map.Entry pacote: pacotes)
            nova.add((byte[]) pacote.getValue());
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
        Comparator<Map.Entry<Cabecalho,byte[]>> comparador = new ComparaPacotes();  //inicia o comparador
        this.pacotes = new TreeSet<>(comparador);                                   //inicia o set
        syncronized=true;
        while(din.available()>0) {          // enquanto houver data
            Cabecalho cb = new Cabecalho(din);   //cria um cabeçalho e atua conforme o seu tipo
            switch (cb.tipo) {
                case 0:
                    break;
                case 1:
                    byte[] pacote = din.readNBytes(791);  //lê os bytes restantes para um pacote
                    Map.Entry<Cabecalho,byte[]> me = new AbstractMap.SimpleEntry<>(cb, pacote); //cria um mapEntry
                    pacotes.add(me);  //adiciona-o ao set dos pacotes
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
            for(Map.Entry<Cabecalho,byte[]> me: pacotes){
                fos.write(me.getValue());
            }
        }
        return file;
    }


    public static void main(String[] args) throws IOException {
    }
}

class ComparaPacotes implements  Comparator<Map.Entry<Cabecalho,byte[]>>{

    @Override
    public int compare(Map.Entry<Cabecalho, byte[]> o1, Map.Entry<Cabecalho, byte[]> o2) {
        return Integer.compare(o1.getKey().seq,o2.getKey().seq);
    }
}
