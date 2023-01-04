package com.deepschneider.addressbook.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.utils.Constants
import com.google.android.material.textfield.TextInputEditText

class CreateOrEditOrganizationActivity : AbstractEntityActivity() {

    private var organizationDto: OrganizationDto? = null

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

        val editTextType =
            findViewById<TextInputEditText>(R.id.create_or_edit_organization_activity_type)
        editTextType.setOnClickListener {
            val builder = AlertDialog.Builder(this@CreateOrEditOrganizationActivity)
            builder.setTitle(R.string.choose_organization_type).setItems(
                R.array.org_types
            ) { dialog, which ->
                if (which == 0) editTextType.text = null
                else editTextType.setText(resources.getStringArray(R.array.org_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
        organizationDto?.let {
            findViewById<TextInputEditText>(R.id.create_or_edit_organization_activity_type).setText(
                it.type
            )
            findViewById<TextInputEditText>(R.id.create_or_edit_organization_activity_zip).setText(
                it.zip
            )
            findViewById<TextInputEditText>(R.id.create_or_edit_organization_activity_address).setText(
                it.street
            )
            findViewById<TextInputEditText>(R.id.create_or_edit_organization_activity_name).setText(
                it.name
            )
            findViewById<Button>(R.id.create_or_edit_organization_activity_save_create_button).text =
                this.getString(R.string.action_save_changes)
        } ?: run {
            findViewById<Button>(R.id.create_or_edit_organization_activity_save_create_button).text =
                this.getString(R.string.action_create)
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