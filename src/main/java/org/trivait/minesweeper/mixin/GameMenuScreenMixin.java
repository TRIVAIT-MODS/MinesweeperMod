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
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void addMinesweeperButton(CallbackInfo ci) {
        TextIconButtonWidget minesweeperBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> this.client.setScreen(new MinesweeperScreen(title)),
                true
        ).width(20).texture(Identifier.of("minesweeper", "icon/button"), 16, 16).build();

        // ставим рядом с кнопкой "Вернуться в игру"
        for (ButtonWidget button : this.children().stream().filter(e -> e instanceof ButtonWidget).map(e -> (ButtonWidget) e).toList()) {
            if (button.getMessage().equals(Text.translatable("menu.returnToGame"))) {
                int buttonX = button.getX();
                int buttonY = button.getY();
                int buttonWidth = button.getWidth();
                minesweeperBtn.setPosition(buttonX + buttonWidth + 5, buttonY);
                break;
            }
        }

        this.addDrawableChild(minesweeperBtn);
    }
}
