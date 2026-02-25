package main;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Descarga los CSVs de football-data.co.uk al iniciar el programa
 * y los borra al terminar.
 *
 * Europa: descarga el ZIP de la temporada actual y extrae los CSVs.
 * Argentina: descarga el archivo ARG.csv directamente.
 */
public class DataDownloader {

    // URLs de descarga
    private static final String EUROPA_ZIP_URL = "https://www.football-data.co.uk/mmz4281/2526/data.zip";
    private static final String ARG_CSV_URL    = "https://www.football-data.co.uk/new/ARG.csv";

    // Directorio temporal donde se guardan los archivos descargados
    // Usar el directorio de trabajo del proyecto para que MatchReader los encuentre
    public static final String EUROPA_DIR = "data/europa/";
    public static final String ARG_DIR    = "data/arg/";

    private final HttpClient client = HttpClient.newHttpClient();

    // ── EUROPA ──────────────────────────────────────────────────────────────

    /**
     * Descarga el ZIP de Europa, extrae todos los CSVs en EUROPA_DIR
     * y devuelve el path del directorio.
     */
    public String downloadEuropa() throws Exception {
        System.out.println("Descargando datos de Europa...");

        // Crear directorio si no existe
        Files.createDirectories(Paths.get(EUROPA_DIR));

        // Descargar el ZIP a un archivo temporal
        byte[] zipBytes = downloadBytes(EUROPA_ZIP_URL);

        // Extraer CSVs del ZIP
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                // Solo nos interesan los archivos .csv que están dentro del ZIP
                if (name.endsWith(".csv")) {
                    // Algunos ZIPs tienen subcarpetas, tomamos solo el nombre del archivo
                    String fileName = Paths.get(name).getFileName().toString();
                    Path dest = Paths.get(EUROPA_DIR + fileName);
                    Files.copy(zis, dest, StandardCopyOption.REPLACE_EXISTING);
                    count++;
                }
                zis.closeEntry();
            }
        }

        System.out.println("Europa: " + count + " archivos CSV descargados en " + EUROPA_DIR);
        return EUROPA_DIR;
    }

    // ── ARGENTINA ───────────────────────────────────────────────────────────

    /**
     * Descarga el ARG.csv en ARG_DIR y devuelve el path del archivo.
     */
    public String downloadArg() throws Exception {
        System.out.println("Descargando datos de Argentina...");

        Files.createDirectories(Paths.get(ARG_DIR));

        byte[] csvBytes = downloadBytes(ARG_CSV_URL);
        Path dest = Paths.get(ARG_DIR + "ARG.csv");
        Files.write(dest, csvBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Argentina: ARG.csv descargado en " + ARG_DIR);
        return dest.toString();
    }

    // ── LIMPIEZA ─────────────────────────────────────────────────────────────

    /**
     * Borra el directorio de Europa y todo su contenido.
     */
    public void cleanEuropa() {
        deleteDirectory(Paths.get(EUROPA_DIR));
        System.out.println("Archivos de Europa eliminados.");
    }

    /**
     * Borra el directorio de Argentina y todo su contenido.
     */
    public void cleanArg() {
        deleteDirectory(Paths.get(ARG_DIR));
        System.out.println("Archivos de Argentina eliminados.");
    }

    // ── UTILIDADES ───────────────────────────────────────────────────────────

    private byte[] downloadBytes(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("Error al descargar " + url + " — HTTP " + response.statusCode());
        }

        return response.body();
    }

    private void deleteDirectory(Path path) {
        if (!Files.exists(path)) return;
        try {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a)) // borrar hijos antes que el padre
                    .forEach(p -> {
                        try { Files.delete(p); }
                        catch (IOException e) { System.out.println("No se pudo borrar: " + p); }
                    });
        } catch (IOException e) {
            System.out.println("Error al limpiar directorio: " + path);
        }
    }
}
