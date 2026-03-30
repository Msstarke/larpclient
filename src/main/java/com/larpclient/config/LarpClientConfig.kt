package com.larpclient.config

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LarpClientConfig : Config() {

    @Expose
    @Category(name = "General", desc = "General settings for Larp Client")
    val general: GeneralConfig = GeneralConfig()

    @Expose
    @Category(name = "Overlays", desc = "Configure overlay rendering")
    val overlays: OverlayConfig = OverlayConfig()

    @Expose
    @Category(name = "Updates", desc = "Auto-update settings")
    val update: UpdateConfig = UpdateConfig()

    class GeneralConfig {
        @Expose
        @ConfigOption(name = "Enable Mod", desc = "Toggle the entire mod on/off")
        @ConfigEditorBoolean
        var enabled: Boolean = true

        @Expose
        @ConfigOption(name = "Show Welcome Message", desc = "Show a welcome message in chat on login")
        @ConfigEditorBoolean
        var showWelcomeMessage: Boolean = true
    }

    class OverlayConfig {
        @Expose
        @ConfigOption(name = "Enable Overlays", desc = "Toggle all overlay rendering")
        @ConfigEditorBoolean
        var enableOverlays: Boolean = true

        @Expose
        @ConfigOption(name = "Example Overlay", desc = "Show the example overlay on screen")
        @ConfigEditorBoolean
        var exampleOverlay: Boolean = true

        @Expose
        @ConfigOption(name = "Cortisol Meter", desc = "Stress gauge replacing hearts - 0 = calm, 20 = dying")
        @ConfigEditorBoolean
        var cortisolMeter: Boolean = true

        @Expose
        @ConfigOption(name = "Overlay Scale", desc = "Scale factor for overlays")
        @ConfigEditorSlider(minValue = 0.5f, maxValue = 3.0f, minStep = 0.1f)
        var overlayScale: Float = 1.0f
    }

    class UpdateConfig {
        @Expose
        @ConfigOption(name = "Check for Updates", desc = "Automatically check for updates on startup")
        @ConfigEditorBoolean
        var checkForUpdates: Boolean = true

        @Expose
        @ConfigOption(name = "Auto Download", desc = "Automatically download updates when available")
        @ConfigEditorBoolean
        var autoDownload: Boolean = false

        @Expose
        @ConfigOption(name = "Update Stream", desc = "Which release channel to follow (releases or beta)")
        @ConfigEditorText
        var updateStream: String = "releases"

        @Expose
        @ConfigOption(name = "GitHub Token", desc = "Personal access token for private repo updates (leave blank for public repos)")
        @ConfigEditorText
        var githubToken: String = ""
    }
}
