package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.OrganizationsListAdapter
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class OrganizationActivity : AbstractActivity<OrganizationDto>() {

    private lateinit var searchEditTextLastUpdated: EditText

    private lateinit var searchEditTextLastComparator: EditText

    private lateinit var organizationsListView: ListView

    private lateinit var searchEditTextType: EditText

    private val lastUpdatedCalendar: Calendar = Calendar.getInstance()

    private var currentFilter: List<FilterDto>? = null

    private var start: Int = 1

    private var pageSize: Int = 15

    private var sortName: String = Constants.ORGANIZATIONS_ID_FIELD

    private var sortOrder: String = Constants.SORT_ORDER_DESC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)
        organizationsListView = findViewById(R.id.organizationsListView)
        organizationsListView.setOnItemClickListener { _, view, _, _ ->
            val intent = Intent(applicationContext, PersonsActivity::class.java)
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
        prepareActionBar(R.id.drawerMain)
        prepareFloatingActionButton()
        prepareSearchEditTextLastUpdated()
        prepareSearchEditTextType()
        updateUserInfo(R.id.username, R.id.rolesListView)
        updateBuildInfo(
            R.id.version_info,
            R.id.build_info,
            R.id.server_host
        )
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
            updateList(filters)
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
        updateList(currentFilter ?: emptyList())
    }

    override fun getParentCoordinatorLayoutForSnackBar(): Int = R.id.organizationsCoordinatorLayout

    override fun getRequestTag(): String = "ORGANIZATIONS_TAG"

    override fun getEmptyListView(): Int = R.id.empty_organizations_list

    override fun getMainList(): ListView = organizationsListView

    override fun getProgressBar(): Int = R.id.organizationsProgressBar

    override fun getStartPage(): Int = start

    override fun getPageSize(): Int = pageSize

    override fun getSortName(): String = sortName

    override fun getSortOrder(): String = sortOrder

    override fun getTargetCache(): String = Constants.ORGANIZATIONS_CACHE_NAME

    override fun getMainListType(): Type =
        object : TypeToken<PageDataDto<TableDataDto<OrganizationDto>>>() {}.type

    override fun getListAdapter(list: List<OrganizationDto>): ListAdapter =
        OrganizationsListAdapter(list, this@OrganizationActivity)
}