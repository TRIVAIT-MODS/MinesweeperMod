package org.trivait.minesweeper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.trivait.minesweeper.screen.MinesweeperScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        TextIconButtonWidget minesweeperBtn = TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> this.client.setScreen(new MinesweeperScreen(this.title)),
                true
        ).width(20).texture(Identifier.of("minesweeper", "icon/button"), 16, 16).build();

        // ставим кнопку справа от стандартных
        minesweeperBtn.setPosition(this.width / 2 + 110, this.height / 4 + 48);
        this.addDrawableChild(minesweeperBtn);
    }
}
