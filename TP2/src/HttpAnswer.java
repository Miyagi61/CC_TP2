import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.net.InetAddress;

public class HttpAnswer implements Runnable{
    public ServerSocket ss;
    public String feedback;
    public boolean running;
    public Lock l;
    public InetAddress ip;

    HttpAnswer(String ip){
        try {
            ss = new ServerSocket(80);
        }catch (IOException e){
            e.printStackTrace();
        }
        running = true;
        feedback = "Waiting conecttion";
        this.l = new ReentrantLock();
        try{
             this.ip = InetAddress.getByName(ip);
        }catch(IOException e){
            e.printStackTrace();
        }  
    }
       

    public void changeMessage(String message){
        l.lock();
        try {
            this.feedback = "<b>"+message+"<b>";
        }finally {
            l.unlock();
        }
    }

    public void addMessage(String message){
        l.lock();
        try {
            String aux = this.feedback;
            this.feedback = aux + "<br>" + "<b>" + message + "<b>";
        }finally {
            l.unlock();
        }
    }

    public void turnOFF() throws IOException {
        running = false;
        Socket s = new Socket(ip,80);
        s.close();
    }

    @Override
    public void run() {
        try {
            while(running){
                Socket clientSocket = ss.accept();
                OutputStream clientOutput = clientSocket.getOutputStream();
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
                clientOutput.write("\r\n".getBytes());
                clientOutput.write(feedback.getBytes());
                clientOutput.write("\r\n\r\n".getBytes());
                clientOutput.flush();
                clientOutput.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
