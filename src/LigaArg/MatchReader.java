package LigaArg;

import java.io.*;
import java.util.*;

public class MatchReader {

    public void readCsv(Map<String, TeamStats> teams, LeagueStats leagueStats, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            if (headerLine == null) return;

            String[] headers = headerLine.split(",");
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                idx.put(headers[i].trim(), i);
            }

            // Columnas criticas para Argentina
            String[] critical = {"Home", "Away", "HG", "AG"};
            for (String col : critical) {
                if (!idx.containsKey(col)) {
                    System.out.println("Error critico: columna '" + col + "' no encontrada en " + filePath);
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] c = line.split(",", -1);
                try {
                    String hName = get(c, idx, "Home");
                    String aName = get(c, idx, "Away");
                    if (hName.isEmpty() || aName.isEmpty()) continue;

                    int hg = parseInt(c, idx, "HG");
                    int ag = parseInt(c, idx, "AG");

                    teams.computeIfAbsent(hName, t -> new TeamStats());
                    teams.computeIfAbsent(aName, t -> new TeamStats());

                    teams.get(hName).addHomeMatch(hg, ag);
                    teams.get(aName).addAwayMatch(ag, hg);

                    leagueStats.addMatch(hg, ag);

                } catch (Exception e) {
                    // fila malformada, continuar
                }
            }

        } catch (Exception e) {
            System.out.println("Error critico leyendo " + filePath + ": " + e.getMessage());
        }
    }

    private String get(String[] cols, Map<String, Integer> idx, String name) {
        Integer i = idx.get(name);
        if (i == null || i >= cols.length) return "";
        return cols[i].trim().replaceAll("\"", "");
    }

    private int parseInt(String[] cols, Map<String, Integer> idx, String name) {
        String val = get(cols, idx, name);
        if (val.isEmpty()) return 0;
        try {
            return (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}