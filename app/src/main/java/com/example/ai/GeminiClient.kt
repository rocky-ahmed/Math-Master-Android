package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST Models ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @param:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiSystemInstruction(
    @param:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @param:Json(name = "temperature") val temperature: Float? = 0.7f,
    @param:Json(name = "topP") val topP: Float? = 0.95f,
    @param:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 2048
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @param:Json(name = "contents") val contents: List<GeminiContent>,
    @param:Json(name = "systemInstruction") val systemInstruction: GeminiSystemInstruction? = null,
    @param:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @param:Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @param:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContentFlash(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContentPro(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @POST("v1beta/models/gemini-3.1-flash-lite-preview:generateContent")
    suspend fun generateContentLite(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// Client singleton
object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    /**
     * Executes a chat completion call with system instructions and chat history.
     */
    suspend fun sendChatMessage(
        messages: List<GeminiContent>,
        systemPrompt: String = SYSTEM_MATH_TUTOR_PROMPT,
        modelType: GeminiModelType = GeminiModelType.FLASH
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "দয়া করে আপনার Gemini API Key AI Studio Secrets প্যানেলে সেট করুন।"
        }

        val request = GeminiRequest(
            contents = messages,
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            )
        )

        try {
            val response = when (modelType) {
                GeminiModelType.FLASH -> apiService.generateContentFlash(apiKey, request)
                GeminiModelType.PRO -> apiService.generateContentPro(apiKey, request)
                GeminiModelType.LITE -> apiService.generateContentLite(apiKey, request)
            }

            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"
        } catch (e: Exception) {
            "একটি সমস্যা হয়েছে: ${e.localizedMessage ?: "সংযোগ ত্রুটি"}"
        }
    }

    const val SYSTEM_MATH_TUTOR_PROMPT = """
আপনি 'Math Master' অ্যাপের একজন বন্ধুসুলভ, দক্ষ ও প্রাঞ্জল বাংলাভাষী গণিত শিক্ষক ও এআই সহকারী (AI Math Tutor)।
আপনার ভূমিকা ও দায়িত্ব:
১. শিক্ষার্থীদের গণিত শেখানো, নামতা (Multiplication Tables ১-৪০), মৌলিক সংখ্যা (Prime Numbers ১-২০০), বর্গ ও বর্গমূল (Squares & Square Roots ১-২০), ঘন ও ঘনমূল (Cubes & Cube Roots ১-২০) এবং বীজগণিতীয় সূত্রাবলি (Algebraic Formulas: বর্গ ও ঘনের সূত্র ও অনুসিদ্ধান্ত) সম্পর্কিত যেকোনো প্রশ্নের উত্তর দেয়া।
২. উত্তর সবসময় বাংলায়, সহজ-সরল ভাষায়, স্পষ্ট ধাপে ধাপে (step-by-step) উপস্থাপন করুন।
৩. গাণিতিক সমীকরণগুলো সুন্দরভাবে ফরম্যাট করে উপস্থাপন করুন।
৪. শিক্ষার্থীকে উৎসাহিত করুন এবং কোনো ভুল হলে স্নেহশীলভাবে বুঝিয়ে দিন।
৫. উত্তর অপ্রয়োজনীয় দীর্ঘ না করে বিষয়ভিত্তিক, নির্ভুল ও আকর্ষণীয় রাখুন।
"""
}

enum class GeminiModelType(val displayNameBn: String, val modelId: String) {
    FLASH("Gemini 3.5 Flash (স্ট্যান্ডার্ড)", "gemini-3.5-flash"),
    PRO("Gemini 3.1 Pro (জটিল গাণিতিক যুক্তি)", "gemini-3.1-pro-preview"),
    LITE("Gemini 3.1 Flash-Lite (দ্রুততম)", "gemini-3.1-flash-lite-preview")
}
