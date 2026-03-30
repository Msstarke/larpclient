package com.larpclient.features.overlay

import com.larpclient.events.GuiRenderEvent
import com.larpclient.utils.Position

/**
 * Base interface for all overlays. Implement this to create a new overlay.
 */
interface Overlay {
    /** Unique name for this overlay, shown in the position editor. */
    val name: String

    /** The position of this overlay on screen (persisted in config). */
    val position: Position

    /** Whether this overlay should currently render. */
    fun isEnabled(): Boolean

    /** Render the overlay. Called every frame when enabled. */
    fun render(event: GuiRenderEvent.OverlayRender)
}
