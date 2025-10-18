package org.trivait.minesweeper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.trivait.minesweeper.MinesweeperModClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", MinesweeperModClient.MOD_ID + ".json");

    public static Config loadOrCreate() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                    Config cfg = GSON.fromJson(r, Config.class);
                    return cfg != null ? cfg : new Config();
                }
            }
        } catch (IOException ignored) {}
        Config cfg = new Config();
        save(cfg);
        return cfg;
    }

    public static void save(Config cfg) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(cfg, w);
            }
        } catch (IOException ignored) {}
    }
}
