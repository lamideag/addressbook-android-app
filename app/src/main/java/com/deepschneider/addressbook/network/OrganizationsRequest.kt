package com.deepschneider.addressbook.network

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.TableDataDto
import org.json.JSONObject
import java.nio.charset.Charset


class OrganizationsRequest : Request<PageDataDto<TableDataDto<Any>>> {
    private var mMethod = 0
    private var mUrl: String? = null
    private var mParams: Map<String, String>? = null
    private var mListener: Response.Listener<JSONObject>? = null


    constructor(
        method: Int, url: String?, params: Map<String?, String?>,
        reponseListener: Listener<JSONObject?>, errorListener: ErrorListener?
    ) {
        super(method, url, errorListener)
        mMethod = method
        mUrl = url
        this.mParams = params
        this.mListener = reponseListener
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<T> {
        return try {
            val json = String(
                response?.data ?: ByteArray(0),
                Charset.forName(HttpHeaderParser.parseCharset(response?.headers)))
            Response.success(
                gson.fromJson(json, clazz),
                HttpHeaderParser.parseCacheHeaders(response))
        } catch ()
    }

    override fun deliverResponse(response: T) = listener.onResponse(response)

}