/*
We are building the backend for a CRM data quality system. The system manages customer records and provides tools to validate, normalize, and deduplicate them. Each customer record has properties mentioned below.

Definitions:
* A "CustomerRecord" is one customer entry in the CRM. It has an ID, a name, an email, a phone number, a city, and a signup day (day number).
* A "DataQualityEngine" is the class used to manage all customer records and run data quality operations.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for DataQualityEngine is not passing due to a bug in the code. Make the necessary changes to DataQualityEngine to fix the bug.
*/
/*
Now that the engine can search records, staff need tools to check data quality and understand the geographic distribution of customers.

2-1) Add validateRecord() to DataQualityEngine. This function takes a record ID and checks the record against three validation rules:
       - "name": the name must be non-empty after trimming whitespace.
       - "email": the email must contain exactly one "@" character.
       - "phone": the phone must be non-empty and contain only ASCII digit characters (0-9).
     Return a list of the field names that failed validation, sorted alphabetically. Return an empty list if all fields are valid. Return null if the record does not exist.

2-2) Add getCitySummary() to DataQualityEngine. This function takes no arguments and returns a map of city name to the count of records in that city. City comparison ignores case and leading or trailing whitespace. The keys in the returned map should be lowercased and trimmed. Cities with no records do not appear. A record whose city is blank or whitespace-only normalizes to an empty string, which is included in the map under that empty-string key.

The testValidateRecord and testGetCitySummary tests are provided.
*/

/*
Now that records can be validated and summarized, the CRM team wants to track customers who have moved between cities over time. Two records represent the same customer if they share the same normalized email (lowercased, trimmed).

3) Add trackCrossCityCustomers() to DataQualityEngine. This function takes no arguments.
For each unique normalized email that has records in more than one distinct normalized city (lowercased, trimmed):

Sort the customer's records by signup day.
Walk through the sorted records and detect city transitions. A transition occurs when a record's normalized city differs from the previous record's normalized city. Consecutive records in the same city are not transitions.
Build a list of transitions as (fromCity, toCity, fromDay, toDay) where fromDay is the signup day of the last record in the previous city and toDay is the signup day of the first record in the new city.
Return a list of (email, transitions) where email is the normalized email and transitions is the list of that customer's transitions.

Sort results by number of transitions descending, then email ascending. Exclude customers with zero transitions. Return an empty list if no customers have transitions.

The testTrackCrossCityCustomers test is provided.
*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {
    /** Data about a single customer entry. */
    static class CustomerRecord {
        final int recordId;
        final String name;
        final String email;
        final String phone;
        final String city;
        final int signupDay;

        CustomerRecord(int recordId, String name, String email, String phone,
                       String city, int signupDay) {
            this.recordId = recordId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.city = city;
            this.signupDay = signupDay;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof CustomerRecord r)) {
                return false;
            }
            return recordId == r.recordId
                    && Objects.equals(name, r.name)
                    && Objects.equals(email, r.email)
                    && Objects.equals(phone, r.phone)
                    && Objects.equals(city, r.city)
                    && signupDay == r.signupDay;
        }

        @Override
        public int hashCode() {
            return Objects.hash(recordId, name, email, phone, city, signupDay);
        }

        @Override
        public String toString() {
            return "CustomerRecord " + recordId + " (name=" + name
                    + ", email=" + email + ", phone=" + phone
                    + ", city=" + city + ", signupDay=" + signupDay + ")";
        }
    }

    /**
     * Data for managing customer records, and methods staff use to validate,
     * normalize, and deduplicate them.
     */
    static class DataQualityEngine {
        final ArrayList<CustomerRecord> records = new ArrayList<>();

        /** Adds a customer record. */
        void addRecord(CustomerRecord record) {
            records.add(record);
        }

        /** Returns the record with the given ID, or null if not found. */
        CustomerRecord getRecord(int recordId) {
            for (CustomerRecord record : records) {
                if (record.recordId == recordId) {
                    return record;
                }
            }
            return null;
        }

        /** Returns the IDs of all records matching the given email, case-insensitively, sorted ascending. */
        List<Integer> findByEmail(String email) {
            List<Integer> result = new ArrayList<>();
            for (CustomerRecord record : records) {
                if (record.email.toLowerCase().equals(email)) {
                    result.add(record.recordId);
                }
            }
            Collections.sort(result);
            return result;
        }
    }

    /**
     * This is not a complete test suite, but tests some basic functionality of
     * the code and shows how to use it.
     */
    public static class TestSuite {

        @Test
        public void testCustomerRecord() {
            CustomerRecord r = new CustomerRecord(1, "Alice Smith", "alice@example.com", "5551234567", "New York", 10);
            assertEquals(1, r.recordId);
            assertEquals("Alice Smith", r.name);
            assertEquals("alice@example.com", r.email);
            assertEquals("5551234567", r.phone);
            assertEquals("New York", r.city);
            assertEquals(10, r.signupDay);
        }

        @Test
        public void testFindByEmail() {
            DataQualityEngine engine = new DataQualityEngine();
            // Record 3 inserted before record 1 to enforce sorting.
            engine.addRecord(new CustomerRecord(3, "Alice Doe", "Alice@Example.Com", "5551112222", "New York", 20));
            engine.addRecord(new CustomerRecord(1, "Alice Smith", "alice@example.com", "5551234567", "New York", 10));
            engine.addRecord(new CustomerRecord(2, "Bob Jones", "BOB@EXAMPLE.COM", "5559876543", "Chicago", 15));
            engine.addRecord(new CustomerRecord(4, "Charlie Brown", "charlie@test.com", "5553334444", "Boston", 25));

            assertEquals(List.of(1, 3), engine.findByEmail("ALICE@EXAMPLE.COM"));
            assertEquals(List.of(2), engine.findByEmail("bob@example.com"));
            assertEquals(List.of(), engine.findByEmail("nobody@test.com"));
        }
        @Test
        public void testValidateRecord() {
            DataQualityEngine engine = new DataQualityEngine();
            engine.addRecord(new CustomerRecord(1, "Alice Smith", "alice@example.com", "5551234567", "New York", 10));
            engine.addRecord(new CustomerRecord(2, "  ", "bobexample.com", "555-987-6543", "Chicago", 15));
            engine.addRecord(new CustomerRecord(3, "Charlie", "charlie@@test.com", "5551112222", "Boston", 20));
            engine.addRecord(new CustomerRecord(4, "", "valid@email.com", "abc1234567", "Denver", 25));
            engine.addRecord(new CustomerRecord(5, "Eve Adams", "eve@test.com", "", "Seattle", 30));

            assertEquals(List.of(), engine.validateRecord(1));
            assertEquals(List.of("email", "name", "phone"), engine.validateRecord(2));
            assertEquals(List.of("email"), engine.validateRecord(3));
            assertEquals(List.of("name", "phone"), engine.validateRecord(4));
            assertEquals(List.of("phone"), engine.validateRecord(5));
            assertNull(engine.validateRecord(999));
        }

        @Test
        public void testGetCitySummary() {
            DataQualityEngine engine = new DataQualityEngine();
            engine.addRecord(new CustomerRecord(1, "Alice", "a@test.com", "1234567890", "New York", 10));
            engine.addRecord(new CustomerRecord(2, "Bob", "b@test.com", "2345678901", "new york", 15));
            engine.addRecord(new CustomerRecord(3, "Charlie", "c@test.com", "3456789012", "Chicago", 20));
            engine.addRecord(new CustomerRecord(4, "Diana", "d@test.com", "4567890123", " NEW YORK ", 25));
            engine.addRecord(new CustomerRecord(5, "Eve", "e@test.com", "5678901234", "chicago", 30));
            engine.addRecord(new CustomerRecord(6, "Frank", "f@test.com", "6789012345", "Boston", 35));

            assertEquals(Map.of("new york", 3, "chicago", 2, "boston", 1), engine.getCitySummary());
            assertEquals(Map.of(), new DataQualityEngine().getCitySummary());
        }

        // Add inside the existing TestSuite class:
@Test
public void testTrackCrossCityCustomers() {
    DataQualityEngine engine = new DataQualityEngine();

    // ---- Customer alice@example.com (normalized) ----
    // Inserted out of signupDay order to force sorting.
    // History by day:  New York(1) -> New York(5) -> Chicago(7) -> Chicago(9) -> Boston(12)
    // Transitions: (new york -> chicago, fromDay=5, toDay=7)   <-- fromDay is LAST NY day, not day 1
    //              (chicago -> boston,  fromDay=9, toDay=12)
    engine.addRecord(new CustomerRecord(3, "Alice", "  ALICE@Example.com ", "1111111111", " NEW YORK ", 5));
    engine.addRecord(new CustomerRecord(1, "Alice", "alice@example.com",   "1111111111", "New York",  1));
    engine.addRecord(new CustomerRecord(5, "Alice", "alice@example.com",   "1111111111", "Boston",    12));
    engine.addRecord(new CustomerRecord(4, "Alice", "Alice@Example.COM",   "1111111111", "chicago",   9));
    engine.addRecord(new CustomerRecord(2, "Alice", "alice@example.com",   "1111111111", "CHICAGO",   7));

    // ---- Customer bob@test.com: two transitions (chicago -> denver -> chicago) ----
    // Days:  chicago(2) -> denver(6) -> chicago(10)
    engine.addRecord(new CustomerRecord(6, "Bob", "bob@test.com", "2222222222", "Chicago", 2));
    engine.addRecord(new CustomerRecord(7, "Bob", "BOB@TEST.COM", "2222222222", "Denver",  6));
    engine.addRecord(new CustomerRecord(8, "Bob", "bob@test.com", "2222222222", "CHICAGO ", 10));

    // ---- Customer carol@test.com: same signupDay, different cities -> tie-break by recordId asc ----
    // Day 3: seattle(recordId 9) then austin(recordId 10).
    // Transition: (seattle -> austin, fromDay=3, toDay=3)
    engine.addRecord(new CustomerRecord(10, "Carol", "carol@test.com", "3333333333", "Austin",  3));
    engine.addRecord(new CustomerRecord(9,  "Carol", "carol@test.com", "3333333333", "Seattle", 3));

    // ---- Customer dave@test.com: only one normalized city despite case/whitespace -> excluded ----
    engine.addRecord(new CustomerRecord(11, "Dave", "dave@test.com", "4444444444", "Boston", 1));
    engine.addRecord(new CustomerRecord(12, "Dave", "dave@test.com", "4444444444", "  boston ", 4));

    // ---- Customer eve@test.com: blank / whitespace city normalizes to "" ----
    // "" -> miami is a real transition; "" is a valid normalized city key.
    engine.addRecord(new CustomerRecord(13, "Eve", "eve@test.com", "5555555555", "   ", 2));
    engine.addRecord(new CustomerRecord(14, "Eve", "eve@test.com", "5555555555", "Miami", 8));

    List<CrossCityCustomer> result = engine.trackCrossCityCustomers();

    // Ordering: transition count desc, then email asc.
    // alice: 2, bob: 2, carol: 1, eve: 1  ->  alice < bob (same count, email asc)
    //                                         carol < eve (same count, email asc)
    assertEquals(4, result.size());
    assertEquals("alice@example.com", result.get(0).email);
    assertEquals("bob@test.com",     result.get(1).email);
    assertEquals("carol@test.com",   result.get(2).email);
    assertEquals("eve@test.com",     result.get(3).email);

    // alice: two transitions; fromDay must be the LAST day in the previous city (5, not 1).
    List<CityTransition> alice = result.get(0).transitions;
    assertEquals(2, alice.size());
    assertEquals("new york", alice.get(0).fromCity);
    assertEquals("chicago",  alice.get(0).toCity);
    assertEquals(5,  alice.get(0).fromDay);   // last NY day, NOT first
    assertEquals(7,  alice.get(0).toDay);     // first Chicago day
    assertEquals("chicago",  alice.get(1).fromCity);
    assertEquals("boston",   alice.get(1).toCity);
    assertEquals(9,  alice.get(1).fromDay);   // last Chicago day
    assertEquals(12, alice.get(1).toDay);

    // bob: chicago -> denver -> chicago (returning to a city still counts as a transition).
    List<CityTransition> bob = result.get(1).transitions;
    assertEquals(2, bob.size());
    assertEquals("chicago", bob.get(0).fromCity);
    assertEquals("denver",  bob.get(0).toCity);
    assertEquals(2,  bob.get(0).fromDay);
    assertEquals(6,  bob.get(0).toDay);
    assertEquals("denver",  bob.get(1).fromCity);
    assertEquals("chicago", bob.get(1).toCity);
    assertEquals(6,  bob.get(1).fromDay);
    assertEquals(10, bob.get(1).toDay);

    // carol: same day tie-break by recordId -> seattle (id 9) before austin (id 10).
    List<CityTransition> carol = result.get(2).transitions;
    assertEquals(1, carol.size());
    assertEquals("seattle", carol.get(0).fromCity);
    assertEquals("austin",  carol.get(0).toCity);
    assertEquals(3, carol.get(0).fromDay);
    assertEquals(3, carol.get(0).toDay);

    // eve: blank city normalizes to "" and counts as a distinct city from "miami".
    List<CityTransition> eve = result.get(3).transitions;
    assertEquals(1, eve.size());
    assertEquals("",      eve.get(0).fromCity);
    assertEquals("miami", eve.get(0).toCity);
    assertEquals(2, eve.get(0).fromDay);
    assertEquals(8, eve.get(0).toDay);

    // dave must be excluded (single normalized city).
    for (CrossCityCustomer c : result) {
        assertNotEquals("dave@test.com", c.email);
    }

    // Empty engine -> empty list (not null).
    assertTrue(new DataQualityEngine().trackCrossCityCustomers().isEmpty());
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