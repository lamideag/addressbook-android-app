package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.view.MenuItem
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.utils.Constants

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