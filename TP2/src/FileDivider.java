package TP2.src;

import java.io.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class FileDivider implements Serializable {
    private String filename;
    private int key;
    private int nr_pacotes;
    private List<byte[]> pacotes;
    private File file;

    public FileDivider(File file){
        this.file=file;
        this.pacotes = new ArrayList<byte[]>();
        this.key=hashCode();
        this.nr_pacotes=0;
        this.filename=file.getName();
        this.key=hashCode(filename);
    }


    public int hashCode(String s) {
        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);}
        return Objects.hash(key);
    }

    void readFileBytes() throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[791];
        while((read=fis.read(buffer))!=-1){
            os.write(buffer, 0, 791);
            pacotes.add(buffer.clone());
            nr_pacotes++;
            /*if(read>791){

                os.write(buffer, 791, buffer.length-791);
                pacotes.add(buffer.clone());
                nr_pacotes++;
            }*/
            buffer = new byte[791];

        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("CC_TP2/TP2/src/test.txt");
        FileDivider fd = new FileDivider(file);
        fd.readFileBytes();
        ArrayList<byte[]> list = (ArrayList<byte[]>) fd.pacotes;
        int j=0;
        for(byte[] pacote: list){
            System.out.print("Pacote"+j+": ");
            for(int i=0; i<pacote.length && pacote[i]!=0 ; i++){
                System.out.write(pacote[i]);
            }
            j++;
            System.out.print("\n");
        }

    }

}
