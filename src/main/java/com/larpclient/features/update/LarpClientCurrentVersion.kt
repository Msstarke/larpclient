package com.larpclient.features.update

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.larpclient.LarpClient
import moe.nea.libautoupdate.CurrentVersion

/**
 * Provides the current mod version to the update system.
 * Versions are compared semantically (e.g., 0.1.0 < 0.2.0 < 1.0.0).
 */
class LarpClientCurrentVersion : CurrentVersion {

    override fun display(): String = LarpClient.VERSION

    override fun isOlderThan(otherVersion: JsonElement): Boolean {
        val other = otherVersion.asString
        return compareVersions(LarpClient.VERSION, other) < 0
    }

    companion object {
        /**
         * Compare two semantic version strings.
         * Returns negative if v1 < v2, 0 if equal, positive if v1 > v2.
         */
        fun compareVersions(v1: String, v2: String): Int {
            val parts1 = v1.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
            val parts2 = v2.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLen) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                if (p1 != p2) return p1 - p2
            }
            return 0
        }
    }
}
