package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.OrganizationsListAdapter
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.network.ListRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Urls
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class OrganizationActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var mainDrawer: DrawerLayout

    private lateinit var searchEditTextLastUpdated: EditText

    private lateinit var searchEditTextLastComparator: EditText

    private lateinit var organizationsListView: ListView

    private lateinit var searchEditTextType: EditText

    private lateinit var requestQueue: RequestQueue

    private val lastUpdatedCalendar: Calendar = Calendar.getInstance()

    private val requestTag = "ORGANIZATIONS_TAG"

    private var serverUrl: String? = null

    private var currentFilter: List<FilterDto>? = null

    private var start: Int = 1

    private var pageSize: Int = 15

    private var sortName: String = Constants.ORGANIZATIONS_ID_FIELD

    private var sortOrder: String = Constants.SORT_ORDER_DESC

    private var targetCache: String = Constants.ORGANIZATIONS_CACHE_NAME

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)
        requestQueue = Volley.newRequestQueue(this)
        serverUrl = NetworkUtils.getServerUrl(this@OrganizationActivity)
        organizationsListView = findViewById(R.id.organizationsListView)
        organizationsListView.setOnItemClickListener { _, view, _, _ ->
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(
                "orgId",
                view.findViewById<TextView>(R.id.organization_id).text.toString()
            )
            intent.putExtra(
                "orgName",
                view.findViewById<TextView>(R.id.organization_title).text.toString()
            )
            startActivity(intent)
        }
        prepareActionBar()
        prepareFloatingActionButton()
        prepareSearchEditTextLastUpdated()
        prepareSearchEditTextType()
        updateUserInfo()
        updateBuildInfo()
        prepareOrganizationSearchButton()
    }

    private fun prepareOrganizationSearchButton() {
        val organizationSearchButton = findViewById<Button>(R.id.organizations_search)
        organizationSearchButton.setOnClickListener {
            mainDrawer.closeDrawer(GravityCompat.START)
            val filters = arrayListOf<FilterDto>()
            Utils.getTextFilterDto(
                Constants.ORGANIZATIONS_ID_FIELD,
                findViewById<EditText>(R.id.searchEditTextId).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.ORGANIZATIONS_NAME_FIELD,
                findViewById<EditText>(R.id.searchEditTextName).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.ORGANIZATIONS_ADDRESS_FIELD,
                findViewById<EditText>(R.id.searchEditTextAddress).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.ORGANIZATIONS_ZIP_FIELD,
                findViewById<EditText>(R.id.searchEditTextZip).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.ORGANIZATIONS_TYPE_FIELD,
                findViewById<EditText>(R.id.searchEditTextType).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getDateFilterDto(
                Constants.ORGANIZATIONS_LAST_UPDATED_FIELD,
                findViewById<EditText>(R.id.searchEditTextLastUpdated).text.toString(),
                findViewById<EditText>(R.id.searchEditTextComparator).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            currentFilter = filters
            updateOrganizationsList(filters)
        }
    }

    private fun prepareSearchEditTextLastUpdated() {
        searchEditTextLastUpdated = findViewById(R.id.searchEditTextLastUpdated)
        searchEditTextLastUpdated.setOnClickListener {
            var isDataSet = false
            val dataPickerDialog = DatePickerDialog(
                this@OrganizationActivity,
                { _, year, month, day ->
                    with(lastUpdatedCalendar) {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    updateLabel()
                    isDataSet = true
                },
                lastUpdatedCalendar[Calendar.YEAR],
                lastUpdatedCalendar[Calendar.MONTH],
                lastUpdatedCalendar[Calendar.DAY_OF_MONTH]
            )
            dataPickerDialog.setOnDismissListener {
                if (!isDataSet) {
                    searchEditTextLastUpdated.text = null
                    searchEditTextLastUpdated.gravity = Gravity.LEFT
                }
            }
            dataPickerDialog.show()
        }
        searchEditTextLastComparator = findViewById(R.id.searchEditTextComparator)
        searchEditTextLastComparator.setOnClickListener {
            val builder = AlertDialog.Builder(this@OrganizationActivity)
            builder.setTitle(R.string.choose_date_comparator)
                .setItems(
                    R.array.date_comparators
                ) { dialog, which ->
                    if (which == 0) {
                        searchEditTextLastComparator.text = null
                        searchEditTextLastComparator.gravity = Gravity.LEFT
                    } else {
                        searchEditTextLastComparator.setText(resources.getStringArray(R.array.date_comparators)[which])
                        searchEditTextLastComparator.gravity = Gravity.CENTER
                    }
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    private fun prepareSearchEditTextType() {
        searchEditTextType = findViewById(R.id.searchEditTextType)
        searchEditTextType.setOnClickListener {
            val builder = AlertDialog.Builder(this@OrganizationActivity)
            builder.setTitle(R.string.choose_organization_type)
                .setItems(
                    R.array.org_types
                ) { dialog, which ->
                    if (which == 0)
                        searchEditTextType.text = null
                    else
                        searchEditTextType.setText(resources.getStringArray(R.array.org_types)[which])
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    private fun prepareFloatingActionButton() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(applicationContext, CreateNewOrganizationActivity::class.java))
        }
    }

    private fun prepareActionBar() {
        mainDrawer = findViewById(R.id.drawerMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toggle = ActionBarDrawerToggle(
            this,
            mainDrawer,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        mainDrawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun updateBuildInfo() {
        requestQueue.add(object : JsonObjectRequest(
            Method.GET,
            serverUrl + Urls.BUILD_INFO,
            null,
            { response ->
                val buildInfo = gson.fromJson(response.toString(), BuildInfoDto::class.java)
                findViewById<TextView>(R.id.version_info).text =
                    "version: " + buildInfo.version?.uppercase()
                findViewById<TextView>(R.id.build_info).text =
                    "build: " + buildInfo.time?.uppercase()
                findViewById<TextView>(R.id.server_host).text =
                    "server host: " + buildInfo.serverHost?.uppercase()
            },
            { error ->
                makeErrorSnackBar(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return NetworkUtils.addAuthHeader(super.getHeaders(), this@OrganizationActivity)
            }
        }.also { it.tag = requestTag })
    }

    private fun updateUserInfo() {
        requestQueue.add(object : JsonObjectRequest(
            Method.GET,
            serverUrl + Urls.USER_INFO,
            null,
            { response ->
                val result = gson.fromJson(response.toString(), User::class.java)
                findViewById<TextView>(R.id.username).text = result.login.uppercase()
                findViewById<ListView>(R.id.rolesListView).adapter = ArrayAdapter(
                    this, android.R.layout.simple_list_item_1, result.roles
                )
            },
            { error ->
                makeErrorSnackBar(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return NetworkUtils.addAuthHeader(super.getHeaders(), this@OrganizationActivity)
            }
        }.also { it.tag = requestTag })
    }

    private fun updateLabel() {
        searchEditTextLastUpdated.setText(
            SimpleDateFormat(
                "MM/dd/yy",
                Locale.US
            ).format(lastUpdatedCalendar.time)
        )
        searchEditTextLastUpdated.gravity = Gravity.CENTER
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_organization, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return when (item.itemId) {
            R.id.action_logout_organizations -> {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .remove(Constants.TOKEN_KEY).commit()
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        findViewById<TextView>(R.id.server_info).text =
            "server: " + NetworkUtils.getServerUrl(this@OrganizationActivity)
        super.onResume()
        updateOrganizationsList(currentFilter ?: emptyList())
    }

    private fun updateOrganizationsList(filterDto: List<FilterDto>) {
        organizationsListView.visibility = View.GONE
        findViewById<TextView>(R.id.empty_organizations_list).visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(R.id.organizationsProgressBar)
        progressBar.visibility = ProgressBar.VISIBLE
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            requestQueue.add(ListRequest(
                "$serverUrl" + Urls.GET_LIST + "?start=$start" +
                        "&pageSize=$pageSize" +
                        "&sortName=$sortName" +
                        "&sortOrder=$sortOrder" +
                        "&cache=$targetCache",
                filterDto,
                { response ->
                    if (response.data?.data?.isEmpty() == true) {
                        handler.post {
                            progressBar.visibility = ProgressBar.INVISIBLE
                            findViewById<TextView>(R.id.empty_organizations_list).visibility =
                                View.VISIBLE
                        }
                    } else {
                        response.data?.data?.let {
                            handler.post {
                                organizationsListView.adapter =
                                    OrganizationsListAdapter(it, this@OrganizationActivity)
                                organizationsListView.visibility = View.VISIBLE
                                progressBar.visibility = ProgressBar.INVISIBLE
                            }
                        }
                    }
                },
                { error ->
                    handler.post {
                        makeErrorSnackBar(error)
                        findViewById<TextView>(R.id.empty_organizations_list).visibility =
                            View.VISIBLE
                        progressBar.visibility = ProgressBar.INVISIBLE
                    }
                },
                this@OrganizationActivity,
                object : TypeToken<PageDataDto<TableDataDto<OrganizationDto>>>() {}.type
            ).also { it.tag = requestTag })
        }
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(requestTag)
    }

    private fun makeErrorSnackBar(error: VolleyError) {
        val snackBar = Snackbar.make(
            findViewById<CoordinatorLayout>(R.id.organizationsCoordinatorLayout),
            when (error) {
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
            },
            Snackbar.LENGTH_LONG
        )
        val view: View = snackBar.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.setMargins(
            0,
            (this@OrganizationActivity.resources.displayMetrics.density * 100).toInt(),
            0,
            0
        )
        view.layoutParams = params
        snackBar.show()
    }
}