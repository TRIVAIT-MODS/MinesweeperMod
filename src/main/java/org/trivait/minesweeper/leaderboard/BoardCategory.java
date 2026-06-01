package org.trivait.minesweeper.leaderboard;

import org.trivait.minesweeper.game.GameSettings;

public enum BoardCategory {
    S8x8("8x8"),
    S16x16("16x16"),
    S26x18("26x18");

    public final String label;

    BoardCategory(String label) { this.label = label; }

    public static BoardCategory from(int w, int h) {
        if (w <= 8  && h <= 8)  return S8x8;
        if (w <= 16 && h <= 16) return S16x16;
        return S26x18;
    }

    public GameSettings toGameSettings() {
        return switch (this) {
            case S8x8   -> new GameSettings(8,  8,  8);
            case S16x16 -> new GameSettings(16, 16, 30);
            case S26x18 -> new GameSettings(26, 18, 65);
        };
    }

    @Override public String toString() { return label; }
}
