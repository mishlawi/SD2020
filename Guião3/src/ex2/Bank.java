package ex2;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

class Bank {

    private static class Account {
        private ReentrantLock lock = new ReentrantLock();
        private int balance;
        Account(int balance) { this.balance = balance; }
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

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReadWriteLock rwLock = new ReadWriteLock() {
        @Override
        public Lock readLock() {
            return null;
        }

        @Override
        public Lock writeLock() {
            return null;
        }
    };

    // create account and return account id
    public int createAccount(int balance) {
        Account c;

        c = new Account(balance);
        rwLock.writeLock().lock();
        try {
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        }
        finally {
            rwLock.writeLock().unlock();
        }

    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        rwLock.writeLock().lock();
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.lock.lock();
        }
        finally {
            rwLock.writeLock().unlock();
        }
        try {
            return c.balance();
        }
        finally {
            c.lock.unlock();
        }

    }

    // account balance; 0 if no such account
    public int balance(int id) {

        Account c;
        rwLock.readLock().lock();
        try {
            c = map.get(id);
            if (c == null)
                return 0;
            c.lock.lock();
        }
        finally {
            rwLock.readLock().unlock();
        }
        try{
            return c.balance();
        }
        finally {
            c.lock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        rwLock.readLock().lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();}
        finally {
            rwLock.readLock().unlock();
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

        rwLock.readLock().lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();

        }
        finally {
            rwLock.writeLock().unlock();
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
        Account cfrom, cto;
        rwLock.readLock().lock();
        try {
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto == null)
                return false;

            //ambas podem ter feito um readLock -> valido
            //mas como temos que obter os locks finos
            //e estes podem-se cruzar aka deadlock
            // precisamos de ordem na obtençao de locks

            if ( from<to){
              cfrom.lock.lock();
              cto.lock.lock();

            }
            else {
                cto.lock.lock();
                cfrom.lock.lock();
            }
        }
        finally {
            rwLock.readLock().unlock();
        }
        try {
            try{
                if(!cfrom.withdraw((value))) return false;
            }
            finally {
                cfrom.lock.unlock();
            }
            return cfrom.withdraw(value) && cto.deposit(value);
        }
        finally {
            cto.lock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        Account [] av = new Account[ids.length];
        ids= ids.clone();
        Arrays.sort(ids);
        rwLock.readLock().lock();
        try{
            for(int i = 0; i< ids.length; i++){
                av[i] = map.get(ids[i]);
                if(av[i] == null)
                    return 0;
            }
            for(Account c : av)
                c.lock.lock();
        }
        finally{
            rwLock.readLock().unlock();
        }
        int total = 0;

        for (Account c : av) {
            total += c.balance();
            c.lock.unlock();
        }
        return total;
    }

}