package PremierLeague;

public class PoissonCalculator {

    // -------------------------
    // Probabilidad Poisson
    // -------------------------
    public static double poissonProbability(int k, double lambda) {
        return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
    }

    private static int factorial(int n) {
        if (n <= 1) return 1;
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // -------------------------
    // Generar matriz de resultados
    // -------------------------
    public static double[][] generateScoreMatrix(double lambdaHome,
                                                 double lambdaAway,
                                                 int maxGoals) {

        double[][] matrix = new double[maxGoals + 1][maxGoals + 1];

        for (int i = 0; i <= maxGoals; i++) {
            for (int j = 0; j <= maxGoals; j++) {

                double pHome = poissonProbability(i, lambdaHome);
                double pAway = poissonProbability(j, lambdaAway);

                matrix[i][j] = pHome * pAway;
            }
        }

        return matrix;
    }

    // -------------------------
    // Calcular 1X2
    // -------------------------
    public static double[] calculate1X2(double[][] matrix) {

        double homeWin = 0;
        double draw = 0;
        double awayWin = 0;

        int size = matrix.length;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                if (i > j) {
                    homeWin += matrix[i][j];
                } else if (i == j) {
                    draw += matrix[i][j];
                } else {
                    awayWin += matrix[i][j];
                }
            }
        }

        return new double[]{homeWin, draw, awayWin};
    }

    public static void printScoreMatrix(double[][] matrix) {

        int size = matrix.length;

        System.out.println("\n----- Matriz de Probabilidades (Marcador) -----");

        // Encabezado columnas (goles visitante)
        System.out.print("      ");
        for (int j = 0; j < size; j++) {
            System.out.printf("  %4d ", j);
        }
        System.out.println();

        // Filas (goles local)
        for (int i = 0; i < size; i++) {
            System.out.printf("  %2d  ", i);

            for (int j = 0; j < size; j++) {
                System.out.printf("%6.4f ", matrix[i][j]);
            }

            System.out.println();
        }
    }
}
