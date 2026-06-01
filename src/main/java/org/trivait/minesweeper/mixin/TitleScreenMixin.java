package org.trivait.minesweeper.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.config.GameMode;
import org.trivait.minesweeper.config.MainMenuButtonPosition;
import org.trivait.minesweeper.game.GameSettings;
import org.trivait.minesweeper.game.SavedGame;
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperModClient.CONFIG;
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        TextIconButtonWidget minesweeperBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> {
                    SavedGame saved = MinesweeperModClient.getSavedGame();
                    if (saved != null) {
                        this.client.setScreen(new MinesweeperScreen(saved, cfg.enableAnimations, GameMode.DEFAULT));
                    } else {
                        this.client.setScreen(new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), cfg.enableAnimations, GameMode.DEFAULT));
                    }
                },
                true
        ).width(20).texture(Identifier.of("minesweeper", "icon/button"), 16, 16).build();

        minesweeperBtn.setPosition(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height)
        );
        this.addDrawableChild(minesweeperBtn);
    }
}
