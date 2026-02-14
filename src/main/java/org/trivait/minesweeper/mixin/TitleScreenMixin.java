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
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperModClient.getConfig();
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = Config.MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        TextIconButtonWidget minesweeperBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> this.client.setScreen(new MinesweeperScreen()),
                true
        ).width(20).texture(Identifier.of("minesweeper", "icon/button"), 16, 16).build();

        minesweeperBtn.setPosition(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height)
        );
        this.addDrawableChild(minesweeperBtn);
    }
}
