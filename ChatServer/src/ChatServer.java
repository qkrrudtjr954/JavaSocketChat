import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ChatServer {
    private static final int PORT = 3000;
    private static HashSet<String> names = new HashSet<>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception{
        System.out.println("The server is running ...");
        ServerSocket listener = new ServerSocket(PORT);
        try{
            while(true){
                System.out.println("ready connection");
                new Handler(listener.accept()).start();
                System.out.println("Connected");
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread{
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out  = new PrintWriter(socket.getOutputStream(), true);

                //register name
                while(true){
                    out.println("SubmitName");
                    name = in.readLine();
                    if(name == null){
                        return ;
                    }synchronized (names){
                        if(!names.contains(name)){
                            break;
                        }
                    }
                }
                out.println("NameAccepted");
                //writers contains several out put stream for broadcasting
                writers.add(out);

                //broadcasting to whole client that connected with server stream.
                while(true){
                    String input = in.readLine();
                    if(input == null){
                        return;
                    }
                    for(PrintWriter writer : writers){
                        writer.println("Message "+ name + ": "+input);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(name!=null){
                    names.remove(name);
                }
                if(out!= null){
                    writers.remove(out);
                }
                try{
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}