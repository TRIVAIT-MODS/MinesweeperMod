package org.trivait.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minesweeper.logic.Board;
import org.trivait.minesweeper.logic.Cell;
import org.trivait.minesweeper.util.MinesweeperStats;

public class MinesweeperButton extends ButtonWidget {
    private final int cellX;
    private final int cellY;
    private final Cell cell;
    private final Board board;
    private final MinesweeperStats stats;

    public MinesweeperButton(int x, int y, int width, int height, int cellX, int cellY, Cell cell, PressAction onPress, MinesweeperStats stats) {
        super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.cellX = cellX;
        this.cellY = cellY;
        this.cell = cell;
        this.board = MinecraftClient.getInstance().currentScreen instanceof MinesweeperScreen screen ? screen.board : null;
        this.stats = stats;
    }

    private static final Identifier CLOSED_TEXTURE = Identifier.of("minesweeper", "textures/gui/button_closed.png");
    private static final Identifier OPEN_TEXTURE = Identifier.of("minesweeper", "textures/gui/button_open.png");

    private static final int[] COLORS = new int[]{
            0xFF0000FF, // 1 – синий
            0xFF008000, // 2 – зелёный
            0xFFFF0000, // 3 – красный
            0xFF000080, // 4 – тёмно-синий
            0xFF800000, // 5 – тёмно-красный
            0xFF00CCCC, // 6 – бирюзовый
            0xFF800080, // 7 – фиолетовый
            0xFF000000  // 8 – чёрный
    };

    @Override
    public void onPress() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && !cell.isRevealed && !cell.isFlagged && !board.gameOver) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
        }

        if (!board.gameOver && !cell.isRevealed && !cell.isFlagged) {
            board.reveal(cellX, cellY);

            if (cell.isBomb) {
                board.gameOver = true;
                if (player != null) {
                    player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
                }
            } else if (board.isWin()) {
                stats.wins++;
                stats.save();
                if (player != null) {
                    player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
                }
            }

            if (MinecraftClient.getInstance().currentScreen instanceof MinesweeperScreen screen) {
                screen.init();
            }
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible || !this.active) return false;

        boolean hovered = this.isHovered();
        if (hovered) {
            if (button == 1) {
                if (!cell.isRevealed && !board.gameOver) {
                    cell.isFlagged = !cell.isFlagged;

                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (player != null) {
                        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                    }

                    if (MinecraftClient.getInstance().currentScreen instanceof MinesweeperScreen screen) {
                        screen.init();
                    }
                }
                return true;
            } else if (button == 0) { // ЛКМ
                this.onPress();
                return true;
            }
        }
        return false;
    }


    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();

        if (!cell.isRevealed) {
            context.drawTexture(RenderLayer::getGuiTextured, CLOSED_TEXTURE, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        } else if (cell.adjacentBombs == 0 && !cell.isBomb) {
            context.drawTexture(RenderLayer::getGuiTextured, OPEN_TEXTURE, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        } else if (cell.isRevealed){
            context.drawTexture(RenderLayer::getGuiTextured, CLOSED_TEXTURE, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (cell.isFlagged && !cell.isRevealed) {
            context.drawCenteredTextWithShadow(textRenderer, "⚑", x + getWidth() / 2, y + 1, 0xFF0000);
        } else if (cell.isRevealed) {
            if (cell.isBomb) {
                context.drawCenteredTextWithShadow(textRenderer, "💣", x + getWidth() / 2, y + 1, 0xFF0000);
            } else if (cell.adjacentBombs > 0) {
                int num = cell.adjacentBombs;
                int color = COLORS[Math.min(num - 1, COLORS.length - 1)];
                context.drawCenteredTextWithShadow(textRenderer, String.valueOf(num), x + getWidth() / 2, y + 1, color);
            }
        }
    }
}
