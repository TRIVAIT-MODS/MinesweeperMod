package org.trivait.minesweeper.config;

import net.minecraft.network.chat.Component;

public enum MainMenuButtonPosition {
    LEFT_SINGLEPLAYER(ButtonSide.LEFT, ButtonRow.SINGLEPLAYER, Component.translatable("config.minesweeper.main_menu_button_position.left_singleplayer")),
    LEFT_MULTIPLAYER(ButtonSide.LEFT, ButtonRow.MULTIPLAYER, Component.translatable("config.minesweeper.main_menu_button_position.left_multiplayer")),
    LEFT_REALMS(ButtonSide.LEFT, ButtonRow.REALMS, Component.translatable("config.minesweeper.main_menu_button_position.left_realms")),
    LEFT_SETTINGS(ButtonSide.LEFT, ButtonRow.SETTINGS, Component.translatable("config.minesweeper.main_menu_button_position.left_settings")),
    RIGHT_SINGLEPLAYER(ButtonSide.RIGHT, ButtonRow.SINGLEPLAYER, Component.translatable("config.minesweeper.main_menu_button_position.right_singleplayer")),
    RIGHT_MULTIPLAYER(ButtonSide.RIGHT, ButtonRow.MULTIPLAYER, Component.translatable("config.minesweeper.main_menu_button_position.right_multiplayer")),
    RIGHT_REALMS(ButtonSide.RIGHT, ButtonRow.REALMS, Component.translatable("config.minesweeper.main_menu_button_position.right_realms")),
    RIGHT_SETTINGS(ButtonSide.RIGHT, ButtonRow.SETTINGS, Component.translatable("config.minesweeper.main_menu_button_position.right_settings")),
    ICONS(ButtonSide.RIGHT, ButtonRow.SETTINGS, Component.translatable("config.minesweeper.main_menu_button_position.icons"));

    private final ButtonSide side;
    private final ButtonRow row;
    private final Component displayName;

    MainMenuButtonPosition(ButtonSide side, ButtonRow row, Component displayName) {
        this.side = side;
        this.row = row;
        this.displayName = displayName;
    }

    public int getX(int screenWidth) {
        return side == ButtonSide.LEFT ? (screenWidth / 2 - 124) : (screenWidth / 2 + 104);
    }

    @Override
    public String toString() {
        return this.displayName.getString();
    }

    public int getY(int screenHeight) {
        return screenHeight / 4 + row.getYOffset();
    }
}