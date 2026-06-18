package org.trivait.minesweeper.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

    @Inject(method = "init", at = @At("TAIL"))
    private void addMinesweeperButton(CallbackInfo ci) {
        Config cfg = MinesweeperModClient.CONFIG;
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }
        SpriteIconButton minesweeperBtn = SpriteIconButton.builder(
                Component.empty(),
                (button) -> {
                    SavedGame saved = MinesweeperModClient.getSavedGame();
                    if (saved != null) {
                        this.minecraft.setScreenAndShow(new MinesweeperScreen(saved, cfg.enableAnimations, GameMode.DEFAULT));
                    } else {
                        this.minecraft.setScreenAndShow(new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), cfg.enableAnimations, GameMode.DEFAULT));
                    }
                },
                true
        ).width(20).sprite(Identifier.fromNamespaceAndPath("minesweeper", "icon/button"), 16, 16).build();

        minesweeperBtn.setPosition(
                cfg.mainMenuButtonPosition.getX(this.width),
                cfg.mainMenuButtonPosition.getY(this.height) + (getModMenuState() ? 24 : 0)
        );

        if (!cfg.mainMenuButtonPosition.equals(MainMenuButtonPosition.ICONS)) {
            this.addRenderableWidget(minesweeperBtn);
        }
    }

    @Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
    @Expression("numberOfButtons = ?")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void adjustAmountOfIconButtons(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons, @Share("addMinesweeperIconWidget") LocalBooleanRef addMinesweeperIconWidget) {
        Config cfg = MinesweeperModClient.CONFIG;
        if (cfg.mainMenuButtonPosition == null) {
            cfg.mainMenuButtonPosition = MainMenuButtonPosition.RIGHT_MULTIPLAYER;
        }

        if (cfg.mainMenuButtonPosition.equals(MainMenuButtonPosition.ICONS)) {
            addMinesweeperIconWidget.set(true);
            numberOfButtons.set(numberOfButtons.get() + 1);
        }
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;getHorizontalPosition(III)I"))
    private int replaceInlinedConstant(TitleScreen instance, int currentButton, int numberOfButtons, int buttonWidth, Operation<Integer> original, @Local(name = "numberOfButtons") int actualNumberOfButtons) {
        return original.call(instance, currentButton, actualNumberOfButtons, buttonWidth);
    }

    @Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/TitleScreen;width:I")
    @Expression("this.width / 2 - 100")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private void addMinewseeperIconWidget(CallbackInfo ci, @Local(name = "currentButton") LocalIntRef currentButton, @Local(name = "topPos") int topPos, @Local(name = "numberOfButtons") int numberOfButtons, @Share("addMinesweeperIconWidget") LocalBooleanRef addMinesweeperIconWidget) {
        if (!addMinesweeperIconWidget.get()) return;
        currentButton.set(currentButton.get()+1);
        Screen screen = (TitleScreen) (Object) this;
        Config cfg = MinesweeperModClient.CONFIG;

        SpriteIconButton minesweeperBtn = SpriteIconButton.builder(
                Component.empty(),
                (button) -> {
                    SavedGame saved = MinesweeperModClient.getSavedGame();
                    if (saved != null) {
                        this.minecraft.setScreenAndShow(new MinesweeperScreen(saved, cfg.enableAnimations, GameMode.DEFAULT));
                    } else {
                        this.minecraft.setScreenAndShow(new MinesweeperScreen(new GameSettings(cfg.gridWidth, cfg.gridHeight, cfg.mines), cfg.enableAnimations, GameMode.DEFAULT));
                    }
                },
                true
        ).width(20).sprite(Identifier.fromNamespaceAndPath("minesweeper", "icon/button"), 16, 16).build();

        minesweeperBtn.setTooltip(Tooltip.create(Component.translatable("menu.minesweeper")));

        minesweeperBtn.setPosition(
                this.getHorizontalPosition(currentButton.get(), numberOfButtons, 20),
                topPos
        );

        Screens.getWidgets(screen).add(minesweeperBtn);
    }

    private boolean getModMenuState() {
        if ((ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC)) {
            return false;
        }
        return true;
    }
}
