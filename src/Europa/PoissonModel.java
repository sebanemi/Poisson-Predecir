package Europa;

public class PoissonModel {

    public static double poisson(int k, double lambda) {
        if (lambda <= 0) return k == 0 ? 1 : 0;
        return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
    }

    private static double factorial(int k) {
        double r = 1;
        for (int i = 2; i <= k; i++) r *= i;
        return r;
    }

    public static void printPredictor(double lH, double lA, String label) {
        double h = 0, d = 0, a = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                double p = poisson(i, lH) * poisson(j, lA);
                if (i > j) h += p;
                else if (i == j) d += p;
                else a += p;
            }
        }
        System.out.printf("\n--- %s ---\n", label);
        System.out.printf("Lambda Local: %.3f | Lambda Visitante: %.3f\n", lH, lA);
        System.out.printf("Gana Local: %.1f%% | Empate: %.1f%% | Gana Visitante: %.1f%%\n",
                h * 100, d * 100, a * 100);
    }

    public static void predictTotalGoals(double lH, double lA) {
        double lTotal = lH + lA;
        System.out.printf("\n⚽ PROBABILIDAD DE GOLES (Total xG: %.2f)\n", lTotal);

        double probUnder25 = 0;
        for (int k = 0; k <= 10; k++) {
            double p = poisson(k, lTotal);
            if (k <= 6) System.out.printf("Exactamente %d goles: %.1f%%\n", k, p * 100);
            if (k <= 2) probUnder25 += p;
        }
        System.out.printf("Over 2.5: %.1f%% | Under 2.5: %.1f%%\n",
                (1 - probUnder25) * 100, probUnder25 * 100);
    }

    /**
     * Mercados secundarios con fórmula corregida para tiros al arco.
     *
     * FÓRMULA CORRECTA para SOT esperados:
     *   SOT_local = avgSOT_For_local * (avgSOT_Against_visitante / ligaAvgSOT)
     *
     * Esto es análogo a la fórmula de goles: cruce ataque/defensa normalizado por media de liga.
     * La media de la Premier/Bundesliga ronda 4.5 SOT por equipo por partido.
     *
     * @param leagueAdj factor de ajuste entre ligas (wLocal / wVisitante)
     */
    public static void predictSecondary(TeamStats h, TeamStats a, double leagueAdj) {
        // Media de SOT por equipo por partido en ligas europeas top (~4.5)
        // SOT esperados: promedio entre ataque propio y defensa rival
        double sH = (h.avgSOT_For(true) + a.avgSOT_Against(false)) / 2.0 * leagueAdj;
        double sA = (a.avgSOT_For(false) + h.avgSOT_Against(true)) / 2.0 / leagueAdj;

        // Amarillas: promedio simple ponderado por liga
        double yellowsH = h.avgYellows() * leagueAdj;
        double yellowsA = a.avgYellows() / leagueAdj;
        // Clamp para no disparar el valor con diferencias de liga extremas
        double totalYellows = Math.min(yellowsH + yellowsA, 8.0);

        // Rojas: probabilidad de que al menos un equipo reciba roja
        double pNoRed = poisson(0, h.avgReds()) * poisson(0, a.avgReds());

        System.out.println("\n--- MERCADOS ADICIONALES ---");
        System.out.printf("Expectativa Amarillas Totales: %.2f\n", totalYellows);
        System.out.printf("Expectativa Tiros al Arco: %.1f (L: %.1f | V: %.1f)\n",
                sH + sA, sH, sA);
        System.out.printf("Probabilidad Tarjeta Roja: %.1f%%\n", (1 - pNoRed) * 100);
    }
}