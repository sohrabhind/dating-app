package com.hindbyte.dating.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.ChatActivity;
import com.hindbyte.dating.adapter.HotgameAdapter;
import com.hindbyte.dating.animation.Pager2Transformer;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.Profile;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.ToastWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HotGameFragment extends Fragment implements Constants {


    private Location mLastLocation;
    Menu MainMenu;

    ToastWindow toastWindow = new ToastWindow();

    TextView mGrantPermissionButton, mShowFiltersButton;

    ViewPager2 mViewPager2;
    LinearLayout mCardsContainer;
    LinearLayout mPermissionPromptContainer, mHotgameEmptyContainer;

    private ArrayList<Profile> itemsList;
    private HotgameAdapter itemsAdapter;

    public ImageView mHotGameLike, mHotGameBack, mHotGameProfile, mHotGameNext;
    public ProgressBar mHotGameProgressBar;

    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    LocationManager locationManager;


    private int gender = 3, distance = 2500;
    String country = "0";
    //Gender 3 = Any
    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean permission_denied = false;

    public HotGameFragment() {
        // Required empty public constructor
    }

    public String getCountryZipCode(String CountryID, Context context) {
        String countryZipCode = "";
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim().toUpperCase())) {
                countryZipCode = g[0];
                break;
            }
        }
        return countryZipCode;
    }

    private static String getDeviceCountryCode(Context context) {
        String countryCode;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm != null) {
            countryCode = tm.getSimCountryIso();
            if (countryCode != null && countryCode.length() == 2) {
                return countryCode.toLowerCase();
            } else {
                countryCode = tm.getNetworkCountryIso();
            }

            if (countryCode != null && countryCode.length() == 2) {
                return countryCode.toLowerCase();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryCode = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            countryCode = context.getResources().getConfiguration().locale.getCountry();
        }

        if (countryCode != null && countryCode.length() == 2) {
            return countryCode.toLowerCase();
        }
        return "us";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        itemsList = new ArrayList<>();
        itemsAdapter = new HotgameAdapter(requireActivity(), itemsList);
        restore = false;
        loading = false;
        itemId = 0;
        distance = 2500;
        gender = 3;
        country = getCountryZipCode(getDeviceCountryCode(requireContext()), requireContext());
        if (country.trim() == "") {
            country = "0";
        }
        readFilterSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hotgame, container, false);
        multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {
            boolean granted = true;
            for (Map.Entry<String, Boolean> x : isGranted.entrySet())
                if (!x.getValue()) granted = false;
            if (granted) {
                Log.e("Permissions", "granted");
                updateLocation();
            } else {
                Log.e("Permissions", "denied");
                permission_denied = true;
                updateView();
            }
        });

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        mHotGameProgressBar = rootView.findViewById(R.id.hotgame_progressbar);
        mCardsContainer = rootView.findViewById(R.id.swipe_cards_container);
        mViewPager2 = rootView.findViewById(R.id.viewPager2);

        mViewPager2.setAdapter(itemsAdapter);
        mViewPager2.setPageTransformer(new Pager2Transformer());

        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (itemsList.size() <= mViewPager2.getCurrentItem()+3) {
                    loading = true;
                    getItems();
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Profile u = itemsList.get(position);
                if (u.isILike()) {
                    mHotGameLike.setImageResource(R.drawable.ic_action_liked);
                    mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.circular_button_liked));
                }
                if (!u.isILike()) {
                    mHotGameLike.setImageResource(R.drawable.ic_action_like);
                    mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.circular_button));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });


        mHotgameEmptyContainer = rootView.findViewById(R.id.hotgame_empty_container);
        mShowFiltersButton = rootView.findViewById(R.id.show_filters_button);
        mPermissionPromptContainer = rootView.findViewById(R.id.permission_prompt_container);
        mGrantPermissionButton = rootView.findViewById(R.id.permission_grant_button);
        mHotGameLike = rootView.findViewById(R.id.fab_like_button);
        mHotGameBack = rootView.findViewById(R.id.fab_back_button);
        mHotGameProfile = rootView.findViewById(R.id.fab_profile_button);
        mHotGameNext = rootView.findViewById(R.id.fab_next_button);

        mHotGameProfile.setOnClickListener(v -> {
            Profile profile = itemsList.get(mViewPager2.getCurrentItem());
            if (profile.getAllowMessages() == 0 && !profile.isMyFan()) {
                toastWindow.makeText(getString(R.string.error_no_friend), 2000);
            } else {
                if (!profile.isInBlackList()) {
                    Intent i = new Intent(requireActivity(), ChatActivity.class);
                    i.putExtra("chatId", 0);
                    i.putExtra("profileId", profile.getId());
                    String fullname = profile.getFullname();
		            if (profile.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			            fullname = fullname.substring(0, fullname.lastIndexOf(' '));
		            }
                    i.putExtra("withProfile", fullname);
                    i.putExtra("with_user_username", profile.getUsername());
                    i.putExtra("with_user_fullname", fullname);
                    i.putExtra("with_user_photo_url", profile.getBigPhotoUrl());
                    i.putExtra("with_user_state", profile.getState());
                    startActivity(i);
                } else {
                    toastWindow.makeText(getString(R.string.error_action), 2000);
                }
            }
        });

        mHotGameLike.setOnClickListener(v -> {
            animateIcon(mHotGameLike);
            Profile p = itemsList.get(mViewPager2.getCurrentItem());
            like(p.getId());
        });


        mHotGameNext.setOnClickListener(v -> {
            animateIcon(mHotGameNext);
            onCardSwiped("Right");
        });

        mHotGameBack.setOnClickListener(v -> {
            animateIcon(mHotGameBack);
            onCardSwiped("Left");
        });

        mShowFiltersButton.setOnClickListener(v -> getHotGameSettings());

        mGrantPermissionButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                    multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
                } else {
                    multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
                }
            }
        });

        if (!restore) {
            getItems();
        }
        updateView();
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!GpsStatus) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        })
                        .setNegativeButton("No", (dialog, id) -> {
                            dialog.cancel();
                        });
                builder.show();
            }
        }
        return rootView;
    }


    private void animateIcon(View view) {
        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(300);
        scale.setInterpolator(new LinearInterpolator());
        view.startAnimation(scale);
    }


    private void updateView() {
        if (loading) {
            mHotgameEmptyContainer.setVisibility(View.GONE);
            mPermissionPromptContainer.setVisibility(View.GONE);
            mCardsContainer.setVisibility(View.GONE);
            mHotGameProgressBar.setVisibility(View.VISIBLE);
        } else {
            mHotGameProgressBar.setVisibility(View.GONE);
            mHotgameEmptyContainer.setVisibility(View.GONE);
            mPermissionPromptContainer.setVisibility(View.GONE);
            mCardsContainer.setVisibility(View.GONE);
            if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!permission_denied) {
                    mPermissionPromptContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (itemsList.size() != 0) {
                    mCardsContainer.setVisibility(View.VISIBLE);
                } else {
                    mHotgameEmptyContainer.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    private void onCardSwiped(String direction) {
        if (Objects.equals(direction, "Right")) {
            mViewPager2.setCurrentItem( mViewPager2.getCurrentItem()+1, true);
        }
        if (Objects.equals(direction, "Left")) {
            mViewPager2.setCurrentItem( mViewPager2.getCurrentItem()-1, true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLocation();
    }


    public void updateLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    mLastLocation = location;
                    App.getInstance().setLat(mLastLocation.getLatitude());
                    App.getInstance().setLng(mLastLocation.getLongitude());
                    App.getInstance().setLocation();
                }
                if (itemsList.size() == 0) {
                    loading = true;
                    updateView();
                    getItems();
                }
            });
        } else {
            Log.e("GPS", "error");
        }
    }


    public void getItems() {
        loading = true;
        @SuppressLint("NotifyDataSetChanged")
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_HOTGAME_GET, null, response -> {
                    if (!isAdded()) {
                        Log.e("ERROR", "HotGame Fragment Not Added to Activity");
                        return;
                    } else {
                        requireActivity();
                    }
                    try {
                        arrayLength = 0;
                        if (!response.getBoolean("error")) {
                            itemId = response.getInt("itemId");
                            if (response.has("items")) {
                                JSONArray usersArray = response.getJSONArray("items");
                                arrayLength = usersArray.length();
                                if (arrayLength > 0) {
                                    for (int i = 0; i < usersArray.length(); i++) {
                                        JSONObject userObj = (JSONObject) usersArray.get(i);
                                        Profile profile = new Profile(userObj);

                                        if (itemsList.size() == 0) {

                                            Log.e("ERROR", "1");    itemsList.add(profile);
                                            itemsAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.e("ERROR", "2");
                                            itemsList.add(profile);
                                            itemsAdapter.notifyItemRangeInserted(itemsList.size(), 1);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loadingComplete();
                        Log.d("Success", response.toString());
                    }
                }, error -> {
            if (!isAdded()) {
                Log.e("ERROR", "HotGame Fragment Not Added to Activity");
                return;
            } else {
                requireActivity();
            }
            loadingComplete();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("distance", String.valueOf(distance));
                params.put("country", String.valueOf(country));
                params.put("lat", Double.toString(App.getInstance().getLat()));
                params.put("lng", Double.toString(App.getInstance().getLng()));
                params.put("itemId", Long.toString(itemId));
                params.put("gender", String.valueOf(gender));
                return params;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {
        loading = false;
        updateView();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_hotgame, menu);
        MainMenu = menu;
    }

    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    private BottomSheetDialog mBottomSheetDialog;

    public void getHotGameSettings() {
        LinearLayout view = (LinearLayout) requireActivity().getLayoutInflater().inflate(R.layout.dialog_hotgame_settings, null);

        mBottomSheetDialog = new BottomSheetDialog(requireActivity(), R.style.BottomSheetRoundCorner);
        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.setCanceledOnTouchOutside(true);

        doKeepDialog(mBottomSheetDialog);



        final RadioButton mAnyGenderRadio = view.findViewById(R.id.radio_gender_any);
        final RadioButton mMaleGenderRadio = view.findViewById(R.id.radio_gender_male);
        final RadioButton mFemaleGenderRadio = view.findViewById(R.id.radio_gender_female);
        final RadioButton mOtherGenderRadio = view.findViewById(R.id.radio_gender_other);
        TextView bottomSheetOk = view.findViewById(R.id.bottom_sheet_ok);

        final TextView mDistanceLabel = view.findViewById(R.id.distance_label);
        final AppCompatSeekBar mDistanceSeekBar = view.findViewById(R.id.choice_distance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDistanceSeekBar.setMin(25);
        }

        switch (gender) {
            case 0: {
                mMaleGenderRadio.setChecked(true);
                break;
            }
            case 1: {
                mFemaleGenderRadio.setChecked(true);
                break;
            }
            case 2: {
                mOtherGenderRadio.setChecked(true);
                break;
            }
            default: {
                mAnyGenderRadio.setChecked(true);
                break;
            }
        }

        mDistanceSeekBar.setProgress(distance);
        mDistanceLabel.setText(String.format(Locale.getDefault(), getString(R.string.label_distance), distance));
        mDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDistanceLabel.setText(String.format(Locale.getDefault(), getString(R.string.label_distance), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bottomSheetOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distance = mDistanceSeekBar.getProgress();

                // Gender

                if (mAnyGenderRadio.isChecked()) {
                    gender = 3;
                }

                if (mMaleGenderRadio.isChecked()) {
                    gender = 0;
                }

                if (mFemaleGenderRadio.isChecked()) {
                    gender = 1;
                }

                if (mOtherGenderRadio.isChecked()) {
                    gender = 2;
                }

                itemsList.clear();
                itemId = 0;
                loading = true;
                saveFilterSettings();
                updateView();
                getItems();
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;

                // get distance


            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_hotgame_settings) {
            getHotGameSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void like(final long profileId) {
        loading = true;
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_LIKE, null,
                response -> {
                    if (!isAdded()) {
                        Log.e("ERROR", "HotgameFragment Not Added to Activity");
                        return;
                    } else {
                        requireActivity();
                    }
                    try {
                        if (!response.getBoolean("error")) {
                            Profile u = itemsList.get(mViewPager2.getCurrentItem());
                            u.setILike(response.getBoolean("iLiked"));
                            if (u.isILike()) {
                                mHotGameLike.setImageResource(R.drawable.ic_action_liked);
                                mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.circular_button_liked));
                            }
                            if (!u.isILike()) {
                                mHotGameLike.setImageResource(R.drawable.ic_action_like);
                                mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.circular_button));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    if (!isAdded()) {
                        Log.e("ERROR", "HotGame Fragment Not Added to Activity");
                    } else {
                        requireActivity();
                    }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void readFilterSettings() {
        gender = App.getInstance().getSharedPref().getInt(getString(R.string.settings_hotgame_gender), 3); // 3 = all
        distance = App.getInstance().getSharedPref().getInt(getString(R.string.settings_hotgame_distance), 2500);
    }

    public void saveFilterSettings() {
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_gender), gender).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_distance), distance).apply();
    }

}