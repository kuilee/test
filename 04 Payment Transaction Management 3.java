/*
We are developing a payment transaction monitoring system that tracks accounts and their transactions. The system can compute each account's current balance and basic statistics.

Definitions:
* An "account" has a unique accountId and an owner name.
* A "transaction" represents money moving in or out of an account. DEPOSIT increases the account balance. WITHDRAWAL decreases the account balance.
* "AccountManager" manages accounts and transactions and provides balance-related methods.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for AccountManager is not passing due to a bug in the code. Make the necessary changes to AccountManager to fix the bug.
*/

/*
We are extending our payment transaction monitoring system to support basic analytics over transactions.

2) Add getAverageTransactionAmountByAccount() to AccountManager. This function returns a map from each accountId to the average transaction amount for that account.

Rules:
- Include both DEPOSIT and WITHDRAWAL transactions.
- Use the transaction amount regardless of whether the transaction is a DEPOSIT or WITHDRAWAL — do not apply any sign adjustment.
- Accounts with no transactions must not appear in the result.

To assist you in testing this new function, we have provided the testGetAverageTransactionAmountByAccount test.
*/

/*
We are extending our payment transaction monitoring system to calculate transaction fees based on business rules.

Each account is charged transaction fees as follows:
- The first 3 transactions for an account are FREE.
- From the 4th transaction onward:
  - DEPOSIT transactions cost a flat fee of $1.
  - WITHDRAWAL transactions cost a flat fee of $2.

Transactions must be processed in chronological order (by timestampSec). Fees are applied per account independently.

3) Add getTransactionFees() to AccountManager. This function returns a map from each accountId to the total transaction fees charged to that account.
- An account that has transactions but no chargeable ones must appear in the result with a fee of 0.0.
- An account with no transactions must not appear in the result.

To assist you in testing this new function, we have provided the testGetTransactionFees test.
*/

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {

    enum TransactionType {
        DEPOSIT,
        WITHDRAWAL
    }

    static class Account {
        int accountId;
        String ownerName;

        Account(int accountId, String ownerName) {
            this.accountId = accountId;
            this.ownerName = ownerName;
        }
    }

    static class Transaction {
        int transactionId;
        int accountId;
        TransactionType type;
        double amount;     // Always positive in inputs
        long timestampSec; // Unix-style seconds (monotonic for tests)

        Transaction(int transactionId, int accountId, TransactionType type, double amount, long timestampSec) {
            this.transactionId = transactionId;
            this.accountId = accountId;
            this.type = type;
            this.amount = amount;
            this.timestampSec = timestampSec;
        }
    }

     static class AccountManager {
        Map<Integer, Account> accounts = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();

        void addAccount(Account account) {
            accounts.put(account.accountId, account);
        }

        void addTransaction(Transaction tx) {
            // Assume input transactions always refer to valid accounts for this question.
            transactions.add(tx);
        }

        // Returns the current balance for the given accountId.
        double getBalance(int accountId) {
            double balance = 0.0;
            for (Transaction tx : transactions) {
                if (tx.accountId == accountId) {
                    if (tx.type == TransactionType.DEPOSIT) {
                        balance += tx.amount;
                    }
                }
            }
            return balance;
        }
    }

    public static class TestSuite {
        @Test
        public void testGetBalanceBasic() {
            AccountManager mgr = new AccountManager();
            mgr.addAccount(new Account(1, "Alice"));

            mgr.addTransaction(new Transaction(101, 1, TransactionType.DEPOSIT, 100.0, 1000));
            mgr.addTransaction(new Transaction(102, 1, TransactionType.WITHDRAWAL, 30.0, 1010));
            mgr.addTransaction(new Transaction(103, 1, TransactionType.WITHDRAWAL, 20.0, 1020));
            mgr.addTransaction(new Transaction(104, 1, TransactionType.DEPOSIT, 10.0, 1030));

            // Expected balance: 100 - 30 - 20 + 10 = 60
            assertEquals(60.0, mgr.getBalance(1), 0.0001);

            mgr.addTransaction(new Transaction(105, 1, TransactionType.WITHDRAWAL, 70.0, 1045));
            assertEquals(-10.0, mgr.getBalance(1), 0.0001);
        }

        @Test
        public void testGetBalanceMultipleAccounts() {
            AccountManager mgr = new AccountManager();
            mgr.addAccount(new Account(1, "Alice"));
            mgr.addAccount(new Account(2, "Bob"));

            mgr.addTransaction(new Transaction(201, 1, TransactionType.DEPOSIT, 50.0, 2000));
            mgr.addTransaction(new Transaction(202, 2, TransactionType.DEPOSIT, 80.0, 2005));
            mgr.addTransaction(new Transaction(203, 1, TransactionType.WITHDRAWAL, 10.0, 2010));
            mgr.addTransaction(new Transaction(204, 2, TransactionType.WITHDRAWAL, 5.5, 2015));
            mgr.addTransaction(new Transaction(205, 2, TransactionType.WITHDRAWAL, 14.5, 2020));

            // Account 1: 50 - 10 = 40
            assertEquals(40.0, mgr.getBalance(1), 0.0001);
            // Account 2: 80 - 5.5 - 14.5 = 60
            assertEquals(60.0, mgr.getBalance(2), 0.0001);
        }
        
        // Add this method inside the TestSuite class, below the existing tests.

        @Test
        public void testGetAverageTransactionAmountByAccount() {
            AccountManager mgr = new AccountManager();

            mgr.addAccount(new Account(51, "Alice"));
            mgr.addAccount(new Account(72, "Bob"));
            mgr.addAccount(new Account(93, "Charlie")); // no transactions

            // Account 51: amounts 100, 30, 20, 10 => avg = 160 / 4 = 40
            mgr.addTransaction(new Transaction(101, 51, TransactionType.DEPOSIT, 100.0, 1000));
            mgr.addTransaction(new Transaction(102, 51, TransactionType.WITHDRAWAL, 30.0, 1010));
            mgr.addTransaction(new Transaction(103, 51, TransactionType.WITHDRAWAL, 20.0, 1020));
            mgr.addTransaction(new Transaction(104, 51, TransactionType.DEPOSIT, 10.0, 1030));

            // Account 72: amounts 80, 5.5, 14.5 => avg = 100 / 3 ≈ 33.3333
            mgr.addTransaction(new Transaction(201, 72, TransactionType.DEPOSIT, 80.0, 2005));
            mgr.addTransaction(new Transaction(202, 72, TransactionType.WITHDRAWAL, 5.5, 2015));
            mgr.addTransaction(new Transaction(203, 72, TransactionType.WITHDRAWAL, 14.5, 2020));

            Map<Integer, Double> avg = mgr.getAverageTransactionAmountByAccount();

            assertEquals(40.0, avg.get(51), 0.0001);
            assertEquals(33.3333, avg.get(72), 0.0001);

            // Account 93 has no transactions — must not appear in result
            assertFalse(avg.containsKey(93));
        }
        
        // Add this method inside the TestSuite class, below the existing tests.

        @Test
        public void testGetTransactionFees() {
            AccountManager mgr = new AccountManager();

            mgr.addAccount(new Account(51, "Alice"));
            mgr.addAccount(new Account(72, "Bob"));
            mgr.addAccount(new Account(84, "Jane"));
            mgr.addAccount(new Account(97, "Frank"));
            mgr.addAccount(new Account(108, "Grace"));

            // Account 51: 5 transactions — first 3 free, then WITHDRAWAL ($2), DEPOSIT ($1)
            mgr.addTransaction(new Transaction(1, 51, TransactionType.DEPOSIT, 100.0, 1000));
            mgr.addTransaction(new Transaction(2, 51, TransactionType.WITHDRAWAL, 20.0, 1010));
            mgr.addTransaction(new Transaction(3, 51, TransactionType.DEPOSIT, 10.0, 1020));
            mgr.addTransaction(new Transaction(4, 51, TransactionType.WITHDRAWAL, 5.0, 1030));  // fee: $2
            mgr.addTransaction(new Transaction(5, 51, TransactionType.DEPOSIT, 7.0, 1040));     // fee: $1

            // Account 72: 4 transactions — first 3 free, then WITHDRAWAL ($2)
            mgr.addTransaction(new Transaction(6, 72, TransactionType.WITHDRAWAL, 50.0, 2000));
            mgr.addTransaction(new Transaction(7, 72, TransactionType.WITHDRAWAL, 10.0, 2010));
            mgr.addTransaction(new Transaction(8, 72, TransactionType.DEPOSIT, 20.0, 2020));
            mgr.addTransaction(new Transaction(9, 72, TransactionType.WITHDRAWAL, 5.0, 2030));  // fee: $2

            // Account 84: 4 transactions added out of order — must be sorted by timestamp
            mgr.addTransaction(new Transaction(26, 84, TransactionType.WITHDRAWAL, 50.0, 2000));
            mgr.addTransaction(new Transaction(27, 84, TransactionType.WITHDRAWAL, 10.0, 2010));
            mgr.addTransaction(new Transaction(28, 84, TransactionType.DEPOSIT, 20.0, 2020));   // chronologically 4th => $1
            mgr.addTransaction(new Transaction(29, 84, TransactionType.WITHDRAWAL, 5.0, 2005)); // chronologically 2nd

            // Account 97: 3 transactions — all free
            mgr.addTransaction(new Transaction(30, 97, TransactionType.WITHDRAWAL, 40.0, 3000));
            mgr.addTransaction(new Transaction(31, 97, TransactionType.DEPOSIT, 15.0, 3010));
            mgr.addTransaction(new Transaction(32, 97, TransactionType.WITHDRAWAL, 5.0, 3020));

            Map<Integer, Double> fees = mgr.getTransactionFees();

            // Account 51: $2 + $1 = $3
            assertEquals(3.0, fees.get(51), 0.0001);
            // Account 72: $2
            assertEquals(2.0, fees.get(72), 0.0001);
            // Account 84: 4th transaction (chronologically) is DEPOSIT => $1
            assertEquals(1.0, fees.get(84), 0.0001);
            // Account 97: has only free transactions
            assertEquals(0.0, fees.get(97), 0.0001);
            // Account 108: no transactions -> omitted entirely
            assertFalse(fees.containsKey(108));
            // Only accounts with transactions appear: 51, 72, 84, 97
            assertEquals(4, fees.size());
        }
    }

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestSuite.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.getTrace());
        }
        if (result.wasSuccessful()) {
            System.out.println("All tests passed successfully.");
        } else {
            System.err.println("Tests failed: " + result.getFailureCount() + " test(s) failed.");
            System.exit(1);
        }
    }
}