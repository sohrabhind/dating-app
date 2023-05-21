package com.hindbyte.dating.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.dialogs.AlcoholViewsSelectDialog;
import com.hindbyte.dating.dialogs.GenderSelectDialog;
import com.hindbyte.dating.dialogs.RelationshipStatusSelectDialog;
import com.hindbyte.dating.dialogs.ReligiousViewSelectDialog;
import com.hindbyte.dating.dialogs.SmokingViewsSelectDialog;
import com.hindbyte.dating.dialogs.YouLikeSelectDialog;
import com.hindbyte.dating.dialogs.YouLookingSelectDialog;
import com.hindbyte.dating.util.CustomRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AccountSettingsFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    private String fullname, location, instagramPage, bio;

    private int gender, year, month, day, age, height, weight;
    private int relationshipStatus, religiousView, viewsOnSmoking, viewsOnAlcohol, youLooking, youLike, allowShowMyBirthday;

    EditText mFullname, mLocation, mInstagramPage, mBio, mAgeField, mHeightField, mWeightField;
    TextView mBirth, mGender, mRelationshipStatus, mReligiousView, mSmokingViews, mAlcoholViews, mYouLooking, mYouLike;

    CheckBox mAllowShowDateBirth;

    private Boolean loading = false;

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = requireActivity().getIntent();
        fullname = i.getStringExtra("fullname");
        location = i.getStringExtra("location");
        instagramPage = i.getStringExtra("instagramPage");
        bio = i.getStringExtra("bio");

        gender = i.getIntExtra("gender", 0);

        age = i.getIntExtra("age", 0);
        height = i.getIntExtra("height", 0);
        weight = i.getIntExtra("weight", 0);

        year = i.getIntExtra("year", 0);
        month = i.getIntExtra("month", 0);
        day = i.getIntExtra("day", 0);

        relationshipStatus = i.getIntExtra("relationshipStatus", 0);
        religiousView = i.getIntExtra("religiousView", 0);
        viewsOnSmoking = i.getIntExtra("viewsOnSmoking", 0);
        viewsOnAlcohol = i.getIntExtra("viewsOnAlcohol", 0);
        youLooking = i.getIntExtra("youLooking", 0);
        youLike = i.getIntExtra("youLike", 0);

        allowShowMyBirthday = i.getIntExtra("allowShowMyBirthday", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_account_settings, container, false);

        if (loading) {

            showpDialog();
        }

        mFullname = rootView.findViewById(R.id.fullname);
        mLocation = rootView.findViewById(R.id.location);
        mInstagramPage = rootView.findViewById(R.id.instagramPage);
        mBio = rootView.findViewById(R.id.bio);

        mAgeField = rootView.findViewById(R.id.ageField);
        mHeightField = rootView.findViewById(R.id.heightField);
        mWeightField = rootView.findViewById(R.id.weightField);

        mBirth = rootView.findViewById(R.id.selectBirth);
        mGender = rootView.findViewById(R.id.selectGender);
        mRelationshipStatus = rootView.findViewById(R.id.selectRelationshipStatus);
        mReligiousView = rootView.findViewById(R.id.selectReligiousView);
        mSmokingViews = rootView.findViewById(R.id.selectSmokingViews);
        mAlcoholViews = rootView.findViewById(R.id.selectAlcoholViews);
        mYouLooking = rootView.findViewById(R.id.selectYouLooking);
        mYouLike = rootView.findViewById(R.id.selectYouLike);

        mAllowShowDateBirth = rootView.findViewById(R.id.allowShowDateBirth);

        mBirth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(requireActivity(), mDateSetListener, year, month, day);
                dpd.getDatePicker().setMaxDate(new Date().getTime());

                dpd.show();
            }
        });

        mGender.setOnClickListener(v -> selectGender(gender));

        mRelationshipStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectRelationshipStatus(relationshipStatus);
            }
        });


        mReligiousView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectReligiousView(religiousView);
            }
        });


        mSmokingViews.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectSmokingViews(viewsOnSmoking);
            }
        });

        mAlcoholViews.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectAlcoholViews(viewsOnAlcohol);
            }
        });

        mYouLooking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectYouLooking(youLooking);
            }
        });

        mYouLike.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                selectYouLike(youLike);
            }
        });


        mFullname.setText(fullname);
        mLocation.setText(location);
        mInstagramPage.setText(instagramPage);
        mBio.setText(bio);

        if (age > 0) {

            mAgeField.setText(String.valueOf(age));
        }

        mHeightField.setHint(getString(R.string.label_height) + " (" + getString(R.string.label_cm) + ")");

        if (height > 0) {

            mHeightField.setText(String.valueOf(height));
        }

        mWeightField.setHint(getString(R.string.label_weight) + " (" + getString(R.string.label_kg) + ")");

        if (weight > 0) {

            mWeightField.setText(String.valueOf(weight));
        }

        getGender(gender);
        getRelationshipStatus(relationshipStatus);
        getReligiousView(religiousView);
        getSmokingViews(viewsOnSmoking);
        getAlcoholViews(viewsOnAlcohol);
        getYouLooking(youLooking);
        getYouLike(youLike);

        int mMonth1 = month + 1;

        mBirth.setText(getString(R.string.action_select_birth) + ": " + new StringBuilder().append(day).append("/").append(mMonth1).append("/").append(year));

        checkAllowShowBirthday(allowShowMyBirthday);

        // Inflate the layout for this fragment
        return rootView;
    }

    public void checkAllowShowBirthday(int value) {

        if (value == 1) {

            mAllowShowDateBirth.setChecked(true);
            allowShowMyBirthday = 1;

        } else {

            mAllowShowDateBirth.setChecked(false);
            allowShowMyBirthday = 0;
        }
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int mYear, int monthOfYear, int dayOfMonth) {

            year = mYear;
            month = monthOfYear;
            day = dayOfMonth;

            int mMonth1 = month + 1;

            mBirth.setText(getString(R.string.action_select_birth) + ": " + new StringBuilder().append(day).append("/").append(mMonth1).append("/").append(year));

        }

    };

    public void selectGender(int position) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        GenderSelectDialog alert = new GenderSelectDialog();
        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_gender");
    }

    public void getGender(int sex) {
        gender = sex;
        switch (gender) {
            case 1: {
                mGender.setText(getString(R.string.label_sex_female));
                break;
            }
            case 2: {
                mGender.setText(getString(R.string.label_sex_other));
                break;
            }
            case 0: default: {
                mGender.setText(getString(R.string.label_sex_male));
                break;
            }
        }
    }


    public void selectRelationshipStatus(int position) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        RelationshipStatusSelectDialog alert = new RelationshipStatusSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_relationship_status");
    }

    public void getRelationshipStatus(int mRelationship) {
        relationshipStatus = mRelationship;

        switch (mRelationship) {

            case 0: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_0));

                break;
            }

            case 1: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_1));

                break;
            }

            case 2: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_2));

                break;
            }

            case 3: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_3));

                break;
            }

            case 4: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_4));

                break;
            }

            case 5: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_5));

                break;
            }

            case 6: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_6));

                break;
            }

            case 7: {

                mRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + getString(R.string.relationship_status_7));

                break;
            }

            default: {

                break;
            }
        }
    }



    public void selectReligiousView(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        ReligiousViewSelectDialog alert = new ReligiousViewSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_religious_view");
    }

    public void getReligiousView(int mWorld) {

        religiousView = mWorld;

        switch (mWorld) {

            case 0: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_0));

                break;
            }

            case 1: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_1));

                break;
            }

            case 2: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_2));

                break;
            }

            case 3: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_3));

                break;
            }

            case 4: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_4));

                break;
            }

            case 5: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_5));

                break;
            }

            case 6: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_6));

                break;
            }

            case 7: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_7));

                break;
            }

            case 8: {

                mReligiousView.setText(getString(R.string.account_religious_view) + ": " + getString(R.string.religious_view_8));

                break;
            }

            default: {

                break;
            }
        }
    }


    public void selectSmokingViews(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        SmokingViewsSelectDialog alert = new SmokingViewsSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_smoking_views");
    }

    public void getSmokingViews(int mSmoking) {

        viewsOnSmoking = mSmoking;

        switch (mSmoking) {

            case 0: {

                mSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + getString(R.string.smoking_views_0));

                break;
            }

            case 1: {

                mSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + getString(R.string.smoking_views_1));

                break;
            }

            case 2: {

                mSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + getString(R.string.smoking_views_2));

                break;
            }

            case 3: {

                mSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + getString(R.string.smoking_views_3));

                break;
            }

            case 4: {

                mSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + getString(R.string.smoking_views_4));

                break;
            }

            case 5: {

                mSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + getString(R.string.smoking_views_5));

                break;
            }

            default: {

                break;
            }
        }
    }

    public void selectAlcoholViews(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        AlcoholViewsSelectDialog alert = new AlcoholViewsSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_alcohol_views");
    }

    public void getAlcoholViews(int mAlcohol) {

        viewsOnAlcohol = mAlcohol;

        switch (mAlcohol) {

            case 0: {

                mAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + getString(R.string.alcohol_views_0));

                break;
            }

            case 1: {

                mAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + getString(R.string.alcohol_views_1));

                break;
            }

            case 2: {

                mAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + getString(R.string.alcohol_views_2));

                break;
            }

            case 3: {

                mAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + getString(R.string.alcohol_views_3));

                break;
            }

            case 4: {

                mAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + getString(R.string.alcohol_views_4));

                break;
            }

            case 5: {

                mAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + getString(R.string.alcohol_views_5));

                break;
            }

            default: {

                break;
            }
        }
    }

    public void selectYouLooking(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        YouLookingSelectDialog alert = new YouLookingSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_you_looking");
    }

    public void getYouLooking(int mLooking) {

        youLooking = mLooking;

        switch (mLooking) {

            case 0: {

                mYouLooking.setText(getString(R.string.account_you_looking_dialog) + ": " + getString(R.string.you_looking_0));

                break;
            }

            case 1: {

                mYouLooking.setText(getString(R.string.account_you_looking_dialog) + ": " + getString(R.string.you_looking_1));

                break;
            }

            case 2: {

                mYouLooking.setText(getString(R.string.account_you_looking_dialog) + ": " + getString(R.string.you_looking_2));

                break;
            }

            case 3: {

                mYouLooking.setText(getString(R.string.account_you_looking_dialog) + ": " + getString(R.string.you_looking_3));

                break;
            }

            default: {

                break;
            }
        }
    }

    public void selectYouLike(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        YouLikeSelectDialog alert = new YouLikeSelectDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_select_you_like");
    }

    public void getYouLike(int mLike) {

        youLike = mLike;

        switch (mLike) {

            case 0: {

                mYouLike.setText(getString(R.string.account_you_like_dialog) + ": " + getString(R.string.you_like_0));

                break;
            }

            case 1: {

                mYouLike.setText(getString(R.string.account_you_like_dialog) + ": " + getString(R.string.you_like_1));

                break;
            }

            case 2: {

                mYouLike.setText(getString(R.string.account_you_like_dialog) + ": " + getString(R.string.you_like_2));

                break;
            }

            default: {

                break;
            }
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save: {

                fullname = mFullname.getText().toString();
                location = mLocation.getText().toString();
                instagramPage = mInstagramPage.getText().toString();
                bio = mBio.getText().toString();

                if (mAgeField.getText().toString().length() > 0) {

                    age = Integer.parseInt(mAgeField.getText().toString());
                }

                if (mHeightField.getText().toString().length() > 0) {

                    height = Integer.parseInt(mHeightField.getText().toString());

                } else {

                    height = 0;
                }

                if (mWeightField.getText().toString().length() > 0) {

                    weight = Integer.parseInt(mWeightField.getText().toString());

                } else {

                    weight = 0;
                }

                if (mAllowShowDateBirth.isChecked()) {

                    allowShowMyBirthday = 1;

                } else {

                    allowShowMyBirthday = 0;
                }

                saveSettings();

                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }

    public void saveSettings() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SAVE_SETTINGS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("error")) {

                                if (!response.getBoolean("error")) {

                                    fullname = response.getString("fullname");
                                    location = response.getString("location");
                                    instagramPage = response.getString("instagram_page");
                                    bio = response.getString("status");

                                    age = response.getInt("age");
                                    height = response.getInt("height");
                                    weight = response.getInt("weight");

                                    Toast.makeText(requireActivity(), getText(R.string.msg_settings_saved), Toast.LENGTH_SHORT).show();

                                    App.getInstance().setFullname(fullname);

                                    Intent i = new Intent();
                                    i.putExtra("fullname", fullname);
                                    i.putExtra("location", location);
                                    i.putExtra("instagramPage", instagramPage);
                                    i.putExtra("bio", bio);

                                    i.putExtra("gender", gender);

                                    i.putExtra("age", age);
                                    i.putExtra("height", height);
                                    i.putExtra("weight", weight);

                                    i.putExtra("year", year);
                                    i.putExtra("month", month);
                                    i.putExtra("day", day);

                                    i.putExtra("relationshipStatus", relationshipStatus);
                                    i.putExtra("religiousView", religiousView);
                                    i.putExtra("viewsOnSmoking", viewsOnSmoking);
                                    i.putExtra("viewsOnAlcohol", viewsOnAlcohol);
                                    i.putExtra("youLooking", youLooking);
                                    i.putExtra("youLike", youLike);
                                    i.putExtra("allowShowMyBirthday", allowShowMyBirthday);

                                    if (isAdded()) {

                                        requireActivity().setResult(RESULT_OK, i);
                                    }

                                    requireActivity().finish();
                                }
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
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("fullname", fullname);
                params.put("location", location);
                params.put("instagramPage", instagramPage);
                params.put("bio", bio);
                params.put("gender", String.valueOf(gender));
                params.put("year", String.valueOf(year));
                params.put("month", String.valueOf(month));
                params.put("day", String.valueOf(day));

                params.put("age", String.valueOf(age));
                params.put("height", String.valueOf(height));
                params.put("weight", String.valueOf(weight));

                params.put("iStatus", String.valueOf(relationshipStatus));
                params.put("religiousViews", String.valueOf(religiousView));
                params.put("smokingViews", String.valueOf(viewsOnSmoking));
                params.put("alcoholViews", String.valueOf(viewsOnAlcohol));
                params.put("lookingViews", String.valueOf(youLooking));
                params.put("interestedViews", String.valueOf(youLike));

                params.put("allowShowMyBirthday", String.valueOf(allowShowMyBirthday));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}