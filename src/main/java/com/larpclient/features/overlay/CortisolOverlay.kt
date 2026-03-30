package com.larpclient.features.overlay

import com.larpclient.LarpClient
import com.larpclient.events.GuiRenderEvent
import com.larpclient.utils.Position
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.*

/**
 * Cortisol meter — speedometer/gauge style semi-circle.
 * Smooth arc rendering, text shadows, SkyHanni-style colors.
 * Needle sweeps from left (0 = calm) to right (20 = dying).
 *
 * Ported from hypixel-skyblock-ai-bot.
 */
object CortisolOverlay : Overlay {
    override val name = "Cortisol Meter"
    override val position = Position(0, 0) // centered, so position is ignored for X

    private const val RADIUS = 38
    private const val ARC_THICKNESS = 6
    private const val NEEDLE_LEN = 30
    private const val ARC_SEGMENTS = 60
    private const val ARC_FILL_STEPS = 140

    private var displayedHealth = 20f

    // SkyHanni-style colors
    private const val TICK_COLOR = 0xFF1A1A2E.toInt()
    private const val LABEL_COLOR = 0xFF8892A8.toInt()
    private const val LABEL_DIM = 0xFF4A5268.toInt()
    private const val NEEDLE_COLOR = 0xFFE2E8F0.toInt()
    private const val NEEDLE_SHADOW = 0xFF0D0D1A.toInt()
    private const val CENTER_DOT = 0xFF6366F1.toInt()
    private const val CENTER_DOT_INNER = 0xFF0D0D1A.toInt()
    private const val UNFILLED = 0xFF1A1A2E.toInt()
    private const val BG_COLOR = 0x88000000.toInt()

    override fun isEnabled(): Boolean {
        return LarpClient.config.overlays.cortisolMeter
    }

    override fun render(event: GuiRenderEvent.OverlayRender) {
        val mc = LarpClient.mc
        val player = mc.player ?: return
        if (mc.options.hideGui) return
        if (mc.screen != null && mc.screen !is net.minecraft.client.gui.screens.ChatScreen) return

        val graphics = event.graphics
        val health = player.health
        val maxHealth = player.maxHealth
        val absorption = player.absorptionAmount
        val totalHealth = health + absorption

        // Smooth animation
        displayedHealth += (totalHealth - displayedHealth) * 0.1f

        // Cortisol: 0 = calm (full HP), 20 = dying. Negative = overflow (absorption)
        val cortisol = 20f * (1f - displayedHealth / maxHealth.coerceAtLeast(1f))
        val displayCortisol = (cortisol * 10f).roundToInt() / 10f
        val barRatio = (cortisol / 20f).coerceIn(0f, 1f)
        val overflowRatio = if (cortisol < 0) (-cortisol / 20f).coerceAtMost(1f) else 0f

        val screenW = mc.window.guiScaledWidth
        val screenH = mc.window.guiScaledHeight
        val centerX = screenW / 2f
        val centerY = screenH - 55f

        val font = mc.font
        val cx = centerX.toInt()
        val cy = centerY.toInt()

        // Subtle background behind the gauge
        fillArcSmooth(graphics, cx, cy, RADIUS + 6, RADIUS + 6, 0f, 1f, BG_COLOR)

        // Overflow arc (absorption hearts)
        if (overflowRatio > 0) {
            val overflowSegs = (ARC_SEGMENTS * 0.4f).toInt()
            val filledOverflow = (overflowSegs * overflowRatio).toInt()
            for (i in 0 until filledOverflow) {
                val segStart = i.toFloat() / overflowSegs
                val segEnd = (i + 1).toFloat() / overflowSegs
                val a1 = 0.0f - segEnd * 0.4f
                val a2 = 0.0f - segStart * 0.4f
                val color = getOverflowColor(segStart)
                fillArcSmooth(graphics, cx, cy, RADIUS, ARC_THICKNESS, a1, a2, color)
            }
        }

        // Main arc (unfilled background)
        fillArcSmooth(graphics, cx, cy, RADIUS, ARC_THICKNESS, 0f, 1f, UNFILLED)

        // Main arc (filled portion)
        if (barRatio > 0) {
            val filledSteps = (ARC_SEGMENTS * barRatio).toInt()
            for (i in 0..filledSteps) {
                val segStart = i.toFloat() / ARC_SEGMENTS
                val segEnd = minOf(barRatio, (i + 1).toFloat() / ARC_SEGMENTS)
                if (segEnd <= segStart) continue
                val color = getStressColor(segStart)
                fillArcSmooth(graphics, cx, cy, RADIUS, ARC_THICKNESS, segStart, segEnd, color)
            }
        }

        // Tick marks
        for (i in 0..10) {
            val t = i.toFloat() / 10f
            val angle = Math.PI + t * Math.PI
            val tickInner = RADIUS - ARC_THICKNESS - 2
            val tickOuter = RADIUS - ARC_THICKNESS - if (i % 5 == 0) 6 else 3
            val tx1 = cx + (cos(angle) * tickInner).toInt()
            val ty1 = cy + (sin(angle) * tickInner).toInt()
            val tx2 = cx + (cos(angle) * tickOuter).toInt()
            val ty2 = cy + (sin(angle) * tickOuter).toInt()
            val tickCol = if (i % 5 == 0) 0xFF2A2A40.toInt() else TICK_COLOR
            graphics.fill(
                minOf(tx1, tx2), minOf(ty1, ty2),
                maxOf(tx1, tx2) + 1, maxOf(ty1, ty2) + 1, tickCol
            )
        }

        // Needle
        val needleRatio = if (cortisol >= 0) barRatio else -overflowRatio * 0.4f
        val needleAngle = Math.PI + needleRatio * Math.PI
        val nx = cx + (cos(needleAngle) * NEEDLE_LEN).toInt()
        val ny = cy + (sin(needleAngle) * NEEDLE_LEN).toInt()

        // Needle shadow
        drawLine(graphics, cx + 1, cy + 1, nx + 1, ny + 1, NEEDLE_SHADOW)
        // Needle
        val needleCol = when {
            cortisol < 0 -> getOverflowColor(overflowRatio)
            barRatio > 0.7f -> getStressColor(barRatio)
            else -> NEEDLE_COLOR
        }
        drawLine(graphics, cx, cy, nx, ny, needleCol)

        // Center dot
        graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, CENTER_DOT)
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, CENTER_DOT_INNER)

        // Pulsing glow when stressed
        if (barRatio > 0.7f) {
            val time = System.currentTimeMillis() % 1000
            val pulse = (sin(time / 1000.0 * Math.PI * 2) * 0.15 + 0.15).toFloat()
            val glowAlpha = (pulse * 150 * barRatio).toInt().coerceIn(0, 255)
            val glow = (glowAlpha shl 24) or 0x00FF2020
            fillArcSmooth(graphics, cx, cy, RADIUS + 3, ARC_THICKNESS + 6, 0f, barRatio, glow)
        }

        // Text
        val valColor = if (cortisol < 0) getOverflowColor(overflowRatio) else getStressColor(barRatio)
        val lblColor = when {
            cortisol < 0 -> getOverflowColor(overflowRatio)
            barRatio > 0.6f -> getStressColor(barRatio)
            else -> LABEL_COLOR
        }

        val valStr = "%.1f".format(displayCortisol)
        graphics.drawString(font, valStr, cx - font.width(valStr) / 2, cy + 3, valColor, true)
        graphics.drawString(font, "0", cx - RADIUS - 10, cy - 4, LABEL_DIM, true)
        graphics.drawString(font, "20", cx + RADIUS + 3, cy - 4, LABEL_DIM, true)
        val label = "CORTISOL"
        graphics.drawString(font, label, cx - font.width(label) / 2, cy - RADIUS - 10, lblColor, true)
    }

    // ===================== Smooth arc rendering =====================

    private fun fillArcSmooth(
        graphics: GuiGraphics, cx: Int, cy: Int,
        radius: Int, thickness: Int,
        startRatio: Float, endRatio: Float, color: Int
    ) {
        for (i in 0 until ARC_FILL_STEPS) {
            val t1 = startRatio + (endRatio - startRatio) * i / ARC_FILL_STEPS
            val t2 = startRatio + (endRatio - startRatio) * (i + 1) / ARC_FILL_STEPS
            val a1 = Math.PI + t1 * Math.PI
            val a2 = Math.PI + t2 * Math.PI

            val ox1 = cx + (cos(a1) * radius).toInt()
            val oy1 = cy + (sin(a1) * radius).toInt()
            val ox2 = cx + (cos(a2) * radius).toInt()
            val oy2 = cy + (sin(a2) * radius).toInt()
            val ix1 = cx + (cos(a1) * (radius - thickness)).toInt()
            val iy1 = cy + (sin(a1) * (radius - thickness)).toInt()
            val ix2 = cx + (cos(a2) * (radius - thickness)).toInt()
            val iy2 = cy + (sin(a2) * (radius - thickness)).toInt()

            val minX = minOf(ox1, ox2, ix1, ix2)
            val maxX = maxOf(ox1, ox2, ix1, ix2)
            val minY = minOf(oy1, oy2, iy1, iy2)
            val maxY = maxOf(oy1, oy2, iy1, iy2)

            if (maxX > minX && maxY > minY) {
                graphics.fill(minX, minY, maxX, maxY, color)
            }
        }
    }

    private fun drawLine(graphics: GuiGraphics, x0: Int, y0: Int, x1: Int, y1: Int, color: Int) {
        var cx = x0
        var cy = y0
        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx - dy
        while (true) {
            graphics.fill(cx, cy, cx + 1, cy + 1, color)
            if (cx == x1 && cy == y1) break
            val e2 = 2 * err
            if (e2 > -dy) { err -= dy; cx += sx }
            if (e2 < dx) { err += dx; cy += sy }
        }
    }

    // ===================== Color functions =====================

    private fun getOverflowColor(t: Float): Int {
        val r = (20 + 30 * (1 - t)).toInt()
        val g = (200 + 55 * (1 - t)).toInt()
        val b = (220 + 35 * (1 - t)).toInt()
        return 0xFF000000.toInt() or (cl(r) shl 16) or (cl(g) shl 8) or cl(b)
    }

    private fun getStressColor(stress: Float): Int {
        val r: Int
        val g: Int
        val b: Int
        when {
            stress < 0.35f -> {
                val t = stress / 0.35f
                r = (30 + 225 * t).toInt()
                g = (200 + 55 * (1 - t)).toInt()
                b = (40 * (1 - t)).toInt()
            }
            stress < 0.65f -> {
                val t = (stress - 0.35f) / 0.3f
                r = 255
                g = (230 - 130 * t).toInt()
                b = 0
            }
            else -> {
                val t = (stress - 0.65f) / 0.35f
                r = 255
                g = (100 - 100 * t).toInt()
                b = (30 * t).toInt()
            }
        }
        return 0xFF000000.toInt() or (cl(r) shl 16) or (cl(g) shl 8) or cl(b)
    }

    private fun cl(v: Int): Int = v.coerceIn(0, 255)
}
