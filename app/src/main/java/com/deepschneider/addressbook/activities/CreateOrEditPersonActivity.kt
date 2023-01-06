package com.deepschneider.addressbook.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.PersonDto
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarItems

class CreateOrEditPersonActivity : AbstractEntityActivity(), IAztecToolbarClickListener {

    private lateinit var idEditText: TextInputEditText
    private lateinit var idEditTextLayout: TextInputLayout

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var firstNameEditTextLayout: TextInputLayout

    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var lastNameEditTextLayout: TextInputLayout

    private lateinit var salaryEditText: TextInputEditText
    private lateinit var salaryEditTextLayout: TextInputLayout

    private lateinit var rteResumeEditor: AztecText
    private lateinit var resumeEditTextLayout: TextInputLayout
    private lateinit var rteToolbarContainer: RelativeLayout

    private lateinit var rteToolbar: AztecToolbar

    private lateinit var saveOrCreateButton: Button

    private var personDto: PersonDto? = null

    private val fieldValidation = BooleanArray(4)

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.create_or_edit_person_activity_first_name -> {
                    validateFirstNameEditText()
                }
                R.id.create_or_edit_person_activity_last_name -> {
                    validateLastNameEditText()
                }
                R.id.create_or_edit_person_activity_salary -> {
                    validateSalaryEditText()
                }
                R.id.rte_resume_editor -> {
                    validateResumeRteEditText()
                }
            }
            updateSaveButtonState()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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

        idEditText = findViewById(R.id.create_or_edit_person_activity_id)
        idEditTextLayout = findViewById(R.id.create_or_edit_person_activity_id_layout)

        firstNameEditText = findViewById(R.id.create_or_edit_person_activity_first_name)
        firstNameEditTextLayout =
            findViewById(R.id.create_or_edit_person_activity_first_name_layout)

        lastNameEditText = findViewById(R.id.create_or_edit_person_activity_last_name)
        lastNameEditTextLayout = findViewById(R.id.create_or_edit_person_activity_last_name_layout)

        salaryEditText = findViewById(R.id.create_or_edit_person_activity_salary)
        salaryEditTextLayout = findViewById(R.id.create_or_edit_person_activity_salary_layout)

        saveOrCreateButton =
            findViewById(R.id.create_or_edit_person_activity_save_create_button)
        saveOrCreateButton.setOnClickListener {

        }

        rteResumeEditor = findViewById(R.id.rte_resume_editor)
        rteToolbar = findViewById(R.id.formatting_toolbar)
        resumeEditTextLayout = findViewById(R.id.create_or_edit_person_activity_resume_layout)
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
        rteResumeEditor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (fieldValidation[2]) {
                    resumeEditTextLayout.background =
                        this.getDrawable(R.drawable.rte_background_focus)
                    rteToolbarContainer.background =
                        this.getDrawable(R.drawable.rte_background_focus)
                } else {
                    resumeEditTextLayout.background =
                        this.getDrawable(R.drawable.rte_background_error_focus)
                    rteToolbarContainer.background =
                        this.getDrawable(R.drawable.rte_background_error_focus)
                }
            } else {
                if (fieldValidation[2]) {
                    resumeEditTextLayout.background =
                        this.getDrawable(R.drawable.rte_background_unfocused)
                    rteToolbarContainer.background =
                        this.getDrawable(R.drawable.rte_background_unfocused)
                } else {
                    resumeEditTextLayout.background =
                        this.getDrawable(R.drawable.rte_background_error_unfocused)
                    rteToolbarContainer.background =
                        this.getDrawable(R.drawable.rte_background_error_unfocused)
                }
            }
        }
        Aztec.with(rteResumeEditor, rteToolbar, this)

        updateUi(personDto)
        setupListeners()
        validateFirstNameEditText()
        validateLastNameEditText()
        validateSalaryEditText()
        validateResumeRteEditText()
        if (fieldValidation[2]) {
            resumeEditTextLayout.background = this.getDrawable(R.drawable.rte_background_unfocused)
            rteToolbarContainer.background = this.getDrawable(R.drawable.rte_background_unfocused)
        } else {
            resumeEditTextLayout.background =
                this.getDrawable(R.drawable.rte_background_error_unfocused)
            rteToolbarContainer.background =
                this.getDrawable(R.drawable.rte_background_error_unfocused)
        }
        updateSaveButtonState()
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

    private fun validateResumeRteEditText() {
        val value = rteResumeEditor.toHtml().trim()
        if (value.isEmpty()) {
            resumeEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
            resumeEditTextLayout.background =
                this.getDrawable(R.drawable.rte_background_error_focus)
            rteToolbarContainer.background =
                this.getDrawable(R.drawable.rte_background_error_focus)
        } else if (value.length > 2000) {
            resumeEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[2] = false
            resumeEditTextLayout.background =
                this.getDrawable(R.drawable.rte_background_error_focus)
            rteToolbarContainer.background =
                this.getDrawable(R.drawable.rte_background_error_focus)
        } else {
            resumeEditTextLayout.error = null
            fieldValidation[2] = true
            resumeEditTextLayout.background =
                this.getDrawable(R.drawable.rte_background_focus)
            rteToolbarContainer.background =
                this.getDrawable(R.drawable.rte_background_focus)

        }
    }

    override fun getParentCoordinatorLayoutForSnackBar(): Int =
        R.id.create_or_edit_person_activity_coordinator_layout

    override fun getRequestTag(): String = "CREATE_OR_EDIT_PERSON_TAG"

    private fun updateUi(personDto: PersonDto?) {
        personDto?.let {
            idEditText.setText(it.id)
            firstNameEditText.setText(it.firstName)
            lastNameEditText.setText(it.lastName)
            salaryEditText.setText(it.salary)
            idEditText.setText(it.id)
            it.resume?.let { it1 -> rteResumeEditor.fromHtml(it1) }
            saveOrCreateButton.text = this.getString(R.string.action_save_changes)
            title =
                this.getString(R.string.edit_activity_header) + " " + it.firstName + " " + it.lastName
        } ?: run {
            saveOrCreateButton.text = this.getString(R.string.action_create)
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