/*
We are developing a payment calculation system for our members. The payment amount is determined as follows:

- For members with BRONZE Membership:
  - The first workout is free.
  - From the second workout onwards, each hour costs $10.

- For members with SILVER Membership:
  - The first three workouts are free.
  - From the fourth workout onwards, each hour costs $8.

- For members with GOLD Membership:
  - The first five workouts are free.
  - From the sixth workout onwards, each hour costs $6.

Workouts may not be listed in ID order. 
The duration of each workout is always rounded up to the nearest hour. For example, if a person spent 80 minutes in a workout, they would be charged for 2 hours.

3) Implement a `getDuePayments` function, which returns a dictionary associating each member ID with their due payment.

To help you understand the requirements and test this new function, we have provided the `testGetDuePayments` function.
*/

/*
We are currently updating our system to include information about workouts for our members. As part of this update, we have introduced the Workout class, which represents a single workout session for a member. Each object of the Workout class has a unique ID, as well as a start time and end time that are represented in the number of minutes spent from the start of the day. You can assume that all the Workouts are from the same day.

To implement these changes, we need to add two functions to the Membership class:

2.1) The `addWorkout` function takes a member ID and a `Workout` as inputs and associates the workout with that member. If the workout is associated successfully, the function should return `true`. If the given member does not exist while calling this function, the workout should be ignored and the function should return `false`.

2.2) The `getAverageWorkoutDurations` function should generate a map of member IDs onto their average workout durations in minutes. The returned map should include all members. If a member has no workouts, their value should be `null`.

To assist you in testing these new functions, we have provided the testAddWorkout and testGetAverageWorkoutDurations functions.
*/

/*
We are building a program to manage a gym's membership. The gym has multiple members, each with a unique ID, name, and membership status. The program allows gym staff to add new members, update members status, and get membership statistics.

Definitions:
* A "member" is an object that represents a gym member. It has properties for the ID, name, and membership status.
* A "membership" is a class which is used for managing members in the gym.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Please take as much time as necessary, and feel free to run the code.
1-2) The test for Membership is not passing due to a bug in the code. Make the necessary changes to Membership to fix the bug.
*/
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

enum MembershipStatus {
  /*
    Membership Status is of three types: BRONZE, SILVER and GOLD.
    BRONZE is the default membership a new member gets.
    SILVER and GOLD are paid memberships for the gym.
  */
  BRONZE,
  SILVER,
  GOLD
}

class Workout {
  /**
   * This class represents a single workout session for a member.
   * Each object of the Workout class has a unique ID, as well as
   * a start time and end time that are represented in the number
   * of minutes spent from the start of the day.
   */

  private int id;
  private int startTime;
  private int endTime;

  public Workout(int id, int startTime, int endTime) {
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public int getId() {
    return id;
  }

  public int getStartTime() {
    return startTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public int getDuration() {
    return endTime - startTime;
  }
}

class Member {
  /* Data about a gym member.*/
  public int memberId;
  public String name;
  public MembershipStatus membershipStatus;
  public List<Workout> workouts;

  public Member(int memberId, String name, MembershipStatus membershipStatus) {
    this.memberId = memberId;
    this.name = name;
    this.membershipStatus = membershipStatus;
    this.workouts = new ArrayList<>();
  }

  @Override
  public String toString() {
    return "Member ID: " + memberId + ", Name: " + name + ", Membership Status: " + membershipStatus;
  }
}

class Membership {
  /*
    Data for managing a gym membership, and methods which staff can
    use to perform any queries or updates.
  */
  public List<Member> members;

  public Membership() {
    members = new ArrayList<>();
  }

  public void addMember(Member member) {
    members.add(member);
  }

  public void updateMembership(int memberId, MembershipStatus membershipStatus) {
    for (Member member : members) {
      if (member.memberId == memberId) {
        member.membershipStatus = membershipStatus;
        break;
      }
    }
  }

  public MembershipStatistics getMembershipStatistics() {
    int totalMembers = members.size();
    int totalPaidMembers = 0;
    for (Member member : members) {
      if (member.membershipStatus == MembershipStatus.SILVER && member.membershipStatus == MembershipStatus.GOLD) {
        totalPaidMembers++;
      }
    }
    double conversionRate = (totalPaidMembers / (double) totalMembers) * 100.0;
    return new MembershipStatistics(totalMembers, totalPaidMembers, conversionRate);
  }
}

class MembershipStatistics {
  /*
    Class for returning the getMembershipStatistics result
  */
  public int totalMembers;
  public int totalPaidMembers;
  public double conversionRate;

  public MembershipStatistics(int totalMembers, int totalPaidMembers, double conversionRate) {
    this.totalMembers = totalMembers;
    this.totalPaidMembers = totalPaidMembers;
    this.conversionRate = conversionRate;
  }
}

public class Solution {

  public static class TestSuite {

    @Test
    public void testMember() {
      Member testMember = new Member(1, "John Doe", MembershipStatus.BRONZE);
      assertEquals(1, testMember.memberId);
      assertEquals("John Doe", testMember.name);
      assertEquals(MembershipStatus.BRONZE, testMember.membershipStatus);
    }

    @Test
    public void testMembership() {
      Membership testMembership = new Membership();
      Member testMember = new Member(1, "John Doe", MembershipStatus.BRONZE);
      testMembership.addMember(testMember);
      assertEquals(1, testMembership.members.size());

      testMembership.updateMembership(1, MembershipStatus.SILVER);
      assertEquals(MembershipStatus.SILVER, testMembership.members.get(0).membershipStatus);

      testMembership.addMember(new Member(2, "Alex C", MembershipStatus.BRONZE));
      testMembership.addMember(new Member(3, "Marie C", MembershipStatus.GOLD));
      testMembership.addMember(new Member(4, "Joe D", MembershipStatus.SILVER));
      testMembership.addMember(new Member(5, "June R", MembershipStatus.BRONZE));
      testMembership.addMember(new Member(6, "Westley D", MembershipStatus.SILVER));

      MembershipStatistics attendanceStats = testMembership.getMembershipStatistics();
      assertEquals(6, attendanceStats.totalMembers);
      assertEquals(4, attendanceStats.totalPaidMembers);
      assertEquals(true, Math.abs(attendanceStats.conversionRate - 66.67) < 0.1);
    }
    
      @Test
  public void testAddWorkout() {
    Membership testMembership = new Membership();
    Member testMember1 = new Member(12, "John Doe", MembershipStatus.SILVER);
    testMembership.addMember(testMember1);

    Member testMember2 = new Member(22, "Alex Cleeve", MembershipStatus.BRONZE);
    testMembership.addMember(testMember2);

    Workout testWorkout1 = new Workout(111, 10, 20);
    Workout testWorkout2 = new Workout(112, 15, 35);
    Workout testWorkout3 = new Workout(113, 20, 25);
    Workout testWorkout99 = new Workout(999, 1, 2);

    assertTrue(testMembership.addWorkout(12, testWorkout1));
    assertTrue(testMembership.addWorkout(22, testWorkout2));
    assertTrue(testMembership.addWorkout(12, testWorkout3));
    assertFalse(testMembership.addWorkout(404, testWorkout99));
  }

  @Test
  public void testGetAverageWorkoutDurations() {
    Membership testMembership = new Membership();
    Member testMember1 = new Member(12, "John Doe", MembershipStatus.SILVER);
    testMembership.addMember(testMember1);

    Member testMember2 = new Member(22, "Alex Cleeve", MembershipStatus.BRONZE);
    testMembership.addMember(testMember2);

    Member testMember3 = new Member(31, "Marie Cardiff", MembershipStatus.GOLD);
    testMembership.addMember(testMember3);

    Member testMember4 = new Member(37, "George Costanza", MembershipStatus.SILVER);
    testMembership.addMember(testMember4);

    Workout testWorkout1 = new Workout(101, 10, 20);
    Workout testWorkout2 = new Workout(102, 15, 35);
    Workout testWorkout3 = new Workout(103, 45, 90);
    Workout testWorkout4 = new Workout(104, 100, 155);
    Workout testWorkout5 = new Workout(105, 120, 200);
    Workout testWorkout6 = new Workout(106, 300, 400);
    Workout testWorkout7 = new Workout(107, 1000, 1010);
    Workout testWorkout8 = new Workout(108, 1010, 1045);

    testMembership.addWorkout(12, testWorkout1);
    testMembership.addWorkout(22, testWorkout2);
    testMembership.addWorkout(31, testWorkout3);
    testMembership.addWorkout(12, testWorkout4);
    testMembership.addWorkout(22, testWorkout5);
    testMembership.addWorkout(31, testWorkout6);
    testMembership.addWorkout(12, testWorkout7);
    testMembership.addWorkout(404, testWorkout8);

    Map<Integer, Double> averageDurations = testMembership.getAverageWorkoutDurations();
    assertTrue(Math.abs(averageDurations.get(12) - 25.0) < 0.1);
    assertTrue(Math.abs(averageDurations.get(22) - 50.0) < 0.1);
    assertTrue(Math.abs(averageDurations.get(31) - 72.5) < 0.1);
    assertTrue(averageDurations.containsKey(37) && averageDurations.get(37) == null);
    assertFalse(averageDurations.containsKey(404));
  }
  
    @Test
  public void testGetDuePayments() {
    // Test get_due_payments function
    Membership testMembership = new Membership();
    testMembership.addMember(new Member(1, "John Doe", MembershipStatus.BRONZE));
    testMembership.addMember(new Member(2, "Alex C", MembershipStatus.SILVER));
    testMembership.addMember(new Member(3, "Marie C", MembershipStatus.GOLD));

    // Add workouts for members
    Map<Integer, List<Workout>> memberWorkouts = new HashMap<>();
    memberWorkouts.put(1, Arrays.asList(
        new Workout(1, 500, 700), new Workout(10, 300, 350), new Workout(12, 10, 20),
        new Workout(3, 50, 90), new Workout(6, 130, 150), new Workout(15, 900, 960)
    ));
    memberWorkouts.put(2, Arrays.asList(
        new Workout(13, 510, 540), new Workout(14, 600, 700), new Workout(2, 15, 35),
        new Workout(4, 100, 155), new Workout(18, 200, 225), new Workout(8, 1050, 1155)
    ));
    memberWorkouts.put(3, Arrays.asList(
        new Workout(5, 120, 135), new Workout(17, 140, 190), new Workout(9, 210, 255),
        new Workout(11, 400, 450), new Workout(16, 910, 940), new Workout(7, 1000, 1100)
    ));

    for (Map.Entry<Integer, List<Workout>> entry : memberWorkouts.entrySet()) {
      int memberId = entry.getKey();
      List<Workout> workoutList = entry.getValue();
      for (Workout workout : workoutList) {
        testMembership.addWorkout(memberId, workout);
      }
    }

    Map<Integer, Integer> duePayments = testMembership.getDuePayments();
    assertEquals(Integer.valueOf(50), duePayments.get(1));
    assertEquals(Integer.valueOf(32), duePayments.get(2));
    assertEquals(Integer.valueOf(6), duePayments.get(3));
    
    // Test member with no workouts
    testMembership.addMember(new Member(4, "Ron Burgundy", MembershipStatus.SILVER));
    Map<Integer, Integer> duePayments2 = testMembership.getDuePayments();
    assertEquals(Integer.valueOf(0), duePayments2.get(4));
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