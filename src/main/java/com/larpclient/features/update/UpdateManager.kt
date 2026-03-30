package com.larpclient.features.update

import com.larpclient.LarpClient
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.GithubReleaseUpdateSource
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateTarget
import moe.nea.libautoupdate.UpdateUtils
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.concurrent.CompletableFuture

class UpdateManager {

    private val context: UpdateContext
    private var potentialUpdate: PotentialUpdate? = null
    private var updateCheckFuture: CompletableFuture<*>? = null

    init {
        context = UpdateContext(
            LarpClientUpdateSource(LarpClient.GITHUB_OWNER, LarpClient.GITHUB_REPO),
            UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager::class.java),
            LarpClientCurrentVersion(),
            LarpClient.MOD_ID
        )
    }

    /**
     * Clean up leftover temp files from a previous update.
     */
    fun cleanup() {
        context.cleanup()
    }

    /**
     * Check for a new update on GitHub releases.
     * @param notify If true, sends a chat message even if no update is found.
     */
    fun checkUpdate(notify: Boolean = false) {
        if (updateCheckFuture != null) {
            if (notify) sendChat("\u00a7eAlready checking for updates...")
            return
        }

        val stream = LarpClient.config.update.updateStream

        LarpClient.logger.info("Checking for updates on stream: $stream")
        updateCheckFuture = context.checkUpdate(stream)
            .thenAcceptAsync({ update ->
                updateCheckFuture = null
                if (update != null && update.isUpdateAvailable) {
                    potentialUpdate = update
                    LarpClient.logger.info("Update available: ${update.update.versionName}")

                    val msg = Component.literal("")
                        .append(prefix())
                        .append(Component.literal("New version available: ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(update.update.versionName).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" (click to download)").withStyle(ChatFormatting.GRAY))
                    msg.style = msg.style.withClickEvent(
                        ClickEvent.RunCommand("/larpupdate download")
                    )
                    sendChat(msg)

                    if (LarpClient.config.update.autoDownload) {
                        downloadUpdate()
                    }
                } else {
                    if (notify) {
                        sendChat("\u00a7aYou are running the latest version!")
                    }
                    LarpClient.logger.info("No updates available")
                }
            }, { runnable -> Minecraft.getInstance().execute(runnable) })
            .exceptionally { e ->
                updateCheckFuture = null
                LarpClient.logger.error("Failed to check for updates", e)
                if (notify) {
                    sendChat("\u00a7cFailed to check for updates. Check the log for details.")
                }
                null
            }
    }

    /**
     * Download and prepare the pending update.
     */
    fun downloadUpdate() {
        val update = potentialUpdate
        if (update == null) {
            sendChat("\u00a7cNo update available to download.")
            return
        }

        sendChat("\u00a7eDownloading update ${update.update.versionName}...")

        update.prepareUpdate()
            .thenRunAsync({
                update.executePreparedUpdate()
                sendChat("\u00a7aUpdate downloaded! Restart the game to apply.")
                LarpClient.logger.info("Update prepared: ${update.update.versionName}")
            }, { runnable -> Minecraft.getInstance().execute(runnable) })
            .exceptionally { e ->
                LarpClient.logger.error("Failed to download update", e)
                sendChat("\u00a7cFailed to download update. Check the log for details.")
                null
            }
    }

    private fun prefix(): MutableComponent {
        return Component.literal("[${LarpClient.MOD_NAME}] ").withStyle(ChatFormatting.AQUA)
    }

    private fun sendChat(message: String) {
        val player = Minecraft.getInstance().player ?: return
        player.sendSystemMessage(Component.literal("").append(prefix()).append(Component.literal(message)))
    }

    private fun sendChat(message: Component) {
        val player = Minecraft.getInstance().player ?: return
        player.sendSystemMessage(message)
    }
}
