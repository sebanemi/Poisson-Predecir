package LigaArg;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;

/**
 * Obtiene tabla de posiciones Y estadísticas detalladas por equipo desde Sofascore.
 *
 * Endpoints usados:
 *  - standings: /api/v1/tournament/143625/season/87913/standings/total
 *  - stats:     /api/v1/team/{id}/unique-tournament/143625/season/87913/statistics/overall
 */
public class SofascoreScraper {

    private static final String BASE = "https://api.sofascore.com/api/v1";
    private static final String TOURNAMENT_ID = "143625";
    private static final String SEASON_ID     = "87913";

    // ── Modelos de datos ──────────────────────────────────────────────────────

    public static class StandingStats {
        public final String team;
        public int teamId;
        public int position;
        public int played;
        public int wins, draws, losses;
        public int goalsFor, goalsAgainst;
        public int points;

        public int goalDiff()  { return goalsFor - goalsAgainst; }
        public double winRate(){ return played == 0 ? 0 : (double) wins / played; }

        public StandingStats(String team) { this.team = team; }

        @Override
        public String toString() {
            return String.format("%2d. %-22s | PJ:%2d G:%2d E:%2d P:%2d | GF:%2d GA:%2d DG:%+3d | Pts:%2d",
                    position, team, played, wins, draws, losses, goalsFor, goalsAgainst, goalDiff(), points);
        }
    }

    public static class TeamDetailStats {
        public final String team;

        // Goles
        public double avgGoalsScored;
        public double avgGoalsConceded;

        // Tiros
        public double avgShotsOnTarget;
        public double avgShotsOnTargetConceded;
        public double avgShots;

        // Posesión
        public double avgPossession;

        // Tarjetas
        public double avgYellowCards;
        public double avgRedCards;

        // Forma reciente (últimos 5 partidos)
        public String form = "";

        public TeamDetailStats(String team) { this.team = team; }

        @Override
        public String toString() {
            return String.format(
                    "%-22s | xG: %.2f concedidos: %.2f | SOT: %.1f vs %.1f | " +
                            "Amarillas: %.2f Rojas: %.2f | Posesión: %.1f%%",
                    team, avgGoalsScored, avgGoalsConceded,
                    avgShotsOnTarget, avgShotsOnTargetConceded,
                    avgYellowCards, avgRedCards, avgPossession);
        }
    }

    // ── Métodos públicos ──────────────────────────────────────────────────────

    /** Descarga tabla de posiciones con team_id de cada equipo */
    public Map<String, StandingStats> fetchStandings() throws Exception {
        String json = get(BASE + "/tournament/" + TOURNAMENT_ID + "/season/" + SEASON_ID + "/standings/total");
        return parseStandings(json);
    }

    /** Descarga estadísticas detalladas de un equipo usando su team_id */
    public TeamDetailStats fetchTeamStats(int teamId, String teamName) throws Exception {
        String url = BASE + "/team/" + teamId + "/unique-tournament/" + TOURNAMENT_ID +
                "/season/" + SEASON_ID + "/statistics/overall";
        String json = get(url);
        return parseTeamStats(json, teamName);
    }

    // ── HTTP ─────────────────────────────────────────────────────────────────

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent",  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept",      "application/json")
                .header("Referer",     "https://www.sofascore.com/")
                .timeout(java.time.Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new Exception("HTTP " + response.statusCode() + " en " + url);

        return response.body();
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private Map<String, StandingStats> parseStandings(String json) throws Exception {
        Map<String, StandingStats> result = new LinkedHashMap<>();

        int rowsStart = json.indexOf("\"rows\"");
        if (rowsStart == -1) throw new Exception("Campo 'rows' no encontrado.");

        int start = json.indexOf("[", rowsStart);
        int end   = findMatchingBracket(json, start);
        if (start == -1 || end == -1) throw new Exception("No se pudo delimitar rows.");

        for (String entry : splitJsonObjects(json.substring(start + 1, end))) {
            try {
                String teamName = extractString(entry, "name");
                if (teamName == null || teamName.isEmpty()) continue;

                StandingStats s = new StandingStats(teamName);
                s.teamId       = extractInt(entry, "id");
                s.position     = extractInt(entry, "position");
                s.played       = extractInt(entry, "matches");
                s.wins         = extractInt(entry, "wins");
                s.draws        = extractInt(entry, "draws");
                s.losses       = extractInt(entry, "losses");
                s.goalsFor     = extractInt(entry, "scoresFor");
                s.goalsAgainst = extractInt(entry, "scoresAgainst");
                s.points       = extractInt(entry, "points");

                result.put(teamName.toLowerCase(), s);
            } catch (Exception ignored) {}
        }
        return result;
    }

    private TeamDetailStats parseTeamStats(String json, String teamName) {
        TeamDetailStats s = new TeamDetailStats(teamName);

        // Los stats vienen como lista de objetos { "key": "...", "value": ... }
        // Buscamos cada clave relevante
        s.avgGoalsScored           = extractStatValue(json, "avgGoalsScored");
        s.avgGoalsConceded         = extractStatValue(json, "avgGoalsConceded");
        s.avgShotsOnTarget         = extractStatValue(json, "avgShotsOnTarget");
        s.avgShotsOnTargetConceded = extractStatValue(json, "avgShotsOnTargetConceded");
        s.avgShots                 = extractStatValue(json, "avgShots");
        s.avgPossession            = extractStatValue(json, "avgBallPossession");
        s.avgYellowCards           = extractStatValue(json, "avgYellowCards");
        s.avgRedCards              = extractStatValue(json, "avgRedCards");

        return s;
    }

    // ── Utilidades de parsing ─────────────────────────────────────────────────

    /**
     * Extrae el valor numérico de un stat por nombre de clave.
     * El JSON de stats de Sofascore tiene este formato:
     * { "statistics": { "avgGoalsScored": 1.5, "avgYellowCards": 2.1, ... } }
     */
    private double extractStatValue(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); }
            catch (Exception ignored) {}
        }
        return 0.0;
    }

    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private int extractInt(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private int findMatchingBracket(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            if      (s.charAt(i) == '[') depth++;
            else if (s.charAt(i) == ']') { if (--depth == 0) return i; }
        }
        return -1;
    }

    private List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0 && start != -1) { objects.add(json.substring(start, i+1)); start = -1; } }
        }
        return objects;
    }
}