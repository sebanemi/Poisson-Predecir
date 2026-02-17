package Europa;

import java.util.Scanner;
import main.PoissonCalculator;

public class EuropaApp {

    public void run() {

        EuropaLoader.EuropeData data = new EuropaLoader().loadAll();

        if (data.teams.isEmpty()) {
            System.out.println("❌ No se cargaron partidos europeos.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        // =========================
        // Ingreso de equipos
        // =========================
        String homeTeam = pedirEquipo("local", data, sc);
        String awayTeam;

        do {
            awayTeam = pedirEquipo("visitante", data, sc);
            if (awayTeam.equals(homeTeam)) {
                System.out.println("❌ El visitante no puede ser el mismo que el local.");
            }
        } while (awayTeam.equals(homeTeam));

        TeamStats home = data.teams.get(homeTeam);
        TeamStats away = data.teams.get(awayTeam);

        LeagueStats homeLeague = data.leagues.get(home.getLeague());
        LeagueStats awayLeague = data.leagues.get(away.getLeague());

        // =========================
        // Promedios de liga
        // =========================
        double leagueHomeAvg = homeLeague.avgHomeGoals();
        double leagueAwayAvg = awayLeague.avgAwayGoals();

        if (leagueHomeAvg == 0 || leagueAwayAvg == 0) {
            System.out.println("❌ No hay suficientes datos para calcular lambdas.");
            sc.close();
            return;
        }

        // =========================
        // Lambdas correctos
        // =========================
        double lambdaHome =
                home.homeGF() * away.awayGA() / leagueAwayAvg;

        double lambdaAway =
                away.awayGF() * home.homeGA() / leagueHomeAvg;

        // Piso de seguridad
        lambdaHome = Math.max(lambdaHome, 0.05);
        lambdaAway = Math.max(lambdaAway, 0.05);

        // =========================
        // Salida informativa
        // =========================
        System.out.println("\n----- Predicción Poisson (Europa) -----");
        System.out.printf("Liga local (%s) - Promedio goles local: %.3f%n",
                home.getLeague(), leagueHomeAvg);
        System.out.printf("Liga visitante (%s) - Promedio goles visitante: %.3f%n",
                away.getLeague(), leagueAwayAvg);

        System.out.println(homeTeam + " λ: " + lambdaHome);
        System.out.println(awayTeam + " λ: " + lambdaAway);
        System.out.printf("λ total partido: %.3f%n", lambdaHome + lambdaAway);

        // =========================
        // Poisson
        // =========================
        int maxGoals = 5;

        double[][] matrix =
                PoissonCalculator.generateScoreMatrix(
                        lambdaHome, lambdaAway, maxGoals);

        PoissonCalculator.printScoreMatrix(matrix);

        double[] probs = PoissonCalculator.calculate1X2(matrix);

        PoissonCalculator.MostProbableResult best =
                PoissonCalculator.getMostProbableScore(matrix);

        System.out.println("\n----- Resultado más probable -----");
        System.out.printf("Marcador: %s %d - %d %s%n",
                homeTeam,
                best.homeGoals,
                best.awayGoals,
                awayTeam);

        System.out.printf("Probabilidad: %.2f%%%n", best.probability * 100);

        // =========================
        // Probabilidades 1X2
        // =========================
        System.out.println("\n----- Probabilidades 1X2 -----");
        System.out.printf("Local: %.2f%%%n", probs[0] * 100);
        System.out.printf("Empate: %.2f%%%n", probs[1] * 100);
        System.out.printf("Visitante: %.2f%%%n", probs[2] * 100);
        System.out.printf("Suma: %.4f%n", probs[0] + probs[1] + probs[2]);

        // =========================
        // Fair Odds
        // =========================
        System.out.println("\n----- Fair Odds -----");
        System.out.printf("Local: %.2f%n", 1 / probs[0]);
        System.out.printf("Empate: %.2f%n", 1 / probs[1]);
        System.out.printf("Visitante: %.2f%n", 1 / probs[2]);

        sc.close();
    }

    // =========================
    // Validación de equipos
    // =========================
    private String pedirEquipo(
            String tipo,
            EuropaLoader.EuropeData data,
            Scanner sc
    ) {
        while (true) {
            System.out.print("Ingresá equipo " + tipo + ": ");
            String input = sc.nextLine().trim().replaceAll("\\s+", " ");

            for (String team : data.teams.keySet()) {
                if (team.equalsIgnoreCase(input)) {
                    return team;
                }
            }
            System.out.println("❌ Equipo no encontrado.");
        }
    }
}