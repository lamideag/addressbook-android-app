package com.deepschneider.addressbook.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.view.GravityCompat
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.PersonsListAdapter
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.listeners.OnSwipeTouchListener
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PersonsActivity : AbstractActivity<PersonDto>() {

    private lateinit var personsListView: ListView

    private var currentFilter: List<FilterDto>? = null

    private var start: Int = 1

    private lateinit var organizationDto: OrganizationDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        organizationDto = intent.extras?.get("organization") as OrganizationDto
        title = organizationDto.name
        setContentView(R.layout.activity_person)
        personsListView = findViewById(R.id.persons_activity_list_view)
        personsListView.setOnTouchListener(object :
            OnSwipeTouchListener(this@PersonsActivity) {
            override fun onSwipeTop() {
                this@PersonsActivity.totalListSize?.let {
                    if (start * getPageSize() < it) {
                        start++
                        updateList(getFilter())
                    }
                }
            }

            override fun onSwipeBottom() {
                if (start > 1) {
                    start--
                    updateList(getFilter())
                }
            }
        })
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
                this.getString(R.string.search_person_obj_id),
                findViewById<EditText>(R.id.persons_activity_search_edit_text_id).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_person_obj_first_name),
                findViewById<EditText>(R.id.persons_activity_search_edit_first_name).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_person_obj_last_name),
                findViewById<EditText>(R.id.persons_activity_search_edit_text_last_name).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_person_obj_resume),
                findViewById<EditText>(R.id.persons_activity_search_edit_text_resume).text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_person_obj_salary),
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
        orgIdFilterDto.value = organizationDto.id
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
                logout()
                return true
            }
            R.id.action_sort_settings_persons -> {
                showSortSettingsDialogs()
                return true
            }
            R.id.action_edit_organization -> {
                val intent =
                    Intent(applicationContext, CreateOrEditOrganizationActivity::class.java)
                intent.putExtra("organization", organizationDto)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        updateList(getFilter())
    }

    override fun getRequestTag(): String = "PERSONS_TAG"

    override fun getParentCoordinatorLayoutForSnackBar(): Int =
        R.id.persons_activity_coordinator_layout

    override fun getTargetCache(): String = Constants.PERSONS_CACHE_NAME

    override fun getMainList(): ListView = personsListView

    override fun getStartPage(): Int = start

    override fun getEmptyListView(): Int = R.id.persons_activity_empty_list

    override fun getListAdapter(list: List<PersonDto>): ListAdapter =
        PersonsListAdapter(list, this@PersonsActivity)

    override fun getProgressBar(): Int = R.id.persons_activity_progress_bar

    override fun getTotalListSizeTextView(): Int = R.id.persons_activity_list_total_size

    override fun getMainListType(): Type =
        object : TypeToken<PageDataDto<TableDataDto<PersonDto>>>() {}.type

    override fun getFilter(): List<FilterDto> = currentFilter ?: listOf(getOrgIdFilterDto())

    override fun getFieldListObjNames(): Int = R.array.persons_list_field_obj_names

    override fun getFieldListDisplayNames(): Int = R.array.persons_list_field_display_names

}