package Europa;
import java.util.*;

public class EuropaApp {

    private final String dataDir;

    public EuropaApp(String dataDir) { this.dataDir = dataDir; }
    public EuropaApp() { this.dataDir = ""; }

    public void run() {
        EuropaLoader.EuropeData data = new EuropaLoader().loadAll(dataDir);
        Scanner sc = new Scanner(System.in);

        // Promedio de goles por liga calculado dinámicamente desde los datos
        Map<String, double[]> leagueAvgs = calcularPromediosPorLiga(data);

        while (true) {

            // ── Ingreso validado local ────────────────────────────────────────
            TeamStats h = null;
            while (h == null) {
                System.out.print("\nLocal (o 'salir' para terminar): ");
                String hIn = sc.nextLine().trim();
                if (hIn.equalsIgnoreCase("salir")) return;
                for (String name : data.teams.keySet()) {
                    if (name.equalsIgnoreCase(hIn)) { h = data.teams.get(name); break; }
                }
                if (h == null) System.out.println("  Equipo no encontrado. Intentá de nuevo.");
            }

            // ── Ingreso validado visitante ─────────────────────────────────────
            TeamStats a = null;
            while (a == null) {
                System.out.print("Visitante: ");
                String aIn = sc.nextLine().trim();
                for (String name : data.teams.keySet()) {
                    if (name.equalsIgnoreCase(aIn)) { a = data.teams.get(name); break; }
                }
                if (a == null) System.out.println("  Equipo no encontrado. Intentá de nuevo.");
                else if (a == h) { System.out.println("  El visitante no puede ser igual al local."); a = null; }
            }

            clearScreen();

            // ── Ajustes de liga ───────────────────────────────────────────────
            double wH = LeagueRanking.weight(h.getLeague());
            double wA = LeagueRanking.weight(a.getLeague());
            double rawRatio = wH / wA;

            double leagueAdj_FT = Math.pow(rawRatio, 2.0);
            double leagueAdj_HT = Math.pow(rawRatio, 3.0);

            double avgWeight = (wH + wA) / 2.0;
            double capFT = 1.80 + (avgWeight - 0.70) * (2.50 - 1.80) / (1.00 - 0.70);
            double capHT = 0.80 + (avgWeight - 0.70) * (1.20 - 0.80) / (1.00 - 0.70);

            System.out.printf("\n[Liga Local: %s (%.2f) | Liga Visitante: %s (%.2f) | Ajuste FT: %.3f | Ajuste HT: %.3f]\n",
                    h.getLeague(), wH, a.getLeague(), wA, leagueAdj_FT, leagueAdj_HT);
            System.out.printf("[Cap FT: %.2f | Cap HT: %.2f]\n", capFT, capHT);

            // ── Promedios de liga dinámicos ───────────────────────────────────
            // Usar el promedio de la liga local para normalizar (o 1.35 si no hay datos)
            double[] hLeagueAvg = leagueAvgs.getOrDefault(h.getLeague(), new double[]{1.35, 1.35});
            double[] aLeagueAvg = leagueAvgs.getOrDefault(a.getLeague(), new double[]{1.35, 1.35});
            double ligaAvgH = hLeagueAvg[0]; // promedio goles local en liga del equipo H
            double ligaAvgA = aLeagueAvg[1]; // promedio goles visitante en liga del equipo A

            // ── Regresión a la media (70% stats propios + 30% promedio de liga) ──
            double hAttack  = h.avgGoalsFor(true)      * 0.70 + ligaAvgH * 0.30;
            double hDefense = h.avgGoalsConceded(true)  * 0.70 + ligaAvgA * 0.30;
            double aAttack  = a.avgGoalsFor(false)      * 0.70 + ligaAvgA * 0.30;
            double aDefense = a.avgGoalsConceded(false)  * 0.70 + ligaAvgH * 0.30;

            // ── FULL TIME lambdas ─────────────────────────────────────────────
            double lH_FT_raw = (hAttack * aDefense / ligaAvgH) * 1.03 * leagueAdj_FT;
            double lA_FT_raw = (aAttack * hDefense / ligaAvgA) / leagueAdj_FT;

            double lH_FT = Math.min(lH_FT_raw, capFT);
            double lA_FT = Math.min(lA_FT_raw, capFT);

            // ── Cap de probabilidad máxima al 75% ─────────────────────────────
            double[] probs = PoissonModel.calcular1X2(lH_FT, lA_FT);
            double maxProb = Math.max(probs[0], probs[2]);
            if (maxProb > 0.75) {
                double scale = 0.75 / maxProb;
                if (probs[0] > probs[2]) {
                    double excess = probs[0] * (1 - scale);
                    probs[0] *= scale;
                    probs[1] += excess * 0.6;
                    probs[2] += excess * 0.4;
                } else {
                    double excess = probs[2] * (1 - scale);
                    probs[2] *= scale;
                    probs[1] += excess * 0.6;
                    probs[0] += excess * 0.4;
                }
            }

            PoissonModel.printPredictorFromProbs(probs, lH_FT, lA_FT, "FULL TIME (90 MIN)");

            // ── HALF TIME ─────────────────────────────────────────────────────
            double ftRatio   = lH_FT / lA_FT;
            double ligaAvgHT = 0.60;
            double lH_HT_raw = (h.avgHTGoalsFor(true)   * a.avgHTGoalsAgainst(false) / ligaAvgHT) * leagueAdj_HT;
            double lA_HT_raw = (a.avgHTGoalsFor(false)  * h.avgHTGoalsAgainst(true)  / ligaAvgHT) / leagueAdj_HT;

            double htRatio      = lH_HT_raw / lA_HT_raw;
            double blendedRatio = ftRatio * 0.6 + htRatio * 0.4;
            double htSum        = lH_HT_raw + lA_HT_raw;
            double lH_HT = Math.min((htSum * blendedRatio) / (1 + blendedRatio), capHT);
            double lA_HT = Math.min(htSum / (1 + blendedRatio), capHT);

            PoissonModel.printPredictor(lH_HT, lA_HT, "HALF TIME (45 MIN)");

            PoissonModel.predictTotalGoals(lH_FT, lA_FT);
            PoissonModel.predictSecondary(h, a, leagueAdj_FT);

            System.out.println("\n────────────────────────────────────────");
            System.out.println("Presioná Enter para analizar otro partido...");
            sc.nextLine();
            clearScreen();
        }
    }

    /**
     * Calcula el promedio de goles local y visitante por liga
     * directamente desde los datos cargados del CSV.
     * Devuelve un mapa leagueCode -> [avgHome, avgAway]
     */
    private Map<String, double[]> calcularPromediosPorLiga(EuropaLoader.EuropeData data) {
        // Acumuladores: leagueCode -> [sumGolesLocal, sumGolesVisitante, partidos]
        Map<String, double[]> acc = new HashMap<>();

        for (TeamStats team : data.teams.values()) {
            String league = team.getLeague();
            acc.computeIfAbsent(league, k -> new double[3]);

            // Sumar goles for como local = goles que anotó como local
            // Sumar goles concedidos como local = goles que recibió como local (= goles visitante)
            acc.get(league)[0] += team.avgGoalsFor(true)      * team.homeMatchCount();
            acc.get(league)[1] += team.avgGoalsConceded(true) * team.homeMatchCount();
            acc.get(league)[2] += team.homeMatchCount();
        }

        Map<String, double[]> result = new HashMap<>();
        for (Map.Entry<String, double[]> e : acc.entrySet()) {
            double partidos = e.getValue()[2];
            if (partidos == 0) { result.put(e.getKey(), new double[]{1.35, 1.10}); continue; }
            double avgHome = e.getValue()[0] / partidos;
            double avgAway = e.getValue()[1] / partidos;
            // Sanity check: si el promedio es irreal, usar fallback
            if (avgHome < 0.5 || avgHome > 3.0) avgHome = 1.35;
            if (avgAway < 0.3 || avgAway > 2.5) avgAway = 1.10;
            result.put(e.getKey(), new double[]{avgHome, avgAway});
        }
        return result;
    }

    private void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
}