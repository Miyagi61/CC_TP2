import java.io.*;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class ListarFicheiro {
    Map<String,Double> list;
    String nome_pasta;

    ListarFicheiro(String pasta){
        this.nome_pasta = pasta;
        this.list = new HashMap<>();
    }

    ListarFicheiro(DatagramPacket dp){
        this.carregarDP(dp);
    }

    void atualizaListaFicheiro(){
        File[] listaF = new File(nome_pasta).listFiles();

        for(File l : listaF){
            if(!l.isDirectory()){
                String nome = l.getName();
                Double size = (double)l.length();
                list.put(nome,size);
            }
        }
    }

    void serialize(DataOutputStream out) throws IOException {
        for(Map.Entry<String,Double> file : list.entrySet()){
            out.writeUTF(file.getKey());
            out.writeUTF("#");
            out.writeDouble(file.getValue());
            out.writeUTF("#");
        }
    }

    static Map<String,Double> deserialize(DataInputStream in) throws IOException{
        Map<String,Double> res = new HashMap<>();
        String name = "";
        Double size;
        try{
            while(true) { // está a ler até dar EOF exception
                name = in.readUTF();
                in.readUTF(); // ler o #
                size = in.readDouble();
                in.readUTF(); // ler o #
                res.put(name, size);
            }
        }catch (EOFException e){  }
        return res;
    }

    public byte[] outputToByte() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bao);
        serialize(dos);
        dos.flush();
        dos.close();
        return bao.toByteArray();
    }

    public void carregarDP(DatagramPacket dp){
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
        try{
            this.list = ListarFicheiro.deserialize(din);
            din.close();
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

