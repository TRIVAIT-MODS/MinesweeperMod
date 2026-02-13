package org.trivait.minesweeper.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.trivait.minesweeper.MinesweeperModClient;
import org.trivait.minesweeper.config.Config;
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperModClient.getConfig();
        if (cfg.pauseMenuButtonPosition == null) {
            cfg.pauseMenuButtonPosition = Config.PauseMenuButtonPosition.RIGHT_NEXT_ROW;
        }
        TextIconButtonWidget minesweeperBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> this.client.setScreen(new MinesweeperScreen(title)),
                true
        ).width(20).texture(Identifier.of("minesweeper", "icon/button"), 16, 16).build();

        for (ButtonWidget button : this.children().stream().filter(e -> e instanceof ButtonWidget).map(e -> (ButtonWidget) e).toList()) {
            if (button.getMessage().equals(Text.translatable("menu.returnToGame"))) {
                int buttonX = button.getX();
                int buttonY = button.getY();
                int buttonWidth = button.getWidth();

                int x = cfg.pauseMenuButtonPosition.getX(buttonX, buttonWidth);
                int y = cfg.pauseMenuButtonPosition.getY(buttonY);

                minesweeperBtn.setPosition(x, y);
                break;
            }
        }

        this.addDrawableChild(minesweeperBtn);
    }
}
