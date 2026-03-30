package com.larpclient.features.update

import com.google.gson.JsonPrimitive
import com.larpclient.LarpClient
import moe.nea.libautoupdate.GithubReleaseUpdateSource
import moe.nea.libautoupdate.UpdateData

/**
 * Custom GitHub release update source that filters release assets
 * to match the current Minecraft version.
 */
class LarpClientUpdateSource(owner: String, repo: String) : GithubReleaseUpdateSource(owner, repo) {

    override fun findAsset(release: GithubRelease): UpdateData? {
        // Look for the JAR that matches our Minecraft version in the filename
        // e.g., LarpClient-0.2.0-mc1.21.10.jar
        val targetVersion = getMinecraftVersion()

        for (asset in release.assets) {
            val name = asset.name
            if (name.endsWith(".jar") && name.contains(targetVersion)) {
                // UpdateData(versionName, versionNumber, sha256, download)
                return UpdateData(
                    release.name ?: release.tagName,
                    JsonPrimitive(release.tagName),
                    null, // sha256 not available from GitHub releases API
                    asset.browserDownloadUrl
                )
            }
        }

        // Fallback: if there's only one JAR, use it
        val jars = release.assets.filter { it.name.endsWith(".jar") && !it.name.contains("sources") }
        if (jars.size == 1) {
            val asset = jars[0]
            return UpdateData(
                release.name ?: release.tagName,
                JsonPrimitive(release.tagName),
                null,
                asset.browserDownloadUrl
            )
        }

        LarpClient.logger.warn("No matching JAR found for MC $targetVersion in release ${release.tagName}")
        return null
    }

    private fun getMinecraftVersion(): String {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
            .getModContainer("minecraft")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }
}
