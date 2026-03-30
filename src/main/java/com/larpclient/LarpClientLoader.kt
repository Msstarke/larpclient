package com.larpclient

import com.larpclient.config.CommandManager
import net.fabricmc.api.ClientModInitializer

object LarpClientLoader : ClientModInitializer {
    override fun onInitializeClient() {
        LarpClient.initialize()
        CommandManager.registerCommands()
    }
}
