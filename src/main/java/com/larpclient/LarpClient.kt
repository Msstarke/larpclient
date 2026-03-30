package com.larpclient

import com.larpclient.config.ConfigManager
import com.larpclient.config.LarpClientConfig
import com.larpclient.events.EventBus
import com.larpclient.features.overlay.GuiEditManager
import com.larpclient.features.overlay.OverlayManager
import com.larpclient.features.overlay.ExampleOverlay
import com.larpclient.features.update.UpdateManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

object LarpClient {
    const val MOD_ID = "larpclient"
    const val MOD_NAME = "Larp Client"
    const val VERSION = "0.1.0"
    const val GITHUB_OWNER = "your-username"
    const val GITHUB_REPO = "larpclient"

    val logger = LoggerFactory.getLogger(MOD_NAME)
    val mc: Minecraft get() = Minecraft.getInstance()

    lateinit var config: LarpClientConfig
        private set

    lateinit var configManager: ConfigManager
        private set

    lateinit var updateManager: UpdateManager
        private set

    // Keybindings
    lateinit var openGuiKey: KeyMapping
        private set
    lateinit var editOverlaysKey: KeyMapping
        private set

    private var initialized = false

    fun initialize() {
        if (initialized) return
        initialized = true

        logger.info("Initializing $MOD_NAME v$VERSION")

        // Config
        configManager = ConfigManager()
        config = configManager.config

        // Update manager
        updateManager = UpdateManager()
        updateManager.cleanup()

        // Register keybinds using KeyMapping.Category.MISC for the category
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping("key.$MOD_ID.open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC)
        )
        editOverlaysKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping("key.$MOD_ID.edit_overlays", GLFW.GLFW_KEY_F12, KeyMapping.Category.MISC)
        )

        // Register features
        OverlayManager.register(ExampleOverlay)
        GuiEditManager.init()

        // Register tick handler
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            onTick(client)
        }

        // Check for updates on startup
        if (config.update.checkForUpdates) {
            updateManager.checkUpdate()
        }

        logger.info("$MOD_NAME initialized successfully!")
    }

    private var tickCount = 0

    private fun onTick(client: Minecraft) {
        tickCount++

        // Handle keybinds
        while (openGuiKey.consumeClick()) {
            configManager.openConfigGui()
        }
        while (editOverlaysKey.consumeClick()) {
            GuiEditManager.openEditor()
        }

        // Post tick event
        EventBus.post(TickEvent(tickCount))
    }

    data class TickEvent(val tickCount: Int)
}
