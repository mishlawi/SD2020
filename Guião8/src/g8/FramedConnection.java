    package g8;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class FramedConnection implements AutoCloseable{

    private final Socket socket;
    private final DataInputStream inStream;
    private final DataOutputStream outStream;
    //temos zona de escrita que é o outputStream --> writeLock
    //temos zona de leitura que é o InputStream --> readLock

    //Estes locks nao sao iguais aos ReadWriteLocks
    private final Lock wLock = new ReentrantLock(); //writeLock
    private final Lock rLock = new ReentrantLock(); //readLock

        public FramedConnection(Socket socket) throws IOException{
            this.socket = socket;
            this.inStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }

        public void send (byte[] data) throws IOException{

            //obtemos lock de escrita pq vamos escrever no outputStream
            wLock.lock();
            try{
                outStream.writeInt(data.length); // escreve-se para o outputStream o tamanho do array data
                //será que um outStream.flush() seria boa ideia imediatamente depois do writeInt?

                outStream.write(data); //lemos os dados
                outStream.flush(); //boa ideia
            }
            finally {
                wLock.unlock();
            }

        }

        public byte[] receive() throws IOException {
            byte[] data;

            try {
                rLock.lock();
                data = new byte[inStream.readInt()];
                inStream.readFully(data);
                // readFully vai ter em atençao o tamanho do array data
                // e garantir que tenta ler toda a quantidade de dados ate preencher o array

            } finally {
                rLock.unlock();
            }
            return data;
        }


        @Override
        public void close() throws Exception {
            socket.close();

        }
    }
