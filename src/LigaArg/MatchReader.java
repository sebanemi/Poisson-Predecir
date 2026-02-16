package LigaArg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MatchReader {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final LocalDate CUTOFF_DATE =
            LocalDate.of(2025, 7, 11);

    public void readCsv(
            Map<String, TeamStats> teamMap,
            LeagueStats leagueStats,
            String filePath
    ) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {

                // Saltear encabezado
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length < 9) continue;

                // Validar que sea una fecha real
                String dateStr = data[3].trim();
                if (!dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    continue;
                }

                LocalDate matchDate =
                        LocalDate.parse(dateStr, DATE_FORMAT);

                // Filtrar partidos previos al torneo
                if (matchDate.isBefore(CUTOFF_DATE)) {
                    continue;
                }

                String homeTeam = data[5].trim();
                String awayTeam = data[6].trim();

                int homeGoals = Integer.parseInt(data[7]);
                int awayGoals = Integer.parseInt(data[8]);

                TeamStats homeStats =
                        teamMap.computeIfAbsent(homeTeam, k -> new TeamStats());
                TeamStats awayStats =
                        teamMap.computeIfAbsent(awayTeam, k -> new TeamStats());

                homeStats.addHomeMatch(homeGoals, awayGoals);
                awayStats.addAwayMatch(awayGoals, homeGoals);

                leagueStats.addMatch(homeGoals, awayGoals);
            }

        } catch (Exception e) {
            System.out.println("❌ Error leyendo CSV: " + e.getMessage());
        }
    }
}