package org.trivait.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.game.Cell;
import org.trivait.minesweeper.game.GameBoard;
import org.trivait.minesweeper.game.GameSettings;
import org.trivait.minesweeper.game.SavedGame;
import org.trivait.minesweeper.screen.widget.DigitDisplayWidget;
import org.trivait.minesweeper.screen.widget.ExplosionAnimation;
import org.trivait.minesweeper.screen.widget.SmileyButtonWidget;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperScreen extends Screen {

    private static final Identifier TEX_FLAG     = Identifier.of(MinesweeperModClient.MOD_ID, "textures/gui/flag.png");
    private static final Identifier TEX_BARRIER  = Identifier.ofVanilla("textures/item/barrier.png");
    private static final Identifier TEX_TNT_SIDE = Identifier.ofVanilla("textures/block/tnt_side.png");

    private static final Text[] ADJ_TEXT = {
        Text.empty(),
        Text.literal("1").styled(s -> s.withBold(true)),
        Text.literal("2").styled(s -> s.withBold(true)),
        Text.literal("3").styled(s -> s.withBold(true)),
        Text.literal("4").styled(s -> s.withBold(true)),
        Text.literal("5").styled(s -> s.withBold(true)),
        Text.literal("6").styled(s -> s.withBold(true)),
        Text.literal("7").styled(s -> s.withBold(true)),
        Text.literal("8").styled(s -> s.withBold(true)),
    };

    private static final int TOP_BAR_H = 28;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final GameSettings newGameSettings;
    private final SavedGame initialSave;
    private final boolean animations;

    protected GameBoard board;

    private int cellSize;
    private int gridX, gridY;
    private int topBarX, topBarY, topBarW;

    private SmileyButtonWidget smileyBtn;
    private DigitDisplayWidget minesDisplay;
    private DigitDisplayWidget timerDisplay;

    private final List<ExplosionAnimation> explosions = new ArrayList<>();

    public final GameMode gameMode;

    private ButtonWidget leaderboardButton;

    public MinesweeperScreen(GameSettings settings, boolean animations, GameMode gameMode) {
        super(Text.empty());
        this.newGameSettings = settings;
        this.initialSave = null;
        this.animations = animations;
        this.gameMode = gameMode;
    }

    public MinesweeperScreen(SavedGame savedGame, boolean animations, GameMode gameMode) {
        super(Text.empty());
        this.newGameSettings = null;
        this.initialSave = savedGame;
        this.animations = animations;
        this.gameMode = gameMode;
    }

    @Override
    protected void init() {
        if (initialSave != null && board == null) {
            board = new GameBoard(initialSave);
        } else if (board == null) {
            board = new GameBoard(newGameSettings != null ? newGameSettings : defaultSettings());
        }
        board.setSoundCallback(makeSoundCallback());

        int marginTop = 40 + TOP_BAR_H + 6;
        int availW = Math.max(1, this.width - 40);
        int availH = Math.max(1, this.height - (marginTop + 50));
        cellSize = Math.min(Math.min(Math.max(8, availW / board.w), Math.max(8, availH / board.h)), 48);

        int totalW = board.w * cellSize;
        int totalH = board.h * cellSize;
        gridX = (this.width - totalW) / 2;
        gridY = Math.max(marginTop, (this.height - totalH) / 2);
        topBarW = totalW;
        topBarX = gridX;
        topBarY = Math.max(10, gridY - TOP_BAR_H - 6);

        this.clearChildren();

        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.back"), b -> this.close())
                .dimensions(8, 8, 60, 20).build()
        );

        int smileSize = Math.max(18, Math.min(26, TOP_BAR_H - 2));
        smileyBtn = new SmileyButtonWidget(
            this.width / 2 - smileSize / 2,
            topBarY + (TOP_BAR_H - smileSize) / 2,
            smileSize,
            this::resetGame
        );
        this.addDrawableChild(smileyBtn);

        minesDisplay = new DigitDisplayWidget(3);
        timerDisplay = new DigitDisplayWidget(3);
        int dispY = topBarY + (TOP_BAR_H - minesDisplay.getHeight()) / 2;
        minesDisplay.setPosition(topBarX + 6, dispY);
        timerDisplay.setPosition(topBarX + topBarW - 6 - timerDisplay.getWidth(), dispY);

        leaderboardButton = ButtonWidget.builder(Text.translatable("leaderboard.name").setStyle(Style.EMPTY.withBold(true).withColor(Formatting.YELLOW)), (b) -> {
            client.setScreen(new SelectLeaderboardScreen(this));
        }).dimensions(5, height-20-5, 100, 20).build();

        if (gameMode == GameMode.DEFAULT) {
            this.addDrawableChild(leaderboardButton);
        }
    }

    private GameSettings defaultSettings() {
        var cfg = MinesweeperModClient.CONFIG;
        return new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines);
    }

    protected void resetGame() {
        MinesweeperModClient.setSavedGame(null);
        explosions.clear();
        board = new GameBoard(newGameSettings != null ? newGameSettings : defaultSettings());
        board.setSoundCallback(makeSoundCallback());
        this.init();
    }

    protected GameBoard.SoundCallback makeSoundCallback() {
        return new GameBoard.SoundCallback() {
            public void onReveal() {
                mc.getSoundManager().play(PositionedSoundInstance.ui(
                    SoundEvents.BLOCK_DEEPSLATE_BREAK, 0.25f, 1.0f));
            }
            public void onExplode(int cellX, int cellY) {
                mc.getSoundManager().play(PositionedSoundInstance.ui(
                    SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.7f, 1.0f));
                if (MinesweeperModClient.CONFIG.enableExplosionAnimation) {
                    int cx = gridX + cellX * cellSize + cellSize / 2;
                    int cy = gridY + cellY * cellSize + cellSize / 2;
                    explosions.add(new ExplosionAnimation(cx, cy, cellSize * 3));
                }
            }
            public void onWin() {
                mc.getSoundManager().play(PositionedSoundInstance.ui(
                    SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.0f));
            }
        };
    }

    @Override
    public void close() {
        if (board != null) MinesweeperModClient.setSavedGame(board.toSavedGame());
        super.close();
    }

    @Override
    public void tick() {
        if (board == null) return;
        board.tick(animations);
        updateSmileyState();
        explosions.forEach(ExplosionAnimation::tick);
        explosions.removeIf(e -> e.done);
    }

    private void updateSmileyState() {
        if (smileyBtn == null) return;
        if (!board.alive) smileyBtn.setState(SmileyButtonWidget.State.LOSE);
        else if (board.won) smileyBtn.setState(SmileyButtonWidget.State.WIN);
        else smileyBtn.setState(SmileyButtonWidget.State.PLAYING);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (board == null) return super.mouseClicked(click, doubled);

        if (!board.alive) {
            if (click.button() == 0) { resetGame(); return true; }
            return super.mouseClicked(click, doubled);
        }

        int gx = (int) Math.floor((click.x() - gridX) / (double) cellSize);
        int gy = (int) Math.floor((click.y() - gridY) / (double) cellSize);
        if (gx < 0 || gx >= board.w || gy < 0 || gy >= board.h)
            return super.mouseClicked(click, doubled);

        mc.getSoundManager().play(PositionedSoundInstance.ui(
            SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.20f, 1.0f));

        Cell c = board.grid[gy][gx];

        if (click.button() == 1) {
            if (!c.revealed) board.toggleFlag(gx, gy);
            MinesweeperModClient.setSavedGame(board.toSavedGame());
            return true;
        }

        if (click.button() == 0 && !c.flagged && !c.revealed) {
            if (board.firstClick) {
                board.placeMinesAvoiding(gx, gy);
                board.firstClick = false;
                board.timerRunning = true;
                board.timerStartMs = System.currentTimeMillis();
                board.elapsedSeconds = 0;
            }
            if (!animations) {
                mc.getSoundManager().play(PositionedSoundInstance.ui(
                    SoundEvents.BLOCK_DEEPSLATE_BREAK, 0.25f, 1.0f));
            }
            board.startRevealWave(gx, gy, animations);
            MinesweeperModClient.setSavedGame(board.toSavedGame());
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (board == null) return;
        drawTopBar(context);
        drawGrid(context, mouseX, mouseY);
        explosions.forEach(e -> e.render(context));
    }

    private void drawTopBar(DrawContext context) {
        int barY2 = topBarY + TOP_BAR_H;
        int barX2 = topBarX + topBarW;
        int hx1 = smileyBtn.getX(), hx2 = hx1 + smileyBtn.getWidth();
        int hy1 = smileyBtn.getY(), hy2 = hy1 + smileyBtn.getHeight();
        int bg = 0xFF2B2B2B, border = 0xFF555555;

        context.fill(topBarX, topBarY, hx1,  barY2, bg);
        context.fill(hx2,     topBarY, barX2, barY2, bg);
        context.fill(hx1,     topBarY, hx2,   hy1,   bg);
        context.fill(hx1,     hy2,     hx2,   barY2, bg);

        context.fill(topBarX,    topBarY,    barX2,       topBarY + 1, border);
        context.fill(topBarX,    barY2 - 1,  barX2,       barY2,       border);
        context.fill(topBarX,    topBarY,    topBarX + 1, barY2,       border);
        context.fill(barX2 - 1,  topBarY,    barX2,       barY2,       border);

        minesDisplay.setValue(Math.max(0, board.mines - board.flaggedCount));
        timerDisplay.setValue(board.elapsedSeconds);
        minesDisplay.render(context);
        timerDisplay.render(context);
    }

    private void drawGrid(DrawContext context, int mouseX, int mouseY) {
        double scaleFactor = mc.getWindow().getScaleFactor();
        float uiScale = cellSize / 24f;
        float textScale = Math.max(0.70f, Math.min(1.30f, uiScale * 1.05f));
        int texSize = Math.max(10, Math.min(cellSize, (int) (cellSize * 0.78f)));
        int border = 0xFF555555;
        boolean highScale = scaleFactor >= 3;
        float invScale = (float) (1.0 / scaleFactor);
        int half = cellSize / 2;

        for (int yy = 0; yy < board.h; yy++) {
            for (int xx = 0; xx < board.w; xx++) {
                int x = gridX + xx * cellSize;
                int y = gridY + yy * cellSize;
                int cx = x + half, cy = y + half;
                Cell c = board.grid[yy][xx];

                boolean hovered = mouseX >= x && mouseX < x + cellSize
                               && mouseY >= y && mouseY < y + cellSize;
                boolean revealedBg = c.revealed && !(c.flagged && !board.alive && !board.won);
                int bg = revealedBg ? 0xFF2D2D2D : 0xFF454545;
                if (hovered && !revealedBg) bg = brighten(bg, 0.10f);

                float cellScale = 1.0f;
                if (animations && c.revealProgress >= 0f && c.revealProgress < 1f) {
                    float t = c.revealProgress < 0.5f ? c.revealProgress / 0.5f : (1f - c.revealProgress) / 0.5f;
                    cellScale = 1.0f + t * 0.15f;
                }

                context.getMatrices().pushMatrix();
                if (cellScale != 1.0f) {
                    context.getMatrices().translate(cx, cy);
                    context.getMatrices().scale(cellScale, cellScale);
                    context.getMatrices().translate(-cx, -cy);
                }

                context.fill(cx - half, cy - half, cx + half, cy + half, bg);

                if (highScale) {
                    context.getMatrices().pushMatrix();
                    context.getMatrices().scale(invScale, invScale);
                    int px1 = (int) Math.round(x * scaleFactor), py1 = (int) Math.round(y * scaleFactor);
                    int px2 = (int) Math.round((x + cellSize) * scaleFactor), py2 = (int) Math.round((y + cellSize) * scaleFactor);
                    context.fill(px1, py1, px2, py1 + 1, border);
                    context.fill(px1, py2 - 1, px2, py2, border);
                    context.fill(px1, py1, px1 + 1, py2, border);
                    context.fill(px2 - 1, py1, px2, py2, border);
                    context.getMatrices().popMatrix();
                } else {
                    context.fill(x, y, x + cellSize, y + 1, border);
                    context.fill(x, y + cellSize - 1, x + cellSize, y + cellSize, border);
                    context.fill(x, y, x + 1, y + cellSize, border);
                    context.fill(x + cellSize - 1, y, x + cellSize, y + cellSize, border);
                }

                if (c.revealed || c.flagged) {
                    drawCellContent(context, c, cx, cy, texSize, textScale);
                }

                context.getMatrices().popMatrix();
            }
        }
    }

    private void drawCellContent(DrawContext context, Cell c, int cx, int cy, int texSize, float textScale) {
        if (c.revealed) {
            if (c.mine && !board.won) {
                Identifier tex = (c.flagged && !board.alive) ? TEX_FLAG : TEX_TNT_SIDE;
                context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, cx - texSize / 2, cy - texSize / 2, 0, 0, texSize, texSize, texSize, texSize);
            } else if (c.adjacent > 0) {
                Text numText = ADJ_TEXT[c.adjacent];
                int color = getAdjColor(c.adjacent);
                context.getMatrices().pushMatrix();
                context.getMatrices().translate(cx, cy);
                if (textScale != 1.0f) context.getMatrices().scale(textScale, textScale);
                context.getMatrices().translate(
                    -mc.textRenderer.getWidth(numText) / 2f,
                    -mc.textRenderer.fontHeight / 2f);
                context.drawText(mc.textRenderer, numText, 0, 0, color, false);
                context.getMatrices().popMatrix();
            }
        } else if (c.flagged) {
            Identifier tex = (!board.alive && !board.won && !c.mine) ? TEX_BARRIER : TEX_FLAG;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, cx - texSize / 2, cy - texSize / 2, 0, 0, texSize, texSize, texSize, texSize);
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
        int r = Math.min(255, (int) (((color >>> 16) & 0xFF) * (1f + amount)));
        int g = Math.min(255, (int) (((color >>> 8)  & 0xFF) * (1f + amount)));
        int b = Math.min(255, (int) ((color & 0xFF)           * (1f + amount)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean shouldPause() { return false; }
}
