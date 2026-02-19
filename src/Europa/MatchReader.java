package Europa;

import java.io.*;
import java.util.*;

public class MatchReader {

    public void readCsv(Map<String, TeamStats> teams, String leagueCode, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String headerLine = br.readLine();
            if (headerLine == null) return;
            String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                idx.put(headers[i].trim(), i);
            }

            // Columnas criticas - sin ellas no se puede procesar el archivo
            String[] critical = {"HomeTeam","AwayTeam","FTHG","FTAG","HTHG","HTAG","HY","AY","HR","AR"};
            for (String col : critical) {
                if (!idx.containsKey(col)) {
                    System.out.println("Error critico: columna '" + col + "' no encontrada en " + filePath);
                }
            }
            // HST/AST son opcionales: si no existen, parseInt devuelve 0
            // y TeamStats usara el fallback de 4.5 automaticamente

            String line;
            while ((line = br.readLine()) != null) {
                String[] c = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                try {
                    String hName = get(c, idx, "HomeTeam");
                    String aName = get(c, idx, "AwayTeam");
                    if (hName.isEmpty() || aName.isEmpty()) continue;

                    int hg   = parseInt(c, idx, "FTHG");
                    int ag   = parseInt(c, idx, "FTAG");
                    int hthg = parseInt(c, idx, "HTHG");
                    int htag = parseInt(c, idx, "HTAG");
                    int hst  = parseInt(c, idx, "HST");
                    int ast  = parseInt(c, idx, "AST");
                    int hy   = parseInt(c, idx, "HY");
                    int ay   = parseInt(c, idx, "AY");
                    int hr   = parseInt(c, idx, "HR");
                    int ar   = parseInt(c, idx, "AR");

                    teams.computeIfAbsent(hName, t -> new TeamStats(leagueCode));
                    teams.computeIfAbsent(aName, t -> new TeamStats(leagueCode));

                    teams.get(hName).addHomeMatch(hg, ag, hst, ast, hthg, htag, hy, hr);

                    teams.get(aName).addAwayMatch(
                            ag,   // myGoals      = FTAG
                            hg,   // theirGoals   = FTHG
                            ast,  // mySot        = AST
                            hst,  // theirSot     = HST
                            htag, // myHtGoals    = HTAG
                            hthg, // theirHtGoals = HTHG
                            ay,   // myYellows    = AY
                            ar    // myReds       = AR
                    );

                } catch (Exception e) {
                    // Fila malformada, continuar
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