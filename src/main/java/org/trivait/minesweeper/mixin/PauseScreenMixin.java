package org.trivait.minesweeper.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
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

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperModClient.getConfig();
        if (cfg.pauseMenuButtonPosition == null) {
            cfg.pauseMenuButtonPosition = Config.PauseMenuButtonPosition.RIGHT_NEXT_ROW;
        }
        SpriteIconButton minesweeperBtn = SpriteIconButton.builder(
                Component.empty(),
                (button) -> this.minecraft.setScreen(new MinesweeperScreen(title)),
                true
        ).width(20).sprite(Identifier.fromNamespaceAndPath("minesweeper", "icon/button"), 16, 16).build();

        for (Button button : this.children().stream().filter(e -> e instanceof Button).map(e -> (Button) e).toList()) {
            if (button.getMessage().equals(Component.translatable("menu.returnToGame"))) {
                int buttonX = button.getX();
                int buttonY = button.getY();
                int buttonWidth = button.getWidth();

                int x = cfg.pauseMenuButtonPosition.getX(buttonX, buttonWidth);
                int y = cfg.pauseMenuButtonPosition.getY(buttonY);

                minesweeperBtn.setPosition(x, y);
                break;
            }
        }

        this.addRenderableWidget(minesweeperBtn);
    }
}
