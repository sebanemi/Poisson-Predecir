package main;

public class PoissonCalculator {

    public static double poissonProbability(int k, double lambda) {
        return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
    }

    private static int factorial(int n) {
        if (n <= 1) return 1;
        int r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }

    public static double[][] generateScoreMatrix(double lh, double la, int max) {

        double[][] m = new double[max + 1][max + 1];

        for (int i = 0; i <= max; i++) {
            for (int j = 0; j <= max; j++) {
                m[i][j] = poissonProbability(i, lh) * poissonProbability(j, la);
            }
        }
        return m;
    }

    public static double[] calculate1X2(double[][] m) {

        double h = 0, d = 0, a = 0;

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                if (i > j) h += m[i][j];
                else if (i == j) d += m[i][j];
                else a += m[i][j];
            }
        }
        return new double[]{h, d, a};
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