import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;


class SessionSocket implements Runnable {
    private HttpAnswer http;
    private DatagramSocket ds;
    private String[] args;
    private String str_dir;
    private SocketAddress dest;

    private final String TEST_DIR = System.getProperty("user.dir");
    private final static  Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
            logr.log(Level.SEVERE,"Unable to initiate application");
        }
    }

    private void runPassive() throws IOException, InterruptedException {
        ListarFicheiro lf = new ListarFicheiro(str_dir);
        lf.atualizaListaFicheiro();
        ByteManager list_files = lf.upSerialize();

        http.changeMessage("Enviando Lista de Ficheiros de "+str_dir+" a "+dest);
        logr.log(Level.FINE,"Sending File List");
        for (int i = 1; i <= list_files.getCount(); i++)
            Pacote.enviaPacoteListaFicheiros(ds, list_files, i,dest);
        /*while (true) {
            try {
                http.changeMessage("Esperando NOACKS da Lista de Ficheiros");
                Pacote.trataACKL(ds, list_files);
            } catch (SocketTimeoutException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        int aux = 1;
        int port = ds.getLocalPort();
        Set<String> send = new TreeSet<>();
        List<Integer> seqs = new ArrayList<>();
        Cabecalho c;
        int maxcount = 0;
        for(int i = 0; i < aux ; i++){
            try {
                ds.setSoTimeout(1000); // 100 milissegundos
                c = Pacote.recebePacoteFicheirosAEnviar(ds,send);
                if(c == null)
                    return;
                aux = c.getHash(); // get numero de pacotes
                seqs.add(c.getSeq());
            }catch (SocketTimeoutException ste ){
                logr.log(Level.SEVERE,"Socket timed out", ste);
                try {
                    maxcount +=1;
                    i-=1;
                    if(maxcount == 20)
                        throw new IOException("Ligação Instável");
                    logr.log(Level.WARNING,"Unstable Connection");
                    http.changeMessage("Enviando NOACKS de Ficheiros a receber para "+args[1]);
                    logr.log(Level.INFO,"Sending NOACKS for Files to receive");
                    Pacote.pedePacotesEmFalta(ds,seqs,dest,aux,7);
                } catch (IOException e) {
                    logr.log(Level.SEVERE,"Error",e);
                    e.printStackTrace();
                }
            }catch (IOException e) {
                logr.log(Level.SEVERE,"Error",e);
                e.printStackTrace();
            }
        }

        List<Thread> threads_send = new ArrayList<>();
        List<Thread> threads_req = new ArrayList<>();

        logr.log(Level.INFO,"Śending Files");
        ds.close();
        for(String s: send){
            logr.log(Level.INFO,s);
            if(ds.isClosed())
                ds = new DatagramSocket(port);
            File f = new File(str_dir+"/"+s);
            DatagramPacket dp = new DatagramPacket(new byte[800],800);
            ds.receive(dp);
            dest = dp.getSocketAddress();
            ds = new DatagramSocket();
            ds.send(dp);

            Thread t = new Thread(new FileHandler(f, dest, true,ds.getLocalPort(),0.0));
            threads_send.add(t); //prepara threads de envio
        }
        for(Thread t: threads_send){
            t.start(); //envia-os
            t.join();
        }



/*

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
    */}

    private void runActive() throws IOException, InterruptedException {
        int password = Integer.parseInt(args[2]);
        Cabecalho c = new Cabecalho((byte) 1, -1, password);

        try {
            Pacote.pedeListaFicheiros(ds, c, InetAddress.getByName(args[1]));
        } catch (IOException e) {
            logr.log(Level.SEVERE,"Error",e);
            e.printStackTrace();
        }

        ListarFicheiro lf_A = new ListarFicheiro(str_dir);
        lf_A.atualizaListaFicheiro();
        ListarFicheiro lf_B = new ListarFicheiro();
        int aux = 1;
        int maxcount = 0;
        List<Integer> seqs = new ArrayList<>();
        http.changeMessage("Recebendo Lista de Ficheiros de "+args[1]);
        logr.log(Level.INFO,"Receiving File List");
        for(int i = 0; i < aux ; i++){
            try {
                ds.setSoTimeout(1000); // 500 milissegundos
                c = Pacote.recebePacoteListaFicheiros(ds,lf_B,i-1);
                if(c == null)
                    return;
                aux = c.getHash(); // get numero de pacotes
                seqs.add(c.getSeq());
            }catch (SocketTimeoutException ste){
                logr.log(Level.SEVERE,"Socket timed out",ste);
                try {
                    maxcount +=1;
                    i-=1;
                    if(maxcount == 20)
                        throw new IOException("Ligação Instável");
                    logr.log(Level.WARNING,"Unstable Connection");
                    http.changeMessage("Enviando NOACKS da Lista de Ficheiros para "+args[1]);
                    logr.log(Level.INFO,"Sending NOACKS about File List to"+args[1]);
                    Pacote.pedePacotesEmFalta(ds,seqs,lf_B.origem,aux,4);
                } catch (IOException e) {
                    logr.log(Level.SEVERE,"Error",e);
                    e.printStackTrace();
                }
            }catch (IOException e) {
                logr.log(Level.SEVERE,"Error",e);
                e.printStackTrace();
            }
        }
        dest = lf_B.origem;

        Set<String> send = lf_A.checkDiff(lf_B);
        Set<String> req = lf_B.checkDiff(lf_A);

        //StringBuilder files_to_request= new StringBuilder(); //para enviar o passivo
        //StringBuilder files_to_send= new StringBuilder(); //para enviar o passivo

        List<Thread> threads_send = new ArrayList<>();
        List<Thread> threads_req = new ArrayList<>();

        http.changeMessage("Iniciando a troca de ficheiros com "+args[1]);
        logr.log(Level.INFO,"Initiating File Sharing With"+args[1]);
        ByteManager files = ListarFicheiro.upSerializeV2(req);//envia ficheiros a receber ao passivo
        for (int i = 0; i < files.getCount(); i++)
            Pacote.enviaPacoteFicheirosAReceber(ds, files, i,dest);


        /*while (true) {
            try {
                http.changeMessage("Esperando NOACKS dos Ficheiros a Receber");
                Pacote.trataACKL(ds, files);
            } catch (SocketTimeoutException e) {
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        logr.log(Level.INFO,"Śending Files");
        for(String s : send){
            logr.log(Level.INFO,s);
        }

        ds.close();
        logr.log(Level.INFO,"Requesting Files");
        int idx = 0;
        for(String s: req){
            logr.log(Level.INFO,s);
            ds = new DatagramSocket();
            File f = new File(str_dir+"/"+s);
            Double tam_file;
            if(lf_A.list.containsKey(s) && lf_B.list.containsKey(s) && lf_A.list.get(s) > lf_B.list.get(s)){
                tam_file = lf_A.list.get(s);
            }else if(!lf_A.list.containsKey(s)){
                tam_file = lf_B.list.get(s);
            }else if(!lf_B.list.containsKey(s)){
                tam_file = lf_A.list.get(s);
            }else{
                tam_file = lf_B.list.get(s);
            }

            c = new Cabecalho((byte)8,idx,0);
            byte[] buf = c.outputToByte();
            DatagramPacket dp2 = new DatagramPacket(buf,buf.length,dest);
            ds.send(dp2);
            ds.receive(dp2);

            Thread t = new Thread(new FileHandler(f, dp2.getSocketAddress(), false,ds.getLocalPort(),tam_file));
            ds.close();
            threads_req.add(t);
        }
              for(Thread ts: threads_req){
            ts.start();  // começam as threads dos ficheiros a espera de receber
            ts.join();
        }
    /*
        for(String s: send){
            File f = new File(s);
            Thread t = new Thread(new FileHandler(f, dest, true,ds,0.0));
            threads_send.add(t);
        }
        for(Thread t: threads_send) t.start();



        for(Thread t: threads_send) t.join();
        for(Thread t: threads_req) t.join();
    */
        http.changeMessage("Troca de ficheiros concluída");

    }
    public void run() {
        if(dest != null){
            try{
                http.changeMessage("Modo Passivo");
                logr.log(Level.INFO,"Passive Mode");
                this.runPassive();
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
                logr.log(Level.SEVERE,"Error",e);
            }
        }
        else{
            http.changeMessage("Modo Ativo");
            logr.log(Level.INFO,"Active Mode");
            try {
                this.runActive();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                logr.log(Level.SEVERE,"Error",e);
            }
        }

        ds.close();
    }
}

class FFSync {

    public static void main(String[] str) throws IOException {
        Logger logr = Log.start();
        DatagramSocket ds = new DatagramSocket(80); // sera porta 80
        DatagramSocket ds2 = new DatagramSocket();
        HttpAnswer http = new HttpAnswer(str[1]);
        new Thread(http).start();
        Thread t = null;
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
                        logr.log(Level.INFO,"Connection Established");
                        SessionSocket ss1 = new SessionSocket(ds2, str, info.getSnd(),http);
                        t = new Thread(ss1);
                        t.start(); // runPassive
                    }
                }else{
                    System.out.println("Mensagem Perdida");
                }
            }catch (SocketTimeoutException e){
                logr.log(Level.SEVERE,"Error",e);
                SessionSocket ss2 = new SessionSocket(ds2,str,null,http);
                t = new Thread(ss2);
                t.start(); // runActive
            }
        }
        else{
            System.out.println("Argumentos inválidos");
            logr.log(Level.WARNING,"Invalid Arguments");
        }
        try{
            if(t != null) t.join();
        }catch(InterruptedException e){
            logr.log(Level.SEVERE,"Error",e);
            e.printStackTrace();
        }


        http.turnOFF();
    }
}