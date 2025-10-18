package org.trivait.minesweeper.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(parent, Text.translatable("config.minesweeper.title"));
    }

    public static class ConfigScreen extends Screen {
        private final Screen parent;
        private TextFieldWidget wField, hField, mField;

        protected ConfigScreen(Screen parent, Text title) {
            super(title);
            this.parent = parent;
        }

        @Override
        protected void init() {
            Config cfg = MinesweeperModClient.getConfig();
            int y = this.height / 4;
            int x = this.width / 2 - 100;

            // Поле ширины
            wField = new TextFieldWidget(this.textRenderer, x, y + 12, 200, 20, Text.empty());
            wField.setText(String.valueOf(cfg.gridWidth));

            // Поле высоты
            hField = new TextFieldWidget(this.textRenderer, x, y + 58, 200, 20, Text.empty());
            hField.setText(String.valueOf(cfg.gridHeight));

            // Поле мин
            mField = new TextFieldWidget(this.textRenderer, x, y + 104, 200, 20, Text.empty());
            mField.setText(String.valueOf(cfg.mines));

            // Кнопки
            ButtonWidget saveBtn = ButtonWidget.builder(Text.translatable("gui.save"), b -> {
                cfg.gridWidth = clampInt(parse(wField.getText(), cfg.gridWidth), 5, 40);
                cfg.gridHeight = clampInt(parse(hField.getText(), cfg.gridHeight), 5, 40);
                int maxMines = Math.max(1, (cfg.gridWidth * cfg.gridHeight) - 1);
                cfg.mines = clampInt(parse(mField.getText(), cfg.mines), 1, maxMines);
                MinesweeperModClient.saveConfig();
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(x, y + 150, 95, 20).build();

            ButtonWidget backBtn = ButtonWidget.builder(Text.translatable("gui.back"), b -> MinecraftClient.getInstance().setScreen(parent))
                    .dimensions(x + 105, y + 150, 95, 20).build();

            addDrawableChild(wField);
            addDrawableChild(hField);
            addDrawableChild(mField);
            addDrawableChild(saveBtn);
            addDrawableChild(backBtn);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            int x = this.width / 2 - 100;
            int y = this.height / 4;

            context.drawText(client.textRenderer,
                    Text.translatable("config.minesweeper.title"),
                    this.width / 2 - client.textRenderer.getWidth(Text.translatable("config.minesweeper.title")) / 2,
                    y - 18,
                    0xFFFFFFFF,
                    false
            );

            context.drawText(client.textRenderer, Text.translatable("config.minesweeper.width"), x, y, 0xFFFFFFFF, false);
            context.drawText(client.textRenderer, Text.translatable("config.minesweeper.height"), x, y + 46, 0xFFFFFFFF, false);
            context.drawText(client.textRenderer, Text.translatable("config.minesweeper.mines"), x, y + 92, 0xFFFFFFFF, false);
        }



        private int parse(String s, int def) {
            try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
        }

        private int clampInt(int v, int min, int max) {
            return Math.max(min, Math.min(max, v));
        }


        @Override
        public boolean shouldPause() { return false; }
    }
}
