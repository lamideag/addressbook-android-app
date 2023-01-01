package com.deepschneider.addressbook.dto

class PageDataDto<T>(data: T?, fieldDescriptionDtoMap: Map<String, FieldDescriptionDto>?) {

    var data: T? = null
    var fieldDescriptionMap: Map<String, FieldDescriptionDto>? = null

    constructor(data: T?) : this(data, null)

    init {
        this.data = data
        this.fieldDescriptionMap = fieldDescriptionDtoMap
    }
}

class TableDataDto<T> constructor(data: List<T>?, totalDataSize: Int?) {

    var data: List<T>? = null
    var totalDataSize: Int? = null

    init {
        this.data = data
        this.totalDataSize = totalDataSize
    }

    constructor(data: List<T>?) : this(data, data?.size)
}