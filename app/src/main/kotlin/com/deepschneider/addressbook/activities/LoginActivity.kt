package com.deepschneider.addressbook.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.databinding.ActivityLoginBinding
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var requestQueue: RequestQueue
    private val requestTag = "LOGIN_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!resources.configuration.isNightModeActive)
            setTheme(R.style.Theme_Addressbook_Light)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(!resources.configuration.isNightModeActive)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")))
        supportActionBar?.elevation = 0F
        title = null
        requestQueue = Volley.newRequestQueue(this)
        binding.loginButton.setOnClickListener {
            createOrRotateLoginToken(true)
        }
        Constants.PAGE_SIZE = (((resources.displayMetrics.run { heightPixels / density } - 50) / 90)).toInt()
    }

    override fun onResume() {
        super.onResume()
        createOrRotateLoginToken(false)
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(requestTag)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                return true
            }
            R.id.action_logout_main -> {
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .remove(Constants.TOKEN_KEY)
                    .commit()
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createOrRotateLoginToken(create: Boolean) {
        hideLoginButton()
        val serverUrl = NetworkUtils.getServerUrl(this)
        if (serverUrl == Constants.NO_VALUE) {
            showLoginButton()
            return
        }
        val targetDto = if (create) getLoginDto() else getTokenDto()
        if (!create && targetDto == null) {
            showLoginButton()
            return
        }
        requestQueue.add(JsonObjectRequest(Request.Method.POST,
            if (create) serverUrl + Urls.AUTH else serverUrl + Urls.ROTATE_TOKEN,
            targetDto,
            { response ->
                saveTokenFromResponse(response)
                showLoginButton()
                startOrganizationActivity()
            },
            { error ->
                makeErrorSnackBar(error)
                showLoginButton()
            }).also { it.tag = requestTag })
    }

    private fun makeErrorSnackBar(error: VolleyError) {
        val snackBar = Snackbar.make(
            binding.coordinatorLayout, when (error) {
                is AuthFailureError -> this.getString(R.string.auth_failure_message)
                is TimeoutError -> this.getString(R.string.server_timeout_message)
                else -> error.message.toString()
            }, Snackbar.LENGTH_LONG
        )
        val view: View = snackBar.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBar.show()
    }

    private fun showLoginButton() {
        binding.loginButton.visibility = View.VISIBLE
        binding.progressBar.visibility = ProgressBar.INVISIBLE
    }

    private fun hideLoginButton() {
        binding.loginButton.visibility = View.GONE
        binding.progressBar.visibility = ProgressBar.VISIBLE
    }

    private fun startOrganizationActivity() {
        val intent = Intent(applicationContext, OrganizationsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun saveTokenFromResponse(response: JSONObject) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
            Constants.TOKEN_KEY, response.get(Constants.TOKEN_KEY) as String?
        ).commit()
    }

    private fun getLoginDto(): JSONObject {
        val loginDto = JSONObject()
        loginDto.put("login", binding.editTextLogin.text)
        loginDto.put("password", binding.editTextPassword.text)
        return loginDto
    }

    private fun getTokenDto(): JSONObject? {
        val token = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.TOKEN_KEY, Constants.NO_VALUE)
        if (token == null || token == Constants.NO_VALUE) return null
        val tokenDto = JSONObject()
        tokenDto.put(Constants.TOKEN_KEY, token)
        return tokenDto
    }
}