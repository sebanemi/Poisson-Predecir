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
            boolean header = true;

            while ((line = br.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                String[] c = line.split(",");

                if (c.length < 19) continue;

                String home = c[3].trim();
                String away = c[4].trim();

                int hg = Integer.parseInt(c[5]);
                int ag = Integer.parseInt(c[6]);

                int hs = Integer.parseInt(c[13]);
                int as = Integer.parseInt(c[14]);
                int hst = Integer.parseInt(c[15]);
                int ast = Integer.parseInt(c[16]);
                int hc = Integer.parseInt(c[17]);
                int ac = Integer.parseInt(c[18]);

                teamMap.putIfAbsent(home, new TeamStats());
                teamMap.putIfAbsent(away, new TeamStats());

                teamMap.get(home).addHomeMatch(hg, ag, hs, hst, hc);
                teamMap.get(away).addAwayMatch(ag, hg, as, ast, ac);

                leagueStats.addMatch(hg, ag, hs, as, hst, ast, hc, ac);
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}