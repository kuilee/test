/*
We are building Customer Sync Queue, a back-end system that sends customer changes to an external CRM.

Definitions:
* A "customer" is an object representing a person or account whose profile is mirrored in the external CRM. A customer has:
  - customerId
  - name
  - email
  - phone
  - company
  - lastSyncedValues (the most recent value of each field that's actually been confirmed synced to the CRM)

* A "queued update" is an object representing a single pending change to one field of a customer, waiting to be pushed to the external CRM. A queued update has:
  - updateId
  - customerId
  - fieldName
  - newValue
  - createdAt
  - status
  - attempts
  - errorType

* A queued update's status can be one of:
  - PENDING
  - PROCESSING
  - SUCCEEDED
  - FAILED
  - CANCELLED

* An update with status FAILED also carries an errorType of either RETRYABLE (a transient failure worth trying again) or NON_RETRYABLE (a permanent rejection that won't succeed on retry).

* Only PENDING updates and retryable FAILED updates are eligible for synchronization.

The CustomerSyncManager class manages customers and queued updates.

To begin with, we present you with two tasks:

1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for CustomerSyncManager is not passing due to a bug in the code. Make the necessary changes to CustomerSyncManager to fix the bug.

-----------------------------------------------------------

We are extending Customer Sync Queue to let support agents queue a customer field change, or cancel one that's no longer needed. This keeps the sync queue accurate before changes reach the CRM.

Add two functions to the CustomerSyncManager class.

2-1) queueUpdate

The queueUpdate function takes a customer ID, a field name, a new value, and a created-at timestamp. It should add a new pending update only when all of the following conditions are satisfied:
- The customer exists.
- The field is one of the supported fields: name, email, phone, company.
- The new value differs from the customer's current value.

A new update should receive the next available update ID, have status PENDING, have zero attempts, and initially have no error type. Ignore the request when any validation fails.

2-2) cancelPendingUpdates

The cancelPendingUpdates function takes a customer ID and a field name. It should cancel all PENDING updates for that customer and field, changing their status to CANCELLED. Updates with any other status must be left untouched.

Return the number of updates cancelled, or 0 if the customer does not exist or no matching updates are found.

*/

/*
We now need to record what the external CRM tells us after we try to sync a change. This lets the system know whether to mark it done, retry it later, or flag it as permanently failed.

3-1) Add recordSyncResult() to the CustomerSyncManager class.

The recordSyncResult function takes an update ID, a succeeded flag, and a retryable flag. It should record the outcome of a sync attempt, following these rules:
- Unknown update IDs are ignored.
- Only updates with status PENDING or PROCESSING can be recorded.
- The update's attempt count is incremented whenever a valid result is recorded.
- When succeeded is true, the update's status is set to SUCCEEDED, its stored error is cleared, and the new value is written to both the customer's field and lastSyncedValues.
- When succeeded is false, the update's status is set to FAILED, with the error type set based on the retryable flag (RETRYABLE if retryable is true, NON_RETRYABLE otherwise).
- Return true when a result was recorded, and false when the update doesn't exist, isn't in a processable state, the customer no longer exists, or the update refers to an unsupported field.

To assist you in testing this new function, we have provided the testRecordSyncResult tests.
*/

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

class Customer {
  String customerId;
  String name;
  String email;
  String phone;
  String company;
  Map<String, String> lastSyncedValues;

  /** Creates a customer record with the fields tracked for CRM sync. */
  public Customer(
    String customerId,
    String name,
    String email,
    String phone,
    String company,
    Map<String, String> lastSyncedValues
  ) {
    this.customerId = customerId;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.company = company;
    this.lastSyncedValues = lastSyncedValues;
  }
}

class QueuedUpdate {
  int updateId;
  String customerId;
  String fieldName;
  String newValue;
  int createdAt;
  String status;
  int attempts;
  String errorType;

  /** Creates a queued update with no prior attempts and no error type. */
  public QueuedUpdate(
    int updateId,
    String customerId,
    String fieldName,
    String newValue,
    int createdAt,
    String status
  ) {
    this(updateId, customerId, fieldName, newValue, createdAt, status, 0, null);
  }

  /** Creates a queued update representing one field change awaiting sync to the CRM. */
  public QueuedUpdate(
    int updateId,
    String customerId,
    String fieldName,
    String newValue,
    int createdAt,
    String status,
    int attempts,
    String errorType
  ) {
    this.updateId = updateId;
    this.customerId = customerId;
    this.fieldName = fieldName;
    this.newValue = newValue;
    this.createdAt = createdAt;
    this.status = status;
    this.attempts = attempts;
    this.errorType = errorType;
  }
}

class CustomerSyncManager {
  static final Set<String> SUPPORTED_FIELDS = new HashSet<>(
    Arrays.asList("name", "email", "phone", "company")
  );

  Map<String, Customer> customers = new HashMap<>();
  Map<Integer, QueuedUpdate> updates = new HashMap<>();
  int nextUpdateId = 1;

  /** Counts how many queued updates are currently eligible for synchronization (pending, or failed and retryable). */
  public int countPendingUpdates() {
    int count = 0;

    for (QueuedUpdate update : updates.values()) {
      if (!update.status.equals("SUCCEEDED")) {
        count++;
      }
    }

    return count;
  }
}

public class Solution {

  public static class TestSuite {

    /** Test pending and retryable failed updates. */
    @Test
    public void testCountPendingUpdates1() {
      // Pending update.
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "PENDING"));
      int result = manager.countPendingUpdates();
      assertEquals(1, result);

      // Retryable failed update.
      manager = new CustomerSyncManager();
      manager.updates.put(
        1,
        new QueuedUpdate(1, "C-1", "phone", "555-0192", 10, "FAILED", 1, "RETRYABLE")
      );
      result = manager.countPendingUpdates();
      assertEquals(1, result);
    }

    /** Test cancelled, non-retryable failed, and processing updates. */
    @Test
    public void testCountPendingUpdates2() {
      // Cancelled update.
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "CANCELLED"));
      int result = manager.countPendingUpdates();
      assertEquals(0, result);

      // Non-retryable failed update.
      manager = new CustomerSyncManager();
      manager.updates.put(
        1,
        new QueuedUpdate(1, "C-1", "phone", "555-0192", 10, "FAILED", 3, "NON_RETRYABLE")
      );
      result = manager.countPendingUpdates();
      assertEquals(0, result);

      // Processing update.
      manager = new CustomerSyncManager();
      manager.updates.put(
        1,
        new QueuedUpdate(1, "C-1", "email", "asha@example.com", 10, "PROCESSING")
      );
      result = manager.countPendingUpdates();
      assertEquals(0, result);
    }

    /** Test a mix of statuses across multiple customers and updates. */
    @Test
    public void testCountPendingUpdates3() {
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "PENDING"));
      manager.updates.put(
        2,
        new QueuedUpdate(2, "C-1", "phone", "555-0192", 12, "FAILED", 1, "RETRYABLE")
      );
      manager.updates.put(
        3,
        new QueuedUpdate(3, "C-1", "email", "asha@example.com", 14, "CANCELLED")
      );
      manager.updates.put(4, new QueuedUpdate(4, "C-1", "company", "Acme", 16, "SUCCEEDED"));
      manager.updates.put(5, new QueuedUpdate(5, "C-2", "name", "Ravi Kapoor", 18, "PROCESSING"));
      manager.updates.put(
        6,
        new QueuedUpdate(6, "C-2", "phone", "555-0111", 20, "FAILED", 2, "NON_RETRYABLE")
      );

      int result = manager.countPendingUpdates();

      // Mixed statuses across customers.
      assertEquals(2, result);
    }

    /** Test an empty collection of updates. */
    @Test
    public void testCountPendingUpdates4() {
      CustomerSyncManager manager = new CustomerSyncManager();

      int result = manager.countPendingUpdates();

      assertEquals(0, result);
    }
    
    private static Map<String, String> lastSyncedValues(
      String name, String email, String phone, String company
    ) {
      Map<String, String> map = new HashMap<>();
      map.put("name", name);
      map.put("email", email);
      map.put("phone", phone);
      map.put("company", company);
      return map;
    }

    // Test that a valid update is queued as PENDING with correct fields, and that
    // successive queued updates each receive the next available update ID.
    @Test
    public void testQueueUpdate1() {
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.customers.put(
        "C-1",
        new Customer(
          "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
          lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
        )
      );

      // Valid update.
      QueuedUpdate first = manager.queueUpdate("C-1", "name", "Asha Rao", 10);

      assertNotNull(first);
      assertEquals(1, first.updateId);
      assertEquals("PENDING", first.status);
      assertEquals(0, first.attempts);
      assertNull(first.errorType);
      assertTrue(manager.updates.containsKey(1));

      // Second update.
      QueuedUpdate second = manager.queueUpdate("C-1", "phone", "555-0192", 12);

      assertEquals(2, second.updateId);
      assertEquals(2, manager.updates.size());
    }

    // Test that queueUpdate rejects a same-value update, an unsupported field, and a
    // non-existent customer.
    @Test
    public void testQueueUpdate2() {
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.customers.put(
        "C-1",
        new Customer(
          "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
          lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
        )
      );

      // Value matches current value.
      QueuedUpdate sameValue = manager.queueUpdate("C-1", "name", "Asha", 10);

      assertNull(sameValue);
      assertTrue(manager.updates.isEmpty());

      // Unsupported field.
      QueuedUpdate unsupportedField = manager.queueUpdate("C-1", "fax", "555-9999", 10);

      assertNull(unsupportedField);
      assertTrue(manager.updates.isEmpty());

      // Non-existent customer.
      QueuedUpdate unknownCustomer = manager.queueUpdate("UNKNOWN", "name", "Asha Rao", 10);

      assertNull(unknownCustomer);
      assertTrue(manager.updates.isEmpty());
    }

    // Test that queueUpdate compares against the customer's live field value, not
    // lastSyncedValues, when the field has no entry there yet.
    @Test
    public void testQueueUpdate3() {
      CustomerSyncManager manager = new CustomerSyncManager();
      // "phone" has no entry in the last synced values map.
      Map<String, String> lastSyncedValues = new HashMap<>();
      lastSyncedValues.put("name", "Asha");
      lastSyncedValues.put("email", "asha@example.com");
      lastSyncedValues.put("company", "Acme");
      manager.customers.put(
        "C-1",
        new Customer("C-1", "Asha", "asha@example.com", "555-0100", "Acme", lastSyncedValues)
      );

      QueuedUpdate update = manager.queueUpdate("C-1", "phone", "555-0192", 10);

      assertNotNull(update);
      assertEquals("555-0192", update.newValue);
      assertEquals("PENDING", update.status);
      assertEquals(0, update.attempts);
      assertNull(update.errorType);
      assertTrue(manager.updates.containsKey(update.updateId));
    }

    // Test rejecting an update whose new value matches the customer's current field value.
    @Test
    public void testQueueUpdate4() {
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.customers.put(
        "C-1",
        new Customer(
          "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
          lastSyncedValues("Asha", "asha@example.com", "555-0000", "Acme")
        )
      );

      QueuedUpdate update = manager.queueUpdate("C-1", "phone", "555-0100", 10);

      assertNull(update);
      assertTrue(manager.updates.isEmpty());
    }

    // Test that all PENDING updates for a customer and field are cancelled.
    @Test
    public void testCancelPendingUpdates1() {
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.customers.put(
        "C-1",
        new Customer("C-1", "Asha", "asha@example.com", "555-0100", "Acme", new HashMap<>())
      );
      manager.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "PENDING"));
      manager.updates.put(2, new QueuedUpdate(2, "C-1", "name", "Asha R.", 12, "PENDING"));

      int cancelled = manager.cancelPendingUpdates("C-1", "name");

      assertEquals(2, cancelled);
      assertEquals("CANCELLED", manager.updates.get(1).status);
      assertEquals("CANCELLED", manager.updates.get(2).status);
    }

    // Test that non-PENDING updates are left untouched, and that only updates matching
    // the given field are cancelled.
    @Test
    public void testCancelPendingUpdates2() {
      // Sub-case: updates with a status other than PENDING (processing, failed,
      // succeeded, cancelled).
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.customers.put(
        "C-1",
        new Customer("C-1", "Asha", "asha@example.com", "555-0100", "Acme", new HashMap<>())
      );
      manager.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "PROCESSING"));
      manager.updates.put(
        2,
        new QueuedUpdate(2, "C-1", "name", "Asha R.", 12, "FAILED", 1, "RETRYABLE")
      );
      manager.updates.put(3, new QueuedUpdate(3, "C-1", "name", "A. Rao", 14, "SUCCEEDED"));
      manager.updates.put(4, new QueuedUpdate(4, "C-1", "name", "AR", 16, "CANCELLED"));

      int cancelled = manager.cancelPendingUpdates("C-1", "name");

      assertEquals(0, cancelled);
      assertEquals("PROCESSING", manager.updates.get(1).status);
      assertEquals("FAILED", manager.updates.get(2).status);
      assertEquals("SUCCEEDED", manager.updates.get(3).status);
      assertEquals("CANCELLED", manager.updates.get(4).status);

      // Sub-case: a PENDING update on a different field.
      CustomerSyncManager manager2 = new CustomerSyncManager();
      manager2.customers.put(
        "C-1",
        new Customer("C-1", "Asha", "asha@example.com", "555-0100", "Acme", new HashMap<>())
      );
      manager2.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "PENDING"));
      manager2.updates.put(2, new QueuedUpdate(2, "C-1", "phone", "555-0192", 12, "PENDING"));

      int cancelled2 = manager2.cancelPendingUpdates("C-1", "name");

      assertEquals(1, cancelled2);
      assertEquals("CANCELLED", manager2.updates.get(1).status);
      assertEquals("PENDING", manager2.updates.get(2).status);
    }

    // Test that cancelling for a non-existent customer returns zero.
    @Test
    public void testCancelPendingUpdates3() {
      CustomerSyncManager manager = new CustomerSyncManager();

      int cancelled = manager.cancelPendingUpdates("UNKNOWN", "name");

      assertEquals(0, cancelled);
    }

    // Test that cancelling one customer/field's updates leaves other customers and fields
    // untouched, and that update IDs keep incrementing correctly after a cancellation.
    @Test
    public void testCancelPendingUpdates4() {
      CustomerSyncManager manager = new CustomerSyncManager();
      manager.customers.put(
        "C-1",
        new Customer(
          "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
          lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
        )
      );
      manager.customers.put(
        "C-2",
        new Customer(
          "C-2", "Ravi", "ravi@example.com", "555-0200", "Globex",
          lastSyncedValues("Ravi", "ravi@example.com", "555-0200", "Globex")
        )
      );

      QueuedUpdate first = manager.queueUpdate("C-1", "phone", "555-0192", 10);
      QueuedUpdate second = manager.queueUpdate("C-1", "name", "Asha Rao", 11);
      QueuedUpdate third = manager.queueUpdate("C-2", "phone", "555-0299", 12);

      int cancelled = manager.cancelPendingUpdates("C-1", "phone");

      // C-1's pending "phone" update.
      assertEquals(1, cancelled);
      assertEquals("CANCELLED", manager.updates.get(first.updateId).status);
      // C-1's "name" update (a different field).
      assertEquals("PENDING", manager.updates.get(second.updateId).status);
      // C-2's "phone" update (a different customer).
      assertEquals("PENDING", manager.updates.get(third.updateId).status);

      // A new update queued after the cancellation.
      QueuedUpdate fourth = manager.queueUpdate("C-1", "company", "Globex Inc", 13);

      assertEquals(4, fourth.updateId);
      assertEquals(4, manager.updates.size());
    }  

    // Add inside the existing TestSuite class:

// Test that a successful result sets SUCCEEDED, clears the error, increments attempts,
// and writes the new value to both the customer's field and lastSyncedValues.
@Test
public void testRecordSyncResult1() {
  CustomerSyncManager manager = new CustomerSyncManager();
  manager.customers.put(
    "C-1",
    new Customer(
      "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
      lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
    )
  );
  manager.updates.put(1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "PENDING"));

  assertTrue(manager.recordSyncResult(1, true, false));

  QueuedUpdate u = manager.updates.get(1);
  assertEquals("SUCCEEDED", u.status);
  assertEquals(1, u.attempts);
  assertNull(u.errorType);
  // Customer's live field is updated.
  assertEquals("Asha Rao", manager.customers.get("C-1").name);
  // lastSyncedValues is updated too.
  assertEquals("Asha Rao", manager.customers.get("C-1").lastSyncedValues.get("name"));
  // Other fields untouched.
  assertEquals("asha@example.com", manager.customers.get("C-1").email);
  assertEquals("555-0100", manager.customers.get("C-1").lastSyncedValues.get("phone"));

  // Unknown update ID is ignored.
  assertFalse(manager.recordSyncResult(999, true, false));
  assertFalse(manager.updates.containsKey(999));

  // Already SUCCEEDED -> not processable, attempts must not change.
  assertFalse(manager.recordSyncResult(1, true, false));
  assertEquals(1, manager.updates.get(1).attempts);
}

// Test that a failed result sets FAILED with the correct errorType, increments attempts,
// and leaves the customer's field and lastSyncedValues untouched.
@Test
public void testRecordSyncResult2() {
  CustomerSyncManager manager = new CustomerSyncManager();
  manager.customers.put(
    "C-1",
    new Customer(
      "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
      lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
    )
  );
  manager.updates.put(
    1, new QueuedUpdate(1, "C-1", "phone", "555-0192", 10, "PROCESSING")
  );

  // Retryable failure.
  assertTrue(manager.recordSyncResult(1, false, true));

  QueuedUpdate u = manager.updates.get(1);
  assertEquals("FAILED", u.status);
  assertEquals("RETRYABLE", u.errorType);
  assertEquals(1, u.attempts);
  // Customer not updated on failure.
  assertEquals("555-0100", manager.customers.get("C-1").phone);
  assertEquals("555-0100", manager.customers.get("C-1").lastSyncedValues.get("phone"));

  // A retryable FAILED update is NOT processable (only PENDING/PROCESSING are).
  assertFalse(manager.recordSyncResult(1, true, false));
  assertEquals(1, manager.updates.get(1).attempts);

  // Non-retryable failure on a fresh update.
  manager.updates.put(
    2, new QueuedUpdate(2, "C-1", "email", "new@example.com", 12, "PENDING")
  );
  assertTrue(manager.recordSyncResult(2, false, false));
  assertEquals("FAILED", manager.updates.get(2).status);
  assertEquals("NON_RETRYABLE", manager.updates.get(2).errorType);
  assertEquals(1, manager.updates.get(2).attempts);
  assertEquals("asha@example.com", manager.customers.get("C-1").email);
}

// Test that non-processable statuses (FAILED, SUCCEEDED, CANCELLED) reject recording,
// and that a missing customer or unsupported field causes a false return.
@Test
public void testRecordSyncResult3() {
  CustomerSyncManager manager = new CustomerSyncManager();
  manager.customers.put(
    "C-1",
    new Customer(
      "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
      lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
    )
  );

  // Already FAILED (retryable) -> not processable.
  manager.updates.put(
    1, new QueuedUpdate(1, "C-1", "name", "Asha Rao", 10, "FAILED", 2, "RETRYABLE")
  );
  assertFalse(manager.recordSyncResult(1, true, false));
  assertEquals("FAILED", manager.updates.get(1).status);
  assertEquals(2, manager.updates.get(1).attempts);

  // Already SUCCEEDED.
  manager.updates.put(2, new QueuedUpdate(2, "C-1", "name", "Asha Rao", 12, "SUCCEEDED"));
  assertFalse(manager.recordSyncResult(2, false, false));
  assertEquals("SUCCEEDED", manager.updates.get(2).status);

  // Already CANCELLED.
  manager.updates.put(3, new QueuedUpdate(3, "C-1", "name", "Asha Rao", 14, "CANCELLED"));
  assertFalse(manager.recordSyncResult(3, true, false));
  assertEquals("CANCELLED", manager.updates.get(3).status);

  // Customer no longer exists.
  manager.updates.put(4, new QueuedUpdate(4, "GONE", "name", "X", 16, "PENDING"));
  assertFalse(manager.recordSyncResult(4, true, false));
  assertEquals("PENDING", manager.updates.get(4).status);
  assertEquals(0, manager.updates.get(4).attempts);

  // Unsupported field.
  manager.updates.put(5, new QueuedUpdate(5, "C-1", "fax", "555-9999", 18, "PENDING"));
  assertFalse(manager.recordSyncResult(5, true, false));
  assertEquals("PENDING", manager.updates.get(5).status);
  assertEquals(0, manager.updates.get(5).attempts);
}

// Test that attempts accumulate across PROCESSING -> FAILED(retryable) -> (requeued as
// PENDING by caller) -> SUCCEEDED, and that a later success clears the prior error.
@Test
public void testRecordSyncResult4() {
  CustomerSyncManager manager = new CustomerSyncManager();
  manager.customers.put(
    "C-1",
    new Customer(
      "C-1", "Asha", "asha@example.com", "555-0100", "Acme",
      lastSyncedValues("Asha", "asha@example.com", "555-0100", "Acme")
    )
  );
  manager.updates.put(
    1, new QueuedUpdate(1, "C-1", "company", "Globex", 10, "PROCESSING")
  );

  assertTrue(manager.recordSyncResult(1, false, true));
  assertEquals(1, manager.updates.get(1).attempts);
  assertEquals("FAILED", manager.updates.get(1).status);
  assertEquals("RETRYABLE", manager.updates.get(1).errorType);

  // Simulate the retry worker putting it back to PROCESSING.
  manager.updates.get(1).status = "PROCESSING";
  assertTrue(manager.recordSyncResult(1, false, true));
  assertEquals(2, manager.updates.get(1).attempts);
  assertEquals("FAILED", manager.updates.get(1).status);

  // Retry again, this time succeeds -> error cleared, customer + lastSyncedValues updated.
  manager.updates.get(1).status = "PROCESSING";
  assertTrue(manager.recordSyncResult(1, true, false));

  QueuedUpdate u = manager.updates.get(1);
  assertEquals("SUCCEEDED", u.status);
  assertEquals(3, u.attempts);
  assertNull(u.errorType);
  assertEquals("Globex", manager.customers.get("C-1").company);
  assertEquals("Globex", manager.customers.get("C-1").lastSyncedValues.get("company"));
}  
    
  }

  public static void main(String[] argv) {
    Result result = JUnitCore.runClasses(TestSuite.class);

    for (Failure failure : result.getFailures()) {
      System.out.println(failure.getTrace());
    }

    if (result.wasSuccessful()) {
      System.out.println("All tests passed successfully.");
    } else {
      System.out.println("Some tests failed.");
    }
  }
}