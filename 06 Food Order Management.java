/*
We are building a program to manage a food delivery platform. The platform has multiple restaurants, customers place orders, and those orders move through statuses: PLACED → PREPARING → OUT_FOR_DELIVERY → DELIVERED, or CANCELED.

Definitions:
* An "order" has: orderId, restaurantId, customerId, orderValue, distanceKm, and status.
* "OrderManager" manages orders and provides order statistics.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for OrderManager is not passing due to a bug in the code. Make the necessary changes to OrderManager to fix the bug.
 */

 /*
We are updating our system to include delivery session information for orders. Each delivery tracks when it started and when it ended.

We introduce a Delivery class:
- Each Delivery has a unique deliveryId.
- startMinute and endMinute represent minutes from the start of the day.
- An order may have multiple deliveries (e.g., reassignment).

2-1) Add addDelivery() to OrderManager. This function takes an order ID and a Delivery object and associates the delivery with that order. If the association is successful, return true. If the order does not exist, ignore the delivery and return false.

2-2) Add getAverageDeliveryTimeByRestaurant() to OrderManager. This function returns a map from each restaurant ID to the average delivery duration in minutes across all deliveries for that restaurant. Count ALL deliveries for that restaurant across all its orders.

To assist you in testing these new functions, we have provided the testAddDelivery and testGetAverageDeliveryTimeByRestaurant tests.
 */

 /*
We want to know the delivery fee for each order. Fees are calculated per order, not per delivery.

A delivery fee is computed in three steps:

Step 1: Round the order's distance UP to the nearest whole km. (1.0 => 1, 1.2 => 2, 2.1 => 3)
Step 2: Calculate the base fee: 2 for the first km, +1 per extra km. (1 km => 2, 2 km => 3, 3 km => 4, and so on)
Step 3: Apply the order value discount:
order_value >= 50 => fee = 0 (free delivery)
order_value >= 30 => fee is 50% off
Otherwise => use the base fee
Example: Order with distance 2.1 km and order value 35.0 — rounded km = 3, base fee = 4, 50% off applies, so fee = 2.0.

3) Add getDeliveryFees() to OrderManager. This function calculates and returns a map from each order ID to its corresponding fee.

To assist you in testing this new function, we have provided the testGetDeliveryFees test.
 */
import java.util.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {

    /**
     * PLACED means the order has been received. PREPARING means the restaurant
     * is preparing the order. OUT_FOR_DELIVERY means the order is on its way.
     * DELIVERED means the order has been received by the customer. CANCELED
     * means the order was cancelled before delivery.
     */
    enum OrderStatus {
        PLACED,
        PREPARING,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELED
    }

    static class Order {

        final int orderId;
        final int restaurantId;
        final int customerId;
        final double orderValue;
        final double distanceKm;
        OrderStatus status;

        Order(int orderId, int restaurantId, int customerId,
                double orderValue, double distanceKm, OrderStatus status) {
            this.orderId = orderId;
            this.restaurantId = restaurantId;
            this.customerId = customerId;
            this.orderValue = orderValue;
            this.distanceKm = distanceKm;
            this.status = status;
        }
    }

    static class OrderStats {

        final int totalOrders;
        final int activeOrders;
        final int closedOrders;

        OrderStats(int totalOrders, int activeOrders, int closedOrders) {
            this.totalOrders = totalOrders;
            this.activeOrders = activeOrders;
            this.closedOrders = closedOrders;
        }
    }

    static class Delivery {

        final int deliveryId;
        final int startMinute;
        final int endMinute;

        Delivery(int deliveryId, int startMinute, int endMinute) {
            this.deliveryId = deliveryId;
            this.startMinute = startMinute;
            this.endMinute = endMinute;
        }

        int getDurationMinutes() {
            return endMinute - startMinute;
        }
    }

    static class OrderManager {

        final List<Order> orders = new ArrayList<>();

        void addOrder(Order order) {
            orders.add(order);
        }

        void updateOrderStatus(int orderId, OrderStatus newStatus) {
            for (Order o : orders) {
                if (o.orderId == orderId) {
                    o.status = newStatus;
                    return;
                }
            }
        }

        OrderStats getOrderStatistics() {
            int total = orders.size();

            int active = 0;
            for (Order o : orders) {
                if (o.status == OrderStatus.PLACED || o.status == OrderStatus.OUT_FOR_DELIVERY) {
                    active++;
                }
            }

            int closed = 0;
            for (Order o : orders) {
                if (o.status == OrderStatus.DELIVERED || o.status == OrderStatus.CANCELED) {
                    closed++;
                }
            }

            return new OrderStats(total, active, closed);
        }
    }

    public static class TestSuite {

        @Test
        public void testOrderManager() {
            OrderManager om = new OrderManager();

            om.addOrder(new Order(1, 10, 100, 25.0, 3.2, OrderStatus.PLACED));
            om.addOrder(new Order(2, 10, 101, 55.0, 1.4, OrderStatus.PREPARING));
            om.addOrder(new Order(3, 11, 102, 15.0, 6.0, OrderStatus.OUT_FOR_DELIVERY));
            om.addOrder(new Order(4, 11, 103, 40.0, 2.0, OrderStatus.DELIVERED));
            om.addOrder(new Order(5, 12, 104, 18.0, 4.5, OrderStatus.CANCELED));

            OrderStats stats = om.getOrderStatistics();
            assertEquals(5, stats.totalOrders);
            assertEquals(3, stats.activeOrders);
            assertEquals(2, stats.closedOrders);
        }

        @Test
        public void testAddDelivery() {
            OrderManager om = new OrderManager();

            om.addOrder(new Order(1, 10, 100, 25.0, 3.2, OrderStatus.DELIVERED));
            om.addOrder(new Order(2, 10, 101, 55.0, 1.4, OrderStatus.DELIVERED));

            assertTrue(om.addDelivery(1, new Delivery(101, 10, 40)));
            assertTrue(om.addDelivery(2, new Delivery(102, 50, 80)));
            assertTrue(om.addDelivery(1, new Delivery(103, 90, 150)));

            // Non-existent order: delivery should be ignored
            assertFalse(om.addDelivery(999, new Delivery(105, 0, 10)));
        }

        @Test
        public void testGetAverageDeliveryTimeByRestaurant() {
            OrderManager om = new OrderManager();

            om.addOrder(new Order(1, 10, 100, 25.0, 3.2, OrderStatus.DELIVERED));
            om.addOrder(new Order(2, 10, 101, 55.0, 1.4, OrderStatus.DELIVERED));
            om.addOrder(new Order(3, 11, 102, 15.0, 6.0, OrderStatus.DELIVERED));

            om.addDelivery(1, new Delivery(101, 10, 40));   // 30 min, restaurant 10
            om.addDelivery(2, new Delivery(102, 50, 80));   // 30 min, restaurant 10
            om.addDelivery(2, new Delivery(103, 90, 150));  // 60 min, restaurant 10
            om.addDelivery(3, new Delivery(104, 20, 50));   // 30 min, restaurant 11

            // Ignore unknown order
            om.addDelivery(999, new Delivery(105, 0, 10));

            Map<Integer, Double> avg = om.getAverageDeliveryTimeByRestaurant();

            // restaurant 10: durations [30, 30, 60] => avg 40.0
            assertEquals(40.0, avg.get(10), 0.001);
            // restaurant 11: durations [30] => avg 30.0
            assertEquals(30.0, avg.get(11), 0.001);
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
