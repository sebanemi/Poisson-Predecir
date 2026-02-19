package Europa;

public class TeamStats {
    private final String league;

    private int homeMatches = 0, awayMatches = 0;

    // Goles Full Time
    private int homeGoalsFor = 0, homeGoalsAgainst = 0;
    private int awayGoalsFor = 0, awayGoalsAgainst = 0;

    // Tiros al arco — con contadores separados para partidos con datos válidos
    private int homeSOT_For = 0, homeSOT_Against = 0, homeSOT_matches = 0;
    private int awaySOT_For = 0, awaySOT_Against = 0, awaySOT_matches = 0;

    // Half Time — goles propios y concedidos, separados por rol
    private int homeHT_GF = 0, homeHT_GA = 0;
    private int awayHT_GF = 0, awayHT_GA = 0;

    // Tarjetas — solo las propias del equipo
    private int yellowsAsHome = 0, yellowsAsAway = 0;
    private int redsAsHome    = 0, redsAsAway    = 0;

    public TeamStats(String league) { this.league = league; }
    public String getLeague() { return league; }

    /**
     * Registra un partido jugado como LOCAL.
     * Todos los parámetros son desde la perspectiva del CSV:
     *   hg   = FTHG  (goles del local = goles propios)
     *   ag   = FTAG  (goles del visitante = goles concedidos)
     *   hst  = HST   (tiros al arco del local = propios)
     *   ast  = AST   (tiros al arco del visitante = concedidos)
     *   hthg = HTHG  (goles HT del local = propios en HT)
     *   htag = HTAG  (goles HT del visitante = concedidos en HT)
     *   hy   = HY    (amarillas del local = propias)
     *   hr   = HR    (rojas del local = propias)
     */
    public void addHomeMatch(int hg, int ag,
                             int hst, int ast,
                             int hthg, int htag,
                             int hy, int hr) {
        homeGoalsFor     += hg;
        homeGoalsAgainst += ag;
        // Solo acumular SOT si hay datos reales (hst > 0 o ast > 0)
        if (hst > 0 || ast > 0) {
            homeSOT_For     += hst;
            homeSOT_Against += ast;
            homeSOT_matches++;
        }
        homeHT_GF        += hthg;
        homeHT_GA        += htag;
        yellowsAsHome    += hy;
        redsAsHome       += hr;
        homeMatches++;
    }

    /**
     * Registra un partido jugado como VISITANTE.
     * Todos los parámetros son desde la perspectiva del equipo visitante:
     *   myGoals        = FTAG  (goles propios)
     *   theirGoals     = FTHG  (goles concedidos)
     *   mySot          = AST   (tiros propios)
     *   theirSot       = HST   (tiros concedidos)
     *   myHtGoals      = HTAG  (goles HT propios)
     *   theirHtGoals   = HTHG  (goles HT concedidos)
     *   myYellows      = AY
     *   myReds         = AR
     */
    public void addAwayMatch(int myGoals, int theirGoals,
                             int mySot, int theirSot,
                             int myHtGoals, int theirHtGoals,
                             int myYellows, int myReds) {
        awayGoalsFor     += myGoals;
        awayGoalsAgainst += theirGoals;
        if (mySot > 0 || theirSot > 0) {
            awaySOT_For     += mySot;
            awaySOT_Against += theirSot;
            awaySOT_matches++;
        }
        awayHT_GF        += myHtGoals;
        awayHT_GA        += theirHtGoals;
        yellowsAsAway    += myYellows;
        redsAsAway       += myReds;
        awayMatches++;
    }

    // ── Goles Full Time ──
    public double avgGoalsFor(boolean home) {
        int m = home ? homeMatches : awayMatches;
        return m == 0 ? 1.35 : (double)(home ? homeGoalsFor : awayGoalsFor) / m;
    }

    public double avgGoalsConceded(boolean home) {
        int m = home ? homeMatches : awayMatches;
        return m == 0 ? 1.35 : (double)(home ? homeGoalsAgainst : awayGoalsAgainst) / m;
    }

    // ── Tiros al arco ──
    public double avgSOT_For(boolean home) {
        int m = home ? homeSOT_matches : awaySOT_matches;
        return m == 0 ? 4.5 : (double)(home ? homeSOT_For : awaySOT_For) / m;
    }

    public double avgSOT_Against(boolean home) {
        int m = home ? homeSOT_matches : awaySOT_matches;
        return m == 0 ? 4.5 : (double)(home ? homeSOT_Against : awaySOT_Against) / m;
    }

    // ── Half Time ──
    public double avgHTGoalsFor(boolean home) {
        int m = home ? homeMatches : awayMatches;
        return m == 0 ? 0.6 : (double)(home ? homeHT_GF : awayHT_GF) / m;
    }

    public double avgHTGoalsAgainst(boolean home) {
        int m = home ? homeMatches : awayMatches;
        return m == 0 ? 0.6 : (double)(home ? homeHT_GA : awayHT_GA) / m;
    }

    // ── Tarjetas ──
    public double avgYellows() {
        int total = homeMatches + awayMatches;
        return total == 0 ? 2.0 : (double)(yellowsAsHome + yellowsAsAway) / total;
    }

    public double avgReds() {
        int total = homeMatches + awayMatches;
        return total == 0 ? 0.05 : (double)(redsAsHome + redsAsAway) / total;
    }
}