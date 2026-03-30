package com.larpclient.config

import com.larpclient.LarpClient
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

object ModMenuInterop : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            LarpClient.configManager.openConfigGui()
            null // openConfigGui sets the screen directly
        }
    }
}
