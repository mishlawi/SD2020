package ServidorConcorrente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


class ServerWorker implements Runnable{
    private Socket socket;


    public ServerWorker (Socket socket){
    this.socket = socket;
    }

@Override
public void run() {
    try{

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        int conta=0;
        int i = 0;
        String line;
        while ((line = in.readLine()) != null) {
            try {
                conta += Integer.parseInt(line);
                i++;
            }
            catch (NumberFormatException e){

            }
            out.println(conta);
            out.flush();

        }

        if(conta<1) conta = 1;

        out.print("Média de: " + conta/i);

        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }
    catch (IOException e){
        e.printStackTrace();
    }


    }
}

public class Server {

    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(12345);

        while (true) {
            Socket socket = ss.accept();
            Thread worker = new Thread(new ServerWorker(socket));
            worker.start();
        }
    }
}