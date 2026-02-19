package Europa;

import java.util.Scanner;

public class EuropaApp {

    public void run() {

        EuropaLoader.EuropeData data = new EuropaLoader().loadAll();

        if (data.teams.isEmpty()) {
            System.out.println("❌ No se cargaron equipos europeos.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        String homeName = pedirEquipo("local", data, sc);
        String awayName;

        do {
            awayName = pedirEquipo("visitante", data, sc);
            if (awayName.equalsIgnoreCase(homeName)) {
                System.out.println("❌ No pueden ser el mismo equipo.");
            }
        } while (awayName.equalsIgnoreCase(homeName));

        TeamStats home = data.teams.get(homeName);
        TeamStats away = data.teams.get(awayName);

        String homeLeague = home.getLeague();
        String awayLeague = away.getLeague();

        double leagueWeightHome = LeagueRanking.weight(homeLeague);
        double leagueWeightAway = LeagueRanking.weight(awayLeague);

        System.out.println("\n----- INFO -----");
        System.out.println("Local: " + homeName + " (" + homeLeague + ")");
        System.out.println("Visitante: " + awayName + " (" + awayLeague + ")");
        System.out.printf("Peso liga local: %.2f%n", leagueWeightHome);
        System.out.printf("Peso liga visitante: %.2f%n", leagueWeightAway);

        // =========================
        // MODELO A – goles + momentum + liga
        // =========================
        System.out.println("\nMODELO A – Goles ponderados + liga");

        // MODELO A – Goles ponderados + liga
        PoissonModel.printMatrixAnd1X2(
                home.avgGoalsFor(true) * leagueWeightHome,
                away.avgGoalsFor(false) * leagueWeightAway
        );

        // =========================
        // MODELO B – momentum + liga
        // =========================
        System.out.println("\nMODELO B – Momentum últimos 5 + liga");

        double lambdaHomeB =
                home.weightedGoalsForLast5(true) * leagueWeightHome;
        double lambdaAwayB =
                away.weightedGoalsForLast5(false) * leagueWeightAway;

        PoissonModel.printMatrixAnd1X2(lambdaHomeB, lambdaAwayB);

        // =========================
        // MODELO C – xG proxy + liga
        // =========================
        System.out.println("\nMODELO C – xG proxy (tiros + SOT) + liga");

        double lambdaHomeXG =
                home.xGProxy(true, homeLeague) * leagueWeightHome;
        double lambdaAwayXG =
                away.xGProxy(false, awayLeague) * leagueWeightAway;

        lambdaHomeXG = Math.max(lambdaHomeXG, 0.05);
        lambdaAwayXG = Math.max(lambdaAwayXG, 0.05);

        PoissonModel.printMatrixAnd1X2(lambdaHomeXG, lambdaAwayXG);

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
            System.out.println("❌ Equipo no encontrado en archivos europeos.");
        }
    }
}