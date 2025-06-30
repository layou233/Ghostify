package com.launium.ghostify.client.ui.modulelist;

import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.compat.SkyblockerCompat;
import com.launium.ghostify.client.feature.AbstractModule;
import com.launium.ghostify.client.ui.Alignment;
import com.launium.ghostify.client.ui.Easy2D;
import com.launium.ghostify.client.ui.Space;
import com.launium.ghostify.client.ui.animation.Smooth;
import com.launium.ghostify.client.ui.font.FontManager;
import com.launium.ghostify.client.ui.font.RenderInfo;
import com.launium.ghostify.client.ui.font.RenderedText;
import com.launium.ghostify.client.util.ChromaColor;
import com.launium.ghostify.client.util.Commands;
import com.launium.ghostify.client.util.FadingColor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.TreeSet;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class HudModuleList implements LayeredDraw.Layer {
    private long lastEventTime = 0;
    private final TreeSet<AbstractModule> moduleSet = new TreeSet<>();
    private final Smooth animatedStartingY = new Smooth(52, 52);
    private Alignment verticalAlignment = Alignment.START;
    private Alignment horizontalAlignment = Alignment.END;

    public void registerCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.then(literal("list")
                .then(literal("alignment")
                        .then(Commands.thenAlignment(literal("vertical"), alignment -> verticalAlignment = alignment))
                        .then(Commands.thenAlignment(literal("horizontal"), alignment -> horizontalAlignment = alignment))
                ));
    }

    @SkipObfuscation
    @Override
    public void render(GuiGraphics context, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        long now = Util.getMillis();
        final long timeDiff; // final for lambda
        {
            long _timeDiff = now - lastEventTime;
            if (_timeDiff > 40) timeDiff = 40;
            else timeDiff = _timeDiff;
        }
        //moduleSet.removeIf(module -> !module.isActive());
        if (verticalAlignment == Alignment.START) {
            animatedStartingY.update(calculateStartingY(client.player));
        } else {
            animatedStartingY.update(1F);
        }
        animatedStartingY.tick(timeDiff / 120F);
        Window window = client.getWindow();
        float scale = (float) window.getGuiScale();
        Space space = new Space(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        space.allocateVertical(verticalAlignment, animatedStartingY.current, 0);
        RenderedText sampleText = FontManager.requestRenderedText(
                new RenderInfo(FontManager.BOLD_FONT, "dp", 8F), scale);
        float elementHeight = 6F + sampleText.bounds.height / scale;
        Easy2D.configure(context);
        moduleSet.removeIf(module -> {
            RenderedText title = FontManager.requestRenderedText(
                    new RenderInfo(FontManager.BOLD_FONT, module.title(), 8F), scale);
            RenderedText subtitle = null;
            {
                String subtitleText = module.subtitle();
                if (subtitleText != null) {
                    subtitle = FontManager.requestRenderedText(
                            new RenderInfo(FontManager.DEFAULT_FONT, module.subtitle(), 8F), scale);
                }
            }
            float elementWidth = 6F + title.bounds.width / scale + (subtitle == null ? 0F : 1.5F + subtitle.bounds.width / scale);
            float y = space.allocateVertical(verticalAlignment, elementHeight, 2F);
            float x = space.borrowHorizontal(horizontalAlignment, elementWidth, 2F);
            if (module.moduleList.initializeAnimation) {
                module.moduleList.initializeAnimation = false;
                module.moduleList.animatedY.reset(y, y);
                module.moduleList.animatedX.reset(
                        horizontalAlignment.calculate(-2 * elementWidth, window.getGuiScaledWidth() + 2 * elementWidth, elementWidth),
                        x);
            } else if (module.isActive()) {
                module.moduleList.animatedY.update(y);
                module.moduleList.animatedY.tick(timeDiff / 120F);
                module.moduleList.animatedX.update(x);
                module.moduleList.animatedX.tick(timeDiff / 120F);
            } else {
                module.moduleList.animatedY.update(y);
                module.moduleList.animatedY.tick(timeDiff / 120F);
                module.moduleList.animatedX.update(horizontalAlignment.calculate(-2 * elementWidth, window.getGuiScaledWidth() + 2 * elementWidth, 0));
                module.moduleList.animatedX.tick(timeDiff / 120F);
                if (Math.abs(module.moduleList.animatedX.ratio()) > 0.95F) {
                    return true;
                }
            }
            Easy2D.drawRoundRect(module.moduleList.animatedX.current,
                    module.moduleList.animatedY.current,
                    module.moduleList.animatedX.current + elementWidth,
                    module.moduleList.animatedY.current + elementHeight,
                    4F, 5F, 0F, 0x66454545);
            title.draw(context, module.moduleList.animatedX.current + 3F,
                    module.moduleList.animatedY.current + (elementHeight - title.lineHeight / scale) / 2,
                    5F, scale, ChromaColor.pale(3L, ((long) (y - animatedStartingY.current)) << 9, 0xFF));
            if (subtitle != null) {
                subtitle.draw(context, module.moduleList.animatedX.current + 3F + title.bounds.width / scale + 1.5F,
                        module.moduleList.animatedY.current + +(elementHeight - subtitle.lineHeight / scale) / 2,
                        5F, scale, FadingColor.aqua(3L, ((long) (y - animatedStartingY.current)) << 9, 0xFF));
            }
            return false;
        });
        Easy2D.cleanup();
        lastEventTime = now;
    }

    private static float calculateStartingY(Player player) {
        int startingY = 1;
        if (SkyblockerCompat.isEffectOverlayHidden()) {
            return startingY;
        }
        boolean beneficialFound = false;
        // constants are from net.minecraft.client.gui.Gui::renderEffects
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (!effect.showIcon()) continue;
            if (effect.getEffect().value().isBeneficial()) {
                if (!beneficialFound) {
                    beneficialFound = true;
                    startingY = 26; // 1+24+1
                }
            } else {
                startingY = 52; // 1+24+26+1
                break;
            }
        }
        return startingY;
    }

    public void showModule(AbstractModule module) {
        if (module.isActive() && !moduleSet.contains(module)) {
            module.moduleList.initializeAnimation = true;
            moduleSet.add(module);
        }
    }
}
