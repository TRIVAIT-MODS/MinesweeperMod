package org.trivait.minesweeper.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class DownloadMinigamesScreen extends Screen {
    public DownloadMinigamesScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.addRenderableOnly(Button.builder(
                Component.literal("Modrinth").withStyle(style -> style.withBold(true).withColor(ChatFormatting.GREEN)),
                (b) -> {
                    ConfirmLinkScreen.confirmLink(this, "https://modrinth.com/mod/minigamesmod");
                }
        ).bounds(width/2-5-100, height/2-10+(height/3)/2+40, 100, 20).build());
        this.addRenderableOnly(Button.builder(
                Component.literal("Curseforge").withStyle(style -> style.withBold(true).withColor(0xFFFF6A00)),
                (b) -> {
                    ConfirmLinkScreen.confirmLink(this, "https://www.curseforge.com/minecraft/mc-mods/minigamesmod");
                }
        ).bounds(width/2+5, height/2-10+(height/3)/2+40, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        int textureW = height/3;
        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("minesweeper", "textures/gui/minigames.png"), width/2-textureW/2, height/2-textureW/2-10, 0, 0, textureW, textureW, textureW, textureW);
        context.centeredText(Minecraft.getInstance().font, Component.literal("MinigamesMod").withStyle(style -> style.withBold(true)), width/2, height/2+textureW/2+5-10, -1);

        context.centeredText(Minecraft.getInstance().font, Component.translatable("minigames.message.1"), width/2, height/2+textureW/2-10+14, -1);
        context.centeredText(Minecraft.getInstance().font, Component.translatable("minigames.message.2"), width/2, height/2+textureW/2-10+14+10, -1);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        context.fill(0, height, width, height-40, 0xFF0094FF);
        context.fillGradient(0, 0, width, height-39, 0x00000000, 0xFF0094FF);
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {}
}
