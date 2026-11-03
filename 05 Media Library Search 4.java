/*
Our media streaming product shows a catalog dashboard that breaks down how many titles of each type the library holds - movies vs. shows vs. podcasts vs. songs. This powers the 'Browse by type' tiles and internal catalog reports.

Definitions:
* A "media item" is one entry in the catalog. It has an item id, a title, a media type, tags, a view count, and a rating.
* A media type is one of MOVIE, SHOW, PODCAST, or SONG.
* A "MediaIndex" stores catalog items and answers catalog queries.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for MediaIndex is not passing due to a bug in the code. Make the necessary changes to MediaIndex to fix the bug.
*/

/*
The catalog dashboard gives editors two at-a-glance signals about the library. First, a per-title popularity badge, so an editor scanning a title immediately sees how watched it is. Second, an average rating per media type, so they can tell where the library is strong and where it needs investment.

2-1) Add popularityTier(itemId) to MediaIndex. Given an item's id, return its popularity tier as a string, based on that item's lifetime viewCount: HOT for 1000 or more, POPULAR for 100 or more (but under 1000), and NICHE for under 100. For example, a title with 5000 views is HOT, one with 500 is POPULAR, and one with 10 is NICHE.

2-2) Add averageRatingByType() to MediaIndex. Return a Map from each media type to the average rating (a floating-point number) of the items of that type. A library with two movies rated 4.0 and 2.0 and one show rated 5.0 returns {"MOVIE": 3.0, "SHOW": 5.0}. A media type with no items simply does not appear in the result.
*/

/*
The browse page shows a Trending tags rail - the tags that appear on the most titles across the whole catalog - so users can jump straight into the genres and themes the library leans into. Tags are free-text, and editors are inconsistent about capitalisation, so the same theme shows up as Action, action, or ACTION on different titles.

3-1) Add topTags(n) to MediaIndex. It returns the n most common tags across the catalog, as a List of tag strings ordered by how many titles carry the tag, descending. Counting is case-insensitive - Action, action, and ACTION are the same tag and count together - and the returned tags are lower-cased. Ties are broken alphabetically (ascending).

Worked example - over a catalog whose titles carry the tags ["Action", "Thriller"], ["action", "drama", "Comedy"], and ["ACTION", "Thriller"]: the tag action appears on all three titles (count 3), thriller on two, and comedy and drama once each. So for n = 2 the result is ["action", "thriller"] - action first by count, then thriller. comedy precedes drama on the alphabetical tie-break.

Counterexample: if you count tags without normalising case first, Action, action, and ACTION become three different tags of count 1 each, and a genuinely less popular tag can outrank the real leader. Fold case before counting, not after.
*/

/*
This is the core of catalog search. When a user types several keywords into the search box - say action 2020 - they expect results that match every keyword, ordered so the most-watched titles appear first.

4-1) Add search(terms) to MediaIndex. Given a list of query terms, it returns a List of item ids for the items whose tags contain all of the terms. Matching is case-insensitive (a tag "Action" matches the term "action"). Results are ranked by viewCount descending; ties are broken by item id ascending.

Worked example - searching ["action", "2020"] over:
- Item 1 - tags ["action", "2020"], 100 views - matches (has both terms).
- Item 2 - tags ["action"], 999 views - excluded: it has only one of the two terms, even though it is the most-watched.
- Item 3 - tags ["action", "2020"], 100 views - matches.

The result is [1, 3]: items 1 and 3 both match all terms and tie on views (100), so they are ordered by id ascending.
*/

import java.util.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

public class Solution {

    enum MediaType {
        MOVIE, SHOW, PODCAST, SONG
    }

    /** One entry in the media catalog. */
    static class MediaItem {
        final int itemId;            // unique integer id
        final String title;          // display title
        final MediaType mediaType;   // the kind of entry
        final List<String> tags;     // free-text keyword tags, e.g. "action"
        final int viewCount;         // lifetime number of plays/views
        final double rating;         // average rating, 0.0-5.0

        MediaItem(int itemId, String title, MediaType mediaType, List<String> tags,
                  int viewCount, double rating) {
            this.itemId = itemId;
            this.title = title;
            this.mediaType = mediaType;
            this.tags = tags;
            this.viewCount = viewCount;
            this.rating = rating;
        }
    }

    /**
     * In-memory catalog index. Stores MediaItems and answers catalog queries
     * for the product's browse and search screens.
     */
    static class MediaIndex {
        private final List<MediaItem> items = new ArrayList<>();

        void addItem(MediaItem item) {
            items.add(item);
        }

        Map<String, Integer> countByType() {
            Map<String, Integer> counts = new HashMap<>();
            for (MediaItem it : items) {
                counts.put(it.mediaType.name(), 1);
            }
            return counts;
        }
    }

    public static class TestSuite {

        @Test
        public void testCountByType() {
            MediaIndex index = new MediaIndex();
            index.addItem(new MediaItem(1, "Heat", MediaType.MOVIE, new ArrayList<>(), 0, 0.0));
            index.addItem(new MediaItem(2, "The Wire", MediaType.SHOW, new ArrayList<>(), 0, 0.0));
            index.addItem(new MediaItem(3, "The Sopranos", MediaType.SHOW, new ArrayList<>(), 0, 0.0));
            index.addItem(new MediaItem(4, "Serial", MediaType.PODCAST, new ArrayList<>(), 0, 0.0));
            index.addItem(new MediaItem(5, "Reply All", MediaType.PODCAST, new ArrayList<>(), 0, 0.0));
            index.addItem(new MediaItem(6, "Heavyweight", MediaType.PODCAST, new ArrayList<>(), 0, 0.0));
            index.addItem(new MediaItem(7, "Blue in Green", MediaType.SONG, new ArrayList<>(), 0, 0.0));

            Map<String, Integer> expected = new HashMap<>();
            expected.put("MOVIE", 1);
            expected.put("SHOW", 2);
            expected.put("PODCAST", 3);
            expected.put("SONG", 1);
            assertEquals(expected, index.countByType());

            MediaIndex movies = new MediaIndex();
            movies.addItem(new MediaItem(8, "Drive", MediaType.MOVIE, new ArrayList<>(), 0, 0.0));
            movies.addItem(new MediaItem(9, "Collateral", MediaType.MOVIE, new ArrayList<>(), 0, 0.0));
            movies.addItem(new MediaItem(10, "Arrival", MediaType.MOVIE, new ArrayList<>(), 0, 0.0));
            movies.addItem(new MediaItem(11, "Inception", MediaType.MOVIE, new ArrayList<>(), 0, 0.0));
            Map<String, Integer> moviesExpected = new HashMap<>();
            moviesExpected.put("MOVIE", 4);
            assertEquals(moviesExpected, movies.countByType());

            assertEquals(new HashMap<String, Integer>(), new MediaIndex().countByType());
        }
        
        @Test
        public void testPopularityTier() {
            MediaIndex index = new MediaIndex();
            index.addItem(new MediaItem(21, "Blockbuster", MediaType.MOVIE, new ArrayList<>(), 5000, 0.0));
            index.addItem(new MediaItem(36, "Midlist", MediaType.MOVIE, new ArrayList<>(), 500, 0.0));
            index.addItem(new MediaItem(41, "Deep Cut", MediaType.SHOW, new ArrayList<>(), 10, 0.0));
            index.addItem(new MediaItem(58, "Edge Hot", MediaType.MOVIE, new ArrayList<>(), 1000, 0.0));
            index.addItem(new MediaItem(72, "Edge Pop", MediaType.SHOW, new ArrayList<>(), 100, 0.0));
            assertEquals("HOT", index.popularityTier(21));
            assertEquals("POPULAR", index.popularityTier(36));
            assertEquals("NICHE", index.popularityTier(41));
            assertEquals("HOT", index.popularityTier(58));
            assertEquals("POPULAR", index.popularityTier(72));
        }

        @Test
        public void testAverageRatingByType() {
            MediaIndex index = new MediaIndex();
            index.addItem(new MediaItem(1, "Heat", MediaType.MOVIE, new ArrayList<>(), 0, 4.0));
            index.addItem(new MediaItem(2, "Drive", MediaType.MOVIE, new ArrayList<>(), 0, 2.0));
            index.addItem(new MediaItem(3, "The Wire", MediaType.SHOW, new ArrayList<>(), 0, 5.0));
            Map<String, Double> expected = new HashMap<>();
            expected.put("MOVIE", 3.0);
            expected.put("SHOW", 5.0);
            assertEquals(expected, index.averageRatingByType());
            assertEquals(new HashMap<>(), new MediaIndex().averageRatingByType());
        }
        
        @Test
        public void testTopTags() {
            MediaIndex index = new MediaIndex();
            index.addItem(new MediaItem(1, "Heat", MediaType.MOVIE, Arrays.asList("Action", "Thriller"), 0, 0.0));
            index.addItem(new MediaItem(2, "Drive", MediaType.MOVIE, Arrays.asList("action", "drama", "Comedy"), 0, 0.0));
            index.addItem(new MediaItem(3, "Collateral", MediaType.SHOW, Arrays.asList("ACTION", "Thriller"), 0, 0.0));
            assertEquals(Arrays.asList("action", "thriller"), index.topTags(2));
            assertEquals(Arrays.asList("action", "thriller", "comedy", "drama"), index.topTags(10));
            index.addItem(new MediaItem(4, "The Raid", MediaType.MOVIE, Arrays.asList("Drama", "drama", "DRAMA"), 0, 0.0));
            assertEquals(Arrays.asList("action", "drama"), index.topTags(2));
        }
        
        @Test
        public void testSearch() {
            MediaIndex index = new MediaIndex();
            index.addItem(new MediaItem(1, "Heat", MediaType.MOVIE, Arrays.asList("Action", "2020"), 100, 0.0));
            index.addItem(new MediaItem(2, "Drive", MediaType.MOVIE, Arrays.asList("action"), 999, 0.0));
            index.addItem(new MediaItem(3, "Collateral", MediaType.MOVIE, Arrays.asList("action", "2020"), 100, 0.0));
            assertEquals(Arrays.asList(1, 3), index.search(Arrays.asList("action", "2020")));
            assertEquals(Arrays.asList(2, 1, 3), index.search(Arrays.asList("action")));
            assertEquals(new ArrayList<Integer>(), index.search(Arrays.asList("drama")));
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
