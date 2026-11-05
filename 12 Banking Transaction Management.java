/*
We are building a program to manage a bank's imported transactions. The system tracks transactions from multiple sources (partner institutions, customer statement uploads, legacy migrations), each belonging to an account. The program allows staff to import transactions, query per-account summaries, and reconcile activity across sources.

Definitions:
* A "transaction" is an object that represents a single financial event. It has properties for the transaction ID, account ID, amount (always positive), transaction type, timestamp (day number since a reference point), description, and source.
* An "import system" is a class used for managing all imported transactions for the bank.
* A "credit" is a transaction type that increases the account balance. DEPOSIT and INTEREST are credits.
* A "debit" is a transaction type that decreases the account balance. WITHDRAWAL, FEE, and TRANSFER are debits.
* netChange represents the net movement in an account's balance across all its transactions. It is positive if credits exceed debits, and negative if debits exceed credits.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for ImportSystem is not passing due to a bug in the code. Make the necessary changes to ImportSystem to fix the bug.
*/


/*
As transactions are imported, the system needs to record each one and update the account's running balance based on transaction direction (credits add, debits subtract). The system also needs to report activity summaries broken down by transaction type.

Definitions (in addition to those from Q1):
- balances is a mapping on ImportSystem from each accountId to its current running balance. New accounts start at $0 before their first transaction.

Add three things to the system:

2-1) Extend the ImportSystem class so it maintains a balances attribute — a mapping from accountId to running balance, initially empty.

2-2) Change addTransaction(transaction) so that, in addition to appending the given Transaction, it also updates the account's balance based on the transaction's direction. Credits (DEPOSIT, INTEREST) add the amount; debits (WITHDRAWAL, FEE, TRANSFER) subtract it. If the account is new (not yet in balances), initialize it to $0 before applying the change.

2-3) Implement getTypeSummary() that returns a mapping from transaction type name (a string, uppercase, matching the enum name exactly — for example "DEPOSIT") to an inner mapping with keys "count" (integer) and "total_amount" (double). Aggregate across all accounts.
Only include types that have at least one transaction; omit types that have none.

The testAddTransaction and testGetTypeSummary tests are provided.
*/

/*
Banks monitor accounts for the first overdraft event. When an account first goes negative, that moment triggers overdraft fees, customer alerts, and compliance review. Because transactions may be imported from multiple sources and can arrive out of chronological order, the system must reason about actual event ordering (by timestamp), not the order in which transactions happened to be recorded.

3-1) Implement findFirstOverdraft(accountId) on ImportSystem. This method examines the given account's transactions and returns the Transaction object that first causes the account's running balance to go negative, or null if the balance never goes negative.

Rules:
- Consider only transactions belonging to the given accountId.
- The starting balance for any account is $0.
- Credits (DEPOSIT, INTEREST) add amount to the running balance. Debits (WITHDRAWAL, FEE, TRANSFER) subtract amount.
- "Overdraft" means the running balance goes strictly below $0 after applying a transaction. A balance of exactly $0 is not overdraft.
- Return the first transaction (in chronological order) whose application causes the balance to go negative. If no such transaction exists, return null.

To assist you in testing this new function, we have provided the testFindFirstOverdraft tests.
*/

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {

    /**
     * TransactionType represents the category of a financial event.
     * DEPOSIT and INTEREST are credits (they increase account balance).
     * WITHDRAWAL, FEE, and TRANSFER are debits (they decrease account balance).
     */
    enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        FEE,
        INTEREST,
    }

    /** Data about a single financial transaction. */
    static class Transaction {
        final String transactionId;
        final int accountId;
        final double amount;
        final TransactionType transactionType;
        final int timestamp;
        final String description;
        final String source;

        Transaction(String transactionId, int accountId, double amount,
                    TransactionType transactionType, int timestamp,
                    String description, String source) {
            this.transactionId = transactionId;
            this.accountId = accountId;
            this.amount = amount;
            this.transactionType = transactionType;
            this.timestamp = timestamp;
            this.description = description;
            this.source = source;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Transaction t)) return false;
            return transactionId.equals(t.transactionId)
                    && accountId == t.accountId
                    && Double.compare(amount, t.amount) == 0
                    && transactionType == t.transactionType
                    && timestamp == t.timestamp
                    && description.equals(t.description)
                    && source.equals(t.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, accountId, amount, transactionType,
                                timestamp, description, source);
        }

        @Override
        public String toString() {
            return "Transaction ID: " + transactionId
                    + ", Account: " + accountId
                    + ", Amount: " + amount
                    + ", Type: " + transactionType
                    + ", Timestamp: " + timestamp
                    + ", Description: " + description
                    + ", Source: " + source;
        }
    }

    /**
     * Data for managing imported bank transactions, and methods which staff
     * can use to perform any queries or updates.
     */
    static class ImportSystem {
        final ArrayList<Transaction> transactions = new ArrayList<>();

        /** Adds a transaction to the system */
        void addTransaction(Transaction transaction) {
            transactions.add(transaction);
        }

        /** Calculates and returns summary statistics for a given account */
        Map<String, Object> getAccountSummary(int accountId) {
            double totalCredits = 0;
            double totalDebits = 0;
            int transactionCount = 0;
            for (Transaction t : transactions) {
                if (t.accountId != accountId) continue;
                transactionCount++;
                if (t.transactionType == TransactionType.DEPOSIT
                        || t.transactionType == TransactionType.INTEREST) {
                    totalCredits += t.amount;
                } else if (t.transactionType == TransactionType.WITHDRAWAL
                        || t.transactionType == TransactionType.FEE
                        || t.transactionType == TransactionType.TRANSFER) {
                    totalDebits += t.amount;
                }
            }

            double netChange = totalCredits + totalDebits;

            Map<String, Object> result = new HashMap<>();
            result.put("total_credits", totalCredits);
            result.put("total_debits", totalDebits);
            result.put("net_change", netChange);
            result.put("transaction_count", transactionCount);
            return result;
        }
    }


    public static class TestSuite {

        @Test
        public void testTransaction() {
            Transaction t = new Transaction(
                "t001", 101, 500.00, TransactionType.DEPOSIT, 1, "Salary", "SRC_A");
            assertEquals("t001", t.transactionId);
            assertEquals(101, t.accountId);
            assertEquals(500.00, t.amount, 0.001);
            assertEquals(TransactionType.DEPOSIT, t.transactionType);
            assertEquals(1, t.timestamp);
            assertEquals("Salary", t.description);
            assertEquals("SRC_A", t.source);
        }

        @Test
        public void testAccountSummary() {
            ImportSystem system = new ImportSystem();

            // Account 101: 6 transactions covering all 5 types
            system.addTransaction(new Transaction("t001", 101, 500.00, TransactionType.DEPOSIT,    1, "Salary",        "SRC_A"));
            system.addTransaction(new Transaction("t002", 101, 300.00, TransactionType.DEPOSIT,    2, "Refund",        "SRC_A"));
            system.addTransaction(new Transaction("t003", 101, 150.00, TransactionType.WITHDRAWAL, 3, "ATM",           "SRC_A"));
            system.addTransaction(new Transaction("t004", 101,  50.00, TransactionType.FEE,        4, "Service fee",   "SRC_A"));
            system.addTransaction(new Transaction("t005", 101, 200.00, TransactionType.TRANSFER,   5, "Wire out",      "SRC_A"));
            system.addTransaction(new Transaction("t006", 101,  20.00, TransactionType.INTEREST,   6, "Interest paid", "SRC_A"));

            // Account 202: 2 transactions to verify per-account filtering
            system.addTransaction(new Transaction("t007", 202, 100.00, TransactionType.DEPOSIT,    7, "Deposit",       "SRC_A"));
            system.addTransaction(new Transaction("t008", 202,  30.00, TransactionType.WITHDRAWAL, 8, "Withdrawal",    "SRC_A"));

            // Expected: credits 820, debits 400, net 420
            Map<String, Object> summary101 = system.getAccountSummary(101);
            assertEquals(820.00, (double) summary101.get("total_credits"), 0.001);
            assertEquals(400.00, (double) summary101.get("total_debits"), 0.001);
            assertEquals(420.00, (double) summary101.get("net_change"), 0.001);
            assertEquals(6, (int) summary101.get("transaction_count"));

            // Expected: credits 100, debits 30, net 70
            Map<String, Object> summary202 = system.getAccountSummary(202);
            assertEquals(100.00, (double) summary202.get("total_credits"), 0.001);
            assertEquals(30.00, (double) summary202.get("total_debits"), 0.001);
            assertEquals(70.00, (double) summary202.get("net_change"), 0.001);
            assertEquals(2, (int) summary202.get("transaction_count"));
        }
        
        @Test
        public void testAddTransaction() {
            ImportSystem system = new ImportSystem();
            assertEquals(0, system.balances.size());

            // A first deposit to a new account.
            system.addTransaction(new Transaction("t001", 101, 500.00, TransactionType.DEPOSIT, 1, "Salary", "SRC_A"));
            assertEquals(1, system.transactions.size());
            assertEquals(500.00, system.balances.get(101), 0.001);

            // A cash withdrawal from the same account.
            system.addTransaction(new Transaction("t002", 101, 150.00, TransactionType.WITHDRAWAL, 2, "ATM", "SRC_A"));
            assertEquals(2, system.transactions.size());
            assertEquals(350.00, system.balances.get(101), 0.001);

            // A service fee charged to the same account.
            system.addTransaction(new Transaction("t003", 101, 50.00, TransactionType.FEE, 3, "Service fee", "SRC_A"));
            assertEquals(3, system.transactions.size());
            assertEquals(300.00, system.balances.get(101), 0.001);

            // Monthly interest credited to the same account.
            system.addTransaction(new Transaction("t004", 101, 20.00, TransactionType.INTEREST, 4, "Interest paid", "SRC_A"));
            assertEquals(4, system.transactions.size());
            assertEquals(320.00, system.balances.get(101), 0.001);

            // A deposit to a different, brand-new account.
            system.addTransaction(new Transaction("t005", 202, 200.00, TransactionType.DEPOSIT, 5, "Deposit", "SRC_A"));
            assertEquals(5, system.transactions.size());
            assertEquals(200.00, system.balances.get(202), 0.001);
            assertEquals(320.00, system.balances.get(101), 0.001);

            // A wire transfer out of the second account.
            system.addTransaction(new Transaction("t006", 202, 50.00, TransactionType.TRANSFER, 6, "Wire", "SRC_A"));
            assertEquals(6, system.transactions.size());
            assertEquals(150.00, system.balances.get(202), 0.001);

            // A brand-new account whose first transaction is a debit.
            system.addTransaction(new Transaction("t007", 303, 40.00, TransactionType.FEE, 7, "Setup fee", "SRC_A"));
            assertEquals(7, system.transactions.size());
            assertEquals(-40.00, system.balances.get(303), 0.001);
        }

        @Test
        public void testGetTypeSummary() {
            ImportSystem system = new ImportSystem();

            // A mix of transactions across two accounts covering all five types.
            system.addTransaction(new Transaction("t001", 101, 500.00, TransactionType.DEPOSIT,    1, "Salary",       "SRC_A"));
            system.addTransaction(new Transaction("t002", 101, 300.00, TransactionType.DEPOSIT,    2, "Refund",       "SRC_A"));
            system.addTransaction(new Transaction("t003", 202, 800.00, TransactionType.DEPOSIT,    3, "Big deposit",  "SRC_A"));
            system.addTransaction(new Transaction("t004", 101, 150.00, TransactionType.WITHDRAWAL, 4, "ATM",          "SRC_A"));
            system.addTransaction(new Transaction("t005", 202,  50.00, TransactionType.WITHDRAWAL, 5, "ATM",          "SRC_A"));
            system.addTransaction(new Transaction("t006", 101,  50.00, TransactionType.FEE,        6, "Service fee",  "SRC_A"));
            system.addTransaction(new Transaction("t007", 202, 200.00, TransactionType.TRANSFER,   7, "Wire",         "SRC_A"));
            system.addTransaction(new Transaction("t008", 101,  20.00, TransactionType.INTEREST,   8, "Interest",     "SRC_A"));

            Map<String, Map<String, Object>> expected = new HashMap<>();
            Map<String, Object> deposit = new HashMap<>();
            deposit.put("count", 3);
            deposit.put("total_amount", 1600.00);
            expected.put("DEPOSIT", deposit);
            Map<String, Object> withdrawal = new HashMap<>();
            withdrawal.put("count", 2);
            withdrawal.put("total_amount", 200.00);
            expected.put("WITHDRAWAL", withdrawal);
            Map<String, Object> fee = new HashMap<>();
            fee.put("count", 1);
            fee.put("total_amount", 50.00);
            expected.put("FEE", fee);
            Map<String, Object> transfer = new HashMap<>();
            transfer.put("count", 1);
            transfer.put("total_amount", 200.00);
            expected.put("TRANSFER", transfer);
            Map<String, Object> interest = new HashMap<>();
            interest.put("count", 1);
            interest.put("total_amount", 20.00);
            expected.put("INTEREST", interest);
            assertEquals(expected, system.getTypeSummary());

            // A separate system where only one transaction type is used.
            ImportSystem system2 = new ImportSystem();
            system2.addTransaction(new Transaction("t100", 300, 100.00, TransactionType.DEPOSIT, 1, "First",  "SRC_A"));
            system2.addTransaction(new Transaction("t101", 300, 200.00, TransactionType.DEPOSIT, 2, "Second", "SRC_A"));

            Map<String, Map<String, Object>> expected2 = new HashMap<>();
            Map<String, Object> depositOnly = new HashMap<>();
            depositOnly.put("count", 2);
            depositOnly.put("total_amount", 300.00);
            expected2.put("DEPOSIT", depositOnly);
            assertEquals(expected2, system2.getTypeSummary());
        }

        // Add inside the existing TestSuite class:

// Test that chronological order (by timestamp) is used, not insertion order, when
// finding the first overdraft-causing transaction.
@Test
public void testFindFirstOverdraft1() {
    ImportSystem system = new ImportSystem();

    // Inserted out of timestamp order. In chronological order (by timestamp):
    //   t1 (ts=1) DEPOSIT 100  -> balance 100
    //   t2 (ts=2) WITHDRAWAL 150 -> balance -50  <-- FIRST overdraft
    //   t3 (ts=3) DEPOSIT 200  -> balance 150
    // If the method naively scans insertion order, it would see t3 first and never flag t2.
    system.addTransaction(new Transaction("t2", 101, 150.00, TransactionType.WITHDRAWAL, 2, "ATM",   "SRC_A"));
    system.addTransaction(new Transaction("t3", 101, 200.00, TransactionType.DEPOSIT,    3, "Refund", "SRC_A"));
    system.addTransaction(new Transaction("t1", 101, 100.00, TransactionType.DEPOSIT,    1, "Salary", "SRC_A"));

    Transaction first = system.findFirstOverdraft(101);
    assertNotNull(first);
    assertEquals("t2", first.transactionId); // the WITHDRAWAL at ts=2
    assertEquals(2, first.timestamp);
}

// Test that a balance landing exactly on $0 is NOT an overdraft, and that the
// account going negative only later returns that later transaction.
@Test
public void testFindFirstOverdraft2() {
    ImportSystem system = new ImportSystem();

    // Chronological order:
    //   ts=1 DEPOSIT 100        -> 100
    //   ts=2 WITHDRAWAL 100     -> 0    (exactly zero: NOT overdraft)
    //   ts=3 FEE 10             -> -10  <-- first overdraft
    system.addTransaction(new Transaction("a", 202, 100.00, TransactionType.DEPOSIT,    1, "Salary", "SRC_A"));
    system.addTransaction(new Transaction("b", 202, 100.00, TransactionType.WITHDRAWAL, 2, "ATM",    "SRC_A"));
    system.addTransaction(new Transaction("c", 202,  10.00, TransactionType.FEE,        3, "Fee",    "SRC_A"));

    Transaction first = system.findFirstOverdraft(202);
    assertNotNull(first);
    assertEquals("c", first.transactionId);
    assertEquals(3, first.timestamp);
}

// Test that an account whose balance never goes strictly negative returns null,
// including the case where it ends exactly at $0.
@Test
public void testFindFirstOverdraft3() {
    ImportSystem system = new ImportSystem();

    // Chronological order: DEPOSIT 100 -> 100, WITHDRAWAL 60 -> 40, FEE 40 -> 0 (never negative).
    system.addTransaction(new Transaction("x", 303, 100.00, TransactionType.DEPOSIT,    1, "Salary", "SRC_A"));
    system.addTransaction(new Transaction("y", 303,  60.00, TransactionType.WITHDRAWAL, 2, "ATM",    "SRC_A"));
    system.addTransaction(new Transaction("z", 303,  40.00, TransactionType.FEE,        3, "Fee",    "SRC_A"));

    assertNull(system.findFirstOverdraft(303));

    // Unknown account -> null.
    assertNull(system.findFirstOverdraft(999));
}

// Test that other accounts' transactions are ignored, and that a first transaction
// which is a debit immediately goes negative.
@Test
public void testFindFirstOverdraft4() {
    ImportSystem system = new ImportSystem();

    // Account 404: first transaction (chronologically) is a debit -> immediate overdraft.
    system.addTransaction(new Transaction("d1", 404, 25.00, TransactionType.TRANSFER, 1, "Wire",   "SRC_A"));
    // Account 505: unrelated activity that would go negative if wrongly included.
    system.addTransaction(new Transaction("d2", 505, 999.00, TransactionType.WITHDRAWAL, 1, "Huge", "SRC_A"));

    Transaction first = system.findFirstOverdraft(404);
    assertNotNull(first);
    assertEquals("d1", first.transactionId);

    // Account 505 on its own also overdrafts on its first transaction.
    Transaction first505 = system.findFirstOverdraft(505);
    assertNotNull(first505);
    assertEquals("d2", first505.transactionId);

    // Account 606 has no transactions -> null.
    assertNull(system.findFirstOverdraft(606));
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