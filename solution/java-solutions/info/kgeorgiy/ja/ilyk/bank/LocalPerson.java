package info.kgeorgiy.ja.ilyk.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson extends AbstractPerson {

    private final LocalBank bank;

    LocalPerson(String name, String surname, String passportId, LocalBank bank) {
        super(name, surname, passportId);
        this.bank = bank;
    }

    LocalPerson(Person person, ConcurrentMap<String, Account> accounts) throws RemoteException {
        super(person.getName(), person.getSurname(), person.getPassportId());
        this.bank = new LocalBank(accounts);
    }


    @Override
    public Account createAccount(String subId) throws RemoteException {
        return bank.createAccount(getFullId(subId));
    }

    @Override
    public Account getAccount(String subId) throws RemoteException {
        return bank.getAccount(getFullId(subId));
    }
}
