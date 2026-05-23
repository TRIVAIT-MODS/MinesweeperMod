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
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperModClient.getConfig();
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = Config.MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        SpriteIconButton minesweeperBtn = SpriteIconButton.builder(
                Component.empty(),
                (button) -> this.minecraft.setScreen(new MinesweeperScreen(this.title)),
                true
        ).width(20).sprite(Identifier.fromNamespaceAndPath("minesweeper", "icon/button"), 16, 16).build();

        minesweeperBtn.setPosition(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height)
        );
        this.addRenderableWidget(minesweeperBtn);
    }
}
