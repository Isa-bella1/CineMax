package cinemax.persistence;
//@author Wilson Bernal Gaia (766890) [VA]
// @author Rahmouni Malek (765952) [VA]
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    public static final String SEPARATORE = ",";

    private FileManager() {
    }

    public static List<String> leggiRighe(String percorsoFile) throws IOException {
        List<String> righe = new ArrayList<>();
        File file = new File(percorsoFile);
        if (!file.exists()) {
            return righe;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                riga = riga.trim();
                if (!riga.isEmpty() && !riga.startsWith("#")) {
                    righe.add(riga);
                }
            }
        }
        return righe;
    }

    public static void scriviRighe(String percorsoFile, List<String> righe) throws IOException {
        File file = new File(percorsoFile);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (String riga : righe) {
                writer.write(riga);
                writer.newLine();
            }
        }
    }

    public static void aggiungiRiga(String percorsoFile, String riga) throws IOException {
        File file = new File(percorsoFile);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.write(riga);
            writer.newLine();
        }
    }

    public static String[] dividiCampi(String riga) {
        return riga.split(SEPARATORE, -1);
    }

    public static String costruisciRiga(String... campi) {
        return String.join(SEPARATORE, campi);
    }

    public static void verificaCampi(String[] campi, int minimoAtteso, String descrizioneRiga) {
        if (campi.length < minimoAtteso) {
            throw new IllegalArgumentException("Riga CSV malformata (" + descrizioneRiga + "): attesi almeno " + minimoAtteso + " campi, trovati " + campi.length);
        }
    }
}
