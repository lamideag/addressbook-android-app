package com.deepschneider.addressbook.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.OrganizationsListAdapter
import com.deepschneider.addressbook.databinding.ActivityOrganizationBinding
import com.deepschneider.addressbook.dto.FilterDto
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.listeners.OnSwipeTouchListener
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class OrganizationsActivity : AbstractListActivity<OrganizationDto>() {

    private lateinit var binding: ActivityOrganizationBinding
    private val lastUpdatedCalendar: Calendar = Calendar.getInstance()
    private var currentFilter: List<FilterDto>? = null
    private var start: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prepareListView()
        prepareActionBar(binding.organizationsActivityDrawerLayout)
        prepareFloatingActionButton()
        prepareSearchEditTextLastUpdated()
        prepareSearchEditTextType()
        prepareOrganizationSearchButton()
    }

    private fun prepareListView(){
        binding.organizationsActivityListView.setOnItemClickListener { parent, _, position, _ ->
            val intent = Intent(applicationContext, PersonsActivity::class.java)
            val organizationDto: OrganizationDto = parent.adapter.getItem(position) as OrganizationDto
            intent.putExtra("organization", organizationDto)
            startActivity(intent)
        }
        binding.organizationsActivityListView.setOnTouchListener(object :
            OnSwipeTouchListener(this@OrganizationsActivity) {
            override fun onSwipeTop() {
                this@OrganizationsActivity.totalListSize?.let {
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

    private fun prepareOrganizationSearchButton() {
        binding.organizationsActivitySearchButton.setOnClickListener {
            mainDrawer.closeDrawer(GravityCompat.START)
            val filters = arrayListOf<FilterDto>()
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_id),
                binding.organizationsActivitySearchEditTextId.text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_name),
                binding.organizationsActivitySearchEditTextName.text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_address),
                binding.organizationsActivitySearchEditTextAddress.text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_zip),
                binding.organizationsActivitySearchEditTextZip.text.toString()
            )?.let { it1 -> filters.add(it1) }
            Utils.getTextFilterDto(
                this.getString(R.string.search_org_obj_type),
                binding.organizationsActivitySearchEditTextType.text.toString()
            )?.let { it1 -> filters.add(it1) }
            val comparatorEnglish = binding.organizationsActivitySearchEditTextDateComparator.text.toString()
            if (comparatorEnglish.isNotBlank() && comparatorEnglish != this.getString(R.string.no_value_placeholder)) {
                val actualComparatorIndex = this.resources.getStringArray(R.array.date_comparators_english).indexOf(comparatorEnglish)
                val actualComparator = this.resources.getStringArray(R.array.date_comparators)[actualComparatorIndex]
                Utils.getDateFilterDto(
                    this.getString(R.string.search_org_obj_last_updated),
                    binding.organizationsActivitySearchEditTextDateLastUpdated.text.toString(),
                    actualComparator
                )?.let { it1 -> filters.add(it1) }
            }
            currentFilter = filters
            start = 1
            updateList(filters)
        }
    }

    private fun prepareSearchEditTextLastUpdated() {
        binding.organizationsActivitySearchEditTextDateLastUpdated.setOnClickListener {
            var isDataSet = false
            val dataPickerDialog = DatePickerDialog(
                this@OrganizationsActivity,
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
                    binding.organizationsActivitySearchEditTextDateLastUpdated.text = null
                    binding.organizationsActivitySearchEditTextDateLastUpdated.gravity = Gravity.LEFT
                }
            }
            dataPickerDialog.show()
        }
        binding.organizationsActivitySearchEditTextDateComparator.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@OrganizationsActivity)
            builder.setTitle(R.string.choose_date_comparator).setItems(
                R.array.date_comparators_english
            ) { dialog, which ->
                if (which == 0) {
                    binding.organizationsActivitySearchEditTextDateComparator.text = null
                    binding.organizationsActivitySearchEditTextDateComparator.gravity = Gravity.LEFT
                } else {
                    binding.organizationsActivitySearchEditTextDateComparator.setText(resources.getStringArray(R.array.date_comparators_english)[which])
                    binding.organizationsActivitySearchEditTextDateComparator.gravity = Gravity.CENTER
                }
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareSearchEditTextType() {
        binding.organizationsActivitySearchEditTextType.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@OrganizationsActivity)
            builder.setTitle(R.string.choose_organization_type).setItems(
                R.array.org_types
            ) { dialog, which ->
                if (which == 0) binding.organizationsActivitySearchEditTextType.text = null
                else binding.organizationsActivitySearchEditTextType.setText(resources.getStringArray(R.array.org_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareFloatingActionButton() {
        binding.organizationsActivityFab.setOnClickListener {
            startActivity(Intent(applicationContext, CreateOrEditOrganizationActivity::class.java))
        }
    }

    private fun updateLabel() {
        binding.organizationsActivitySearchEditTextDateLastUpdated.setText(
            SimpleDateFormat(
                "MM/dd/yy", Locale.US
            ).format(lastUpdatedCalendar.time)
        )
        binding.organizationsActivitySearchEditTextDateLastUpdated.gravity = Gravity.CENTER
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_organization, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return when (item.itemId) {
            R.id.action_logout_organizations -> {
                logout()
                return true
            }
            R.id.action_sort_settings_organizations -> {
                showSortSettingsDialogs()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSortSettings()
        updateList(getFilter())
    }

    private fun loadSortSettings() {
        sortName = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_FIELD, this.getString(R.string.search_org_obj_id))
            .toString()
        sortOrder = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_ORDER, "desc").toString()
    }

    override fun saveSortSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_FIELD, sortName).commit()
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(Constants.SETTINGS_ORGANIZATION_LIST_SORT_ORDER, sortOrder).commit()
    }

    override fun getParentCoordinatorLayoutForSnackBar(): CoordinatorLayout = binding.organizationsActivityCoordinatorLayout

    override fun getRequestTag(): String = "ORGANIZATIONS_TAG"

    override fun getEmptyListView(): TextView = binding.organizationsActivityEmptyList

    override fun getMainList(): ListView = binding.organizationsActivityListView

    override fun getProgressBar(): ProgressBar = binding.organizationsActivityProgressBar

    override fun getTotalListSizeTextView(): TextView = binding.organizationsActivityListTotalSize

    override fun getStartPage(): Int = start

    override fun getTargetCache(): String = Constants.ORGANIZATIONS_CACHE_NAME

    override fun getMainListType(): Type = object : TypeToken<PageDataDto<TableDataDto<OrganizationDto>>>() {}.type

    override fun getListAdapter(list: List<OrganizationDto>): ListAdapter = OrganizationsListAdapter(list, this@OrganizationsActivity)

    override fun getFilter(): List<FilterDto> = currentFilter ?: emptyList()

    override fun getFieldListObjNames(): Int = R.array.organizations_list_field_obj_names

    override fun getFieldListDisplayNames(): Int = R.array.organizations_list_field_display_names
}