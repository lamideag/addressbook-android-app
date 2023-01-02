package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.network.ListRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.lang.reflect.Type
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class AbstractActivity<in T> : AppCompatActivity() {

    protected lateinit var toggle: ActionBarDrawerToggle

    private lateinit var requestQueue: RequestQueue

    protected lateinit var mainDrawer: DrawerLayout

    private var serverUrl: String? = null

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue = Volley.newRequestQueue(this)
        serverUrl = NetworkUtils.getServerUrl(this@AbstractActivity)
    }

    protected fun prepareActionBar(drawer: Int) {
        mainDrawer = findViewById(drawer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toggle = ActionBarDrawerToggle(
            this, mainDrawer, R.string.drawer_opened, R.string.drawer_closed
        )
        mainDrawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    abstract fun getRequestTag(): String

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(getRequestTag())
    }

    protected fun makeErrorSnackBar(error: VolleyError) {
        val snackBar = Snackbar.make(
            findViewById<CoordinatorLayout>(getParentCoordinatorLayoutForSnackBar()), when (error) {
                is AuthFailureError -> Constants.FORBIDDEN_MESSAGE
                is TimeoutError -> Constants.SERVER_TIMEOUT_MESSAGE
                is ServerError -> {
                    val result = error.networkResponse?.data?.toString(Charsets.UTF_8)
                    if (result != null) {
                        gson.fromJson(result, AlertDto::class.java).headline.toString()
                    } else {
                        error.message.toString()
                    }
                }
                else -> error.message.toString()
            }, Snackbar.LENGTH_LONG
        )
        val view: View = snackBar.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.setMargins(
            0, (this@AbstractActivity.resources.displayMetrics.density * 100).toInt(), 0, 0
        )
        view.layoutParams = params
        snackBar.show()
    }

    protected fun updateList(filterDto: List<FilterDto>) {
        getMainList().visibility = View.GONE
        findViewById<TextView>(getEmptyListView()).visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(getProgressBar())
        progressBar.visibility = ProgressBar.VISIBLE
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            requestQueue.add(ListRequest(
                "$serverUrl" + Urls.GET_LIST + "?start=${getStartPage()}" + "&pageSize=${getPageSize()}" + "&sortName=${getSortName()}" + "&sortOrder=${getSortOrder()}" + "&cache=${getTargetCache()}",
                filterDto,
                { response ->
                    if (response.data?.data?.isEmpty() == true) {
                        handler.post {
                            progressBar.visibility = ProgressBar.INVISIBLE
                            findViewById<TextView>(getEmptyListView()).visibility = View.VISIBLE
                        }
                    } else {
                        response.data?.data?.let {
                            handler.post {
                                getMainList().adapter = getListAdapter(it)
                                getMainList().visibility = View.VISIBLE
                                progressBar.visibility = ProgressBar.INVISIBLE
                            }
                        }
                    }
                },
                { error ->
                    handler.post {
                        makeErrorSnackBar(error)
                        findViewById<TextView>(getEmptyListView()).visibility = View.VISIBLE
                        progressBar.visibility = ProgressBar.INVISIBLE
                    }
                },
                this@AbstractActivity,
                getMainListType()
            ).also { it.tag = getRequestTag() })
        }
    }

    abstract fun getParentCoordinatorLayoutForSnackBar(): Int

    abstract fun getEmptyListView(): Int

    abstract fun getMainList(): ListView

    abstract fun getProgressBar(): Int

    abstract fun getStartPage(): Int

    abstract fun getPageSize(): Int

    abstract fun getSortName(): String

    abstract fun getSortOrder(): String

    abstract fun getTargetCache(): String

    abstract fun getMainListType(): Type

    abstract fun getListAdapter(list: List<T>): ListAdapter
}