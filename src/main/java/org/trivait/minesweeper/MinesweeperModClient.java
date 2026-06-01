package org.trivait.minesweeper;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.game.SavedGame;

public class MinesweeperModClient implements ClientModInitializer {

    public static final String MOD_ID = "minesweeper";
    public static Config CONFIG;

    private static SavedGame savedGame;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    public static SavedGame getSavedGame() { return savedGame; }
    public static void setSavedGame(SavedGame game) { savedGame = game; }
}
