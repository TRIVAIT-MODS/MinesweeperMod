package org.trivait.minesweeper.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class DownloadMinigamesScreen extends Screen {
    public DownloadMinigamesScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Modrinth").styled(style -> style.withBold(true).withFormatting(Formatting.GREEN)),
                (b) -> {
                    ConfirmLinkScreen.open(this, "https://modrinth.com/mod/minigamesmod");
                }
        ).dimensions(width/2-5-100, height/2-10+(height/3)/2+40, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Curseforge").styled(style -> style.withBold(true).withColor(0xFFFF6A00)),
                (b) -> {
                    ConfirmLinkScreen.open(this, "https://www.curseforge.com/minecraft/mc-mods/minigamesmod");
                }
        ).dimensions(width/2+5, height/2-10+(height/3)/2+40, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int textureW = height/3;
        context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("minesweeper", "textures/gui/minigames.png"), width/2-textureW/2, height/2-textureW/2-10, 0, 0, textureW, textureW, textureW, textureW);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("MinigamesMod").styled(style -> style.withBold(true)), width/2, height/2+textureW/2+5-10, -1);

        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable("minigames.message.1"), width/2, height/2+textureW/2-10+14, -1);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable("minigames.message.2"), width/2, height/2+textureW/2-10+14+10, -1);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, height, width, height-40, 0xFF0094FF);
        context.fillGradient(0, 0, width, height-39, 0x00000000, 0xFF0094FF);
    }

    @Override
    public void blur() {}
}
