/*
We are building Floating License Pool, a back-end system that manages a shared pool of floating software licenses used by an internal engineering tools team.

Definitions:
* A "license" is an object representing one floating software license in the shared pool. A license has:
  - licenseId
  - status
  - assignedUserId
  - checkedOutAt (the time a license was assigned to a user)
  - lastHeartbeatAt (the most recent time the checked-out client confirmed it's still actively using the license)

* A license's status can be one of:
  - AVAILABLE
  - IN_USE

The LicensePoolManager class manages the pool of licenses.

To begin with, we present you with two tasks:

1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for LicensePoolManager is not passing due to a bug in the code. Make the necessary changes to LicensePoolManager to fix the bug.
*/

/*
We are extending Floating License Pool to let users check out and return licenses. This lets an engineer get a license when they need one, and free it up for someone else when they're done.

Add two functions to the LicensePoolManager class.

2-1) checkoutLicense

The checkoutLicense function takes a user ID and the current time. It should assign an available license to that user, following these rules:
- A user may hold at most one license at a time; ignore the request if the user already holds one.
- When more than one license is available, assign the one with the lowest licenseId.
- Set the license status to IN_USE, store the assigned user, and set both the checkout time and heartbeat time to the given current time.

Return the assigned licenseId, or null if no license is available or the user already holds one.

2-2) returnLicense

The returnLicense function takes a user ID. It should find the license currently held by that user, set its status to AVAILABLE, and clear the assigned user, checkout time, and heartbeat time.

Return true when a license is returned, and false if the user does not hold a license.

Example:
- Licenses 2 and 4 are AVAILABLE.
- Checking out a license for U-1 at time 100 assigns license 2, the lowest available id.
- If U-1 tries to check out again at time 150, the request returns null, since they already hold a license.
*/

/*
We now need to reclaim licenses from users who have stopped sending heartbeats. This stops a crashed or disconnected client from locking a license forever.

Add one function to the LicensePoolManager class.

3-1) Add reclaimInactiveLicenses() to the LicensePoolManager class.

The reclaimInactiveLicenses function takes the currentTime and a timeout. It should reclaim licenses that have gone inactive, following these rules:
- Only licenses with status IN_USE are considered.
- A license is reclaimed when its lastHeartbeatAt plus the timeout is on or before the currentTime (i.e., lastHeartbeatAt + timeout <= currentTime).
- Reclaimed licenses are set to AVAILABLE, with their assigned user, checkout time, and heartbeat time cleared.
- Licenses whose heartbeat is still within the timeout are left unchanged.
- Return the IDs of reclaimed licenses in ascending order.

Example: licenses 1 and 3 are IN_USE with stale heartbeats, while license 2 is IN_USE but still within its timeout. Reclaiming at the given time and timeout resets licenses 1 and 3 to AVAILABLE and returns their IDs in order, leaving license 2 untouched.

To assist you in testing this new function, we have provided the testReclaimInactiveLicenses tests.
*/

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

class License {
  int licenseId;
  String status;
  String assignedUserId;
  Integer checkedOutAt;
  Integer lastHeartbeatAt;

  public License(int licenseId, String status) {
    this(licenseId, status, null, null, null);
  }

  public License(int licenseId, String status, String assignedUserId) {
    this(licenseId, status, assignedUserId, null, null);
  }

  // Represents a single floating license and its current checkout state.
  public License(
    int licenseId,
    String status,
    String assignedUserId,
    Integer checkedOutAt,
    Integer lastHeartbeatAt
  ) {
    this.licenseId = licenseId;
    this.status = status;
    this.assignedUserId = assignedUserId;
    this.checkedOutAt = checkedOutAt;
    this.lastHeartbeatAt = lastHeartbeatAt;
  }
}

class WaitingRequest {
  int requestId;
  String userId;
  int requestedAt;
  String status;

  public WaitingRequest(int requestId, String userId, int requestedAt, String status) {
    this.requestId = requestId;
    this.userId = userId;
    this.requestedAt = requestedAt;
    this.status = status;
  }
}

// Manages the pool of licenses, keyed by license ID.
class LicensePoolManager {
  Map<Integer, License> licenses = new HashMap<>();
  List<WaitingRequest> waitingRequests = new ArrayList<>();

  public boolean isLicenseAvailable(License license) {
    return license.status.equals("IN_USE") && license.assignedUserId == null;
  }
}

public class Solution {

  public static class TestSuite {

    @Test
    public void testIsLicenseAvailable1() {
      // Test isLicenseAvailable for AVAILABLE, unassigned licenses.
      LicensePoolManager manager = new LicensePoolManager();

      // Available, unassigned license using the no-assignment constructor
      License licenseDefault = new License(1, "AVAILABLE");
      assertTrue(manager.isLicenseAvailable(licenseDefault));

      // Available license with explicit null fields
      License licenseExplicitNull = new License(
        2, "AVAILABLE", null, null, null
      );
      assertTrue(manager.isLicenseAvailable(licenseExplicitNull));
    }

    @Test
    public void testIsLicenseAvailable2() {
      // Test isLicenseAvailable for IN_USE licenses, both assigned and unassigned.
      LicensePoolManager manager = new LicensePoolManager();

      // In-use license that is assigned to a user
      License licenseAssigned = new License(
        1, "IN_USE", "U-1", 10, 10
      );
      assertFalse(manager.isLicenseAvailable(licenseAssigned));

      // IN_USE license with no assigned user.
      License licenseUnassigned = new License(3, "IN_USE", null);
      assertFalse(manager.isLicenseAvailable(licenseUnassigned));
    }

    @Test
    public void testIsLicenseAvailable3() {
      // Test isLicenseAvailable for AVAILABLE licenses with an assigned user.
      LicensePoolManager manager = new LicensePoolManager();

      // Available license that still has an assigned user
      License licenseWithUser = new License(4, "AVAILABLE", "U-2");
      assertFalse(manager.isLicenseAvailable(licenseWithUser));

      // AVAILABLE license with an empty string assigned user.
      License licenseEmptyUser = new License(5, "AVAILABLE", "");
      assertFalse(manager.isLicenseAvailable(licenseEmptyUser));
    }

    @Test
    public void testIsLicenseAvailable4() {
      // Test isLicenseAvailable across a pool with mixed statuses and assignments.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "AVAILABLE"));
      manager.licenses.put(2, new License(2, "IN_USE", "U-1", 10, 10));
      manager.licenses.put(3, new License(3, "AVAILABLE"));
      manager.licenses.put(4, new License(4, "IN_USE", "U-2", 20, 20));

      List<Integer> availableIds = new ArrayList<>();
      for (License license : manager.licenses.values()) {
        if (manager.isLicenseAvailable(license)) {
          availableIds.add(license.licenseId);
        }
      }
      Collections.sort(availableIds);

      assertEquals(Arrays.asList(1, 3), availableIds);
    }
    
        @Test
    public void testCheckoutLicense1() {
      // Test that checkoutLicense assigns an available license and sets all of its fields.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "AVAILABLE"));

      Integer licenseId = manager.checkoutLicense("U-1", 100);

      assertEquals(Integer.valueOf(1), licenseId);
      License license = manager.licenses.get(1);
      assertEquals("IN_USE", license.status);
      assertEquals("U-1", license.assignedUserId);
      assertEquals(Integer.valueOf(100), license.checkedOutAt);
      assertEquals(Integer.valueOf(100), license.lastHeartbeatAt);
    }

    @Test
    public void testCheckoutLicense2() {
      // Test checkoutLicense when multiple licenses are available.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(5, new License(5, "AVAILABLE"));
      manager.licenses.put(2, new License(2, "AVAILABLE"));

      Integer licenseId = manager.checkoutLicense("U-1", 100);

      // Multiple licenses available.
      assertEquals(Integer.valueOf(2), licenseId);
    }

    @Test
    public void testCheckoutLicense3() {
      // Test that checkoutLicense returns null whenever there is nothing to assign: the user already holds a license, no license is available, or the pool is empty.
      // No license available: the only license in the pool is already IN_USE.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "IN_USE", "U-2"));

      Integer licenseId = manager.checkoutLicense("U-1", 100);

      assertNull(licenseId);

      // IN_USE with no assigned user is still not available.
      manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "IN_USE"));

      licenseId = manager.checkoutLicense("U-1", 100);

      assertNull(licenseId);

      // User already holds a license, and another license is available.
      manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "IN_USE", "U-1"));
      manager.licenses.put(2, new License(2, "AVAILABLE"));

      licenseId = manager.checkoutLicense("U-1", 100);

      assertNull(licenseId);
      // The available license must remain untouched.
      assertEquals("AVAILABLE", manager.licenses.get(2).status);

      // Empty pool: there are no licenses at all.
      manager = new LicensePoolManager();

      licenseId = manager.checkoutLicense("U-1", 100);

      assertNull(licenseId);
    }

    @Test
    public void testCheckoutLicense4() {
      // Test that checkoutLicense skips a lower-id license already IN_USE and held by a
      // different user, assigning the available higher-id license instead.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "IN_USE", "U-1"));
      manager.licenses.put(2, new License(2, "AVAILABLE"));

      Integer licenseId = manager.checkoutLicense("U-3", 200);

      assertEquals(Integer.valueOf(2), licenseId);
      License license2 = manager.licenses.get(2);
      assertEquals("IN_USE", license2.status);
      assertEquals("U-3", license2.assignedUserId);
      assertEquals(Integer.valueOf(200), license2.checkedOutAt);
      assertEquals(Integer.valueOf(200), license2.lastHeartbeatAt);

      // The lower-id license, held by a different user, must remain untouched.
      License license1 = manager.licenses.get(1);
      assertEquals("IN_USE", license1.status);
      assertEquals("U-1", license1.assignedUserId);
    }

    @Test
    public void testReturnLicense1() {
      // Test that returnLicense clears an in-use license's fields and makes it available.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(
        1,
        new License(1, "IN_USE", "U-1", 100, 150)
      );

      boolean result = manager.returnLicense("U-1");

      assertTrue(result);
      License license = manager.licenses.get(1);
      assertEquals("AVAILABLE", license.status);
      assertNull(license.assignedUserId);
      assertNull(license.checkedOutAt);
      assertNull(license.lastHeartbeatAt);
    }

    @Test
    public void testReturnLicense2() {
      // Test that returnLicense returns false when the user does not hold a license.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "AVAILABLE"));

      boolean result = manager.returnLicense("U-1");

      assertFalse(result);
    }

    @Test
    public void testReturnLicense3() {
      // Test that a license freed by returnLicense can immediately be checked out by another user.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(1, new License(1, "IN_USE", "U-1"));

      manager.returnLicense("U-1");
      Integer licenseId = manager.checkoutLicense("U-2", 200);

      assertEquals(Integer.valueOf(1), licenseId);
      assertEquals("U-2", manager.licenses.get(1).assignedUserId);
    }

    @Test
    public void testReturnLicense4() {
      // Test returnLicense with an unrelated license present, followed by a checkoutLicense call.
      LicensePoolManager manager = new LicensePoolManager();
      manager.licenses.put(5, new License(5, "IN_USE", "U-1", 100, 150));
      manager.licenses.put(2, new License(2, "AVAILABLE"));

      boolean result = manager.returnLicense("U-1");

      assertTrue(result);
      License license5 = manager.licenses.get(5);
      assertEquals("AVAILABLE", license5.status);
      assertNull(license5.assignedUserId);
      assertNull(license5.checkedOutAt);
      assertNull(license5.lastHeartbeatAt);

      // Unrelated license present.
      License license2 = manager.licenses.get(2);
      assertEquals("AVAILABLE", license2.status);
      assertNull(license2.assignedUserId);
      assertNull(license2.checkedOutAt);
      assertNull(license2.lastHeartbeatAt);

      // Checkout after return.
      Integer licenseId = manager.checkoutLicense("U-2", 200);

      assertEquals(Integer.valueOf(2), licenseId);
      assertEquals("AVAILABLE", manager.licenses.get(5).status);
    }
    // Add inside the existing TestSuite class.

// Basic reclaim: stale IN_USE licenses are reset and returned in ascending ID order,
// while a still-active IN_USE license is untouched.
@Test
public void testReclaimInactiveLicenses1() {
  LicensePoolManager manager = new LicensePoolManager();
  // License 1: heartbeat 10, timeout 50, currentTime 100 -> 10+50=60 <= 100 -> reclaim.
  manager.licenses.put(1, new License(1, "IN_USE", "U-1", 10, 10));
  // License 2: heartbeat 80, 80+50=130 > 100 -> still active, keep.
  manager.licenses.put(2, new License(2, "IN_USE", "U-2", 70, 80));
  // License 3: heartbeat 0, 0+50=50 <= 100 -> reclaim.
  manager.licenses.put(3, new License(3, "IN_USE", "U-3", 0, 0));

  List<Integer> reclaimed = manager.reclaimInactiveLicenses(100, 50);

  assertEquals(Arrays.asList(1, 3), reclaimed);

  // Reclaimed licenses are AVAILABLE with all assignment fields cleared.
  License l1 = manager.licenses.get(1);
  assertEquals("AVAILABLE", l1.status);
  assertNull(l1.assignedUserId);
  assertNull(l1.checkedOutAt);
  assertNull(l1.lastHeartbeatAt);

  License l3 = manager.licenses.get(3);
  assertEquals("AVAILABLE", l3.status);
  assertNull(l3.assignedUserId);
  assertNull(l3.checkedOutAt);
  assertNull(l3.lastHeartbeatAt);

  // License 2 untouched.
  License l2 = manager.licenses.get(2);
  assertEquals("IN_USE", l2.status);
  assertEquals("U-2", l2.assignedUserId);
  assertEquals(Integer.valueOf(70), l2.checkedOutAt);
  assertEquals(Integer.valueOf(80), l2.lastHeartbeatAt);
}

// Exact boundary: lastHeartbeatAt + timeout == currentTime counts as reclaimable,
// and one minute short of the boundary is NOT reclaimed.
@Test
public void testReclaimInactiveLicenses2() {
  LicensePoolManager manager = new LicensePoolManager();
  // heartbeat 50, timeout 50, currentTime 100 -> 50+50=100 == 100 -> reclaim (boundary).
  manager.licenses.put(1, new License(1, "IN_USE", "U-1", 10, 50));
  // heartbeat 51, 51+50=101 > 100 -> still active.
  manager.licenses.put(2, new License(2, "IN_USE", "U-2", 20, 51));

  List<Integer> reclaimed = manager.reclaimInactiveLicenses(100, 50);

  assertEquals(Arrays.asList(1), reclaimed);
  assertEquals("AVAILABLE", manager.licenses.get(1).status);
  assertEquals("IN_USE", manager.licenses.get(2).status);
  assertEquals(Integer.valueOf(51), manager.licenses.get(2).lastHeartbeatAt);
}

// Only IN_USE licenses are considered: AVAILABLE licenses are ignored even if their
// (stale) heartbeat fields would otherwise qualify, and they are never returned.
@Test
public void testReclaimInactiveLicenses3() {
  LicensePoolManager manager = new LicensePoolManager();
  // AVAILABLE license with stale-looking heartbeat fields: must be ignored.
  manager.licenses.put(1, new License(1, "AVAILABLE", null, 10, 10));
  // AVAILABLE license with null fields: must be ignored.
  manager.licenses.put(2, new License(2, "AVAILABLE"));
  // IN_USE stale license: reclaimed.
  manager.licenses.put(3, new License(3, "IN_USE", "U-3", 5, 5));

  List<Integer> reclaimed = manager.reclaimInactiveLicenses(100, 50);

  assertEquals(Arrays.asList(3), reclaimed);
  // AVAILABLE licenses remain AVAILABLE and untouched.
  assertEquals("AVAILABLE", manager.licenses.get(1).status);
  assertEquals("AVAILABLE", manager.licenses.get(2).status);
  assertEquals("AVAILABLE", manager.licenses.get(3).status);
}

// Empty / all-active cases return an empty list, ascending order is enforced even
// when licenses are inserted out of ID order, and reclaimed licenses become
// immediately checkout-able afterwards.
@Test
public void testReclaimInactiveLicenses4() {
  // Empty pool -> empty list.
  assertTrue(new LicensePoolManager().reclaimInactiveLicenses(100, 50).isEmpty());

  // All active -> empty list, nothing changed.
  LicensePoolManager manager = new LicensePoolManager();
  manager.licenses.put(1, new License(1, "IN_USE", "U-1", 90, 95));
  assertTrue(manager.reclaimInactiveLicenses(100, 50).isEmpty());
  assertEquals("IN_USE", manager.licenses.get(1).status);

  // Insert out of ID order to verify ascending sort of the result.
  LicensePoolManager manager2 = new LicensePoolManager();
  manager2.licenses.put(7, new License(7, "IN_USE", "U-7", 0, 0));
  manager2.licenses.put(2, new License(2, "IN_USE", "U-2", 0, 0));
  manager2.licenses.put(5, new License(5, "IN_USE", "U-5", 0, 0));

  List<Integer> reclaimed = manager2.reclaimInactiveLicenses(100, 50);
  assertEquals(Arrays.asList(2, 5, 7), reclaimed);

  // A reclaimed license can be checked out again immediately.
  Integer newLicense = manager2.checkoutLicense("U-9", 200);
  assertEquals(Integer.valueOf(2), newLicense); // lowest reclaimed id
  assertEquals("U-9", manager2.licenses.get(2).assignedUserId);
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