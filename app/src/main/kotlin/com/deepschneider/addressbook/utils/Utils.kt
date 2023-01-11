package com.deepschneider.addressbook.utils

import android.app.Activity
import android.os.Build
import com.deepschneider.addressbook.dto.FilterDto
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

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
            "MM/dd/yy",
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
}