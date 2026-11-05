/*
We are building Stock Ledger, a back-end system that tracks inventory across warehouses for an e-commerce fulfillment platform. This keeps available-stock counts accurate so the platform doesn't oversell inventory that's already running low.

Definitions:
* A "SKU" (Stock Keeping Unit) uniquely identifies a product variant that can be sold (for example, a specific size and color); inventory is tracked per SKU at each warehouse.
* An "inventory record" is an object representing the current stock state of one SKU at one warehouse.
* An "inventory update" is an object representing an incoming change to one SKU's stock at one warehouse, in the same shape as an inventory record.
* A record's "available stock" is the difference between its onHandQuantity and its reservedQuantity. If reservedQuantity is greater than onHandQuantity, available stock is treated as 0 instead of a negative number.

Updates come from multiple warehouse systems and may arrive out of order because of network delays. The updatedAt value shows when the stock information was recorded by the warehouse system, not when Stock Ledger received it.

If an update has an updatedAt value older than the inventory record's current updatedAt, the update contains outdated stock information and should not replace the current record.

The StockLedger class manages inventory records.

To begin with, we present you with two tasks:

1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for StockLedger is not passing due to a bug in the code. Make the necessary changes to StockLedger to fix the bug.
*/

/*
We are extending Stock Ledger to let operations teams preview an incoming update's impact before it's applied, and see total available stock per SKU across all warehouses. This lets operations teams sanity-check incoming updates and monitor stock levels without waiting for changes to take effect.

Add two functions to the StockLedger class.

2-1) previewUpdate

The previewUpdate function takes an inventory update. It should return the change in on-hand quantity the update would cause, without applying it, following these rules:
- The change is the update's on-hand quantity minus the record's current on-hand quantity.
- If the update refers to an unknown SKU and warehouse pair, return null instead of a number.

2-2) getAvailableStockBySku

The getAvailableStockBySku function takes no arguments. It should return a Map mapping each SKU to its total available stock summed across all warehouses.
*/

/*
We now need to identify inventory records that are running low so operations can determine where replenishment is required.

Add one function to the StockLedger class.

3-1) Add getLowStockReport() to the StockLedger class.

The getLowStockReport function takes a lowStockThreshold and a targetStock value. It should return a list containing one entry for every inventory record that requires replenishment, following these rules:
- A record requires replenishment when its available stock is less than or equal to lowStockThreshold.
- Each entry contains the SKU, warehouse ID, available stock, and shortage quantity.
- Shortage quantity is targetStock minus available stock.
- Sort the result by shortage quantity descending, then SKU ascending, then warehouse ID ascending.
- If lowStockThreshold is negative, targetStock is zero or negative, or targetStock is less than or equal to lowStockThreshold, return an empty list.
- The function must not modify the inventory records.

To assist you in testing this new function, we have provided the testGetLowStockReport tests.
*/

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.*;
import static org.junit.Assert.assertTrue;


class InventoryRecord {
  String sku;
  int warehouseId;
  int onHandQuantity;
  int reservedQuantity;
  int updatedAt;

  public InventoryRecord(
    String sku,
    int warehouseId,
    int onHandQuantity,
    int reservedQuantity,
    int updatedAt
  ) {
    this.sku = sku;
    this.warehouseId = warehouseId;
    this.onHandQuantity = onHandQuantity;
    this.reservedQuantity = reservedQuantity;
    this.updatedAt = updatedAt;
  }
}

class InventoryUpdate {
  String sku;
  int warehouseId;
  int onHandQuantity;
  int reservedQuantity;
  int updatedAt;

  public InventoryUpdate(
    String sku,
    int warehouseId,
    int onHandQuantity,
    int reservedQuantity,
    int updatedAt
  ) {
    this.sku = sku;
    this.warehouseId = warehouseId;
    this.onHandQuantity = onHandQuantity;
    this.reservedQuantity = reservedQuantity;
    this.updatedAt = updatedAt;
  }
}
class LowStockEntry {
  String sku;
  int warehouseId;
  int availableStock;
  int shortage;

  public LowStockEntry(String sku, int warehouseId, int availableStock, int shortage) {
    this.sku = sku;
    this.warehouseId = warehouseId;
    this.availableStock = availableStock;
    this.shortage = shortage;
  }
}

class StockLedger {
  Map<String, InventoryRecord> inventoryRecords = new HashMap<>();

  public String recordKey(String sku, int warehouseId) {
    return sku + ":" + warehouseId;
  }

  public void registerInventoryRecord(InventoryRecord record) {
    inventoryRecords.put(
      recordKey(record.sku, record.warehouseId),
      record
    );
  }

  public void applyInventoryUpdate(InventoryUpdate update) {
    String key = recordKey(update.sku, update.warehouseId);

    if (!inventoryRecords.containsKey(key)) {
      return;
    }

    InventoryRecord record = inventoryRecords.get(key);
    record.onHandQuantity = update.onHandQuantity;
    record.reservedQuantity = update.reservedQuantity;
    record.updatedAt = update.updatedAt;
  }

  public int getAvailableStock(String sku, int warehouseId) {
    String key = recordKey(sku, warehouseId);

    if (!inventoryRecords.containsKey(key)) {
      return 0;
    }

    InventoryRecord record = inventoryRecords.get(key);

    return Math.max(
      record.onHandQuantity - record.reservedQuantity,
      0
    );
  }
}

public class Solution {

  public static class TestSuite {

    @Test
    public void testApplyInventoryUpdate1() {
      // Test that a newer update's values replace the record's stored values, including a boundary update whose timestamp equals the record's current timestamp.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 3000)
      );

      ledger.applyInventoryUpdate(
        new InventoryUpdate("SKU-1", 101, 80, 10, 2000)
      );

      InventoryRecord record =
        ledger.inventoryRecords.get("SKU-1:101");

      assertEquals(100, record.onHandQuantity);
      assertEquals(20, record.reservedQuantity);
      assertEquals(3000, record.updatedAt);
      assertEquals(80, ledger.getAvailableStock("SKU-1", 101));

      ledger.applyInventoryUpdate(
        new InventoryUpdate("SKU-1", 101, 120, 30, 4000)
      );

      assertEquals(120, record.onHandQuantity);
      assertEquals(30, record.reservedQuantity);
      assertEquals(4000, record.updatedAt);
      assertEquals(90, ledger.getAvailableStock("SKU-1", 101));

      // Update timestamp equal to the record's current timestamp (boundary).
      ledger.applyInventoryUpdate(
        new InventoryUpdate("SKU-1", 101, 200, 50, 4000)
      );

      assertEquals(200, record.onHandQuantity);
      assertEquals(50, record.reservedQuantity);
      assertEquals(4000, record.updatedAt);
    }

    @Test
    public void testApplyInventoryUpdate2() {
      // Test that an update for an unknown SKU and warehouse pair is ignored.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 3000)
      );

      ledger.applyInventoryUpdate(
        new InventoryUpdate("SKU-2", 202, 50, 5, 4000)
      );

      assertEquals(1, ledger.inventoryRecords.size());
      assertFalse(ledger.inventoryRecords.containsKey("SKU-2:202"));
    }

    @Test
    public void testApplyInventoryUpdate3() {
      // Test that available stock is floored at 0 when an applied update leaves reserved quantity greater than on-hand quantity.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 1000)
      );

      ledger.applyInventoryUpdate(
        new InventoryUpdate("SKU-1", 101, 30, 50, 2000)
      );

      InventoryRecord record =
        ledger.inventoryRecords.get("SKU-1:101");

      assertEquals(30, record.onHandQuantity);
      assertEquals(50, record.reservedQuantity);
      assertEquals(0, ledger.getAvailableStock("SKU-1", 101));
    }
  
    @Test
    public void testPreviewUpdate1() {
      // Test that the impact reflects the on-hand quantity change without modifying the record, in both directions.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 0)
      );

      Integer impact = ledger.previewUpdate(
        new InventoryUpdate("SKU-1", 101, 80, 10, 4000)
      );
      assertEquals(Integer.valueOf(-20), impact);

      impact = ledger.previewUpdate(
        new InventoryUpdate("SKU-1", 101, 150, 30, 1000)
      );
      assertEquals(Integer.valueOf(50), impact);

      InventoryRecord record =
        ledger.inventoryRecords.get("SKU-1:101");

      assertEquals(100, record.onHandQuantity);
      assertEquals(0, record.updatedAt);
    }

    @Test
    public void testPreviewUpdate2() {
      // Test that a zero-impact update returns 0, and that an unknown SKU and warehouse pair returns null.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 1000)
      );

      Integer impact = ledger.previewUpdate(
        new InventoryUpdate("SKU-1", 101, 100, 30, 2000)
      );
      assertEquals(Integer.valueOf(0), impact);

      impact = ledger.previewUpdate(
        new InventoryUpdate("SKU-2", 202, 50, 5, 9000)
      );
      assertEquals(null, impact);
    }

    @Test
    public void testGetAvailableStockBySku1() {
      // Test that available stock is summed across all warehouses for the same SKU, grouped by SKU.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 1000)
      );
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 102, 50, 10, 1000)
      );
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-2", 101, 30, 5, 1000)
      );

      Map<String, Integer> result =
        ledger.getAvailableStockBySku();

      Map<String, Integer> expected = new HashMap<>();
      expected.put("SKU-1", 120);
      expected.put("SKU-2", 25);

      assertEquals(expected, result);
    }

    @Test
    public void testGetAvailableStockBySku2() {
      // Test that a record with reserved quantity exceeding on-hand quantity contributes zero, not a negative amount.
      StockLedger ledger = new StockLedger();
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 100, 20, 1000)
      );
      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 102, 10, 50, 1000)
      );

      Map<String, Integer> result =
        ledger.getAvailableStockBySku();

      Map<String, Integer> expected = new HashMap<>();
      expected.put("SKU-1", 80);

      assertEquals(expected, result);
    }

    @Test
    public void testGetAvailableStockBySku3() {
      // Test that an empty ledger returns an empty result, and that a SKU whose only record has zero available stock still appears with a value of 0.
      StockLedger ledger = new StockLedger();

      assertEquals(new HashMap<>(), ledger.getAvailableStockBySku());

      ledger.registerInventoryRecord(
        new InventoryRecord("SKU-1", 101, 10, 50, 1000)
      );

      Map<String, Integer> result =
        ledger.getAvailableStockBySku();

      Map<String, Integer> expected = new HashMap<>();
      expected.put("SKU-1", 0);

      assertEquals(expected, result);
    }
    // Add inside the existing TestSuite class:
// Add inside the existing TestSuite class:

// Test basic filtering (available <= threshold), shortage = target - available,
// floored-at-zero available stock, exclusion of non-low records, and the primary
// sort key (shortage descending).
@Test
public void testGetLowStockReport1() {
  StockLedger ledger = new StockLedger();
  // available = onHand - reserved, floored at 0.
  ledger.registerInventoryRecord(new InventoryRecord("SKU-A", 101, 10, 2, 1000));  // avail 8
  ledger.registerInventoryRecord(new InventoryRecord("SKU-A", 102, 20, 0, 1000));  // avail 20 (not low)
  ledger.registerInventoryRecord(new InventoryRecord("SKU-B", 101, 5, 0, 1000));   // avail 5
  ledger.registerInventoryRecord(new InventoryRecord("SKU-C", 101, 3, 10, 1000));  // avail 0 (floored)

  List<LowStockEntry> report = ledger.getLowStockReport(10, 50);

  // Three low records; sorted by shortage desc:
  //   SKU-C/101 avail 0 -> shortage 50
  //   SKU-B/101 avail 5 -> shortage 45
  //   SKU-A/101 avail 8 -> shortage 42
  assertEquals(3, report.size());
  assertEquals("SKU-C", report.get(0).sku);
  assertEquals(101, report.get(0).warehouseId);
  assertEquals(0, report.get(0).availableStock);
  assertEquals(50, report.get(0).shortage);

  assertEquals("SKU-B", report.get(1).sku);
  assertEquals(101, report.get(1).warehouseId);
  assertEquals(5, report.get(1).availableStock);
  assertEquals(45, report.get(1).shortage);

  assertEquals("SKU-A", report.get(2).sku);
  assertEquals(101, report.get(2).warehouseId);
  assertEquals(8, report.get(2).availableStock);
  assertEquals(42, report.get(2).shortage);

  // SKU-A/102 is not low (avail 20 > 10) and must be absent.
  for (LowStockEntry e : report) {
    assertFalse(e.sku.equals("SKU-A") && e.warehouseId == 102);
  }

  // The report must not modify records.
  InventoryRecord a101 = ledger.inventoryRecords.get("SKU-A:101");
  assertEquals(10, a101.onHandQuantity);
  assertEquals(2, a101.reservedQuantity);
  assertEquals(1000, a101.updatedAt);
  InventoryRecord c101 = ledger.inventoryRecords.get("SKU-C:101");
  assertEquals(3, c101.onHandQuantity);
  assertEquals(10, c101.reservedQuantity);
  assertEquals(1000, c101.updatedAt);
}

// Test the secondary and tertiary sort keys: equal shortage -> SKU ascending, then
// warehouse ID ascending, and the "available == threshold" boundary counts as low.
@Test
public void testGetLowStockReport2() {
  StockLedger ledger = new StockLedger();
  // All four have avail 10 -> shortage 40 with target 50. Inserted out of order.
  ledger.registerInventoryRecord(new InventoryRecord("SKU-B", 102, 10, 0, 1000)); // avail 10
  ledger.registerInventoryRecord(new InventoryRecord("SKU-B", 101, 10, 0, 1000)); // avail 10
  ledger.registerInventoryRecord(new InventoryRecord("SKU-A", 102, 10, 0, 1000)); // avail 10
  ledger.registerInventoryRecord(new InventoryRecord("SKU-A", 101, 10, 0, 1000)); // avail 10

  List<LowStockEntry> report = ledger.getLowStockReport(10, 50);

  assertEquals(4, report.size());
  // Equal shortage 40 -> SKU asc, then warehouseId asc:
  //   SKU-A/101, SKU-A/102, SKU-B/101, SKU-B/102
  assertEquals("SKU-A", report.get(0).sku); assertEquals(101, report.get(0).warehouseId);
  assertEquals("SKU-A", report.get(1).sku); assertEquals(102, report.get(1).warehouseId);
  assertEquals("SKU-B", report.get(2).sku); assertEquals(101, report.get(2).warehouseId);
  assertEquals("SKU-B", report.get(3).sku); assertEquals(102, report.get(3).warehouseId);
  for (LowStockEntry e : report) {
    assertEquals(40, e.shortage);
    assertEquals(10, e.availableStock);
  }

  // Boundary: available (10) == threshold (10) counts as low -> included.
  boolean foundBoundary = false;
  for (LowStockEntry e : report) {
    if (e.sku.equals("SKU-A") && e.warehouseId == 101) foundBoundary = true;
  }
  assertTrue(foundBoundary);
}

// Test every invalid-argument guard returns an empty list, and that a valid call on
// the same ledger is non-empty (so the guards are not masking a broken filter).
@Test
public void testGetLowStockReport3() {
  StockLedger ledger = new StockLedger();
  // avail = 10 - 0 = 10.
  ledger.registerInventoryRecord(new InventoryRecord("SKU-A", 101, 10, 0, 1000));

  // Negative threshold.
  assertTrue(ledger.getLowStockReport(-1, 50).isEmpty());
  // Zero target.
  assertTrue(ledger.getLowStockReport(10, 0).isEmpty());
  // Negative target.
  assertTrue(ledger.getLowStockReport(10, -5).isEmpty());
  // target == threshold.
  assertTrue(ledger.getLowStockReport(10, 10).isEmpty());
  // target < threshold.
  assertTrue(ledger.getLowStockReport(10, 5).isEmpty());

  // Valid call on the same ledger: threshold 10 >= 0, target 11 > 10, avail 10 <= 10.
  // -> one entry with shortage 11 - 10 = 1. Proves the guards above did not hide a
  // broken filter, and that target > threshold by exactly 1 is legal.
  List<LowStockEntry> valid = ledger.getLowStockReport(10, 11);
  assertEquals(1, valid.size());
  assertEquals("SKU-A", valid.get(0).sku);
  assertEquals(101, valid.get(0).warehouseId);
  assertEquals(10, valid.get(0).availableStock);
  assertEquals(1, valid.get(0).shortage);
}

// Test the empty ledger, the floored-at-zero available stock (reserved > onHand),
// and that the call leaves records untouched.
@Test
public void testGetLowStockReport4() {
  StockLedger ledger = new StockLedger();

  // Empty ledger -> empty list (not null).
  assertTrue(ledger.getLowStockReport(10, 50).isEmpty());

  // reserved (20) > onHand (5) -> available floored at 0, not -15.
  ledger.registerInventoryRecord(new InventoryRecord("SKU-X", 7, 5, 20, 1234));

  List<LowStockEntry> report = ledger.getLowStockReport(10, 50);
  assertEquals(1, report.size());
  assertEquals("SKU-X", report.get(0).sku);
  assertEquals(7, report.get(0).warehouseId);
  assertEquals(0, report.get(0).availableStock); // floored, not -15
  assertEquals(50, report.get(0).shortage);

  // Record must be unchanged after the report.
  InventoryRecord rec = ledger.inventoryRecords.get("SKU-X:7");
  assertEquals(5, rec.onHandQuantity);
  assertEquals(20, rec.reservedQuantity);
  assertEquals(1234, rec.updatedAt);
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