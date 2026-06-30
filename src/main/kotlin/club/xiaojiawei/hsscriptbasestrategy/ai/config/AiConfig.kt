package club.xiaojiawei.hsscriptbasestrategy.ai.config

import club.xiaojiawei.hsscriptbase.config.log
import org.ini4j.Ini
import java.io.File
import java.nio.file.Path

object AiConfig {

    private const val SECTION = "ai"
    private const val CONFIG_FILE_NAME = "script.ini"

    private val configFile: File by lazy {
        Path.of(System.getProperty("user.dir", "."), "config", CONFIG_FILE_NAME).toFile()
    }

    private val ini: Ini by lazy {
        val file = configFile
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        Ini(file)
    }

    private val pendingChanges = mutableMapOf<String, String>()

    private fun get(key: String, default: String): String =
        pendingChanges[key] ?: try {
            ini.get(SECTION, key) ?: default
        } catch (e: Exception) {
            default
        }

    private fun put(key: String, value: String) {
        pendingChanges[key] = value
    }

    fun save() {
        try {
            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                configFile.createNewFile()
            }
            val content = configFile.readText()
            val withoutAi = content.replace(Regex("(?s)\\[ai\\].*?(?=\\n\\[|$)"), "").trimEnd()
            val keys = listOf(
                "AI_ENABLED", "AI_BASE_URL", "AI_PROVIDER", "AI_MODEL",
                "AI_API_KEY", "AI_TIMEOUT", "AI_TEMPERATURE",
                "AI_FALLBACK_STRATEGY", "AI_INCLUDE_DESC"
            )
            val defaults = mapOf(
                "AI_ENABLED" to "false", "AI_BASE_URL" to "", "AI_PROVIDER" to "openai",
                "AI_MODEL" to "", "AI_API_KEY" to "", "AI_TIMEOUT" to "30000",
                "AI_TEMPERATURE" to "0.7",
                "AI_FALLBACK_STRATEGY" to "e71234fa-1-radical-deck-97e9-1f4e126cd33b",
                "AI_INCLUDE_DESC" to "false"
            )
            val aiSection = buildString {
                append("\n\n[ai]\n")
                for (key in keys) {
                    val value = pendingChanges[key] ?: ini.get(SECTION, key) ?: defaults[key] ?: ""
                    append("$key = $value\n")
                }
            }
            configFile.writeText(withoutAi + aiSection)
            log.info { "AI配置已保存到 ${configFile.absolutePath}" }
        } catch (e: Exception) {
            log.error { "AI配置保存失败: ${e.message}" }
        }
    }

    fun isEnabled(): Boolean = get("AI_ENABLED", "false").toBoolean()
    fun baseUrl(): String = get("AI_BASE_URL", "")
    fun provider(): String = get("AI_PROVIDER", "openai")
    fun model(): String = get("AI_MODEL", "")
    fun apiKey(): String = get("AI_API_KEY", "")
    fun timeout(): Int = get("AI_TIMEOUT", "30000").toIntOrNull() ?: 30000
    fun temperature(): Double = get("AI_TEMPERATURE", "0.7").toDoubleOrNull() ?: 0.7
    fun fallbackStrategyId(): String = get("AI_FALLBACK_STRATEGY", "e71234fa-1-radical-deck-97e9-1f4e126cd33b")
    fun includeDesc(): Boolean = get("AI_INCLUDE_DESC", "false").toBoolean()

    fun setEnabled(v: Boolean) { put("AI_ENABLED", v.toString()) }
    fun setBaseUrl(v: String) { put("AI_BASE_URL", v) }
    fun setProvider(v: String) { put("AI_PROVIDER", v) }
    fun setModel(v: String) { put("AI_MODEL", v) }
    fun setApiKey(v: String) { put("AI_API_KEY", v) }
    fun setTimeout(v: Int) { put("AI_TIMEOUT", v.toString()) }
    fun setIncludeDesc(v: Boolean) { put("AI_INCLUDE_DESC", v.toString()) }
}
