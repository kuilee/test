/*
We are building the backend for a retail order notification service. When something happens to a customer's order, the service creates a Notification for that order. Each notification has properties mentioned below. One order can have many notifications.

Definitions:
* A "Notification" is one message the service creates for an order. It has a notificationId, an orderId, and a createdAt time (in minutes since the start of the current window).
* A "delivery status" describes where a notification is in the delivery lifecycle:
  QUEUED, SENT, DELIVERED, or FAILED. (Delivery status is used from the next question onward.)
* A "NotificationCenter" is the class used to manage all notifications for the window.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for NotificationCenter is not passing due to a bug in the code. Make the necessary changes to NotificationCenter to fix the bug.
*/

/*
Delivery updates for a notification arrive from a provider, sometimes in batches, and the same update can be reported to us more than once. Each update carries a delivery status (one of QUEUED, SENT, DELIVERED, or FAILED) and the time it happened (in minutes since the start of the window). We need to ingest these batches while ignoring any update we have already stored.

Add two methods to NotificationCenter:

2-1) addUpdates(notificationId, updates): `updates` is a List<Update> for one notification. If the notification exists, store the Updates not already stored and return how many were newly stored; otherwise store nothing and return 0. An Update is a duplicate if the same status and timestamp is already stored, whether from an earlier batch or earlier in the same batch.

2-2) getUpdateCount(notificationId): return how many updates are currently stored for that notification (0 if it has none, or if there is no such notification).

Example:

Input (notification 40 already exists):
  addUpdates(40, Arrays.asList(new Update(DeliveryStatus.SENT, 5), new Update(DeliveryStatus.DELIVERED, 15)))  -> 2
  addUpdates(40, Arrays.asList(new Update(DeliveryStatus.SENT, 5), new Update(DeliveryStatus.FAILED, 30)))      -> 1
  getUpdateCount(40)                                                                                           -> 3

Explanation:
The first call stores both Updates, so it returns 2. In the second call, new Update(DeliveryStatus.SENT, 5) is already stored, so only new Update(DeliveryStatus.FAILED, 30) is new and it returns 1. Notification 40 now has 3 stored updates in total.

The testAddUpdates and testGetUpdateCount tests are provided.
*/
/*
We want to spot the orders where customers waited longest to be notified. For each order, we look at its delivered notifications and take the largest delivery latency. A notification's delivery latency is the time difference between its createdAt and its DELIVERED update.

3-1) Implement getSlowestDeliveryPerOrder(). It should return a Map of order ID to the largest delivery latency among that order's delivered notifications. Only include orders that have at least one delivered notification; an order whose notifications were never delivered is left out.

A notification's updates are not necessarily recorded in order, so the DELIVERED update is not always the last one recorded.

Example:

Input (order 7 has two notifications):
  notification 70: createdAt 0, with a DELIVERED update at time 40
  notification 71: createdAt 10, with a DELIVERED update at time 90

Output (returned by getSlowestDeliveryPerOrder()):
  {7: 80}

Explanation:
Notification 70's latency is 40 - 0 = 40, and notification 71's latency is 90 - 10 = 80.
The slowest delivery for order 7 is 80.

The testGetSlowestDeliveryPerOrder test is provided.
*/
import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;
import static org.junit.Assert.*;

public class Solution {

    enum DeliveryStatus {
        QUEUED(1),
        SENT(2),
        DELIVERED(3),
        FAILED(4);

        private final int rank;

        DeliveryStatus(int rank) {
            this.rank = rank;
        }

        int getRank() {
            return rank;
        }
    }

    /**
     * Represents one delivery status update.
     *
     * Example:
     * new Update(DeliveryStatus.SENT, 5)
     *
     * means the notification became SENT at minute 5.
     */
    record Update(DeliveryStatus status, int timestamp) {}

    /** Data about a single notification created for an order. */
    static class Notification {
        final int notificationId;
        final int orderId;
        final int createdAt; // minutes since the start of the window

        Notification(int notificationId, int orderId, int createdAt) {
            this.notificationId = notificationId;
            this.orderId = orderId;
            this.createdAt = createdAt;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Notification n)) {
                return false;
            }

            return notificationId == n.notificationId
                    && orderId == n.orderId
                    && createdAt == n.createdAt;
        }

        @Override
        public int hashCode() {
            return Objects.hash(notificationId, orderId, createdAt);
        }

        @Override
        public String toString() {
            return "Notification " + notificationId
                    + " (order " + orderId + ") created=" + createdAt;
        }
    }

    /**
     * Data for managing order notifications, and methods staff use to query them.
     */
    static class NotificationCenter {
        
        private final ArrayList<Notification> notifications = new ArrayList<>();

        /** Registers a notification for the current window. */
        void addNotification(Notification notification) {
            notifications.add(notification);
        }

        /** Returns the notification with the given ID, or null if there is no such notification. */
        Notification getNotification(int notificationId) {
            for (Notification notification : notifications) {
                if (notification.notificationId == notificationId) {
                    return notification;
                }
            }

            return null;
        }

        /** Returns the IDs of all notifications for the given order, sorted in ascending order. */
        List<Integer> getOrderNotifications(int orderId) {
            List<Integer> result = new ArrayList<>();

            for (Notification notification : notifications) {
                if (notification.notificationId == orderId) {
                    result.add(notification.notificationId);
                }
            }

            result.sort(Integer::compareTo);
            return result;
        }
    }

    public static class TestSuite {

        @Test
        public void testNotification() {
            Notification n = new Notification(1, 500, 30);

            assertEquals(1, n.notificationId);
            assertEquals(500, n.orderId);
            assertEquals(30, n.createdAt);
        }

        @Test
        public void testGetOrderNotifications() {
            NotificationCenter center = new NotificationCenter();

            center.addNotification(new Notification(1, 1, 0));
            center.addNotification(new Notification(2, 2, 0));
            center.addNotification(new Notification(3, 1, 0));
            center.addNotification(new Notification(4, 2, 0));
            center.addNotification(new Notification(5, 1, 0));

            // Order 1 owns notifications 1, 3, and 5.
            assertEquals(
                    Arrays.asList(1, 3, 5),
                    center.getOrderNotifications(1)
            );

            // Order 2 owns notifications 2 and 4.
            assertEquals(
                    Arrays.asList(2, 4),
                    center.getOrderNotifications(2)
            );

            // An order with no notifications returns an empty list.
            assertEquals(
                    new ArrayList<>(),
                    center.getOrderNotifications(99)
            );
        }
        @Test
        public void testAddUpdates() {
            NotificationCenter center = new NotificationCenter();
            center.addNotification(new Notification(10, 1, 0));
            center.addNotification(new Notification(20, 1, 0));

            // Two new updates stored.
            assertEquals(2, center.addUpdates(10, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 5),
                    new Update(DeliveryStatus.DELIVERED, 15))));

            // (SENT, 5) is already stored, so only (FAILED, 20) is new.
            assertEquals(1, center.addUpdates(10, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 5),
                    new Update(DeliveryStatus.FAILED, 20))));

            // Both already stored -> nothing new.
            assertEquals(0, center.addUpdates(10, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 5),
                    new Update(DeliveryStatus.FAILED, 20))));

            // Within-batch duplicate: the second (SENT, 8) repeats the first,
            // so only 2 are stored.
            assertEquals(2, center.addUpdates(20, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 8),
                    new Update(DeliveryStatus.SENT, 8),
                    new Update(DeliveryStatus.DELIVERED, 12))));
            assertEquals(2, center.getUpdateCount(20));

            // No notification with this ID: nothing is stored.
            assertEquals(0, center.addUpdates(999, Collections.singletonList(
                    new Update(DeliveryStatus.SENT, 0))));

            // Same timestamp, different status -> both kept.
            assertEquals(2, center.addUpdates(10, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 25),
                    new Update(DeliveryStatus.FAILED, 25))));

            // Same status, different timestamp -> both kept.
            assertEquals(2, center.addUpdates(20, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 30),
                    new Update(DeliveryStatus.SENT, 35))));
        }

        @Test
        public void testGetUpdateCount() {
            NotificationCenter center = new NotificationCenter();
            center.addNotification(new Notification(10, 1, 0));
            center.addNotification(new Notification(30, 2, 0)); // never updated

            center.addUpdates(10, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 5),
                    new Update(DeliveryStatus.DELIVERED, 15)));

            center.addUpdates(10, Arrays.asList(
                    new Update(DeliveryStatus.SENT, 5),
                    new Update(DeliveryStatus.FAILED, 20))); // one duplicate

            center.addUpdates(999, Collections.singletonList(
                    new Update(DeliveryStatus.SENT, 0))); // ignored

            assertEquals(3, center.getUpdateCount(10));
            assertEquals(0, center.getUpdateCount(30));  // exists but no updates
            assertEquals(0, center.getUpdateCount(999)); // no such notification
        }
                // Add this method inside the TestSuite class.
        @Test
        public void testGetSlowestDeliveryPerOrder() {
            NotificationCenter center = new NotificationCenter();
            // Order 1: three delivered notifications; the slowest (max) is neither first nor last.
            center.addNotification(new Notification(10, 1, 0));
            center.addNotification(new Notification(11, 1, 100));
            center.addNotification(new Notification(12, 1, 0));
            // Order 2: one delivered (with a stray FAILED after delivery) and one never delivered.
            center.addNotification(new Notification(20, 2, 50));
            center.addNotification(new Notification(21, 2, 0));
            // Order 3: no delivered notifications at all -> omitted.
            center.addNotification(new Notification(30, 3, 0));
            center.addNotification(new Notification(31, 3, 0));
            // Order 4: delivered at time 0 -> latency 0 (boundary).
            center.addNotification(new Notification(40, 4, 0));

            center.addUpdates(10, java.util.Arrays.asList(
                    new Update(DeliveryStatus.SENT, 10),
                    new Update(DeliveryStatus.DELIVERED, 30)));   // latency 30
            center.addUpdates(11, java.util.Arrays.asList(
                    new Update(DeliveryStatus.DELIVERED, 150),
                    new Update(DeliveryStatus.SENT, 120))); // latency 50 (out of order)
            center.addUpdates(12, java.util.Arrays.asList(
                    new Update(DeliveryStatus.SENT, 5),
                    new Update(DeliveryStatus.DELIVERED, 20)));    // latency 20
            center.addUpdates(20, java.util.Arrays.asList(
                    new Update(DeliveryStatus.SENT, 60),
                    new Update(DeliveryStatus.DELIVERED, 80),
                    new Update(DeliveryStatus.FAILED, 90)));  // latency 30
            center.addUpdates(21, java.util.Collections.singletonList(
                    new Update(DeliveryStatus.SENT, 10)));                                   // never delivered
            center.addUpdates(30, java.util.Collections.singletonList(
                    new Update(DeliveryStatus.SENT, 5)));                                    // never delivered
            center.addUpdates(40, java.util.Collections.singletonList(
                    new Update(DeliveryStatus.DELIVERED, 0)));                               // latency 0

            java.util.Map<Integer, Integer> expected = new java.util.HashMap<>();
            expected.put(1, 50);
            expected.put(2, 30);
            expected.put(4, 0);
            assertEquals(expected, center.getSlowestDeliveryPerOrder());
        }
    }

    public static void main(String[] argv) {
        JUnitCore runner = new JUnitCore();

        runner.addListener(new RunListener() {
            private boolean failed;

            @Override
            public void testStarted(Description description) { failed = false; }

            @Override
            public void testFailure(Failure failure) { failed = true; }

            @Override
            public void testFinished(Description description) {
                String status = failed ? "Failed" : "Passed";
                System.out.println(status + ": " + description.getMethodName());
            }
        });

        Result result = runner.run(TestSuite.class);

        int failureNumber = 1;
        for (Failure failure : result.getFailures()) {
            System.out.println("\n  " + failureNumber++ + ") Failure: " + failure.getDescription().getMethodName());
            System.out.println(failure.getTrimmedTrace());
        }

        if (result.wasSuccessful()) {
            System.out.println("\nAll tests passed successfully.");
        } else {
            System.out.println("Some tests failed.");
        }
    }
}
