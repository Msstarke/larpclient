package com.larpclient.features.overlay

import com.larpclient.LarpClient
import com.larpclient.events.GuiRenderEvent
import com.larpclient.utils.Position
import com.larpclient.utils.RenderUtils

/**
 * An example overlay that displays mod info on screen.
 * Use this as a template for creating your own overlays.
 */
object ExampleOverlay : Overlay {
    override val name = "Example Overlay"
    override val position = Position(10, 10)

    override fun isEnabled(): Boolean {
        return LarpClient.config.overlays.exampleOverlay
    }

    override fun render(event: GuiRenderEvent.OverlayRender) {
        val lines = buildList {
            add("\u00a7b${LarpClient.MOD_NAME} \u00a77v${LarpClient.VERSION}")
            add("\u00a77FPS: \u00a7f${LarpClient.mc.fps}")
            add("\u00a77Overlays: \u00a7a${OverlayManager.getOverlays().count { it.isEnabled() }} active")
        }

        // Draw a semi-transparent background
        val font = LarpClient.mc.font
        val maxWidth = lines.maxOf { font.width(it.replace("\u00a7.", "")) }
        val totalHeight = lines.size * (font.lineHeight + 2)
        RenderUtils.drawBackground(
            event.graphics,
            position.getEffectiveX(LarpClient.mc.window.guiScaledWidth),
            position.getEffectiveY(LarpClient.mc.window.guiScaledHeight),
            maxWidth,
            totalHeight
        )

        RenderUtils.renderStringsAt(
            graphics = event.graphics,
            position = position,
            lines = lines,
            label = name
        )
    }
}
