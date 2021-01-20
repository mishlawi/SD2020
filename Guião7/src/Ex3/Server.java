package Ex3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


class ContactList {
    private List<Ex3.Contact> contacts;
    private ReentrantLock lock = new ReentrantLock();

    public ContactList() {
        contacts = new ArrayList<>();
        contacts.add(new Ex3.Contact("John", 20, 253123321, null, new ArrayList<>(Arrays.asList("john@mail.com"))));
        contacts.add(new Ex3.Contact("Alice", 30, 253987654, "CompanyInc.", new ArrayList<>(Arrays.asList("alice.personal@mail.com", "alice.business@mail.com"))));
        contacts.add(new Ex3.Contact("Bob", 40, 253123456, "Comp.Ld", new ArrayList<>(Arrays.asList("bob@mail.com", "bob.work@mail.com"))));
    }


    public boolean addContact (DataInputStream in) throws IOException {
        Ex3.Contact contact = Ex3.Contact.deserialize(in);
        lock.lock();
        try{
            if(contact!=null) {
                contacts.add(contact);
                return true;
            }
            else
                return false;
        }
        finally {
            lock.unlock();
        }
    }


    public void getContacts (DataOutputStream out) throws IOException {
        lock.lock();
        try {
            out.writeInt(contacts.size());
            out.flush();
            for(Contact c: this.contacts){
                c.serialize(out);
            }

        }
        finally {
            lock.unlock();
        }
    }


//para debug
public void printContacts(){
    for(Ex3.Contact c: this.contacts){
        System.out.println(c.toString());}
    }
}


//--------------------------------------------------------------------//
class ReaderWorker implements Runnable{
    private Socket socket;
    private ContactList contacts;

    public ReaderWorker ( Socket socket, ContactList contactList){
        this.socket = socket;
        this.contacts = contactList;
    }

    public void run(){
        try{
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            contacts.getContacts(out);

            socket.shutdownOutput();
            socket.close();
        }

        catch (IOException e){
            e.printStackTrace();
        }
    }
}


class ServerSocketReaderWorker implements Runnable {
    private ServerSocket serverSocket;
    private ContactList contactList;

    public ServerSocketReaderWorker(int port, ContactList contactList) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.contactList = contactList;
    }


    public void run() {
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                Thread worker = new Thread(new ReaderWorker(socket, contactList));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

//--------------------------------------------------------------------//
class WriterWorker implements Runnable{
    private Socket socket;
    private ContactList contacts;

    public WriterWorker ( Socket socket, ContactList contactList){
        this.socket = socket;
        this.contacts = contactList;
    }

        // @TODO
        @Override
        public void run() {

            try {

                //usamos so o input stream, info enviada pelo cliente;
                DataInputStream in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));

                boolean open = true;

                while(open)
                    contacts.addContact(in);


                contacts.printContacts();

                socket.shutdownInput();
                socket.close();

                System.out.println("Conexão terminada.");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

class ServerSocketWriterWorker implements Runnable{
    private ServerSocket serverSocket;
    private ContactList contactList;

    public ServerSocketWriterWorker(int port,ContactList contactList)throws IOException{
        this.serverSocket = new ServerSocket(port);
        this.contactList = contactList;
    }

    public void run(){
        while(true){
            Socket socket;
            try{
                socket = serverSocket.accept();
                Thread worker = new Thread (new WriterWorker(socket,contactList));
                worker.start();
            }

            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
//--------------------------------------------------------------------//

    public class Server {

        public static void main(String[] args) throws IOException {
            ContactList contactList = new ContactList();

            Thread writer_worker = new Thread(new ServerSocketWriterWorker(12345, contactList));
            Thread reader_worker = new Thread(new ServerSocketReaderWorker(34567, contactList));

            writer_worker.start();
            reader_worker.start();

        }

    }
