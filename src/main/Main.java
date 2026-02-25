package main;

import Europa.EuropaApp;
import LigaArg.LigaArgApp;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("Elegí una opción:");
        System.out.println("1 - Europa");
        System.out.println("2 - Liga Arg");

        int opcion = sc.nextInt();
        sc.nextLine(); // consumir el salto de línea

        DataDownloader downloader = new DataDownloader();

        switch (opcion) {
            case 1 -> {
                try {
                    String europaDir = downloader.downloadEuropa();
                    new EuropaApp(europaDir).run();
                } catch (Exception e) {
                    System.out.println("Error al descargar datos de Europa: " + e.getMessage());
                } finally {
                    downloader.cleanEuropa();
                }
            }
            case 2 -> {
                try {
                    String argFile = downloader.downloadArg();
                    new LigaArgApp(argFile).run();
                } catch (Exception e) {
                    System.out.println("Error al descargar datos de Argentina: " + e.getMessage());
                } finally {
                    downloader.cleanArg();
                }
            }
            default -> System.out.println("Opción inválida");
        }
    }
}