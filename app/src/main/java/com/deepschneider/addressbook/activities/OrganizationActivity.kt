package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.deepschneider.addressbook.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class OrganizationActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var searchEditTextLastUpdated: EditText

    private lateinit var searchEditTextType: EditText

    private lateinit var requestQueue: RequestQueue

    private val lastUpdatedCalendar: Calendar = Calendar.getInstance()

    private val requestTag = "ORGANIZATIONS_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)
        requestQueue = Volley.newRequestQueue(this)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val organizationsListView = findViewById<ListView>(R.id.organizationsListView)
        organizationsListView.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, arrayOf(
                "Apple", "Microsoft", "IBM", "Oracle", "Red Hat",
                "Citibank", "Netflix", "Nvidia", "Intel", "AMD",
                "Facebook", "Sony", "Nintendo"
            )
        )

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(applicationContext, CreateNewOrganizationActivity::class.java))
        }
        searchEditTextLastUpdated = findViewById(R.id.searchEditTextLastUpdated)
        searchEditTextLastUpdated.setOnClickListener {
            var isDataSet = false
            val dataPickerDialog = DatePickerDialog(
                this@OrganizationActivity,
                { _, year, month, day ->
                    lastUpdatedCalendar.set(Calendar.YEAR, year)
                    lastUpdatedCalendar.set(Calendar.MONTH, month)
                    lastUpdatedCalendar.set(Calendar.DAY_OF_MONTH, day)
                    updateLabel()
                    isDataSet = true
                },
                lastUpdatedCalendar[Calendar.YEAR],
                lastUpdatedCalendar[Calendar.MONTH],
                lastUpdatedCalendar[Calendar.DAY_OF_MONTH]
            )
            dataPickerDialog.setOnDismissListener {
                if (!isDataSet) searchEditTextLastUpdated.text = null
            }
            dataPickerDialog.show()
        }
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

        val serverUrl = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("server_url", "no value")
        requestQueue.add(object : JsonObjectRequest(
            Method.GET,
            "$serverUrl/rest/getUserInfo",
            null,
            { response ->
                findViewById<TextView>(R.id.username).text = (response.get("login") as String?)?.uppercase()
                val array = response.get("roles") as JSONArray
                val roles = arrayListOf<String>()
                for (i in 0 until array.length()) {
                    roles.add(array.get(i).toString())
                }
                findViewById<ListView>(R.id.rolesListView).adapter = ArrayAdapter(
                    this, android.R.layout.simple_list_item_1, roles
                )
            },
            { error ->
                makeErrorSnackBar(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return addAuthHeader(super.getHeaders())
            }
        }.also { it.tag = requestTag })

        requestQueue.add(object : JsonObjectRequest(
            Method.GET,
            "$serverUrl/rest/getBuildInfo",
            null,
            { response ->
                findViewById<TextView>(R.id.version_info).text = "version: " + (response.get("version") as String?)?.uppercase()
                findViewById<TextView>(R.id.build_info).text = "build: " + (response.get("time") as String?)?.uppercase()
            },
            { error ->
                makeErrorSnackBar(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return addAuthHeader(super.getHeaders())
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        findViewById<TextView>(R.id.server_info).text = "server: " +
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("server_url", "no value")
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(requestTag)
    }

    private fun makeErrorSnackBar(error: VolleyError) {
        val snackBar = Snackbar.make(
            findViewById<CoordinatorLayout>(R.id.organizationsCoordinatorLayout),
            when (error) {
                is AuthFailureError -> "FORBIDDEN"
                is TimeoutError -> "SERVER CONNECTION TIMEOUT"
                else -> error.message.toString()
            },
            Snackbar.LENGTH_LONG
        )
        val view: View = snackBar.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBar.show()
    }

    private fun addAuthHeader(sourceHeaders: MutableMap<String, String>?): MutableMap<String, String>{
        var headers = sourceHeaders
        if (headers == null || headers == emptyMap<String, String>()) {
            headers = HashMap()
        }
        headers["authorization"] =
            "Bearer " + PreferenceManager.getDefaultSharedPreferences(this@OrganizationActivity)
                .getString("token", "no value")
        return headers
    }
}