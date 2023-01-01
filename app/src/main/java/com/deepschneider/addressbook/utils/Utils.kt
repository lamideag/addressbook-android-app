package com.deepschneider.addressbook.utils

import com.deepschneider.addressbook.dto.FilterDto
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

    fun getDateFilterDto(name: String, value: String?): FilterDto? {
        if (value.isNullOrBlank()) return null
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
        filterDto.comparator = "="
        filterDto.type = "DateFilter"
        return filterDto
    }
}