package com.deepschneider.addressbook.utils

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.HashMap

object NetworkUtils {
    fun addAuthHeader(
        sourceHeaders: MutableMap<String, String>?,
        context: Context
    ): MutableMap<String, String> {
        var headers = sourceHeaders
        if (headers == null || headers == emptyMap<String, String>()) {
            headers = HashMap()
        }
        headers["authorization"] =
            "Bearer " + PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.TOKEN_KEY, Constants.NO_VALUE)
        return headers
    }

    fun getServerUrl(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("server_url", Constants.NO_VALUE)
    }
}