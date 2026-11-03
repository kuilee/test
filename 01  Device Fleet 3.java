/*
We are building Device Fleet, a back-end system that manages the lifecycle of IoT devices across multiple locations.

Definitions:
* A "device" is an object representing a single piece of IoT hardware deployed at a location. A device has:
  - deviceId
  - deviceType
  - location
  - status
  - registeredDay (the day number, relative to a fixed reference point, on which the device was registered into the fleet)
  - nextMaintenanceDay (the day number by which the device's next scheduled maintenance must occur)
  - failureCount (the running count of failures reported by the device since registration)

* A device's status can be one of:
  - REGISTERED
  - ACTIVE
  - SUSPENDED
  - MAINTENANCE_DUE
  - RETIRED

The DeviceLifecycleManager class manages devices.

To begin with, we present you with two tasks:

1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for DeviceLifecycleManager is not passing due to a bug in the code. Make the necessary changes to DeviceLifecycleManager to fix the bug.
*/
/*
We are extending Device Fleet to register new devices and track how many are active by type. This helps operations and procurement teams see what's actually deployed and plan capacity.

Add two functions to the DeviceLifecycleManager class.

2-1) addDevice

The addDevice function takes a device object. It should add the device only when all of the following conditions are satisfied:
- Its device ID is not already used by another device.
- Its status is valid.
- Its device type is supported.
- Its registration day is zero or greater.

Return true when the device is added and false when any validation condition is not satisfied.

The function must leave the existing device collection unchanged when the device is invalid.

2-2) getActiveDeviceCountsByType

The getActiveDeviceCountsByType function takes no arguments. It should return a Map mapping each device type to the number of active devices of that type.

Device types with zero active devices should not be present in the Map.
*/
/*
We now need to flag devices that are due for maintenance. This lets maintenance teams know which devices need a technician dispatched before they keep running past their scheduled service window.

Add one function to the DeviceLifecycleManager class.

3-1) markDevicesDueForMaintenance

The markDevicesDueForMaintenance function takes a current day and a maximum failure count. It should update every qualifying device's status to MAINTENANCE_DUE, following these rules:
- A device qualifies only if its status is ACTIVE, its maintenance day is on or before the given current day, and its failure count is at or below the given maximum failure count.
- A device whose failure count is above the maximum is excluded, even if its maintenance day has arrived — it needs replacement rather than maintenance.
- Every other device is left unchanged, and no other attribute of a qualifying device is modified.

Return the number of devices whose status changed.

If the current day is negative, or the maximum failure count is negative, return 0 and make no changes.
*/
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import java.util.*;

class Device {
  String deviceId;
  String deviceType;
  String location;
  String status;
  int registeredDay;
  int nextMaintenanceDay;
  int failureCount;

  /**
   * Represents a single device with its identifying and lifecycle attributes.
   */
  public Device(
    String deviceId,
    String deviceType,
    String location,
    String status,
    int registeredDay,
    int nextMaintenanceDay,
    int failureCount
  ) {
    this.deviceId = deviceId;
    this.deviceType = deviceType;
    this.location = location;
    this.status = status;
    this.registeredDay = registeredDay;
    this.nextMaintenanceDay = nextMaintenanceDay;
    this.failureCount = failureCount;
  }
}

class DeviceLifecycleConfig {
  static final Set<String> SUPPORTED_DEVICE_TYPES = new HashSet<>(
    Arrays.asList("SENSOR", "CAMERA", "GATEWAY")
  );

  static final Set<String> VALID_STATUSES = new HashSet<>(
    Arrays.asList("REGISTERED", "ACTIVE", "SUSPENDED", "MAINTENANCE_DUE", "RETIRED")
  );

  static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();
  static {
    VALID_TRANSITIONS.put("REGISTERED", new HashSet<>(Arrays.asList("ACTIVE", "RETIRED")));
    VALID_TRANSITIONS.put("ACTIVE", new HashSet<>(Arrays.asList("SUSPENDED", "MAINTENANCE_DUE", "RETIRED")));
    VALID_TRANSITIONS.put("SUSPENDED", new HashSet<>(Arrays.asList("ACTIVE", "RETIRED")));
    VALID_TRANSITIONS.put("MAINTENANCE_DUE", new HashSet<>(Arrays.asList("ACTIVE", "RETIRED")));
    VALID_TRANSITIONS.put("RETIRED", new HashSet<>());
  }
}

class DeviceLifecycleManager {
  /**
   * Manages the collection of registered devices, keyed by device ID.
   */
  Map<String, Device> devices = new HashMap<>();

  /**
   * Updates the device's status if the transition is valid, and returns whether the update was applied.
   */
  public boolean updateDeviceStatus(String deviceId, String newStatus) {
    Device device = devices.get(deviceId);

    if (device == null) {
      return false;
    }

    if (!DeviceLifecycleConfig.VALID_STATUSES.contains(newStatus)) {
      return false;
    }

    device.status = newStatus;
    return true;
  }
}

public class Solution {

  public static class TestSuite {

    /**
     * Test updateDeviceStatus.
     */
    @Test
    public void testUpdateDeviceStatus1() {
      // ACTIVE -> SUSPENDED: valid transition.
      DeviceLifecycleManager managerA = new DeviceLifecycleManager();
      managerA.devices.put(
        "D-1",
        new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 50, 1)
      );
      boolean resultA = managerA.updateDeviceStatus("D-1", "SUSPENDED");
      assertTrue(resultA);
      assertEquals("SUSPENDED", managerA.devices.get("D-1").status);

      // MAINTENANCE_DUE -> ACTIVE.
      DeviceLifecycleManager managerB = new DeviceLifecycleManager();
      managerB.devices.put(
        "D-3",
        new Device("D-3", "GATEWAY", "WAREHOUSE-B", "MAINTENANCE_DUE", 1, 15, 2)
      );
      boolean resultB = managerB.updateDeviceStatus("D-3", "ACTIVE");
      assertTrue(resultB);
      assertEquals("ACTIVE", managerB.devices.get("D-3").status);

      // REGISTERED -> RETIRED.
      DeviceLifecycleManager managerC = new DeviceLifecycleManager();
      managerC.devices.put(
        "D-4",
        new Device("D-4", "SENSOR", "WAREHOUSE-B", "REGISTERED", 3, 30, 0)
      );
      boolean resultC = managerC.updateDeviceStatus("D-4", "RETIRED");
      assertTrue(resultC);
      assertEquals("RETIRED", managerC.devices.get("D-4").status);

      // SUSPENDED -> ACTIVE.
      DeviceLifecycleManager managerD = new DeviceLifecycleManager();
      managerD.devices.put(
        "D-5",
        new Device("D-5", "CAMERA", "WAREHOUSE-A", "SUSPENDED", 7, 40, 1)
      );
      boolean resultD = managerD.updateDeviceStatus("D-5", "ACTIVE");
      assertTrue(resultD);
      assertEquals("ACTIVE", managerD.devices.get("D-5").status);
    }

    /**
     * Test updateDeviceStatus.
     */
    @Test
    public void testUpdateDeviceStatus2() {
      // ACTIVE -> REGISTERED: invalid transition.
      DeviceLifecycleManager managerA = new DeviceLifecycleManager();
      managerA.devices.put(
        "D-1",
        new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 50, 1)
      );
      boolean resultA = managerA.updateDeviceStatus("D-1", "REGISTERED");
      // System.out.println(resultA);
      assertFalse(resultA);
      assertEquals("ACTIVE", managerA.devices.get("D-1").status);

      // RETIRED device.
      DeviceLifecycleManager managerB = new DeviceLifecycleManager();
      managerB.devices.put(
        "D-2",
        new Device("D-2", "CAMERA", "WAREHOUSE-A", "RETIRED", 5, 20, 4)
      );
      boolean resultB = managerB.updateDeviceStatus("D-2", "ACTIVE");
      assertFalse(resultB);
      assertEquals("RETIRED", managerB.devices.get("D-2").status);

      // Same-status transition.
      DeviceLifecycleManager managerC = new DeviceLifecycleManager();
      managerC.devices.put(
        "D-1",
        new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 50, 1)
      );
      boolean resultC = managerC.updateDeviceStatus("D-1", "ACTIVE");
      assertFalse(resultC);
      assertEquals("ACTIVE", managerC.devices.get("D-1").status);
    }

    /**
     * Test updateDeviceStatus.
     */
    @Test
    public void testUpdateDeviceStatus3() {
      // Unknown device ID.
      DeviceLifecycleManager managerA = new DeviceLifecycleManager();
      boolean resultA = managerA.updateDeviceStatus("UNKNOWN", "ACTIVE");
      assertFalse(resultA);

      // Unrecognized status.
      DeviceLifecycleManager managerB = new DeviceLifecycleManager();
      managerB.devices.put(
        "D-1",
        new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 50, 1)
      );
      boolean resultB = managerB.updateDeviceStatus("D-1", "BROKEN");
      assertFalse(resultB);
      assertEquals("ACTIVE", managerB.devices.get("D-1").status);
    }
    
    @Test
    public void testAddDevice1() {
      // Test that a valid device is added and stored, and a duplicate device ID is rejected.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();

      // A valid device is added and stored by its device ID.
      Device first = new Device("D-1", "SENSOR", "WAREHOUSE-A", "REGISTERED", 10, 40, 0);
      assertTrue(manager.addDevice(first));
      assertEquals(first, manager.devices.get("D-1"));

      // Duplicate device ID.
      Device second = new Device("D-1", "CAMERA", "WAREHOUSE-B", "ACTIVE", 20, 50, 1);
      assertFalse(manager.addDevice(second));
      assertEquals(first, manager.devices.get("D-1"));
    }

    @Test
    public void testAddDevice2() {
      // Test that a device failing any single validation rule is rejected and the collection is left unchanged.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();

      // Unsupported device type is rejected and never stored.
      Device unsupportedType = new Device("D-1", "THERMOSTAT", "WAREHOUSE-A", "REGISTERED", 10, 40, 0);
      assertFalse(manager.addDevice(unsupportedType));
      assertFalse(manager.devices.containsKey("D-1"));

      // Invalid status is rejected.
      Device invalidStatus = new Device("D-1", "SENSOR", "WAREHOUSE-A", "BROKEN", 10, 40, 0);
      assertFalse(manager.addDevice(invalidStatus));
      assertFalse(manager.devices.containsKey("D-1"));

      // Negative registeredDay is rejected.
      Device negativeRegisteredDay = new Device("D-1", "SENSOR", "WAREHOUSE-A", "REGISTERED", -1, 40, 0);
      assertFalse(manager.addDevice(negativeRegisteredDay));
      assertFalse(manager.devices.containsKey("D-1"));
    }

    @Test
    public void testAddDevice3() {
      // Test that a registeredDay of 0 is a valid boundary value.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();

      // registeredDay of 0.
      Device zeroRegisteredDay = new Device("D-1", "SENSOR", "WAREHOUSE-A", "REGISTERED", 0, 10, 0);
      assertTrue(manager.addDevice(zeroRegisteredDay));
    }

    @Test
    public void testGetActiveDeviceCountsByType1() {
      // Test that active devices are counted and grouped by device type, ignoring non-active devices.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();
      List<Device> devices = Arrays.asList(
        new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 40, 0),
        new Device("D-2", "SENSOR", "WAREHOUSE-B", "ACTIVE", 12, 42, 1),
        new Device("D-3", "CAMERA", "WAREHOUSE-A", "SUSPENDED", 15, 45, 2),
        new Device("D-4", "GATEWAY", "WAREHOUSE-C", "ACTIVE", 20, 50, 0),
        // REGISTERED device.
        new Device("D-5", "CAMERA", "WAREHOUSE-B", "REGISTERED", 18, 48, 0)
      );

      for (Device device : devices) {
        manager.devices.put(device.deviceId, device);
      }

      Map<String, Integer> result = manager.getActiveDeviceCountsByType();

      Map<String, Integer> expected = new HashMap<>();
      expected.put("SENSOR", 2);
      expected.put("GATEWAY", 1);

      assertEquals(expected, result);
    }

    @Test
    public void testGetActiveDeviceCountsByType2() {
      // Test that a device type with no active devices does not appear in the result.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();
      manager.devices.put(
        "D-1",
        new Device("D-1", "SENSOR", "WAREHOUSE-A", "SUSPENDED", 10, 40, 0)
      );

      Map<String, Integer> result = manager.getActiveDeviceCountsByType();

      assertEquals(new HashMap<>(), result);
    }
    
    // Test that ACTIVE devices due for maintenance are marked and counted, covering the inclusive day boundary, the failure count boundary, multiple qualifying devices, and preservation of other attributes.
    @Test
    public void testMarkDevicesDueForMaintenance1() {
      // Device due exactly on the current day (boundary); another not yet due.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();
      manager.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 0));
      manager.devices.put("D-2", new Device("D-2", "CAMERA", "WAREHOUSE-A", "ACTIVE", 10, 40, 1));

      int updated = manager.markDevicesDueForMaintenance(30, 5);

      assertEquals(1, updated);
      assertEquals("MAINTENANCE_DUE", manager.devices.get("D-1").status);
      assertEquals("ACTIVE", manager.devices.get("D-2").status);

      // Multiple ACTIVE devices due on the current day.
      DeviceLifecycleManager managerMulti = new DeviceLifecycleManager();
      managerMulti.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 0));
      managerMulti.devices.put("D-2", new Device("D-2", "CAMERA", "WAREHOUSE-A", "ACTIVE", 10, 40, 1));

      int updatedMulti = managerMulti.markDevicesDueForMaintenance(40, 5);

      assertEquals(2, updatedMulti);
      assertEquals("MAINTENANCE_DUE", managerMulti.devices.get("D-1").status);
      assertEquals("MAINTENANCE_DUE", managerMulti.devices.get("D-2").status);

      // Other device attributes on a marked device.
      DeviceLifecycleManager managerAttrs = new DeviceLifecycleManager();
      managerAttrs.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 0));

      Device device = managerAttrs.devices.get("D-1");
      int originalMaintenanceDay = device.nextMaintenanceDay;
      int originalFailures = device.failureCount;

      int updatedAttrs = managerAttrs.markDevicesDueForMaintenance(30, 5);

      assertEquals(1, updatedAttrs);
      assertEquals(originalMaintenanceDay, device.nextMaintenanceDay);
      assertEquals(originalFailures, device.failureCount);

      // failureCount exactly equal to maxFailureCount (boundary).
      DeviceLifecycleManager managerFailureBoundary = new DeviceLifecycleManager();
      managerFailureBoundary.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 5));

      int updatedFailureBoundary = managerFailureBoundary.markDevicesDueForMaintenance(30, 5);

      assertEquals(1, updatedFailureBoundary);
      assertEquals("MAINTENANCE_DUE", managerFailureBoundary.devices.get("D-1").status);
    }

    // Test that devices which are not ACTIVE (suspended, retired, or already awaiting maintenance), or whose failure count exceeds the maximum, are never changed or counted.
    @Test
    public void testMarkDevicesDueForMaintenance2() {
      DeviceLifecycleManager manager = new DeviceLifecycleManager();
      manager.devices.put("D-3", new Device("D-3", "GATEWAY", "WAREHOUSE-B", "SUSPENDED", 10, 20, 2));
      // Device already MAINTENANCE_DUE.
      manager.devices.put("D-4", new Device("D-4", "SENSOR", "WAREHOUSE-B", "MAINTENANCE_DUE", 10, 15, 3));
      manager.devices.put("D-5", new Device("D-5", "CAMERA", "WAREHOUSE-C", "RETIRED", 5, 10, 5));

      int updated = manager.markDevicesDueForMaintenance(100, 100);

      assertEquals(0, updated);
      assertEquals("SUSPENDED", manager.devices.get("D-3").status);
      assertEquals("MAINTENANCE_DUE", manager.devices.get("D-4").status);
      assertEquals("RETIRED", manager.devices.get("D-5").status);

      // failureCount above maxFailureCount, despite being ACTIVE and due.
      DeviceLifecycleManager managerFailures = new DeviceLifecycleManager();
      managerFailures.devices.put("D-6", new Device("D-6", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 6));

      int updatedFailures = managerFailures.markDevicesDueForMaintenance(30, 5);

      assertEquals(0, updatedFailures);
      assertEquals("ACTIVE", managerFailures.devices.get("D-6").status);
    }

    // Test the negative currentDay guard, the negative maxFailureCount guard, the currentDay==0 boundary, and an empty device collection.
    @Test
    public void testMarkDevicesDueForMaintenance3() {
      // Negative currentDay devices.
      DeviceLifecycleManager manager = new DeviceLifecycleManager();
      manager.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 0));
      manager.devices.put("D-2", new Device("D-2", "CAMERA", "WAREHOUSE-A", "ACTIVE", 10, 40, 1));

      // Negative currentDay.
      int updated = manager.markDevicesDueForMaintenance(-1, 5);

      assertEquals(0, updated);
      assertEquals("ACTIVE", manager.devices.get("D-1").status);
      assertEquals("ACTIVE", manager.devices.get("D-2").status);

      // Device with a negative nextMaintenanceDay.
      DeviceLifecycleManager negativeDayManager = new DeviceLifecycleManager();
      negativeDayManager.devices.put("D-7", new Device("D-7", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, -5, 0));

      int negativeDayUpdated = negativeDayManager.markDevicesDueForMaintenance(-1, 5);

      assertEquals(0, negativeDayUpdated);
      assertEquals("ACTIVE", negativeDayManager.devices.get("D-7").status);

      // Negative maxFailureCount.
      DeviceLifecycleManager negativeMaxManager = new DeviceLifecycleManager();
      negativeMaxManager.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, 0));

      int negativeMaxUpdated = negativeMaxManager.markDevicesDueForMaintenance(30, -1);

      assertEquals(0, negativeMaxUpdated);
      assertEquals("ACTIVE", negativeMaxManager.devices.get("D-1").status);

      // Device with a negative failureCount, otherwise due.
      DeviceLifecycleManager negativeFailureManager = new DeviceLifecycleManager();
      negativeFailureManager.devices.put("D-8", new Device("D-8", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 30, -2));

      int negativeFailureUpdated = negativeFailureManager.markDevicesDueForMaintenance(30, -1);

      assertEquals(0, negativeFailureUpdated);
      assertEquals("ACTIVE", negativeFailureManager.devices.get("D-8").status);

      // currentDay of 0 (boundary).
      DeviceLifecycleManager zeroManager = new DeviceLifecycleManager();
      zeroManager.devices.put("D-1", new Device("D-1", "SENSOR", "WAREHOUSE-A", "ACTIVE", 10, 0, 0));

      int zeroUpdated = zeroManager.markDevicesDueForMaintenance(0, 5);

      assertEquals(1, zeroUpdated);
      assertEquals("MAINTENANCE_DUE", zeroManager.devices.get("D-1").status);

      DeviceLifecycleManager emptyManager = new DeviceLifecycleManager();

      // Empty device collection.
      int emptyUpdated = emptyManager.markDevicesDueForMaintenance(0, 5);

      assertEquals(0, emptyUpdated);
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