package Europa;

import java.util.Map;

public class LeagueRanking {

    // Pesos base por liga (ajustables)
    private static final Map<String, Double> LEAGUE_WEIGHTS = Map.ofEntries(
            Map.entry("E0", 1.00), // Premier League
            Map.entry("D1", 0.95), // Bundesliga
            Map.entry("SP1", 0.95), // La Liga
            Map.entry("I1", 0.90), // Serie A
            Map.entry("F1", 0.90), // Ligue 1

            Map.entry("E1", 0.80),
            Map.entry("D2", 0.80),
            Map.entry("SP2", 0.80),
            Map.entry("I2", 0.80),

            Map.entry("B1", 0.75),
            Map.entry("SC0", 0.75),
            Map.entry("N1", 0.75),
            Map.entry("P1", 0.75),
            Map.entry("T1", 0.75)
    );

    /**
     * Devuelve el peso de la liga.
     * Si la liga no está rankeada, devuelve 0.70 por defecto.
     */
    public static double weight(String leagueCode) {
        return LEAGUE_WEIGHTS.getOrDefault(leagueCode, 0.70);
    }
}
