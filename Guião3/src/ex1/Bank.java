package ex1;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

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

    private ReentrantLock l = new ReentrantLock();
    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        l.lock();
        try{

            int id = nextId;
            nextId += 1;
            map.put(id, c);

            return id;
        }
        finally {
            l.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        l.lock();
        try {
             c = map.remove(id);
            if (c == null)
                return 0;
            c.lock.lock();

         }
        finally {
            l.unlock();
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
        l.lock();
        try{
        c = map.get(id);
        if (c == null)
            return 0;
        c.lock.lock();
        }
        finally {
            l.unlock();
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
        l.lock();
        Account c;
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();
        }
        finally {
            l.unlock();
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
        l.lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();
        }
        finally {
            l.unlock();
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
        l.lock();
        try{
        cfrom = map.get(from);
        cto = map.get(to);
        if (cfrom == null || cto ==  null)
            return false;
        cfrom.lock.lock();
        cto.lock.lock();
        }
        finally {
            l.unlock();
        }
        try{

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
        Arrays.sort(ids);
        l.lock();
        try {
            for (int i = 0; i < ids.length; i++) {
                acs[i] = map.get(ids[i]);
                if (acs[i] == null) return 0;

            }

            for (Account c : acs) {
                c.lock.lock();
            }
        }
        finally{
            l.unlock();
        }
        int total = 0;
        for (Account c: acs) {
            total += c.balance();
            c.lock.unlock();
        }
        return total;
    }

}