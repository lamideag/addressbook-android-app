package com.deepschneider.addressbook.network

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Urls

class LogoutRequest(
    serverUrl: String,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener,
    private var context: Context,
) : StringRequest(Method.GET, serverUrl + Urls.LOGOUT, listener, errorListener) {
    override fun getHeaders(): MutableMap<String, String> {
        return NetworkUtils.addAuthHeader(super.getHeaders(), context)
    }
}