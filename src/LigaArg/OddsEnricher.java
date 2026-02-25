package LigaArg;

import java.io.*;
import java.util.*;

/**
 * Lee el mismo ARG.csv y extrae las cuotas promedio (AvgCH, AvgCD, AvgCA)
 * para calcular probabilidades implícitas del mercado por equipo.
 *
 * Esto complementa el modelo Poisson con la "sabiduría del mercado":
 * las casas de apuestas tienen sus propios modelos y sus cuotas son
 * información adicional muy valiosa.
 */
public class OddsEnricher {

    public static class MarketStats {
        public final String team;
        public double impliedHomeWin = 0;   // prob implícita promedio ganando de local
        public double impliedAwayWin = 0;   // prob implícita promedio ganando de visitante
        public int homeMatches = 0;
        public int awayMatches = 0;

        public MarketStats(String team) { this.team = team; }

        public double avgImpliedHome() {
            return homeMatches == 0 ? 0 : impliedHomeWin / homeMatches;
        }

        public double avgImpliedAway() {
            return awayMatches == 0 ? 0 : impliedAwayWin / awayMatches;
        }
    }

    public Map<String, MarketStats> readOdds(String csvPath) {
        Map<String, MarketStats> result = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = br.readLine();
            if (headerLine == null) return result;

            String[] headers = headerLine.split(",");
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) idx.put(headers[i].trim(), i);

            // Usamos AvgCH/AvgCD/AvgCA (promedio de casas) — más estable que una sola casa
            // Si no están, intentamos con B365CH/B365CD/B365CA
            String oddsH = idx.containsKey("AvgCH") ? "AvgCH" : "B365CH";
            String oddsD = idx.containsKey("AvgCD") ? "AvgCD" : "B365CD";
            String oddsA = idx.containsKey("AvgCA") ? "AvgCA" : "B365CA";

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] c = line.split(",", -1);
                try {
                    String home = get(c, idx, "Home");
                    String away = get(c, idx, "Away");
                    if (home.isEmpty() || away.isEmpty()) continue;

                    double oh = parseDouble(c, idx, oddsH);
                    double od = parseDouble(c, idx, oddsD);
                    double oa = parseDouble(c, idx, oddsA);

                    if (oh <= 0 || od <= 0 || oa <= 0) continue;

                    // Probabilidades implícitas (normalizadas para quitar el margen)
                    double margin = 1/oh + 1/od + 1/oa;
                    double pH = (1/oh) / margin;
                    double pA = (1/oa) / margin;

                    result.computeIfAbsent(home, MarketStats::new);
                    result.computeIfAbsent(away, MarketStats::new);

                    result.get(home).impliedHomeWin += pH;
                    result.get(home).homeMatches++;

                    result.get(away).impliedAwayWin += pA;
                    result.get(away).awayMatches++;

                } catch (Exception e) {
                    // fila malformada
                }
            }

        } catch (Exception e) {
            System.out.println("Error leyendo odds: " + e.getMessage());
        }

        return result;
    }

    private String get(String[] cols, Map<String, Integer> idx, String name) {
        Integer i = idx.get(name);
        if (i == null || i >= cols.length) return "";
        return cols[i].trim().replaceAll("\"", "");
    }

    private double parseDouble(String[] cols, Map<String, Integer> idx, String name) {
        String val = get(cols, idx, name);
        if (val.isEmpty()) return 0;
        try { return Double.parseDouble(val); }
        catch (Exception e) { return 0; }
    }
}