package org.trivait.minesweeper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MinesweeperStats {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(MinecraftClient.getInstance().runDirectory, "config/minesweeper.json");

    public int wins = 0;

    public static MinesweeperStats load() {
        try (FileReader reader = new FileReader(FILE)) {
            return GSON.fromJson(reader, MinesweeperStats.class);
        } catch (Exception e) {
            return new MinesweeperStats();
        }
    }

    public void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception ignored) {}
    }
}
