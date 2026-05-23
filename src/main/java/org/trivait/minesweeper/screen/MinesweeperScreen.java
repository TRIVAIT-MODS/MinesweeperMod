package org.trivait.minesweeper.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;

import java.util.*;
import java.util.function.Function;

public class MinesweeperScreen extends Screen {

    private static final Identifier TEX_FLAG = Identifier.fromNamespaceAndPath(MinesweeperModClient.MOD_ID, "textures/gui/flag.png");
    private static final Identifier TEX_BARRIER = Identifier.withDefaultNamespace("textures/item/barrier.png");
    private static final Identifier TEX_TNT_SIDE = Identifier.withDefaultNamespace("textures/block/tnt_side.png");

    private static final Identifier TEX_SMILE_PLAYING = Identifier.fromNamespaceAndPath(MinesweeperModClient.MOD_ID, "textures/gui/smiley_playing.png");
    private static final Identifier TEX_SMILE_WIN = Identifier.fromNamespaceAndPath(MinesweeperModClient.MOD_ID, "textures/gui/smiley_win.png");
    private static final Identifier TEX_SMILE_LOSE = Identifier.fromNamespaceAndPath(MinesweeperModClient.MOD_ID, "textures/gui/smiley_lose.png");
    private static final Identifier TEX_SMILE_HOVER = Identifier.fromNamespaceAndPath(MinesweeperModClient.MOD_ID, "textures/gui/smiley_hover.png");
    private static final Component[] ADJ_TEXT = new Component[]{
            Component.empty(),
            Component.literal("1").withStyle(s -> s.withBold(true)),
            Component.literal("2").withStyle(s -> s.withBold(true)),
            Component.literal("3").withStyle(s -> s.withBold(true)),
            Component.literal("4").withStyle(s -> s.withBold(true)),
            Component.literal("5").withStyle(s -> s.withBold(true)),
            Component.literal("6").withStyle(s -> s.withBold(true)),
            Component.literal("7").withStyle(s -> s.withBold(true)),
            Component.literal("8").withStyle(s -> s.withBold(true))
    };

    private static final RenderPipeline RENDER_PIPELINES = RenderPipelines.GUI_TEXTURED;

    private static class Cell {
        boolean mine, revealed, flagged;
        int adjacent;
        float revealProgress = -1f;
        int delayTicks = -1;
        boolean scheduled = false;
    }

    private class SmileyButtonWidget extends AbstractWidget {
        private final Runnable onPress;
        private boolean pressed = false;

        public SmileyButtonWidget(int x, int y, int width, int height, Runnable onPress) {
            super(x, y, width, height, Component.empty());
            this.onPress = onPress;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float a) {
            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;

            boolean hovered = this.isHovered();

            int border = hovered ? 0xFFB5B5B5 : 0xFF7A7A7A;
            int bg = hovered ? 0xFF3A3A3A : 0xFF2E2E2E;
            if (pressed) {
                border = 0xFF5E5E5E;
                bg = 0xFF232323;
            }

            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
            context.fill(x, y, x + w, y + h, bg);

            Identifier tex;
            if (hovered) {
                tex = TEX_SMILE_HOVER;
            } else if (!alive) {
                tex = TEX_SMILE_LOSE;
            } else if (won) {
                tex = TEX_SMILE_WIN;
            } else {
                tex = TEX_SMILE_PLAYING;
            }

            int pad = Math.max(1, Math.min(3, Math.min(w, h) / 10));
            int ix = x + pad + (pressed ? 1 : 0);
            int iy = y + pad + (pressed ? 1 : 0);
            int iw = Math.max(1, w - pad * 2);
            int ih = Math.max(1, h - pad * 2);
            context.blit(RENDER_PIPELINES, tex, ix, iy, 0, 0, iw, ih, iw, ih);
        }

        @Override
        public void onClick(MouseButtonEvent click, boolean doubled) {
            if (this.active) {
                this.onPress.run();
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (!this.active || click.button() != 0) return false;
            if (!this.isMouseOver(click.x(), click.y())) return false;
            this.pressed = true;
            super.mouseClicked(click, doubled);
            return true;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent click) {
            if (click.button() == 0) {
                this.pressed = false;
            }
            return super.mouseReleased(click);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {

        }
    }

    private final Minecraft mc = Minecraft.getInstance();
    private Cell[][] grid;
    private int w, h, mines;

    private boolean minesPlaced = false;
    private int remainingSafe = -1;

    private int flaggedCount = 0;

    private boolean alive = true;
    private boolean won = false;
    private boolean firstClick = true;

    private boolean timerRunning = false;
    private long timerStartMs = 0L;
    private int elapsedSeconds = 0;

    private int cellSize = 24;
    private int gridX, gridY;

    private int topBarX, topBarY, topBarW;
    private int topBarH = 28;

    private Button backBtn;
    private SmileyButtonWidget smileyBtn;

    private final Random rng = new Random();

    private int[] activeCells = new int[256];
    private int activeCount = 0;

    public MinesweeperScreen(Component title) {
        super(title);
    }

    private boolean tryRestoreSavedGame() {
        MinesweeperModClient.SavedGame sg = MinesweeperModClient.getSavedGame();
        if (sg == null) return false;

        Config cfg = MinesweeperModClient.getConfig();
        int cw = Math.max(5, Math.min(cfg.gridWidth, 40));
        int ch = Math.max(5, Math.min(cfg.gridHeight, 40));
        int maxMines = Math.max(1, (cw * ch) - 1);
        int cm = Math.max(1, Math.min(cfg.mines, maxMines));

        if (sg.w != cw || sg.h != ch || sg.mines != cm) return false;
        int len = sg.w * sg.h;
        if (sg.mine == null || sg.mine.length != len) return false;
        if (sg.revealed == null || sg.revealed.length != len) return false;
        if (sg.flagged == null || sg.flagged.length != len) return false;
        if (sg.adjacent == null || sg.adjacent.length != len) return false;

        this.w = sg.w;
        this.h = sg.h;
        this.mines = sg.mines;

        grid = new Cell[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                Cell c = new Cell();
                c.mine = sg.mine[idx];
                c.revealed = sg.revealed[idx];
                c.flagged = sg.flagged[idx];
                c.adjacent = sg.adjacent[idx];
                c.revealProgress = (sg.revealProgress != null && sg.revealProgress.length == len) ? sg.revealProgress[idx] : (c.revealed ? 1f : -1f);
                c.delayTicks = (sg.delayTicks != null && sg.delayTicks.length == len) ? sg.delayTicks[idx] : -1;
                c.scheduled = (sg.scheduled != null && sg.scheduled.length == len) && sg.scheduled[idx];
                grid[y][x] = c;
            }
        }

        minesPlaced = sg.minesPlaced;
        remainingSafe = sg.remainingSafe;
        alive = sg.alive;
        won = sg.won;
        firstClick = sg.firstClick;

        int flags = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x].flagged) flags++;
            }
        }
        flaggedCount = flags;

        elapsedSeconds = Math.max(0, Math.min(999, sg.elapsedSeconds));
        timerRunning = sg.timerRunning && alive && !won;
        timerStartMs = System.currentTimeMillis() - (elapsedSeconds * 1000L);

        activeCount = 0;
        return true;
    }

    private void saveGameState() {
        if (grid == null) return;
        MinesweeperModClient.SavedGame sg = new MinesweeperModClient.SavedGame();
        sg.w = w;
        sg.h = h;
        sg.mines = mines;

        sg.minesPlaced = minesPlaced;
        sg.remainingSafe = remainingSafe;
        sg.alive = alive;
        sg.won = won;
        sg.firstClick = firstClick;

        sg.timerRunning = timerRunning;
        sg.elapsedSeconds = elapsedSeconds;

        int len = w * h;
        sg.mine = new boolean[len];
        sg.revealed = new boolean[len];
        sg.flagged = new boolean[len];
        sg.adjacent = new int[len];
        sg.revealProgress = new float[len];
        sg.delayTicks = new int[len];
        sg.scheduled = new boolean[len];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                Cell c = grid[y][x];
                sg.mine[idx] = c.mine;
                sg.revealed[idx] = c.revealed;
                sg.flagged[idx] = c.flagged;
                sg.adjacent[idx] = c.adjacent;
                sg.revealProgress[idx] = c.revealProgress;
                sg.delayTicks[idx] = c.delayTicks;
                sg.scheduled[idx] = c.scheduled;
            }
        }

        MinesweeperModClient.setSavedGame(sg);
    }

    @Override
    protected void init() {
        boolean restored = tryRestoreSavedGame();
        if (!restored) {
            Config cfg = MinesweeperModClient.getConfig();
            this.w = Math.max(5, Math.min(cfg.gridWidth, 40));
            this.h = Math.max(5, Math.min(cfg.gridHeight, 40));
            int maxMines = Math.max(1, (w * h) - 1);
            this.mines = Math.max(1, Math.min(cfg.mines, maxMines));
        }

        int marginTop = 40 + topBarH + 6;
        int marginBottom = 50;
        int marginSides = 20;
        int availW = Math.max(1, this.width - marginSides * 2);
        int availH = Math.max(1, this.height - (marginTop + marginBottom));
        int maxCellW = Math.max(8, availW / w);
        int maxCellH = Math.max(8, availH / h);
        this.cellSize = Math.min(Math.min(maxCellW, maxCellH), 48);

        if (!restored) {
            grid = new Cell[h][w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    grid[y][x] = new Cell();
                }
            }
        }

        int totalW = w * cellSize;
        int totalH = h * cellSize;
        gridX = (this.width - totalW) / 2;
        gridY = Math.max(marginTop, (this.height - totalH) / 2);

        topBarW = totalW;
        topBarX = gridX;
        topBarY = Math.max(10, gridY - topBarH - 6);

        if (!restored) {
            alive = true;
            won = false;
            firstClick = true;
            minesPlaced = false;
            remainingSafe = -1;
            activeCount = 0;

            flaggedCount = 0;

            timerRunning = false;
            timerStartMs = 0L;
            elapsedSeconds = 0;
        } else {
            if (!alive || won) {
                timerRunning = false;
            }
        }

        this.clearWidgets();

        int backW = 60;
        int backH = 20;
        int backX = 8;
        int backY = 8;
        backBtn = Button.builder(Component.translatable("gui.back"), b -> this.onClose())
                .bounds(backX, backY, backW, backH).build();
        this.addRenderableWidget(backBtn);

        int smileSize = Math.max(18, Math.min(26, topBarH - 2));
        int smileX = this.width / 2 - (smileSize / 2);
        int smileY = topBarY + (topBarH - smileSize) / 2;
        smileyBtn = new SmileyButtonWidget(smileX, smileY, smileSize, smileSize, () -> {
            MinesweeperModClient.setSavedGame(null);
            MinesweeperScreen.this.init();
        });
        this.addRenderableWidget(smileyBtn);
    }

    @Override
    public void onClose() {
        saveGameState();
        super.onClose();
    }

    private void placeMinesAvoiding(int avoidX, int avoidY) {
        int placed = 0;
        while (placed < mines) {
            int x = rng.nextInt(w), y = rng.nextInt(h);
            if ((x == avoidX && y == avoidY) || grid[y][x].mine) continue;
            grid[y][x].mine = true;
            placed++;
        }

        minesPlaced = true;
        remainingSafe = (w * h) - mines;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x].mine) {
                    grid[y][x].adjacent = -1;
                } else {
                    int count = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx, ny = y + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h && grid[ny][nx].mine) count++;
                        }
                    }
                    grid[y][x].adjacent = count;
                }
            }
        }
    }

    private void drawTopBar(GuiGraphicsExtractor context) {
        int bg = 0xFF2B2B2B;
        int border = 0xFF555555;

        int barX1 = topBarX;
        int barY1 = topBarY;
        int barX2 = topBarX + topBarW;
        int barY2 = topBarY + topBarH;

        int smileX1 = smileyBtn != null ? smileyBtn.getX() : (this.width / 2);
        int smileY1 = smileyBtn != null ? smileyBtn.getY() : topBarY;
        int smileX2 = smileyBtn != null ? (smileyBtn.getX() + smileyBtn.getWidth()) : (this.width / 2);
        int smileY2 = smileyBtn != null ? (smileyBtn.getY() + smileyBtn.getHeight()) : (topBarY + topBarH);

        // Fill the bar but avoid covering the smiley button area.
        int holeX1 = Math.max(barX1, Math.min(smileX1, smileX2));
        int holeX2 = Math.min(barX2, Math.max(smileX1, smileX2));
        int holeY1 = Math.max(barY1, Math.min(smileY1, smileY2));
        int holeY2 = Math.min(barY2, Math.max(smileY1, smileY2));

        // Left + right of smiley (full height)
        context.fill(barX1, barY1, holeX1, barY2, bg);
        context.fill(holeX2, barY1, barX2, barY2, bg);

        // Above + below smiley (only between smiley X-range)
        context.fill(holeX1, barY1, holeX2, holeY1, bg);
        context.fill(holeX1, holeY2, holeX2, barY2, bg);
        context.fill(topBarX, topBarY, topBarX + topBarW, topBarY + 1, border);
        context.fill(topBarX, topBarY + topBarH - 1, topBarX + topBarW, topBarY + topBarH, border);
        context.fill(topBarX, topBarY, topBarX + 1, topBarY + topBarH, border);
        context.fill(topBarX + topBarW - 1, topBarY, topBarX + topBarW, topBarY + topBarH, border);

        int minesLeft = Math.max(0, mines - flaggedCount);

        int digitH = topBarH - 10;
        int digitW = Math.max(10, digitH / 2);
        int padX = 6;
        int y = topBarY + (topBarH - digitH) / 2;

        drawSevenSegNumber(context, topBarX + padX, y, digitW, digitH, minesLeft, 3);
        drawSevenSegNumber(context, topBarX + topBarW - padX - (digitW * 3 + 2 * 2), y, digitW, digitH, elapsedSeconds, 3);
    }

    private void drawSevenSegNumber(GuiGraphicsExtractor context, int x, int y, int digitW, int digitH, int value, int digits) {
        int v = Math.max(0, Math.min(999, value));
        int[] out = new int[digits];
        for (int i = digits - 1; i >= 0; i--) {
            out[i] = v % 10;
            v /= 10;
        }

        int gap = 2;
        for (int i = 0; i < digits; i++) {
            drawSevenSegDigit(context, x + i * (digitW + gap), y, digitW, digitH, out[i]);
        }
    }

    private void drawSevenSegDigit(GuiGraphicsExtractor context, int x, int y, int w, int h, int d) {
        int on = 0xFFFF2D2D;
        int off = 0xFF3A0C0C;
        int bg = 0xFF121212;

        context.fill(x - 2, y - 2, x + w + 2, y + h + 2, bg);

        int t = Math.max(2, Math.min(4, h / 6));
        int inset = Math.max(0, t / 2);
        int midY = y + h / 2;

        boolean a = d != 1 && d != 4;
        boolean b = d != 5 && d != 6;
        boolean c = d != 2;
        boolean dSeg = d != 1 && d != 4 && d != 7;
        boolean e = d == 0 || d == 2 || d == 6 || d == 8;
        boolean f = d != 1 && d != 2 && d != 3 && d != 7;
        boolean g = d != 0 && d != 1 && d != 7;

        fillSeg(context, x + inset, y, x + w - inset, y + t, a ? on : off);
        fillSeg(context, x + w - t, y + inset, x + w, midY - t / 2, b ? on : off);
        fillSeg(context, x + w - t, midY + t / 2, x + w, y + h - inset, c ? on : off);
        fillSeg(context, x + inset, y + h - t, x + w - inset, y + h, dSeg ? on : off);
        fillSeg(context, x, midY + t / 2, x + t, y + h - inset, e ? on : off);
        fillSeg(context, x, y + inset, x + t, midY - t / 2, f ? on : off);
        fillSeg(context, x + inset, midY - t / 2, x + w - inset, midY + t / 2, g ? on : off);
    }

    private void fillSeg(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (!alive) {
            if (MinesweeperModClient.getConfig().quickRestartOnLose && button == 0) {
                MinesweeperModClient.setSavedGame(null);
                this.init();
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        int x = (int) Math.floor((mouseX - gridX) / (double) cellSize);
        int y = (int) Math.floor((mouseY - gridY) / (double) cellSize);
        if (x < 0 || x >= w || y < 0 || y >= h) return super.mouseClicked(click, doubled);

        mc.getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.NOTE_BLOCK_HAT.value(), 0.20f, 1.0f));

        Cell c = grid[y][x];

        if (button == 1) {
            if (!c.revealed) {
                boolean next = !c.flagged;
                c.flagged = next;
                flaggedCount += next ? 1 : -1;
                if (flaggedCount < 0) flaggedCount = 0;
            }
            saveGameState();
            return true;
        }

        if (button == 0) {
            if (firstClick) {
                placeMinesAvoiding(x, y);
                firstClick = false;

                timerRunning = true;
                timerStartMs = System.currentTimeMillis();
                elapsedSeconds = 0;
            }
            if (!c.flagged && !c.revealed) {
                if (!MinesweeperModClient.getConfig().enableAnimations) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(
                            SoundEvents.DEEPSLATE_BREAK, 0.25f, 1.0f));
                }
                startRevealWave(x, y);
                if (!alive) return true;
            }
            saveGameState();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void addActiveCell(int idx) {
        if (activeCount >= activeCells.length) {
            activeCells = Arrays.copyOf(activeCells, activeCells.length * 2);
        }
        activeCells[activeCount++] = idx;
    }

    private Cell cellAtIndex(int idx) {
        int y = idx / w;
        int x = idx - (y * w);
        return grid[y][x];
    }

    private void revealCell(int x, int y) {
        Cell c = grid[y][x];
        if (c.revealed) return;
        c.revealed = true;
        boolean anims = MinesweeperModClient.getConfig().enableAnimations;
        c.revealProgress = anims ? 0f : 1f;
        c.delayTicks = -1;
        addActiveCell(y * w + x);

        if (anims) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.DEEPSLATE_BREAK, 0.25f, 1.0f));
        }

        if (!c.mine) {
            remainingSafe--;
            if (remainingSafe <= 0) {
                handleWin();
            }
        }
    }

    private void startRevealWave(int sx, int sy) {
        Queue<int[]> queue = new ArrayDeque<>();
        Cell start = grid[sy][sx];
        boolean anims = MinesweeperModClient.getConfig().enableAnimations;
        if (start.mine) {
            start.revealed = true;
            alive = false;
            timerRunning = false;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.7f, 1.0f));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.7f, 1.0f));
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    if (grid[yy][xx].mine) grid[yy][xx].revealed = true;
                }
            }
            return;
        }

        if (anims) {
            start.scheduled = true;
            start.delayTicks = 0;
            addActiveCell(sy * w + sx);
            queue.add(new int[]{sx, sy});

            while (!queue.isEmpty()) {
                int[] pos = queue.poll();
                int cx = pos[0], cy = pos[1];
                Cell c = grid[cy][cx];
                if (c.adjacent == 0) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = cx + dx, ny = cy + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                                Cell n = grid[ny][nx];
                                if (!n.scheduled && !n.flagged && !n.mine) {
                                    n.scheduled = true;
                                    n.delayTicks = c.delayTicks + 3;
                                    addActiveCell(ny * w + nx);
                                    queue.add(new int[]{nx, ny});
                                }
                            }
                        }
                    }
                }
            }
        } else {
            queue.add(new int[]{sx, sy});
            while (!queue.isEmpty()) {
                int[] pos = queue.poll();
                int cx = pos[0], cy = pos[1];
                Cell c = grid[cy][cx];

                if (c.revealed) continue;
                if (c.flagged || c.mine) continue;

                revealCell(cx, cy);

                if (c.adjacent == 0) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = cx + dx, ny = cy + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                                Cell n = grid[ny][nx];
                                if (!n.revealed && !n.flagged && !n.mine) {
                                    queue.add(new int[]{nx, ny});
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleWin() {
        if (!alive || won) return;
        won = true;

        timerRunning = false;

        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                Cell c = grid[yy][xx];
                if (!c.mine) {
                    c.revealed = true;
                    c.revealProgress = 1f;
                    c.delayTicks = -1;
                }
            }
        }

        mc.getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.FIREWORK_ROCKET_BLAST, 0.8f, 1.0f));
        MinesweeperModClient.incrementWins();
        saveGameState();
    }

    @Override
    public void tick() {
        if (timerRunning) {
            int sec = (int) ((System.currentTimeMillis() - timerStartMs) / 1000L);
            elapsedSeconds = Math.min(999, Math.max(0, sec));
        }
        if (!MinesweeperModClient.getConfig().enableAnimations) {
            return;
        }
        for (int i = 0; i < activeCount; ) {
            int idx = activeCells[i];
            Cell c = cellAtIndex(idx);

            boolean keep = false;

            if (c.delayTicks > 0) {
                c.delayTicks--;
                keep = true;
            } else if (c.delayTicks == 0 && !c.revealed && c.scheduled) {
                int y = idx / w;
                int x = idx - (y * w);
                revealCell(x, y);
                c.scheduled = false;
                keep = true;
            }

            if (c.revealProgress >= 0f && c.revealProgress < 1f) {
                c.revealProgress = Math.min(1f, c.revealProgress + 0.2f);
                keep = true;
            }

            if (!keep) {
                activeCells[i] = activeCells[activeCount - 1];
                activeCount--;
                continue;
            }

            i++;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        drawTopBar(context);

        double scaleFactor = mc.getWindow().getGuiScale();
        boolean anims = MinesweeperModClient.getConfig().enableAnimations;
        float uiScale = cellSize / 24f;
        float textScale = Math.max(0.70f, Math.min(1.30f, uiScale * 1.05f));
        int texSizeAdjust = Math.max(10, Math.min(cellSize, (int) (cellSize * 0.78f)));
        int border = 0xFF555555;

        boolean isHighScale = scaleFactor >= 3;
        float p = isHighScale ? (float) (1.0 / scaleFactor) : 1.0f;

        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int x = gridX + xx * cellSize;
                int y = gridY + yy * cellSize;
                Cell c = grid[yy][xx];

                boolean hovered = mouseX >= x && mouseX < x + cellSize && mouseY >= y && mouseY < y + cellSize;
                boolean showRevealedBg = c.revealed && !(c.flagged && !alive && !won);
                int bg = showRevealedBg ? 0xFF2D2D2D : 0xFF454545;
                if (hovered && !showRevealedBg) bg = brighten(bg, 0.10f);

                context.fill(x, y, x + cellSize, y + cellSize, bg);

                if (isHighScale) {
                    context.pose().pushMatrix();
                    context.pose().scale(p, p);
                    int px1 = (int) Math.round(x * scaleFactor);
                    int py1 = (int) Math.round(y * scaleFactor);
                    int px2 = (int) Math.round((x + cellSize) * scaleFactor);
                    int py2 = (int) Math.round((y + cellSize) * scaleFactor);
                    context.fill(px1, py1, px2, py1 + 1, border);
                    context.fill(px1, py2 - 1, px2, py2, border);
                    context.fill(px1, py1, px1 + 1, py2, border);
                    context.fill(px2 - 1, py1, px2, py2, border);
                    context.pose().popMatrix();
                } else {
                    context.fill(x, y, x + cellSize, y + 1, border);
                    context.fill(x, y + cellSize - 1, x + cellSize, y + cellSize, border);
                    context.fill(x, y, x + 1, y + cellSize, border);
                    context.fill(x + cellSize - 1, y, x + cellSize, y + cellSize, border);
                }

                float contentScale = 1.0f;
                if (anims && c.revealProgress >= 0f && c.revealProgress < 1f) {
                    float ph = c.revealProgress < 0.5f ? (c.revealProgress / 0.5f) : ((1f - c.revealProgress) / 0.5f);
                    contentScale = 1.0f + ph * 0.20f;
                }

                boolean hasDraw = c.revealed || c.flagged;
                if (!hasDraw) continue;

                int cx = x + cellSize / 2;
                int cy = y + cellSize / 2;

                context.pose().pushMatrix();
                context.pose().translate((float) cx, (float) cy);
                if (contentScale != 1.0f) context.pose().scale(contentScale, contentScale);
                context.pose().translate((float) -cx, (float) -cy);

                if (c.revealed) {
                    if (c.mine && !won) {
                        int ox = cx - texSizeAdjust / 2, oy = cy - texSizeAdjust / 2;
                        context.blit(RENDER_PIPELINES, c.flagged && !alive ? TEX_FLAG : TEX_TNT_SIDE, ox, oy, 0, 0, texSizeAdjust, texSizeAdjust, texSizeAdjust, texSizeAdjust);
                    } else if (c.adjacent > 0) {
                        Component numText = ADJ_TEXT[c.adjacent];
                        int color = getAdjColor(c.adjacent);
                        context.pose().pushMatrix();
                        context.pose().translate((float) cx, (float) cy);
                        if (textScale != 1.0f) context.pose().scale(textScale, textScale);
                        context.pose().translate(-mc.font.width(numText) / 2f, -mc.font.lineHeight / 2f);
                        context.text(mc.font, numText, 0, 0, color, false);
                        context.pose().popMatrix();
                    }
                } else if (c.flagged) {
                    int ox = cx - texSizeAdjust / 2, oy = cy - texSizeAdjust / 2;
                    context.blit(RENDER_PIPELINES, !alive && !won && !c.mine ? TEX_BARRIER : TEX_FLAG, ox, oy, 0, 0, texSizeAdjust, texSizeAdjust, texSizeAdjust, texSizeAdjust);
                }
                context.pose().popMatrix();
            }
        }

    }

    private int getAdjColor(int adj) {
        return switch (adj) {
            case 1 -> 0xFF3EB2FF;
            case 2 -> 0xFF41D45E;
            case 3 -> 0xFFFF4E4E;
            case 4 -> 0xFF7757FF;
            case 5 -> 0xFFFFA84E;
            case 6 -> 0xFF4EE0FF;
            case 7 -> 0xFFFFFFFF;
            default -> 0xFFBBBBBB;
        };
    }

    private static int brighten(int color, float amount) {
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;
        r = Math.min(255, (int) (r * (1f + amount)));
        g = Math.min(255, (int) (g * (1f + amount)));
        b = Math.min(255, (int) (b * (1f + amount)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
