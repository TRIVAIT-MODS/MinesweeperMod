package org.trivait.minesweeper.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.trivait.minesweeper.logic.Board;
import org.trivait.minesweeper.logic.Cell;
import org.trivait.minesweeper.util.MinesweeperSession;
import org.trivait.minesweeper.util.MinesweeperStats;

import java.util.function.Function;

public class MinesweeperScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.of("minesweeper", "textures/gui/minesweeper.png");
    private static final int TEXTURE_WIDTH = 230;
    private static final int TEXTURE_HEIGHT = 230;
    private static final int CELL_SIZE = 16;
    private static final int GRID_OFFSET_X = 45;
    private static final int GRID_OFFSET_Y = 10;
    public final MinesweeperStats stats = MinesweeperStats.load();


    public final Board board;

    public MinesweeperScreen() {
        super(Text.translatable("screen.minesweeper.title"));
        Cell[][] saved = MinesweeperSession.load();
        this.board = new Board();

        if (saved != null) {
            for (int x = 0; x < Board.SIZE; x++)
                for (int y = 0; y < Board.SIZE; y++)
                    board.cells[x][y] = saved[x][y];
        }

    }

    @Override
    protected void init() {
        int xBase = (width - TEXTURE_WIDTH) / 2;
        int yBase = (height - TEXTURE_HEIGHT) / 2;

        this.clearChildren();

        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                int px = xBase + GRID_OFFSET_X + x * CELL_SIZE;
                int py = yBase + GRID_OFFSET_Y + y * CELL_SIZE;

                this.addDrawableChild(new MinesweeperButton(
                        px, py,
                        CELL_SIZE, CELL_SIZE,
                        x, y,
                        board.cells[x][y],
                        btn -> {},
                        stats// ЛКМ обрабатывается внутри кнопки
                ));
            }
        }

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("button.minesweeper.close"),
                        btn -> this.close())
                .dimensions(xBase + 43, yBase + 146, 65, 15)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("button.minesweeper.restart"),
                        btn -> {
                            board.generate();
                            this.init();
                        })
                .dimensions(xBase + 110, yBase + 146, 65, 15)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int x = (width - TEXTURE_WIDTH) / 2;
        int y = (height - TEXTURE_HEIGHT) / 2;

        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x+30, y, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(
                textRenderer,
                Text.translatable("screen.minesweeper.wins", stats.wins),
                12, 12,
                0xFFFFFF
        );
    }

    @Override
    public void close() {
        super.close();
        MinesweeperSession.save(board);
    }
}
