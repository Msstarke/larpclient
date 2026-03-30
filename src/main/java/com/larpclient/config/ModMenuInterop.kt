package com.larpclient.config

import com.larpclient.LarpClient
import net.fabricmc.loader.api.FabricLoader

/**
 * ModMenu integration. This is registered in fabric.mod.json as a modmenu entrypoint.
 * Since ModMenu is optional, we use reflection-free approach via the entrypoint system.
 * ModMenu will call getModConfigScreenFactory() automatically.
 */
object ModMenuInterop {
    // ModMenu uses the entrypoint system to discover config screen factories.
    // The actual integration is handled by the fabric.mod.json entrypoint declaration.
    // If ModMenu is installed, it will invoke this object and look for a config screen factory.

    @JvmStatic
    fun getConfigScreen(parent: net.minecraft.client.gui.screens.Screen?): net.minecraft.client.gui.screens.Screen? {
        LarpClient.configManager.openConfigGui()
        return null
    }
}
