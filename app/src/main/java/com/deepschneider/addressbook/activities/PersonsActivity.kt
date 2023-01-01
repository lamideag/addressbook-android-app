package com.deepschneider.addressbook.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.PersonsListAdapter
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PersonsActivity : AbstractActivity<PersonDto>() {

    private lateinit var personsListView: ListView

    private var currentFilter: List<FilterDto>? = null

    private var start: Int = 1

    private var pageSize: Int = 15

    private var sortName: String = Constants.PERSONS_ID_FIELD

    private var sortOrder: String = Constants.SORT_ORDER_DESC

    private lateinit var orgId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orgId = intent.getStringExtra("orgId").toString()
        title = intent.getStringExtra("orgName").toString()
        setContentView(R.layout.activity_person)
        personsListView = findViewById(R.id.personsListView)
        prepareActionBar(R.id.personsDrawerMain)
        prepareFloatingActionButton()
        updateUserInfo(R.id.usernamePersons, R.id.rolesListViewPersons)
        updateBuildInfo(
            R.id.version_info_persons,
            R.id.build_info_persons,
            R.id.server_host_persons
        )
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
            updateList(filters)
        }
    }

    private fun getOrgIdFilterDto(): FilterDto {
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
        updateList(currentFilter ?: listOf(getOrgIdFilterDto()))
    }

    override fun getRequestTag(): String = "PERSONS_TAG"

    override fun getParentCoordinatorLayoutForSnackBar(): Int = R.id.personsCoordinatorLayout

    override fun getSortName(): String = sortName

    override fun getTargetCache(): String = Constants.PERSONS_CACHE_NAME

    override fun getSortOrder(): String = sortOrder

    override fun getPageSize(): Int = pageSize

    override fun getMainList(): ListView = personsListView

    override fun getStartPage(): Int = start

    override fun getEmptyListView(): Int = R.id.empty_persons_list

    override fun getListAdapter(list: List<PersonDto>): ListAdapter =
        PersonsListAdapter(list, this@PersonsActivity)

    override fun getProgressBar(): Int = R.id.personsProgressBar

    override fun getMainListType(): Type =
        object : TypeToken<PageDataDto<TableDataDto<PersonDto>>>() {}.type
}