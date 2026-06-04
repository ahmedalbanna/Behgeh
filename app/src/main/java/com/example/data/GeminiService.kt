package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun findMatchingVenueResponse(
        userInput: String,
        venues: List<Venue>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "عذراً، مفتاح واجهة برمجة تطبيقات Gemini غير مضبوط بشكل صحيح. يرجى تكوينه في لوحة الأسرار (Secrets panel) في الذكاء الاصطناعي لاستخدام مستشار بهجة الذكي الفعلي."
        }

        // Prepare venues description context in Arabic
        val venuesContext = venues.joinToString(separator = "\n") { v ->
            "- القاعة رقم #${v.id}: الاسم (${v.nameAr})، المدينة (${v.city})، السعة القصوى (${v.capacity} ضيف)، السعر الأساسي للكتلة الزمنية (${v.basePrice} ريال سعودي)، الوصف (${v.descriptionAr})، الموقع الجغرافي (${v.address})"
        }

        val systemInstructionText = """
            أنت "مستشار بهجة الذكي لمعاونة العرائس" (Behgeh AI Wedding Consult).
            مهمتك تكمن في مساعدة المستخدمين للبحث عن قاعات حفلات الزفاف والخطوبة المناسبة من قصر البيانات المتاحة أدناه.
            أجب بلغة عربية ودية، دافئة وبطابع راقٍ ومطمئن ممتلئ بعبارات البهجة والسرور.
            قم بتحليل رغبة العميل (مثل المدينة، الميزانية، وعدد الضيوف) ومطابقتها بذكاء مع القاعات التالية:
            $venuesContext
            
            قواعد الإجابة:
            1. حدد القاعة أو القاعات الأقرب لمتطلبات العميل من بين القاعات المتاحة فقط!
            2. قدم تقديراً موجزاً للتكلفة بناءً على الميزانية المذكورة، وشع غبطة وبهجة في قلب العميل وحثه على حجز القاعة واختيار باقات الضيافة والتصوير المدمجة.
            3. إذا لم تكن هناك قاعة تتناسب تماماً، اقترح الخيارات الأقرب واذكر فوارق السعة أو السعر، بلطف.
            4. اجعل الإجابة منسقة ونقية (باستخدام نقاط واضحة وعناوين أنيقة).
        """.trimIndent()

        // Construct standard Gemini JSON request manual body to stay robust or Moshi
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userInput)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstructionText)
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "عذراً، حدث خطأ أثناء الاتصال بخوادم مستشار بهجة الذكي (كود الرد ${response.code}). يرجى التحقق من صحة مفتاح Gemini API."
                }
                val rawBody = response.body?.string() ?: return@withContext "رد فارغ من خادم بهجة الذكي."
                
                val responseObj = JSONObject(rawBody)
                val candidates = responseObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "لا توجد تفاصيل.")
                    }
                }
                return@withContext "لم نتمكن من تحليل التوصيات المناسبة في الوقت الحالي، يرجى إعادة الصياغة."
            }
        } catch (e: Exception) {
            return@withContext "خطأ فني في محرك الذكاء الاصطناعي: ${e.localizedMessage ?: e.message}"
        }
    }
}
