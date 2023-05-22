package com.hindbyte.dating.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.ChatActivity;
import com.hindbyte.dating.adapter.HotgameAdapter;
import com.hindbyte.dating.animation.Pager2Transformer;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.Profile;
import com.hindbyte.dating.util.CustomRequest;

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


    TextView mGrantPermissionButton, mShowFiltersButton;

    ViewPager2 mViewPager2;
    LinearLayout mCardsContainer;
    LinearLayout mPermissionPromptContainer, mHotgameEmptyContainer;

    private ArrayList<Profile> itemsList;
    private HotgameAdapter itemsAdapter;

    public ImageView mHotGameLike, mHotGameBack, mHotGameProfile, mHotGameNext;
    public ProgressBar mHotGameProgressBar;

    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    LocationManager lm;

    private int gender = 3, liked = 1, distance = 1000;

    private int itemId = 0;
    private int arrayLength = 0;
    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean permission_denied = false;

    public HotGameFragment() {
        // Required empty public constructor
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
        distance = 1000;
        gender = 3;
        liked = 1;
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

        lm = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
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
                if (u.isMyLike()) {
                    mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.app_circular_button_green));
                }
                if (!u.isMyLike()) {
                    mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.app_circular_button));

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
            if (profile.getAllowMessages() == 0 && !profile.isFriend()) {
                Toast.makeText(requireActivity(), getString(R.string.error_no_friend), Toast.LENGTH_SHORT).show();
            } else {
                if (!profile.isInBlackList()) {
                    Intent i = new Intent(requireActivity(), ChatActivity.class);
                    i.putExtra("chatId", 0);
                    i.putExtra("profileId", profile.getId());
                    i.putExtra("withProfile", profile.getFullname());
                    i.putExtra("with_user_username", profile.getUsername());
                    i.putExtra("with_user_fullname", profile.getFullname());
                    i.putExtra("with_user_photo_url", profile.getBigPhotoUrl());
                    i.putExtra("with_user_state", profile.getState());
                    startActivity(i);
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mHotGameLike.setOnClickListener(v -> {
            Profile p = itemsList.get(mViewPager2.getCurrentItem());
            like(p.getId());
        });


        mHotGameNext.setOnClickListener(v -> onCardSwiped("Right"));

        mHotGameBack.setOnClickListener(v -> onCardSwiped("Left"));

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

        if (!restore && App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {
            getItems();
        }
        updateView();
        return rootView;
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
            if (App.getInstance().getLat() != 0.0 && App.getInstance().getLng() != 0.0) {
                if (itemsList.size() != 0) {
                    mCardsContainer.setVisibility(View.VISIBLE);
                } else {
                    mHotgameEmptyContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (!permission_denied) {
                        mPermissionPromptContainer.setVisibility(View.VISIBLE);
                    }
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
        //updateLocation();
    }


    public void updateLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
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
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
                                    alertDialog.setTitle(getText(R.string.app_name));
                                    alertDialog.setMessage(getText(R.string.msg_location_detect_error));
                                    alertDialog.setCancelable(true);
                                    alertDialog.setPositiveButton(getText(R.string.action_ok), (dialog, which) -> dialog.cancel());
                                    alertDialog.show();
                                }
                            }
                        }
                    });
        } else {
            Log.e("GPS", "error");
        }
    }


    public void getItems() {
        loading = true;
        if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {
            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_HOTGAME_GET, null,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(JSONObject response) {
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
                    params.put("lat", Double.toString(App.getInstance().getLat()));
                    params.put("lng", Double.toString(App.getInstance().getLng()));
                    params.put("itemId", Long.toString(itemId));
                    params.put("gender", String.valueOf(gender));
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

    public void getHotGameSettings() {
        AlertDialog.Builder b = new AlertDialog.Builder(requireActivity());
        b.setTitle(getText(R.string.label_hotgame_dialog_title));
        LinearLayout view = (LinearLayout) requireActivity().getLayoutInflater().inflate(R.layout.dialog_hotgame_settings, null);
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

    @SuppressLint("NonConstantResourceId")
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
                            u.setMyLike(response.getBoolean("myLike"));
                            if (u.isMyLike()) {
                                mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.app_circular_button_green));
                            }
                            if (!u.isMyLike()) {
                                mHotGameLike.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.app_circular_button));
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
        liked = App.getInstance().getSharedPref().getInt(getString(R.string.settings_hotgame_liked), 1);
        distance = App.getInstance().getSharedPref().getInt(getString(R.string.settings_hotgame_distance), 1000);
    }

    public void saveFilterSettings() {
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_gender), gender).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_liked), liked).apply();
        App.getInstance().getSharedPref().edit().putInt(getString(R.string.settings_hotgame_distance), distance).apply();
    }

}