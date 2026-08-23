package org.trivait.minesweeper;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.game.SavedGame;

public class MinesweeperMod implements ClientModInitializer {

    public static final String MOD_ID = "minesweeper";
    public static Config CONFIG;

    private static SavedGame savedGame;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();

        FabricLoader.getInstance().getModContainer("minesweeper").ifPresent(s ->
                ResourceManagerHelper.registerBuiltinResourcePack(Identifier.fromNamespaceAndPath("minesweeper", "minesweeper"), s, Component.translatable("resourcePack.minesweeper"), ResourcePackActivationType.NORMAL)
        );
    }

    public static SavedGame getSavedGame() { return savedGame; }
    public static void setSavedGame(SavedGame game) { savedGame = game; }
}
