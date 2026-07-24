package org.trivait.minesweeper.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@me.shedaniel.autoconfig.annotation.Config(name = "minesweeper")
public class Config implements ConfigData {

    @ConfigEntry.BoundedDiscrete(min = 4, max = 32)
    public int gridWidth = 16;

    @ConfigEntry.BoundedDiscrete(min = 4, max = 32)
    public int gridHeight = 16;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int soundsVolume = 100;

    public int mines = 40;

    public boolean enableAnimations = true;
    public boolean enableExplosionAnimation = false;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MainMenuButtonPosition mainMenuButtonPosition = MainMenuButtonPosition.ICONS;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public PauseMenuButtonPosition pauseMenuButtonPosition = PauseMenuButtonPosition.ICONS;
}
