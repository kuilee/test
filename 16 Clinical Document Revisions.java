/*
We are building a system that stores the revision history of patients' clinical documents.
Every time someone edits a document, a new Revision is recorded. Each revision has a unique ID, the ID of the document it belongs to, the author who made it, and the time it was made (in minutes since the start of the day). One document can have many revisions.

Definitions:
* A "Revision" is a single recorded edit of a document. It has an ID, a document ID, an author, and a createdAt time. It also has a revertsTo field that is used in a later task.
* A "DocumentStore" is the class that manages all revisions. Revisions are added to it with addRevision, and it answers questions about them - for example, getRevisionCounts reports how many revisions each document has. You will add more methods to this class in the later tasks.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for DocumentStore is not passing because of a bug in the code. Make the necessary changes to DocumentStore to fix the bug.
*/

/*
Clinicians refer to a document's revisions by version number - the oldest revision of a document is version 1, the next-oldest is version 2, and so on, in order of when the revisions were made (by createdAt). Note that revisions are not always added in that order, so version 1 is not necessarily the first revision in the list.

Add two methods to DocumentStore:

2-1) getVersionNumber: It takes a revisionId and returns that revision's version number within its document - its position, starting at 1, when the document's revisions are ordered from oldest to newest. If there is no revision with that ID, it returns 0.

2-2) findRevisionByVersion: It takes a documentId and a versionNumber and returns the ID of the revision that is that version of the document.
If the document has no such version - the number is below 1, past the document's newest version, or the document is unknown - it returns 0.

The testGetVersionNumber and testFindRevisionByVersion tests are provided.
*/

/*
Clinicians can revert a document to one of its earlier revisions. A revert is recorded as a revision whose revertsTo holds the ID of the earlier revision being restored; a normal edit has revertsTo set to null. After a revert, the document shows the content of the revision that was restored, so the newest revision is not always the one currently in effect.

3-1) Add getCurrentRevision() to DocumentStore.

It takes a documentId and returns the ID of the revision whose content the document currently shows. Work through the document's revisions from oldest to newest (by createdAt). A normal revision makes its own content current. A revert makes the content represented by its target current; if that target is itself a revert, follow the chain until you reach the underlying normal revision. Return 0 if the document has no revisions.

Example:
  Document 800 has three revisions, added in createdAt order:
    Revision(81, 800, "dr_a", 10)
    Revision(82, 800, "dr_a", 20)
    Revision(83, 800, "dr_a", 30, revertsTo=81)
  getCurrentRevision(800) -> 81
  Explanation: in createdAt order the revisions are 81, 82, then 83. Revision 83 reverts to 81, so the document shows 81 again — even though 83 is the newest revision.

To assist you in testing this new function, we have provided the testGetCurrentRevision tests.
*/

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;
import java.util.stream.Collectors;

public class Solution {

    /** A single recorded edit of a document. */
    static class Revision {
        final int revisionId;
        final int documentId;
        final String author;
        final int createdAt;        // minutes since the start of the day
        final Integer revertsTo;    // revision ID this reverts to, or null
        final Integer parentId;     // revision this one was edited from, or null

        Revision(int revisionId, int documentId, String author, int createdAt) {
            this(revisionId, documentId, author, createdAt, null, null);
        }

        Revision(int revisionId, int documentId, String author, int createdAt,
                 Integer revertsTo, Integer parentId) {
            this.revisionId = revisionId;
            this.documentId = documentId;
            this.author = author;
            this.createdAt = createdAt;
            this.revertsTo = revertsTo;
            this.parentId = parentId;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Revision r)) {
                return false;
            }
            return revisionId == r.revisionId
                    && documentId == r.documentId
                    && Objects.equals(author, r.author)
                    && createdAt == r.createdAt
                    && Objects.equals(revertsTo, r.revertsTo)
                    && Objects.equals(parentId, r.parentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(revisionId, documentId, author, createdAt, revertsTo, parentId);
        }

        @Override
        public String toString() {
            return "Revision " + revisionId + " (doc " + documentId + ") by " + author + " at " + createdAt;
        }
    }

    /** Holds all revisions and answers questions about them. */
    static class DocumentStore {
        private final ArrayList<Revision> revisions = new ArrayList<>();

        /** Records a new revision. */
        void addRevision(Revision revision) {
            revisions.add(revision);
        }

        /** Returns a map from each document ID to the number of revisions it has. */
        Map<Integer, Integer> getRevisionCounts() {
            // First, collect the distinct document IDs, in the order they are first seen.
            List<Integer> documentIds = new ArrayList<>();
            for (Revision revision : revisions) {
                if (!documentIds.contains(revision.documentId)) {
                    documentIds.add(revision.documentId);
                }
            }

            // Then count how many revisions belong to each document.
            Map<Integer, Integer> counts = new HashMap<>();
            int count = 0;
            for (int documentId : documentIds) {
                for (Revision revision : revisions) {
                    if (revision.documentId == documentId) {
                        count = count + 1;
                    }
                }
                counts.put(documentId, count);
            }
            return counts;
        }
    }


    public static class TestSuite {

        @Test
        public void testRevision() {
            Revision r = new Revision(1, 100, "dr_a", 10);
            assertEquals(1, r.revisionId);
            assertEquals(100, r.documentId);
            assertEquals("dr_a", r.author);
            assertEquals(10, r.createdAt);
        }

        @Test
        public void testGetRevisionCounts() {
            // Empty store -> no counts.
            DocumentStore store = new DocumentStore();
            assertEquals(new HashMap<>(), store.getRevisionCounts());

            // A single document with a single revision.
            store = new DocumentStore();
            store.addRevision(new Revision(1, 100, "dr_a", 10));
            Map<Integer, Integer> expectedOne = new HashMap<>();
            expectedOne.put(100, 1);
            assertEquals(expectedOne, store.getRevisionCounts());

            // Several documents whose revisions are interleaved in the list.
            store = new DocumentStore();
            store.addRevision(new Revision(1, 100, "dr_a", 10));
            store.addRevision(new Revision(2, 200, "dr_b", 12));
            store.addRevision(new Revision(3, 100, "dr_a", 15));
            store.addRevision(new Revision(4, 300, "dr_c", 18));
            store.addRevision(new Revision(5, 200, "dr_b", 20));
            store.addRevision(new Revision(6, 200, "dr_a", 25));
            Map<Integer, Integer> expectedMany = new HashMap<>();
            expectedMany.put(100, 2);
            expectedMany.put(200, 3);
            expectedMany.put(300, 1);
            assertEquals(expectedMany, store.getRevisionCounts());
        }
        
        // Add these two methods inside the TestSuite class.
        @Test
        public void testGetVersionNumber() {
            DocumentStore store = new DocumentStore();
            // Document 100: five revisions; earliest (createdAt=10) is inserted last.
            store.addRevision(new Revision(3, 100, "dr_a", 30));
            store.addRevision(new Revision(5, 100, "dr_a", 20));
            store.addRevision(new Revision(8, 100, "dr_a", 50));
            store.addRevision(new Revision(9, 100, "dr_a", 40));
            store.addRevision(new Revision(1, 100, "dr_a", 10));  // earliest, inserted last
            store.addRevision(new Revision(2, 200, "dr_b", 5));
            store.addRevision(new Revision(4, 200, "dr_b", 8));
            store.addRevision(new Revision(6, 300, "dr_c", 100));
            // Revision 7 shares createdAt=5 with document 200's revision 2, in a different document.
            store.addRevision(new Revision(7, 300, "dr_c", 5));

            // Version follows createdAt order, not the order revisions were added.
            // Doc 100 by createdAt: 1@10, 5@20, 3@30, 9@40, 8@50 -> versions 1..5.
            assertEquals(1, store.getVersionNumber(1));
            assertEquals(2, store.getVersionNumber(5));
            assertEquals(3, store.getVersionNumber(3));
            assertEquals(4, store.getVersionNumber(9));
            assertEquals(5, store.getVersionNumber(8));
            assertEquals(1, store.getVersionNumber(2));
            assertEquals(2, store.getVersionNumber(4));
            assertEquals(2, store.getVersionNumber(6));   // now 2nd in document 300, after revision 7
            // Same createdAt as document 200's revision 2, but scoped to document 300 -> version 1.
            assertEquals(1, store.getVersionNumber(7));
            // Unknown revision -> 0.
            assertEquals(0, store.getVersionNumber(999));
        }

        @Test
        public void testFindRevisionByVersion() {
            DocumentStore store = new DocumentStore();
            // Document 100: five revisions; earliest (createdAt=10) is inserted last.
            store.addRevision(new Revision(3, 100, "dr_a", 30));
            store.addRevision(new Revision(5, 100, "dr_a", 20));
            store.addRevision(new Revision(8, 100, "dr_a", 50));
            store.addRevision(new Revision(9, 100, "dr_a", 40));
            store.addRevision(new Revision(1, 100, "dr_a", 10));  // earliest, inserted last
            store.addRevision(new Revision(2, 200, "dr_b", 5));
            store.addRevision(new Revision(4, 200, "dr_b", 8));
            store.addRevision(new Revision(7, 400, "dr_d", 25));

            // Versions follow createdAt order.
            assertEquals(1, store.findRevisionByVersion(100, 1));
            assertEquals(5, store.findRevisionByVersion(100, 2));
            assertEquals(3, store.findRevisionByVersion(100, 3));
            assertEquals(9, store.findRevisionByVersion(100, 4));
            assertEquals(8, store.findRevisionByVersion(100, 5));
            assertEquals(4, store.findRevisionByVersion(200, 2));
            // A single-revision document's only revision is version 1.
            assertEquals(7, store.findRevisionByVersion(400, 1));
            // Below 1 (including negative), past the last version, and unknown document -> 0.
            assertEquals(0, store.findRevisionByVersion(100, 0));
            assertEquals(0, store.findRevisionByVersion(100, -1));
            assertEquals(0, store.findRevisionByVersion(100, 6));
            assertEquals(0, store.findRevisionByVersion(999, 1));
        }
        // Add inside the existing TestSuite class:

// Test the worked example: a revert after a normal edit makes the earlier target
// current, even though the revert itself is the newest revision.
@Test
public void testGetCurrentRevision1() {
    DocumentStore store = new DocumentStore();
    store.addRevision(new Revision(81, 800, "dr_a", 10));
    store.addRevision(new Revision(82, 800, "dr_a", 20));
    store.addRevision(new Revision(83, 800, "dr_a", 30, 81, null)); // reverts to 81

    assertEquals(81, store.getCurrentRevision(800));

    // A subsequent normal edit becomes current again.
    store.addRevision(new Revision(84, 800, "dr_a", 40));
    assertEquals(84, store.getCurrentRevision(800));
}

// Test that reverts are processed in createdAt order, not insertion order, and that a
// revert target can itself be a revert (chain following).
@Test
public void testGetCurrentRevision2() {
    DocumentStore store = new DocumentStore();

    // Inserted out of createdAt order. Chronological:
    //   1 @10 normal
    //   2 @20 normal
    //   3 @30 reverts to 1   -> current 1
    //   4 @40 reverts to 3   -> chain: 3 -> 1, so current 1
    //   5 @50 normal         -> current 5
    store.addRevision(new Revision(5, 900, "dr_a", 50));
    store.addRevision(new Revision(4, 900, "dr_a", 40, 3, null));
    store.addRevision(new Revision(2, 900, "dr_a", 20));
    store.addRevision(new Revision(3, 900, "dr_a", 30, 1, null));
    store.addRevision(new Revision(1, 900, "dr_a", 10));

    assertEquals(5, store.getCurrentRevision(900));

    // Reverting the newest normal edit back to the chain resolves to the base normal rev.
    store.addRevision(new Revision(6, 900, "dr_a", 60, 4, null)); // 4 -> 3 -> 1
    assertEquals(1, store.getCurrentRevision(900));
}

// Test that a revert is only "undone" by a later normal edit or a later revert, and
// that reverts to a normal revision in the middle of the history work correctly.
@Test
public void testGetCurrentRevision3() {
    DocumentStore store = new DocumentStore();

    // Chronological:
    //   1 @10 normal
    //   2 @20 normal
    //   3 @30 normal
    //   4 @40 reverts to 2   -> current 2
    //   5 @50 normal         -> current 5
    //   6 @60 reverts to 4   -> chain 4 -> 2, current 2
    store.addRevision(new Revision(1, 910, "dr_a", 10));
    store.addRevision(new Revision(2, 910, "dr_a", 20));
    store.addRevision(new Revision(3, 910, "dr_a", 30));
    store.addRevision(new Revision(4, 910, "dr_a", 40, 2, null));
    store.addRevision(new Revision(5, 910, "dr_a", 50));
    store.addRevision(new Revision(6, 910, "dr_a", 60, 4, null));

    assertEquals(2, store.getCurrentRevision(910));

    // Reverting to a normal revision mid-history makes exactly that one current.
    store.addRevision(new Revision(7, 910, "dr_a", 70, 3, null));
    assertEquals(3, store.getCurrentRevision(910));
}

// Test empty/unknown documents, single-revision documents, and document isolation.
@Test
public void testGetCurrentRevision4() {
    DocumentStore empty = new DocumentStore();
    // Unknown document -> 0.
    assertEquals(0, empty.getCurrentRevision(999));

    DocumentStore store = new DocumentStore();

    // Single normal revision -> itself.
    store.addRevision(new Revision(1, 920, "dr_a", 10));
    assertEquals(1, store.getCurrentRevision(920));

    // Document isolation: reverts in document 930 must not affect document 920.
    store.addRevision(new Revision(2, 920, "dr_a", 20));
    store.addRevision(new Revision(10, 930, "dr_b", 10));
    store.addRevision(new Revision(11, 930, "dr_b", 20));
    store.addRevision(new Revision(12, 930, "dr_b", 30, 10, null)); // doc 930 reverts to 10
    assertEquals(2, store.getCurrentRevision(920)); // unaffected
    assertEquals(10, store.getCurrentRevision(930));

    // A document whose only revision is a revert to a normal revision of another
    // document still resolves the chain by revision ID (IDs are globally unique).
    store.addRevision(new Revision(13, 940, "dr_c", 10, 10, null)); // reverts to doc 930's rev 10
    assertEquals(10, store.getCurrentRevision(940));
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
