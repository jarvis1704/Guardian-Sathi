package com.biprangshu.guardiansathi.Global.Elder.core

import com.biprangshu.guardiansathi.Global.Elder.data.local.ElderNotification
import org.json.JSONArray


fun parseScamNotifications(jsonResponse: String): List<ElderNotification> {
    val result = mutableListOf<ElderNotification>()

    try {
        val jsonArray = JSONArray(jsonResponse.trim())

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val notif = ElderNotification(
                title = obj.optString("title", ""),
                body = obj.optString("body", ""),
                desc = obj.optString("desc", ""),
                imp = obj.optString("imp", "LOW"),
                appName = obj.optString("appName", ""),
                time = obj.optDouble("time", 0.0).toLong()
            )

            result.add(notif)
        }

    } catch (e: Exception) {
        // Return empty list if JSON is malformed
    }

    return result
}