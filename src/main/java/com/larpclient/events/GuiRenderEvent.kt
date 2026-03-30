package com.larpclient.events

import net.minecraft.client.gui.GuiGraphics

/**
 * Events fired during GUI rendering, used by overlays to draw on screen.
 */
sealed class GuiRenderEvent(val graphics: GuiGraphics, val tickDelta: Float) {
    /** Fired every frame on top of the game HUD. Use this for always-visible overlays. */
    class OverlayRender(graphics: GuiGraphics, tickDelta: Float) : GuiRenderEvent(graphics, tickDelta)

    /** Fired every frame on top of everything (including inventory screens). */
    class TopLayerRender(graphics: GuiGraphics, tickDelta: Float) : GuiRenderEvent(graphics, tickDelta)
}
