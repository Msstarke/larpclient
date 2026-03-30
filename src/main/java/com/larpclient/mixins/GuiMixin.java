package com.larpclient.mixins;

import com.larpclient.events.EventBus;
import com.larpclient.events.GuiRenderEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
        EventBus.INSTANCE.post(new GuiRenderEvent.OverlayRender(graphics, tickDelta));
        EventBus.INSTANCE.post(new GuiRenderEvent.TopLayerRender(graphics, tickDelta));
    }
}
