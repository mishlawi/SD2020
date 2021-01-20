package ex1;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Bank {

    private static class Account {
        private ReentrantLock lok = new ReentrantLock();
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

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantLock lock = new ReentrantLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        lock.lock();
        try {
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        } finally {
            lock.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {

        Account c;
        lock.lock();
        try {
            c = map.remove(id);
            if (c == null)
                return 0;
            c.lok.lock();
        } finally {
            lock.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.lok.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        lock.lock();
        try {
            c = map.get(id);
            if (c == null)
                return 0;
            c.lok.lock();
        } finally {
            lock.unlock();
        }
        try {
            return c.balance();
        } finally {
            lock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        lock.lock();
        try {
            c = map.get(id);
            if (c == null)
                return false;
            c.lok.lock();
        } finally {
            lock.unlock();
        }
        try {
            return c.deposit(value);
        } finally {
            c.lok.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        c = map.get(id);
        lock.lock();
        try {
            if (c == null)
                return false;
            c.lok.lock();
        } finally {
            lock.unlock();
        }
        try {
            return c.withdraw(value);
        } finally {
            c.lok.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        lock.lock();
        try {
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto == null)
                return false;
            cfrom.lok.lock(); //nao da deadlock pois estao dentro do lock externo do banco
            cto.lok.lock();
        } finally {
            lock.unlock();
        }
        try {
            try {
                if (!cfrom.withdraw(value)) return false;
            } finally {
                cfrom.lok.unlock();
            }
            return cto.deposit(value);
        } finally {
            cto.lok.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        Account[] acs = new Account[ids.length];
        ids = ids.clone();
        Arrays.sort(ids);
        lock.lock();
        try {
            for (int i = 0; i < ids.length; i++) {
                acs[i] = map.get(ids[i]);
                if (acs[i] == null)
                    return 0;
            }

            for (Account c : acs)
                c.lok.lock();
        } finally {
            lock.unlock();
        }
        int total = 0;
        for (Account c : acs) {
            total += c.balance();
            c.lok.unlock();
        }
        return total;
    }
}