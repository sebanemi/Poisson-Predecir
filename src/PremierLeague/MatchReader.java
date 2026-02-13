package PremierLeague;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class MatchReader {

    public void readCsv(Map<String, TeamStats> teamMap,
                        LeagueStats leagueStats,
                        String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] columns = line.split(",");

                if (columns.length <= 6) continue;

                String homeTeam = columns[3].trim();
                String awayTeam = columns[4].trim();

                if (columns[5].isEmpty() || columns[6].isEmpty()) continue;

                int homeGoals;
                int awayGoals;

                try {
                    homeGoals = Integer.parseInt(columns[5].trim());
                    awayGoals = Integer.parseInt(columns[6].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                teamMap.putIfAbsent(homeTeam, new TeamStats());
                teamMap.putIfAbsent(awayTeam, new TeamStats());

                teamMap.get(homeTeam).addHomeMatch(homeGoals, awayGoals);
                teamMap.get(awayTeam).addAwayMatch(awayGoals, homeGoals);

                // 🔹 NUEVO: alimentar estadística de liga
                leagueStats.addMatch(homeGoals, awayGoals);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}