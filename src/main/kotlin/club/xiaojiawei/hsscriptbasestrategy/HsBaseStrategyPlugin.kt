package club.xiaojiawei.hsscriptbasestrategy

import club.xiaojiawei.hsscriptbasestrategy.ai.config.AiConfig
import club.xiaojiawei.hsscriptbasestrategy.ai.llm.LlmClient
import club.xiaojiawei.hsscriptstrategysdk.StrategyPlugin
import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.util.Duration

/**
 * @author 肖嘉威
 * @date 2024/9/8 14:57
 */
class HsBaseStrategyPlugin : StrategyPlugin {
    override fun version(): String = VersionInfo.VERSION

    override fun author(): String = "XiaoJiawei"

    override fun description(): String =
        """
        捆绑
        """.trimIndent()

    override fun id(): String = "xjw-base-plugin"

    override fun name(): String = "基础"

    override fun homeUrl(): String = "https://github.com/xjw580/Hearthstone-Script"

    override fun cardSDKVersion(): String? = if (VersionInfo.CARD_SDK_VERSION_USED.endsWith("}")) null else VersionInfo.CARD_SDK_VERSION_USED

    override fun strategySDKVersion(): String? = if (VersionInfo.STRATEGY_SDK_VERSION_USED.endsWith("}")) null else VersionInfo.STRATEGY_SDK_VERSION_USED

    override fun graphicDescription(): VBox {
        val box = VBox(6.0)
        val title = Label("AI决策策略配置").apply { font = Font.font(15.0) }

        val enabledCb = CheckBox("启用AI").apply { isSelected = AiConfig.isEnabled() }
        val baseUrlField = TextField(AiConfig.baseUrl()).apply {
            promptText = "https://dashscope.aliyuncs.com/compatible-mode/v1"
            prefWidth = 480.0
        }
        val modelCombo = ComboBox<String>(FXCollections.observableArrayList()).apply {
            isEditable = true
            editor.text = AiConfig.model()
            promptText = "输入或拉取选择模型"
            prefWidth = 350.0
        }
        val apiKeyField = TextField(AiConfig.apiKey()).apply {
            promptText = "sk-xxx"
            prefWidth = 480.0
        }
        val providerField = TextField(AiConfig.provider()).apply {
            promptText = "openai"
            prefWidth = 200.0
        }
        val timeoutField = TextField(AiConfig.timeout().toString()).apply {
            promptText = "30000"
            prefWidth = 100.0
        }
        val descCb = CheckBox("发送卡牌描述").apply {
            isSelected = AiConfig.includeDesc()
        }
        val descHint = Label("开启后LLM能看到所有卡牌的效果描述，决策更准确但prompt变大、响应变慢。若超时设置较短(如30s)不建议开启，容易超时。").apply {
            font = Font.font(11.0)
            style = "-fx-text-fill: gray;"
            isWrapText = true
            prefWidth = 480.0
        }

        val pullResultLabel = Label("")
        val pullBtn = Button("拉取模型").apply {
            style = "-fx-background-color: #FF9800; -fx-text-fill: white;"
            setOnAction {
                text = "拉取中..."
                isDisable = true
                pullResultLabel.text = ""
                val url = baseUrlField.text.trim()
                val key = apiKeyField.text.trim()
                Thread {
                    try {
                        val models = LlmClient.fetchModelList()
                        if (models.isEmpty()) {
                            Platform.runLater {
                                pullResultLabel.text = "❌ 未拉取到模型"
                                pullResultLabel.style = "-fx-text-fill: red;"
                                text = "拉取模型"
                                isDisable = false
                            }
                            return@Thread
                        }
                        val results = mutableListOf<String>()
                        var done = 0
                        val total = models.size
                        for (modelId in models) {
                            done++
                            Platform.runLater {
                                pullResultLabel.text = "测试中... ($done/$total)"
                            }
                            val r = LlmClient.testModel(modelId)
                            val display = if (r.available) {
                                "$modelId (✅ ${r.responseTime}ms)"
                            } else {
                                "$modelId (❌ ${r.error ?: "失败"})"
                            }
                            results.add(display)
                        }
                        val sorted = results.sortedWith(
                            compareBy(
                                { it.contains("❌") },
                                { it.replace(Regex(".*\\(✅ (\\d+)ms\\)"), "$1").toLongOrNull() ?: Long.MAX_VALUE }
                            )
                        )
                        Platform.runLater {
                            modelCombo.items.clear()
                            modelCombo.items.addAll(sorted)
                            if (sorted.isNotEmpty()) {
                                modelCombo.selectionModel.selectFirst()
                            }
                            val okCount = sorted.count { it.contains("✅") }
                            pullResultLabel.text = "拉取完成: $okCount/$total 可用"
                            pullResultLabel.style = "-fx-text-fill: ${if (okCount > 0) "green" else "red"};"
                            text = "拉取模型"
                            isDisable = false
                        }
                    } catch (e: Exception) {
                        Platform.runLater {
                            pullResultLabel.text = "❌ 拉取失败: ${e.message}"
                            pullResultLabel.style = "-fx-text-fill: red;"
                            text = "拉取模型"
                            isDisable = false
                        }
                    }
                }.start()
            }
        }

        val saveBtn = Button("保存配置").apply {
            style = "-fx-background-color: #4CAF50; -fx-text-fill: white;"
            setOnAction {
                val selectedModel = modelCombo.editor.text.trim().substringBefore(" (")
                AiConfig.setEnabled(enabledCb.isSelected)
                AiConfig.setBaseUrl(baseUrlField.text.trim())
                AiConfig.setModel(selectedModel)
                AiConfig.setApiKey(apiKeyField.text.trim())
                AiConfig.setProvider(providerField.text.trim())
                AiConfig.setTimeout(timeoutField.text.trim().toIntOrNull() ?: 30000)
                AiConfig.setIncludeDesc(descCb.isSelected)
                AiConfig.save()
                text = "已保存 ✓"
                val pause = PauseTransition(Duration.seconds(1.0))
                pause.setOnFinished { text = "保存配置" }
                pause.play()
            }
        }
        val testResultLabel = Label("")
        val testBtn = Button("测试连通性").apply {
            style = "-fx-background-color: #2196F3; -fx-text-fill: white;"
            setOnAction {
                val selectedModel = modelCombo.editor.text.trim().substringBefore(" (")
                AiConfig.setBaseUrl(baseUrlField.text.trim())
                AiConfig.setModel(selectedModel)
                AiConfig.setApiKey(apiKeyField.text.trim())
                text = "测试中..."
                testResultLabel.text = ""
                Thread {
                    val result = LlmClient.testConnection()
                    Platform.runLater {
                        testResultLabel.text = result
                        testResultLabel.style = if (result.startsWith("✅")) "-fx-text-fill: green;" else "-fx-text-fill: red;"
                        text = "测试连通性"
                    }
                }.start()
            }
        }
        val hint = Label("保存后立即生效。配置写入 config/script.ini [ai] 分组。模型选择可手输或拉取。").apply {
            font = Font.font(11.0)
            style = "-fx-text-fill: gray;"
            isWrapText = true
        }
        box.children.addAll(
            title, enabledCb,
            Label("API地址:"), baseUrlField,
            Label("模型:"), HBox(5.0, modelCombo, pullBtn), pullResultLabel,
            Label("API Key:"), apiKeyField,
            Label("Provider:"), providerField,
            Label("超时(ms, 推理模型建议60000+):"), timeoutField,
            descCb, descHint,
            HBox(10.0, saveBtn, testBtn), testResultLabel, hint,
        )
        return box
    }
}
