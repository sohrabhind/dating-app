package com.hindbyte.dating.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.util.CustomRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PrivacySettingsFragment extends PreferenceFragmentCompat implements Constants {

    private CheckBoxPreference mAllowInfo, mAllowGallery, mAllowFriends, mAllowLikes;

    private ProgressDialog pDialog;

    int allowLikes, allowFriends, allowInfo, allowGallery;

    private Boolean loading = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.privacy_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
initpDialog();

        mAllowLikes = (CheckBoxPreference) getPreferenceManager().findPreference("allowLikes");

        mAllowLikes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        allowLikes = 1;

                    } else {

                        allowLikes = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        saveSettings();

                    } else {

                        Toast.makeText(requireActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        mAllowFriends = (CheckBoxPreference) getPreferenceManager().findPreference("allowFriends");

        mAllowFriends.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        allowFriends = 1;

                    } else {

                        allowFriends = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        saveSettings();

                    } else {

                        Toast.makeText(requireActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        mAllowGallery = (CheckBoxPreference) getPreferenceManager().findPreference("allowGallery");

        mAllowGallery.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        allowGallery = 1;

                    } else {

                        allowGallery = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        saveSettings();

                    } else {

                        Toast.makeText(requireActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });


        mAllowInfo = (CheckBoxPreference) getPreferenceManager().findPreference("allowInfo");

        mAllowInfo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        allowInfo = 1;

                    } else {

                        allowInfo = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        saveSettings();

                    } else {

                        Toast.makeText(requireActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        checkAllowLikes(App.getInstance().getAllowShowMyLikes());
        checkAllowFriends(App.getInstance().getAllowShowMyFriends());
        checkAllowGallery(App.getInstance().getAllowShowMyGallery());
        checkAllowInfo(App.getInstance().getAllowShowMyInfo());
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            loading = savedInstanceState.getBoolean("loading");

        } else {

            loading = false;
        }

        if (loading) {

            showpDialog();
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    public void checkAllowLikes(int value) {

        if (value == 1) {

            mAllowLikes.setChecked(true);
            allowLikes = 1;

        } else {

            mAllowLikes.setChecked(false);
            allowLikes = 0;
        }
    }

    public void checkAllowFriends(int value) {

        if (value == 1) {

            mAllowFriends.setChecked(true);
            allowFriends = 1;

        } else {

            mAllowFriends.setChecked(false);
            allowFriends = 0;
        }
    }

    public void checkAllowGallery(int value) {

        if (value == 1) {

            mAllowGallery.setChecked(true);
            allowGallery = 1;

        } else {

            mAllowGallery.setChecked(false);
            allowGallery = 0;
        }
    }


    public void checkAllowInfo(int value) {

        if (value == 1) {

            mAllowInfo.setChecked(true);
            allowInfo = 1;

        } else {

            mAllowInfo.setChecked(false);
            allowInfo = 0;
        }
    }

    public void saveSettings() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_SETTINGS_PRIVACY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowShowMyLikes(response.getInt("allowShowMyLikes"));
                                App.getInstance().setAllowShowMyFriends(response.getInt("allowShowMyFriends"));
                                App.getInstance().setAllowShowMyGallery(response.getInt("allowShowMyGallery"));
                                App.getInstance().setAllowShowMyInfo(response.getInt("allowShowMyInfo"));

                                checkAllowLikes(App.getInstance().getAllowShowMyLikes());
                                checkAllowFriends(App.getInstance().getAllowShowMyFriends());
                                checkAllowGallery(App.getInstance().getAllowShowMyGallery());
                                checkAllowInfo(App.getInstance().getAllowShowMyInfo());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            Log.e("Privacy Success", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Log.d("Privacy Error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowShowMyLikes", String.valueOf(allowLikes));
                params.put("allowShowMyFriends", String.valueOf(allowFriends));
                params.put("allowShowMyGallery", String.valueOf(allowGallery));
                params.put("allowShowMyInfo", String.valueOf(allowInfo));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing())
            pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}