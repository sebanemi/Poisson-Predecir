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

        switch (opcion) {
            case 1:
                new EuropaApp().run();
                break;

            case 2:
                new LigaArgApp().run();
                break;

            default:
                System.out.println("Opción inválida");
        }
    }
}
