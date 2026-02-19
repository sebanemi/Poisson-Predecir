package Europa;

public class PoissonModel {

    public static double poisson(int k, double lambda) {
        return Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k);
    }

    private static double factorial(int k) {
        double r = 1;
        for (int i = 1; i <= k; i++) r *= i;
        return r;
    }

    public static void printMatrixAnd1X2(double lambdaHome, double lambdaAway) {

        double[][] matrix = new double[6][6];
        double sum = 0;

        for (int i = 0; i <= 5; i++) {
            for (int j = 0; j <= 5; j++) {
                matrix[i][j] = poisson(i, lambdaHome) * poisson(j, lambdaAway);
                sum += matrix[i][j];
            }
        }

        double home = 0, draw = 0, away = 0;
        double max = 0;
        String best = "";

        System.out.println("Matriz de Probabilidades:");
        for (int i = 0; i <= 5; i++) {
            for (int j = 0; j <= 5; j++) {
                double p = matrix[i][j] / sum;
                System.out.printf("%6.3f ", p * 100);

                if (i > j) home += p;
                else if (i == j) draw += p;
                else away += p;

                if (p > max) {
                    max = p;
                    best = i + "-" + j;
                }
            }
            System.out.println();
        }

        System.out.printf("\n1: %.2f%%  X: %.2f%%  2: %.2f%%\n",
                home * 100, draw * 100, away * 100);
        System.out.println("Resultado más probable: " + best);
    }
}