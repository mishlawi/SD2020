package ex1;

import java.util.concurrent.locks.ReentrantLock;

class BankEx12 {


    private static class Account {
        private int balance;

        Account(int balance) {
            this.balance = balance; }
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    // Bank slots and vector of accounts
    private ReentrantLock lock = new ReentrantLock();
    private int slots;
    private Account[] av;



    public BankEx12(int n)
    {



        slots=n;
        av=new Account[slots];

        for (int i=0; i<slots; i++) av[i]=new Account(0);
    }

    // Account balance
    public int balance(int id) {
        if (id < 0 || id >= slots)
            return 0;
        lock.lock();
        try {
            return av[id].balance();
        }
        finally{
            lock.unlock();
        }
    }

    // Deposit
    boolean deposit(int id, int value) {
        if (id < 0 || id >= slots)

            return false;
        lock.lock();
        try {
            return av[id].deposit(value);
        }
        finally{
            lock.unlock();
        }
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        if (id < 0 || id >= slots)
            return false;
        lock.lock();
        try {
            return av[id].withdraw(value);
        }
        finally {
            lock.unlock();
        }
    }

    public boolean transfer(int from, int to, int value){
        if ( from<0 || from>= slots || to<0 || to>=slots )
            return false;

        lock.lock();
        try{
            av[from].withdraw(value);
            av[to].deposit(value);
            return true;
        }
        finally
        {
            lock.unlock();

        }
    }

    public int totalBalance(){
        int total=0;
        lock.lock();
        for(int i=0;i<slots;i++)
            total+=av[i].balance();
        lock.unlock();

        return total;
    }

}
