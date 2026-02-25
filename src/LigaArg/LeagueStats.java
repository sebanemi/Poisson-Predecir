package LigaArg;

public class LeagueStats {

    private int totalHomeGoals = 0;
    private int totalAwayGoals = 0;
    private int totalMatches   = 0;

    public void addMatch(int hg, int ag) {
        totalHomeGoals += hg;
        totalAwayGoals += ag;
        totalMatches++;
    }

    public double getHomeAverageGoals() {
        return totalMatches == 0 ? 1.20 : (double) totalHomeGoals / totalMatches;
    }

    public double getAwayAverageGoals() {
        return totalMatches == 0 ? 0.90 : (double) totalAwayGoals / totalMatches;
    }

    public int getTotalMatches() { return totalMatches; }
}