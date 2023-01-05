package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.onegravity.rteditor.RTEditText
import com.onegravity.rteditor.RTManager
import com.onegravity.rteditor.RTToolbar
import com.onegravity.rteditor.api.RTApi
import com.onegravity.rteditor.api.RTMediaFactoryImpl
import com.onegravity.rteditor.api.RTProxyImpl

class CreateOrEditPersonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ThemeLight);
        setContentView(R.layout.activity_create_new_person)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val rtApi = RTApi(this, RTProxyImpl(this), RTMediaFactoryImpl(this, true))
        val rtManager = RTManager(rtApi, savedInstanceState)
        val toolbarContainer = findViewById<View>(R.id.rte_toolbar_container) as ViewGroup
        val rtToolbar1 =
            findViewById<View>(com.onegravity.rteditor.R.id.rte_toolbar_paragraph) as RTToolbar
        rtManager.registerToolbar(toolbarContainer, rtToolbar1)

        val rtToolbar2 =
            findViewById<View>(com.onegravity.rteditor.R.id.rte_toolbar_character) as RTToolbar
        rtManager.registerToolbar(toolbarContainer, rtToolbar2)

        rtManager.setToolbarVisibility(RTManager.ToolbarVisibility.SHOW)

        val rtEditText = findViewById<View>(R.id.rtEditTextResume) as RTEditText
        rtManager.registerEditor(rtEditText, true)
        rtEditText.setRichTextEditing(true, true)
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