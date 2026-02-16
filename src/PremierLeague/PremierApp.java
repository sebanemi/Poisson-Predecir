package PremierLeague;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import main.PoissonCalculator;

public class PremierApp {

    public void run() {

        Map<String, TeamStats> teamMap = new HashMap<>();
        LeagueStats leagueStats = new LeagueStats();

        MatchReader reader = new MatchReader();
        reader.readCsv(teamMap, leagueStats, "E0.csv");

        Scanner sc = new Scanner(System.in);

        // =========================
        // Ingreso validado de equipos
        // =========================
        String homeTeam = pedirEquipoValido("local", teamMap, sc);
        String awayTeam;

        do {
            awayTeam = pedirEquipoValido("visitante", teamMap, sc);

            if (awayTeam.equals(homeTeam)) {
                System.out.println("❌ El equipo visitante no puede ser el mismo que el local.");
            }

        } while (awayTeam.equals(homeTeam));

        TeamStats homeStats = teamMap.get(homeTeam);
        TeamStats awayStats = teamMap.get(awayTeam);

        double mu = leagueStats.getAverageGoalsPerTeam();

        // -------------------------
        // λ base normalizado
        // -------------------------
        double lambdaHome =
                (homeStats.getHomeAverageGoalsFor()
                        * awayStats.getAwayAverageGoalsAgainst()) / mu;

        double lambdaAway =
                (awayStats.getAwayAverageGoalsFor()
                        * homeStats.getHomeAverageGoalsAgainst()) / mu;

        // -------------------------
        // 🔹 Factores ofensivos
        // -------------------------
        double attackFactorHome =
                0.5 * homeStats.getHomeShotsOnTargetRatio(leagueStats) +
                        0.3 * homeStats.getHomeShotsRatio(leagueStats) +
                        0.2 * homeStats.getHomeCornersRatio(leagueStats);

        double attackFactorAway =
                0.5 * awayStats.getAwayShotsOnTargetRatio(leagueStats) +
                        0.3 * awayStats.getAwayShotsRatio(leagueStats) +
                        0.2 * awayStats.getAwayCornersRatio(leagueStats);

        lambdaHome *= attackFactorHome;
        lambdaAway *= attackFactorAway;

        System.out.println("\n----- Predicción Poisson Ajustada -----");
        System.out.println("μ liga: " + mu);
        System.out.println(homeTeam + " λ ajustado: " + lambdaHome);
        System.out.println(awayTeam + " λ ajustado: " + lambdaAway);

        int maxGoals = 5;

        double[][] matrix =
                PoissonCalculator.generateScoreMatrix(lambdaHome, lambdaAway, maxGoals);

        PoissonCalculator.printScoreMatrix(matrix);

        double[] probs = PoissonCalculator.calculate1X2(matrix);

        System.out.println("\n----- Probabilidades 1X2 -----");
        System.out.printf("Local: %.2f%%%n", probs[0] * 100);
        System.out.printf("Empate: %.2f%%%n", probs[1] * 100);
        System.out.printf("Visitante: %.2f%%%n", probs[2] * 100);

        System.out.println("\n----- Fair Odds -----");
        System.out.printf("Local: %.2f%n", 1 / probs[0]);
        System.out.printf("Empate: %.2f%n", 1 / probs[1]);
        System.out.printf("Visitante: %.2f%n", 1 / probs[2]);
    }

    // =====================================================
    // Método privado de validación (encapsulado en la clase)
    // =====================================================
    private String pedirEquipoValido(String tipo, Map<String, TeamStats> teamMap, Scanner sc) {

        while (true) {
            System.out.print("Ingresá el equipo " + tipo + ": ");
            String input = sc.nextLine();

            // Normalización del input
            input = input.trim();
            input = input.replaceAll("\\s+", " ");

            for (String teamName : teamMap.keySet()) {
                if (teamName.equalsIgnoreCase(input)) {
                    return teamName; // devuelve la clave EXACTA del map
                }
            }

            System.out.println("❌ Equipo no encontrado. Revisá el nombre e intentá de nuevo.");
        }
    }
}