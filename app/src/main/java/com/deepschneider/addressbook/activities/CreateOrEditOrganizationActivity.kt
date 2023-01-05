package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.network.SaveOrCreateEntityRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.reflect.TypeToken
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateOrEditOrganizationActivity : AbstractEntityActivity() {

    private var organizationDto: OrganizationDto? = null

    private lateinit var typeEditText: TextInputEditText
    private lateinit var typeEditTextLayout: TextInputLayout

    private lateinit var zipEditText: TextInputEditText
    private lateinit var zipEditTextLayout: TextInputLayout

    private lateinit var addressEditText: TextInputEditText
    private lateinit var addressEditTextLayout: TextInputLayout

    private lateinit var nameEditText: TextInputEditText
    private lateinit var nameEditTextLayout: TextInputLayout

    private lateinit var idEditText: TextInputEditText
    private lateinit var lastUpdatedEditText: TextInputEditText
    private lateinit var saveOrCreateButton: Button

    private val fieldValidation = BooleanArray(4)

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.create_or_edit_organization_activity_zip -> {
                    validateZipEditText()
                }
                R.id.create_or_edit_organization_activity_address -> {
                    validateAddressEditText()
                }
                R.id.create_or_edit_organization_activity_type -> {
                    validateTypeEditText()
                }
                R.id.create_or_edit_organization_activity_name -> {
                    validateNameEditText()
                }
            }
            updateSaveButtonState()
        }
    }

    private fun validateNameEditText() {
        val value = nameEditText.text.toString().trim()
        if (value.isEmpty()) {
            nameEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[3] = false
        } else if (value.length > 500) {
            nameEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[3] = false
        } else {
            nameEditTextLayout.error = null
            fieldValidation[3] = true
        }
    }

    private fun validateAddressEditText() {
        val value = addressEditText.text.toString().trim()
        if (value.isEmpty()) {
            addressEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[1] = false
        } else if (value.length > 500) {
            addressEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[1] = false
        } else {
            addressEditTextLayout.error = null
            fieldValidation[1] = true
        }
    }

    private fun validateZipEditText() {
        val value = zipEditText.text.toString().trim()
        if (value.isEmpty()) {
            zipEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[0] = false
        } else if (value.length > 100) {
            zipEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[0] = false
        } else {
            zipEditTextLayout.error = null
            fieldValidation[0] = true
        }
    }

    private fun validateTypeEditText() {
        if (typeEditText.text.toString().trim().isEmpty()) {
            typeEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
        } else {
            typeEditTextLayout.error = null
            fieldValidation[2] = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_organization)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val extra = intent.extras?.get("organization")
        if (extra != null) {
            organizationDto = extra as OrganizationDto
            organizationDto?.let {
                title = this.getString(R.string.edit_activity_header) + " " + it.name
            }
        }
        typeEditText = findViewById(R.id.create_or_edit_organization_activity_type)
        typeEditTextLayout = findViewById(R.id.create_or_edit_organization_activity_type_layout)

        zipEditText = findViewById(R.id.create_or_edit_organization_activity_zip)
        zipEditTextLayout = findViewById(R.id.create_or_edit_organization_activity_zip_layout)

        addressEditText = findViewById(R.id.create_or_edit_organization_activity_address)
        addressEditTextLayout =
            findViewById(R.id.create_or_edit_organization_activity_address_layout)

        nameEditText = findViewById(R.id.create_or_edit_organization_activity_name)
        nameEditTextLayout = findViewById(R.id.create_or_edit_organization_activity_name_layout)

        idEditText = findViewById(R.id.create_or_edit_organization_activity_id)
        lastUpdatedEditText = findViewById(R.id.create_or_edit_organization_activity_last_updated)

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
        setupListeners()
        validateTypeEditText()
        validateZipEditText()
        validateAddressEditText()
        validateNameEditText()
        updateSaveButtonState()
    }

    private fun setupListeners() {
        typeEditText.addTextChangedListener(TextFieldValidation(typeEditText))
        zipEditText.addTextChangedListener(TextFieldValidation(zipEditText))
        addressEditText.addTextChangedListener(TextFieldValidation(addressEditText))
        nameEditText.addTextChangedListener(TextFieldValidation(nameEditText))
    }

    private fun updateUi(organizationDto: OrganizationDto?) {
        organizationDto?.let {
            typeEditText.setText(it.type)
            zipEditText.setText(it.zip)
            addressEditText.setText(it.street)
            nameEditText.setText(it.name)
            idEditText.setText(it.id)
            lastUpdatedEditText.setText(it.lastUpdated)
            saveOrCreateButton.text = this.getString(R.string.action_save_changes)
            title = this.getString(R.string.edit_activity_header) + " " + it.name
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

    private fun updateSaveButtonState() {
        saveOrCreateButton.isEnabled = fieldValidation.all { it }
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
                requestQueue.add(SaveOrCreateEntityRequest(
                    url,
                    it,
                    { response ->
                        response.data?.let {
                            handler.post {
                                it.type?.let {
                                    response.data?.type = convertIndexToType(it)
                                }
                                organizationDto = it
                                handler.post {
                                    updateUi(it)
                                }
                                organizationDto?.id?.let {
                                    if (create) sendLockRequest(
                                        true, Constants.ORGANIZATIONS_CACHE_NAME, it
                                    ) else {
                                        makeSnackBar(
                                            this@CreateOrEditOrganizationActivity.getString(
                                                R.string.changes_saved_message
                                            )
                                        )
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