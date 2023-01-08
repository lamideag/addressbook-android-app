package com.deepschneider.addressbook.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.ListView
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.PersonsListAdapter
import com.deepschneider.addressbook.dto.*
import com.deepschneider.addressbook.listeners.OnSwipeTouchListener
import com.deepschneider.addressbook.network.EntityGetRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Urls
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PersonsActivity : AbstractListActivity<PersonDto>() {

    private lateinit var personsListView: ListView
    private var currentFilter: List<FilterDto>? = null
    private var start: Int = 1
    private lateinit var organizationDto: OrganizationDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        organizationDto = intent.extras?.get("organization") as OrganizationDto
        title = organizationDto.name
        setContentView(R.layout.activity_person)
        prepareListView()
        prepareActionBar(R.id.persons_activity_drawer_layout)
        prepareFloatingActionButton()
        preparePersonSearchButton()
    }

    private fun prepareListView(){
        personsListView = findViewById(R.id.persons_activity_list_view)
        personsListView.setOnItemClickListener { parent, _, position, _ ->
            val intent = Intent(applicationContext, CreateOrEditPersonActivity::class.java)
            val personDto: PersonDto = parent.adapter.getItem(position) as PersonDto
            intent.putExtra("person", personDto)
            intent.putExtra("orgId", organizationDto.id)
            startActivity(intent)
        }
        personsListView.setOnTouchListener(object :
            OnSwipeTouchListener(this@PersonsActivity) {

            override fun onSwipeTop() {
                this@PersonsActivity.totalListSize?.let {
                    if (start * Constants.PAGE_SIZE < it) {
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

    private fun updateOrganization() {
        val handler = Handler(Looper.getMainLooper())
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val url = "$serverUrl" + Urls.GET_ORGANIZATION + "?id=${organizationDto.id}"
        executor.execute {
            requestQueue.add(
                EntityGetRequest<OrganizationDto>(
                    url,
                    { response ->
                        response.data?.let {
                            organizationDto = it
                        }
                        handler.post {
                            title = organizationDto.name
                        }
                    },
                    { error ->
                        handler.post {
                            makeErrorSnackBar(error)
                        }
                    },
                    this@PersonsActivity,
                    object : TypeToken<PageDataDto<OrganizationDto>>() {}.type
                ).also { it.tag = getRequestTag() })
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
            val intent = Intent(applicationContext, CreateOrEditPersonActivity::class.java)
            intent.putExtra("orgId", organizationDto.id)
            startActivity(intent)
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
        loadSortSettings()
        updateOrganization()
        updateList(getFilter())
    }

    override fun saveSortSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString("person_list_sort_field", sortName).commit()
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString("person_list_sort_order", sortOrder).commit()
    }

    private fun loadSortSettings() {
        sortName = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("person_list_sort_field", this.getString(R.string.search_person_obj_id))
            .toString()
        sortOrder = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("person_list_sort_order", "desc").toString()
    }

    override fun getRequestTag(): String = "PERSONS_TAG"

    override fun getParentCoordinatorLayoutForSnackBar(): Int = R.id.persons_activity_coordinator_layout

    override fun getTargetCache(): String = Constants.PERSONS_CACHE_NAME

    override fun getMainList(): ListView = personsListView

    override fun getStartPage(): Int = start

    override fun getEmptyListView(): Int = R.id.persons_activity_empty_list

    override fun getListAdapter(list: List<PersonDto>): ListAdapter = PersonsListAdapter(list, this@PersonsActivity)

    override fun getProgressBar(): Int = R.id.persons_activity_progress_bar

    override fun getTotalListSizeTextView(): Int = R.id.persons_activity_list_total_size

    override fun getMainListType(): Type = object : TypeToken<PageDataDto<TableDataDto<PersonDto>>>() {}.type

    override fun getFilter(): List<FilterDto> = currentFilter ?: listOf(getOrgIdFilterDto())

    override fun getFieldListObjNames(): Int = R.array.persons_list_field_obj_names

    override fun getFieldListDisplayNames(): Int = R.array.persons_list_field_display_names
}