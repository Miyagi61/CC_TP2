import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
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

    private void runPassive() throws IOException {
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
        http.changeMessage("A enviar Ficheiros");
    }

    private void runActive(){
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

        System.out.println("Requested" + req.toString());
        System.out.println("Sending" + send.toString());
        http.changeMessage("Iniciando a troca de ficheiros com "+args[1]);


    }
    public void run() {
            if(dest != null){
                try{
                    http.changeMessage("Modo Passivo");
                    this.runPassive();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                http.changeMessage("Modo Ativo");
                this.runActive();
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
