package org.trivait.minesweeper.config;

public class Config {
    public enum MainMenuButtonPosition {
        LEFT_SINGLEPLAYER(ButtonSide.LEFT, ButtonRow.SINGLEPLAYER),
        LEFT_MULTIPLAYER(ButtonSide.LEFT, ButtonRow.MULTIPLAYER),
        LEFT_REALMS(ButtonSide.LEFT, ButtonRow.REALMS),
        LEFT_MODS(ButtonSide.LEFT, ButtonRow.MODS),
        RIGHT_SINGLEPLAYER(ButtonSide.RIGHT, ButtonRow.SINGLEPLAYER),
        RIGHT_MULTIPLAYER(ButtonSide.RIGHT, ButtonRow.MULTIPLAYER),
        RIGHT_REALMS(ButtonSide.RIGHT, ButtonRow.REALMS),
        RIGHT_MODS(ButtonSide.RIGHT, ButtonRow.MODS);

        private final ButtonSide side;
        private final ButtonRow row;

        MainMenuButtonPosition(ButtonSide side, ButtonRow row) {
            this.side = side;
            this.row = row;
        }

        public int getX(int screenWidth) {
            return side == ButtonSide.LEFT ? (screenWidth / 2 - 124) : (screenWidth / 2 + 104);
        }

        public int getY(int screenHeight) {
            return screenHeight / 4 + row.getYOffset();
        }
    }

    public enum PauseMenuButtonPosition {
        LEFT_SAME_ROW(ButtonSide.LEFT, PauseRowOffset.SAME),
        LEFT_NEXT_ROW(ButtonSide.LEFT, PauseRowOffset.NEXT),
        LEFT_2_ROWS_DOWN(ButtonSide.LEFT, PauseRowOffset.TWO_DOWN),
        LEFT_3_ROWS_DOWN(ButtonSide.LEFT, PauseRowOffset.THREE_DOWN),
        LEFT_4_ROWS_DOWN(ButtonSide.LEFT, PauseRowOffset.FOUR_DOWN),
        RIGHT_SAME_ROW(ButtonSide.RIGHT, PauseRowOffset.SAME),
        RIGHT_NEXT_ROW(ButtonSide.RIGHT, PauseRowOffset.NEXT),
        RIGHT_2_ROWS_DOWN(ButtonSide.RIGHT, PauseRowOffset.TWO_DOWN),
        RIGHT_3_ROWS_DOWN(ButtonSide.RIGHT, PauseRowOffset.THREE_DOWN),
        RIGHT_4_ROWS_DOWN(ButtonSide.RIGHT, PauseRowOffset.FOUR_DOWN);

        private final ButtonSide side;
        private final PauseRowOffset rowOffset;

        PauseMenuButtonPosition(ButtonSide side, PauseRowOffset rowOffset) {
            this.side = side;
            this.rowOffset = rowOffset;
        }

        public int getX(int baseButtonX, int baseButtonWidth) {
            return side == ButtonSide.LEFT ? (baseButtonX - 25) : (baseButtonX + baseButtonWidth + 5);
        }

        public int getY(int baseButtonY) {
            return baseButtonY + rowOffset.getYOffset();
        }
    }

    private enum ButtonSide {
        LEFT, RIGHT
    }

    private enum ButtonRow {
        SINGLEPLAYER(24),
        MULTIPLAYER(48),
        REALMS(72),
        MODS(96);

        private final int yOffset;

        ButtonRow(int yOffset) {
            this.yOffset = yOffset;
        }

        int getYOffset() {
            return yOffset;
        }
    }

    private enum PauseRowOffset {
        SAME(0),
        NEXT(24),
        TWO_DOWN(48),
        THREE_DOWN(72),
        FOUR_DOWN(96);

        private final int yOffset;

        PauseRowOffset(int yOffset) {
            this.yOffset = yOffset;
        }

        int getYOffset() {
            return yOffset;
        }
    }

    public int gridWidth = 16;
    public int gridHeight = 16;
    public int mines = 40;

    public boolean quickRestartOnLose = true;

    public boolean enableAnimations = true;

    public int wins = 0;

    public MainMenuButtonPosition mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;

    public PauseMenuButtonPosition pauseMenuButtonPosition = PauseMenuButtonPosition.RIGHT_NEXT_ROW;
}
