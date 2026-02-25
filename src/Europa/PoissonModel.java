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

    /**
     * Factor de corrección Dixon-Coles para resultados de baja puntuación.
     * Corrige la sobreestimación del 0-0 y subestimación del 1-1 del modelo Poisson puro.
     * rho = -0.13 es el valor estándar calibrado históricamente en ligas europeas.
     */
    private static double dixonColes(int i, int j, double lH, double lA, double rho) {
        if (i == 0 && j == 0) return 1 - lH * lA * rho;
        if (i == 0 && j == 1) return 1 + lH * rho;
        if (i == 1 && j == 0) return 1 + lA * rho;
        if (i == 1 && j == 1) return 1 - rho;
        return 1.0; // sin corrección para marcadores > 1-1
    }

    /**
     * Calcula probabilidades 1X2 con corrección Dixon-Coles.
     * Devuelve [home, draw, away]
     */
    public static double[] calcular1X2(double lH, double lA) {
        double rho = -0.13;
        double h = 0, d = 0, a = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                double p = poisson(i, lH) * poisson(j, lA) *
                        dixonColes(i, j, lH, lA, rho);
                if      (i > j) h += p;
                else if (i == j) d += p;
                else             a += p;
            }
        }
        // Normalizar por si la corrección DC alteró levemente la suma total
        double total = h + d + a;
        return new double[]{h / total, d / total, a / total};
    }

    /** Imprime predictor calculando internamente las probabilidades */
    public static void printPredictor(double lH, double lA, String label) {
        double[] probs = calcular1X2(lH, lA);
        printPredictorFromProbs(probs, lH, lA, label);
    }

    /** Imprime predictor con probabilidades ya calculadas (y posiblemente cappadas) */
    public static void printPredictorFromProbs(double[] probs, double lH, double lA, String label) {
        System.out.printf("\n--- %s ---\n", label);
        System.out.printf("Lambda Local: %.3f | Lambda Visitante: %.3f\n", lH, lA);
        System.out.printf("Gana Local: %.1f%% | Empate: %.1f%% | Gana Visitante: %.1f%%\n",
                probs[0] * 100, probs[1] * 100, probs[2] * 100);
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

    public static void predictSecondary(TeamStats h, TeamStats a, double leagueAdj) {
        double sH = (h.avgSOT_For(true) + a.avgSOT_Against(false)) / 2.0 * leagueAdj;
        double sA = (a.avgSOT_For(false) + h.avgSOT_Against(true)) / 2.0 / leagueAdj;

        // Ajuste SOT para partidos de alto xG: cuando se esperan muchos goles
        // los equipos generan más remates de lo que el promedio predice
        double xGTotal = (h.avgGoalsFor(true) + a.avgGoalsConceded(false)) / 1.35 +
                (a.avgGoalsFor(false) + h.avgGoalsConceded(true)) / 1.35;
        if (xGTotal > 3.0) {
            double sotMultiplier = 1.0 + (xGTotal - 3.0) * 0.08; // +8% por cada gol extra sobre 3.0
            sotMultiplier = Math.min(sotMultiplier, 1.30); // cap en +30%
            sH *= sotMultiplier;
            sA *= sotMultiplier;
        }

        double yellowsH = h.avgYellows() * leagueAdj;
        double yellowsA = a.avgYellows() / leagueAdj;
        double totalYellows = Math.min(yellowsH + yellowsA, 8.0);

        double pNoRed = poisson(0, h.avgReds()) * poisson(0, a.avgReds());

        System.out.println("\n--- MERCADOS ADICIONALES ---");
        System.out.printf("Expectativa Amarillas Totales: %.2f\n", totalYellows);
        System.out.printf("Expectativa Tiros al Arco: %.1f (L: %.1f | V: %.1f)\n",
                sH + sA, sH, sA);
        System.out.printf("Probabilidad Tarjeta Roja: %.1f%%\n", (1 - pNoRed) * 100);
    }
}