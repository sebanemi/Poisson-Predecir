package PremierLeague;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        Map<String, TeamStats> teamMap = new HashMap<>();
        LeagueStats leagueStats = new LeagueStats();

        MatchReader reader = new MatchReader();
        reader.readCsv(teamMap, leagueStats, "E0.csv");

        String homeTeam = "Wolves";
        String awayTeam = "Arsenal";

        if (teamMap.containsKey(homeTeam) && teamMap.containsKey(awayTeam)) {

            TeamStats homeStats = teamMap.get(homeTeam);
            TeamStats awayStats = teamMap.get(awayTeam);

            double mu = leagueStats.getAverageGoalsPerTeam();

            // 🔹 Modelo normalizado
            double lambdaHome =
                    (homeStats.getHomeAverageGoalsFor() *
                            awayStats.getAwayAverageGoalsAgainst()) / mu;

            double lambdaAway =
                    (awayStats.getAwayAverageGoalsFor() *
                            homeStats.getHomeAverageGoalsAgainst()) / mu;

            System.out.println("----- Predicción Poisson Normalizada -----");
            System.out.println("μ liga: " + mu);
            System.out.println(homeTeam + " λ: " + lambdaHome);
            System.out.println(awayTeam + " λ: " + lambdaAway);

            int maxGoals = 5;

            double[][] matrix =
                    PoissonCalculator.generateScoreMatrix(lambdaHome, lambdaAway, maxGoals);

            PoissonCalculator.printScoreMatrix(matrix);

            double[] result = PoissonCalculator.calculate1X2(matrix);

            double homeProb = result[0];
            double drawProb = result[1];
            double awayProb = result[2];

            System.out.println("\n----- 1X2 Probabilidades -----");
            System.out.printf("Local: %.4f (%.2f%%)%n", homeProb, homeProb * 100);
            System.out.printf("Empate: %.4f (%.2f%%)%n", drawProb, drawProb * 100);
            System.out.printf("Visitante: %.4f (%.2f%%)%n", awayProb, awayProb * 100);

            // -------------------------
            // Calcular odds teóricas
            // -------------------------

            double homeOdds = homeProb > 0 ? 1.0 / homeProb : 0;
            double drawOdds = drawProb > 0 ? 1.0 / drawProb : 0;
            double awayOdds = awayProb > 0 ? 1.0 / awayProb : 0;

            System.out.println("\n----- Odds Teóricas (Fair Odds) -----");
            System.out.printf("Local: %.2f%n", homeOdds);
            System.out.printf("Empate: %.2f%n", drawOdds);
            System.out.printf("Visitante: %.2f%n", awayOdds);
        }
    }
}