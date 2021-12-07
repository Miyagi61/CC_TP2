import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

    class SessionSocket implements Runnable {
        private DatagramSocket socket;

        SessionSocket(DatagramSocket s) {
            this.socket = s;
        }

        public void run() {

        }
    }
    class Session{
        public static void main(String[] str) throws IOException {
            DatagramSocket ds = new DatagramSocket();
            while(true){

                Thread worker = new Thread(new SessionSocket(ds));
                worker.start();
            }
        }
    }
