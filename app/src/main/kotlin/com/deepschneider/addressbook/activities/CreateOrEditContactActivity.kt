package com.deepschneider.addressbook.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.ContactDto
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateOrEditContactActivity : AppCompatActivity() {

    private lateinit var idEditText: TextInputEditText
    private lateinit var typeEditText: TextInputEditText
    private lateinit var typeEditTextLayout: TextInputLayout
    private lateinit var dataEditText: TextInputEditText
    private lateinit var dataEditTextLayout: TextInputLayout
    private lateinit var descEditText: TextInputEditText
    private lateinit var descEditTextLayout: TextInputLayout
    private var contactDto: ContactDto? = null
    private lateinit var contactTypes: Array<String>
    private val fieldValidation = BooleanArray(3)
    private lateinit var applyOrAddButton: Button
    private lateinit var deleteContactButton: Button

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.create_or_edit_contact_activity_data -> validateDataEditText()
                R.id.create_or_edit_contact_activity_type -> validateTypeEditText()
                R.id.create_or_edit_contact_activity_desc -> validateDescEditText()
            }
            updateSaveButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_contact)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        prepareExtras()
        prepareTypeEditText()
        prepareLayout()
        prepareDeleteContactButton()
        prepareAddOrApplyButton()
        updateUi(contactDto)
        setupListeners()
        validateDataEditText()
        validateDescEditText()
        validateTypeEditText()
        updateSaveButtonState()
    }

    private fun prepareLayout() {
        idEditText = findViewById(R.id.create_or_edit_contact_activity_id)
        dataEditText = findViewById(R.id.create_or_edit_contact_activity_data)
        dataEditTextLayout = findViewById(R.id.create_or_edit_contact_activity_data_layout)
        descEditText = findViewById(R.id.create_or_edit_contact_activity_desc)
        descEditTextLayout = findViewById(R.id.create_or_edit_contact_activity_desc_layout)
        contactTypes = this.resources.getStringArray(R.array.contact_types)
    }

    private fun prepareExtras() {
        val extra = intent.extras?.get("contact")
        if (extra != null) contactDto = extra as ContactDto
    }

    private fun prepareTypeEditText() {
        typeEditText = findViewById(R.id.create_or_edit_contact_activity_type)
        typeEditText.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
            builder.setTitle(R.string.choose_contact_type).setItems(
                R.array.contact_types
            ) { dialog, which ->
                if (which == 0) typeEditText.text = null
                else typeEditText.setText(resources.getStringArray(R.array.contact_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
        typeEditTextLayout = findViewById(R.id.create_or_edit_contact_activity_type_layout)
    }

    private fun prepareDeleteContactButton() {
        deleteContactButton = findViewById(R.id.create_or_edit_contact_activity_delete_contact_button)
        deleteContactButton.setOnClickListener {
            MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
                .setTitle(this.getString(R.string.delete_contact_confirmation))
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    val data = Intent()
                    data.putExtra("contact", contactDto)
                    data.putExtra("delete", true)
                    setResult(RESULT_OK, data)
                    finish()
                })
                .setNegativeButton("CANCEL", null)
                .show();
        }
    }

    private fun prepareAddOrApplyButton() {
        applyOrAddButton = findViewById(R.id.create_or_edit_contact_activity_add_apply_button)
        applyOrAddButton.setOnClickListener {
            val targetContactDto = if (contactDto == null) ContactDto() else contactDto
            targetContactDto?.data = dataEditText.text.toString()
            targetContactDto?.description = descEditText.text.toString()
            targetContactDto?.type = (this.resources.getStringArray(R.array.contact_types).indexOf(typeEditText.text.toString()) - 1).toString()
            val data = Intent()
            data.putExtra("contact", targetContactDto)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun validateDataEditText() {
        val value = dataEditText.text.toString().trim()
        if (value.isEmpty()) {
            dataEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[1] = false
        } else if (value.length > 500) {
            dataEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[1] = false
        } else {
            dataEditTextLayout.error = null
            fieldValidation[1] = true
        }
    }

    private fun validateDescEditText() {
        val value = descEditText.text.toString().trim()
        if (value.isEmpty()) {
            descEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[0] = false
        } else if (value.length > 100) {
            descEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[0] = false
        } else {
            descEditTextLayout.error = null
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

    private fun updateSaveButtonState() {
        applyOrAddButton.isEnabled = fieldValidation.all { it }
    }

    private fun setupListeners() {
        typeEditText.addTextChangedListener(TextFieldValidation(typeEditText))
        dataEditText.addTextChangedListener(TextFieldValidation(dataEditText))
        descEditText.addTextChangedListener(TextFieldValidation(descEditText))
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

    private fun updateUi(contactDto: ContactDto?) {
        contactDto?.let { contact ->
            idEditText.setText(contact.id)
            contact.type?.let { typeEditText.setText(contactTypes[it.toInt() + 1]) }
            dataEditText.setText(contact.data)
            descEditText.setText(contact.description)
            contact.type?.let { contactType ->
                title = this.getString(R.string.edit_activity_header) + " " + contactTypes[contactType.toInt() + 1]
                deleteContactButton.visibility = View.VISIBLE
            }
            applyOrAddButton.text = this.getString(R.string.action_apply_contact_changes)
        } ?: run {
            applyOrAddButton.text = this.getString(R.string.action_add_contact)
            deleteContactButton.visibility = View.GONE
        }
    }
}