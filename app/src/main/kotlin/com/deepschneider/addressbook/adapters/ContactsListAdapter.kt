package com.deepschneider.addressbook.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.deepschneider.addressbook.activities.CreateOrEditContactActivity
import com.deepschneider.addressbook.databinding.ContactListItemBinding
import com.deepschneider.addressbook.dto.ContactDto

class ContactsListAdapter(
    var contacts: List<ContactDto>,
    private val contactTypes: Array<String>,
    private val activity: Activity,
    private val startForResult: ActivityResultLauncher<Intent>
) :
    RecyclerView.Adapter<ContactsListAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(binding: ContactListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var currentItem: ContactDto? = null
        var contactData = binding.contactItemData
        var contactDesc = binding.contactItemDesc
        var contactType = binding.contactItemType

        init {
            binding.root.setOnClickListener {
                val intent = Intent(activity, CreateOrEditContactActivity::class.java)
                intent.putExtra("contact", currentItem)
                startForResult.launch(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            ContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.currentItem = contacts[position]
        holder.contactData.text = contacts[position].data
        holder.contactDesc.text = contacts[position].description
        contacts[position].type?.let {
            holder.contactType.text = contactTypes[it.toInt() + 1]
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}