package org.trivait.minesweeper.modmenu;

import com.terraformersmc.modmenu.api.*;
import com.terraformersmc.modmenu.util.mod.ModrinthUpdateInfo;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.MainMenuButtonPosition;
import org.trivait.minesweeper.config.PauseMenuButtonPosition;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Config config = AutoConfig.getConfigHolder(Config.class).getConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("text.autoconfig.minesweeper.title"))
                    .setSavingRunnable(() -> {
                        AutoConfig.getConfigHolder(Config.class).save();
                    });

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("text.autoconfig.minesweeper.title"));

            general.addEntry(entryBuilder.startIntSlider(
                            Component.translatable("text.autoconfig.minesweeper.option.gridWidth"), config.gridWidth, 4, 32)
                    .setDefaultValue(16)
                    .setSaveConsumer(newValue -> config.gridWidth = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(
                            Component.translatable("text.autoconfig.minesweeper.option.gridHeight"), config.gridHeight, 4, 32)
                    .setDefaultValue(16)
                    .setSaveConsumer(newValue -> config.gridHeight = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntField(
                            Component.translatable("text.autoconfig.minesweeper.option.mines"), config.mines)
                    .setDefaultValue(40)
                    .setSaveConsumer(newValue -> config.mines = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.translatable("text.autoconfig.minesweeper.option.enableAnimations"), config.enableAnimations)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.enableAnimations = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.translatable("text.autoconfig.minesweeper.option.enableExplosionAnimation"), config.enableExplosionAnimation)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> config.enableExplosionAnimation = newValue)
                    .build());

            general.addEntry(entryBuilder.startEnumSelector(
                            Component.translatable("text.autoconfig.minesweeper.option.mainMenuButtonPosition"), MainMenuButtonPosition.class, config.mainMenuButtonPosition)
                    .setDefaultValue(MainMenuButtonPosition.RIGHT_MULTIPLAYER)
                    .setSaveConsumer(newValue -> config.mainMenuButtonPosition = newValue)
                    .build());

            general.addEntry(entryBuilder.startEnumSelector(
                            Component.translatable("text.autoconfig.minesweeper.option.pauseMenuButtonPosition"), PauseMenuButtonPosition.class, config.pauseMenuButtonPosition)
                    .setDefaultValue(PauseMenuButtonPosition.RIGHT_NEXT_ROW)
                    .setSaveConsumer(newValue -> config.pauseMenuButtonPosition = newValue)
                    .build());

            return builder.build();
        };
    }
}
