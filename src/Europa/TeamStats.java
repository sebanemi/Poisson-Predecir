package Europa;

import java.util.ArrayList;
import java.util.List;

public class TeamStats {
    private final String league;

    // Guardamos cada partido para poder calcular promedios ponderados
    private final List<double[]> homeMatches = new ArrayList<>(); // [gf, ga, sot_f, sot_a, htgf, htga, y, r]
    private final List<double[]> awayMatches = new ArrayList<>();

    // Pesos de decaimiento exponencial: partidos recientes valen más
    // Los últimos 5 partidos tienen peso 1.5, los anteriores decaen con factor 0.85
    private static final double RECENT_WEIGHT  = 1.5;  // peso extra para últimos 5
    private static final double DECAY_FACTOR   = 0.85; // decaimiento por partido hacia atrás

    public TeamStats(String league) { this.league = league; }
    public String getLeague() { return league; }

    public void addHomeMatch(int hg, int ag, int hst, int ast,
                             int hthg, int htag, int hy, int hr) {
        homeMatches.add(new double[]{hg, ag, hst, ast, hthg, htag, hy, hr});
    }

    public void addAwayMatch(int myGoals, int theirGoals, int mySot, int theirSot,
                             int myHtGoals, int theirHtGoals, int myYellows, int myReds) {
        awayMatches.add(new double[]{myGoals, theirGoals, mySot, theirSot,
                myHtGoals, theirHtGoals, myYellows, myReds});
    }

    // ── Promedios ponderados por tiempo ───────────────────────────────────────

    /**
     * Calcula el promedio ponderado de un campo de los partidos.
     * Los partidos más recientes (al final de la lista) tienen mayor peso.
     * @param matches lista de partidos
     * @param field   índice del campo en el array
     * @param fallback valor si no hay datos
     * @param onlyIfNonZero si true, ignora filas donde field == 0 (para SOT)
     */
    private double weightedAvg(List<double[]> matches, int field,
                               double fallback, boolean onlyIfNonZero) {
        if (matches.isEmpty()) return fallback;

        double sumWeighted = 0, sumWeights = 0;
        int n = matches.size();

        for (int i = 0; i < n; i++) {
            double val = matches.get(i)[field];
            if (onlyIfNonZero && val == 0 &&
                    matches.get(i)[2] == 0 && matches.get(i)[3] == 0) continue; // sin datos SOT

            // Posición desde el final (0 = más reciente)
            int fromEnd = n - 1 - i;

            double weight;
            if (fromEnd < 5) {
                // Últimos 5 partidos: peso extra
                weight = RECENT_WEIGHT * Math.pow(DECAY_FACTOR, fromEnd);
            } else {
                // Partidos anteriores: decaimiento normal desde el umbral
                weight = RECENT_WEIGHT * Math.pow(DECAY_FACTOR, 4) *
                        Math.pow(DECAY_FACTOR, fromEnd - 4);
            }

            sumWeighted += val * weight;
            sumWeights  += weight;
        }

        return sumWeights == 0 ? fallback : sumWeighted / sumWeights;
    }

    // ── Goles Full Time ──
    public double avgGoalsFor(boolean home) {
        return weightedAvg(home ? homeMatches : awayMatches, 0, 1.35, false);
    }

    public double avgGoalsConceded(boolean home) {
        return weightedAvg(home ? homeMatches : awayMatches, 1, 1.35, false);
    }

    // ── Tiros al arco ──
    public double avgSOT_For(boolean home) {
        List<double[]> matches = home ? homeMatches : awayMatches;
        // Filtrar solo partidos con datos de SOT (sot_for + sot_against > 0)
        List<double[]> withSOT = new ArrayList<>();
        for (double[] m : matches) {
            if (m[2] > 0 || m[3] > 0) withSOT.add(m);
        }
        return weightedAvg(withSOT, 2, 4.5, false);
    }

    public double avgSOT_Against(boolean home) {
        List<double[]> matches = home ? homeMatches : awayMatches;
        List<double[]> withSOT = new ArrayList<>();
        for (double[] m : matches) {
            if (m[2] > 0 || m[3] > 0) withSOT.add(m);
        }
        return weightedAvg(withSOT, 3, 4.5, false);
    }

    // ── Half Time ──
    public double avgHTGoalsFor(boolean home) {
        return weightedAvg(home ? homeMatches : awayMatches, 4, 0.6, false);
    }

    public double avgHTGoalsAgainst(boolean home) {
        return weightedAvg(home ? homeMatches : awayMatches, 5, 0.6, false);
    }

    // ── Tarjetas ──
    public double avgYellows() {
        double h = weightedAvg(homeMatches, 6, 2.0, false);
        double a = weightedAvg(awayMatches, 6, 2.0, false);
        int total = homeMatches.size() + awayMatches.size();
        if (total == 0) return 2.0;
        // Promedio ponderado por cantidad de partidos de cada rol
        return (h * homeMatches.size() + a * awayMatches.size()) / total;
    }

    public double avgReds() {
        double h = weightedAvg(homeMatches, 7, 0.05, false);
        double a = weightedAvg(awayMatches, 7, 0.05, false);
        int total = homeMatches.size() + awayMatches.size();
        if (total == 0) return 0.05;
        return (h * homeMatches.size() + a * awayMatches.size()) / total;
    }

    public int homeMatchCount() { return homeMatches.size(); }
    public int awayMatchCount() { return awayMatches.size(); }
}