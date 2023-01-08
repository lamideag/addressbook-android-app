package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.ContactDto

class CreateOrEditContactActivity : AppCompatActivity() {

    private var contactDto: ContactDto? = null
    private lateinit var contactTypes: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_contact)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        contactTypes = this.resources.getStringArray(R.array.contact_types)
        val extra = intent.extras?.get("contact")
        if (extra != null) {
            contactDto = extra as ContactDto
            contactDto?.let {
                it.type?.let { contactType ->
                    title =
                        this.getString(R.string.edit_activity_header) + " " + contactTypes[contactType.toInt()]
                }
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