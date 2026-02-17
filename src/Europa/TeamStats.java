package Europa;

public class TeamStats {

    private final String league;

    private int homeMatches = 0;
    private int awayMatches = 0;

    private int homeGoalsFor = 0;
    private int homeGoalsAgainst = 0;
    private int awayGoalsFor = 0;
    private int awayGoalsAgainst = 0;

    private int homeShots = 0;
    private int awayShots = 0;

    private int homeShotsOnTarget = 0;
    private int awayShotsOnTarget = 0;

    private int homeCorners = 0;
    private int awayCorners = 0;

    public TeamStats(String league) {
        this.league = league;
    }

    public String getLeague() {
        return league;
    }

    // =========================
    // Carga de partidos
    // =========================
    public void addHomeMatch(int gf, int ga, int shots, int shotsOT, int corners) {
        homeGoalsFor += gf;
        homeGoalsAgainst += ga;
        homeShots += shots;
        homeShotsOnTarget += shotsOT;
        homeCorners += corners;
        homeMatches++;
    }

    public void addAwayMatch(int gf, int ga, int shots, int shotsOT, int corners) {
        awayGoalsFor += gf;
        awayGoalsAgainst += ga;
        awayShots += shots;
        awayShotsOnTarget += shotsOT;
        awayCorners += corners;
        awayMatches++;
    }

    // =========================
    // Promedios
    // =========================
    public double homeGF() {
        return homeMatches == 0 ? 0 : (double) homeGoalsFor / homeMatches;
    }

    public double homeGA() {
        return homeMatches == 0 ? 0 : (double) homeGoalsAgainst / homeMatches;
    }

    public double awayGF() {
        return awayMatches == 0 ? 0 : (double) awayGoalsFor / awayMatches;
    }

    public double awayGA() {
        return awayMatches == 0 ? 0 : (double) awayGoalsAgainst / awayMatches;
    }

    public double homeSOT() {
        return homeMatches == 0 ? 0 : (double) homeShotsOnTarget / homeMatches;
    }

    public double awaySOT() {
        return awayMatches == 0 ? 0 : (double) awayShotsOnTarget / awayMatches;
    }
}