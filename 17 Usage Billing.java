/*
We are building the backend for a SaaS usage billing system. The system tracks how much of each resource type (API calls, storage, compute) each customer consumes. Each usage event is recorded as a UsageRecord. One customer can have many usage records.

Definitions:
* A "UsageRecord" is one usage event recorded by the system. It has an ID, a customer ID, an amount (units consumed), a resource type (API_CALLS, STORAGE, or COMPUTE), and a timestamp (day number in the billing period).
* A "BillingEngine" is the class used to manage all usage records and compute billing.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for BillingEngine is not passing due to a bug in the code. Make the necessary changes to BillingEngine to fix the bug.
*/
/*
Now that the engine can summarize usage per customer, staff need tools to query usage within specific billing periods and identify the highest-volume customers.

2-1) Add getUsageByPeriod() to BillingEngine.
     This function takes a customer ID and a day range, and returns the total usage amount for that customer across all resource types, considering only records where the timestamp falls between startDay and endDay inclusive.
     Return 0 if no records fall in that range.

2-2) Add getTopCustomers() to BillingEngine.
     This function takes a resource type and a count, and returns the top n customer IDs by total usage for that resource type, sorted by total usage descending. If two customers have the same total usage, sort by customerId ascending. Return fewer than n if fewer customers have usage. Return an empty list if no customers have usage for that type.

The testGetUsageByPeriod and testGetTopCustomers tests are provided.
*/

/*
Beyond querying usage, the engine needs to compute actual bills. SaaS pricing uses progressive tiers, similar to tax brackets: usage is split into bands, and each band is billed at its own rate.

3-1) Add calculateBill() to BillingEngine.

This function takes a customerId, a resourceType, a list of tiers, and a minimumCharge amount. It computes the bill for a customer's total usage of the given resource type.

The tiers parameter is a list of (limit, rate) pairs, processed in order. Each pair means: bill up to "limit" units at "rate" per unit. Once a tier is full, excess usage spills into the next tier. A limit of 0 in the final tier means it accepts all remaining units with no cap.

For example, with tiers [(100, 0.10), (400, 0.07), (0, 0.03)], the first 100 units are billed at $0.10 each, the next 400 at $0.07 each, and any units beyond 500 at $0.03 each.

If the computed bill is below minimumCharge and the customer's usage is greater than 0, return minimumCharge instead. If the customer has no usage for the given resource type, return 0 regardless of minimumCharge.

To assist you in testing this new function, we have provided the testCalculateBill tests.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {
    /**
     * ResourceType indicates the category of the usage event:
     * API_CALLS, STORAGE, and COMPUTE.
     */
    enum ResourceType {
        API_CALLS, STORAGE, COMPUTE
    }

    /** Data about a single usage event. */
    static class UsageRecord {
        final int recordId;
        final int customerId;
        final double amount;                  // units consumed
        final ResourceType resourceType;      // a ResourceType
        final int timestamp;                  // day number in the billing period

        UsageRecord(int recordId, int customerId, double amount,
                    ResourceType resourceType, int timestamp) {
            this.recordId = recordId;
            this.customerId = customerId;
            this.amount = amount;
            this.resourceType = resourceType;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof UsageRecord r)) {
                return false;
            }
            return recordId == r.recordId
                    && customerId == r.customerId
                    && Double.compare(amount, r.amount) == 0
                    && resourceType == r.resourceType
                    && timestamp == r.timestamp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(recordId, customerId, amount, resourceType, timestamp);
        }

        @Override
        public String toString() {
            return "UsageRecord " + recordId + " (customer " + customerId
                    + ") amount=" + amount + ", type=" + resourceType
                    + ", timestamp=" + timestamp;
        }
    }

    /** Data for managing usage records, and methods staff use to query and compute billing. */
     static class BillingEngine {
        final ArrayList<UsageRecord> usageRecords = new ArrayList<>();

        /** Records a usage event. */
        void addUsageRecord(UsageRecord record) {
            usageRecords.add(record);
        }

        /** Returns the usage record with the given ID, or null if not found. */
        UsageRecord getUsageRecord(int recordId) {
            for (UsageRecord record : usageRecords) {
                if (record.recordId == recordId) {
                    return record;
                }
            }
            return null;
        }

        /** Returns a map of resource type name to total usage for the given customer. */
        Map<String, Double> getCustomerUsage(int customerId) {
            Map<String, Double> result = new HashMap<>();
            for (UsageRecord record : usageRecords) {
                if (record.customerId == customerId) {
                    String typeName = record.resourceType.name();
                    result.put(typeName, result.getOrDefault(typeName, 0.0) + record.amount);
                    return result;
                }
            }
            return result;
        }
    }


    /** This is not a complete test suite, but tests some basic functionality of the code and shows how to use it. */
    public static class TestSuite {

        @Test
        public void testUsageRecord() {
            // Test basic UsageRecord construction.
            UsageRecord r = new UsageRecord(1, 100, 50.0, ResourceType.API_CALLS, 5);
            assertEquals(1, r.recordId);
            assertEquals(100, r.customerId);
            assertEquals(50.0, r.amount, 0.001);
            assertEquals(ResourceType.API_CALLS, r.resourceType);
            assertEquals(5, r.timestamp);
        }

        @Test
        public void testGetCustomerUsage() {
            BillingEngine engine = new BillingEngine();
            engine.addUsageRecord(new UsageRecord(1, 100, 50.0, ResourceType.API_CALLS, 1));
            engine.addUsageRecord(new UsageRecord(2, 200, 30.0, ResourceType.STORAGE, 2));
            engine.addUsageRecord(new UsageRecord(3, 100, 100.0, ResourceType.API_CALLS, 3));
            engine.addUsageRecord(new UsageRecord(4, 100, 75.0, ResourceType.STORAGE, 5));
            engine.addUsageRecord(new UsageRecord(5, 100, 25.0, ResourceType.COMPUTE, 7));

            Map<String, Double> expected100 = Map.of(
                    "API_CALLS", 150.0, "STORAGE", 75.0, "COMPUTE", 25.0);
            assertEquals(expected100, engine.getCustomerUsage(100));

            Map<String, Double> expected200 = Map.of("STORAGE", 30.0);
            assertEquals(expected200, engine.getCustomerUsage(200));

            assertEquals(Map.of(), engine.getCustomerUsage(999));
        }
        
                // Add testGetUsageByPeriod and testGetTopCustomers inside TestSuite.

        @Test
        public void testGetUsageByPeriod() {
            BillingEngine engine = new BillingEngine();
            engine.addUsageRecord(new UsageRecord(1, 100, 50.0, ResourceType.API_CALLS, 1));
            engine.addUsageRecord(new UsageRecord(2, 100, 100.0, ResourceType.API_CALLS, 5));
            engine.addUsageRecord(new UsageRecord(3, 100, 75.0, ResourceType.STORAGE, 10));
            engine.addUsageRecord(new UsageRecord(4, 100, 200.0, ResourceType.COMPUTE, 15));
            engine.addUsageRecord(new UsageRecord(5, 200, 80.0, ResourceType.API_CALLS, 3));

            assertEquals(225.0, engine.getUsageByPeriod(100, 1, 10), 0.001);
            assertEquals(100.0, engine.getUsageByPeriod(100, 5, 5), 0.001);
            assertEquals(0.0, engine.getUsageByPeriod(100, 20, 30), 0.001);
            assertEquals(0.0, engine.getUsageByPeriod(999, 1, 30), 0.001);
        }

        @Test
        public void testGetTopCustomers() {
            BillingEngine engine = new BillingEngine();
            // Customer 300: 200 total API_CALLS (highest).
            engine.addUsageRecord(new UsageRecord(1, 300, 200.0, ResourceType.API_CALLS, 7));
            // Customer 200: 150 total API_CALLS (tied with 100).
            engine.addUsageRecord(new UsageRecord(2, 200, 150.0, ResourceType.API_CALLS, 3));
            // Customer 100: 150 total API_CALLS (tied with 200).
            engine.addUsageRecord(new UsageRecord(3, 100, 50.0, ResourceType.API_CALLS, 1));
            engine.addUsageRecord(new UsageRecord(4, 100, 100.0, ResourceType.API_CALLS, 5));
            // Customer 400: STORAGE only, no API_CALLS.
            engine.addUsageRecord(new UsageRecord(5, 400, 500.0, ResourceType.STORAGE, 2));

            assertEquals(List.of(300, 100), engine.getTopCustomers(ResourceType.API_CALLS, 2));
            assertEquals(List.of(300, 100, 200), engine.getTopCustomers(ResourceType.API_CALLS, 5));
            assertEquals(List.of(), engine.getTopCustomers(ResourceType.COMPUTE, 3));
        }
        // Add inside the existing TestSuite class.

/** Simple (limit, rate) pair used by calculateBill tests. */
static class Tier {
    final int limit;
    final double rate;
    Tier(int limit, double rate) {
        this.limit = limit;
        this.rate = rate;
    }
}

// Worked example from the prompt, plus the minimum-charge floor and the exact
// tier-boundary behaviour.
@Test
public void testCalculateBill1() {
    BillingEngine engine = new BillingEngine();
    engine.addUsageRecord(new UsageRecord(1, 100, 600.0, ResourceType.API_CALLS, 1));

    List<Tier> tiers = List.of(
        new Tier(100, 0.10),
        new Tier(400, 0.07),
        new Tier(0,   0.03)
    );

    // 600 units: 100*0.10 + 400*0.07 + 100*0.03 = 10 + 28 + 3 = 41.0
    assertEquals(41.0, engine.calculateBill(100, ResourceType.API_CALLS, tiers, 5.0), 0.001);

    // Customer 100 with no COMPUTE usage -> 0 regardless of minimumCharge.
    assertEquals(0.0, engine.calculateBill(100, ResourceType.COMPUTE, tiers, 99.0), 0.001);

    // Unknown customer -> 0 regardless of minimumCharge.
    assertEquals(0.0, engine.calculateBill(999, ResourceType.API_CALLS, tiers, 99.0), 0.001);
}

// Exact tier boundaries: usage equal to the first limit stays in the first tier;
// usage of zero is "no usage" and must return 0 even with a positive minimum.
@Test
public void testCalculateBill2() {
    BillingEngine engine = new BillingEngine();
    List<Tier> tiers = List.of(
        new Tier(100, 0.10),
        new Tier(400, 0.07),
        new Tier(0,   0.03)
    );

    // Exactly 100 units -> 100*0.10 = 10.0 (does not spill into the second tier).
    engine.addUsageRecord(new UsageRecord(1, 1, 100.0, ResourceType.API_CALLS, 1));
    assertEquals(10.0, engine.calculateBill(1, ResourceType.API_CALLS, tiers, 1.0), 0.001);

    // Exactly 500 units (100 + 400) -> 10 + 28 = 38.0, still nothing in the third tier.
    engine.addUsageRecord(new UsageRecord(2, 2, 500.0, ResourceType.API_CALLS, 1));
    assertEquals(38.0, engine.calculateBill(2, ResourceType.API_CALLS, tiers, 1.0), 0.001);

    // 501 units -> 10 + 28 + 0.03 = 38.03 (one unit spills into the third tier).
    engine.addUsageRecord(new UsageRecord(3, 3, 501.0, ResourceType.API_CALLS, 1));
    assertEquals(38.03, engine.calculateBill(3, ResourceType.API_CALLS, tiers, 1.0), 0.001);

    // Zero usage -> 0 even though minimumCharge is 5.0.
    engine.addUsageRecord(new UsageRecord(4, 4, 0.0, ResourceType.API_CALLS, 1));
    assertEquals(0.0, engine.calculateBill(4, ResourceType.API_CALLS, tiers, 5.0), 0.001);
}

// Test the minimum-charge floor: a bill strictly below minimumCharge is replaced
// by minimumCharge, and a bill at or above minimumCharge is returned unchanged.
@Test
public void testCalculateBill3() {
    BillingEngine engine = new BillingEngine();
    engine.addUsageRecord(new UsageRecord(1, 10, 50.0, ResourceType.STORAGE, 1));

    // Single tier: 50 units * 0.01 = 0.50 -> below min 5.0 -> floor to 5.0.
    List<Tier> cheap = List.of(new Tier(0, 0.01));
    assertEquals(5.0, engine.calculateBill(10, ResourceType.STORAGE, cheap, 5.0), 0.001);

    // Single tier: 50 units * 0.20 = 10.0 -> above min 5.0 -> 10.0.
    List<Tier> pricey = List.of(new Tier(0, 0.20));
    assertEquals(10.0, engine.calculateBill(10, ResourceType.STORAGE, pricey, 5.0), 0.001);

    // Exactly at the minimum -> returned as-is (not floored up, no rounding surprise).
    List<Tier> exact = List.of(new Tier(0, 0.10)); // 50 * 0.10 = 5.0
    assertEquals(5.0, engine.calculateBill(10, ResourceType.STORAGE, exact, 5.0), 0.001);

    // Minimum charge of 0 must not force a floor: 50 * 0.01 = 0.50 stays 0.50.
    assertEquals(0.50, engine.calculateBill(10, ResourceType.STORAGE, cheap, 0.0), 0.001);
}

// Test that usage for OTHER resource types (and other customers) is ignored, that
// multiple records for the same customer+type are summed before tiering, and that a
// one-tier "limit 0" schedule bills the whole usage at a flat rate.
@Test
public void testCalculateBill4() {
    BillingEngine engine = new BillingEngine();

    // Customer 20 has three API_CALLS records and one STORAGE record.
    engine.addUsageRecord(new UsageRecord(1, 20, 50.0,  ResourceType.API_CALLS, 1));
    engine.addUsageRecord(new UsageRecord(2, 20, 70.0,  ResourceType.API_CALLS, 2));
    engine.addUsageRecord(new UsageRecord(3, 20, 130.0, ResourceType.API_CALLS, 3));
    engine.addUsageRecord(new UsageRecord(4, 20, 999.0, ResourceType.STORAGE,   4));
    // Customer 21's API_CALLS must not affect customer 20's bill.
    engine.addUsageRecord(new UsageRecord(5, 21, 500.0, ResourceType.API_CALLS, 5));

    List<Tier> tiers = List.of(
        new Tier(100, 0.10),
        new Tier(0,   0.05)
    );

    // Customer 20 API_CALLS total = 50 + 70 + 130 = 250.
    // 100*0.10 + 150*0.05 = 10 + 7.5 = 17.5
    assertEquals(17.5, engine.calculateBill(20, ResourceType.API_CALLS, tiers, 1.0), 0.001);

    // Customer 20 STORAGE uses a flat "limit 0" tier: 999 * 0.02 = 19.98.
    List<Tier> flat = List.of(new Tier(0, 0.02));
    assertEquals(19.98, engine.calculateBill(20, ResourceType.STORAGE, flat, 1.0), 0.001);
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