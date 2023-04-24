package com.hindbyte.dating.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.BalanceActivity;
import com.hindbyte.dating.activity.ChatActivity;
import com.hindbyte.dating.activity.ProfileActivity;
import com.hindbyte.dating.adapter.HotgameAdapter;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.Profile;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.cardstackview.CardStackLayoutManager;
import com.hindbyte.dating.cardstackview.CardStackListener;
import com.hindbyte.dating.cardstackview.CardStackView;
import com.hindbyte.dating.cardstackview.Direction;
import com.hindbyte.dating.cardstackview.Duration;
import com.hindbyte.dating.cardstackview.StackFrom;
import com.hindbyte.dating.cardstackview.SwipeAnimationSetting;
import com.hindbyte.dating.cardstackview.SwipeableMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HotgameFragment extends Fragment implements Constants, CardStackListener{

    private static final String STATE_LIST = "State Adapter Data";

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    Menu MainMenu;

    TextView mHotgameUsername, mHotgameStatus;
    LinearLayout mSpotLight, mPermissionSpotlight;
    RelativeLayout mHotgameLayout;

    Button mGrantPermissionButton, mShowFiltersButton;
    TextView mDetailsButton;

    CardStackView mCardStackView;
    RelativeLayout mCardsContainer;
    LinearLayout mPermissionPromptContainer, mPermissionDeniedContainer, mHotgameEmptyContainer;

    private ArrayList<Profile> itemsList;
    private HotgameAdapter itemsAdapter;

    private CardStackLayoutManager manager;

    public FloatingActionButton mHotgameLike, mHotgameBack, mHotgameProfile;
    public ProgressBar mHotgameProgressBar;

    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    private ActivityResultLauncher<Intent> appSettingsActivityResultLauncher;
    LocationManager lm;

    private int gender = 3, liked = 1, distance = 1000;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean permission_denied = false;

    private int position = -1;


    private static final int PROFILE_CHAT = 7;


    public HotgameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new HotgameAdapter(getActivity(), itemsList);

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            itemId = savedInstanceState.getInt("itemId");
            position = savedInstanceState.getInt("position");
            distance = savedInstanceState.getInt("distance");

            gender = savedInstanceState.getInt("gender");
            liked = savedInstanceState.getInt("liked");
        } else {
            itemsList = new ArrayList<>();
            itemsAdapter = new HotgameAdapter(getActivity(), itemsList);
            restore = false;
            loading = false;
            itemId = 0;
            position = -1;
            distance = 1000;
            gender = 3;
            liked = 1;
            readFilterSettings();
        }

        //

        appSettingsActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Log.e("test", "appSettingsActivityResultLauncher");
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hotgame, container, false);
        requireActivity().setTitle(R.string.app_name);
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

        //

        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //
        mHotgameProgressBar = rootView.findViewById(R.id.hotgame_progressbar);
        mCardsContainer = rootView.findViewById(R.id.swipe_cards_container);
        //

        mCardStackView = rootView.findViewById(R.id.card_stack_view);

        manager = new CardStackLayoutManager(getActivity(), this);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(true);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        manager.setOverlayInterpolator(new LinearInterpolator());

        manager.setTopPosition(position);

        mCardStackView.setLayoutManager(manager);
        mCardStackView.setAdapter(itemsAdapter);
        mCardStackView.setItemAnimator(new DefaultItemAnimator());
        //

        mHotgameEmptyContainer = rootView.findViewById(R.id.hotgame_empty_container);
        mShowFiltersButton = rootView.findViewById(R.id.show_filters_button);
        mPermissionPromptContainer = rootView.findViewById(R.id.permission_prompt_container);
        mGrantPermissionButton = rootView.findViewById(R.id.permission_grant_button);
        mPermissionDeniedContainer = rootView.findViewById(R.id.permission_denied_container);
        mDetailsButton = rootView.findViewById(R.id.open_location_settings_button);
        mHotgameLike = rootView.findViewById(R.id.fab_like_button);
        mHotgameBack = rootView.findViewById(R.id.fab_back_button);
        mHotgameProfile = rootView.findViewById(R.id.fab_profile_button);


        mHotgameProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = itemsList.get(manager.getTopPosition());
                /*
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", u.getId());
                startActivity(intent);
                */
                if (App.getInstance().getLevelMode() > 0 || App.getInstance().getFreeMessagesCount() > 0) {
                    if (profile.getAllowMessages() == 0 && !profile.isFriend()) {
                        Toast.makeText(getActivity(), getString(R.string.error_no_friend), Toast.LENGTH_SHORT).show();
                    } else {
                        if (!profile.isInBlackList()) {
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatId", 0);
                            i.putExtra("profileId", profile.getId());
                            i.putExtra("withProfile", profile.getFullname());
                            i.putExtra("with_user_username", profile.getUsername());
                            i.putExtra("with_user_fullname", profile.getFullname());
                            i.putExtra("with_user_photo_url", profile.getNormalPhotoUrl());
                            i.putExtra("with_user_state", profile.getState());
                            startActivityForResult(i, PROFILE_CHAT);
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    initiatePopupWindow();
                    //Toast.makeText(getActivity(), getString(R.string.msg_pro_mode_alert), Toast.LENGTH_LONG).show();
                }
            }
        });

        mHotgameLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = manager.getTopPosition();
                Profile p = itemsList.get(manager.getTopPosition());
                like(p.getId());
            }
        });

        mHotgameBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hotgameButtonAction(Direction.Left);
            }
        });

        mDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApplicationSettings();
            }
        });

        mShowFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHotGameSettings();
            }
        });

        mGrantPermissionButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                    multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
                } else {
                    multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
                }
            }
        });

        if (!restore && App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {
            getItems();
        }
        updateView();
        return rootView;
    }

    private void updateView() {
        if (loading) {
            mHotgameEmptyContainer.setVisibility(View.GONE);
            mPermissionDeniedContainer.setVisibility(View.GONE);
            mPermissionPromptContainer.setVisibility(View.GONE);
            mCardsContainer.setVisibility(View.GONE);
            mHotgameProgressBar.setVisibility(View.VISIBLE);
        } else {
            mHotgameProgressBar.setVisibility(View.GONE);
            mHotgameEmptyContainer.setVisibility(View.GONE);
            mPermissionDeniedContainer.setVisibility(View.GONE);
            mPermissionPromptContainer.setVisibility(View.GONE);
            mCardsContainer.setVisibility(View.GONE);
            if (App.getInstance().getLat() != 0.0 && App.getInstance().getLng() != 0.0) {
                if (itemsList.size() != 0) {
                    mCardsContainer.setVisibility(View.VISIBLE);
                } else {
                    mHotgameEmptyContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (permission_denied) {
                        mPermissionDeniedContainer.setVisibility(View.VISIBLE);
                    } else {
                        mPermissionPromptContainer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initiatePopupWindow() {

        AlertDialog.Builder builder3 = new AlertDialog.Builder(requireContext());
        builder3.setCancelable(true);
        @SuppressLint("InflateParams") LinearLayout signInLayout2 = (LinearLayout) LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_to_home, null, false);

        TextView silverPackageBtn = signInLayout2.findViewById(R.id.silverPackageBtn);
        TextView goldPackageBtn = signInLayout2.findViewById(R.id.goldPackageBtn);
        TextView diamondPackageBtn = signInLayout2.findViewById(R.id.diamondPackageBtn);
        TextView packageDesc = signInLayout2.findViewById(R.id.packageDesc);

        silverPackageBtn.setBackgroundResource(R.color.green_text);
        packageDesc.setText("Validity 30 Days\n\n₹ 300\n\n1000 Messages\n\nSilver Profile Badge");
        Intent intentX = new Intent(getActivity(), BalanceActivity.class);
        intentX.putExtra("package", "silver");

        silverPackageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silverPackageBtn.setBackgroundResource(R.color.green_text);
                goldPackageBtn.setBackgroundResource(R.color.white);
                diamondPackageBtn.setBackgroundResource(R.color.white);
                packageDesc.setText("Validity 30 Days\n\n₹ 300\n\n1000 Messages\n\nSilver Profile Badge");
                intentX.putExtra("package", "silver");
            }
        });

        goldPackageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silverPackageBtn.setBackgroundResource(R.color.white);
                goldPackageBtn.setBackgroundResource(R.color.green_text);
                diamondPackageBtn.setBackgroundResource(R.color.white);
                packageDesc.setText("Validity 30 Days\n\n₹ 600\n\n5000 Messages\n\nGold Profile Badge");
                intentX.putExtra("package", "gold");
            }
        });

        diamondPackageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silverPackageBtn.setBackgroundResource(R.color.white);
                goldPackageBtn.setBackgroundResource(R.color.white);
                diamondPackageBtn.setBackgroundResource(R.color.green_text);
                packageDesc.setText("Validity 30 Days\n\n₹ 900\n\n10000 Messages\n\nDiamond Profile Badge");
                intentX.putExtra("package", "diamond");
            }
        });


        builder3.setView(signInLayout2);
        builder3.setPositiveButton("Continue", (dialog2, which) -> {
            startActivity(intentX);
        });
        builder3.setNegativeButton("Cancel", (dialog2, which) -> dialog2.dismiss());
        builder3.create().show();

    }

    private void hotgameButtonAction(Direction direction) {
        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                .setDirection(direction)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(new AccelerateInterpolator())
                .build();
        manager.setSwipeAnimationSetting(setting);
        if (direction == Direction.Right) {
            mCardStackView.swipe();
        }
        if (direction == Direction.Left) {
            mCardStackView.rewind();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //updateSpotLight();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLocation();
    }

    public void updateLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mLastLocation = location;
                                App.getInstance().setLat(mLastLocation.getLatitude());
                                App.getInstance().setLng(mLastLocation.getLongitude());
                                if (itemsList.size() == 0) {
                                    loading = true;
                                    updateView();
                                    getItems();
                                }
                            } else {
                                if (App.getInstance().getLat() == 0.000000 || App.getInstance().getLng() == 0.000000) {
                                    App.getInstance().setLat(39.9199);
                                    App.getInstance().setLng(32.8543);
                                    if (itemsList.size() == 0) {
                                        loading = true;
                                        updateView();
                                        getItems();
                                    }
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                    alertDialog.setTitle(getText(R.string.app_name));
                                    alertDialog.setMessage(getText(R.string.msg_location_detect_error));
                                    alertDialog.setCancelable(true);
                                    alertDialog.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alertDialog.show();
                                }
                            }
                        }
                    });
        } else {
            Log.e("GPS", "error");
        }
    }

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        appSettingsActivityResultLauncher.launch(appSettingsIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putInt("itemId", itemId);
        outState.putInt("position", position);
        outState.putInt("gender", gender);
        outState.putInt("liked", liked);
        outState.putInt("distance", distance);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void getItems() {
        loading = true;
        if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {
            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_HOTGAME_GET, null,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(JSONObject response) {
                            if (!isAdded() || getActivity() == null) {
                                Log.e("ERROR", "HotgameFragment Not Added to Activity");
                                return;
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
                                                    itemsList.add(profile);
                                                    itemsAdapter.notifyDataSetChanged();
                                                } else {
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
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (!isAdded() || getActivity() == null) {
                        Log.e("ERROR", "HotgameFragment Not Added to Activity");
                        return;
                    }
                    loadingComplete();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("distance", String.valueOf(distance));
                    params.put("lat", Double.toString(App.getInstance().getLat()));
                    params.put("lng", Double.toString(App.getInstance().getLng()));
                    params.put("itemId", Long.toString(itemId));
                    params.put("sex", String.valueOf(gender));
                    params.put("liked", String.valueOf(liked));
                    return params;
                }
            };
            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonReq.setRetryPolicy(policy);
            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void loadingComplete() {
        loading = false;
        updateView();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        //inflater.inflate(R.menu.menu_hotgame, menu);

        MainMenu = menu;
    }

    public void getHotGameSettings() {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(getText(R.string.label_hotgame_dialog_title));

        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_hotgame_settings, null);

        b.setView(view);

        final CheckBox mLikedCheckBox = view.findViewById(R.id.likedCheckBox);

        final RadioButton mAnyGenderRadio = view.findViewById(R.id.radio_gender_any);
        final RadioButton mMaleGenderRadio = view.findViewById(R.id.radio_gender_male);
        final RadioButton mFemaleGenderRadio = view.findViewById(R.id.radio_gender_female);


        final TextView mDistanceLabel = view.findViewById(R.id.distance_label);

        final AppCompatSeekBar mDistanceSeekBar = view.findViewById(R.id.choice_distance);

        switch (gender) {
            case 0: {
                mMaleGenderRadio.setChecked(true);
                break;
            }

            case 1: {
                mFemaleGenderRadio.setChecked(true);
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

        if (liked == 1) {
            mLikedCheckBox.setChecked(true);
        } else {
            mLikedCheckBox.setChecked(false);
        }

        b.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // get distance

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


                if (mLikedCheckBox.isChecked()) {
                    liked = 1;
                } else {
                    liked = 0;
                }
                itemsList.clear();
                itemId = 0;
                position = -1;
                loading = true;
                saveFilterSettings();
                updateView();
                getItems();
            }
        });

        b.setNegativeButton(getText(R.string.action_cancel), (dialog, which) -> dialog.cancel());

        AlertDialog d = b.create();
        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        d.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_hotgame_settings: {
                getHotGameSettings();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void like(final long profileId) {
        loading = true;
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_LIKE, null,
                response -> {
                    if (!isAdded() || getActivity() == null) {
                        Log.e("ERROR", "HotgameFragment Not Added to Activity");
                        return;
                    }
                    try {
                        if (!response.getBoolean("error")) {
                            Profile u = itemsList.get(manager.getTopPosition());
                            u.setMyLike(response.getBoolean("myLike"));
                            if (u.isMyLike()) {
                                mHotgameLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                            }
                            if (!u.isMyLike()) {
                                mHotgameLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.statusBarColor)));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    if (!isAdded() || getActivity() == null) {
                        Log.e("ERROR", "HotgameFragment Not Added to Activity");
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
        liked = App.getInstance().getSharedPref().getInt(getString(R.string.settings_hotgame_liked), 1);
        distance = App.getInstance().getSharedPref().getInt(getString(R.string.settings_hotgame_distance), 1000);
    }

    public void saveFilterSettings() {
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_gender), gender).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_liked), liked).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_distance), distance).apply();
    }

    //

    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (direction == Direction.Right) {
        }
        if (direction == Direction.Left) {
        }
        if (itemsList.size() <= manager.getTopPosition()+3) {
            loading = true;
            getItems();
        }
    }

    @Override
    public void onCardRewound() {
        //Log.e("Swipe", "onCardRewound");
    }

    @Override
    public void onCardCanceled() {

    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    public void onCardAppeared(View view, int position) {
        Profile u = itemsList.get(manager.getTopPosition());
        if (u.isMyLike()) {
            mHotgameLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        }
        if (!u.isMyLike()) {
            mHotgameLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.statusBarColor)));
        }
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        //Log.e("Swipe", "onCardDisappeared");
    }

}