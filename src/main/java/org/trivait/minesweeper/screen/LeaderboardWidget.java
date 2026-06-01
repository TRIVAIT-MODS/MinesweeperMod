package org.trivait.minesweeper.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.leaderboard.BoardCategory;
import org.trivait.minesweeper.leaderboard.LeaderboardCache;
import org.trivait.minesweeper.leaderboard.LeaderboardEntry;

import java.util.List;

public class LeaderboardWidget extends AbstractWidget {

    private static final int BG         = 0xCC1A1A1A;
    private static final int BORDER     = 0xFF555555;
    private static final int HEADER_BG  = 0xFF2B2B2B;
    private static final int ROW_ODD    = 0xFF222222;
    private static final int ROW_EVEN   = 0xFF1A1A1A;
    private static final int ROW_HOVER  = 0xFF2E2E2E;
    private static final int ROW_H      = 13;
    private static final int HEADER_H   = 14;
    private static final int PAD_X      = 6;

    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_GOLD   = 0xFFFFD700;
    private static final int COLOR_SILVER = 0xFFB0B0B0;
    private static final int COLOR_BRONZE = 0xFFCD7F32;

    private final LeaderboardCache cache;
    private GameMode gameMode;
    private BoardCategory category;

    private int scrollOffset = 0;

    public LeaderboardWidget(int x, int y, int width, int height,
                             LeaderboardCache cache, GameMode gameMode, BoardCategory category) {
        super(x, y, width, height, Component.empty());
        this.cache = cache;
        this.gameMode = gameMode;
        this.category = category;
    }

    public void setGameMode(GameMode mode) { this.gameMode = mode; scrollOffset = 0; }
    public void setCategory(BoardCategory cat) { this.category = cat; scrollOffset = 0; }
    public GameMode getGameMode() { return gameMode; }
    public BoardCategory getCategory() { return category; }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = width, h = height;
        var mc = Minecraft.getInstance();

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.fill(x, y, x + w, y + 1, BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, BORDER);
        ctx.fill(x, y, x + 1, y + h, BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, BORDER);

        ctx.fill(x + 1, y + 1, x + w - 1, y + HEADER_H + 1, HEADER_BG);
        String modeLabel = gameMode == GameMode.LEADERBOARD_TIME ? Component.translatable("leaderboard.mode.time").getString() : Component.translatable("leaderboard.mode.score").getString();
        String catLabel  = category != null ? category.label : "";
        ctx.text(mc.font, modeLabel + " - " + catLabel, x + PAD_X, y + 4, COLOR_WHITE, false);

        int listY = y + HEADER_H + 2;
        int listH = h - HEADER_H - 2;

        LeaderboardCache.State state = cache.getState(gameMode);
        if (state == LeaderboardCache.State.LOADING || state == LeaderboardCache.State.IDLE) {
            drawCentered(ctx, mc, Component.translatable("leaderboard.loading").getString(), x, listY, w, listH, 0xFFAAAAAA);
            return;
        }
        if (state == LeaderboardCache.State.ERROR) {
            drawCentered(ctx, mc, Component.translatable("leaderboard.error").getString() + cache.getError(gameMode), x, listY, w, listH, 0xFFFF5555);
            return;
        }

        List<LeaderboardEntry> entries = cache.getData(gameMode).stream()
                .filter(e -> category == null || e.category().equalsIgnoreCase(category.label))
                .toList();

        int visibleRows = listH / ROW_H;
        int maxScroll = Math.max(0, entries.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        boolean isTime = gameMode == GameMode.LEADERBOARD_TIME;
        int rankW = mc.font.width("00. ");

        for (int i = 0; i < visibleRows && (i + scrollOffset) < entries.size(); i++) {
            int rowY = listY + i * ROW_H;
            int rank = i + scrollOffset + 1;
            LeaderboardEntry entry = entries.get(i + scrollOffset);

            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= rowY && mouseY < rowY + ROW_H;
            ctx.fill(x + 1, rowY, x + w - 1, rowY + ROW_H, hovered ? ROW_HOVER : (i % 2 == 0 ? ROW_EVEN : ROW_ODD));

            int placeColor = placeColor(rank);
            boolean top3 = rank <= 3;

            Component rankText = Component.literal(rank + ".").withStyle(s -> s.withBold(top3));
            ctx.text(mc.font, rankText, x + PAD_X, rowY + 2, placeColor, false);

            Component nameText = Component.literal(entry.name()).withStyle(s -> s.withBold(true));
            ctx.text(mc.font, nameText, x + PAD_X + rankW, rowY + 2, top3 ? placeColor : COLOR_WHITE, false);

            String suffix = isTime ? Component.translatable("second.suffix").getString() : "";
            Component valueText = Component.literal(entry.value() + suffix).withStyle(s -> s.withBold(true));
            int vx = x + w - PAD_X - mc.font.width(valueText);
            ctx.text(mc.font, valueText, vx, rowY + 2, top3 ? placeColor : COLOR_WHITE, false);
        }

        if (entries.size() > visibleRows && maxScroll > 0) {
            int sbX = x + w - 3;
            float ratio = (float) visibleRows / entries.size();
            int thumbH = Math.max(8, (int) (listH * ratio));
            int thumbY = listY + (int) ((listH - thumbH) * ((float) scrollOffset / maxScroll));
            ctx.fill(sbX, listY, sbX + 2, listY + listH, 0xFF333333);
            ctx.fill(sbX, thumbY, sbX + 2, thumbY + thumbH, 0xFF888888);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    private static int placeColor(int rank) {
        return switch (rank) {
            case 1 -> COLOR_GOLD;
            case 2 -> COLOR_SILVER;
            case 3 -> COLOR_BRONZE;
            default -> COLOR_WHITE;
        };
    }

    private void drawCentered(GuiGraphicsExtractor ctx, Minecraft mc, String text, int x, int y, int w, int h, int color) {
        int tw = mc.font.width(text);
        ctx.text(mc.font, text, x + (w - tw) / 2, y + (h - mc.font.lineHeight) / 2, color, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        scrollOffset = Math.max(0, Math.min(
            scrollOffset - (int) (verticalAmount * 3),
            Math.max(0, getEntryCount() - height / ROW_H)
        ));
        return true;
    }

    private int getEntryCount() {
        if (cache.getState(gameMode) != LeaderboardCache.State.READY) return 0;
        return (int) cache.getData(gameMode).stream()
                .filter(e -> category == null || e.category().equalsIgnoreCase(category.label))
                .count();
    }
}
