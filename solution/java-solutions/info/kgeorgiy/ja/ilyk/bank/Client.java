package info.kgeorgiy.ja.ilyk.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        final PersonalBank bank;
        try {
            bank = (PersonalBank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect arguments, expected: <name> <surname> <passport id> <account id> <difference>");
            return;
        }

        final String name = args[0], surname= args[1], passportId = args[2], accountId = args[3];
        int difference;
        try {
             difference = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect arguments difference must be integer");
            return;
        }

        try {
            Person person = bank.createPerson(name, surname, passportId);
            Account account = person.createAccount(accountId);
            System.out.println("Account id: " + account.getId());
            System.out.println("Money: " + account.getAmount());
            System.out.println("Adding money");
            account.setAmount(account.getAmount() + difference);
            System.out.println("Money: " + account.getAmount());
        } catch (IllegalArgumentException e) {
            System.err.println("Wrong personal information. Another person with this passport id already exists.");
        }

    }
}
