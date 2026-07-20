package org.trivait.minesweeper.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.game.GameBoard;
import org.trivait.minesweeper.game.GameSettings;
import org.trivait.minesweeper.game.SavedGame;
import org.trivait.minesweeper.leaderboard.BoardCategory;
import org.trivait.minesweeper.leaderboard.SheetsApi;

public class LeaderboardMinesweeperScreen extends MinesweeperScreen {

    private final GameMode lbMode;
    private final BoardCategory category;
    private final String playerName;
    private long timerStartMs = 0;
    private boolean lbTimerRunning = false;
    private int elapsedTenths = 0;
    private boolean resultSubmitted = false;

    private int winCount = 0;

    public LeaderboardMinesweeperScreen(GameSettings settings, boolean animations,
                                        GameMode lbMode, BoardCategory category) {
        super(settings, animations, lbMode);
        this.lbMode = lbMode;
        this.category = category;
        this.playerName = Minecraft.getInstance().getUser().getName();
    }

    @Override
    protected void resetGame() {
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            lbTimerRunning = false;
            elapsedTenths = 0;
            resultSubmitted = false;
        }
        super.resetGame();
    }

    @Override
    protected GameBoard.SoundCallback makeSoundCallback() {
        GameBoard.SoundCallback base = super.makeSoundCallback();
        return new GameBoard.SoundCallback() {
            @Override public void onReveal() { base.onReveal(); }

            @Override
            public void onExplode(int cellX, int cellY) {
                base.onExplode(cellX, cellY);
                if (lbMode == GameMode.LEADERBOARD_TIME && lbTimerRunning) {
                    elapsedTenths = (int) ((System.currentTimeMillis() - timerStartMs) / 100);
                    lbTimerRunning = false;
                }
            }

            @Override
            public void onWin() {
                base.onWin();
                handleWin();
            }
        };
    }

    private void handleWin() {
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            if (lbTimerRunning) {
                elapsedTenths = (int) ((System.currentTimeMillis() - timerStartMs) / 100);
                lbTimerRunning = false;
            }
            if (!resultSubmitted) {
                resultSubmitted = true;
                SheetsApi.submitTimeAsync(playerName, (elapsedTenths + 5) / 10, category);
            }
        } else if (lbMode == GameMode.LEADERBOARD_WIN_COUNT) {
            winCount++;
            SheetsApi.submitScoreAsync(playerName, 1, category);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (board == null) return;

        if (lbMode == GameMode.LEADERBOARD_TIME) {
            if (board.timerRunning && !lbTimerRunning && !resultSubmitted) {
                timerStartMs = System.currentTimeMillis();
                lbTimerRunning = true;
            }
            if (lbTimerRunning) {
                elapsedTenths = (int) ((System.currentTimeMillis() - timerStartMs) / 100);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        drawTopCounter(ctx);
    }

    private void drawTopCounter(GuiGraphicsExtractor ctx) {
        String text;
        if (lbMode == GameMode.LEADERBOARD_TIME) {
            int secs = elapsedTenths / 10;
            int tenths = elapsedTenths % 10;
            text = secs + "." + tenths;
        } else {
            text = String.valueOf(winCount);
        }

        Component label = Component.literal(text);
        int tw = font.width(label);

        ctx.pose().pushMatrix();
        ctx.pose().translate(width / 2f, 4f);
        ctx.pose().scale(2f, 2f);
        ctx.pose().translate(-tw / 2f, 0f);
        ctx.text(font, label, 0, 0, 0xFFFFFFFF, true);
        ctx.pose().popMatrix();
    }

    @Override
    public void onClose() {
        super.onClose();
        Config cfg = MinesweeperModClient.CONFIG;
        SavedGame saved = MinesweeperModClient.getSavedGame();
        if (saved != null) {
            this.minecraft.gui.setScreen(new MinesweeperScreen(saved, cfg.enableAnimations, GameMode.DEFAULT));
        } else {
            this.minecraft.gui.setScreen(new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), cfg.enableAnimations, GameMode.DEFAULT));
        }
    }
}
