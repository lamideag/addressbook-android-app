package com.deepschneider.addressbook

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OrganizationActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val listView = findViewById<ListView>(R.id.organizationsListView)
        listView.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, arrayOf(
                "Apple", "Microsoft", "IBM", "Oracle", "Red Hat",
                "Citibank", "Netflix", "Nvidia", "Intel", "AMD",
                "Facebook", "Sony", "Nintendo"
            )
        )

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            startActivity(Intent(applicationContext, CreateNewOrganization::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        findViewById<TextView>(R.id.textView).text =
            PreferenceManager.getDefaultSharedPreferences(this).getString("server_url", "NO VALUE")
        super.onResume()
    }
}