package com.hindbyte.dating.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.HomeActivity;
import com.hindbyte.dating.activity.BalanceHistoryActivity;
import com.hindbyte.dating.activity.BlackListActivity;
import com.hindbyte.dating.activity.ChangePasswordActivity;
import com.hindbyte.dating.activity.DeactivateActivity;
import com.hindbyte.dating.activity.NotificationsSettingsActivity;
import com.hindbyte.dating.activity.ServicesActivity;
import com.hindbyte.dating.activity.SupportActivity;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.ToastWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class SettingsFragment extends PreferenceFragmentCompat implements Constants {

    private Preference logoutPreference, itemContactUs, changePassword, itemBalanceHistory, itemServices, itemBlackList, itemNotifications, itemDeactivateAccount;
    private CheckBoxPreference allowMessages;
    private PreferenceScreen screen;

    ToastWindow toastWindow = new ToastWindow();
    private ProgressDialog pDialog;

    int mAllowMessages;

    LinearLayout aboutDialogContent;
    TextView aboutDialogAppName, aboutDialogAppVersion, aboutDialogAppCopyright;

    private Boolean loading = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initpDialog();

        screen = this.getPreferenceScreen();

        logoutPreference = findPreference("settings_logout");
        changePassword = findPreference("settings_change_password");
        itemDeactivateAccount = findPreference("settings_deactivate_account");
        itemServices = getPreferenceScreen().findPreference("settings_services");
        itemBalanceHistory = getPreferenceScreen().findPreference("settings_balance_history");
        itemBlackList = findPreference("settings_blocked_list");
        itemNotifications = findPreference("settings_push_notifications");
        itemContactUs = findPreference("settings_contact_us");

        if (!GOOGLE_AUTHORIZATION) {
            PreferenceCategory headerGeneral = findPreference("header_general");
            assert headerGeneral != null;
            headerGeneral.removePreference(itemServices);
        }


        itemBalanceHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(@NonNull Preference arg0) {

                Intent i = new Intent(requireActivity(), BalanceHistoryActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemContactUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(@NonNull Preference arg0) {

                Intent i = new Intent(requireActivity(), SupportActivity.class);
                startActivity(i);

                return true;
            }
        });


        itemNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(@NonNull Preference arg0) {

                Intent i = new Intent(requireActivity(), NotificationsSettingsActivity.class);
                startActivity(i);

                return true;
            }
        });


        itemBlackList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(@NonNull Preference arg0) {

                Intent i = new Intent(requireActivity(), BlackListActivity.class);
                startActivity(i);

                return true;
            }
        });


        logoutPreference.setOnPreferenceClickListener(arg0 -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
            alertDialog.setTitle(getText(R.string.action_logout));

            alertDialog.setMessage(getText(R.string.msg_action_logout));
            alertDialog.setCancelable(true);

            alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.cancel();
                }
            });

            alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    loading = true;

                    showpDialog();

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {

                                        if (!response.getBoolean("error")) {

                                            Log.d("Logout", "Logout success");
                                        }

                                    } catch (JSONException e) {

                                        e.printStackTrace();

                                    } finally {

                                        loading = false;

                                        hidepDialog();

                                        App.getInstance().removeData();
                                        App.getInstance().readData();

                                        App.getInstance().setNotificationsCount(0);
                                        App.getInstance().setMessagesCount(0);
                                        App.getInstance().setId(0);
                                        App.getInstance().setUsername("");
                                        App.getInstance().setFullname("");

                                        Intent intent = new Intent(requireActivity(), HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            loading = false;

                            App.getInstance().removeData();
                            App.getInstance().readData();

                            App.getInstance().setNotificationsCount(0);
                            App.getInstance().setMessagesCount(0);
                            App.getInstance().setId(0);
                            App.getInstance().setUsername("");
                            App.getInstance().setFullname("");

                            Intent intent = new Intent(requireActivity(), HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            hidepDialog();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("accountId", Long.toString(App.getInstance().getId()));
                            params.put("accessToken", App.getInstance().getAccessToken());

                            return params;
                        }
                    };

                    RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                    jsonReq.setRetryPolicy(policy);

                    App.getInstance().addToRequestQueue(jsonReq);
                }
            });

            alertDialog.show();

            return true;
        });

        changePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(requireActivity(), ChangePasswordActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemDeactivateAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(requireActivity(), DeactivateActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemServices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(requireActivity(), ServicesActivity.class);
                startActivity(i);

                return true;
            }
        });

        allowMessages = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessages");

        allowMessages.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowMessages = 1;

                    } else {

                        mAllowMessages = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowMessages();

                    } else {

                        toastWindow.makeText(getText(R.string.msg_network_error), 2000);
                    }
                }

                return true;
            }
        });

        //
        checkAllowMessages(App.getInstance().getAllowMessages());
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


    public void checkAllowMessages(int value) {

        if (value == 1) {

            allowMessages.setChecked(true);
            mAllowMessages = 1;

        } else {

            allowMessages.setChecked(false);
            mAllowMessages = 0;
        }
    }

    public void setAllowMessages() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_ALLOW_MESSAGES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowMessages(response.getInt("allowMessages"));

                                checkAllowMessages(App.getInstance().getAllowMessages());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                toastWindow.makeText(getText(R.string.error_data_loading), 2000);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowMessages", String.valueOf(mAllowMessages));

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