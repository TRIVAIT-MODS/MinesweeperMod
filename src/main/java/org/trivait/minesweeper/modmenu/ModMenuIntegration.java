package org.trivait.minesweeper.modmenu;

import com.terraformersmc.modmenu.api.*;
import com.terraformersmc.modmenu.util.mod.ModrinthUpdateInfo;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
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
        return parent -> AutoConfigClient.getConfigScreen(Config.class, parent).get();
    }
}
