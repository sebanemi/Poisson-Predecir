package main;

public class PoissonCalculator {

    public static double[][] generateScoreMatrix(double lambdaHome, double lambdaAway, int maxGoals) {

        double[][] matrix = new double[maxGoals + 1][maxGoals + 1];

        for (int i = 0; i <= maxGoals; i++) {
            for (int j = 0; j <= maxGoals; j++) {
                matrix[i][j] =
                        poisson(i, lambdaHome) *
                                poisson(j, lambdaAway);
            }
        }
        return matrix;
    }

    private static double poisson(int k, double lambda) {
        return Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k);
    }

    private static long factorial(int n) {
        long r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }

    // =========================
    // 1X2
    // =========================
    public static double[] calculate1X2(double[][] matrix) {

        double home = 0, draw = 0, away = 0;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (i > j) home += matrix[i][j];
                else if (i == j) draw += matrix[i][j];
                else away += matrix[i][j];
            }
        }
        return new double[]{home, draw, away};
    }

    // =========================
    // Resultado más probable
    // =========================
    public static class MostProbableResult {
        public int homeGoals;
        public int awayGoals;
        public double probability;
    }

    public static MostProbableResult getMostProbableScore(double[][] matrix) {

        MostProbableResult best = new MostProbableResult();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] > best.probability) {
                    best.probability = matrix[i][j];
                    best.homeGoals = i;
                    best.awayGoals = j;
                }
            }
        }
        return best;
    }

    public static void printScoreMatrix(double[][] matrix) {

        System.out.println("\n----- Matriz de Probabilidades (Marcador) -----");

        System.out.print("     ");
        for (int j = 0; j < matrix.length; j++)
            System.out.printf("%6d", j);
        System.out.println();

        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("%3d ", i);
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%6.4f", matrix[i][j]);
            }
            System.out.println();
        }
    }
}