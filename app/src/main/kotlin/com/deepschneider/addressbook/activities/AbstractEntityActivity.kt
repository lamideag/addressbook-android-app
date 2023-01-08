package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.AlertDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.network.EntityGetRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbstractEntityActivity : AppCompatActivity() {

    protected lateinit var requestQueue: RequestQueue
    private var showLockNotifications = true
    protected var serverUrl: String? = null
    protected val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue = Volley.newRequestQueue(this)
        serverUrl = NetworkUtils.getServerUrl(this@AbstractEntityActivity)
        showLockNotifications = PreferenceManager.getDefaultSharedPreferences(this@AbstractEntityActivity)
            .getBoolean(Constants.SETTINGS_SHOW_LOCK_NOTIFICATIONS, true)
    }

    protected fun sendLockRequest(lock: Boolean, cache: String, id: String) {
        val handler = Handler(Looper.getMainLooper())
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val url = "$serverUrl" + (if (lock) Urls.LOCK_RECORD else Urls.UNLOCK_RECORD) + "?type=${cache}" + "&id=${id}"
        executor.execute {
            requestQueue.add(
                EntityGetRequest<AlertDto>(
                    url,
                    { response ->
                        response.data?.let {
                            if (showLockNotifications) {
                                handler.post {
                                    it.headline?.let { headline -> makeSnackBar(headline) }
                                }
                            }
                        }
                    },
                    { error ->
                        handler.post {
                            makeErrorSnackBar(error)
                        }
                    },
                    this@AbstractEntityActivity,
                    object : TypeToken<PageDataDto<AlertDto>>() {}.type
                ).also { it.tag = getRequestTag() })
        }
    }

    protected fun makeSnackBar(message: String) {
        Snackbar.make(
            findViewById(getParentCoordinatorLayoutForSnackBar()),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    protected fun makeErrorSnackBar(error: VolleyError) {
        when (error) {
            is AuthFailureError -> makeSnackBar(this.getString(R.string.forbidden_message))
            is TimeoutError -> makeSnackBar(this.getString(R.string.server_timeout_message))
            is ServerError -> {
                val result = error.networkResponse?.data?.toString(Charsets.UTF_8)
                if (result != null) {
                    makeSnackBar(gson.fromJson(result, AlertDto::class.java).headline.toString())
                } else {
                    makeSnackBar(error.message.toString())
                }
            }
            else -> makeSnackBar(error.message.toString())
        }
    }

    abstract fun getParentCoordinatorLayoutForSnackBar(): Int

    abstract fun getRequestTag(): String
}