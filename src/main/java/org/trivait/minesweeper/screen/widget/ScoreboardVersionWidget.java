package org.trivait.minesweeper.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.trivait.minesweeper.MinesweeperMod;
import org.trivait.minesweeper.leaderboard.SheetsApi;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardVersionWidget extends AbstractWidget {

    private static final int SIZE = 26;
    private static final Identifier TEX_SCORE = Identifier.fromNamespaceAndPath(MinesweeperMod.MOD_ID, "textures/gui/api.png");

    public ScoreboardVersionWidget(int x, int y) {
        super(x, y, SIZE, SIZE, Component.empty());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_SCORE, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        if (isMouseOver(mouseX, mouseY)) {
            List<Component> tooltip = buildTooltip();
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    private List<Component> buildTooltip() {
        List<Component> lines = new ArrayList<>();
        String scriptVer = SheetsApi.getScriptVersion() != null ? SheetsApi.getScriptVersion() : "?";
        String apiVer = SheetsApi.SCOREBOARD_API_VERSION;
        lines.add(Component.translatable("scoreboard.version.script", scriptVer));
        lines.add(Component.translatable("scoreboard.version.api", apiVer));
        if (SheetsApi.isVersionMismatch()) {
            lines.add(Component.translatable("scoreboard.version.update_mod").withStyle(s -> s.withColor(0xFF5555)));
        }
        return lines;
    }
}
