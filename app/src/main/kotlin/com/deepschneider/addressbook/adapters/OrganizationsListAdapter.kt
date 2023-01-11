package com.deepschneider.addressbook.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.databinding.OrganizationsListItemBinding
import com.deepschneider.addressbook.dto.OrganizationDto

class OrganizationsListAdapter(
    private val organizations: List<OrganizationDto>,
    private val activity: Activity
) : ArrayAdapter<OrganizationDto>(activity, R.layout.organizations_list_item, organizations) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = OrganizationsListItemBinding.inflate(LayoutInflater.from(activity), parent, false)
        binding.name.text = organizations[position].name
        binding.addressAndZip.text = organizations[position].street + " " + organizations[position].zip
        binding.lastUpdated.text = organizations[position].lastUpdated
        binding.id.text = organizations[position].id
        binding.type.text = organizations[position].type
        return binding.root
    }
}