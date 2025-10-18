package org.trivait.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
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

        // Заголовок: "Minesweeper — WxH, Mines: N"
        String head = String.format("%s — %d×%d, %s: %d",
                Text.translatable("menu.minesweeper").getString(),
                this.w, this.h,
                Text.translatable("config.minesweeper.mines").getString(),
                this.mines
        );
        int headX = this.width / 2 - this.client.textRenderer.getWidth(head) / 2;
        int headY = this.gridY - 28;
        context.drawText(this.client.textRenderer, head, headX, headY, 0xFFFFFF, false);

        // Статус: win / playing / lose
        Text status = this.won
                ? Text.translatable("gui.minesweeper.status.win")
                : (this.alive
                ? Text.translatable("gui.minesweeper.status.playing")
                : Text.translatable("gui.minesweeper.status.lose"));

        int statusColor = this.won ? 0x00FF00 : (this.alive ? 0xFFFFFF : 0xFF5555);
        int statusX = this.width / 2 - this.client.textRenderer.getWidth(status) / 2;
        int statusY = this.gridY - 12;
        context.drawText(this.client.textRenderer, status, statusX, statusY, statusColor, false);

        // Отрисовка сетки
        for (int yy = 0; yy < this.h; yy++) {
            for (int xx = 0; xx < this.w; xx++) {
                int x = this.gridX + xx * this.cellSize;
                int y = this.gridY + yy * this.cellSize;
                Cell c = this.grid[yy][xx];

                int border = 0xFF555555;      // серый
                int bg = c.revealed ? 0xFF2A2A2A : 0xFF444444;

                // фон и рамка
                context.fill(x, y, x + this.cellSize, y + this.cellSize, bg);
                context.fill(x, y, x + this.cellSize, y + 1, border);
                context.fill(x, y + this.cellSize - 1, x + this.cellSize, y + this.cellSize, border);
                context.fill(x, y, x + 1, y + this.cellSize, border);
                context.fill(x + this.cellSize - 1, y, x + this.cellSize, y + this.cellSize, border);

                if (c.revealed) {
                    // Мина
                    if (c.mine && !this.won) {
                        Identifier TNT_SIDE = Identifier.of("textures/block/tnt_side.png");
                        int texSize = 16;
                        int offsetX = x + (this.cellSize - texSize) / 2;
                        int offsetY = y + (this.cellSize - texSize) / 2;
                        context.drawTexture(RenderLayer::getGuiTextured,TNT_SIDE, offsetX, offsetY, 0, 0, texSize, texSize, texSize, texSize);
                    }
                    // Число соседних мин
                    else if (c.adjacent > 0) {
                        int color = switch (c.adjacent) {
                            case 1 -> 0xFF3DD3FE;
                            case 2 -> 0xFF3CB371;
                            case 3 -> 0xFFEE2222;
                            case 4 -> 0xFF7777FF;
                            case 5 -> 0xFFFF5555;
                            case 6 -> 0xFF228B22;
                            case 7 -> 0xFFFFFFFF;
                            default -> 0xFFAAAAAA;
                        };

                        String numStr = Integer.toString(c.adjacent);
                        Text numBold = Text.literal(numStr).styled(s -> s.withBold(true));
                        OrderedText ordered = numBold.asOrderedText();

                        float scale = 1.15F;
                        int numW = this.client.textRenderer.getWidth(ordered);
                        int numH = 9;

                        float tx = (x + (this.cellSize - numW * scale) / 2f) / scale;
                        float ty = (y + (this.cellSize - numH * scale) / 2f) / scale;

                        context.getMatrices().push();
                        context.getMatrices().scale(scale, scale, 1.0f);
                        context.drawText(this.client.textRenderer, ordered, (int) tx, (int) ty, color, false);
                        context.getMatrices().pop();
                    }
                }
                // Флажки
                else if (c.flagged) {
                    if (!this.alive) {
                        if (c.mine) {
                            int pole = 0xFF222222;
                            int flag = 0xFFFF0000;
                            context.fill(x + this.cellSize / 2 - 1, y + 4, x + this.cellSize / 2 + 1, y + this.cellSize - 4, pole);
                            context.fill(x + this.cellSize / 2, y + 4, x + this.cellSize - 6, y + 10, flag);
                        } else {
                            Identifier BARRIER = Identifier.of("textures/item/barrier.png");
                            int texSize = 16;
                            int offsetX = x + (this.cellSize - texSize) / 2;
                            int offsetY = y + (this.cellSize - texSize) / 2;
                            context.drawTexture(RenderLayer::getGuiTextured,BARRIER, offsetX, offsetY, 0, 0, texSize, texSize, texSize, texSize);
                        }
                    } else {
                        int pole = 0xFF222222;
                        int flag = 0xFFFF0000;
                        context.fill(x + this.cellSize / 2 - 1, y + 4, x + this.cellSize / 2 + 1, y + this.cellSize - 4, pole);
                        context.fill(x + this.cellSize / 2, y + 4, x + this.cellSize - 6, y + 10, flag);
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
