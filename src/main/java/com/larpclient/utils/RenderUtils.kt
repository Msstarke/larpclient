package com.larpclient.utils

import com.larpclient.LarpClient
import com.larpclient.features.overlay.GuiEditManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

/**
 * Utility functions for rendering overlays on screen.
 */
object RenderUtils {

    /**
     * Render a list of text lines at a given Position.
     * Automatically registers with GuiEditManager for drag-to-reposition.
     */
    fun renderStringsAt(
        graphics: GuiGraphics,
        position: Position,
        lines: List<String>,
        label: String,
        color: Int = 0xFFFFFF,
        shadow: Boolean = true
    ) {
        if (lines.isEmpty()) return

        val mc = LarpClient.mc
        val font = mc.font
        val screenWidth = mc.window.guiScaledWidth
        val screenHeight = mc.window.guiScaledHeight

        val effectiveX = position.getEffectiveX(screenWidth)
        val effectiveY = position.getEffectiveY(screenHeight)

        val lineHeight = font.lineHeight + 2
        var maxWidth = 0

        val poseStack = graphics.pose()
        poseStack.pushPose()
        poseStack.scale(position.scale, position.scale, 1f)

        val scaledX = (effectiveX / position.scale).toInt()
        val scaledY = (effectiveY / position.scale).toInt()

        for ((index, line) in lines.withIndex()) {
            val width = font.width(line)
            if (width > maxWidth) maxWidth = width

            val y = scaledY + (index * lineHeight)
            if (shadow) {
                graphics.drawString(font, line, scaledX, y, color)
            } else {
                graphics.drawString(font, line, scaledX, y, color, false)
            }
        }

        poseStack.popPose()

        // Register with the overlay editor for drag-to-reposition
        val totalWidth = (maxWidth * position.scale).toInt()
        val totalHeight = (lines.size * lineHeight * position.scale).toInt()
        GuiEditManager.register(position, label, effectiveX, effectiveY, totalWidth, totalHeight)
    }

    /**
     * Render a single component at a position.
     */
    fun renderComponentAt(
        graphics: GuiGraphics,
        position: Position,
        component: Component,
        label: String,
        color: Int = 0xFFFFFF
    ) {
        renderStringsAt(graphics, position, listOf(component.string), label, color)
    }

    /**
     * Draw a filled rectangle with transparency.
     */
    fun drawBackground(
        graphics: GuiGraphics,
        x: Int, y: Int,
        width: Int, height: Int,
        color: Int = 0x80000000.toInt()
    ) {
        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, color)
    }
}
