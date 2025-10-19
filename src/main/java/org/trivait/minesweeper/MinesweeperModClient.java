package org.trivait.minesweeper;

import net.fabricmc.api.ClientModInitializer;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.ConfigManager;

public class MinesweeperModClient implements ClientModInitializer {

    public static final String MOD_ID = "minesweeper";
    private static Config config;

    @Override
    public void onInitializeClient() {
        config = ConfigManager.loadOrCreate();
    }

    public static Config getConfig() {
        return config;
    }

    public static void saveConfig() {
        ConfigManager.save(config);
    }

    public static void incrementWins() {
        config.wins++;
        saveConfig();
    }
}
