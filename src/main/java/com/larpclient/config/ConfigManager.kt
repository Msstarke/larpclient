package com.larpclient.config

import com.larpclient.LarpClient
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import net.minecraft.client.Minecraft
import java.io.File

class ConfigManager {
    val config: LarpClientConfig
    private val managedConfig: ManagedConfig<LarpClientConfig>

    private val configFile: File
        get() = File(Minecraft.getInstance().gameDirectory, "config/${LarpClient.MOD_ID}.json")

    init {
        managedConfig = ManagedConfig.create(configFile, LarpClientConfig::class.java) {
            checkExpose = true
        }
        config = managedConfig.instance
        LarpClient.logger.info("Config loaded from ${configFile.absolutePath}")
    }

    fun openConfigGui(search: String? = null) {
        if (search != null) {
            managedConfig.getEditor().search(search)
        }
        managedConfig.openConfigGui()
    }

    fun save() {
        managedConfig.saveToFile()
    }
}
