package Europa;

import java.util.ArrayDeque;
import java.util.Deque;

public class TeamStats {

    private final String league;
    private int homeMatches = 0;
    private int awayMatches = 0;
    private int homeGoalsFor = 0;
    private int awayGoalsFor = 0;

    // =========================
    // Historial últimos partidos
    // =========================
    private static class Match {
        int goalsFor;
        int shots;
        int shotsOT;

        Match(int gf, int shots, int sot) {
            this.goalsFor = gf;
            this.shots = shots;
            this.shotsOT = sot;
        }
    }

    private final Deque<Match> lastHomeMatches = new ArrayDeque<>();
    private final Deque<Match> lastAwayMatches = new ArrayDeque<>();

    private static final int MAX_LAST = 5;

    public TeamStats(String league) {
        this.league = league;
    }

    public String getLeague() {
        return league;
    }

    // =========================
    // Carga de partidos
    // =========================
    public void addHomeMatch(int gf, int ga, int shots, int shotsOT) {
        homeGoalsFor += gf;
        homeMatches++;
        pushMatch(lastHomeMatches, new Match(gf, shots, shotsOT));
    }

    public void addAwayMatch(int gf, int ga, int shots, int shotsOT) {
        awayGoalsFor += gf;
        awayMatches++;
        pushMatch(lastAwayMatches, new Match(gf, shots, shotsOT));
    }

    public double avgGoalsFor(boolean home) {
        if (home) {
            return homeMatches == 0 ? 0.05 : (double) homeGoalsFor / homeMatches;
        } else {
            return awayMatches == 0 ? 0.05 : (double) awayGoalsFor / awayMatches;
        }
    }

    private void pushMatch(Deque<Match> list, Match m) {
        if (list.size() == MAX_LAST) {
            list.removeFirst(); // saca el más viejo
        }
        list.addLast(m); // agrega el más reciente
    }

    // =========================
    // MODELO B – Momentum últimos 5
    // =========================
    public double weightedGoalsForLast5(boolean home) {
        Deque<Match> matches = home ? lastHomeMatches : lastAwayMatches;

        if (matches.isEmpty()) return 0.05;

        double weight = 1.0;
        double totalWeight = 0;
        double sum = 0;

        // Más peso a los más recientes
        for (Match m : matches) {
            sum += m.goalsFor * weight;
            totalWeight += weight;
            weight += 0.5;
        }

        return sum / totalWeight;
    }

    // =========================
    // MODELO C – xG proxy (tiros + SOT)
    // =========================
    public double xGProxy(boolean home, String league) {
        Deque<Match> matches = home ? lastHomeMatches : lastAwayMatches;

        if (matches.isEmpty()) return 0.05;

        double weight = 1.0;
        double totalWeight = 0;
        double sum = 0;

        for (Match m : matches) {
            double accuracy = m.shots == 0 ? 0 : (double) m.shotsOT / m.shots;

            double xg =
                    (0.05 * m.shots) +
                            (0.30 * m.shotsOT) +
                            (0.50 * accuracy);

            sum += xg * weight;
            totalWeight += weight;
            weight += 0.5;
        }

        return sum / totalWeight;
    }
}