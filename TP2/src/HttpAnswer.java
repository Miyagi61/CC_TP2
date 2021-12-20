import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpAnswer implements Runnable{
    public ServerSocket ss;
    public String feedback;
    public boolean running;

    HttpAnswer(){
        try {
            ss = new ServerSocket(80);
        }catch (IOException e){
            e.printStackTrace();
        }
        running = true;
        feedback = "Running";
    }

    public void changeMessage(String message){
        this.feedback = message;
    }

    public void turnOFF(InetAddress ip) throws IOException {
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
                String str = "<b>"+feedback+"<b>";
                clientOutput.write(str.getBytes());
                clientOutput.write("\r\n\r\n".getBytes());
                clientOutput.flush();
                clientOutput.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
