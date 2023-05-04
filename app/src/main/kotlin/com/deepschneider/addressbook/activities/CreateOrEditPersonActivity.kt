package com.deepschneider.addressbook.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.ContactsListAdapter
import com.deepschneider.addressbook.databinding.ActivityCreateOrEditPersonBinding
import com.deepschneider.addressbook.dto.ContactDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.PersonDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.network.EntityGetRequest
import com.deepschneider.addressbook.network.SaveOrCreateEntityRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Urls
import com.deepschneider.addressbook.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.CheckableImageButton
import com.google.gson.reflect.TypeToken
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarItems
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateOrEditPersonActivity : AbstractEntityActivity(), IAztecToolbarClickListener {

    private lateinit var binding: ActivityCreateOrEditPersonBinding
    private var personDto: PersonDto? = null
    private lateinit var orgId: String
    private val fieldValidation = BooleanArray(4)
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var currentContactList: MutableList<ContactDto>

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.first_name -> validateFirstNameEditText()
                R.id.last_name -> validateLastNameEditText()
                R.id.salary -> validateSalaryEditText()
                R.id.rte_resume_editor -> validateResumeRteEditText()
            }
            updateSaveButtonState()
        }
    }

    private fun prepareFloatingActionButton() {
        binding.fab.setOnClickListener {
            clearFocus()
            startForResult.launch(
                Intent(
                    applicationContext,
                    CreateOrEditContactActivity::class.java
                )
            )
        }
    }

    private fun prepareExtras() {
        val extra = Utils.getSerializable(this, "person", PersonDto::class.java)
        if (extra != null) personDto = extra
    }

    private fun prepareLayout() {
        orgId = intent.getStringExtra("orgId").toString()
        binding.saveCreateButton.setOnClickListener {
            saveOrCreatePerson()
        }
        binding.contactsListView.setHasFixedSize(true)
        binding.contactsListView.layoutManager = LinearLayoutManager(this)
        binding.contactsListView.itemAnimator = DefaultItemAnimator()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_current_person, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!resources.configuration.isNightModeActive)
            setTheme(R.style.Theme_Addressbook_Light)
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOrEditPersonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        prepareExtras()
        prepareLayout()
        prepareResumeRichTextEditor()
        prepareCurrencyEditText()
        updateUi(personDto)
        setupListeners()
        validateFirstNameEditText()
        validateLastNameEditText()
        validateSalaryEditText()
        validateResumeRteEditText()
        if (fieldValidation[2]) {
            highlightRteUnfocused()
        } else {
            highlightRteErrorUnfocused()
        }
        updateSaveButtonState()
        updateContactList()
        prepareFloatingActionButton()
        prepareLauncher()
    }

    private fun prepareCurrencyEditText() {
        binding.salaryCurrency.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditPersonActivity)
            builder.setTitle(R.string.choose_salary_currency).setItems(
                Constants.currencies.toTypedArray()
            ) { dialog, which ->
                binding.salaryCurrency.setText(Constants.currencies.toTypedArray()[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareResumeRichTextEditor() {
        prepareAztecTextEditor()
        prepareAztecToolbar()
        Aztec.with(binding.rteResumeEditor, binding.formattingToolbar, this)
    }

    private fun prepareAztecTextEditor() {
        val errorTextView = binding.resumeLayout.findViewById<TextView>(com.google.android.material.R.id.textinput_error)
        val layoutParams = errorTextView.layoutParams as android.widget.FrameLayout.LayoutParams
        layoutParams.bottomMargin = (this@CreateOrEditPersonActivity.resources.displayMetrics.density * 10).toInt()
        val errorButton = binding.resumeLayout.findViewById<CheckableImageButton>(com.google.android.material.R.id.text_input_error_icon)
        val layoutParamsButton = errorButton.layoutParams as android.widget.LinearLayout.LayoutParams
        layoutParamsButton.topMargin = (this@CreateOrEditPersonActivity.resources.displayMetrics.density * 12).toInt()
        binding.rteResumeEditor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (fieldValidation[2]) {
                    highlightRteFocus()
                } else {
                    highlightRteErrorFocus()
                }
            } else {
                if (fieldValidation[2]) {
                    highlightRteUnfocused()
                } else {
                    highlightRteErrorUnfocused()
                }
            }
        }
    }

    private fun prepareAztecToolbar() {
        binding.formattingToolbar.setToolbarItems(
            ToolbarItems.BasicLayout(
                ToolbarAction.HEADING,
                ToolbarAction.LIST,
                ToolbarAction.BOLD,
                ToolbarAction.ITALIC,
                ToolbarAction.UNDERLINE,
                ToolbarAction.STRIKETHROUGH,
                ToolbarAction.ALIGN_LEFT,
                ToolbarAction.ALIGN_CENTER,
                ToolbarAction.ALIGN_RIGHT,
                ToolbarAction.HORIZONTAL_RULE
            )
        )
        binding.formattingToolbar.visibility = View.VISIBLE
        binding.formattingToolbar.enableMediaMode(false)
    }

    @Suppress("DEPRECATION")
    private fun prepareLauncher() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultContactDto = result.data?.extras?.get("contact") as ContactDto
                    val shouldDelete = result.data?.extras?.getBoolean("delete")
                    resultContactDto.id?.let {
                        if (shouldDelete == true) {
                            currentContactList.removeIf { x -> x.id == resultContactDto.id }
                        } else {
                            val originalContactDto = currentContactList.find { x -> x.id == resultContactDto.id }
                            originalContactDto?.data = resultContactDto.data
                            originalContactDto?.type = resultContactDto.type
                            originalContactDto?.description = resultContactDto.description
                        }
                    } ?: run {
                        currentContactList.add(resultContactDto)
                    }
                    if (currentContactList.isEmpty()) {
                        binding.emptyContactsList.visibility = View.VISIBLE
                        binding.contactsListView.visibility = View.GONE
                    } else {
                        binding.emptyContactsList.visibility = View.GONE
                        binding.contactsListView.visibility = View.VISIBLE
                    }
                    updateContactAdapter()
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateContactAdapter() {
        val adapter = binding.contactsListView.adapter
        if (adapter != null) {
            (adapter as ContactsListAdapter).contacts = currentContactList
            adapter.notifyDataSetChanged()
        } else {
            binding.contactsListView.swapAdapter(
                ContactsListAdapter(
                    currentContactList,
                    this.resources.getStringArray(R.array.contact_types),
                    this@CreateOrEditPersonActivity,
                    startForResult
                ), false
            )
        }
    }

    private fun updateSaveButtonState() {
        binding.saveCreateButton.isEnabled = fieldValidation.all { it }
    }

    fun clearFocus() {
        binding.lastName.clearFocus()
        binding.firstName.clearFocus()
        binding.rteResumeEditor.clearFocus()
        binding.salary.clearFocus()
    }

    private fun setupListeners() {
        binding.lastName.addTextChangedListener(TextFieldValidation(binding.lastName))
        binding.firstName.addTextChangedListener(TextFieldValidation(binding.firstName))
        binding.rteResumeEditor.addTextChangedListener(TextFieldValidation(binding.rteResumeEditor))
        binding.salary.addTextChangedListener(TextFieldValidation(binding.salary))
    }

    private fun validateFirstNameEditText() {
        val value = binding.firstName.text.toString().trim()
        if (value.isEmpty()) {
            binding.firstNameLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[3] = false
        } else if (value.length > 500) {
            binding.firstNameLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[3] = false
        } else {
            binding.firstNameLayout.error = null
            fieldValidation[3] = true
        }
    }

    private fun updateContactList() {
        personDto?.let {
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            executor.execute {
                requestQueue.add(
                    EntityGetRequest<TableDataDto<ContactDto>>(
                        "$serverUrl" + Urls.GET_CONTACTS + "?personId=${personDto?.id}",
                        { response ->
                            if (response.data?.data?.isEmpty() == true) {
                                handler.post {
                                    binding.emptyContactsList.visibility = View.VISIBLE
                                    currentContactList = arrayListOf()
                                }
                            } else {
                                response.data?.data?.let {
                                    handler.post {
                                        currentContactList = it.toMutableList()
                                        updateContactAdapter()
                                        binding.contactsListView.visibility = View.VISIBLE
                                        binding.emptyContactsList.visibility = View.GONE
                                    }
                                }
                            }
                        },
                        { error ->
                            handler.post {
                                makeErrorSnackBar(error)
                                binding.emptyContactsList.visibility = View.VISIBLE
                                binding.contactsListView.visibility = View.GONE
                            }
                        },
                        this@CreateOrEditPersonActivity,
                        object : TypeToken<PageDataDto<TableDataDto<ContactDto>>>() {}.type
                    ).also { it.tag = getRequestTag() })
            }
        } ?: run {
            currentContactList = arrayListOf()
            binding.emptyContactsList.visibility = View.VISIBLE
        }
    }

    private fun validateLastNameEditText() {
        val value = binding.lastName.text.toString().trim()
        if (value.isEmpty()) {
            binding.lastNameLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[1] = false
        } else if (value.length > 500) {
            binding.lastNameLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[1] = false
        } else {
            binding.lastNameLayout.error = null
            fieldValidation[1] = true
        }
    }

    private fun validateSalaryEditText() {
        val value = binding.salary.text.toString().trim()
        if (value.isEmpty()) {
            binding.salaryLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[0] = false
        } else if (value.length > 100) {
            binding.salaryLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[0] = false
        } else {
            binding.salaryLayout.error = null
            fieldValidation[0] = true
        }
    }

    private fun saveOrCreatePerson() {
        var targetPersonDto: PersonDto? = null
        var create = false
        personDto?.let {
            targetPersonDto = it
            targetPersonDto?.firstName = binding.firstName.text.toString()
            targetPersonDto?.lastName = binding.lastName.text.toString()
            targetPersonDto?.resume = binding.rteResumeEditor.toHtml()
            targetPersonDto?.salary = binding.salary.text.toString() + " " + binding.salaryCurrency.text.toString()
        } ?: run {
            create = true
            targetPersonDto = PersonDto()
            targetPersonDto?.firstName = binding.firstName.text.toString()
            targetPersonDto?.lastName = binding.lastName.text.toString()
            targetPersonDto?.resume = binding.rteResumeEditor.toHtml()
            targetPersonDto?.salary = binding.salary.text.toString() + " " + binding.salaryCurrency.text.toString()
            targetPersonDto?.orgId = orgId
        }
        targetPersonDto?.let {
            val handler = Handler(Looper.getMainLooper())
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            val url = "$serverUrl" + Urls.SAVE_OR_CREATE_PERSON
            executor.execute {
                requestQueue.add(
                    SaveOrCreateEntityRequest(
                        url,
                        it,
                        { response ->
                            response.data?.let { savedPersonDto ->
                                requestQueue.add(SaveOrCreateEntityRequest(
                                    "$serverUrl" + Urls.SAVE_OR_CREATE_CONTACTS + "?personId=" + savedPersonDto.id,
                                    currentContactList,
                                    { response ->
                                        response.data?.let {
                                            handler.post {
                                                personDto = savedPersonDto
                                                handler.post {
                                                    updateUi(personDto)
                                                    updateContactList()
                                                    clearFocus()
                                                }
                                                personDto?.id?.let {
                                                    if (create) {
                                                        sendLockRequest(
                                                            true, Constants.PERSONS_CACHE_NAME, it
                                                        )
                                                        makeSnackBar(
                                                            this@CreateOrEditPersonActivity.getString(
                                                                R.string.person_created_message
                                                            )
                                                        )
                                                    } else {
                                                        makeSnackBar(
                                                            this@CreateOrEditPersonActivity.getString(
                                                                R.string.changes_saved_message
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    { error ->
                                        handler.post {
                                            makeErrorSnackBar(error)
                                        }
                                    },
                                    this@CreateOrEditPersonActivity,
                                    object : TypeToken<PageDataDto<List<ContactDto>>>() {}.type
                                ).also { it.tag = getRequestTag() })
                            }
                        },
                        { error ->
                            handler.post {
                                makeErrorSnackBar(error)
                            }
                        },
                        this@CreateOrEditPersonActivity,
                        object : TypeToken<PageDataDto<PersonDto>>() {}.type
                    ).also { it.tag = getRequestTag() })
            }
        }
    }

    private fun validateResumeRteEditText() {
        val value = binding.rteResumeEditor.toHtml().trim()
        if (value.isEmpty()) {
            binding.resumeLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
            if(binding.rteResumeEditor.hasFocus())
                highlightRteErrorFocus()
            else
                highlightRteErrorUnfocused()
        } else if (value.length > 2000) {
            binding.resumeLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[2] = false
            if(binding.rteResumeEditor.hasFocus())
                highlightRteErrorFocus()
            else
                highlightRteErrorUnfocused()
        } else {
            binding.resumeLayout.error = null
            fieldValidation[2] = true
            if(binding.rteResumeEditor.hasFocus())
                highlightRteFocus()
        }
    }

    override fun getParentCoordinatorLayoutForSnackBar(): View = binding.coordinatorLayout

    override fun getRequestTag(): String = "CREATE_OR_EDIT_PERSON_TAG"

    private fun updateUi(personDto: PersonDto?) {
        personDto?.let {
            binding.id.setText(it.id)
            binding.firstName.setText(it.firstName)
            binding.lastName.setText(it.lastName)
            it.salary?.let { salary -> binding.salary.setText(salary.substring(0, salary.length - 4)) }
            it.resume?.let { it1 -> binding.rteResumeEditor.fromHtml(it1) }
            binding.saveCreateButton.text = this.getString(R.string.action_save_changes)
            title = " " + it.firstName + " " + it.lastName
            it.salary?.let { salary -> binding.salaryCurrency.setText(salary.substring(salary.length - 3)) }
        } ?: run {
            binding.saveCreateButton.text = this.getString(R.string.action_create)
            binding.salaryCurrency.setText(Constants.DEFAULT_CURRENCY)
        }
    }

    private fun getCurrentPersonAsHTML(): String {
        val main = StringBuilder()
            .append("<p>First name: ${binding.firstName.text}<br/>")
            .append("Last name: ${binding.lastName.text}<br/>")
            .append("Resume: ${binding.rteResumeEditor.toHtml()}<br/>")
            .append("Salary: ${binding.salary.text} ${binding.salaryCurrency.text}</p>")
        val contactTypes = this.resources.getStringArray(R.array.contact_types)
        if (currentContactList.isNotEmpty()) {
            main.append("<p>Contacts:</p><ol>")
            for (contact in currentContactList) {
                main.append("<li><div>")
                contact.type?.let { main.append("<p>${contactTypes[it.toInt() + 1]}<br/>") }
                main.append("${contact.data}<br/>")
                main.append("${contact.description}</p>")
                main.append("</div></li>")
            }
            main.append("</ol>")
        }
        return main.toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_share_person -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        Html.fromHtml(getCurrentPersonAsHTML(), Html.FROM_HTML_MODE_LEGACY)
                    )
                    type = "text/html"
                }
                startActivity(Intent.createChooser(sendIntent, null))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        personDto?.id?.let { sendLockRequest(true, Constants.PERSONS_CACHE_NAME, it) }
    }

    override fun onStop() {
        super.onStop()
        personDto?.id?.let { sendLockRequest(false, Constants.PERSONS_CACHE_NAME, it) }
    }

    private fun highlightRteErrorFocus() {
        binding.resumeLayout.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_error_focus)
        binding.rteToolbarContainer.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_error_focus)
    }

    private fun highlightRteUnfocused() {
        binding.resumeLayout.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_unfocused)
        binding.rteToolbarContainer.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_unfocused)
    }

    private fun highlightRteErrorUnfocused() {
        binding.resumeLayout.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_error_unfocused)
        binding.rteToolbarContainer.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_error_unfocused)
    }

    private fun highlightRteFocus() {
        binding.resumeLayout.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_focus)
        binding.rteToolbarContainer.background = AppCompatResources.getDrawable(this@CreateOrEditPersonActivity, R.drawable.ic_rte_background_focus)
    }

    override fun onToolbarCollapseButtonClicked() {}
    override fun onToolbarExpandButtonClicked() {}
    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {}
    override fun onToolbarHeadingButtonClicked() {}
    override fun onToolbarHtmlButtonClicked() {}
    override fun onToolbarListButtonClicked() {}
    override fun onToolbarMediaButtonClicked(): Boolean = false
}