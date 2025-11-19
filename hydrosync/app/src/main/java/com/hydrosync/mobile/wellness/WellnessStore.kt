package com.hydrosync.mobile.wellness

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class WellnessStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hydrosync_wellness", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGS = "drink_logs"     // JSON array of logs
        private const val KEY_DAILY_NUDGE = "daily_nudge_enabled"
        private const val KEY_SMART_MORNING = "smart_morning_enabled"
    }

    fun addDrinkLog(timestamp: Long, ml: Int) {
        val arr = getLogsArray()
        val obj = JSONObject()
        obj.put("ts", timestamp)
        obj.put("ml", ml)
        arr.put(obj)
        prefs.edit().putString(KEY_LOGS, arr.toString()).apply()
    }

    fun getDrinkLogs(): List<DrinkLog> {
        val arr = getLogsArray()
        val list = mutableListOf<DrinkLog>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            list.add(DrinkLog(o.optLong("ts"), o.optInt("ml")))
        }
        return list
    }

    private fun getLogsArray(): JSONArray {
        val raw = prefs.getString(KEY_LOGS, null) ?: return JSONArray()
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    fun setDailyNudge(enabled: Boolean) = prefs.edit().putBoolean(KEY_DAILY_NUDGE, enabled).apply()
    fun getDailyNudge() = prefs.getBoolean(KEY_DAILY_NUDGE, false)

    fun setSmartMorning(enabled: Boolean) = prefs.edit().putBoolean(KEY_SMART_MORNING, enabled).apply()
    fun getSmartMorning() = prefs.getBoolean(KEY_SMART_MORNING, false)
}

data class DrinkLog(val timestamp: Long, val ml: Int)
