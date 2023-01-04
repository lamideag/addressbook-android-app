package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Button
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.network.SaveOrCreateEntityRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.reflect.TypeToken
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateOrEditOrganizationActivity : AbstractEntityActivity() {

    private var organizationDto: OrganizationDto? = null

    private lateinit var typeEditText: TextInputEditText
    private lateinit var zipEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var saveOrCreateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_organization)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val extra = intent.extras?.get("organization")
        if (extra != null) {
            organizationDto = extra as OrganizationDto
            organizationDto?.let {
                title = "Edit " + it.name
            }
        }

        typeEditText = findViewById(R.id.create_or_edit_organization_activity_type)
        zipEditText = findViewById(R.id.create_or_edit_organization_activity_zip)
        addressEditText = findViewById(R.id.create_or_edit_organization_activity_address)
        nameEditText = findViewById(R.id.create_or_edit_organization_activity_name)
        saveOrCreateButton =
            findViewById(R.id.create_or_edit_organization_activity_save_create_button)
        saveOrCreateButton.setOnClickListener {
            saveOrCreateOrganization()
        }
        typeEditText.setOnClickListener {
            val builder = AlertDialog.Builder(this@CreateOrEditOrganizationActivity)
            builder.setTitle(R.string.choose_organization_type).setItems(
                R.array.org_types
            ) { dialog, which ->
                if (which == 0) typeEditText.text = null
                else typeEditText.setText(resources.getStringArray(R.array.org_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
        updateUi(organizationDto)
    }

    private fun updateUi(organizationDto: OrganizationDto?) {
        organizationDto?.let {
            typeEditText.setText(it.type)
            zipEditText.setText(it.zip)
            addressEditText.setText(it.street)
            nameEditText.setText(it.name)
            saveOrCreateButton.text = this.getString(R.string.action_save_changes)
        } ?: run {
            saveOrCreateButton.text = this.getString(R.string.action_create)
        }
    }

    private fun convertTypeToIndex(type: String): String {
        return (this.resources.getStringArray(R.array.org_types).indexOf(type) - 1).toString()
    }

    private fun convertIndexToType(type: String): String {
        return this.resources.getStringArray(R.array.org_types)[type.toInt() + 1]
    }

    private fun saveOrCreateOrganization() {
        var targetOrganizationDto: OrganizationDto? = null
        var create = false
        organizationDto?.let {
            targetOrganizationDto = it
            targetOrganizationDto?.name = nameEditText.text.toString()
            targetOrganizationDto?.street = addressEditText.text.toString()
            targetOrganizationDto?.zip = zipEditText.text.toString()
            targetOrganizationDto?.type = convertTypeToIndex(typeEditText.text.toString())
        } ?: run {
            create = true
            targetOrganizationDto = OrganizationDto()
            targetOrganizationDto?.id = UUID.randomUUID().toString()
            targetOrganizationDto?.name = nameEditText.text.toString()
            targetOrganizationDto?.street = addressEditText.text.toString()
            targetOrganizationDto?.zip = zipEditText.text.toString()
            targetOrganizationDto?.type = convertTypeToIndex(typeEditText.text.toString())
        }
        targetOrganizationDto?.let {
            val handler = Handler(Looper.getMainLooper())
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            val url = "$serverUrl" + Urls.SAVE_OR_CREATE_ORGANIZATION
            executor.execute {
                requestQueue.add(
                    SaveOrCreateEntityRequest(
                        url,
                        it,
                        { response ->
                            response.data?.let {
                                handler.post {
                                    it.type?.let {
                                        response.data?.type = convertIndexToType(it)
                                    }
                                    organizationDto = it
                                    updateUi(it)
                                    organizationDto?.id?.let {
                                        if (create)
                                            sendLockRequest(
                                                true,
                                                Constants.ORGANIZATIONS_CACHE_NAME,
                                                it
                                            ) else {
                                                makeSnackBar("Changes saved!")
                                        }
                                    }
                                }
                            }
                        },
                        { error ->
                            handler.post {
                                makeErrorSnackBar(error)
                            }
                        },
                        this@CreateOrEditOrganizationActivity,
                        object : TypeToken<PageDataDto<OrganizationDto>>() {}.type
                    ).also { it.tag = getRequestTag() })
            }
        }
    }

    override fun getParentCoordinatorLayoutForSnackBar(): Int =
        R.id.create_or_edit_organization_activity_coordinator_layout

    override fun getRequestTag(): String = "CREATE_OR_EDIT_ORGANIZATION_TAG"

    override fun onResume() {
        super.onResume()
        organizationDto?.id?.let {
            sendLockRequest(true, Constants.ORGANIZATIONS_CACHE_NAME, it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        organizationDto?.id?.let {
            sendLockRequest(false, Constants.ORGANIZATIONS_CACHE_NAME, it)
        }
    }
}