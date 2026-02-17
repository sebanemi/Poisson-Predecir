package Europa;

import java.util.HashMap;
import java.util.Map;

public class EuropaLoader {

    public static class EuropeData {
        public Map<String, TeamStats> teams = new HashMap<>();
        public Map<String, LeagueStats> leagues = new HashMap<>();
    }

    public EuropeData loadAll() {

        EuropeData data = new EuropeData();

        Map<String, String> files = Map.ofEntries(
                Map.entry("B1","B1.csv"),
                Map.entry("D1", "D1.csv"),
                Map.entry("D2", "D2.csv"),
                Map.entry("E0", "E0.csv"),
                Map.entry("E1", "E1.csv"),
                Map.entry("E2", "E2.csv"),
                Map.entry("EC", "EC.csv"),
                Map.entry("F1", "F1.csv"),
                Map.entry("F2", "F2.csv"),
                Map.entry("G1", "G1.csv"),
                Map.entry("I1", "I1.csv"),
                Map.entry("I2", "I2.csv"),
                Map.entry("N1", "N1.csv"),
                Map.entry("P1", "P1.csv"),
                Map.entry("SC0", "SC0.csv"),
                Map.entry("SC1", "SC1.csv"),
                Map.entry("SC2", "SC2.csv"),
                Map.entry("SC3", "SC3.csv"),
                Map.entry("SP1", "SP1.csv"),
                Map.entry("SP2", "SP2.csv"),
                Map.entry("T1", "T1.csv")
        );

        for (var e : files.entrySet()) {
            LeagueStats ls = new LeagueStats();
            data.leagues.put(e.getKey(), ls);

            new MatchReader().readCsv(
                    data.teams,
                    ls,
                    e.getKey(),
                    e.getValue()
            );
        }

        return data;
    }
}