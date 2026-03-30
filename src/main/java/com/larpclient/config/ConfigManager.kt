package com.larpclient.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.larpclient.LarpClient
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import net.minecraft.client.Minecraft
import java.io.File

class ConfigManager {
    val config: LarpClientConfig
    private val managedConfig: ManagedConfig

    private val configFile: File
        get() = File(Minecraft.getInstance().gameDirectory, "config/${LarpClient.MOD_ID}.json")

    val gson: Gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting()
        .create()

    init {
        managedConfig = ManagedConfig.create(configFile) {
            checkExpose = true
            it.withConfig(LarpClientConfig::class.java)
        }
        config = managedConfig.instance as LarpClientConfig
        LarpClient.logger.info("Config loaded from ${configFile.absolutePath}")
    }

    fun openConfigGui(search: String? = null) {
        val editor = managedConfig.getEditor()
        if (search != null) {
            editor.search(search)
        }
        Minecraft.getInstance().setScreen(editor.asScreen())
    }

    fun save() {
        managedConfig.saveToFile()
    }
}
