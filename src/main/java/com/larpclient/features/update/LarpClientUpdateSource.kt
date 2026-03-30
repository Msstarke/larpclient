package com.larpclient.features.update

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import com.larpclient.LarpClient
import moe.nea.libautoupdate.UpdateData
import moe.nea.libautoupdate.UpdateSource
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

/**
 * Custom update source that supports private GitHub repos via token auth.
 * Falls back to unauthenticated requests if no token is configured.
 */
class LarpClientUpdateSource(
    private val owner: String,
    private val repo: String
) : UpdateSource {

    private val gson = Gson()

    override fun checkUpdate(updateStream: String): CompletableFuture<UpdateData?> {
        return CompletableFuture.supplyAsync {
            try {
                val releases = fetchReleases()
                if (releases == null || releases.size() == 0) return@supplyAsync null

                val targetMcVersion = getMinecraftVersion()
                val isPrerelease = updateStream.equals("beta", ignoreCase = true)

                for (element in releases) {
                    val release = element.asJsonObject
                    val prerelease = release.get("prerelease")?.asBoolean ?: false
                    if (!isPrerelease && prerelease) continue

                    val tagName = release.get("tag_name")?.asString ?: continue
                    val releaseName = release.get("name")?.asString ?: tagName
                    val assets = release.getAsJsonArray("assets") ?: continue

                    // Find matching JAR for our MC version
                    for (assetElement in assets) {
                        val asset = assetElement.asJsonObject
                        val name = asset.get("name")?.asString ?: continue
                        if (!name.endsWith(".jar")) continue
                        if (name.contains("sources")) continue

                        if (name.contains(targetMcVersion) || assets.size() == 1) {
                            val downloadUrl = asset.get("browser_download_url")?.asString ?: continue
                            return@supplyAsync UpdateData(
                                releaseName,
                                JsonPrimitive(tagName),
                                null,
                                downloadUrl
                            )
                        }
                    }
                }
                null
            } catch (e: Exception) {
                LarpClient.logger.error("Failed to check for updates", e)
                null
            }
        }
    }

    private fun fetchReleases(): JsonArray? {
        val url = URL("https://api.github.com/repos/$owner/$repo/releases")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
        conn.setRequestProperty("User-Agent", "LarpClient/${LarpClient.VERSION}")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        // Add auth token for private repos
        val token = LarpClient.config.update.githubToken
        if (token.isNotBlank()) {
            conn.setRequestProperty("Authorization", "token $token")
        }

        val responseCode = conn.responseCode
        if (responseCode != 200) {
            LarpClient.logger.warn("GitHub API returned $responseCode for releases")
            return null
        }

        return InputStreamReader(conn.inputStream).use { reader ->
            gson.fromJson(reader, JsonArray::class.java)
        }
    }

    private fun getMinecraftVersion(): String {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
            .getModContainer("minecraft")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }
}
