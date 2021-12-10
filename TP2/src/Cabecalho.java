import java.io.*;
import java.util.Random;

public class Cabecalho implements Serializable {
    byte tipo;
    int seq;
    int hash;


    Cabecalho(int hash, byte tipo){
        Random rn = new Random();
        this.hash = hash;
        this.seq=0;
        this.tipo = tipo;
    }

    Cabecalho(byte tipo, int seq, int hash){
        this.hash = hash;
        this.seq = seq;
        this.tipo = tipo;
    }

    Cabecalho(Cabecalho cb){
        this.hash = cb.getHash();
        this.seq = cb.getSeq();
        this.tipo = cb.getTipo();
    }

    Cabecalho(DataInputStream din){
        //DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
        Cabecalho c = null;
        try {
            c = this.deserialize(din);
            this.seq = c.getSeq();
            this.tipo = c.getTipo();
            this.hash = c.getHash();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    int getHash(){
        return this.hash;
    }

    int getSeq(){
        return this.seq;
    }

    byte getTipo() { return this.tipo; }

    void serialize(DataOutputStream out) throws IOException {
        out.write(tipo);
        out.writeInt(seq);
        out.writeInt(hash);
    }

    Cabecalho deserialize(DataInputStream in) throws IOException {
        byte t = in.readByte();
        int sq = in.readInt();
        int hash = in.readInt();
        return new Cabecalho(t,sq,hash);
    }

    public byte[] outputToByte() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bao);
        serialize(dos);
        dos.flush();
        dos.close();
        return bao.toByteArray();
    }
}
