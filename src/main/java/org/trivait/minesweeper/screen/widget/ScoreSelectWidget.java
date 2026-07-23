package org.trivait.minesweeper.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.trivait.minesweeper.MinesweeperMod;
import org.trivait.minesweeper.screen.ScoreLeaderboardScreen;

public class ScoreSelectWidget extends AbstractWidget {
    private float scale = 1.0f;
    private float targetScale = 1.0f;
    private final float speed = 0.30f;

    private final Screen parent;

    public ScoreSelectWidget(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack matrixStack = ctx.pose();

        if (this.isHovered()) {
            targetScale = 1.05f;
        } else {
            targetScale = 1.0f;
        }

        scale += (targetScale - scale) * speed;

        int cx = getX() + getWidth() / 2;
        int cy = getY() + getHeight() / 2;

        matrixStack.pushMatrix();
        matrixStack.translate(cx, cy);
        matrixStack.scale(scale, scale);
        matrixStack.translate(-cx, -cy);

        ctx.fill(getX(), getY(), getX()+width, getY()+2, -2);
        ctx.fill(getX(), getY()+height, getX()+width, getY()+height-2, -1);
        ctx.fill(getX(), getY(), getX()+2, getY()+height-2, -2);
        ctx.fill(getX()+width, getY(), getX()+width-2, getY()+height-2, -1);

        ctx.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MinesweeperMod.MOD_ID, "textures/gui/score.png"), getX() + 1, getY() + 1, 0, 0, width - 2, height - 2, width - 2, height - 2);

        var mc = net.minecraft.client.Minecraft.getInstance();
        Component label = net.minecraft.network.chat.Component.translatable("leaderboard.mode.score").withStyle(s -> s.withBold(true));
        int tw = mc.font.width(label) * 2;
        int tx = getX() + (width - tw) / 2;
        int ty = getY() + height - mc.font.lineHeight * 2 - 8;
        matrixStack.pushMatrix();
        matrixStack.translate(tx, ty);
        matrixStack.scale(2f, 2f);
        ctx.text(mc.font, label, 0, 0, -1, true);
        matrixStack.popMatrix();

        matrixStack.popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        Minecraft.getInstance().setScreenAndShow(new ScoreLeaderboardScreen(parent));

        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }
}
