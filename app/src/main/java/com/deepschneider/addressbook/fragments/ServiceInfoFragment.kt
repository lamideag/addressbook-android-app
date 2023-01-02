package com.deepschneider.addressbook.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.dto.BuildInfoDto
import com.deepschneider.addressbook.utils.NetworkUtils
import com.deepschneider.addressbook.utils.Urls
import com.google.gson.Gson

class ServiceInfoFragment : Fragment() {

    private lateinit var requestQueue: RequestQueue

    private lateinit var listener: FragmentActivity

    private var serverUrl: String? = null

    private val requestTag = "BUILD_INFO_TAG"

    private val gson = Gson()

    private var versionInfoTextView: TextView? = null

    private var buildInfoTextView: TextView? = null

    private var serverHostTextView: TextView? = null

    private var serverInfoTextView: TextView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            listener = (context as FragmentActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue = Volley.newRequestQueue(listener)
        serverUrl = NetworkUtils.getServerUrl(listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.service_info_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        updateBuildInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        versionInfoTextView = view.findViewById(R.id.build_info_fragment_version_info)
        buildInfoTextView = view.findViewById(R.id.build_info_fragment_build_info)
        serverHostTextView = view.findViewById(R.id.build_info_fragment_server_host)
        serverInfoTextView = view.findViewById(R.id.build_info_fragment_server_info)
    }

    private fun updateBuildInfo() {
        requestQueue.add(object :
            JsonObjectRequest(Method.GET, serverUrl + Urls.BUILD_INFO, null, { response ->
                val buildInfo = gson.fromJson(response.toString(), BuildInfoDto::class.java)
                versionInfoTextView?.text = "version: " + buildInfo.version?.uppercase()
                buildInfoTextView?.text = "build: " + buildInfo.time?.uppercase()
                serverHostTextView?.text = "server host: " + buildInfo.serverHost?.uppercase()
                serverInfoTextView?.text = "server: $serverUrl"
            }, { error ->
                Log.d("SERVICE INFO ERROR", error.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return NetworkUtils.addAuthHeader(super.getHeaders(), listener)
            }
        }.also { it.tag = requestTag })
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(requestTag)
    }
}