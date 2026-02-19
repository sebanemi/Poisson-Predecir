package Europa;
import java.util.Scanner;

public class EuropaApp {
    public void run() {
        EuropaLoader.EuropeData data = new EuropaLoader().loadAll();
        Scanner sc = new Scanner(System.in);

        System.out.print("Local: "); String hIn = sc.nextLine();
        System.out.print("Visitante: "); String aIn = sc.nextLine();

        TeamStats h = null, a = null;
        for (String name : data.teams.keySet()) {
            if (name.equalsIgnoreCase(hIn)) h = data.teams.get(name);
            if (name.equalsIgnoreCase(aIn)) a = data.teams.get(name);
        }

        if (h == null || a == null) {
            System.out.println("Error: uno o ambos equipos no fueron encontrados en los datos.");
            return;
        }

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

        // ── FULL TIME ──
        double ligaAvg = 1.35;
        double lH_FT = Math.min((h.avgGoalsFor(true) * a.avgGoalsConceded(false) / ligaAvg) * 1.08 * leagueAdj_FT, capFT);
        double lA_FT = Math.min((a.avgGoalsFor(false) * h.avgGoalsConceded(true) / ligaAvg) / leagueAdj_FT, capFT);

        PoissonModel.printPredictor(lH_FT, lA_FT, "FULL TIME (90 MIN)");

        // ── HALF TIME ──
        // El HT debe ser consistente con el FT: si en FT local es favorito, en HT también debe serlo.
        // Forzamos coherencia escalando el HT por el mismo ratio relativo que el FT.
        double ftRatio = lH_FT / lA_FT; // ratio de favorito en FT
        double ligaAvgHT = 0.60;
        double lH_HT_raw = (h.avgHTGoalsFor(true) * a.avgHTGoalsAgainst(false) / ligaAvgHT) * leagueAdj_HT;
        double lA_HT_raw = (a.avgHTGoalsFor(false) * h.avgHTGoalsAgainst(true) / ligaAvgHT) / leagueAdj_HT;

        // Si el ratio HT contradice al FT, empujamos suavemente hacia coherencia
        double htRatio = lH_HT_raw / lA_HT_raw;
        double blendedRatio = (ftRatio * 0.6 + htRatio * 0.4); // 60% FT, 40% HT propio
        double htSum = lH_HT_raw + lA_HT_raw;
        double lH_HT = Math.min((htSum * blendedRatio) / (1 + blendedRatio), capHT);
        double lA_HT = Math.min(htSum / (1 + blendedRatio), capHT);

        PoissonModel.printPredictor(lH_HT, lA_HT, "HALF TIME (45 MIN)");

        PoissonModel.predictTotalGoals(lH_FT, lA_FT);
        PoissonModel.predictSecondary(h, a, leagueAdj_FT);
    }
}