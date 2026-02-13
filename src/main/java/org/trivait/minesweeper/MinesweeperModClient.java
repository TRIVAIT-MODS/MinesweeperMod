package org.trivait.minesweeper;

import net.fabricmc.api.ClientModInitializer;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.ConfigManager;

public class MinesweeperModClient implements ClientModInitializer {

    public static final String MOD_ID = "minesweeper";
    private static Config config;

    public static final class SavedGame {
        public int w;
        public int h;
        public int mines;

        public boolean minesPlaced;
        public int remainingSafe;

        public boolean alive;
        public boolean won;
        public boolean firstClick;

        public boolean timerRunning;
        public int elapsedSeconds;

        public boolean[] mine;
        public boolean[] revealed;
        public boolean[] flagged;
        public int[] adjacent;
        public float[] revealProgress;
        public int[] delayTicks;
        public boolean[] scheduled;
    }

    private static SavedGame savedGame;

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

    public static SavedGame getSavedGame() {
        return savedGame;
    }

    public static void setSavedGame(SavedGame game) {
        savedGame = game;
    }
}
