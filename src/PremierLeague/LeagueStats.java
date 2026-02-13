package PremierLeague;

public class LeagueStats {

    private int totalGoals = 0;
    private int totalMatches = 0;

    public void addMatch(int homeGoals, int awayGoals) {
        totalGoals += homeGoals + awayGoals;
        totalMatches++;
    }

    public double getAverageGoalsPerTeam() {
        if (totalMatches == 0) return 0;
        return (double) totalGoals / (totalMatches * 2);
    }
}