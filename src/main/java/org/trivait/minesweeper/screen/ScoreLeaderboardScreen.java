package org.trivait.minesweeper.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.leaderboard.BoardCategory;
import org.trivait.minesweeper.leaderboard.LeaderboardCache;
import org.trivait.minesweeper.leaderboard.SheetsApi;
import org.trivait.minesweeper.screen.widget.LeaderboardWidget;
import org.trivait.minesweeper.screen.widget.ScoreboardVersionWidget;

public class ScoreLeaderboardScreen extends Screen {
    private final Screen parent;

    private LeaderboardWidget leaderboard;
    private BoardCategory boardCategory = BoardCategory.S8x8;
    private LeaderboardCache CACHE = new LeaderboardCache();

    private ButtonWidget c8x8Button;
    private ButtonWidget c16x16Button;
    private ButtonWidget c26x18Button;

    private ButtonWidget playButton;

    private ScoreboardVersionWidget versionWidget;

    public ScoreLeaderboardScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
        SheetsApi.fetchScriptVersionAsync();
    }

    @Override
    protected void init() {
        this.leaderboard = new LeaderboardWidget(10, 10, width/2+125, height-20, CACHE, GameMode.LEADERBOARD_WIN_COUNT, boardCategory);
        this.versionWidget = new ScoreboardVersionWidget(width-5-26, 5);

        this.c8x8Button = ButtonWidget.builder(Text.literal("8x8"), b -> {
            boardCategory = BoardCategory.S8x8;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.of(Text.translatable("leaderboard.mines").append(Text.literal("" + BoardCategory.S8x8.mines)))).dimensions(width/2+125+(((width/2-125)-100)/2), height/2-10-10-20-20, 100, 20).build();
        this.addDrawableChild(c8x8Button);

        c8x8Button.active=false;

        this.c16x16Button = ButtonWidget.builder(Text.literal("16x16"), b -> {
            boardCategory = BoardCategory.S16x16;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c8x8Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.of(Text.translatable("leaderboard.mines").append(Text.literal("" + BoardCategory.S16x16.mines)))).dimensions(width/2+125+(((width/2-125)-100)/2), height/2-10-20, 100, 20).build();
        this.addDrawableChild(c16x16Button);

        this.c26x18Button = ButtonWidget.builder(Text.literal("26x18"), b -> {
            boardCategory = BoardCategory.S26x18;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c8x8Button.active = true;
        }).tooltip(Tooltip.of(Text.translatable("leaderboard.mines").append(Text.literal("" + BoardCategory.S26x18.mines)))).dimensions(width/2+125+(((width/2-125)-100)/2), height/2, 100, 20).build();
        this.addDrawableChild(c26x18Button);

        this.playButton = ButtonWidget.builder(Text.translatable("leaderboard.play").setStyle(Style.EMPTY.withBold(true)), button -> {
            client.setScreen(new LeaderboardMinesweeperScreen(
                    boardCategory.toGameSettings(),
                    MinesweeperModClient.CONFIG.enableAnimations,
                    GameMode.LEADERBOARD_WIN_COUNT,
                    boardCategory
            ));
        }).dimensions(width/2+125+(((width/2-125)-100)/2)-2, height/2+30-2, 104, 24).build();
        this.addDrawableChild(playButton);

        this.addDrawableChild(leaderboard);
        this.addDrawableChild(versionWidget);
        refresh();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 294) {
            refresh();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void refresh() {
        CACHE.invalidate(GameMode.LEADERBOARD_WIN_COUNT);
        CACHE.refreshIfNeeded(GameMode.LEADERBOARD_WIN_COUNT, () -> {});
    }

    @Override
    public void close() {
        super.close();
        this.client.setScreen(parent);
    }
}
