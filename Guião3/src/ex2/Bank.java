package ex2;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {

    private static class Account {
        ReentrantLock lock;
        private int balance;

        Account(int balance) {
            this.balance = balance;
        }
        int balance() {
            return balance;

        }

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

    private ReentrantReadWriteLock l = new ReentrantReadWriteLock();
    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private Lock rl = l.readLock();
    private Lock wl = l.writeLock();
    private int nextId = 0;

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        wl.lock();
        try{
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        }
        finally {
            wl.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        wl.lock();
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.lock.lock();

        }
        finally {
            wl.unlock();
        }
        try{
            return c.balance();
        }
        finally{
            c.lock.unlock();
        }

    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        rl.lock();          //uso de read lock
        try{
            c = map.get(id);
            if (c == null)
                return 0;
            c.lock.lock();
        }
        finally {
            rl.unlock();    //libertação; basicamente o mesmo processo feito no exercicio anterior
        }
        try{
            return c.balance();

        }
        finally{
            c.lock.unlock();
        }

    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        rl.lock();
        Account c;
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();
        }
        finally {
            rl.unlock();
        }

        try{
            return c.deposit(value);
        }
        finally {
            c.lock.unlock();
        }

    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        rl.lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();
        }
        finally {
            rl.unlock();
        }
        try{
            return c.withdraw(value);
        }
        finally {
            c.lock.unlock();
        }

    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        //existindo o lock global nao ha necessidade de ordenar o acesso aos locks
        Account cfrom, cto;
        rl.lock();
        try{
            cfrom = map.get(from);             //obtenção pode ser feita pela ordem que quisermos
            cto = map.get(to);                 //pois temos um read lock retido
            if (cfrom == null || cto ==  null)
                return false;

                            //como temos obtenção de multiplos locks
                            //tem que haver uma disciplina de acesso a locks
                            // visto no guiao 2
            if(from<to) {
                cfrom.lock.lock();
                cto.lock.lock();
            }
            else{
                cto.lock.lock();
                cfrom.lock.lock();
            }
        }
        finally {
            rl.unlock();
        }
        try{            //faz se o mesmo raciocinio feito no ex1 para o fim do método

            try { if (!cfrom.withdraw(value)) return false;
            }
            finally{
                cfrom.lock.unlock();
            }
            return  cto.deposit(value);
        }
        finally {

            cto.lock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        Account [] acs = new Account [ids.length];
        ids = ids.clone();
        Arrays.sort(ids);//essencial que os ids estejam ordenados para que os locks sejam feitos convenientemente
        rl.lock();
        try {
            for (int i = 0; i < ids.length; i++) {
                acs[i] = map.get(ids[i]);
                if (acs[i] == null) return 0;

            }

            for (Account c : acs) { //está ordenado
                c.lock.lock();
            }
        }
        finally{
            rl.unlock();
        }
        int total = 0;
        for (Account c: acs) {
            total += c.balance();
            c.lock.unlock();
        }
        return total;
    }

}