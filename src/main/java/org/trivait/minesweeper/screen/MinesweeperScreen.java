package org.trivait.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;

import java.util.*;

public class MinesweeperScreen extends Screen {

    private static class Cell {
        boolean mine, revealed, flagged;
        int adjacent;
        float revealProgress = -1f;
        boolean chainTriggered = false;
        int delayTicks = -1;
        boolean scheduled = false;
    }

    private static class Animation {
        int x, y, width, height;
        String animation;
        int frames;
        float frame;

        Animation(int x, int y, int width, int height, String animation, int frames) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.animation = animation;
            this.frames = frames;
            this.frame = 0f;
        }

        void draw(DrawContext context, String modId) {
            Identifier tex = Identifier.of(modId, "animation/" + animation + "/" + (int) frame + ".png");
            context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, width, height, width, height);
        }

        boolean advance(float dFrames) {
            frame += dFrames;
            return frame <= frames;
        }
    }

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private Cell[][] grid;
    private int w, h, mines;

    private boolean alive = true;
    private boolean won = false;
    private boolean firstClick = true;

    private int cellSize = 24;
    private int gridX, gridY;

    private final Random rng = new Random();
    private final List<Animation> animations = new ArrayList<>();

    public MinesweeperScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        Config cfg = MinesweeperModClient.getConfig();
        this.w = Math.max(5, Math.min(cfg.gridWidth, 40));
        this.h = Math.max(5, Math.min(cfg.gridHeight, 40));
        int maxMines = Math.max(1, (w * h) - 1);
        this.mines = Math.max(1, Math.min(cfg.mines, maxMines));

        int marginTop = 64, marginBottom = 56, marginSides = 32;
        int availW = Math.max(1, this.width - marginSides * 2);
        int availH = Math.max(1, this.height - (marginTop + marginBottom));
        int maxCellW = Math.max(8, availW / w);
        int maxCellH = Math.max(8, availH / h);
        this.cellSize = Math.min(Math.min(maxCellW, maxCellH), 48);

        double scaleFactor = mc.getWindow().getScaleFactor();
        if (scaleFactor >= 3) {
            this.cellSize += 2;
        }

        grid = new Cell[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) grid[y][x] = new Cell();

        int totalW = w * cellSize;
        int totalH = h * cellSize;
        gridX = (this.width - totalW) / 2;
        gridY = Math.max(marginTop, (this.height - totalH) / 2);

        alive = true;
        won = false;
        firstClick = true;
        animations.clear();

        int btnY = gridY + totalH + 10;
        int centerX = this.width / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.minesweeper.restart"), b -> this.init())
                .dimensions(centerX - 100, btnY, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> MinecraftClient.getInstance().setScreen(null))
                .dimensions(centerX + 5, btnY, 95, 20).build());
    }

    private void placeMinesAvoiding(int avoidX, int avoidY) {
        int placed = 0;
        while (placed < mines) {
            int x = rng.nextInt(w), y = rng.nextInt(h);
            if ((x == avoidX && y == avoidY) || grid[y][x].mine) continue;
            grid[y][x].mine = true;
            placed++;
        }
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

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!alive) {
            if (MinesweeperModClient.getConfig().quickRestartOnLose && click.button() == 0) {
                this.init();
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        int x = (int)((click.x() - gridX) / cellSize);
        int y = (int)((click.y() - gridY) / cellSize);
        if (x < 0 || x >= w || y < 0 || y >= h) return super.mouseClicked(click, doubled);

        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.20f, 1.0f));

        Cell c = grid[y][x];

        if (click.button() == 1) {
            if (!c.revealed) c.flagged = !c.flagged;
            return true;
        }

        if (click.button() == 0) {
            if (firstClick) {
                placeMinesAvoiding(x, y);
                firstClick = false;
            }
            if (!c.flagged && !c.revealed && !c.scheduled) {
                startRevealWave(x, y);
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void startRevealWave(int sx, int sy) {
        Queue<int[]> queue = new ArrayDeque<>();
        Cell start = grid[sy][sx];
        if (start.mine) {
            start.revealed = true;
            alive = false;
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.7f, 1.0f));
            spawnExplosionAnimation(sx, sy);
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    Cell cell = grid[yy][xx];
                    if (cell.mine && !cell.flagged) {
                        cell.revealed = true;
                    }
                }
            }
            return;
        }

        start.scheduled = true;
        start.delayTicks = 0;
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
                                queue.add(new int[]{nx, ny});
                            }
                        }
                    }
                }
            }
        }
    }

    private void spawnExplosionAnimation(int cellX, int cellY) {
        int px = gridX + cellX * cellSize + cellSize/2;
        int py = gridY + cellY * cellSize + cellSize/2;
        int width = cellSize * 4;
        int height = cellSize * 9 / 4;
        animations.add(new Animation(px - width/2, py - height/2, width, height, "explosion", 20));
    }

    private void checkWinAndCount() {
        if (!alive || won) return;

        int hiddenSafe = 0;
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                Cell c = grid[yy][xx];
                if (!c.mine && !c.revealed) {
                    hiddenSafe++;
                }
            }
        }

        if (hiddenSafe == 0) {
            won = true;
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

            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.0f));
            MinesweeperModClient.incrementWins();
        }
    }

    @Override
    public void tick() {
        boolean anyRevealThisTick = false;

        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                Cell c = grid[yy][xx];

                if (c.delayTicks > 0) {
                    c.delayTicks--;
                } else if (c.delayTicks == 0 && !c.revealed && c.scheduled) {
                    c.revealed = true;
                    c.revealProgress = 0f;
                    c.delayTicks = -1;
                    anyRevealThisTick = true;
                }

                if (c.revealProgress >= 0f && c.revealProgress < 1f) {
                    c.revealProgress = Math.min(1f, c.revealProgress + 0.2f);
                }
            }
        }

        if (anyRevealThisTick && alive && !won) {
            checkWinAndCount();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Text winsText = Text.translatable("gui.minesweeper.wins", MinesweeperModClient.getConfig().wins);
        context.drawText(mc.textRenderer, winsText, 8, 8, 0xFFFFFFFF, false);

        String head = String.format("%s — %d×%d, %s: %d",
                Text.translatable("menu.minesweeper").getString(),
                w, h,
                Text.translatable("config.minesweeper.mines").getString(),
                mines);
        int headX = this.width / 2 - mc.textRenderer.getWidth(head) / 2;
        int headY = gridY - 28;
        context.drawText(mc.textRenderer, head, headX, headY, 0xFFFFFFFF, false);

        Text status = won
                ? Text.translatable("gui.minesweeper.status.win")
                : (alive ? Text.translatable("gui.minesweeper.status.playing")
                : Text.translatable("gui.minesweeper.status.lose"));
        int statusColor = won ? 0x00FF00 : (alive ? 0xFFFFFF : 0xFF5555);
        int statusX = this.width / 2 - mc.textRenderer.getWidth(status) / 2;
        int statusY = gridY - 12;
        context.drawText(mc.textRenderer, status, statusX, statusY, statusColor, false);

        double scaleFactor = mc.getWindow().getScaleFactor();
        float textScale = (scaleFactor <= 2) ? 1.2f : 0.9f;
        int texSizeAdjust = (scaleFactor <= 2) ? (int)(cellSize * 0.7) : cellSize;

        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int x = gridX + xx * cellSize;
                int y = gridY + yy * cellSize;
                Cell c = grid[yy][xx];

                boolean hovered = mouseX >= x && mouseX < x + cellSize && mouseY >= y && mouseY < y + cellSize;

                int border = 0xFF555555;
                int bg = c.revealed ? 0xFF2D2D2D : 0xFF454545;
                if (hovered && !c.revealed) bg = brighten(bg, 0.10f);

                float p = c.revealProgress;
                boolean animActive = p >= 0f && p < 1f;
                float phase = animActive ? (p < 0.5f ? (p / 0.5f) : ((1f - p) / 0.5f)) : 0f;
                float scale = 1.0f + phase * 0.20f;

                int cx = x + cellSize / 2;
                int cy = y + cellSize / 2;
                int sSize = Math.max(1, Math.round(cellSize * scale));
                int sx = cx - sSize / 2;
                int sy = cy - sSize / 2;

                context.fill(sx, sy, sx + sSize, sy + sSize, bg);
                context.fill(sx, sy, sx + sSize, sy + 1, border);
                context.fill(sx, sy + sSize - 1, sx + sSize, sy + sSize, border);
                context.fill(sx, sy, sx + 1, sy + sSize, border);
                context.fill(sx + sSize - 1, sy, sx + sSize, sy + sSize, border);

                if (c.revealed) {
                    if (c.mine && !won) {
                        if (c.flagged && !alive) {
                            Identifier FLAG = Identifier.of(MinesweeperModClient.MOD_ID, "textures/gui/flag.png");
                            int tSize = Math.min(texSizeAdjust, sSize);
                            int tx = cx - tSize / 2;
                            int ty = cy - tSize / 2;
                            context.drawTexture(RenderPipelines.GUI_TEXTURED, FLAG,
                                    tx, ty, 0, 0, tSize, tSize, tSize, tSize, -1);
                        } else {
                            Identifier TNT_SIDE = Identifier.ofVanilla("textures/block/tnt_side.png");
                            int tSize = Math.min(texSizeAdjust, sSize);
                            int tx = cx - tSize / 2;
                            int ty = cy - tSize / 2;
                            context.drawTexture(RenderPipelines.GUI_TEXTURED, TNT_SIDE,
                                    tx, ty, 0, 0, tSize, tSize, tSize, tSize, -1);
                        }
                    } else if (c.adjacent > 0) {
                        int color = switch (c.adjacent) {
                            case 1 -> 0xFF3EB2FF;
                            case 2 -> 0xFF41D45E;
                            case 3 -> 0xFFFF4E4E;
                            case 4 -> 0xFF7757FF;
                            case 5 -> 0xFFFFA84E;
                            case 6 -> 0xFF4EE0FF;
                            case 7 -> 0xFFFFFFFF;
                            default -> 0xFFBBBBBB;
                        };

                        String num = Integer.toString(c.adjacent);
                        Text numText = Text.literal(num).styled(s -> s.withBold(true));

                        float ts = (mc.getWindow().getScaleFactor() <= 2) ? 1.6f : 1.0f;

                        int tw = (int)(mc.textRenderer.getWidth(numText) * ts);
                        int th = (int)(mc.textRenderer.fontHeight * ts);

                        int tx = cx - tw / 2;
                        int ty = cy - th / 2;

                        context.drawText(mc.textRenderer, numText, tx+2, ty+2, color, false);
                    }
                } else if (c.flagged) {
                    if (!alive && !won && !c.mine) {
                        Identifier BARRIER = Identifier.ofVanilla("textures/item/barrier.png");
                        int tSize = Math.min(texSizeAdjust, sSize);
                        int tx = cx - tSize / 2;
                        int ty = cy - tSize / 2;
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, BARRIER,
                                tx, ty, 0, 0, tSize, tSize, tSize, tSize, -1);
                    } else {
                        Identifier FLAG = Identifier.of(MinesweeperModClient.MOD_ID, "textures/gui/flag.png");
                        int tSize = Math.min(texSizeAdjust, sSize);
                        int tx = cx - tSize / 2;
                        int ty = cy - tSize / 2;
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, FLAG,
                                tx, ty, 0, 0, tSize, tSize, tSize, tSize, -1);
                    }
                }

                if (animActive && !c.chainTriggered && c.revealProgress >= 0.5f) {
                    c.chainTriggered = true;
                    mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_DEEPSLATE_BREAK, 0.25f, 1.0f));
                }
            }
        }

        if (!animations.isEmpty()) {
            for (int i = animations.size() - 1; i >= 0; i--) {
                Animation a = animations.get(i);
                Identifier tex = Identifier.of(MinesweeperModClient.MOD_ID, "animation/" + a.animation + "/" + (int)a.frame + ".png");
                context.drawTexture(RenderPipelines.GUI_TEXTURED, tex,
                        a.x, a.y, 0, 0, a.width, a.height, a.width, a.height, -1);
                boolean aliveAnim = a.advance(0.5f);
                if (!aliveAnim) animations.remove(i);
            }
        }
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
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }
}