package Europa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

public class MatchReader {

    public void readCsv(
            Map<String, TeamStats> teams,
            LeagueStats leagueStats,
            String leagueCode,
            String filePath
    ) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                String[] c = line.split(",");
                if (c.length < 17) continue;

                try {
                    String home = c[3].trim();
                    String away = c[4].trim();

                    int hg  = Integer.parseInt(c[5]);
                    int ag  = Integer.parseInt(c[6]);

                    int hs  = Integer.parseInt(c[13]);
                    int as  = Integer.parseInt(c[14]);

                    int hst = Integer.parseInt(c[15]);
                    int ast = Integer.parseInt(c[16]);

                    // CREACIÓN SEGURA DE EQUIPOS
                    teams.computeIfAbsent(home, t -> new TeamStats(leagueCode));
                    teams.computeIfAbsent(away, t -> new TeamStats(leagueCode));

                    teams.get(home).addHomeMatch(hg, ag, hs, hst);
                    teams.get(away).addAwayMatch(ag, hg, as, ast);

                    leagueStats.addMatch(hg, ag, hst, ast);

                } catch (NumberFormatException ignored) {
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error leyendo " + filePath + ": " + e.getMessage());
        }
    }
}