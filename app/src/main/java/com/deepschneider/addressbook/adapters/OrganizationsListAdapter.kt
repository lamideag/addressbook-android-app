package com.deepschneider.addressbook.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.OrganizationDto

class OrganizationsListAdapter(
    private val organizations: List<OrganizationDto>, private val activity: Activity
) : ArrayAdapter<OrganizationDto>(activity, R.layout.organizations_list_item, organizations) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = activity.layoutInflater.inflate(R.layout.organizations_list_item, null, true)
        rowView.findViewById<TextView>(R.id.organization_item_name).text =
            organizations[position].name
        rowView.findViewById<TextView>(R.id.organization_item_address_and_zip).text =
            organizations[position].street + " " + organizations[position].zip

        rowView.findViewById<TextView>(R.id.organization_item_last_updated).text =
            organizations[position].lastUpdated
        rowView.findViewById<TextView>(R.id.organization_item_id).text = organizations[position].id
        rowView.findViewById<TextView>(R.id.organization_item_type).text =
            organizations[position].type
        return rowView
    }
}