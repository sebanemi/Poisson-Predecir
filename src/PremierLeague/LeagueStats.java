package PremierLeague;

public class LeagueStats {

    private int matches = 0;
    private int totalGoals = 0;

    private int homeShots = 0, awayShots = 0;
    private int homeShotsOT = 0, awayShotsOT = 0;
    private int homeCorners = 0, awayCorners = 0;

    public void addMatch(int hg, int ag,
                         int hs, int ast,
                         int hst, int astt,
                         int hc, int ac) {

        totalGoals += hg + ag;
        homeShots += hs;
        awayShots += ast;
        homeShotsOT += hst;
        awayShotsOT += astt;
        homeCorners += hc;
        awayCorners += ac;
        matches++;
    }

    public double getAverageGoalsPerTeam() {
        return matches == 0 ? 0 : (double) totalGoals / (matches * 2);
    }

    public double getAvgHomeShots() { return homeShots / (double) matches; }
    public double getAvgAwayShots() { return awayShots / (double) matches; }

    public double getAvgHomeShotsOnTarget() { return homeShotsOT / (double) matches; }
    public double getAvgAwayShotsOnTarget() { return awayShotsOT / (double) matches; }

    public double getAvgHomeCorners() { return homeCorners / (double) matches; }
    public double getAvgAwayCorners() { return awayCorners / (double) matches; }
}