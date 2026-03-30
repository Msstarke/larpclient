package com.larpclient.features.overlay

import com.larpclient.LarpClient
import com.larpclient.events.EventBus
import com.larpclient.events.GuiRenderEvent

/**
 * Manages all registered overlays and dispatches render events to them.
 */
object OverlayManager {
    private val overlays = mutableListOf<Overlay>()

    init {
        EventBus.subscribe<GuiRenderEvent.OverlayRender> { event ->
            if (!LarpClient.config.general.enabled) return@subscribe
            if (!LarpClient.config.overlays.enableOverlays) return@subscribe

            for (overlay in overlays) {
                try {
                    if (overlay.isEnabled()) {
                        overlay.render(event)
                    }
                } catch (e: Exception) {
                    LarpClient.logger.error("Error rendering overlay '${overlay.name}'", e)
                }
            }
        }
    }

    fun register(overlay: Overlay) {
        overlays.add(overlay)
        LarpClient.logger.info("Registered overlay: ${overlay.name}")
    }

    fun getOverlays(): List<Overlay> = overlays.toList()
}
