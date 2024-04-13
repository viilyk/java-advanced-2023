package info.kgeorgiy.ja.ilyk.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalBank implements Bank, Serializable {

    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    LocalBank(ConcurrentMap<String, Account> accounts) throws RemoteException {
        for (ConcurrentMap.Entry<String, Account> entry : accounts.entrySet()) {
            this.accounts.put(entry.getKey(), new RemoteAccount(entry.getValue()));
        }
    }

    @Override
    public Account createAccount(String id) {
        return accounts.put(id, new RemoteAccount(id));
    }

    @Override
    public Account getAccount(String id)  {
        return accounts.get(id);
    }

}
