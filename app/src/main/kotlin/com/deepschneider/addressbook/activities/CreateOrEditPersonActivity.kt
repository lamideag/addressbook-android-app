package com.deepschneider.addressbook.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.adapters.ContactsListAdapter
import com.deepschneider.addressbook.dto.ContactDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.PersonDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.network.EntityGetRequest
import com.deepschneider.addressbook.network.SaveOrCreateEntityRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.internal.CheckableImageButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.reflect.TypeToken
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarItems
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateOrEditPersonActivity : AbstractEntityActivity(), IAztecToolbarClickListener {

    private lateinit var idEditText: TextInputEditText
    private lateinit var idEditTextLayout: TextInputLayout
    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var firstNameEditTextLayout: TextInputLayout
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var lastNameEditTextLayout: TextInputLayout
    private lateinit var salaryEditText: TextInputEditText
    private lateinit var salaryEditTextLayout: TextInputLayout
    private lateinit var currencyEditText: TextInputEditText
    private lateinit var currencyEditTextLayout: TextInputLayout
    private lateinit var rteResumeEditor: AztecText
    private lateinit var resumeEditTextLayout: TextInputLayout
    private lateinit var rteToolbarContainer: RelativeLayout
    private lateinit var contactsListView: RecyclerView
    private lateinit var emptyContactsListTextView: CardView
    private lateinit var rteToolbar: AztecToolbar
    private lateinit var saveOrCreateButton: Button
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
                R.id.create_or_edit_person_activity_first_name -> validateFirstNameEditText()
                R.id.create_or_edit_person_activity_last_name -> validateLastNameEditText()
                R.id.create_or_edit_person_activity_salary -> validateSalaryEditText()
                R.id.rte_resume_editor -> validateResumeRteEditText()
            }
            updateSaveButtonState()
        }
    }

    private fun prepareFloatingActionButton() {
        findViewById<FloatingActionButton>(R.id.create_or_edit_person_activity_fab).setOnClickListener {
            startForResult.launch(
                Intent(
                    applicationContext,
                    CreateOrEditContactActivity::class.java
                )
            )
        }
    }

    private fun prepareExtras() {
        val extra = intent.extras?.get("person")
        if (extra != null) {
            personDto = extra as PersonDto
        }
    }

    private fun prepareLayout() {
        orgId = intent.getStringExtra("orgId").toString()
        idEditText = findViewById(R.id.create_or_edit_person_activity_id)
        idEditTextLayout = findViewById(R.id.create_or_edit_person_activity_id_layout)
        firstNameEditText = findViewById(R.id.create_or_edit_person_activity_first_name)
        firstNameEditTextLayout = findViewById(R.id.create_or_edit_person_activity_first_name_layout)
        lastNameEditText = findViewById(R.id.create_or_edit_person_activity_last_name)
        lastNameEditTextLayout = findViewById(R.id.create_or_edit_person_activity_last_name_layout)
        salaryEditText = findViewById(R.id.create_or_edit_person_activity_salary)
        salaryEditTextLayout = findViewById(R.id.create_or_edit_person_activity_salary_layout)
        saveOrCreateButton = findViewById(R.id.create_or_edit_person_activity_save_create_button)
        saveOrCreateButton.setOnClickListener {
            saveOrCreatePerson()
        }
        contactsListView = findViewById(R.id.create_or_edit_person_activity_contacts_list_view)
        contactsListView.setHasFixedSize(true)
        contactsListView.layoutManager = LinearLayoutManager(this)
        contactsListView.itemAnimator = DefaultItemAnimator()
        emptyContactsListTextView = findViewById(R.id.create_or_edit_person_activity_empty_contacts_list)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_person)
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
        currencyEditText = findViewById(R.id.create_or_edit_person_activity_salary_currency)
        currencyEditTextLayout = findViewById(R.id.create_or_edit_person_activity_salary_currency_layout)
        currencyEditText.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditPersonActivity)
            builder.setTitle(R.string.choose_salary_currency).setItems(
                Constants.currencies.toTypedArray()
            ) { dialog, which ->
                currencyEditText.setText(Constants.currencies.toTypedArray()[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun prepareResumeRichTextEditor(){
        prepareAztecTextEditor()
        prepareAztecToolbar()
        Aztec.with(rteResumeEditor, rteToolbar, this)
    }

    private fun prepareAztecTextEditor(){
        rteResumeEditor = findViewById(R.id.rte_resume_editor)
        resumeEditTextLayout = findViewById(R.id.create_or_edit_person_activity_resume_layout)
        val errorTextView = resumeEditTextLayout.findViewById<TextView>(com.google.android.material.R.id.textinput_error)
        val layoutParams = errorTextView.layoutParams as android.widget.FrameLayout.LayoutParams
        layoutParams.bottomMargin = (this@CreateOrEditPersonActivity.resources.displayMetrics.density * 10).toInt()
        val errorButton = resumeEditTextLayout.findViewById<CheckableImageButton>(com.google.android.material.R.id.text_input_error_icon)
        val layoutParamsButton = errorButton.layoutParams as android.widget.LinearLayout.LayoutParams
        layoutParamsButton.topMargin = (this@CreateOrEditPersonActivity.resources.displayMetrics.density * 12).toInt()
        rteResumeEditor.setOnFocusChangeListener { _, hasFocus ->
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

    private fun prepareAztecToolbar(){
        rteToolbar = findViewById(R.id.formatting_toolbar)
        rteToolbarContainer = findViewById(R.id.rte_toolbar_container)
        rteToolbar.visibility = View.VISIBLE
        rteToolbar.enableMediaMode(false)
        rteToolbar.setToolbarItems(
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
    }

    private fun prepareLauncher(){
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultContactDto = result.data?.extras?.getSerializable(
                        "contact",
                        ContactDto::class.java
                    ) as ContactDto
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
                        emptyContactsListTextView.visibility = View.VISIBLE
                        contactsListView.visibility = View.GONE
                    } else {
                        emptyContactsListTextView.visibility = View.GONE
                        contactsListView.visibility = View.VISIBLE
                    }
                    updateContactAdapter()
                }
            }
    }

    private fun updateContactAdapter() {
        val adapter = contactsListView.adapter
        if (adapter != null) {
            (contactsListView.adapter as ContactsListAdapter).contacts = currentContactList
            (contactsListView.adapter as ContactsListAdapter).notifyDataSetChanged()
        } else {
            contactsListView.swapAdapter(
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
        saveOrCreateButton.isEnabled = fieldValidation.all { it }
    }

    private fun setupListeners() {
        lastNameEditText.addTextChangedListener(TextFieldValidation(lastNameEditText))
        firstNameEditText.addTextChangedListener(TextFieldValidation(firstNameEditText))
        rteResumeEditor.addTextChangedListener(TextFieldValidation(rteResumeEditor))
        salaryEditText.addTextChangedListener(TextFieldValidation(salaryEditText))
    }

    private fun validateFirstNameEditText() {
        val value = firstNameEditText.text.toString().trim()
        if (value.isEmpty()) {
            firstNameEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[3] = false
        } else if (value.length > 500) {
            firstNameEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[3] = false
        } else {
            firstNameEditTextLayout.error = null
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
                                    emptyContactsListTextView.visibility = View.VISIBLE
                                    currentContactList = arrayListOf()
                                }
                            } else {
                                response.data?.data?.let {
                                    handler.post {
                                        currentContactList = it.toMutableList()
                                        updateContactAdapter()
                                        contactsListView.visibility = View.VISIBLE
                                        emptyContactsListTextView.visibility = View.GONE
                                    }
                                }
                            }
                        },
                        { error ->
                            handler.post {
                                makeErrorSnackBar(error)
                                emptyContactsListTextView.visibility = View.VISIBLE
                                contactsListView.visibility = View.GONE
                            }
                        },
                        this@CreateOrEditPersonActivity,
                        object : TypeToken<PageDataDto<TableDataDto<ContactDto>>>() {}.type
                    ).also { it.tag = getRequestTag() })
            }
        } ?: run {
            currentContactList = arrayListOf()
            emptyContactsListTextView.visibility = View.VISIBLE
        }
    }

    private fun validateLastNameEditText() {
        val value = lastNameEditText.text.toString().trim()
        if (value.isEmpty()) {
            lastNameEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[1] = false
        } else if (value.length > 500) {
            lastNameEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[1] = false
        } else {
            lastNameEditTextLayout.error = null
            fieldValidation[1] = true
        }
    }

    private fun validateSalaryEditText() {
        val value = salaryEditText.text.toString().trim()
        if (value.isEmpty()) {
            salaryEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[0] = false
        } else if (value.length > 100) {
            salaryEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[0] = false
        } else {
            salaryEditTextLayout.error = null
            fieldValidation[0] = true
        }
    }

    private fun saveOrCreatePerson() {
        var targetPersonDto: PersonDto? = null
        var create = false
        personDto?.let {
            targetPersonDto = it
            targetPersonDto?.firstName = firstNameEditText.text.toString()
            targetPersonDto?.lastName = lastNameEditText.text.toString()
            targetPersonDto?.resume = rteResumeEditor.toHtml()
            targetPersonDto?.salary = salaryEditText.text.toString() + " " + currencyEditText.text.toString()
        } ?: run {
            create = true
            targetPersonDto = PersonDto()
            targetPersonDto?.firstName = firstNameEditText.text.toString()
            targetPersonDto?.lastName = lastNameEditText.text.toString()
            targetPersonDto?.resume = rteResumeEditor.toHtml()
            targetPersonDto?.salary = salaryEditText.text.toString() + " " + currencyEditText.text.toString()
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
        val value = rteResumeEditor.toHtml().trim()
        if (value.isEmpty()) {
            resumeEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
            highlightRteErrorFocus()
        } else if (value.length > 2000) {
            resumeEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[2] = false
            highlightRteErrorFocus()
        } else {
            resumeEditTextLayout.error = null
            fieldValidation[2] = true
            highlightRteFocus()
        }
    }

    override fun getParentCoordinatorLayoutForSnackBar(): Int = R.id.create_or_edit_person_activity_coordinator_layout

    override fun getRequestTag(): String = "CREATE_OR_EDIT_PERSON_TAG"

    private fun updateUi(personDto: PersonDto?) {
        personDto?.let {
            idEditText.setText(it.id)
            firstNameEditText.setText(it.firstName)
            lastNameEditText.setText(it.lastName)
            it.salary?.let { salary -> salaryEditText.setText(salary.substring(0, salary.length - 4)) }
            idEditText.setText(it.id)
            it.resume?.let { it1 -> rteResumeEditor.fromHtml(it1) }
            saveOrCreateButton.text = this.getString(R.string.action_save_changes)
            title = this.getString(R.string.edit_activity_header) + " " + it.firstName + " " + it.lastName
            it.salary?.let { salary -> currencyEditText.setText(salary.substring(salary.length - 3)) }
        } ?: run {
            saveOrCreateButton.text = this.getString(R.string.action_create)
            currencyEditText.setText(Constants.DEFAULT_CURRENCY)
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

    override fun onStart() {
        super.onStart()
        personDto?.id?.let { sendLockRequest(true, Constants.PERSONS_CACHE_NAME, it) }
    }

    override fun onStop() {
        super.onStop()
        personDto?.id?.let { sendLockRequest(false, Constants.PERSONS_CACHE_NAME, it) }
    }

    private fun highlightRteErrorFocus() {
        resumeEditTextLayout.background = this.getDrawable(R.drawable.ic_rte_background_error_focus)
        rteToolbarContainer.background = this.getDrawable(R.drawable.ic_rte_background_error_focus)
    }

    private fun highlightRteUnfocused() {
        resumeEditTextLayout.background = this.getDrawable(R.drawable.ic_rte_background_unfocused)
        rteToolbarContainer.background = this.getDrawable(R.drawable.ic_rte_background_unfocused)
    }

    private fun highlightRteErrorUnfocused() {
        resumeEditTextLayout.background = this.getDrawable(R.drawable.ic_rte_background_error_unfocused)
        rteToolbarContainer.background = this.getDrawable(R.drawable.ic_rte_background_error_unfocused)
    }

    private fun highlightRteFocus() {
        resumeEditTextLayout.background = this.getDrawable(R.drawable.ic_rte_background_focus)
        rteToolbarContainer.background = this.getDrawable(R.drawable.ic_rte_background_focus)
    }

    override fun onToolbarCollapseButtonClicked() {}
    override fun onToolbarExpandButtonClicked() {}
    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {}
    override fun onToolbarHeadingButtonClicked() {}
    override fun onToolbarHtmlButtonClicked() {}
    override fun onToolbarListButtonClicked() {}
    override fun onToolbarMediaButtonClicked(): Boolean = false
}