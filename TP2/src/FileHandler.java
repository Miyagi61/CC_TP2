import java.io.*;
import java.util.*;


public class FileHandler implements Serializable {
    private Map<Cabecalho , byte[] > pacotes;  //como sabemos a ordem dos pacotes que chegam ?
    private boolean syncronized;


    public FileHandler(){
        this.pacotes = new HashMap<>();
        this.syncronized=true;
    }


    public int getNr_pacotes() {
        return pacotes.size();
    }

    public List<byte[]> getPacotes() {
        List<byte[]> nova = new ArrayList<byte[]>();
        for(byte[] pacote: pacotes.values())
            nova.add(pacote.clone());
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
        return syncronized == that.syncronized && pacotes.equals(that.pacotes);
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

    /*public int hashCode(String s) {
        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);}
        return Objects.hash(cabecalho.hash);
    }*///hashcode
    
    public FileHandler(DataInputStream din) throws IOException {
        pacotes = new HashMap<>();
        syncronized=true;
        while(din.available()>0) {
            Cabecalho cb = new Cabecalho(din);
            switch (cb.tipo) {
                case 0:
                    break;
                case 1:
                    byte[] pacote = new byte[791];
                    pacote = din.readNBytes(791);
                    pacotes.put(cb, pacote);
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

/*
    void readFileBytes() throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[791];
        while((read=fis.read(buffer))!=-1){
            os.write(buffer, 0, 791);
            pacotes.add(buffer.clone());
            nr_pacotes++;
            buffer = new byte[791];
        }
        fis.close();
    }


    File juntaPacotes() throws FileNotFoundException , IOException{
        Path path = file.toPath();
        File f = new File(2+file.getName());
        FileOutputStream fos = new FileOutputStream(f);

        if(syncronized){
            for(byte[] pacote: pacotes){
                for(byte b: pacote){
                    if(b!=0)
                        fos.write(b);
                }
            }
            fos.flush();
            fos.close();
        }
        return f;
    }
*/

    public static void main(String[] args) throws IOException {

/*
        FileHandler bh = new FileHandler();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[791];
        String s = "Hello Wolrd!!";

        bh.recebePacote(s.getBytes());
        os.write(buffer,0,s.length()+1);
        for(byte[] arr: bh.pacotes)
            System.out.write(arr,0,arr.length);
    }*/
    }
}
