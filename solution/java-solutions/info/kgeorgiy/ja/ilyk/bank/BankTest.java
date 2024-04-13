package info.kgeorgiy.ja.ilyk.bank;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;;import java.rmi.RemoteException;

@RunWith(JUnit4.class)

public class BankTest {

    private static PersonalBank bank;

    @BeforeClass
    public static void beforeAllTests() throws RemoteException {
        // big open
    }

    @AfterClass
    public static void afterAllTests() {
        // big close
    }

    @Before
    public void beforeEachTest() throws RemoteException {
        // open
    }

    @After
    public void afterEachTest() throws RemoteException {
        // close
    }

    @Test
    public void test1() throws RemoteException {
        String name = "test_name_1";
        String surname = "test_surname_1_1";
        String passportId = "passportID_1";
        String accountId = "accountID_1";

        bank.createPerson(name, surname, passportId);
        Person remotePerson = bank.getRemotePerson(passportId);
        Account remoteAccount = remotePerson.getAccount(accountId);

        Person localPerson = bank.getLocalPerson(passportId);
        Account localAccount = localPerson.getAccount(accountId);
        localAccount.setAmount(100);

        Assert.assertEquals(remoteAccount.getAmount(), );
        remotePerson.createAccount(accountId)
    }

    @Test
    public void test2() {
        //test2
    }

}
