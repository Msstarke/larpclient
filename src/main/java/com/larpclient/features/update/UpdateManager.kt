package com.larpclient.features.update

import com.larpclient.LarpClient
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateData
import moe.nea.libautoupdate.UpdateTarget
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.concurrent.CompletableFuture

class UpdateManager {

    private val updateSource = LarpClientUpdateSource(LarpClient.GITHUB_OWNER, LarpClient.GITHUB_REPO)
    private val updateTarget = UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager::class.java)
    private val currentVersion = LarpClientCurrentVersion()
    private val context = UpdateContext(
        updateSource, updateTarget, currentVersion, LarpClient.MOD_ID
    )

    private var potentialUpdate: PotentialUpdate? = null
    private var updateCheckFuture: CompletableFuture<*>? = null

    fun cleanup() {
        context.cleanup()
    }

    fun checkUpdate(notify: Boolean = false) {
        if (updateCheckFuture != null) {
            if (notify) sendChat("\u00a7eAlready checking for updates...")
            return
        }

        val stream = LarpClient.config.update.updateStream
        LarpClient.logger.info("Checking for updates on stream: $stream")

        // Use our custom source directly (supports private repos with token)
        updateCheckFuture = updateSource.checkUpdate(stream)
            .thenAccept { updateData ->
                updateCheckFuture = null
                if (updateData != null && currentVersion.isOlderThan(updateData.versionNumber)) {
                    val update = PotentialUpdate(updateData, context)
                    potentialUpdate = update
                    LarpClient.logger.info("Update available: ${updateData.versionName}")

                    Minecraft.getInstance().execute {
                        val msg = Component.literal("")
                            .append(prefix())
                            .append(Component.literal("New version available: ").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(updateData.versionName).withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(" - Use /larpupdate to download").withStyle(ChatFormatting.GRAY))
                        sendChat(msg)

                        if (LarpClient.config.update.autoDownload) {
                            downloadUpdate()
                        }
                    }
                } else {
                    if (notify) {
                        Minecraft.getInstance().execute {
                            sendChat("\u00a7aYou are running the latest version!")
                        }
                    }
                    LarpClient.logger.info("No updates available")
                }
            }
            .exceptionally { e ->
                updateCheckFuture = null
                LarpClient.logger.error("Failed to check for updates", e)
                if (notify) {
                    Minecraft.getInstance().execute {
                        sendChat("\u00a7cFailed to check for updates. Check the log for details.")
                    }
                }
                null
            }
    }

    fun downloadUpdate() {
        val update = potentialUpdate
        if (update == null) {
            sendChat("\u00a7cNo update available to download. Run /larpupdate first.")
            return
        }

        sendChat("\u00a7eDownloading update ${update.update.versionName}...")

        update.launchUpdate()
            .thenAccept {
                Minecraft.getInstance().execute {
                    sendChat("\u00a7aUpdate downloaded! Restart the game to apply.")
                }
                LarpClient.logger.info("Update prepared: ${update.update.versionName}")
            }
            .exceptionally { e ->
                LarpClient.logger.error("Failed to download update", e)
                Minecraft.getInstance().execute {
                    sendChat("\u00a7cFailed to download update. Check the log for details.")
                }
                null
            }
    }

    private fun prefix(): MutableComponent {
        return Component.literal("[${LarpClient.MOD_NAME}] ").withStyle(ChatFormatting.AQUA)
    }

    private fun sendChat(message: String) {
        val player = Minecraft.getInstance().player ?: return
        player.displayClientMessage(Component.literal("").append(prefix()).append(Component.literal(message)), false)
    }

    private fun sendChat(message: Component) {
        val player = Minecraft.getInstance().player ?: return
        player.displayClientMessage(message, false)
    }
}
