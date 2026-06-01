package org.trivait.minesweeper.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.leaderboard.BoardCategory;
import org.trivait.minesweeper.leaderboard.LeaderboardCache;

public class TimeLeaderboardScreen extends Screen {
    private final Screen parent;

    private LeaderboardWidget leaderboard;
    private BoardCategory boardCategory = BoardCategory.S8x8;
    private LeaderboardCache CACHE = new LeaderboardCache();

    private Button c8x8Button;
    private Button c16x16Button;
    private Button c26x18Button;

    private Button playButton;

    public TimeLeaderboardScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.leaderboard = new LeaderboardWidget(10, 10, width/2+125, height-20, CACHE, GameMode.LEADERBOARD_TIME, boardCategory);

        this.c8x8Button = Button.builder(Component.literal("8x8"), b -> {
            boardCategory = BoardCategory.S8x8;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c26x18Button.active = true;
        }).bounds(width/2+((width/2-15)/2), height/2-10-10-20-20, 100, 20).build();
        this.addRenderableWidget(c8x8Button);

        c8x8Button.active=false;

        this.c16x16Button = Button.builder(Component.literal("16x16"), b -> {
            boardCategory = BoardCategory.S16x16;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c8x8Button.active = true;
            c26x18Button.active = true;
        }).bounds(width/2+((width/2-15)/2), height/2-10-20, 100, 20).build();
        this.addRenderableWidget(c16x16Button);

        this.c26x18Button = Button.builder(Component.literal("26x18"), b -> {
            boardCategory = BoardCategory.S26x18;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c8x8Button.active = true;
        }).bounds(width/2+((width/2-15)/2), height/2, 100, 20).build();
        this.addRenderableWidget(c26x18Button);

        this.playButton = Button.builder(Component.translatable("leaderboard.play"), button -> {
            minecraft.setScreen(new LeaderboardMinesweeperScreen(
                    boardCategory.toGameSettings(),
                    MinesweeperModClient.CONFIG.enableAnimations,
                    GameMode.LEADERBOARD_TIME,
                    boardCategory
            ));
        }).bounds(width/2+((width/2-15)/2), height/2+30, 100, 20).build();
        this.addRenderableWidget(playButton);

        this.addRenderableWidget(leaderboard);
        refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == 294) {
            refresh();
        }

        return super.keyPressed(input);
    }

    private void refresh() {
        CACHE.invalidate(GameMode.LEADERBOARD_TIME);
        CACHE.refreshIfNeeded(GameMode.LEADERBOARD_TIME, () -> {});
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(parent);
    }
}
