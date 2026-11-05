/*
We are building a system to manage physical access to a corporate campus. Each user has a unique ID, a name, and an access level. Access levels are ranked: VISITOR is the lowest, STAFF is higher, and ADMIN is the highest. A user is authorized for a required access level when their own level is the same or higher — so an ADMIN is authorized for STAFF and VISITOR areas, and a STAFF user is authorized for VISITOR areas.

Definitions:
* A "user" is an object representing someone with campus access. It has a userId, a name, and an accessLevel.
* VISITOR is the default access level a new user receives.
* An "AccessManager" is the class used to manage all users on the campus.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for AccessManager is not passing due to a bug in the code. Make the necessary changes to AccessManager to fix the bug.
*/

/*
We are extending the system to record building access. The new AccessEvent class represents one badge-in/badge-out session for a user. Each event has a unique ID, an entryTime, and an exitTime, all measured in minutes since the start of the day. All events happen on the same day.

The campus is open from minute 480 (08:00) to minute 1200 (20:00). An event is "after-hours" if it starts before 480 or ends after 1200. Starting exactly at 480 or ending exactly at 1200 still counts as within hours.

Add two methods to AccessManager:

2-1) logAccess(userId, event): associate the event with that user. Return true if the user exists. If the user does not exist, ignore the event and return false.

2-2) getAfterHoursUsers(): return the IDs of all users who have at least one after-hours event, sorted in ascending order. Do not include a user who has no after-hours events, including a user with no events at all.

The testLogAccess and testGetAfterHoursUsers tests are provided.
*/

/*
We are adding a building utilization report. For capacity planning we want to know, for each hour of the day, how many distinct users were present in the building during that hour.

The day is divided into 24 one-hour buckets: hour h covers the minutes from h*60 up to (but not including) (h+1)*60.
For example, hour 0 is minutes 0–59, hour 1 is minutes 60–119, and so on. A user is "present during hour h" if any of their access events overlaps that hour. A session that ends exactly on an hour boundary does not count as present in the next hour (an event covering minutes 120–180 is present in hour 2 only, not hour 3).

3-1) Add getHourlyOccupancy() to AccessManager. It takes no arguments and returns a map from each hour to the number of distinct users present during that hour. Only include hours that have at least one user present; omit hours with no users.

Example:
  User 20 has one session: minute 130 to 260.
  User 21 has one session: minute 200 to 230.
  Result: {2: 1, 3: 2, 4: 1}

Explanation:
  User 20's session covers hour 2 (120–179), hour 3 (180–239), and hour 4 (240–299).
  User 21's session falls entirely within hour 3.
  Hour 3 has two users; hours 2 and 4 each have only user 20.

A session ending exactly on an hour boundary (e.g., 120–180) covers hour 2 only, not hour 3, so the end minute itself is treated as exclusive when computing covered hours.

To assist you in testing this new function, we have provided the testGetHourlyOccupancy tests.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {

    enum AccessLevel {
        VISITOR(1),
        STAFF(2),
        ADMIN(3);

        private final int rank;

        AccessLevel(int rank) {
            this.rank = rank;
        }

        int getRank() {
            return rank;
        }
    }

    /** Data about a campus user. */
    static class User {
        final int userId;
        final String name;
        AccessLevel accessLevel;

        User(int userId, String name, AccessLevel accessLevel) {
            this.userId = userId;
            this.name = name;
            this.accessLevel = accessLevel;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof User u)) {
                return false;
            }
            return userId == u.userId
                    && name.equals(u.name)
                    && accessLevel == u.accessLevel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, name, accessLevel);
        }

        @Override
        public String toString() {
            return "User ID: " + userId + ", Name: " + name + ", Access Level: " + accessLevel;
        }
    }

    /**
     * Data for managing campus access, and methods staff use to query or update it.
     */
     static class AccessManager {
        private final ArrayList<User> users = new ArrayList<>();

        /** Registers a user on the campus. */
        void addUser(User user) {
            users.add(user);
        }

        /** Updates the access level of the given user. */
        void updateAccessLevel(int userId, AccessLevel accessLevel) {
            for (User user : users) {
                if (user.userId == userId) {
                    user.accessLevel = accessLevel;
                    break;
                }
            }
        }

        /** Returns whether the given user is authorized for the required access level. */
        boolean isAuthorized(int userId, AccessLevel requiredLevel) {
            for (User user : users) {
                if (user.userId == userId) {
                    return user.accessLevel == requiredLevel;
                }
            }
            return false;
        }
    }

    // Add this class near the other data classes:
    /**
     * A single badge-in/badge-out session for a user. Has a unique ID and an entry
     * and exit time, each measured in minutes since the start of the day.
     */
    static class AccessEvent {
        final int id;
        final int entryTime;
        final int exitTime;

        AccessEvent(int id, int entryTime, int exitTime) {
            this.id = id;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
        }
    }

    public static class TestSuite {

        @Test
        public void testUser() {
            User u = new User(1, "John Doe", AccessLevel.VISITOR);
            assertEquals(1, u.userId);
            assertEquals("John Doe", u.name);
            assertEquals(AccessLevel.VISITOR, u.accessLevel);
        }

        @Test
        public void testIsAuthorized() {
            AccessManager manager = new AccessManager();
            manager.addUser(new User(1, "Vera Visitor", AccessLevel.VISITOR));
            manager.addUser(new User(2, "Sam Staff", AccessLevel.STAFF));
            manager.addUser(new User(3, "Ada Admin", AccessLevel.ADMIN));

            // A user is authorized for their own level and for any lower level.
            assertTrue(manager.isAuthorized(1, AccessLevel.VISITOR));
            assertFalse(manager.isAuthorized(1, AccessLevel.STAFF));
            assertFalse(manager.isAuthorized(1, AccessLevel.ADMIN));
            assertTrue(manager.isAuthorized(2, AccessLevel.VISITOR));
            assertTrue(manager.isAuthorized(2, AccessLevel.STAFF));
            assertFalse(manager.isAuthorized(2, AccessLevel.ADMIN));
            assertTrue(manager.isAuthorized(3, AccessLevel.VISITOR));
            assertTrue(manager.isAuthorized(3, AccessLevel.STAFF));
            assertTrue(manager.isAuthorized(3, AccessLevel.ADMIN));

            // An unknown user is never authorized.
            assertFalse(manager.isAuthorized(999, AccessLevel.VISITOR));
        }
        
        // Add the tests below inside the TestSuite class.
        @Test
        public void testLogAccess() {
            AccessManager manager = new AccessManager();
            manager.addUser(new User(12, "John Doe", AccessLevel.STAFF));
            manager.addUser(new User(22, "Alex Cleeve", AccessLevel.VISITOR));

            AccessEvent e1 = new AccessEvent(111, 10, 20);
            AccessEvent e2 = new AccessEvent(112, 15, 35);
            AccessEvent e3 = new AccessEvent(113, 20, 25);
            AccessEvent e99 = new AccessEvent(999, 1, 2);

            assertTrue(manager.logAccess(12, e1));
            assertTrue(manager.logAccess(22, e2));
            assertTrue(manager.logAccess(12, e3));

            // Non-existent user: event should be ignored.
            assertFalse(manager.logAccess(404, e99));
        }

        @Test
        public void testGetAfterHoursUsers() {
            AccessManager manager = new AccessManager();
            manager.addUser(new User(30, "Late Leaver", AccessLevel.ADMIN));
            manager.addUser(new User(10, "Early Bird", AccessLevel.STAFF));
            manager.addUser(new User(50, "No Shows", AccessLevel.VISITOR));
            manager.addUser(new User(40, "On The Dot", AccessLevel.STAFF));
            manager.addUser(new User(20, "Nine To Five", AccessLevel.VISITOR));

            // user 30 -> single event enters in-hours but exits after closing (1250 > 1200)
            manager.logAccess(30, new AccessEvent(505, 1150, 1250));
            // user 10 -> one event starts before opening (400 < 480): after-hours
            manager.logAccess(10, new AccessEvent(501, 400, 450));
            manager.logAccess(10, new AccessEvent(502, 500, 600));
            // user 20 -> both events within hours
            manager.logAccess(20, new AccessEvent(503, 600, 700));
            manager.logAccess(20, new AccessEvent(504, 800, 1100));
            // user 40 -> single event exactly on the boundary (opens 480, closes 1200): within hours
            manager.logAccess(40, new AccessEvent(506, 480, 1200));
            // user 50 -> no events

            java.util.List<Integer> expected = new ArrayList<>();
            expected.add(10);
            expected.add(30);
            assertEquals(expected, manager.getAfterHoursUsers());
        }

        // Add inside the existing TestSuite class:

// Test the worked example from the prompt: overlapping sessions, distinct-user counting
// (a user present in the same hour via multiple events counts once), and the
// end-boundary exclusivity rule.
@Test
public void testGetHourlyOccupancy1() {
    AccessManager manager = new AccessManager();
    manager.addUser(new User(20, "User Twenty", AccessLevel.STAFF));
    manager.addUser(new User(21, "User TwentyOne", AccessLevel.STAFF));

    // Example from the prompt.
    manager.logAccess(20, new AccessEvent(1, 130, 260));
    manager.logAccess(21, new AccessEvent(2, 200, 230));

    Map<Integer, Integer> result = manager.getHourlyOccupancy();

    Map<Integer, Integer> expected = new HashMap<>();
    expected.put(2, 1); // user 20 only (120–179)
    expected.put(3, 2); // user 20 (180–239) + user 21 (200–229)
    expected.put(4, 1); // user 20 only (240–259)
    assertEquals(expected, result);
}

// Test the end-boundary rule directly: an event 120–180 must cover hour 2 only, NOT
// hour 3, even though minute 180 is exactly the start of hour 3.
@Test
public void testGetHourlyOccupancy2() {
    AccessManager manager = new AccessManager();
    manager.addUser(new User(1, "Boundary", AccessLevel.VISITOR));

    // 120–180: covers minutes [120, 180) -> hour 2 only.
    manager.logAccess(1, new AccessEvent(10, 120, 180));

    Map<Integer, Integer> result = manager.getHourlyOccupancy();
    Map<Integer, Integer> expected = new HashMap<>();
    expected.put(2, 1);
    assertEquals(expected, result);
    assertFalse(result.containsKey(3)); // must NOT spill into hour 3

    // A separate user with 180–240 covers hour 3 only.
    manager.addUser(new User(2, "BoundaryTwo", AccessLevel.VISITOR));
    manager.logAccess(2, new AccessEvent(11, 180, 240));

    Map<Integer, Integer> result2 = manager.getHourlyOccupancy();
    // User 1: {2:1}; User 2: {3:1}; combined.
    Map<Integer, Integer> expected2 = new HashMap<>();
    expected2.put(2, 1);
    expected2.put(3, 1);
    assertEquals(expected2, result2);
}

// Test distinct-user counting: a user with two separate events in the same hour must
// count once, not twice.
@Test
public void testGetHourlyOccupancy3() {
    AccessManager manager = new AccessManager();
    manager.addUser(new User(7, "Frequent", AccessLevel.STAFF));
    manager.addUser(new User(8, "Other", AccessLevel.VISITOR));

    // User 7 enters hour 2 twice via two separate events, plus once in hour 3.
    manager.logAccess(7, new AccessEvent(20, 125, 130));
    manager.logAccess(7, new AccessEvent(21, 140, 150));
    manager.logAccess(7, new AccessEvent(22, 190, 200));
    // User 8 present in hour 2 only.
    manager.logAccess(8, new AccessEvent(23, 150, 160));

    Map<Integer, Integer> result = manager.getHourlyOccupancy();
    Map<Integer, Integer> expected = new HashMap<>();
    expected.put(2, 2); // users 7 and 8 (7 counted once despite two events)
    expected.put(3, 1); // user 7 only
    assertEquals(expected, result);
}

// Test that a user with no events, an empty manager, and zero-hour edge cases
// (minute 0 and the day's final hour 23) are all handled.
@Test
public void testGetHourlyOccupancy4() {
    // Empty manager -> empty map (not null).
    AccessManager empty = new AccessManager();
    assertTrue(empty.getHourlyOccupancy().isEmpty());

    AccessManager manager = new AccessManager();
    manager.addUser(new User(9, "NoEvents", AccessLevel.VISITOR));
    manager.addUser(new User(10, "NightOwl", AccessLevel.STAFF));

    // User 10 has an event touching hour 0 (minutes 0–59) and an event in hour 23.
    manager.logAccess(10, new AccessEvent(30, 0, 30));    // hour 0
    manager.logAccess(10, new AccessEvent(31, 1380, 1439)); // 23*60=1380 .. 1439 -> hour 23

    Map<Integer, Integer> result = manager.getHourlyOccupancy();
    Map<Integer, Integer> expected = new HashMap<>();
    expected.put(0, 1);   // user 10
    expected.put(23, 1);  // user 10
    assertEquals(expected, result);

    // User 9 (no events) must not appear anywhere.
    for (Integer count : result.values()) {
        assertTrue(count >= 1);
    }
    // And the map must not contain hours with zero occupancy.
    assertFalse(result.containsKey(1));
    assertFalse(result.containsKey(22));
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
