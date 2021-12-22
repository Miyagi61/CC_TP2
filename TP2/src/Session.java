import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class SessionSocket implements Runnable {
    private HttpAnswer http;
    private DatagramSocket ds;
    private String[] args;
    private String str_dir;
    private SocketAddress dest;

    private final String TEST_DIR = System.getProperty("user.dir");

    SessionSocket(DatagramSocket s,String[] args,SocketAddress d, HttpAnswer http) {
        this.ds = s;
        this.args = new String[args.length];
        for(int i = 0; i < args.length ; i++){
            this.args[i] = args[i];
        }
        this.dest = d;
        this.http = http;
        this.init();
    }

    public void init(){
        if(args[0].equals("."))
            str_dir = TEST_DIR;
        else
            str_dir = TEST_DIR+"/"+args[0];

        File dir = new File(str_dir);

        if(!dir.exists() && !dir.mkdirs()){
            System.out.println("Something went wrong");
        }
    }

    private void runPassive() throws IOException, InterruptedException {
        ListarFicheiro lf = new ListarFicheiro(str_dir);
        lf.atualizaListaFicheiro();
        ByteManager list_files = lf.upSerialize();

        http.changeMessage("Enviando Lista de Ficheiros de "+str_dir+" a "+dest);
        for (int i = 1; i <= list_files.getCount(); i++)
            Pacote.enviaPacoteListaFicheiros(ds, list_files, i,dest);
        while (true) {
            try {
                http.changeMessage("Esperando NOACKS da Lista de Ficheiros");
                Pacote.trataACKL(ds, list_files);
            } catch (SocketTimeoutException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        DatagramSocket dsr = new DatagramSocket(dest);
        byte[] buffer=new byte[800];
        DatagramPacket dp = new DatagramPacket(buffer,800);     //recebe a lista de ficheiros que tem que enviar

        dsr.receive(dp);        //recebe uma string fo tipo "file0 file1 file2 ..."
        String files_to_send = Arrays.toString(dp.getData());
        String[] files = files_to_send.split(" ");     //cria array com a[0]=file0 , a[1]=file1, ...

        List<Thread> threads_send = new ArrayList<>();
        List<Thread> threads_req = new ArrayList<>();

        for(String s: files){
            File f = new File(s);
            Thread t = new Thread(new FileHandler(f, ds.getLocalSocketAddress(), true));
            threads_send.add(t); //prepara threads de envio
        }
        for(Thread t: threads_send) t.start(); //envia-os


        byte[] buffer2 = new byte[800];
        DatagramPacket dp_r = new DatagramPacket(buffer2, buffer2.length);          //espera ficheiros que
        dsr.receive(dp_r);                                                          //faltam (recebe os que faltam)
        String files_to_receive = Arrays.toString(dp.getData());
        String[] files_r = files_to_send.split(" ");



        if(files_r.length > 0) {        // se houverem ficheiros a receber
            for (String s : files_r) {
                File f = new File(s);
                boolean create = f.createNewFile(); //caso nao exista
                Thread t = new Thread(new FileHandler(f, ds.getLocalSocketAddress(), false));
                threads_req.add(t);
            }
            for (Thread t : threads_req) t.start();

            byte[] confirmacao = "Ready to receive".getBytes(StandardCharsets.UTF_8);
            DatagramPacket dp_confirmacao = new DatagramPacket(confirmacao,confirmacao.length, ds.getLocalSocketAddress());
            dsr.send(dp_confirmacao); //enviar confirmação (falta garantir que começa antes a espera e depois o envio
            for (Thread t : threads_req) t.join();
        }
        for (Thread t : threads_send) t.join();

        http.changeMessage("A enviar Ficheiros");
    }

    private void runActive() throws IOException, InterruptedException {
        int password = Integer.parseInt(args[2]);
        Cabecalho c = new Cabecalho((byte) 1, -1, password);

        try {
            Pacote.pedeListaFicheiros(ds, c, InetAddress.getByName(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ListarFicheiro lf_A = new ListarFicheiro(str_dir);
        lf_A.atualizaListaFicheiro();

        ListarFicheiro lf_B = new ListarFicheiro();
        int aux = 1;
        int maxcount = 0;
        List<Integer> seqs = new ArrayList<>();
        http.changeMessage("Recebendo Lista de Ficheiros de "+args[1]);
        for(int i = 0; i < aux ; i++){
            try {
                ds.setSoTimeout(100); // 100 milissegundos
                c = Pacote.recebePacoteListaFicheiros(ds,lf_B,i-1);
                if(c == null)
                    return;
                aux = c.getHash(); // get numero de pacotes
                seqs.add(c.getSeq());
            }catch (SocketTimeoutException ste){
                try {
                    maxcount +=1;
                    i-=1;
                    if(maxcount == 20)
                        throw new IOException("Ligação Instável");
                    http.changeMessage("Enviando NOACKS da Lista de Ficheiros para "+args[1]);
                    Pacote.pedePacotesEmFaltaLF(ds,seqs,lf_B.origem,aux);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        Set<String> send = lf_A.checkDiff(lf_B);
        Set<String> req = lf_B.checkDiff(lf_A);

        String files_to_request=""; //para enviar o passivo
        String files_to_send=""; //para enviar o passivo

        List<Thread> threads_send = new ArrayList<>();
        List<Thread> threads_req = new ArrayList<>();

        for(String s: req){
            files_to_request = files_to_request + " " + s;
            File f = new File(s);
            boolean create = f.createNewFile(); //caso nao exista
            Thread t = new Thread(new FileHandler(f, ds.getLocalSocketAddress(), false));
            threads_req.add(t);
        }
        for(Thread t: threads_req) t.start();  // começam as threads dos ficheiros a espera de receber
        byte[] b1 = files_to_request.getBytes(StandardCharsets.UTF_8);
        DatagramPacket dp_to_receber = new DatagramPacket(b1,b1.length,dest);
        ds.send(dp_to_receber); //envia ficheiros a receber ao passivo

        for(String s: send)files_to_send = files_to_send + " " + s; //prepara a lista de ficheiros que vai enviar

        byte[] b2 = files_to_request.getBytes(StandardCharsets.UTF_8); //envia ficheiros a receber ao passivo , que lhos
        DatagramPacket dp_to_enviar = new DatagramPacket(b2,b2.length,dest);
        ds.send(dp_to_enviar);

        ds.receive(new DatagramPacket(new byte[100],100));  //recebe a confirmação e começa o processo de enviar
                                                                    //o conteudo nao importa
        for(String s: send){
            File f = new File(s);
            Thread t = new Thread(new FileHandler(f, dest, true));
            threads_send.add(t);
        }
        for(Thread t: threads_send) t.start();



        for(Thread t: threads_send) t.join();
        for(Thread t: threads_req) t.join();

        System.out.println("Requested" + req.toString());
        System.out.println("Sending" + send.toString());
        http.changeMessage("Iniciando a troca de ficheiros com "+args[1]);


    }
    public void run() {
            if(dest != null){
                try{
                    http.changeMessage("Modo Passivo");
                    this.runPassive();
                }catch (IOException | InterruptedException e){
                    e.printStackTrace();
                }
            }
            else{
                http.changeMessage("Modo Ativo");
                try {
                    this.runActive();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            this.ds.close();
        }
}

class Session{
    public static void main(String[] str) throws IOException {
        DatagramSocket ds = new DatagramSocket(5252); // sera porta 80
        DatagramSocket ds2 = new DatagramSocket();
        HttpAnswer http = new HttpAnswer();
        new Thread(http).start();
        if(str.length == 3) { // <pasta> <ip> <segredo>
            try {
                Par<Cabecalho,SocketAddress> info = Pacote.receive(ds);
                Cabecalho c = info.getFst();

                if(c.tipo == 1 && c.seq == -1) {
                    if(c.getHash() != Integer.parseInt(str[2])){
                        System.out.println("Palavra Passe Errada");
                        Pacote.enviaPacoteErro(ds,0,info.getSnd());
                    }else{
                        System.out.println("Conexão Estabelecida");
                        SessionSocket ss1 = new SessionSocket(ds2, str, info.getSnd(),http);
                        new Thread(ss1).start(); // runPassive
                    }
                }else{
                    System.out.println("Mensagem Perdida");
                }
            }catch (SocketTimeoutException e){
                SessionSocket ss2 = new SessionSocket(ds2,str,null,http);
                new Thread(ss2).start(); // runActive
            }
        }
        else{
            System.out.println("Argumentos inválidos");
        }
        http.turnOFF();
    }
}
