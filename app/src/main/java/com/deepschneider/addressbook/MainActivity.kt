package com.deepschneider.addressbook

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.deepschneider.addressbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowInsetsControllerCompat(window, window.decorView).apply { hide(WindowInsetsCompat.Type.statusBars()) }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findViewById<Button>(R.id.login_button).setOnClickListener { _ ->
            val intent = Intent(applicationContext, OrganizationActivity::class.java)
            intent.putExtra("message_key", "test");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent);
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}