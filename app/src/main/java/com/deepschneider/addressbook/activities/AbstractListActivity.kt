package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.content.Intent
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
import androidx.preference.PreferenceManager
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

abstract class AbstractListActivity<in T> : AppCompatActivity() {

    protected lateinit var toggle: ActionBarDrawerToggle

    protected lateinit var requestQueue: RequestQueue

    protected lateinit var mainDrawer: DrawerLayout

    protected var serverUrl: String? = null

    private val gson = Gson()

    protected var totalListSize: Int? = null

    protected var sortName: String = "id"

    protected var sortOrder: String = "asc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue = Volley.newRequestQueue(this)
        serverUrl = NetworkUtils.getServerUrl(this@AbstractListActivity)
    }

    protected fun prepareActionBar(drawer: Int) {
        mainDrawer = findViewById(drawer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toggle = ActionBarDrawerToggle(this, mainDrawer, R.string.drawer_opened, R.string.drawer_closed)
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
                is AuthFailureError -> this.getString(R.string.forbidden_message)
                is TimeoutError -> this.getString(R.string.server_timeout_message)
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
            0, (this@AbstractListActivity.resources.displayMetrics.density * 100).toInt(), 0, 0
        )
        view.layoutParams = params
        snackBar.show()
    }

    protected fun updateList(filterDto: List<FilterDto>) {
        getMainList().visibility = View.GONE
        findViewById<TextView>(getTotalListSizeTextView()).visibility = View.GONE
        findViewById<TextView>(getEmptyListView()).visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(getProgressBar())
        progressBar.visibility = ProgressBar.VISIBLE
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            requestQueue.add(ListRequest(
                "$serverUrl" + Urls.GET_LIST + "?start=${getStartPage()}" + "&pageSize=${Constants.PAGE_SIZE}" + "&sortName=${sortName}" + "&sortOrder=${sortOrder}" + "&cache=${getTargetCache()}",
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
                                findViewById<TextView>(getTotalListSizeTextView()).visibility =
                                    View.VISIBLE
                                val totalListSize = response.data?.totalDataSize
                                totalListSize?.let {
                                    var upperBound = getStartPage() * Constants.PAGE_SIZE
                                    if (upperBound > totalListSize) upperBound = totalListSize
                                    val total: String =
                                        "From " + ((getStartPage() - 1) * Constants.PAGE_SIZE + 1) +
                                                " to " + upperBound + " total " + totalListSize
                                    findViewById<TextView>(getTotalListSizeTextView()).text = total
                                    this.totalListSize = totalListSize
                                }
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
                this@AbstractListActivity,
                getMainListType()
            ).also { it.tag = getRequestTag() })
        }
    }

    protected fun logout() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Constants.TOKEN_KEY).commit()
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    protected fun showSortSettingsDialogs() {
        val builderSortField = AlertDialog.Builder(this)
        builderSortField.setTitle(R.string.choose_sort_field).setItems(
            getFieldListDisplayNames()
        ) { dialogField, whichField ->
            sortName = this.resources.getStringArray(getFieldListObjNames())[whichField]
            dialogField.dismiss()
            val builderSortOrder = AlertDialog.Builder(this)
            builderSortOrder.setTitle(R.string.choose_sort_order).setItems(
                R.array.list_sort_order_display_names
            ) { dialogOrder, whichOrder ->
                sortOrder = this.resources.getStringArray(R.array.list_sort_order_obj_names)[whichOrder]
                dialogOrder.dismiss()
                saveSortSettings()
                updateList(getFilter())
            }
            builderSortOrder.create().show()
        }
        builderSortField.create().show()
    }

    abstract fun getParentCoordinatorLayoutForSnackBar(): Int

    abstract fun getEmptyListView(): Int

    abstract fun getMainList(): ListView

    abstract fun getProgressBar(): Int

    abstract fun getStartPage(): Int

    abstract fun getTotalListSizeTextView(): Int

    abstract fun getTargetCache(): String

    abstract fun getMainListType(): Type

    abstract fun getListAdapter(list: List<T>): ListAdapter

    abstract fun getFilter(): List<FilterDto>

    abstract fun getFieldListDisplayNames(): Int

    abstract fun getFieldListObjNames(): Int

    abstract fun saveSortSettings()
}