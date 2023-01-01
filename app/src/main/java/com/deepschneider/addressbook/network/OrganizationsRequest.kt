package com.deepschneider.addressbook.network

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.utils.NetworkUtils
import org.json.JSONObject
import java.nio.charset.Charset


class OrganizationsRequest(
    url: String,
    responseListener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener,
    context: Context
) : Request<PageDataDto<TableDataDto<List<OrganizationDto>>>>(Method.POST, url, errorListener) {

    private var mListener: Response.Listener<JSONObject>? = responseListener
    private var mContext: Context = context

    override fun getHeaders(): MutableMap<String, String> {
        val headers = NetworkUtils.addAuthHeader(super.getHeaders(), mContext)
        headers["Content-Type"] = "application/json; charset=utf-8"
        return headers
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<PageDataDto<TableDataDto<List<OrganizationDto>>>> {
        return try {
            val json = String(
                response?.data ?: ByteArray(0),
                Charset.forName(HttpHeaderParser.parseCharset(response?.headers)))
            Response.success(
                gson.fromJson(json, clazz),
                HttpHeaderParser.parseCacheHeaders(response))
        } catch ()
    }

    override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
    }
}