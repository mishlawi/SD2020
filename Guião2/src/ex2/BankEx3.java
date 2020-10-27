package ex2;

import java.util.concurrent.locks.ReentrantLock;

class BankEx3 {


    private static class Account {
        private int balance;
        ReentrantLock lock;
        Account(int balance) {
            this.balance = balance;
        this.lock= new ReentrantLock();
        }

        int balance() {
            lock.lock();
            try{
            return balance;
            }
            finally{
            lock.unlock();}
            }
        boolean deposit(int value) {
            lock.lock();
            try {
                balance += value;
                return true;
            }
            finally {
                lock.unlock();
            }
        }

        boolean withdraw(int value) {
            lock.lock();
            try {
                if (value > balance)
                    return false;
                balance -= value;
                return true;
            }
            finally{
                lock.unlock();
            }
        }
    }

    // Bank slots and vector of accounts

    private int slots;
    private Account[] av;



    public BankEx3(int n)
    {



        slots=n;
        av=new Account[slots];

        for (int i=0; i<slots; i++) av[i]=new Account(0);
    }

    // Account balance
    public int balance(int id) {
        if (id < 0 || id >= slots)
            return 0;
            return av[id].balance();
        }


    // Deposit
    boolean deposit(int id, int value) {
        if (id < 0 || id >= slots)

            return false;
            return av[id].deposit(value);
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        if (id < 0 || id >= slots)
            return false;


            return av[id].withdraw(value);

    }

    public boolean transfer(int from, int to, int value){
        if ( from<0 || from>= slots || to<0 || to>=slots )
            return false;
        //aqui entra a ordem para acesso aos locks
        if(from<to) {
            av[from].lock.lock();
            av[to].lock.lock();

        }
        else {
            av[to].lock.lock();
            av[from].lock.lock();

        }
        try{
            if(av[from].balance<value) return false;
            else {
                av[from].withdraw(value);
                av[to].deposit(value);
                return true;
            }
        }
        finally
        {
            //aqui é indiferente a ordem com que se libertam os locks
            av[to].lock.unlock();
            av[from].lock.unlock();

        }
    }

    public int totalBalance(){
        int total=0;

        for(int i=0;i<slots;i++)
            av[i].lock.lock();

        for(int i=0;i<slots;i++) {
            total += av[i].balance();
            av[i].lock.unlock();
        }

        return total;
    }

}
