package com.investokar.poppi.util

import android.app.Application
import android.content.Context
import com.android.volley.Response
import com.investokar.poppi.R
import com.investokar.poppi.app.App
import com.investokar.poppi.constants.Constants
import org.json.JSONException

class Api(var context: Context) : Application(), Constants {
    var toastWindow = ToastWindow()
    fun profileReport(profileId: Long, reason: Int) {
        val jsonReq: CustomRequest = object : CustomRequest(
            Method.POST,
            Constants.METHOD_PROFILE_REPORT,
            null,
            Response.Listener { response ->
                try {
                    if (!response.getBoolean("error")) {
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    toastWindow.makeText(context.getText(R.string.label_profile_reported), 2000)
                }
            },
            Response.ErrorListener {
                toastWindow.makeText(
                    context.getText(R.string.label_profile_reported),
                    2000
                )
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["accountId"] = java.lang.Long.toString(App.getInstance().id)
                params["accessToken"] = App.getInstance().accessToken
                params["profileId"] = java.lang.Long.toString(profileId)
                params["reason"] = reason.toString()
                return params
            }
        }
        App.getInstance().addToRequestQueue(jsonReq)
    }

    fun photoDelete(itemId: Long) {
        val jsonReq: CustomRequest = object : CustomRequest(
            Method.POST,
            Constants.METHOD_GALLERY_REMOVE,
            null,
            Response.Listener { response ->
                try {
                    if (!response.getBoolean("error")) {
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                toastWindow.makeText(
                    context.getString(R.string.error_data_loading),
                    2000
                )
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["accountId"] = java.lang.Long.toString(App.getInstance().id)
                params["accessToken"] = App.getInstance().accessToken
                params["itemId"] = java.lang.Long.toString(itemId)
                return params
            }
        }
        App.getInstance().addToRequestQueue(jsonReq)
    }

    fun photoReport(itemId: Long, reasonId: Int) {
        val jsonReq: CustomRequest = object : CustomRequest(
            Method.POST,
            Constants.METHOD_GALLERY_REPORT,
            null,
            Response.Listener { response ->
                try {
                    if (!response.getBoolean("error")) {
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    toastWindow.makeText(context.getString(R.string.label_item_reported), 2000)
                }
            },
            Response.ErrorListener {
                toastWindow.makeText(
                    context.getString(R.string.label_item_reported),
                    2000
                )
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["accountId"] = java.lang.Long.toString(App.getInstance().id)
                params["accessToken"] = App.getInstance().accessToken
                params["itemId"] = java.lang.Long.toString(itemId)
                params["abuseId"] = reasonId.toString()
                return params
            }
        }
        App.getInstance().addToRequestQueue(jsonReq)
    }
}