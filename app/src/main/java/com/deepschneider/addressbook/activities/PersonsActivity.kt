package com.deepschneider.addressbook.activities

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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PersonsActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var mainDrawer: DrawerLayout

    private lateinit var personsListView: ListView

    private lateinit var requestQueue: RequestQueue

    private val requestTag = "PERSONS_TAG"

    private var serverUrl: String? = null

    private var currentFilter: List<FilterDto>? = null

    private var start: Int = 1

    private var pageSize: Int = 15

    private var sortName: String = Constants.PERSONS_ID_FIELD

    private var sortOrder: String = Constants.SORT_ORDER_DESC

    private var targetCache: String = Constants.PERSONS_CACHE_NAME

    private val gson = Gson()

    private lateinit var orgId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orgId = intent.getStringExtra("orgId").toString()
        title = intent.getStringExtra("orgName").toString()
        setContentView(R.layout.activity_person)
        requestQueue = Volley.newRequestQueue(this)
        serverUrl = NetworkUtils.getServerUrl(this@PersonsActivity)
        personsListView = findViewById(R.id.personsListView)
        prepareActionBar()
        prepareFloatingActionButton()
        updateUserInfo()
        updateBuildInfo()
        preparePersonSearchButton()
    }

    private fun preparePersonSearchButton() {
        val personSearchButton = findViewById<Button>(R.id.persons_search)
        personSearchButton.setOnClickListener {
            mainDrawer.closeDrawer(GravityCompat.START)
            val filters = arrayListOf<FilterDto>()
            Utils.getTextFilterDto(
                Constants.PERSONS_ID_FIELD,
                findViewById<EditText>(R.id.searchEditTextPersonId).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_FIRST_NAME_FIELD,
                findViewById<EditText>(R.id.searchEditPersonFirstName).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_LAST_NAME_FIELD,
                findViewById<EditText>(R.id.searchEditTextPersonLastName).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_RESUME_FIELD,
                findViewById<EditText>(R.id.searchEditTextResume).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_SALARY_FIELD,
                findViewById<EditText>(R.id.searchEditTextSalary).text.toString()
            )
                ?.let { it1 -> filters.add(it1) }

            filters.add(getOrgIdFilterDto())
            currentFilter = filters
            updatePersonsList(filters)
        }
    }

    private fun getOrgIdFilterDto(): FilterDto{
        val orgIdFilterDto = FilterDto()
        orgIdFilterDto.name = "orgId"
        orgIdFilterDto.value = orgId
        orgIdFilterDto.comparator = ""
        orgIdFilterDto.type = "TextFilter"
        return orgIdFilterDto
    }

    private fun prepareFloatingActionButton() {
        findViewById<FloatingActionButton>(R.id.fab_persons).setOnClickListener {
            startActivity(Intent(applicationContext, CreateNewPersonActivity::class.java))
        }
    }

    private fun prepareActionBar() {
        mainDrawer = findViewById(R.id.personsDrawerMain)
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
                findViewById<TextView>(R.id.version_info_persons).text =
                    "version: " + buildInfo.version?.uppercase()
                findViewById<TextView>(R.id.build_info_persons).text =
                    "build: " + buildInfo.time?.uppercase()
                findViewById<TextView>(R.id.server_host_persons).text =
                    "server host: " + buildInfo.serverHost?.uppercase()
            },
            { error ->
                makeErrorSnackBar(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return NetworkUtils.addAuthHeader(super.getHeaders(), this@PersonsActivity)
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
                findViewById<TextView>(R.id.usernamePersons).text = result.login.uppercase()
                findViewById<ListView>(R.id.rolesListViewPersons).adapter = ArrayAdapter(
                    this, android.R.layout.simple_list_item_1, result.roles
                )
            },
            { error ->
                makeErrorSnackBar(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return NetworkUtils.addAuthHeader(super.getHeaders(), this@PersonsActivity)
            }
        }.also { it.tag = requestTag })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_person, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return when (item.itemId) {
            R.id.action_logout_persons -> {
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
        findViewById<TextView>(R.id.server_info_persons).text =
            "server: " + NetworkUtils.getServerUrl(this@PersonsActivity)
        super.onResume()
        updatePersonsList(currentFilter ?: listOf(getOrgIdFilterDto()))
    }

    private fun updatePersonsList(filterDto: List<FilterDto>) {
        personsListView.visibility = View.GONE
        findViewById<TextView>(R.id.empty_persons_list).visibility = View.GONE
        val progressBar = findViewById<ProgressBar>(R.id.personsProgressBar)
        progressBar.visibility = ProgressBar.VISIBLE
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            requestQueue.add(
                ListRequest(
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
                                findViewById<TextView>(R.id.empty_persons_list).visibility =
                                    View.VISIBLE
                            }
                        } else {
                            response.data?.data?.let {
                                handler.post {
                                    personsListView.adapter =
                                        OrganizationsListAdapter(it, this@PersonsActivity)
                                    personsListView.visibility = View.VISIBLE
                                    progressBar.visibility = ProgressBar.INVISIBLE
                                }
                            }
                        }
                    },
                    { error ->
                        handler.post {
                            makeErrorSnackBar(error)
                            findViewById<TextView>(R.id.empty_persons_list).visibility =
                                View.VISIBLE
                            progressBar.visibility = ProgressBar.INVISIBLE
                        }
                    },
                    this@PersonsActivity,
                    object : TypeToken<PageDataDto<TableDataDto<PersonDto>>>() {}.type
                ).also { it.tag = requestTag })
        }
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(requestTag)
    }

    private fun makeErrorSnackBar(error: VolleyError) {
        val snackBar = Snackbar.make(
            findViewById<CoordinatorLayout>(R.id.personsCoordinatorLayout),
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
            (this@PersonsActivity.resources.displayMetrics.density * 100).toInt(),
            0,
            0
        )
        view.layoutParams = params
        snackBar.show()
    }
}