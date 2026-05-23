package org.trivait.minesweeper.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;

import java.util.Locale;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(parent, Component.translatable("config.minesweeper.title"));
    }

    public static class ConfigScreen extends Screen {
        private final Screen parent;
        private EditBox wField, hField, mField;
        private Button positionBtn;
        private Button pausePositionBtn;
        private Button animationsBtn;

        protected ConfigScreen(Screen parent, Component title) {
            super(title);
            this.parent = parent;
        }

        @Override
        protected void init() {
            Config cfg = MinesweeperModClient.getConfig();
            int y = this.height / 4;
            int x = this.width / 2 - 100;

            wField = new EditBox(this.font, x, y + 12, 200, 20, Component.empty());
            wField.setValue(String.valueOf(cfg.gridWidth));

            hField = new EditBox(this.font, x, y + 58, 200, 20, Component.empty());
            hField.setValue(String.valueOf(cfg.gridHeight));

            mField = new EditBox(this.font, x, y + 104, 200, 20, Component.empty());
            mField.setValue(String.valueOf(cfg.mines));

            animationsBtn = Button.builder(getAnimationsText(cfg.enableAnimations), b -> {
                cfg.enableAnimations = !cfg.enableAnimations;
                b.setMessage(getAnimationsText(cfg.enableAnimations));
            }).bounds(x, y + 150, 200, 20).build();

            if (cfg.mainMenuButtonPosition == null) {
                cfg.mainMenuButtonPosition = Config.MainMenuButtonPosition.RIGHT_MULTIPLAYER;
            }

            positionBtn = Button.builder(getPositionText(cfg.mainMenuButtonPosition), b -> {
                cfg.mainMenuButtonPosition = next(cfg.mainMenuButtonPosition);
                b.setMessage(getPositionText(cfg.mainMenuButtonPosition));
            }).bounds(x, y + 196, 200, 20).build();

            if (cfg.pauseMenuButtonPosition == null) {
                cfg.pauseMenuButtonPosition = Config.PauseMenuButtonPosition.RIGHT_NEXT_ROW;
            }

            pausePositionBtn = Button.builder(getPausePositionText(cfg.pauseMenuButtonPosition), b -> {
                cfg.pauseMenuButtonPosition = next(cfg.pauseMenuButtonPosition);
                b.setMessage(getPausePositionText(cfg.pauseMenuButtonPosition));
            }).bounds(x, y + 242, 200, 20).build();

            Button saveBtn = Button.builder(Component.translatable("gui.save"), b -> {
                cfg.gridWidth = clampInt(parse(wField.getValue(), cfg.gridWidth), 5, 40);
                cfg.gridHeight = clampInt(parse(hField.getValue(), cfg.gridHeight), 5, 40);
                int maxMines = Math.max(1, (cfg.gridWidth * cfg.gridHeight) - 1);
                cfg.mines = clampInt(parse(mField.getValue(), cfg.mines), 1, maxMines);
                MinesweeperModClient.saveConfig();
                Minecraft.getInstance().setScreen(parent);
            }).bounds(x, y + 288, 95, 20).build();

            Button backBtn = Button.builder(Component.translatable("gui.back"), b -> Minecraft.getInstance().setScreen(parent))
                    .bounds(x + 105, y + 288, 95, 20).build();

            addRenderableWidget(wField);
            addRenderableWidget(hField);
            addRenderableWidget(mField);
            addRenderableWidget(animationsBtn);
            addRenderableWidget(positionBtn);
            addRenderableWidget(pausePositionBtn);
            addRenderableWidget(saveBtn);
            addRenderableWidget(backBtn);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
            super.extractRenderState(context, mouseX, mouseY, delta);

            int x = this.width / 2 - 100;
            int y = this.height / 4;

            context.text(minecraft.font,
                    Component.translatable("config.minesweeper.title"),
                    this.width / 2 - minecraft.font.width(Component.translatable("config.minesweeper.title")) / 2,
                    y - 18,
                    0xFFFFFFFF,
                    false
            );

            context.text(minecraft.font, Component.translatable("config.minesweeper.width"), x, y, 0xFFFFFFFF, false);
            context.text(minecraft.font, Component.translatable("config.minesweeper.height"), x, y + 46, 0xFFFFFFFF, false);
            context.text(minecraft.font, Component.translatable("config.minesweeper.mines"), x, y + 92, 0xFFFFFFFF, false);
            context.text(minecraft.font, Component.translatable("config.minesweeper.animations"), x, y + 140, 0xFFFFFFFF, false);
            context.text(minecraft.font, Component.translatable("config.minesweeper.button_position"), x, y + 186, 0xFFFFFFFF, false);
            context.text(minecraft.font, Component.translatable("config.minesweeper.pause_button_position"), x, y + 232, 0xFFFFFFFF, false);
        }



        private int parse(String s, int def) {
            try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
        }

        private int clampInt(int v, int min, int max) {
            return Math.max(min, Math.min(max, v));
        }

        private Config.MainMenuButtonPosition next(Config.MainMenuButtonPosition cur) {
            Config.MainMenuButtonPosition[] values = Config.MainMenuButtonPosition.values();
            int idx = cur == null ? 0 : cur.ordinal();
            return values[(idx + 1) % values.length];
        }

        private Config.PauseMenuButtonPosition next(Config.PauseMenuButtonPosition cur) {
            Config.PauseMenuButtonPosition[] values = Config.PauseMenuButtonPosition.values();
            int idx = cur == null ? 0 : cur.ordinal();
            return values[(idx + 1) % values.length];
        }

        private Component getPositionText(Config.MainMenuButtonPosition pos) {
            String key = "config.minesweeper.main_menu_button_position." + pos.name().toLowerCase(Locale.ROOT);
            return Component.translatable(key);
        }

        private Component getPausePositionText(Config.PauseMenuButtonPosition pos) {
            String key = "config.minesweeper.pause_menu_button_position." + pos.name().toLowerCase(Locale.ROOT);
            return Component.translatable(key);
        }

        private Component getAnimationsText(boolean enabled) {
            return Component.translatable(enabled ? "options.on" : "options.off");
        }


        @Override
        public boolean isPauseScreen() { return false; }
    }
}
