package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.PersonDto
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarItems

class CreateOrEditPersonActivity : AppCompatActivity(), IAztecToolbarClickListener {

    private var personDto: PersonDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_person)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val extra = intent.extras?.get("person")
        if (extra != null) {
            personDto = extra as PersonDto
            personDto?.let {
                title =
                    this.getString(R.string.edit_activity_header) + " " + it.firstName + " " + it.lastName
            }
        }
        val visualEditor = findViewById<AztecText>(R.id.visual)
        val toolbar = findViewById<AztecToolbar>(R.id.formatting_toolbar)
        val rteContainer = findViewById<RelativeLayout>(R.id.rte_container)
        val rteToolbarContainer = findViewById<RelativeLayout>(R.id.rte_toolbar_container)
        rteContainer.background = this.getDrawable(R.drawable.rte_background_unfocused)
        toolbar.visibility = View.VISIBLE
        toolbar.enableMediaMode(false)
        toolbar.setToolbarItems(
            ToolbarItems.BasicLayout(
                ToolbarAction.LIST,
                ToolbarAction.QUOTE,
                ToolbarAction.BOLD,
                ToolbarAction.ITALIC,
                ToolbarAction.LINK,
                ToolbarAction.UNDERLINE,
                ToolbarAction.STRIKETHROUGH,
                ToolbarAction.ALIGN_LEFT,
                ToolbarAction.ALIGN_CENTER,
                ToolbarAction.ALIGN_RIGHT,
                ToolbarAction.HORIZONTAL_RULE
            )
        )
        visualEditor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                rteContainer.background = this.getDrawable(R.drawable.rte_bckgrnd_focus)
                rteToolbarContainer.background = this.getDrawable(R.drawable.rte_bckgrnd_focus)
            } else {
                rteContainer.background = this.getDrawable(R.drawable.rte_background_unfocused)
                rteToolbarContainer.background =
                    this.getDrawable(R.drawable.rte_background_unfocused)
            }
        }
        Aztec.with(visualEditor, toolbar, this)
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

    override fun onToolbarCollapseButtonClicked() {

    }

    override fun onToolbarExpandButtonClicked() {

    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {

    }

    override fun onToolbarHeadingButtonClicked() {

    }

    override fun onToolbarHtmlButtonClicked() {

    }

    override fun onToolbarListButtonClicked() {

    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }
}