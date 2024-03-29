package com.hindbyte.dating.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;

public class NotificationsSettingsFragment extends PreferenceFragmentCompat implements Constants {

    private CheckBoxPreference mAllowLikesGCM, mAllowMessagesGCM;

    private ProgressDialog pDialog;

    int mAllowLikes, mAllowMessages;

    private Boolean loading = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.notifications_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initpDialog();
        
        mAllowLikesGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowLikesGCM");

        mAllowLikesGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowLikes = 1;

                    } else {

                        mAllowLikes = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });


        mAllowMessagesGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessagesGCM");

        mAllowMessagesGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowMessages = 1;

                    } else {

                        mAllowMessages = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });


        checkAllowLikes(App.getInstance().getAllowLikesGCM());
        checkAllowMessages(App.getInstance().getAllowMessagesGCM());
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

            mAllowLikesGCM.setChecked(true);
            mAllowLikes = 1;

        } else {

            mAllowLikesGCM.setChecked(false);
            mAllowLikes = 0;
        }
    }


    public void checkAllowMessages(int value) {

        if (value == 1) {

            mAllowMessagesGCM.setChecked(true);
            mAllowMessages = 1;

        } else {

            mAllowMessagesGCM.setChecked(false);
            mAllowMessages = 0;
        }
    }


    public void saveSettings() {

        App.getInstance().setAllowLikesGCM(mAllowLikes);
        App.getInstance().setAllowMessagesGCM(mAllowMessages);

        App.getInstance().saveData();
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