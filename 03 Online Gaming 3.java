/*
We are building the back-end for an online gaming platform. The system tracks
players and their match history. Each match result records the outcome from
that player's perspective.

Definitions:
* A "player" has: playerId, username.
* A "match result" has: playerId, opponentId, outcome, score, timestamp.
* Outcome is one of: WIN, LOSS, DRAW.
* "GameManager" manages players and match results and provides player statistics.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Feel free to run it.
1-2) The test for GameManager is not passing due to a bug in the code.
     Make the necessary changes to GameManager to fix the bug.

We are extending the platform to support recording match results
and computing per-player score statistics.

Each MatchResult represents one player's experience of a single match:
- playerId      : the player this record belongs to
- opponentId    : the opponent in that match
- outcome        : one of WIN, LOSS, DRAW
- score          : the player's score in that match
- timestamp      : when the match was played

To implement these changes, we need to add two functions to the GameManager class:

2.1) The addMatchResult function should be used to store a match result.
     Only the playerId is validated: if it does not refer to a known
     player in GameManager, the match result should be ignored. A match
     result with a known playerId must still be stored even when the
     opponentId does not refer to a known player.

2.2) The getAverageScoreByOutcome function should return a dictionary
     mapping each outcome (WIN, LOSS, DRAW) to the player's average score
     for that outcome. Only outcomes the player has at least one result
     for should appear in the dictionary. If the player has no match
     results at all, return an empty dictionary.

To assist you in testing these new functions, we have provided the
testAddMatchResult and testGetAverageScoreByOutcome tests.

We want to summarize the match history between two specific players.

For each pair of players, we want to know how each player has performed
against the other and when they last played.

We have added a HeadToHead class to represent this summary:
- winsPlayer1            : number of matches the first player won
- winsPlayer2            : number of matches the second player won
- draws                   : number of matches that ended in a draw
- totalMatches           : total number of matches played between them
- lastResult             : the outcome of the most recent match from
                            player1's perspective (null if they have
                            never played)
- lastMatchTimestamp    : timestamp of the most recent match between
                            them (null if they have never played)

Add one function to the GameManager class:

3) The getHeadToHead function takes two player IDs and returns a
   HeadToHead object summarizing all matches between them.
   If the two players have never faced each other, the function should
   return a HeadToHead with all numeric fields set to 0, lastResult
   set to null, and lastMatchTimestamp set to null.

To assist you in testing this new function, we have provided the
testGetHeadToHead test.

*/
import java.util.*;
import java.util.stream.Collectors;

enum Outcome {
    WIN, LOSS, DRAW
}

class Player {
    public int playerId;
    public String username;

    public Player(int playerId, String username) {
        this.playerId = playerId;
        this.username = username;
    }
}
class HeadToHead {
    int winsPlayer1;
    int winsPlayer2;
    int draws;
    int totalMatches;
    Outcome lastResult;
    Integer lastMatchTimestamp;

    HeadToHead(int winsPlayer1, int winsPlayer2, int draws,
               int totalMatches, Outcome lastResult, Integer lastMatchTimestamp) {
        this.winsPlayer1 = winsPlayer1;
        this.winsPlayer2 = winsPlayer2;
        this.draws = draws;
        this.totalMatches = totalMatches;
        this.lastResult = lastResult;
        this.lastMatchTimestamp = lastMatchTimestamp;
    }
}
class MatchResult {
    public int playerId;
    public int opponentId;
    public Outcome outcome;
    public int score;
    public int timestamp;

    public MatchResult(int playerId, int opponentId, Outcome outcome, int score, int timestamp) {
        this.playerId = playerId;
        this.opponentId = opponentId;
        this.outcome = outcome;
        this.score = score;
        this.timestamp = timestamp;
    }
}

class PlayerStats {
    public int totalMatches;
    public int wins;
    public double winRate;

    public PlayerStats(int totalMatches, int wins, double winRate) {
        this.totalMatches = totalMatches;
        this.wins = wins;
        this.winRate = winRate;
    }
}

class GameManager {
    public Map<Integer, Player> players = new HashMap<>();
    public List<MatchResult> matchResults = new ArrayList<>();

    public void addPlayer(Player player) {
        players.put(player.playerId, player);
    }

    public PlayerStats getPlayerStatistics(int playerId) {
        /*
            Return statistics for a single player:
            * totalMatches: total number of matches this player has played (wins, losses, and draws)
            * wins:         number of those matches the player won
            * winRate:      wins / totalMatches, expressed as a value between 0.0 and 1.0
        */
        List<MatchResult> playerMatches = new ArrayList<>();
        for (MatchResult m : matchResults) {
            if (m.playerId == playerId) {
                playerMatches.add(m);
            }
        }

        int totalMatches = 0;
        for (MatchResult m : playerMatches) {
            if (m.outcome == Outcome.WIN || m.outcome == Outcome.LOSS) {
                totalMatches++;
            }
        }

        int wins = 0;
        for (MatchResult m : playerMatches) {
            if (m.outcome == Outcome.WIN) {
                wins++;
            }
        }

        double winRate;
        if (totalMatches > 0) {
            winRate = (double) wins / totalMatches;
        } else {
            winRate = 0.0;
        }

        return new PlayerStats(totalMatches, wins, winRate);
    }
}

public class Solution {
    public static void main(String[] args) {
        testGetPlayerStatistics();
        testAddMatchResult();
        testGetAverageScoreByOutcome();
        testGetHeadToHead();
        System.out.println("All Tests Passed!");
    }

    public static void testGetPlayerStatistics() {
        System.out.println("Running testGetPlayerStatistics");
        GameManager gm = new GameManager();
        gm.addPlayer(new Player(1, "player1"));
        gm.addPlayer(new Player(2, "player2"));

        gm.matchResults.add(new MatchResult(1, 2, Outcome.WIN,  80, 1000));
        gm.matchResults.add(new MatchResult(1, 2, Outcome.LOSS, 50, 2000));
        gm.matchResults.add(new MatchResult(1, 2, Outcome.DRAW, 60, 3000));
        gm.matchResults.add(new MatchResult(1, 2, Outcome.WIN,  90, 4000));

        PlayerStats stats = gm.getPlayerStatistics(1);
        assert stats.totalMatches == 4 :
            "totalMatches should be 4, was " + stats.totalMatches;
        assert stats.wins == 2 :
            "wins should be 2, was " + stats.wins;
        assert Math.abs(stats.winRate - 0.5) < 1e-4 :
            "winRate should be 0.5, was " + stats.winRate;

        gm.matchResults.add(new MatchResult(2, 1, Outcome.DRAW, 60, 1000));
        gm.matchResults.add(new MatchResult(2, 1, Outcome.DRAW, 60, 2000));

        PlayerStats stats2 = gm.getPlayerStatistics(2);
        assert stats2.totalMatches == 2 :
            "totalMatches should be 2, was " + stats2.totalMatches;
        assert stats2.wins == 0 :
            "wins should be 0, was " + stats2.wins;
        assert Math.abs(stats2.winRate - 0.0) < 1e-4 :
            "winRate should be 0.0, was " + stats2.winRate;
    }
    
    static void testAddMatchResult() {
        System.out.println("Running testAddMatchResult");
        GameManager gm = new GameManager();
        gm.addPlayer(new Player(1, "player1"));
        gm.addPlayer(new Player(2, "player2"));

        gm.addMatchResult(new MatchResult(1, 2, Outcome.WIN, 80, 1000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.LOSS, 50, 1000));

        // unknown player ignored
        gm.addMatchResult(new MatchResult(99, 1, Outcome.WIN, 100, 2000));

        // known player, unregistered opponent -> still stored
        gm.addMatchResult(new MatchResult(1, 99, Outcome.WIN, 70, 3000));

        assert gm.matchResults.size() == 3 : "Expected 3 match results";
        assert gm.matchResults.get(0).playerId == 1 && gm.matchResults.get(1).playerId == 2 && gm.matchResults.get(2).playerId == 1;
        assert gm.matchResults.stream().anyMatch(m -> m.opponentId == 99);
    }

    static void testGetAverageScoreByOutcome() {
        System.out.println("Running testGetAverageScoreByOutcome");
        GameManager gm = new GameManager();
        gm.addPlayer(new Player(1, "player1"));
        gm.addPlayer(new Player(2, "player2"));

        // match 1 - player1 wins
        gm.addMatchResult(new MatchResult(1, 2, Outcome.WIN,  80, 1000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.LOSS, 50, 1000));

        // match 2 - player1 wins again
        gm.addMatchResult(new MatchResult(1, 2, Outcome.WIN,  91, 2000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.LOSS, 61, 2000));

        // match 3 - draw
        gm.addMatchResult(new MatchResult(1, 2, Outcome.DRAW, 70, 3000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.DRAW, 70, 3000));

        // match 4 - another draw
        gm.addMatchResult(new MatchResult(1, 2, Outcome.DRAW, 75, 4000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.DRAW, 75, 4000));

        // match 5 - player1 wins a third time
        gm.addMatchResult(new MatchResult(1, 2, Outcome.WIN,  54, 5000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.LOSS, 69, 5000));

        Map<Outcome, Double> avg1 = gm.getAverageScoreByOutcome(1);
        assert Math.abs(75.0 - avg1.get(Outcome.WIN))  < 1e-4 : "Expected 75.0 for WIN";   // (80+91+54)/3
        assert Math.abs(72.5 - avg1.get(Outcome.DRAW)) < 1e-4 : "Expected 72.5 for DRAW";  // (70+75)/2
        assert !avg1.containsKey(Outcome.LOSS) : "player1 has no losses";                   // player1 has no losses

        Map<Outcome, Double> avg2 = gm.getAverageScoreByOutcome(2);
        assert Math.abs(60.0 - avg2.get(Outcome.LOSS)) < 1e-4 : "Expected 60.0 for LOSS";  // (50+61+69)/3
        assert Math.abs(72.5 - avg2.get(Outcome.DRAW)) < 1e-4 : "Expected 72.5 for DRAW";  // (70+75)/2
        assert !avg2.containsKey(Outcome.WIN) : "player2 has no wins";                      // player2 has no wins

        // outcome with exactly one result
        gm.addPlayer(new Player(4, "player4"));
        gm.addMatchResult(new MatchResult(4, 1, Outcome.WIN, 42, 6000));
        Map<Outcome, Double> avg4 = gm.getAverageScoreByOutcome(4);
        assert Math.abs(42.0 - avg4.get(Outcome.WIN)) < 1e-4 : "Expected 42.0 for a single WIN";
        assert !avg4.containsKey(Outcome.LOSS) : "player4 has no losses";
        assert !avg4.containsKey(Outcome.DRAW) : "player4 has no draws";

        // player with no match results
        gm.addPlayer(new Player(3, "player3"));
        assert gm.getAverageScoreByOutcome(3).isEmpty() : "Expected empty map for player3";
    }
    
    static void testGetHeadToHead() {
        System.out.println("Running testGetHeadToHead");
        GameManager gm = new GameManager();
        gm.addPlayer(new Player(1, "player1"));
        gm.addPlayer(new Player(2, "player2"));
        gm.addPlayer(new Player(3, "player3"));

        // match 4 - player1 wins (most recent)
        gm.addMatchResult(new MatchResult(1, 2, Outcome.WIN,  85, 4000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.LOSS, 65, 4000));

        // match 1 - player1 wins
        gm.addMatchResult(new MatchResult(1, 2, Outcome.WIN,  80, 1000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.LOSS, 50, 1000));

        // match 2 - player2 wins
        gm.addMatchResult(new MatchResult(1, 2, Outcome.LOSS, 60, 2000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.WIN,  90, 2000));

        // match 3 - draw
        gm.addMatchResult(new MatchResult(1, 2, Outcome.DRAW, 70, 3000));
        gm.addMatchResult(new MatchResult(2, 1, Outcome.DRAW, 70, 3000));


        HeadToHead h2h = gm.getHeadToHead(1, 2);
        assert h2h.winsPlayer1 == 2 : "Expected 2";           // match 1 + match 4
        assert h2h.winsPlayer2 == 1 : "Expected 1";           // match 2 only
        assert h2h.draws == 1 : "Expected 1";                  // match 3 only
        assert h2h.totalMatches == 4 : "Expected 4";           // all 4 matches
        assert h2h.lastResult == Outcome.WIN : "Expected WIN"; // match 4 was a WIN for player1
        assert h2h.lastMatchTimestamp == 4000 : "Expected 4000";

        // from player2's perspective
        HeadToHead h2hReverse = gm.getHeadToHead(2, 1);
        assert h2hReverse.winsPlayer1 == 1 : "Expected 1";              // player2 won match 2
        assert h2hReverse.winsPlayer2 == 2 : "Expected 2";              // player1 won match 1 + 4
        assert h2hReverse.draws == 1 : "Expected 1";
        assert h2hReverse.totalMatches == 4 : "Expected 4";
        assert h2hReverse.lastResult == Outcome.LOSS : "Expected LOSS"; // match 4 was a LOSS   for player2
        assert h2hReverse.lastMatchTimestamp == 4000 : "Expected 4000";

        // players who have never faced each other — unchanged
        HeadToHead h2hEmpty = gm.getHeadToHead(1, 3);
        assert h2hEmpty.totalMatches == 0 : "Expected 0";
        assert h2hEmpty.winsPlayer1 == 0 : "Expected 0";
        assert h2hEmpty.winsPlayer2 == 0 : "Expected 0";
        assert h2hEmpty.draws == 0 : "Expected 0";
        assert h2hEmpty.lastResult == null : "Expected null";
        assert h2hEmpty.lastMatchTimestamp == null : "Expected null";
    }
}