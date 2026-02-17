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
                if (c.length < 19) continue;

                try {
                    String home = c[3].trim();
                    String away = c[4].trim();

                    int hg  = Integer.parseInt(c[5]);
                    int ag  = Integer.parseInt(c[6]);

                    int hs  = Integer.parseInt(c[13]);
                    int as  = Integer.parseInt(c[14]);

                    int hst = Integer.parseInt(c[15]);
                    int ast = Integer.parseInt(c[16]);

                    int hc  = Integer.parseInt(c[17]);
                    int ac  = Integer.parseInt(c[18]);

                    teams.putIfAbsent(home, new TeamStats(leagueCode));
                    teams.putIfAbsent(away, new TeamStats(leagueCode));

                    teams.get(home).addHomeMatch(hg, ag, hs, hst, hc);
                    teams.get(away).addAwayMatch(ag, hg, as, ast, ac);

                    leagueStats.addMatch(hg, ag, hst, ast);

                } catch (NumberFormatException ignored) {
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error leyendo " + filePath + ": " + e.getMessage());
        }
    }
}