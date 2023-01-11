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
                R.id.data -> validateDataEditText()
                R.id.type -> validateTypeEditText()
                R.id.desc -> validateDescEditText()
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
        binding.type.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditContactActivity)
            builder.setTitle(R.string.choose_contact_type).setItems(
                R.array.contact_types
            ) { dialog, which ->
                if (which == 0) binding.type.text = null
                else binding.type.setText(resources.getStringArray(R.array.contact_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareDeleteContactButton() {
        binding.deleteContactButton.setOnClickListener {
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
        binding.addApplyButton.setOnClickListener {
            val targetContactDto = if (contactDto == null) ContactDto() else contactDto
            targetContactDto?.data = binding.data.text.toString()
            targetContactDto?.description = binding.desc.text.toString()
            targetContactDto?.type = (this.resources.getStringArray(R.array.contact_types).indexOf(binding.type.text.toString()) - 1).toString()
            val data = Intent()
            data.putExtra("contact", targetContactDto)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun validateDataEditText() {
        val dataEditText = binding.data
        val dataEditTextLayout = binding.dataLayout
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
        val descEditText = binding.desc
        val descEditTextLayout = binding.descLayout
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
        val typeEditText = binding.type
        val typeEditTextLayout = binding.typeLayout
        if (typeEditText.text.toString().trim().isEmpty()) {
            typeEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
        } else {
            typeEditTextLayout.error = null
            fieldValidation[2] = true
        }
    }

    private fun updateSaveButtonState() {
        binding.addApplyButton.isEnabled = fieldValidation.all { it }
    }

    private fun setupListeners() {
        binding.type.addTextChangedListener(TextFieldValidation(binding.type))
        binding.data.addTextChangedListener(TextFieldValidation(binding.data))
        binding.desc.addTextChangedListener(TextFieldValidation(binding.desc))
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
            binding.id.setText(contact.id)
            contact.type?.let { binding.type.setText(contactTypes[it.toInt() + 1]) }
            binding.data.setText(contact.data)
            binding.desc.setText(contact.description)
            contact.type?.let { contactType ->
                title = this.getString(R.string.edit_activity_header) + " " + contactTypes[contactType.toInt() + 1]
                binding.deleteContactButton.visibility = View.VISIBLE
            }
            binding.addApplyButton.text = this.getString(R.string.action_apply_contact_changes)
        } ?: run {
            binding.addApplyButton.text = this.getString(R.string.action_add_contact)
            binding.deleteContactButton.visibility = View.GONE
        }
    }
}