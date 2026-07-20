package org.trivait.minesweeper.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;
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

    private Button c8x8Button;
    private Button c16x16Button;
    private Button c26x18Button;

    private Button playButton;

    private ScoreboardVersionWidget versionWidget;

    public ScoreLeaderboardScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
        SheetsApi.fetchScriptVersionAsync();
    }

    @Override
    protected void init() {
        this.leaderboard = new LeaderboardWidget(10, 10, width/2+125, height-20, CACHE, GameMode.LEADERBOARD_WIN_COUNT, boardCategory);
        this.versionWidget = new ScoreboardVersionWidget(width-5-26, 5);

        this.c8x8Button = Button.builder(Component.literal("8x8"), b -> {
            boardCategory = BoardCategory.S8x8;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.create(Component.translatable("leaderboard.mines").append(Component.literal("" + BoardCategory.S8x8.mines)))).bounds(width/2+125+(((width/2-125)-100)/2), height/2-10-10-20-20, 100, 20).build();
        this.addRenderableWidget(c8x8Button);

        c8x8Button.active=false;

        this.c16x16Button = Button.builder(Component.literal("16x16"), b -> {
            boardCategory = BoardCategory.S16x16;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c8x8Button.active = true;
            c26x18Button.active = true;
        }).tooltip(Tooltip.create(Component.translatable("leaderboard.mines").append(Component.literal("" + BoardCategory.S16x16.mines)))).bounds(width/2+125+(((width/2-125)-100)/2), height/2-10-20, 100, 20).build();
        this.addRenderableWidget(c16x16Button);

        this.c26x18Button = Button.builder(Component.literal("26x18"), b -> {
            boardCategory = BoardCategory.S26x18;
            leaderboard.setCategory(boardCategory);
            b.active = false;
            c16x16Button.active = true;
            c8x8Button.active = true;
        }).tooltip(Tooltip.create(Component.translatable("leaderboard.mines").append(Component.literal("" + BoardCategory.S26x18.mines)))).bounds(width/2+125+(((width/2-125)-100)/2), height/2, 100, 20).build();
        this.addRenderableWidget(c26x18Button);

        this.playButton = Button.builder(Component.translatable("leaderboard.play").setStyle(Style.EMPTY.withBold(true)), button -> {
            minecraft.setScreen(new LeaderboardMinesweeperScreen(
                    boardCategory.toGameSettings(),
                    MinesweeperModClient.CONFIG.enableAnimations,
                    GameMode.LEADERBOARD_WIN_COUNT,
                    boardCategory
            ));
        }).bounds(width/2+125+(((width/2-125)-100)/2)-2, height/2+30-2, 104, 24).build();
        this.addRenderableWidget(playButton);

        this.addRenderableWidget(leaderboard);
        this.addRenderableWidget(versionWidget);
        refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_F5) {
            refresh();
        }
        return super.keyPressed(event);
    }

    private void refresh() {
        CACHE.invalidate(GameMode.LEADERBOARD_WIN_COUNT);
        CACHE.refreshIfNeeded(GameMode.LEADERBOARD_WIN_COUNT, () -> {});
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(parent);
    }
}
