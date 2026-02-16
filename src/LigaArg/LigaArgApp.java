package LigaArg;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import main.PoissonCalculator;

public class LigaArgApp {

    public void run() {

        Map<String, TeamStats> teamMap = new HashMap<>();
        LeagueStats leagueStats = new LeagueStats();

        MatchReader reader = new MatchReader();
        reader.readCsv(teamMap, leagueStats, "ARG.csv");

        if (teamMap.isEmpty()) {
            System.out.println("❌ No se cargaron partidos para la temporada seleccionada.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        // =========================
        // Ingreso validado de equipos
        // =========================
        String homeTeam = pedirEquipoValido("local", teamMap, sc);
        String awayTeam;

        do {
            awayTeam = pedirEquipoValido("visitante", teamMap, sc);
            if (awayTeam.equals(homeTeam)) {
                System.out.println("❌ El visitante no puede ser el mismo que el local.");
            }
        } while (awayTeam.equals(homeTeam));

        TeamStats homeStats = teamMap.get(homeTeam);
        TeamStats awayStats = teamMap.get(awayTeam);

        // =========================
        // Promedios de liga correctos
        // =========================
        double avgHomeGoals = leagueStats.getHomeAverageGoals();
        double avgAwayGoals = leagueStats.getAwayAverageGoals();

        if (avgHomeGoals == 0 || avgAwayGoals == 0) {
            System.out.println("❌ No hay suficientes datos para calcular lambdas.");
            sc.close();
            return;
        }

        // =========================
        // Lambdas CORRECTOS
        // =========================
        double leagueHomeAvg = leagueStats.getHomeAverageGoals();
        double leagueAwayAvg = leagueStats.getAwayAverageGoals();

        double lambdaHome = safeLambda(
                (homeStats.getHomeAverageGoalsFor()
                        * awayStats.getAwayAverageGoalsAgainst())
                        / leagueHomeAvg
        );

        double lambdaAway = safeLambda(
                (awayStats.getAwayAverageGoalsFor()
                        * homeStats.getHomeAverageGoalsAgainst())
                        / leagueAwayAvg
        );

        System.out.println("\n----- Predicción Poisson (Liga Argentina) -----");
        System.out.printf("Promedio goles local liga: %.3f%n", leagueHomeAvg);
        System.out.printf("Promedio goles visitante liga: %.3f%n", leagueAwayAvg);
        System.out.println(homeTeam + " λ: " + lambdaHome);
        System.out.println(awayTeam + " λ: " + lambdaAway);
        System.out.println("λ total partido: " + (lambdaHome + lambdaAway));

        // =========================
        // Poisson
        // =========================
        int maxGoals = 5;

        double[][] matrix =
                PoissonCalculator.generateScoreMatrix(lambdaHome, lambdaAway, maxGoals);

        PoissonCalculator.printScoreMatrix(matrix);

        double[] probs = PoissonCalculator.calculate1X2(matrix);

        System.out.println("\n----- Probabilidades 1X2 -----");
        System.out.printf("Local: %.2f%%%n", probs[0] * 100);
        System.out.printf("Empate: %.2f%%%n", probs[1] * 100);
        System.out.printf("Visitante: %.2f%%%n", probs[2] * 100);
        System.out.printf("Suma: %.4f%n", probs[0] + probs[1] + probs[2]);

        System.out.println("\n----- Fair Odds -----");
        System.out.printf("Local: %.2f%n", 1 / probs[0]);
        System.out.printf("Empate: %.2f%n", 1 / probs[1]);
        System.out.printf("Visitante: %.2f%n", 1 / probs[2]);

        sc.close();
    }

    // =========================
    // Lambda mínimo de seguridad
    // =========================
    private static double safeLambda(double v) {
        return v < 0.05 ? 0.05 : v;
    }

    // =========================
    // Validación de equipos
    // =========================
    private String pedirEquipoValido(String tipo, Map<String, TeamStats> teamMap, Scanner sc) {

        while (true) {
            System.out.print("Ingresá el equipo " + tipo + ": ");
            String input = sc.nextLine().trim().replaceAll("\\s+", " ");

            for (String team : teamMap.keySet()) {
                if (team.equalsIgnoreCase(input)) {
                    return team;
                }
            }

            System.out.println("❌ Equipo no encontrado.");
        }
    }
}