package LigaArg;

public class TeamStats {

    private int homeMatches = 0;
    private int awayMatches = 0;

    private int homeGoalsFor = 0;
    private int homeGoalsAgainst = 0;

    private int awayGoalsFor = 0;
    private int awayGoalsAgainst = 0;

    public void addHomeMatch(int goalsFor, int goalsAgainst) {
        homeMatches++;
        homeGoalsFor += goalsFor;
        homeGoalsAgainst += goalsAgainst;
    }

    public void addAwayMatch(int goalsFor, int goalsAgainst) {
        awayMatches++;
        awayGoalsFor += goalsFor;
        awayGoalsAgainst += goalsAgainst;
    }

    // --- Promedios ---

    public double getHomeAverageGoalsFor() {
        return homeMatches == 0 ? 0 : (double) homeGoalsFor / homeMatches;
    }

    public double getHomeAverageGoalsAgainst() {
        return homeMatches == 0 ? 0 : (double) homeGoalsAgainst / homeMatches;
    }

    public double getAwayAverageGoalsFor() {
        return awayMatches == 0 ? 0 : (double) awayGoalsFor / awayMatches;
    }

    public double getAwayAverageGoalsAgainst() {
        return awayMatches == 0 ? 0 : (double) awayGoalsAgainst / awayMatches;
    }
}