import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ListarFicheiro {
    Map<String,Double> list;
    String nome_pasta;
    SocketAddress origem;

    ListarFicheiro(String pasta){
        this.nome_pasta = pasta;
        this.list = new HashMap<>();
    }
    ListarFicheiro(){
        this.nome_pasta = null;
        this.list = new HashMap<>();
    }

    static ByteManager upSerializeV2(Set<String> files) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bao);
        ByteManager bm = new ByteManager();

        for(String file : files){
            if(bao.size() + file.length() + 2 >= 791 ){// 2 + x + 2 + 1 + 8 + 2 + 1
                bm.addElement(bao.toByteArray());
                bao.reset();
            }
            out.writeUTF(file);
            out.writeUTF(" ");
            bm.addItem();
        }
        bm.addElement(bao.toByteArray());

        out.flush();
        out.close();
        return bm;
    }

    void atualizaListaFicheiro(){
        File[] listaF = new File(nome_pasta).listFiles();
        if(listaF != null) {
            for (File l : listaF) {
                if (!l.isDirectory()) {
                    String nome = l.getName();
                    ByteBuffer buffer = StandardCharsets.UTF_8.encode(nome);
                    nome = StandardCharsets.UTF_8.decode(buffer).toString();

                    Double size = (double) l.length();
                    list.put(nome, size);
                }
            }
        }else{ //
            // criar uma diretoria
        }
    }

    ByteManager upSerialize() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bao);
        ByteManager bm = new ByteManager();

        out.writeUTF(nome_pasta);
        out.writeUTF("#");
        bm.addItem();

        for(Map.Entry<String,Double> file : list.entrySet()){
            if(bao.size() + file.getKey().length() + 16 >= 791 ){// 2 + x + 2 + 1 + 8 + 2 + 1
                bm.addElement(bao.toByteArray());
                bao.reset();
            }
            serialize(out,file.getKey(),file.getValue());
            bm.addItem();
        }
        bm.addElement(bao.toByteArray());

        out.flush();
        out.close();
        return bm;
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
            if(tipo == -1) {
                this.nome_pasta = din.readUTF();
                din.readUTF();
            }
            this.list.putAll(ListarFicheiro.deserialize(din));
            din.close();
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Set<String> checkDiff(ListarFicheiro lf){
        Set<String> res = new TreeSet<>();
        for(Map.Entry<String,Double> file : this.list.entrySet()){
            if(!lf.list.entrySet().contains(file)){
                res.add(file.getKey());
            }
        }
        return  res;
    }
}

