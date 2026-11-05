/*
We are building a program to manage equipment bookings at a sports facility. The facility has multiple pieces of equipment, such as treadmills and rowing machines, and members can reserve them for specific time periods.

Definitions:
- An "equipment" is an object representing a reservable item. It has properties for the ID and name.
- A "reservation" is an object representing a member booking for a specific piece of equipment over a time period. Times are represented as integers in minutes from the start of the day.
- A "FacilityManager" is the class used to manage all equipment and reservations.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) One of the tests is failing due to a bug in the code. Make the necessary changes to FacilityManager to fix the bug.
*/
/*
We are updating our system to support richer reservation queries. Facility managers need to look up all active reservations made by a specific member, and equipment coordinators need a quick summary of how each piece of equipment is being used.

2-1) Add getReservationsForMember() to FacilityManager.
     This function takes a member's name and returns a list of all their active reservations. If the member has no active reservations, return an empty list.

2-2) Add getEquipmentSummary() to FacilityManager.
     This function takes an equipment ID and returns a map with the following keys:
     - "total_reservations": the number of active reservations for that equipment.
     - "total_minutes": the total duration in minutes across all active reservations for that equipment.
     If the equipment has no active reservations, both values should be 0.

To assist you in testing these new functions, we have provided the testGetReservationsForMember and testGetEquipmentSummary tests.
*/

/*
The facility requires a minimum turnaround time of 30 minutes between reservations for the same piece of equipment, to allow for cleaning and sanitisation between members.

A piece of equipment is unavailable if the requested period overlaps an active reservation, or if there is less than 30 minutes between the requested period and any active reservation before or after it.

3-1) Add getAvailableEquipment() to FacilityManager.

This function takes a requested startTime and endTime and returns a sorted list of equipment IDs (ascending) that are fully available for the requested period, including the required 30-minute turnaround on either side. If no equipment is available, return an empty list.

A piece of equipment is unavailable when, considering only ACTIVE reservations:
- the requested period overlaps an active reservation, OR
- the gap between the requested period and an active reservation before it is less than 30 minutes, OR
- the gap between the requested period and an active reservation after it is less than 30 minutes.

To assist you in testing this new function, we have provided the testGetAvailableEquipment tests.
*/

import java.util.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {

    enum ReservationStatus {
        ACTIVE,
        CANCELLED
    }

    static class Equipment {
        final int equipmentId;
        final String name;

        Equipment(int equipmentId, String name) {
            this.equipmentId = equipmentId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Equipment ID: " + equipmentId + ", Name: " + name;
        }
    }

    static class Reservation {
        final int reservationId;
        final String memberName;
        final int equipmentId;
        final int startTime; //minutes 1 , 2,  60
        final int endTime;
        ReservationStatus status;

        Reservation(int reservationId, String memberName,
                    int equipmentId, int startTime, int endTime) {
            this.reservationId = reservationId;
            this.memberName = memberName;
            this.equipmentId = equipmentId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = ReservationStatus.ACTIVE;
        }

        int getDuration() {
            return endTime - startTime;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Reservation r)) return false;
            return reservationId == r.reservationId;
        }

        @Override public int hashCode() { 
            return Integer.hashCode(reservationId); 
        }

        @Override
        public String toString() {
            return "Reservation ID: " + reservationId
                    + ", Member: " + memberName
                    + ", Equipment ID: " + equipmentId
                    + ", Start: " + startTime
                    + ", End: " + endTime
                    + ", Status: " + status;
        }
    }

    static class FacilityManager {
        final ArrayList<Equipment> equipmentList = new ArrayList<>();
        final ArrayList<Reservation> reservations = new ArrayList<>();

        /** Adds a piece of equipment to the facility inventory. */
        void addEquipment(Equipment equipment) {
            equipmentList.add(equipment);
        }

        /**
         * Makes a reservation if the equipment is available for the requested period.
         * Returns true if successfully reserved, false if the equipment is unavailable.
         */
        boolean makeReservation(Reservation reservation) {
            if (!isAvailable(reservation.equipmentId,
                             reservation.startTime,
                             reservation.endTime)) {
                return false;
            }
            reservations.add(reservation);
            return true;
        }

        boolean isAvailable(int equipmentId, int startTime, int endTime) {
            for (Reservation res : reservations) {
                if (res.equipmentId == equipmentId) {
                    if (startTime < res.endTime && endTime > res.startTime) {
                        return false;
                    }
                }
            }
            return true;
        }

        boolean cancelReservation(int reservationId) {
            for (Reservation res : reservations) {
                if (res.reservationId == reservationId) {
                    res.status = ReservationStatus.CANCELLED;
                    return true;
                }
            }
            return false;
        }
    }

    // These tests are not meant to be exhaustive, and primarily show usage.
    public static class TestSuite {

        @Test
        public void testMakeReservation() {
            FacilityManager manager = new FacilityManager();
            Reservation res = new Reservation(101, "Alice Johnson", 1, 480, 600);
            assertTrue(manager.makeReservation(res));
            assertEquals(1, manager.reservations.size());
        }

        @Test
        public void testConflictDetected() {
            FacilityManager manager = new FacilityManager();
            Reservation res1 = new Reservation(101, "Alice Johnson", 1, 480, 600);
            Reservation res2 = new Reservation(102, "Bob Smith", 1, 540, 660);
            manager.makeReservation(res1);
            assertFalse(manager.makeReservation(res2));
        }

        @Test
        public void testDifferentEquipmentNoConflict() {
            FacilityManager manager = new FacilityManager();
            Reservation res1 = new Reservation(101, "Alice Johnson", 1, 480, 600);
            Reservation res2 = new Reservation(102, "Bob Smith", 2, 480, 600);
            assertTrue(manager.makeReservation(res1));
            assertTrue(manager.makeReservation(res2));
        }

        @Test
        public void testCancelledReservationFreesSlot() {
            FacilityManager manager = new FacilityManager();
            Reservation res1 = new Reservation(101, "Alice Johnson", 1, 480, 600);
            manager.makeReservation(res1);
            assertTrue(manager.cancelReservation(101));
            assertEquals(1, manager.reservations.size());
            assertEquals(ReservationStatus.CANCELLED, res1.status);
            // After cancellation, the same slot should be bookable again
            Reservation res2 = new Reservation(102, "Bob Smith", 1, 480, 600);
            assertTrue(manager.makeReservation(res2));
            assertEquals(2, manager.reservations.size());
        }
        
        @Test
        public void testGetReservationsForMember() {
            FacilityManager manager = new FacilityManager();
            Reservation res1 = new Reservation(101, "Alice Johnson", 1, 480, 600);
            Reservation res2 = new Reservation(102, "Bob Smith", 1, 660, 720);
            Reservation res3 = new Reservation(103, "Alice Johnson", 2, 720, 840);
            Reservation res4 = new Reservation(104, "Carol White", 2, 480, 540);
            manager.makeReservation(res1);
            manager.makeReservation(res2);
            manager.makeReservation(res3);
            manager.makeReservation(res4);

            List<Reservation> aliceRes = manager.getReservationsForMember("Alice Johnson");
            assertEquals(2, aliceRes.size());
            assertTrue(aliceRes.contains(res1));
            assertTrue(aliceRes.contains(res3));

            // new String(...) ensures comparison uses equals, not ==
            List<Reservation> aliceResByNewString =
                    manager.getReservationsForMember(new String("Alice Johnson"));
            assertEquals(2, aliceResByNewString.size());
            assertTrue(aliceResByNewString.contains(res1));
            assertTrue(aliceResByNewString.contains(res3));

            List<Reservation> bobRes = manager.getReservationsForMember("Bob Smith");
            assertEquals(1, bobRes.size());
            assertTrue(bobRes.contains(res2));

            // Member with no reservations
            assertEquals(0, manager.getReservationsForMember("David Brown").size());

            // Cancelled reservations should not appear
            manager.cancelReservation(101);
            List<Reservation> aliceResAfter = manager.getReservationsForMember("Alice Johnson");
            assertEquals(1, aliceResAfter.size());
            assertTrue(aliceResAfter.contains(res3));
        }

        @Test
        public void testGetEquipmentSummary() {
            FacilityManager manager = new FacilityManager();
            Reservation res1 = new Reservation(101, "Alice Johnson", 1, 480, 540);
            Reservation res2 = new Reservation(102, "Bob Smith", 1, 660, 720);
            Reservation res3 = new Reservation(103, "Carol White", 1, 720, 780);
            Reservation res4 = new Reservation(104, "David Brown", 2, 480, 600);
            manager.makeReservation(res1);
            manager.makeReservation(res2);
            manager.makeReservation(res3);
            manager.makeReservation(res4);

            Map<String, Integer> summaryE1 = manager.getEquipmentSummary(1);
            assertEquals(3, (int) summaryE1.get("total_reservations"));
            assertEquals(180, (int) summaryE1.get("total_minutes"));

            Map<String, Integer> summaryE2 = manager.getEquipmentSummary(2);
            assertEquals(1, (int) summaryE2.get("total_reservations"));
            assertEquals(120, (int) summaryE2.get("total_minutes"));

            // Equipment with no reservations
            Map<String, Integer> summaryE3 = manager.getEquipmentSummary(3);
            assertEquals(0, (int) summaryE3.get("total_reservations"));
            assertEquals(0, (int) summaryE3.get("total_minutes"));

            // Cancelled reservations should not be counted
            manager.cancelReservation(101);
            Map<String, Integer> summaryE1After = manager.getEquipmentSummary(1);
            assertEquals(2, (int) summaryE1After.get("total_reservations"));
            assertEquals(120, (int) summaryE1After.get("total_minutes"));
        }

        // Add inside the existing TestSuite class.

// Basic availability: free equipment is returned, overlapping equipment is excluded,
// and the result is sorted by equipmentId ascending.
@Test
public void testGetAvailableEquipment1() {
    FacilityManager manager = new FacilityManager();
    manager.addEquipment(new Equipment(1, "Treadmill"));
    manager.addEquipment(new Equipment(2, "Rower"));
    manager.addEquipment(new Equipment(3, "Bike"));

    // Equipment 2 is booked exactly over the requested window.
    manager.makeReservation(new Reservation(101, "Alice", 2, 480, 600));

    // Request 480-600: equipment 1 and 3 are free; equipment 2 overlaps.
    List<Integer> available = manager.getAvailableEquipment(480, 600);
    assertEquals(Arrays.asList(1, 3), available);
}

// Turnaround gap: a reservation ending (or starting) less than 30 minutes from the
// requested window makes the equipment unavailable; exactly 30 minutes is allowed.
@Test
public void testGetAvailableEquipment2() {
    FacilityManager manager = new FacilityManager();
    manager.addEquipment(new Equipment(1, "Treadmill")); // before, gap 20 -> unavailable
    manager.addEquipment(new Equipment(2, "Rower"));     // before, gap 30 -> available
    manager.addEquipment(new Equipment(3, "Bike"));      // after,  gap 20 -> unavailable
    manager.addEquipment(new Equipment(4, "Bench"));     // after,  gap 30 -> available

    // Requested window is 600-700.
    // Equipment 1: reservation ends 580 -> gap 20 (< 30) -> unavailable.
    manager.makeReservation(new Reservation(101, "Alice", 1, 500, 580));
    // Equipment 2: reservation ends 570 -> gap 30 (== 30) -> available.
    manager.makeReservation(new Reservation(102, "Bob",   2, 500, 570));
    // Equipment 3: reservation starts 720 -> gap 20 (< 30) -> unavailable.
    manager.makeReservation(new Reservation(103, "Carol", 3, 720, 800));
    // Equipment 4: reservation starts 730 -> gap 30 (== 30) -> available.
    manager.makeReservation(new Reservation(104, "Dave",  4, 730, 800));

    List<Integer> available = manager.getAvailableEquipment(600, 700);
    assertEquals(Arrays.asList(2, 4), available);
}

// Cancelled reservations must not block equipment, and a reservation that fully
// contains the requested window also makes the equipment unavailable.
@Test
public void testGetAvailableEquipment3() {
    FacilityManager manager = new FacilityManager();
    manager.addEquipment(new Equipment(1, "Treadmill"));
    manager.addEquipment(new Equipment(2, "Rower"));

    // Equipment 1: cancelled reservation overlapping the request -> must be ignored.
    Reservation cancelled = new Reservation(101, "Alice", 1, 550, 650);
    manager.makeReservation(cancelled);
    manager.cancelReservation(101);

    // Equipment 2: active reservation that fully contains the requested window.
    manager.makeReservation(new Reservation(102, "Bob", 2, 400, 900));

    // Request 600-620: equipment 1 available (cancelled ignored); equipment 2 blocked.
    List<Integer> available = manager.getAvailableEquipment(600, 620);
    assertEquals(Arrays.asList(1), available);
}

// Empty facility, equipment with no reservations, and both before+after reservations
// near the request (turnaround checked on both sides).
@Test
public void testGetAvailableEquipment4() {
    // Empty facility -> empty list.
    assertTrue(new FacilityManager().getAvailableEquipment(0, 60).isEmpty());

    FacilityManager manager = new FacilityManager();
    manager.addEquipment(new Equipment(1, "Never Booked"));
    manager.addEquipment(new Equipment(2, "Sandwiched"));

    // Equipment 2 has an active reservation before AND after the request, both within
    // 30 minutes -> unavailable on both sides.
    manager.makeReservation(new Reservation(101, "Alice", 2, 500, 570)); // before: gap 30? 570->600 = 30
    manager.makeReservation(new Reservation(102, "Bob",   2, 630, 700)); // after:  700? 620->630 = 10

    // Request 600-620:
    // Equipment 1: no reservations -> available.
    // Equipment 2: before gap 30 (ok) but after gap 10 (< 30) -> unavailable.
    List<Integer> available = manager.getAvailableEquipment(600, 620);
    assertEquals(Arrays.asList(1), available);

    // Tighten the "before" side too: move the earlier reservation to end at 590 (gap 10).
    FacilityManager manager2 = new FacilityManager();
    manager2.addEquipment(new Equipment(1, "Free"));
    manager2.addEquipment(new Equipment(2, "Tight"));
    manager2.makeReservation(new Reservation(201, "Carol", 2, 500, 590)); // gap 10 before
    assertEquals(Arrays.asList(1), manager2.getAvailableEquipment(600, 620));
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