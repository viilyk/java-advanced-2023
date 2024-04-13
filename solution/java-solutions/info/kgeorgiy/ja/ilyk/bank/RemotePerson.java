package info.kgeorgiy.ja.ilyk.bank;

import java.rmi.RemoteException;

public class RemotePerson extends AbstractPerson {

    private final RemoteBank bank;

    RemotePerson(String name, String surname, String passportNumber, RemoteBank bank) {
        super(name, surname, passportNumber);
        this.bank = bank;
    }

    @Override
    public Account createAccount(String subId) throws RemoteException {
        return bank.createAccount(this, getFullId(subId));
    }

    @Override
    public Account getAccount(String subId) throws RemoteException {
        return bank.getAccount(getFullId(subId));
    }
}
