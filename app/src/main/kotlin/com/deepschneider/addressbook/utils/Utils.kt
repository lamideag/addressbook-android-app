package com.deepschneider.addressbook.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.preference.PreferenceManager
import com.deepschneider.addressbook.dto.FilterDto
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {
    fun getTextFilterDto(name: String, value: String?): FilterDto? {
        if (value.isNullOrBlank()) return null
        val filterDto = FilterDto()
        filterDto.name = name
        filterDto.value = value
        filterDto.comparator = ""
        filterDto.type = "TextFilter"
        return filterDto
    }

    fun getDateFilterDto(name: String, value: String?, comparator: String?): FilterDto? {
        if (value.isNullOrBlank() || comparator.isNullOrBlank()) return null
        val filterDto = FilterDto()
        filterDto.name = name
        filterDto.value = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).parse(value)?.let {
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                Locale.US
            ).format(it)
        }
        filterDto.comparator = comparator
        filterDto.type = "DateFilter"
        return filterDto
    }

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)
        else
            activity.intent.getSerializableExtra(name) as T?
    }

    fun saveBiometrics(context: Context, data: ByteArray) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(Constants.BIOMETRICS, Base64.encodeToString(data, Base64.NO_WRAP))
            .commit()
    }

    fun getBiometrics(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Constants.BIOMETRICS, null)
    }
}