/*
We are building a program to manage employee onboarding at a company. Each new employee is assigned a set of onboarding steps that must be completed before their onboarding is considered finished. Steps are assigned to owners (HR staff, IT, managers) who are responsible for completing them.

Definitions:
- An "Employee" is an object representing a new hire. It has properties for the employee ID, name, and start date (integer day number since a reference point).
- An "OnboardingStep" is a task that must be completed as part of an employee's onboarding. It has properties for the step ID, employee ID, step name, status, owner ID, and deadline (integer day number).
- An "OnboardingManager" is the class used to manage all employees and their onboarding steps.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) One of the tests is failing due to a bug in the code. Make the necessary changes to OnboardingManager to fix the bug.
*/
 
 /*
As onboarding workflows are managed, steps need to be reassigned to different owners, and HR managers need a view of how much active work each owner currently has.

2-1) Add assignStep() to OnboardingManager.
     This function takes a stepId and an ownerId, and reassigns the given step to the specified owner.
     Returns true if the step was found and updated, false if the step does not exist.

2-2) Add getWorkloadSummary() to OnboardingManager.
     This function returns a map from ownerId to the number of steps currently assigned to them that are NOT COMPLETED.
     Only include owners who have at least one non-completed step.
     Owners whose steps are all COMPLETED should not appear.

To assist you in testing these new functions, we have provided the testAssignStep and testGetWorkloadSummary tests.
*/

/*
We are adding a readiness check to help onboarding coordinators see what work can start next. Not all onboarding steps can be started immediately — some depend on earlier steps being finished first. A step is ready to start only when all steps with a lower step ID for the same employee are already COMPLETED.

3) Add getReadySteps() to OnboardingManager.

This function takes an employeeId and returns the onboarding steps that are ready to start for the given employee — steps that are PENDING and whose prerequisites (all lower-numbered steps) are fully COMPLETED.

Rules:
- Only PENDING steps can be ready to start.
- A PENDING step is ready only if every step with a lower stepId for the same employee is COMPLETED.
- Return steps sorted by stepId ascending.
- Return an empty list if no steps are ready.

To assist you in testing this new function, we have provided the testGetReadySteps tests.
*/
 
import java.util.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;


public class Solution {

    enum StepStatus {
        /**
         * PENDING means the step has not been started yet.
         * IN_PROGRESS means the step is currently being worked on.
         * COMPLETED means the step has been finished.
         * BLOCKED means the step cannot proceed due to a dependency.
         */
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        BLOCKED
    }

    /** Data about a new hire. */
    static class Employee {
        final int employeeId;
        final String name;
        final int startDate;

        Employee(int employeeId, String name, int startDate) {
            this.employeeId = employeeId;
            this.name = name;
            this.startDate = startDate;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Employee e)) return false;
            return employeeId == e.employeeId;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(employeeId);
        }

        @Override
        public String toString() {
            return "Employee ID: " + employeeId
                    + ", Name: " + name
                    + ", Start Date: " + startDate;
        }
    }

    /** Data about a single onboarding task. */
    static class OnboardingStep {
        final int stepId;
        final int employeeId;
        final String name;
        StepStatus status;
        int ownerId;
        final int deadline;

        OnboardingStep(int stepId, int employeeId, String name,
                       StepStatus status, int ownerId, int deadline) {
            this.stepId = stepId;
            this.employeeId = employeeId;
            this.name = name;
            this.status = status;
            this.ownerId = ownerId;
            this.deadline = deadline;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof OnboardingStep s)) return false;
            return stepId == s.stepId;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(stepId);
        }

        @Override
        public String toString() {
            return "Step ID: " + stepId
                    + ", Employee ID: " + employeeId
                    + ", Name: " + name
                    + ", Status: " + status
                    + ", Owner: " + ownerId
                    + ", Deadline: " + deadline;
        }
    }

    /**
     * Manages employee onboarding workflows.
     * Employees are identified by employeeId.
     * Steps are stored and can be queried or updated.
     * Onboarding is complete only when ALL steps for an employee are COMPLETED.
     */
   static class OnboardingManager {
        final List<Employee> employees = new ArrayList<>();
        final List<OnboardingStep> steps = new ArrayList<>();

        /** Adds a new employee to the system. */
        void addEmployee(Employee employee) {
            employees.add(employee);
        }

        /** Adds an onboarding step for an employee. */
        void addStep(OnboardingStep step) {
            steps.add(step);
        }

        /**
         * Updates the status of a step by ID.
         * Returns true if found and updated, false otherwise.
         */
        boolean updateStepStatus(int stepId, StepStatus status) {
            for (OnboardingStep step : steps) {
                if (step.stepId == stepId) {
                    step.status = status;
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns true if ALL onboarding steps for the given employee are COMPLETED. Returns false if any step is not yet COMPLETED or if the employee has no steps.
         */
        boolean isOnboardingComplete(int employeeId) {
            List<OnboardingStep> employeeSteps = new ArrayList<>();
            for (OnboardingStep step : steps) {
                if (step.employeeId == employeeId) employeeSteps.add(step);
            }
            if (employeeSteps.isEmpty()) return false;
            for (OnboardingStep step : employeeSteps) {
                if (step.status == StepStatus.COMPLETED) return true;
            }
            return false;
        }

        /** Returns all onboarding steps for a given employee. */
        List<OnboardingStep> getStepsForEmployee(int employeeId) {
            List<OnboardingStep> result = new ArrayList<>();
            for (OnboardingStep step : steps) {
                if (step.employeeId == employeeId) result.add(step);
            }
            return result;
        }
    }

    public static class TestSuite {
        // These tests are not meant to be exhaustive, and primarily show usage.

        @Test
        public void testAddEmployeeAndSteps() {
            OnboardingManager manager = new OnboardingManager();
            Employee emp = new Employee(1, "Alice Johnson", 100);
            manager.addEmployee(emp);
            OnboardingStep step1 = new OnboardingStep(1, 1, "Submit Documents", StepStatus.PENDING,    10, 105);
            OnboardingStep step2 = new OnboardingStep(2, 1, "IT Setup",         StepStatus.PENDING,    20, 107);
            OnboardingStep step3 = new OnboardingStep(3, 1, "Manager Meeting",  StepStatus.PENDING,    30, 110);
            manager.addStep(step1);
            manager.addStep(step2);
            manager.addStep(step3);
            assertEquals(1, manager.employees.size());
            assertEquals(3, manager.steps.size());
        }

        @Test
        public void testOnboardingCompleteWhenAllDone() {
            OnboardingManager manager = new OnboardingManager();
            manager.addEmployee(new Employee(1, "Alice Johnson", 100));
            manager.addStep(new OnboardingStep(1, 1, "Submit Documents", StepStatus.COMPLETED, 10, 105));
            manager.addStep(new OnboardingStep(2, 1, "IT Setup",         StepStatus.COMPLETED, 20, 107));
            manager.addStep(new OnboardingStep(3, 1, "Manager Meeting",  StepStatus.COMPLETED, 30, 110));
            assertTrue(manager.isOnboardingComplete(1));
        }

        @Test
        public void testOnboardingNotCompleteWhenPartiallyDone() {
            OnboardingManager manager = new OnboardingManager();
            manager.addEmployee(new Employee(1, "Alice Johnson", 100));
            manager.addStep(new OnboardingStep(1, 1, "Submit Documents", StepStatus.COMPLETED,   10, 105));
            manager.addStep(new OnboardingStep(2, 1, "IT Setup",         StepStatus.IN_PROGRESS, 20, 107));
            manager.addStep(new OnboardingStep(3, 1, "Manager Meeting",  StepStatus.PENDING,     30, 110));
            // Only one step is COMPLETED — onboarding should NOT be complete
            assertFalse(manager.isOnboardingComplete(1));
        }

        @Test
        public void testOnboardingNotCompleteWhenNoSteps() {
            OnboardingManager manager = new OnboardingManager();
            manager.addEmployee(new Employee(1, "Alice Johnson", 100));
            assertFalse(manager.isOnboardingComplete(1));
        }
        
        // Add testAssignStep and testGetWorkloadSummary inside TestSuite.

        @Test
        public void testAssignStep() {
            OnboardingManager manager = new OnboardingManager();
            manager.addEmployee(new Employee(1, "Alice Johnson", 100));
            manager.addStep(new OnboardingStep(1, 1, "Submit Documents", StepStatus.PENDING,     10, 105));
            manager.addStep(new OnboardingStep(2, 1, "IT Setup",         StepStatus.IN_PROGRESS, 20, 107));
            manager.addStep(new OnboardingStep(3, 1, "Manager Meeting",  StepStatus.PENDING,     30, 110));

            // Successfully reassign a step to a new owner
            assertTrue(manager.assignStep(1, 99));
            assertEquals(99, manager.steps.get(0).ownerId);

            // Owner of other steps unchanged
            assertEquals(20, manager.steps.get(1).ownerId);
            assertEquals(30, manager.steps.get(2).ownerId);

            // Step does not exist
            assertFalse(manager.assignStep(999, 10));

            // Reassign to same owner is still valid
            assertTrue(manager.assignStep(2, 20));
            assertEquals(20, manager.steps.get(1).ownerId);
        }

        @Test
        public void testGetWorkloadSummary() {
            OnboardingManager manager = new OnboardingManager();
            manager.addEmployee(new Employee(1, "Alice Johnson", 100));
            manager.addEmployee(new Employee(2, "Bob Smith",     101));

            // Owner 10: 2 steps — one COMPLETED, one PENDING → workload = 1
            manager.addStep(new OnboardingStep(1, 1, "Submit Documents", StepStatus.COMPLETED,   10, 105));
            manager.addStep(new OnboardingStep(2, 1, "IT Setup",         StepStatus.PENDING,     10, 107));

            // Owner 20: 2 steps — both non-completed → workload = 2
            manager.addStep(new OnboardingStep(3, 1, "Manager Meeting",  StepStatus.IN_PROGRESS, 20, 110));
            manager.addStep(new OnboardingStep(4, 2, "Badge Setup",      StepStatus.BLOCKED,     20, 108));

            // Owner 30: 1 step — COMPLETED → should not appear
            manager.addStep(new OnboardingStep(5, 2, "HR Orientation",   StepStatus.COMPLETED,   30, 106));

            Map<Integer, Integer> summary = manager.getWorkloadSummary();

            // Owner 10 has 1 non-completed step
            assertEquals(Integer.valueOf(1), summary.get(10));

            // Owner 20 has 2 non-completed steps
            assertEquals(Integer.valueOf(2), summary.get(20));

            // Owner 30 has no non-completed steps — must not appear
            assertFalse(summary.containsKey(30));

            // Only 2 owners in summary
            assertEquals(2, summary.size());

            // All steps completed — empty summary
            OnboardingManager manager2 = new OnboardingManager();
            manager2.addEmployee(new Employee(3, "Carol White", 102));
            manager2.addStep(new OnboardingStep(6, 3, "Submit Documents", StepStatus.COMPLETED, 10, 105));
            manager2.addStep(new OnboardingStep(7, 3, "IT Setup",         StepStatus.COMPLETED, 20, 107));
            assertTrue(manager2.getWorkloadSummary().isEmpty());
        }

        // Add inside the existing TestSuite class:

// Test the basic readiness gate: the first step of an employee is ready when PENDING,
// and a later step becomes ready only after all lower stepIds are COMPLETED.
@Test
public void testGetReadySteps1() {
    OnboardingManager manager = new OnboardingManager();
    manager.addEmployee(new Employee(1, "Alice", 100));

    // Steps 1..3 are all PENDING. Only step 1 is ready (nothing lower than it).
    manager.addStep(new OnboardingStep(1, 1, "Docs",   StepStatus.PENDING, 10, 105));
    manager.addStep(new OnboardingStep(2, 1, "IT",     StepStatus.PENDING, 20, 107));
    manager.addStep(new OnboardingStep(3, 1, "Meet",   StepStatus.PENDING, 30, 110));

    List<OnboardingStep> ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(1, ready.get(0).stepId);

    // Complete step 1 -> step 2 becomes ready, step 3 still blocked by step 2.
    manager.updateStepStatus(1, StepStatus.COMPLETED);
    ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(2, ready.get(0).stepId);

    // Complete steps 1 and 2 -> step 3 becomes ready.
    manager.updateStepStatus(2, StepStatus.COMPLETED);
    ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(3, ready.get(0).stepId);
}

// Test that only PENDING steps can be ready: a lower step that is IN_PROGRESS or
// BLOCKED still blocks all higher steps.
@Test
public void testGetReadySteps2() {
    OnboardingManager manager = new OnboardingManager();
    manager.addEmployee(new Employee(1, "Alice", 100));

    // Step 1 PENDING -> ready. Step 2 PENDING but blocked by step 1.
    manager.addStep(new OnboardingStep(1, 1, "Docs", StepStatus.PENDING,     10, 105));
    manager.addStep(new OnboardingStep(2, 1, "IT",   StepStatus.PENDING,     20, 107));

    List<OnboardingStep> ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(1, ready.get(0).stepId);

    // Step 1 becomes IN_PROGRESS -> not COMPLETED, so step 2 remains blocked,
    // and step 1 itself is not PENDING so it is no longer ready either.
    manager.updateStepStatus(1, StepStatus.IN_PROGRESS);
    ready = manager.getReadySteps(1);
    assertTrue(ready.isEmpty());

    // Step 1 becomes BLOCKED -> same: nothing ready.
    manager.updateStepStatus(1, StepStatus.BLOCKED);
    assertTrue(manager.getReadySteps(1).isEmpty());

    // Step 1 COMPLETED -> step 2 ready.
    manager.updateStepStatus(1, StepStatus.COMPLETED);
    ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(2, ready.get(0).stepId);
}

// Test that a non-PENDING step is never returned even if all its prerequisites are
// COMPLETED, and that results are sorted by stepId ascending.
@Test
public void testGetReadySteps3() {
    OnboardingManager manager = new OnboardingManager();
    manager.addEmployee(new Employee(1, "Alice", 100));

    // Lower steps all COMPLETED, higher steps mixed.
    manager.addStep(new OnboardingStep(1, 1, "A", StepStatus.COMPLETED,   10, 100));
    manager.addStep(new OnboardingStep(2, 1, "B", StepStatus.COMPLETED,   10, 101));
    // Step 3 is PENDING and all lower are COMPLETED -> ready.
    manager.addStep(new OnboardingStep(3, 1, "C", StepStatus.PENDING,     10, 102));
    // Step 4 is IN_PROGRESS -> never ready, even though lower steps are done.
    manager.addStep(new OnboardingStep(4, 1, "D", StepStatus.IN_PROGRESS, 10, 103));
    // Step 5 PENDING but blocked by step 4 (not COMPLETED) -> not ready.
    manager.addStep(new OnboardingStep(5, 1, "E", StepStatus.PENDING,     10, 104));

    List<OnboardingStep> ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(3, ready.get(0).stepId);

    // Now complete step 4 -> step 5's prerequisites (1..4) are all COMPLETED.
    manager.updateStepStatus(4, StepStatus.COMPLETED);
    ready = manager.getReadySteps(1);
    // Step 3 still PENDING and ready; step 5 now PENDING and ready.
    assertEquals(2, ready.size());
    assertEquals(3, ready.get(0).stepId);  // sorted ascending
    assertEquals(5, ready.get(1).stepId);
}

// Test employee isolation, empty results, out-of-order insertion, and the
// "stepId ordering, not insertion ordering" requirement.
@Test
public void testGetReadySteps4() {
    OnboardingManager manager = new OnboardingManager();
    manager.addEmployee(new Employee(1, "Alice", 100));
    manager.addEmployee(new Employee(2, "Bob",   101));

    // Insert out of stepId order.
    manager.addStep(new OnboardingStep(3, 1, "C", StepStatus.PENDING,     10, 103));
    manager.addStep(new OnboardingStep(1, 1, "A", StepStatus.COMPLETED,   10, 101));
    manager.addStep(new OnboardingStep(2, 1, "B", StepStatus.PENDING,     10, 102));

    // Employee 1: step 1 COMPLETED, step 2 PENDING (lower all done -> ready),
    // step 3 PENDING but blocked by step 2 -> only step 2 ready.
    List<OnboardingStep> ready = manager.getReadySteps(1);
    assertEquals(1, ready.size());
    assertEquals(2, ready.get(0).stepId);

    // Employee 2 has no steps -> empty list.
    assertTrue(manager.getReadySteps(2).isEmpty());

    // Unknown employee -> empty list.
    assertTrue(manager.getReadySteps(999).isEmpty());

    // Employee isolation: Bob's activity must not affect Alice's readiness.
    manager.addStep(new OnboardingStep(1, 2, "X", StepStatus.PENDING, 20, 101));
    manager.addStep(new OnboardingStep(2, 2, "Y", StepStatus.PENDING, 20, 102));
    List<OnboardingStep> bobReady = manager.getReadySteps(2);
    assertEquals(1, bobReady.size());
    assertEquals(1, bobReady.get(0).stepId); // only step 1; step 2 blocked by step 1

    // Alice unchanged.
    assertEquals(1, manager.getReadySteps(1).size());
    assertEquals(2, manager.getReadySteps(1).get(0).stepId);

    // Employee with all steps COMPLETED -> empty list.
    manager.addEmployee(new Employee(3, "Carol", 102));
    manager.addStep(new OnboardingStep(1, 3, "P", StepStatus.COMPLETED, 30, 101));
    manager.addStep(new OnboardingStep(2, 3, "Q", StepStatus.COMPLETED, 30, 102));
    assertTrue(manager.getReadySteps(3).isEmpty());
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