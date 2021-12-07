import org.apache.groovy.parser.antlr4.util.StringUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ListarFicheiro {
    Map<String,Double> list;
    String nome_pasta;

    ListarFicheiro(String pasta){
        this.nome_pasta = pasta;
        this.list = new HashMap<>();
    }

    ListarFicheiro(DataInputStream din) throws IOException{
        //DataInputStream din = new DataInputStream(new ByteArrayInputStream(dp.getData(), dp.getOffset(), dp.getLength()));
        this.list = new HashMap<>();
        this.carregarDP(din,-1);
    }

    void atualizaListaFicheiro(){
        File[] listaF = new File(nome_pasta).listFiles();

        for(File l : listaF){
            if(!l.isDirectory()){
                String nome = l.getName();
                ByteBuffer buffer = StandardCharsets.UTF_8.encode(nome);
                nome = StandardCharsets.UTF_8.decode(buffer).toString();

                Double size = (double)l.length();
                list.put(nome,size);
            }
        }
    }

    byte[] upSerialize() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bao);

        for(Map.Entry<String,Double> file : list.entrySet()){
            serialize(out,file.getKey(),file.getValue());
        }

        out.flush();
        out.close();

        return bao.toByteArray();
    }

    void serialize(DataOutputStream out, String file, Double value) throws IOException{
        out.writeUTF(file);
        out.writeUTF("#");
        out.writeDouble(value);
        out.writeUTF("#");
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

    public void carregarDP(DataInputStream din,int tipo){
        try{
            if(tipo == -1)
                this.nome_pasta = din.readUTF();
            else{
                this.list.putAll(ListarFicheiro.deserialize(din));
            }
            din.close();
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

