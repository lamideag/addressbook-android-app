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
import com.deepschneider.addressbook.dto.FilterDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.PersonDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.utils.Constants
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
        personsListView = findViewById(R.id.persons_activity_list_view)
        prepareActionBar(R.id.persons_activity_drawer_layout)
        prepareFloatingActionButton()
        preparePersonSearchButton()
    }

    private fun preparePersonSearchButton() {
        val personSearchButton = findViewById<Button>(R.id.persons_activity_search_button)
        personSearchButton.setOnClickListener {
            mainDrawer.closeDrawer(GravityCompat.START)
            val filters = arrayListOf<FilterDto>()
            Utils.getTextFilterDto(
                Constants.PERSONS_ID_FIELD,
                findViewById<EditText>(R.id.persons_activity_search_edit_text_id).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_FIRST_NAME_FIELD,
                findViewById<EditText>(R.id.persons_activity_search_edit_first_name).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_LAST_NAME_FIELD,
                findViewById<EditText>(R.id.persons_activity_search_edit_text_last_name).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_RESUME_FIELD,
                findViewById<EditText>(R.id.persons_activity_search_edit_text_resume).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                Constants.PERSONS_SALARY_FIELD,
                findViewById<EditText>(R.id.persons_activity_search_edit_text_salary).text.toString()
            )?.let { it1 -> filters.add(it1) }

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
        findViewById<FloatingActionButton>(R.id.persons_activity_fab).setOnClickListener {
            startActivity(Intent(applicationContext, CreateOrEditPersonActivity::class.java))
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
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        updateList(currentFilter ?: listOf(getOrgIdFilterDto()))
    }

    override fun getRequestTag(): String = "PERSONS_TAG"

    override fun getParentCoordinatorLayoutForSnackBar(): Int =
        R.id.persons_activity_coordinator_layout

    override fun getSortName(): String = sortName

    override fun getTargetCache(): String = Constants.PERSONS_CACHE_NAME

    override fun getSortOrder(): String = sortOrder

    override fun getPageSize(): Int = pageSize

    override fun getMainList(): ListView = personsListView

    override fun getStartPage(): Int = start

    override fun getEmptyListView(): Int = R.id.persons_activity_empty_list

    override fun getListAdapter(list: List<PersonDto>): ListAdapter =
        PersonsListAdapter(list, this@PersonsActivity)

    override fun getProgressBar(): Int = R.id.persons_activity_progress_bar

    override fun getTotalListSizeTextView(): Int = R.id.persons_activity_list_total_size

    override fun getMainListType(): Type =
        object : TypeToken<PageDataDto<TableDataDto<PersonDto>>>() {}.type
}