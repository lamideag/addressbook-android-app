package com.deepschneider.addressbook.adapters

import android.app.Activity
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.databinding.PersonsListItemBinding
import com.deepschneider.addressbook.dto.PersonDto

class PersonsListAdapter(
    private val persons: List<PersonDto>,
    private val activity: Activity
) : ArrayAdapter<PersonDto>(activity, R.layout.persons_list_item, persons) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = PersonsListItemBinding.inflate(LayoutInflater.from(activity), parent, false)
        binding.firstNameAndLastName.text = "${persons[position].firstName} ${persons[position].lastName}"
        binding.resume.text = Html.fromHtml(persons[position].resume, Html.FROM_HTML_MODE_COMPACT)
        binding.id.text = persons[position].id
        binding.salary.text = persons[position].salary
        return binding.root
    }
}