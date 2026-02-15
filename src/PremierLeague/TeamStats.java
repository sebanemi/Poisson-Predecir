package PremierLeague;

public class TeamStats {

    private int homeMatches = 0, awayMatches = 0;

    private int homeGoalsFor = 0, homeGoalsAgainst = 0;
    private int awayGoalsFor = 0, awayGoalsAgainst = 0;

    private int homeShots = 0, homeShotsOnTarget = 0, homeCorners = 0;
    private int awayShots = 0, awayShotsOnTarget = 0, awayCorners = 0;

    // -------------------------
    // Actualización
    // -------------------------
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

    // -------------------------
    // Goles
    // -------------------------
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

    // -------------------------
    // Ratios normalizados vs liga
    // -------------------------
    public double getHomeShotsRatio(LeagueStats league) {
        return homeMatches == 0 ? 1 :
                (homeShots / (double) homeMatches) / league.getAvgHomeShots();
    }

    public double getHomeShotsOnTargetRatio(LeagueStats league) {
        return homeMatches == 0 ? 1 :
                (homeShotsOnTarget / (double) homeMatches) / league.getAvgHomeShotsOnTarget();
    }

    public double getHomeCornersRatio(LeagueStats league) {
        return homeMatches == 0 ? 1 :
                (homeCorners / (double) homeMatches) / league.getAvgHomeCorners();
    }

    public double getAwayShotsRatio(LeagueStats league) {
        return awayMatches == 0 ? 1 :
                (awayShots / (double) awayMatches) / league.getAvgAwayShots();
    }

    public double getAwayShotsOnTargetRatio(LeagueStats league) {
        return awayMatches == 0 ? 1 :
                (awayShotsOnTarget / (double) awayMatches) / league.getAvgAwayShotsOnTarget();
    }

    public double getAwayCornersRatio(LeagueStats league) {
        return awayMatches == 0 ? 1 :
                (awayCorners / (double) awayMatches) / league.getAvgAwayCorners();
    }
}