package com.larpclient.utils

import com.google.gson.annotations.Expose

/**
 * Represents a draggable screen position for an overlay.
 * Coordinates can be negative to anchor from the right/bottom edge.
 */
class Position(
    @Expose var x: Int = 10,
    @Expose var y: Int = 10,
    @Expose var scale: Float = 1.0f
) {
    /**
     * Get the effective X position, resolving negative values relative to screen width.
     */
    fun getEffectiveX(screenWidth: Int): Int {
        return if (x < 0) screenWidth + x else x
    }

    /**
     * Get the effective Y position, resolving negative values relative to screen height.
     */
    fun getEffectiveY(screenHeight: Int): Int {
        return if (y < 0) screenHeight + y else y
    }

    fun set(newX: Int, newY: Int) {
        x = newX
        y = newY
    }

    fun copy(): Position = Position(x, y, scale)
}
