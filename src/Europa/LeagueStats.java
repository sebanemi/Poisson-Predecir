package Europa;

public class LeagueStats {

    private int matches = 0;
    private int homeGoals = 0;
    private int awayGoals = 0;

    private int homeShotsOT = 0;
    private int awayShotsOT = 0;

    public void addMatch(int hg, int ag, int hst, int ast) {
        homeGoals += hg;
        awayGoals += ag;
        homeShotsOT += hst;
        awayShotsOT += ast;
        matches++;
    }

    public double avgHomeGoals() {
        return matches == 0 ? 0 : (double) homeGoals / matches;
    }

    public double avgAwayGoals() {
        return matches == 0 ? 0 : (double) awayGoals / matches;
    }

    public double homeGoalPerSOT() {
        return homeShotsOT == 0 ? 0.25 : (double) homeGoals / homeShotsOT;
    }

    public double awayGoalPerSOT() {
        return awayShotsOT == 0 ? 0.25 : (double) awayGoals / awayShotsOT;
    }

}