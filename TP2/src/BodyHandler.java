package TP2.src;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class BodyHandler implements Serializable {
    private Cabecalho cabecalho;
    private int nr_pacotes;
    private List<byte[]> pacotes;
    private boolean syncronized;


    public BodyHandler(){
        this.cabecalho = new Cabecalho((byte)1, -1 , 123);
        this.pacotes = new ArrayList<byte[]>();
        this.nr_pacotes=0;
        this.syncronized=true;
    }

    public int getNr_pacotes() {
        return nr_pacotes;
    }

    public void setNr_pacotes(int nr_pacotes) {
        this.nr_pacotes = nr_pacotes;
    }

    public List<byte[]> getPacotes() {
        List<byte[]> nova = new ArrayList<byte[]>();
        for(byte[] pacote: pacotes)
            nova.add(pacote.clone());
        return nova;
    }

    public void setPacotes(List<byte[]> pacotes) {
        List<byte[]> nova = new ArrayList<byte[]>();
        for(byte[] pacote: pacotes)
            nova.add(pacote.clone());
        this.pacotes=nova;
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
        BodyHandler that = (BodyHandler) o;
        return  nr_pacotes == that.nr_pacotes &&
                syncronized == that.syncronized &&
                cabecalho.equals(that.cabecalho) &&
                checkIfPacotesIguais(pacotes,that.pacotes);
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

    public int hashCode(String s) {
        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);}
        return Objects.hash(cabecalho.hash);
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

    void recebePacote(byte[] pacote){
            int len=pacote.length;
            if(len<=791) {
                byte[] novo = new byte[len];
                for (int i = 0; i < len; i++) {
                    novo[i] = pacote[i];
                }
                pacotes.add(novo);
                nr_pacotes++;
            }
    }

    public static void main(String[] args) throws IOException {


        BodyHandler bh = new BodyHandler();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[791];
        String s = "Hello Wolrd!!";

        bh.recebePacote(s.getBytes());
        os.write(buffer,0,s.length()+1);
        for(byte[] arr: bh.pacotes)
            System.out.writeBytes(arr);
    }

}
