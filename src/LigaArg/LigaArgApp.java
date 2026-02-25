package LigaArg;

import java.util.*;

public class LigaArgApp {

    private final String csvPath;

    public LigaArgApp(String csvPath) { this.csvPath = csvPath; }
    public LigaArgApp() { this.csvPath = "ARG.csv"; }

    public void run() {

        // ── 1. Resultados CSV ────────────────────────────────────────────────
        Map<String, TeamStats> teams = new HashMap<>();
        LeagueStats leagueStats = new LeagueStats();
        new MatchReader().readCsv(teams, leagueStats, csvPath);

        if (teams.isEmpty()) { System.out.println("No se cargaron partidos."); return; }

        // ── 2. Probabilidades implícitas del mercado ─────────────────────────
        Map<String, OddsEnricher.MarketStats> market = new OddsEnricher().readOdds(csvPath);

        // ── 3. Sofascore: standings + stats detalladas ───────────────────────
        SofascoreScraper scraper = new SofascoreScraper();
        Map<String, SofascoreScraper.StandingStats> standings = new HashMap<>();
        Map<String, SofascoreScraper.TeamDetailStats> detailStats = new HashMap<>();

        try {
            System.out.println("Descargando tabla de posiciones (Sofascore)...");
            standings = scraper.fetchStandings();
            System.out.println("Sofascore: " + standings.size() + " equipos en tabla.");

            // Descargar stats detalladas para cada equipo del standings
            System.out.println("Descargando estadísticas detalladas...");
            int loaded = 0;
            for (Map.Entry<String, SofascoreScraper.StandingStats> e : standings.entrySet()) {
                try {
                    SofascoreScraper.TeamDetailStats ds = scraper.fetchTeamStats(
                            e.getValue().teamId, e.getValue().team);
                    detailStats.put(e.getKey(), ds);
                    loaded++;
                    Thread.sleep(300); // pausa para no saturar la API
                } catch (Exception ignored) {}
            }
            System.out.println("Stats detalladas: " + loaded + " equipos.");

        } catch (Exception e) {
            System.out.println("Advertencia Sofascore: " + e.getMessage());
        }

        // ── 4. Ingreso de equipos ────────────────────────────────────────────
        Scanner sc = new Scanner(System.in);
        String homeTeam = pedirEquipo("local", teams, sc);
        String awayTeam;
        do {
            awayTeam = pedirEquipo("visitante", teams, sc);
            if (awayTeam.equals(homeTeam))
                System.out.println("El visitante no puede ser igual al local.");
        } while (awayTeam.equals(homeTeam));

        TeamStats h = teams.get(homeTeam);
        TeamStats a = teams.get(awayTeam);

        double leagueHomeAvg = leagueStats.getHomeAverageGoals();
        double leagueAwayAvg = leagueStats.getAwayAverageGoals();

        // ── 5. Lambdas de goles (modelo Poisson base del CSV) ────────────────
        double lH = safeLambda((h.avgGoalsFor(true)  * a.avgGoalsConceded(false)) / leagueHomeAvg);
        double lA = safeLambda((a.avgGoalsFor(false) * h.avgGoalsConceded(true))  / leagueAwayAvg);
        double[] poissonProbs = calcular1X2(lH, lA, 8);

        System.out.println("\n--- PREDICCION LIGA ARGENTINA ---");
        System.out.printf("Lambda Local: %.3f | Lambda Visitante: %.3f%n", lH, lA);

        System.out.println("\n--- FULL TIME (Modelo Poisson) ---");
        System.out.printf("Gana Local: %.1f%% | Empate: %.1f%% | Gana Visitante: %.1f%%%n",
                poissonProbs[0]*100, poissonProbs[1]*100, poissonProbs[2]*100);

        // ── 6. Blend con mercado ─────────────────────────────────────────────
        OddsEnricher.MarketStats mH = market.get(homeTeam);
        OddsEnricher.MarketStats mA = market.get(awayTeam);
        double[] finalProbs = poissonProbs;

        if (mH != null && mA != null && mH.homeMatches > 0 && mA.awayMatches > 0) {
            double blendH = poissonProbs[0]*0.70 + mH.avgImpliedHome()*0.30;
            double blendA = poissonProbs[2]*0.70 + mA.avgImpliedAway()*0.30;
            double blendD = 1 - blendH - blendA;
            finalProbs = new double[]{blendH, blendD, blendA};

            System.out.println("\n--- PROBABILIDAD COMBINADA (70% Poisson + 30% Mercado) ---");
            System.out.printf("Gana Local: %.1f%% | Empate: %.1f%% | Gana Visitante: %.1f%%%n",
                    blendH*100, blendD*100, blendA*100);
        }

        System.out.println("\n--- FAIR ODDS ---");
        System.out.printf("Local: %.2f | Empate: %.2f | Visitante: %.2f%n",
                1/finalProbs[0], 1/finalProbs[1], 1/finalProbs[2]);

        // ── 7. Goles ─────────────────────────────────────────────────────────
        System.out.printf("%n⚽ PROBABILIDAD DE GOLES (Total xG: %.2f)%n", lH + lA);
        for (int g = 0; g <= 7; g++) {
            System.out.printf("Exactamente %d goles: %.1f%%%n", g, probExactos(g, lH, lA)*100);
        }
        double over25 = 1 - (probExactos(0,lH,lA) + probExactos(1,lH,lA) + probExactos(2,lH,lA));
        System.out.printf("Over 2.5: %.1f%% | Under 2.5: %.1f%%%n", over25*100, (1-over25)*100);

        // ── 8. Tabla de posiciones ───────────────────────────────────────────
        SofascoreScraper.StandingStats sH = standings.get(homeTeam.toLowerCase());
        SofascoreScraper.StandingStats sA = standings.get(awayTeam.toLowerCase());

        if (sH != null || sA != null) {
            System.out.println("\n--- TABLA DE POSICIONES ---");
            if (sH != null) System.out.println("[Local]     " + sH);
            if (sA != null) System.out.println("[Visitante] " + sA);
        }

        // ── 9. Mercados adicionales (igual que Europa) ────────────────────────
        SofascoreScraper.TeamDetailStats dH = detailStats.get(homeTeam.toLowerCase());
        SofascoreScraper.TeamDetailStats dA = detailStats.get(awayTeam.toLowerCase());

        if (dH != null && dA != null) {
            // Liga avg SOT para normalizar (igual que Europa usa 4.5)
            double ligaAvgSOT = detailStats.values().stream()
                    .mapToDouble(d -> d.avgShotsOnTarget).average().orElse(4.0);

            double sotH = (dH.avgShotsOnTarget + dA.avgShotsOnTargetConceded) / 2.0;
            double sotA = (dA.avgShotsOnTarget + dH.avgShotsOnTargetConceded) / 2.0;

            double yellH = dH.avgYellowCards;
            double yellA = dA.avgYellowCards;
            double redsH = dH.avgRedCards > 0 ? dH.avgRedCards : 0.05;
            double redsA = dA.avgRedCards > 0 ? dA.avgRedCards : 0.05;

            double probNoRed = poisson(0, redsH) * poisson(0, redsA);

            System.out.println("\n--- MERCADOS ADICIONALES (Sofascore) ---");
            System.out.printf("Expectativa Tiros al Arco: %.1f (L: %.1f | V: %.1f)%n",
                    sotH + sotA, sotH, sotA);
            System.out.printf("Expectativa Amarillas Totales: %.2f%n", yellH + yellA);
            System.out.printf("Probabilidad Tarjeta Roja: %.1f%%%n", (1 - probNoRed) * 100);
            System.out.printf("Posesión esperada: %.1f%% / %.1f%%%n",
                    dH.avgPossession, dA.avgPossession);
        }

        sc.close();
    }

    // ── Poisson ──────────────────────────────────────────────────────────────

    private double probExactos(int total, double lH, double lA) {
        double result = 0;
        for (int h = 0; h <= total; h++) result += poisson(h, lH) * poisson(total - h, lA);
        return result;
    }

    private double poisson(int k, double lambda) {
        return Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k);
    }

    private double factorial(int n) {
        double f = 1;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }

    private double[] calcular1X2(double lH, double lA, int maxGoals) {
        double home = 0, draw = 0, away = 0;
        for (int h = 0; h <= maxGoals; h++) {
            for (int a = 0; a <= maxGoals; a++) {
                double p = poisson(h, lH) * poisson(a, lA);
                if      (h > a) home += p;
                else if (h == a) draw += p;
                else             away += p;
            }
        }
        return new double[]{home, draw, away};
    }

    private static double safeLambda(double v) { return v < 0.05 ? 0.05 : v; }

    private String pedirEquipo(String tipo, Map<String, TeamStats> teams, Scanner sc) {
        while (true) {
            System.out.print("Ingresá el equipo " + tipo + ": ");
            String input = sc.nextLine().trim().replaceAll("\\s+", " ");
            for (String name : teams.keySet()) {
                if (name.equalsIgnoreCase(input)) return name;
            }
            System.out.println("Equipo no encontrado.");
        }
    }
}