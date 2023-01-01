package com.deepschneider.addressbook.network

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.deepschneider.addressbook.dto.FilterDto
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.utils.NetworkUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class OrganizationsRequest(
    url: String,
    private val filterDto: List<FilterDto>,
    responseListener: Response.Listener<PageDataDto<TableDataDto<OrganizationDto>>>,
    errorListener: Response.ErrorListener,
    private var context: Context
) : Request<PageDataDto<TableDataDto<OrganizationDto>>>(Method.POST, url, errorListener) {

    private var listener: Response.Listener<PageDataDto<TableDataDto<OrganizationDto>>> =
        responseListener
    private val gson = Gson()

    override fun getHeaders(): MutableMap<String, String> {
        return NetworkUtils.addAuthHeader(super.getHeaders(), context)
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<PageDataDto<TableDataDto<OrganizationDto>>> {
        return try {
            val type =
                object : TypeToken<PageDataDto<TableDataDto<OrganizationDto>>>() {}.type
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
            return Response.error(ParseError(e));
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return Response.error(ParseError(e));
        }
    }

    override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
    }

    override fun deliverResponse(response: PageDataDto<TableDataDto<OrganizationDto>>?) {
        listener.onResponse(response)
    }

    override fun getBody(): ByteArray {
        return gson.toJson(filterDto).toByteArray(Charsets.UTF_8)
    }
}