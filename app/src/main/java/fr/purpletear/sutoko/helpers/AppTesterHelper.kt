package fr.purpletear.sutoko.helpers

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.example.sutokosharedelements.SutokoSharedElementsData
import com.github.kittinunf.fuel.httpPost
import fr.purpletear.sutoko.BuildConfig
import purpletear.fr.purpleteartools.Language

object AppTesterHelper {


    fun sendReport(
        activity: Activity,
        exception: String,
        value: String?,
        onComplete: (() -> Unit)? = null
    ) {
        val parameters: List<Pair<String, Comparable<*>?>>

        try {
            parameters = listOf(
                "os" to 0,
                "os_version_code" to android.os.Build.VERSION.SDK_INT.toString(),
                "sutoko_version_code" to BuildConfig.VERSION_CODE,
                "exception_message" to exception,
                "optional_value" to value,
                "lang_code" to Language.determineLangDirectory()
            )
        } catch (e: Exception) {
            if (onComplete != null) {
                Handler(Looper.getMainLooper()).post(onComplete)
            }
            return
        }

        var body = "{"
        parameters.forEachIndexed { index, it ->
            if (it.second == null) {
                body += "\"${it.first}\":null"
            } else if (it.second is Int) {
                body += "\"${it.first}\":${it.second}"
            } else {
                body += "\"${it.first}\":\"${it.second}\""
            }

            if (index + 1 != parameters.size) {
                body += ","
            }
        }
        body += "}"
        try {
            SutokoSharedElementsData.getSutokoErrorReportUrl().httpPost(parameters)
                .body(body)
                .timeout(7000)
                .response { _, _, _ ->
                    if (activity.isFinishing) {
                        return@response
                    }
                    Handler(Looper.getMainLooper()).post(onComplete ?: return@response)
                }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post(onComplete ?: return)
        }
    }

}