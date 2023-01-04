package com.deepschneider.addressbook.network

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.utils.NetworkUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class LockRequest(
    url: String,
    private val responseListener: Response.Listener<PageDataDto<AlertDto>>,
    errorListener: Response.ErrorListener,
    private var context: Context
) : Request<PageDataDto<AlertDto>>(Method.GET, url, errorListener) {

    private val gson = Gson()

    private val type = object : TypeToken<PageDataDto<AlertDto>>() {}.type

    override fun getHeaders(): MutableMap<String, String> {
        return NetworkUtils.addAuthHeader(super.getHeaders(), context)
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<PageDataDto<AlertDto>> {
        return try {
            val json = String(
                response?.data ?: ByteArray(0),
                Charset.forName(HttpHeaderParser.parseCharset(response?.headers))
            )
            Response.success(
                gson.fromJson(json, type),
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return Response.error(ParseError(e))
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return Response.error(ParseError(e))
        }
    }

    override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
    }

    override fun deliverResponse(response: PageDataDto<AlertDto>?) {
        responseListener.onResponse(response)
    }
}