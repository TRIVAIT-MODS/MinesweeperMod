package org.trivait.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;

import java.util.Random;

public class MinesweeperScreen extends Screen {

    private static class Cell {
        boolean mine;
        boolean revealed;
        boolean flagged;
        int adjacent;
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

        grid = new Cell[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) grid[y][x] = new Cell();

        int totalW = w * cellSize;
        int totalH = h * cellSize;
        gridX = (this.width - totalW) / 2;
        gridY = Math.max(30, (this.height - totalH) / 2);

        alive = true;
        won = false;
        firstClick = true;

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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!alive) {
            if (MinesweeperModClient.getConfig().quickRestartOnLose && button == 0) {
                this.init();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int x = (int)((mouseX - gridX) / cellSize);
        int y = (int)((mouseY - gridY) / cellSize);
        if (x < 0 || x >= w || y < 0 || y >= h) return super.mouseClicked(mouseX, mouseY, button);

        Cell c = grid[y][x];

        if (button == 1) {
            if (!c.revealed) c.flagged = !c.flagged;
            return true;
        }

        if (button == 0) {
            if (firstClick) {
                placeMinesAvoiding(x, y);
                firstClick = false;
            }
            if (!c.flagged && !c.revealed) {
                reveal(x, y);
                if (!alive) return true;
                checkWin();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void reveal(int x, int y) {
        Cell c = grid[y][x];
        if (c.revealed || c.flagged) return;
        c.revealed = true;
        if (c.mine) {
            alive = false;
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 1.0f));
            for (int yy = 0; yy < h; yy++) for (int xx = 0; xx < w; xx++) {
                if (grid[yy][xx].mine) grid[yy][xx].revealed = true;
            }
            return;
        }
        if (c.adjacent == 0) {
            for (int dy = -1; dy <= 1; dy++)
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = x + dx, ny = y + dy;
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                        Cell n = grid[ny][nx];
                        if (!n.revealed && !n.mine) reveal(nx, ny);
                    }
                }
        }
    }

    private void checkWin() {
        int hiddenSafe = 0;
        for (int yy = 0; yy < h; yy++) for (int xx = 0; xx < w; xx++) {
            if (!grid[yy][xx].mine && !grid[yy][xx].revealed) hiddenSafe++;
        }
        if (hiddenSafe == 0 && alive) {
            won = true;
            for (int yy = 0; yy < h; yy++) for (int xx = 0; xx < w; xx++) {
                if (!grid[yy][xx].mine) grid[yy][xx].revealed = true;
            }
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f));
        }
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
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

        // Поле
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int x = gridX + xx * cellSize;
                int y = gridY + yy * cellSize;

                Cell c = grid[yy][xx];
                int border = 0xFF555555;
                int bg = c.revealed ? 0xFF2D2D2D : 0xFF454545;

                context.fill(x, y, x + cellSize, y + cellSize, bg);
                context.fill(x, y, x + cellSize, y + 1, border);
                context.fill(x, y + cellSize - 1, x + cellSize, y + cellSize, border);
                context.fill(x, y, x + 1, y + cellSize, border);
                context.fill(x + cellSize - 1, y, x + cellSize, y + cellSize, border);

                if (c.revealed) {
                    if (c.mine && !won) {
                        Identifier TNT_SIDE = Identifier.ofVanilla("textures/block/tnt_side.png");
                        int texSize = 16;
                        int offsetX = x + (cellSize - texSize) / 2;
                        int offsetY = y + (cellSize - texSize) / 2;
                        context.drawTexture(
                                RenderLayer::getGuiTextured,
                                TNT_SIDE,
                                offsetX, offsetY,
                                0, 0,
                                texSize, texSize,
                                texSize, texSize,
                                -1
                        );
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
                        int tx = x + (cellSize - mc.textRenderer.getWidth(num)) / 2;
                        int ty = y + (cellSize - mc.textRenderer.fontHeight) / 2;
                        context.drawText(mc.textRenderer, num, tx, ty, color, false);
                    }
                } else if (c.flagged) {
                    if (!alive) {
                        if (c.mine) {
                            int pole = 0xFF222222;
                            int flag = 0xFFFF0000;
                            context.fill(x + cellSize / 2 - 1, y + 4, x + cellSize / 2 + 1, y + cellSize - 4, pole);
                            context.fill(x + cellSize / 2, y + 4, x + cellSize - 6, y + 10, flag);
                        } else {
                            Identifier BARRIER = Identifier.ofVanilla("textures/item/barrier.png");
                            int texSize = 16;
                            int offsetX = x + (cellSize - texSize) / 2;
                            int offsetY = y + (cellSize - texSize) / 2;
                            context.drawTexture(
                                    RenderLayer::getGuiTextured,
                                    BARRIER,
                                    offsetX, offsetY,
                                    0, 0,
                                    texSize, texSize,
                                    texSize, texSize,
                                    -1
                            );
                        }
                    } else {
                        int pole = 0xFF222222;
                        int flag = 0xFFFF0000;
                        context.fill(x + cellSize / 2 - 1, y + 4, x + cellSize / 2 + 1, y + cellSize - 4, pole);
                        context.fill(x + cellSize / 2, y + 4, x + cellSize - 6, y + 10, flag);
                    }
                }
            }
        }
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
