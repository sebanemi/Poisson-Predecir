package PremierLeague;

public class TeamStats {

    private int homeGoalsFor = 0;
    private int homeGoalsAgainst = 0;
    private int homeMatches = 0;

    private int awayGoalsFor = 0;
    private int awayGoalsAgainst = 0;
    private int awayMatches = 0;

    // -------------------------
    // Métodos para actualizar
    // -------------------------

    public void addHomeMatch(int goalsFor, int goalsAgainst) {
        homeGoalsFor += goalsFor;
        homeGoalsAgainst += goalsAgainst;
        homeMatches++;
    }

    public void addAwayMatch(int goalsFor, int goalsAgainst) {
        awayGoalsFor += goalsFor;
        awayGoalsAgainst += goalsAgainst;
        awayMatches++;
    }

    // -------------------------
    // Promedios como local
    // -------------------------

    public double getHomeAverageGoalsFor() {
        return homeMatches == 0 ? 0 : (double) homeGoalsFor / homeMatches;
    }

    public double getHomeAverageGoalsAgainst() {
        return homeMatches == 0 ? 0 : (double) homeGoalsAgainst / homeMatches;
    }

    // -------------------------
    // Promedios como visitante
    // -------------------------

    public double getAwayAverageGoalsFor() {
        return awayMatches == 0 ? 0 : (double) awayGoalsFor / awayMatches;
    }

    public double getAwayAverageGoalsAgainst() {
        return awayMatches == 0 ? 0 : (double) awayGoalsAgainst / awayMatches;
    }

    // -------------------------
    // Promedios generales (opcional)
    // -------------------------

    public double getTotalAverageGoalsFor() {
        int totalMatches = homeMatches + awayMatches;
        return totalMatches == 0 ? 0 :
                (double) (homeGoalsFor + awayGoalsFor) / totalMatches;
    }

    public double getTotalAverageGoalsAgainst() {
        int totalMatches = homeMatches + awayMatches;
        return totalMatches == 0 ? 0 :
                (double) (homeGoalsAgainst + awayGoalsAgainst) / totalMatches;
    }
}
