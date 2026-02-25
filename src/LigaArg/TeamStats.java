package LigaArg;

public class TeamStats {

    private int homeMatches = 0, awayMatches = 0;

    private int homeGoalsFor = 0, homeGoalsAgainst = 0;
    private int awayGoalsFor = 0, awayGoalsAgainst = 0;

    // ── Registro de partidos ──

    public void addHomeMatch(int hg, int ag) {
        homeGoalsFor     += hg;
        homeGoalsAgainst += ag;
        homeMatches++;
    }

    public void addAwayMatch(int ag, int hg) {
        awayGoalsFor     += ag;
        awayGoalsAgainst += hg;
        awayMatches++;
    }

    // ── Getters ──

    public double avgGoalsFor(boolean home) {
        int m = home ? homeMatches : awayMatches;
        return m == 0 ? 1.20 : (double)(home ? homeGoalsFor : awayGoalsFor) / m;
    }

    public double avgGoalsConceded(boolean home) {
        int m = home ? homeMatches : awayMatches;
        return m == 0 ? 1.20 : (double)(home ? homeGoalsAgainst : awayGoalsAgainst) / m;
    }

    public int getHomeMatches() { return homeMatches; }
    public int getAwayMatches() { return awayMatches; }

    // Compatibilidad con LigaArgApp viejo
    public double getHomeAverageGoalsFor()      { return avgGoalsFor(true); }
    public double getHomeAverageGoalsAgainst()  { return avgGoalsConceded(true); }
    public double getAwayAverageGoalsFor()      { return avgGoalsFor(false); }
    public double getAwayAverageGoalsAgainst()  { return avgGoalsConceded(false); }
}