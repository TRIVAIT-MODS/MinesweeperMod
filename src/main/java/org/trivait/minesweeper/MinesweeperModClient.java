package org.trivait.minesweeper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.ConfigManager;
import org.trivait.minesweeper.screen.MinesweeperScreen;

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
}
