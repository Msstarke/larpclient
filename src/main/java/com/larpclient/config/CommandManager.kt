package com.larpclient.config

import com.larpclient.LarpClient
import com.larpclient.features.overlay.GuiEditManager
import com.larpclient.features.update.UpdateManager
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object CommandManager {
    fun registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            // /larp - opens config GUI
            dispatcher.register(
                ClientCommandManager.literal("larp")
                    .executes { context ->
                        LarpClient.configManager.openConfigGui()
                        1
                    }
                    .then(
                        ClientCommandManager.argument("search", StringArgumentType.greedyString())
                            .executes { context ->
                                val search = StringArgumentType.getString(context, "search")
                                LarpClient.configManager.openConfigGui(search)
                                1
                            }
                    )
            )

            // /larpedit - opens overlay position editor
            dispatcher.register(
                ClientCommandManager.literal("larpedit")
                    .executes { context ->
                        GuiEditManager.openEditor()
                        1
                    }
            )

            // /larpupdate - check for updates
            dispatcher.register(
                ClientCommandManager.literal("larpupdate")
                    .executes { context ->
                        LarpClient.updateManager.checkUpdate(notify = true)
                        1
                    }
            )
        }
    }
}
