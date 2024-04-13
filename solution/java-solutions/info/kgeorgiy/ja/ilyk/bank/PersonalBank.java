package info.kgeorgiy.ja.ilyk.bank;

import java.rmi.RemoteException;

public interface PersonalBank extends Bank {

    Person createPerson(String name, String surname, String passportNumber) throws RemoteException, IllegalArgumentException;

    Account createAccount(Person person, String id) throws RemoteException;

    Person getRemotePerson(String passportNumber) throws RemoteException;

    Person getLocalPerson(String passportNumber) throws RemoteException;
}
