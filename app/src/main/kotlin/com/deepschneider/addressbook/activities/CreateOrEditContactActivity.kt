package com.deepschneider.addressbook.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.databinding.ActivityCreateNewContactBinding
import com.deepschneider.addressbook.dto.ContactDto
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateOrEditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewContactBinding
    private var contactDto: ContactDto? = null
    private lateinit var contactTypes: Array<String>
    private val fieldValidation = BooleanArray(3)

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
        binding = ActivityCreateNewContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        contactTypes = this.resources.getStringArray(R.array.contact_types)
    }

    private fun prepareExtras() {
        val extra = intent.extras?.get("contact")
        if (extra != null) contactDto = extra as ContactDto
    }

    private fun prepareTypeEditText() {
        binding.createOrEditContactActivityType.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
            builder.setTitle(R.string.choose_contact_type).setItems(
                R.array.contact_types
            ) { dialog, which ->
                if (which == 0) binding.createOrEditContactActivityType.text = null
                else binding.createOrEditContactActivityType.setText(resources.getStringArray(R.array.contact_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareDeleteContactButton() {
        binding.createOrEditContactActivityDeleteContactButton.setOnClickListener {
            MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
                .setTitle(this.getString(R.string.delete_contact_confirmation))
                .setPositiveButton("DELETE") { _, _ ->
                    val data = Intent()
                    data.putExtra("contact", contactDto)
                    data.putExtra("delete", true)
                    setResult(RESULT_OK, data)
                    finish()
                }
                .setNegativeButton("CANCEL", null).show()
        }
    }

    private fun prepareAddOrApplyButton() {
        binding.createOrEditContactActivityAddApplyButton.setOnClickListener {
            val targetContactDto = if (contactDto == null) ContactDto() else contactDto
            targetContactDto?.data = binding.createOrEditContactActivityData.text.toString()
            targetContactDto?.description = binding.createOrEditContactActivityDesc.text.toString()
            targetContactDto?.type = (this.resources.getStringArray(R.array.contact_types).indexOf(binding.createOrEditContactActivityType.text.toString()) - 1).toString()
            val data = Intent()
            data.putExtra("contact", targetContactDto)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun validateDataEditText() {
        val dataEditText = binding.createOrEditContactActivityData
        val dataEditTextLayout = binding.createOrEditContactActivityDataLayout
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
        val descEditText = binding.createOrEditContactActivityDesc
        val descEditTextLayout = binding.createOrEditContactActivityDescLayout
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
        val typeEditText = binding.createOrEditContactActivityType
        val typeEditTextLayout = binding.createOrEditContactActivityTypeLayout
        if (typeEditText.text.toString().trim().isEmpty()) {
            typeEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
        } else {
            typeEditTextLayout.error = null
            fieldValidation[2] = true
        }
    }

    private fun updateSaveButtonState() {
        binding.createOrEditContactActivityAddApplyButton.isEnabled = fieldValidation.all { it }
    }

    private fun setupListeners() {
        binding.createOrEditContactActivityType.addTextChangedListener(TextFieldValidation(binding.createOrEditContactActivityType))
        binding.createOrEditContactActivityData.addTextChangedListener(TextFieldValidation(binding.createOrEditContactActivityData))
        binding.createOrEditContactActivityDesc.addTextChangedListener(TextFieldValidation(binding.createOrEditContactActivityDesc))
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
            binding.createOrEditContactActivityId.setText(contact.id)
            contact.type?.let { binding.createOrEditContactActivityType.setText(contactTypes[it.toInt() + 1]) }
            binding.createOrEditContactActivityData.setText(contact.data)
            binding.createOrEditContactActivityDesc.setText(contact.description)
            contact.type?.let { contactType ->
                title = this.getString(R.string.edit_activity_header) + " " + contactTypes[contactType.toInt() + 1]
                binding.createOrEditContactActivityDeleteContactButton.visibility = View.VISIBLE
            }
            binding.createOrEditContactActivityAddApplyButton.text = this.getString(R.string.action_apply_contact_changes)
        } ?: run {
            binding.createOrEditContactActivityAddApplyButton.text = this.getString(R.string.action_add_contact)
            binding.createOrEditContactActivityDeleteContactButton.visibility = View.GONE
        }
    }
}