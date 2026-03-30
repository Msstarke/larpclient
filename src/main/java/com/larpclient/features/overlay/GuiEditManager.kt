package com.larpclient.features.overlay

import com.larpclient.LarpClient
import com.larpclient.utils.Position
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

/**
 * Manages the overlay position editor screen.
 * Overlays register their bounding boxes each frame so they can be dragged.
 */
object GuiEditManager {
    data class OverlayRect(
        val position: Position,
        val label: String,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    private val currentFrameRects = mutableListOf<OverlayRect>()

    fun init() {
        // Initialization hook - called once during mod startup
    }

    /** Called by RenderUtils each frame to register an overlay's bounding box. */
    fun register(position: Position, label: String, x: Int, y: Int, width: Int, height: Int) {
        currentFrameRects.add(OverlayRect(position, label, x, y, width, height))
    }

    /** Get and clear the current frame's overlay rects. */
    fun consumeRects(): List<OverlayRect> {
        val rects = currentFrameRects.toList()
        currentFrameRects.clear()
        return rects
    }

    /** Open the overlay position editor screen. */
    fun openEditor() {
        Minecraft.getInstance().setScreen(OverlayEditorScreen())
    }
}

/**
 * A screen that lets you drag overlay positions around.
 */
class OverlayEditorScreen : Screen(Component.literal("Overlay Position Editor")) {

    private var dragging: GuiEditManager.OverlayRect? = null
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0
    private var lastRects: List<GuiEditManager.OverlayRect> = emptyList()

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Dark transparent background
        renderBackground(graphics, mouseX, mouseY, partialTick)

        // Grab the latest overlay rects
        val rects = GuiEditManager.consumeRects()
        if (rects.isNotEmpty()) {
            lastRects = rects
        }

        val font = Minecraft.getInstance().font

        // Draw instructions
        graphics.drawCenteredString(font, "Drag overlays to reposition them. Press ESC to close.", width / 2, 5, 0xFFFFFF)

        // Draw each overlay's bounding box
        for (rect in lastRects) {
            val x = rect.position.getEffectiveX(width)
            val y = rect.position.getEffectiveY(height)
            val w = rect.width
            val h = rect.height

            // Border - draw manually since method name differs across versions
            val borderColor = 0xFF00FF00.toInt()
            graphics.fill(x - 1, y - 1, x + w + 1, y, borderColor) // top
            graphics.fill(x - 1, y + h, x + w + 1, y + h + 1, borderColor) // bottom
            graphics.fill(x - 1, y, x, y + h, borderColor) // left
            graphics.fill(x + w, y, x + w + 1, y + h, borderColor) // right

            // Label above
            graphics.drawString(font, rect.label, x, y - 10, 0xFF00FF00.toInt())
        }

        super.render(graphics, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(event: MouseButtonEvent, consumed: Boolean): Boolean {
        if (!consumed) {
            val mouseX = event.x()
            val mouseY = event.y()
            for (rect in lastRects) {
                val x = rect.position.getEffectiveX(width)
                val y = rect.position.getEffectiveY(height)
                if (mouseX >= x && mouseX <= x + rect.width && mouseY >= y && mouseY <= y + rect.height) {
                    dragging = rect
                    dragOffsetX = mouseX - x
                    dragOffsetY = mouseY - y
                    return true
                }
            }
        }
        return super.mouseClicked(event, consumed)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        val drag = dragging
        if (drag != null) {
            val newX = (event.x() - dragOffsetX).toInt()
            val newY = (event.y() - dragOffsetY).toInt()
            drag.position.set(newX, newY)
            return true
        }
        return super.mouseDragged(event, dragX, dragY)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (dragging != null) {
            dragging = null
            // Save config after repositioning
            LarpClient.configManager.save()
            return true
        }
        return super.mouseReleased(event)
    }

    override fun isPauseScreen(): Boolean = false
}
