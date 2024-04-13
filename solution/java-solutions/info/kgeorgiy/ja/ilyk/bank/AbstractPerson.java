package info.kgeorgiy.ja.ilyk.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public abstract class AbstractPerson implements Person, Serializable {
    String name, surname, passportId;

    AbstractPerson(String name, String surname, String passportId) {
        this.name = name;
        this.surname = surname;
        this.passportId = passportId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassportId() {
        return passportId;
    }

    @Override
    public abstract Account createAccount(String subId) throws RemoteException;

    @Override
    public abstract Account getAccount(String subId) throws RemoteException;

    protected String getFullId(String subId) throws RemoteException {
        return passportId + ":" + subId;
    }
}
