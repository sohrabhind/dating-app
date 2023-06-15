package com.hindbyte.dating.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Api extends Application implements Constants {

    Context context;

    public Api (Context context) {
        this.context = context;
    }

    ToastWindow toastWindow = new ToastWindow();

    public void profileReport(final long profileId, final int reason) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_REPORT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            toastWindow.makeText(context.getText(R.string.label_profile_reported), 2000);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                toastWindow.makeText(context.getText(R.string.label_profile_reported), 2000);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("reason", String.valueOf(reason));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }
    



    public void photoDelete(final long itemId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toastWindow.makeText(context.getString(R.string.error_data_loading), 2000);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void photoReport(final long itemId, final int reasonId) {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_REPORT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            toastWindow.makeText(context.getString(R.string.label_item_reported), 2000);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                toastWindow.makeText(context.getString(R.string.label_item_reported), 2000);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("abuseId", String.valueOf(reasonId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

}
