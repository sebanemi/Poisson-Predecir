package LigaArg;

public class LeagueStats {

    private int matches = 0;

    private int homeGoals = 0;
    private int awayGoals = 0;

    public void addMatch(int home, int away) {
        matches++;
        homeGoals += home;
        awayGoals += away;
    }

    public double getHomeAverageGoals() {
        return matches == 0 ? 0 : (double) homeGoals / matches;
    }

    public double getAwayAverageGoals() {
        return matches == 0 ? 0 : (double) awayGoals / matches;
    }
}