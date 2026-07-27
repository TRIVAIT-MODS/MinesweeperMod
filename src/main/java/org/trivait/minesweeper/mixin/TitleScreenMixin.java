package org.trivait.minesweeper.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minesweeper.MinesweeperMod;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.config.MainMenuButtonPosition;
import org.trivait.minesweeper.game.GameSettings;
import org.trivait.minesweeper.game.SavedGame;
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperMod.CONFIG;
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        SpriteIconButton minesweeperBtn = SpriteIconButton.builder(
                Component.empty(),
                (button) -> {
                    SavedGame saved = MinesweeperMod.getSavedGame();
                    if (saved != null) {
                        this.minecraft.setScreen(new MinesweeperScreen(saved, GameMode.DEFAULT));
                    } else {
                        this.minecraft.setScreen(new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), GameMode.DEFAULT));
                    }
                },
                true
        ).width(20).sprite(Identifier.fromNamespaceAndPath("minesweeper", "icon/button"), 16, 16).build();

        minesweeperBtn.setPosition(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height)
        );
        this.addRenderableWidget(minesweeperBtn);
    }
}
