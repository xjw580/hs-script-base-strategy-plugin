package club.xiaojiawei.hsscriptbasestrategy.ai.llm

import club.xiaojiawei.hsscriptbasestrategy.ai.config.AiConfig
import club.xiaojiawei.hsscriptbase.config.log
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelListResponse(val data: List<ModelItem> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelItem(val id: String = "")

data class ModelTestResult(
    val modelId: String,
    val available: Boolean,
    val responseTime: Long,
    val error: String? = null,
)

object LlmClient {

    private val mapper = jacksonObjectMapper()

    var lastResponseTime: Long = 0

    private val httpClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(AiConfig.timeout().toLong()))
            .build()
    }

    fun chat(messages: List<ChatMessage>): String {
        val baseUrl = AiConfig.baseUrl().trimEnd('/')
        if (baseUrl.isEmpty()) {
            throw IllegalStateException("AI_BASE_URL 未配置")
        }
        val url = buildUrl(baseUrl)
        val request = ChatRequest(
            model = AiConfig.model(),
            messages = messages,
            temperature = AiConfig.temperature(),
        )
        val bodyJson = mapper.writeValueAsString(request)
        log.info { "AI请求: url=$url, model=${request.model}, temperature=${request.temperature}" }

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(AiConfig.timeout().toLong()))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${AiConfig.apiKey()}")
            .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
            .build()

        val start = System.currentTimeMillis()
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        lastResponseTime = System.currentTimeMillis() - start
        if (response.statusCode() !in 200..299) {
            log.error { "AI请求失败, status=${response.statusCode()}, body=${response.body()}" }
            throw RuntimeException("AI请求失败, status=${response.statusCode()}")
        }

        val chatResponse = mapper.readValue(response.body(), ChatResponse::class.java)
        val content = chatResponse.choices.firstOrNull()?.message?.content
            ?: throw RuntimeException("AI响应无内容")
        log.info { "AI响应(${lastResponseTime}ms): $content" }
        return content
    }

    private val mockGameData = """
        {"turn":"my","my_hero":{"name":"猎人","health":30,"armor":0},
        "my_hand":[{"index":0,"name":"测试随从","cost":1,"type":"MINION","atk":1,"hp":1}],
        "my_board":[],"my_mana":{"total":1,"available":1},
        "rival_hero":{"name":"法师","health":30,"armor":0},
        "rival_board":[],"rival_hand_count":3}
    """.trimIndent()

    fun testConnection(): String {
        return try {
            val messages = listOf(
                ChatMessage("system", "你是炉石传说策略AI，返回一个JSON动作。"),
                ChatMessage("user", "当前游戏状态：$mockGameData\n请返回一个动作JSON。"),
            )
            val start = System.currentTimeMillis()
            val response = chat(messages)
            val time = System.currentTimeMillis() - start
            "✅ 连通成功 | 耗时: ${time}ms | 响应: ${response.take(40)}"
        } catch (e: Exception) {
            "❌ 连通失败: ${e.message}"
        }
    }

    fun fetchModelList(): List<String> {
        val baseUrl = AiConfig.baseUrl().trimEnd('/')
        if (baseUrl.isEmpty()) return emptyList()
        val url = if (baseUrl.endsWith("/v1")) "$baseUrl/models" else "$baseUrl/v1/models"
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Bearer ${AiConfig.apiKey()}")
            .GET()
            .build()
        var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 429) {
            Thread.sleep(1000)
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        }
        if (response.statusCode() !in 200..299) {
            throw RuntimeException("拉取模型失败: HTTP ${response.statusCode()}")
        }
        val modelList = mapper.readValue(response.body(), ModelListResponse::class.java)
        return modelList.data.map { it.id }.filter { it.isNotEmpty() }
    }

    fun testModel(modelId: String): ModelTestResult {
        val baseUrl = AiConfig.baseUrl().trimEnd('/')
        if (baseUrl.isEmpty()) {
            return ModelTestResult(modelId, false, 0, "BASE_URL未配置")
        }
        val url = buildUrl(baseUrl)
        val body = mapper.writeValueAsString(ChatRequest(modelId, listOf(ChatMessage("user", "OK")), 0.0))
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${AiConfig.apiKey()}")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val start = System.currentTimeMillis()
        return try {
            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            val time = System.currentTimeMillis() - start
            if (response.statusCode() in 200..299) {
                ModelTestResult(modelId, true, time)
            } else {
                ModelTestResult(modelId, false, time, "HTTP ${response.statusCode()}")
            }
        } catch (e: Exception) {
            ModelTestResult(modelId, false, 0, e.message)
        }
    }

    private fun buildUrl(baseUrl: String): String =
        if (baseUrl.endsWith("/v1")) {
            "$baseUrl/chat/completions"
        } else if (baseUrl.endsWith("/chat/completions")) {
            baseUrl
        } else {
            "$baseUrl/v1/chat/completions"
        }

}
