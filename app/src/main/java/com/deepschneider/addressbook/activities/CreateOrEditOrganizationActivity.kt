package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.OrganizationDto

class CreateOrEditOrganizationActivity : AppCompatActivity() {

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}