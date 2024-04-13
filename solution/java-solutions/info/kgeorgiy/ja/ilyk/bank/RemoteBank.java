package info.kgeorgiy.ja.ilyk.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements PersonalBank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<Person, ConcurrentMap<String, Account>> wallets = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            System.out.println("Creating account " + id);
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            System.out.println("Account " + id + "already exist");
            return getAccount(id);
        }
    }

    @Override
    public Account createAccount(final Person person, final String id) throws RemoteException {
        Account account = createAccount(id);
        wallets.computeIfAbsent(person, p -> new ConcurrentHashMap<>()).put(id, account); // hmm
        return account;
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Person createPerson(final String name, final String surname, final String passportId) throws RemoteException, IllegalArgumentException {
        if (!persons.containsKey(passportId)) {
            System.out.println("Creating person");
            Person person = new RemotePerson(name, surname, passportId, this);
            UnicastRemoteObject.exportObject(person, port);
            persons.put(passportId, person);
            return person;
        }
        Person person = persons.get(passportId);
        if (person.getName().equals(name) && person.getSurname().equals(surname)) {
            System.out.println("Person with passport id " + passportId + " already exists");
            return person;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Person getRemotePerson(final String passportNumber) {
        return persons.get(passportNumber);
    }

    @Override
    public Person getLocalPerson(final String passportNumber) throws RemoteException {
        Person remotePerson = persons.get(passportNumber);
        if (remotePerson == null) {
            return null;
        }
        ConcurrentMap<String, Account> personsAccounts = new ConcurrentHashMap<>(
                wallets.computeIfAbsent(remotePerson, p -> new ConcurrentHashMap<>())
        );
        return new LocalPerson(remotePerson, personsAccounts);
    }

}
