package _Minecraft2;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class GameSettings {

    public static float mouseSensitivity = 0.1f;
    public static String texturePath = "assets/textures/";

    // Chemin vers le fichier de configuration ( TODO )
    private static final Path configPath = Paths.get(System.getProperty("user.home"), ".monjeu", "options.properties");


    public static void load() {
        try {
            // Créer le dossier si nécessaire 
            Files.createDirectories(configPath.getParent());

            Properties props = new Properties();

            if (Files.exists(configPath)) {
                try (InputStream in = Files.newInputStream(configPath)) {
                    props.load(in);
                    mouseSensitivity = Float.parseFloat(props.getProperty("mouseSensitivity", "0.1"));
                    texturePath = props.getProperty("texturePath", "textures/");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des paramètres : " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("mouseSensitivity", Float.toString(mouseSensitivity));
            props.setProperty("texturePath", texturePath);

            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "Game Options");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des paramètres : " + e.getMessage());
        }
    }
}

