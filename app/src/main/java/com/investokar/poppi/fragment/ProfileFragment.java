package com.investokar.poppi.fragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.investokar.poppi.R;
import com.investokar.poppi.activity.AccountSettingsActivity;
import com.investokar.poppi.activity.AddPhotoActivity;
import com.investokar.poppi.activity.ChatActivity;
import com.investokar.poppi.activity.PhotoViewActivity;
import com.investokar.poppi.activity.ProfileActivity;
import com.investokar.poppi.adapter.GalleryListAdapter;
import com.investokar.poppi.app.App;
import com.investokar.poppi.common.ActivityBase;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.dialogs.ProfileBlockDialog;
import com.investokar.poppi.dialogs.ProfileReportDialog;
import com.investokar.poppi.model.Image;
import com.investokar.poppi.model.Profile;
import com.investokar.poppi.tagview.TagContainerLayout;
import com.investokar.poppi.util.Api;
import com.investokar.poppi.util.CustomRequest;
import com.investokar.poppi.util.Helper;
import com.investokar.poppi.util.ToastWindow;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    ToastWindow toastWindow = new ToastWindow();
    private ProgressDialog pDialog;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 1;
    private static final int PROFILE_EDIT = 3;
    private static final int PROFILE_NEW_POST = 4;
    private static final int CREATE_PHOTO = 5;
    private static final int PROFILE_CHAT = 7;


    private int mAccountAction = 0; // 0 = choicePhoto

    RelativeLayout mProfileLoadingScreen, mProfileErrorScreen, mProfileDisabledScreen;

    LinearLayout mLocationContainer, mProfileInfoContainer;

    LinearLayout mProfileAgeContainer, mProfileHeightContainer, mProfileStatusContainer, mProfileJoinDateContainer, mProfileGenderContainer, mProfileReligiousViewContainer, mProfileSiteContainer;
    LinearLayout mProfileSmokingViewsContainer, mProfileAlcoholViewsContainer, mProfileProfileLookingContainer, mProfileGenderLikeContainer;
    TextView mProfileSmokingViews, mProfileAlcoholViews, mProfileProfileLooking, mProfileGenderLike;
    TextView mProfileAge, mProfileHeight, mProfileStatus, mProfileJoinDate, mProfileGender, mProfileReligiousView;
    TagContainerLayout mTagContainerLayout;

    SwipeRefreshLayout mProfileRefreshLayout;
    NestedScrollView mNestedScrollView;

    ImageView mProfileLevelIcon;

    ImageView mProfilePhoto;
    ImageView mProfileOnlineIcon;
    TextView mProfileLocation, mProfileFullname, mProfileUsername;
    RecyclerView mRecyclerView;

    TextView mProfileMessageBtn, mProfileActionBtn;

    Profile profile;

    private ArrayList<Image> itemsList;

    private GalleryListAdapter itemsAdapter;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    private Boolean loadingComplete = false;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;

    private String profile_mention;
    public long profile_id;
    int itemId = 0;
    int arrayLength = 0;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;

    private Boolean isMainScreen = false;

    //

    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = requireActivity().getIntent();


        profile_id = i.getLongExtra("profileId", 0);

        if (profile_id == 0) {
            profile_id = App.getInstance().getId();
            isMainScreen = true;
        }

        profile = new Profile();
        profile.setId(profile_id);

        itemsList = new ArrayList<>();
        itemsAdapter = new GalleryListAdapter(requireActivity(), itemsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new GalleryListAdapter(requireActivity(), itemsList);

            itemId = savedInstanceState.getInt("itemId");

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            profile = savedInstanceState.getParcelable("profileObj");
        } else {
            itemsList = new ArrayList<>();
            itemsAdapter = new GalleryListAdapter(requireActivity(), itemsList);

            itemId = 0;

            restore = false;
            loading = false;
            preload = false;
        }

        if (loading) {
            showpDialog();
        }

        //

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() == Activity.RESULT_OK) {

                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                if (result.getData() != null) {
                    selectedImage = result.getData().getData();

                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = requireContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    selectedImagePath = cursor.getString(columnIndex);
                    cursor.close();

                    File f = new File(selectedImagePath);
                    uploadFile(f, mAccountAction);
                }
            }
        });


        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;
            String storage_permission = Manifest.permission.READ_EXTERNAL_STORAGE;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                storage_permission = Manifest.permission.READ_MEDIA_IMAGES;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(storage_permission)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {

                Log.e("Permissions", "granted");

                choiceImage();

            } else {

                Log.e("Permissions", "denied");


                Snackbar snackbar = Snackbar.make(requireView(), getString(R.string.label_no_storage_permission), Snackbar.LENGTH_LONG);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    snackbar.setText(getString(R.string.label_grant_media_permission));
                }
                snackbar.setAction(getString(R.string.action_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);
                    }
                }).show();

            }

        });

        //


        //


        mProfileRefreshLayout = rootView.findViewById(R.id.profileRefreshLayout);
        mProfileRefreshLayout.setOnRefreshListener(this);

        mProfileLoadingScreen = rootView.findViewById(R.id.profileLoadingScreen);
        mProfileErrorScreen = rootView.findViewById(R.id.profileErrorScreen);
        mProfileDisabledScreen = rootView.findViewById(R.id.profileDisabledScreen);

        mProfileInfoContainer = rootView.findViewById(R.id.profileInfoContainer);

        mProfileAgeContainer = rootView.findViewById(R.id.profileAgeContainer);
        mProfileHeightContainer = rootView.findViewById(R.id.profileHeightContainer);

        mProfileAge = rootView.findViewById(R.id.profileAge);
        mProfileHeight = rootView.findViewById(R.id.profileHeight);


        mProfileStatusContainer = rootView.findViewById(R.id.profileStatusContainer);
        mProfileJoinDateContainer = rootView.findViewById(R.id.profileJoinDateContainer);
        mProfileGenderContainer = rootView.findViewById(R.id.profileGenderContainer);
        mProfileReligiousViewContainer = rootView.findViewById(R.id.profileReligiousViewContainer);
        mProfileSmokingViewsContainer = rootView.findViewById(R.id.profileSmokingViewsContainer);
        mProfileAlcoholViewsContainer = rootView.findViewById(R.id.profileAlcoholViewsContainer);
        mProfileProfileLookingContainer = rootView.findViewById(R.id.profileProfileLookingContainer);
        mProfileGenderLikeContainer = rootView.findViewById(R.id.profileGenderLikeContainer);

        mProfileSiteContainer = rootView.findViewById(R.id.profileSiteContainer);

        mProfileStatus = rootView.findViewById(R.id.profileStatus);
        mProfileJoinDate = rootView.findViewById(R.id.profileJoinDate);
        mProfileGender = rootView.findViewById(R.id.profileGender);
        mProfileReligiousView = rootView.findViewById(R.id.profileReligiousView);
        mProfileSmokingViews = rootView.findViewById(R.id.profileSmokingViews);
        mProfileAlcoholViews = rootView.findViewById(R.id.profileAlcoholViews);
        mProfileProfileLooking = rootView.findViewById(R.id.profileProfileLooking);
        mProfileGenderLike = rootView.findViewById(R.id.profileGenderLike);


        mTagContainerLayout = rootView.findViewById(R.id.tagcontainerLayout);

        mNestedScrollView = rootView.findViewById(R.id.nestedScrollView);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(requireActivity(), 1); //Helper.getGalleryGridCount(requireActivity()));
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setPadding(2, 2, 2, 2);

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    if (!loadingMore && (viewMore) && !(mProfileRefreshLayout.isRefreshing())) {
                        mProfileRefreshLayout.setRefreshing(true);
                        loadingMore = true;
                        getItems();
                    }
                }
            }
        });

        mProfileActionBtn = rootView.findViewById(R.id.profileActionBtn);
        mProfileMessageBtn = rootView.findViewById(R.id.profileMessageBtn);

        mProfileFullname = rootView.findViewById(R.id.profileFullname);
        mProfileUsername = rootView.findViewById(R.id.profileUsername);

        mProfileOnlineIcon = rootView.findViewById(R.id.profileOnlineIcon);


        mLocationContainer = rootView.findViewById(R.id.profileLocationContainer);
        mProfileLocation = rootView.findViewById(R.id.profileLocation);



        mProfilePhoto = rootView.findViewById(R.id.profilePhoto);
        int screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mProfilePhoto.setMinimumHeight(screenwidth);
        mProfilePhoto.setMaxHeight((int) (screenwidth*1.3f));
        //mProfilePhoto.setMaxZoom(10f);
        mProfileLevelIcon = rootView.findViewById(R.id.profileLevelIcon);



        mProfileActionBtn.setOnClickListener(v -> {
            if (App.getInstance().getId() == profile.getId()) {
                getAccountSettings();
            }
        });

        ((ProfileActivity)requireActivity()).mFabButton.setOnClickListener(v -> {
            if (profile.getId() == App.getInstance().getId()) {
                Intent intent = new Intent(requireActivity(), AddPhotoActivity.class);
                startActivity(intent);
            } else {
                if (!profile.isInBlackList()) {
                    animateIcon(((ProfileActivity)requireActivity()).mFabButton);
                    like(profile.getId());
                } else {
                    toastWindow.makeText(getString(R.string.error_action), 2000);
                }
            }
        });

        mProfileMessageBtn.setOnClickListener(v -> {
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

                    i.putExtra("level", profile.getLevelMode());

                    startActivity(i);
                } else {
                    toastWindow.makeText(getString(R.string.error_action), 2000);
                }
        });


        mProfilePhoto.setOnClickListener(v -> {

            if (!profile.getBigPhotoUrl().isEmpty() && (App.getInstance().getSettings().isAllowShowNotModeratedProfilePhotos() || App.getInstance().getId() == profile.getId() || profile.getPhotoModerateAt() != 0)) {
                Intent i = new Intent(requireActivity(), PhotoViewActivity.class);
                i.putExtra("imageUrl", profile.getBigPhotoUrl());
                startActivity(i);
            }
        });

        if (profile.getFullname() == null || profile.getFullname().isEmpty()) {
            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getData();

                Log.e("Profile", "OnReload");

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {
                if (profile.getState() == ACCOUNT_STATE_ENABLED) {

                    showContentScreen();

                    loadingComplete();
                    updateProfile();

                } else {
                    showDisabledScreen();
                }

            } else {
                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onDestroyView() {
        super.onDestroyView();
        hidepDialog();
    }


    private void animateIcon(View view) {
        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(300);
        scale.setInterpolator(new LinearInterpolator());
        view.startAnimation(scale);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("itemId", itemId);

        outState.putBoolean("restore", restore);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putParcelable("profileObj", profile);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    private Bitmap resize(String path){

        int maxWidth = 512;
        int maxHeight = 512;

        // create the options
        BitmapFactory.Options opts = new BitmapFactory.Options();

        //just decode the file
        opts.inJustDecodeBounds = true;
        Bitmap bp = BitmapFactory.decodeFile(path, opts);

        //get the original size
        int orignalHeight = opts.outHeight;
        int orignalWidth = opts.outWidth;

        //initialization of the scale
        int resizeScale = 1;

        //get the good scale
        if (orignalWidth > maxWidth || orignalHeight > maxHeight) {

            final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
            final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
            resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        //put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        opts.inSampleSize = resizeScale;
        opts.inJustDecodeBounds = false;

        //get the futur size of the bitmap
        int bmSize = (orignalWidth / resizeScale) * (orignalHeight / resizeScale) * 4;

        //check if it's possible to store into the vm java the picture
        if (Runtime.getRuntime().freeMemory() > bmSize) {

            //decode the file
            bp = BitmapFactory.decodeFile(path, opts);

        } else {

            return null;
        }

        return bp;
    }

    public void save(String outFile, String inFile) {

        try {

            Bitmap bmp = resize(outFile);

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER, inFile);
            FileOutputStream fOut = new FileOutputStream(file);

            assert bmp != null;
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();

        } catch (Exception ex) {

            Log.e("Error", ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STREAM_NEW_POST && resultCode == Activity.RESULT_OK && null != data) {
            profile.setImagesCount(profile.getImagesCount() + 1);
            itemId = 0;
            getItems();
        } else if (requestCode == PROFILE_EDIT && resultCode == Activity.RESULT_OK) {

            profile.setFullname(data.getStringExtra("fullname"));
            profile.setLocation(data.getStringExtra("location"));
            profile.setInterests(data.getStringExtra("interests"));
            profile.setBio(data.getStringExtra("bio"));

            profile.setGender(data.getIntExtra("gender", 0));

            profile.setAge(data.getIntExtra("age", 0));
            profile.setHeight(data.getIntExtra("height", 0));

            profile.setReligiousView(data.getIntExtra("religiousView", 0));
            profile.setViewsOnSmoking(data.getIntExtra("viewsOnSmoking", 0));
            profile.setViewsOnAlcohol(data.getIntExtra("viewsOnAlcohol", 0));
            profile.setYouLooking(data.getIntExtra("youLooking", 0));
            profile.setYouLike(data.getIntExtra("youLike", 0));
            
            updateProfile();

        } else if (requestCode == PROFILE_NEW_POST && resultCode == requireActivity().RESULT_OK) {

            getData();

        }
    }

    public void choiceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imgFromGalleryActivityResultLauncher.launch(intent);
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            getData();

        } else {

            mProfileRefreshLayout.setRefreshing(false);
        }
    }


    @SuppressLint("SetTextI18n")
    public void updateProfile() {
        updateFullname();
        updateActionButton();

        mProfileUsername.setText("@" + profile.getUsername());
        mProfileLocation.setText(profile.getLocation());

        // Show settings button is your profile
        if (profile.getId() == App.getInstance().getId()) {
            ((ProfileActivity)requireActivity()).mFabButton.setImageResource(R.drawable.ic_action_new);

            mProfileActionBtn.setText(R.string.action_profile_edit);
            mProfileActionBtn.setVisibility(View.VISIBLE);
            mProfileMessageBtn.setVisibility(View.GONE);
        } else {
            mProfileActionBtn.setVisibility(View.GONE);
            if (profile.isILike()) {
                ((ProfileActivity)requireActivity()).mFabButton.setImageResource(R.drawable.ic_action_liked);
            }
            if (!profile.isILike()) {
                ((ProfileActivity)requireActivity()).mFabButton.setImageResource(R.drawable.ic_action_like);
            }

            mProfileMessageBtn.setText(R.string.action_message);
            mProfileMessageBtn.setVisibility(View.VISIBLE);

            if (!profile.isInBlackList()) {

                mProfileMessageBtn.setEnabled(true);

            } else {

                mProfileMessageBtn.setEnabled(false);
            }
        }

        if (profile.getLocation() != null && profile.getLocation().length() != 0) {

            mLocationContainer.setVisibility(View.VISIBLE);

        } else {

            mLocationContainer.setVisibility(View.GONE);
        }


        if (profile.getInterests() != null && profile.getInterests().length() != 0) {

            mProfileSiteContainer.setVisibility(View.VISIBLE);
            //mProfileInterests.setText(profile.getInterests());

            List<String> items = Arrays.asList(profile.getInterests().split("\\s*,\\s*"));
            mTagContainerLayout.setTags(items);

        } else {

            mProfileSiteContainer.setVisibility(View.GONE);
        }

        if (profile.getBio() != null && profile.getBio().length() != 0) {

            mProfileStatusContainer.setVisibility(View.VISIBLE);
            mProfileStatus.setText(profile.getBio());

        } else {
            mProfileStatusContainer.setVisibility(View.GONE);
        }

        if (profile.getGender() == 0) {
            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_male));
        } else if (profile.getGender() == 1) {
            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_female));
        } else if (profile.getGender() == 2) {
            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_other));
        }

        mProfileAge.setText(getString(R.string.label_age) + ": " + profile.getAge());
        mProfileHeight.setText(getString(R.string.label_height) + ": " + profile.getHeight() + " (" + getString(R.string.label_cm) + ")");
        
        Helper helper = new Helper(getContext());

        mProfileReligiousView.setText(getString(R.string.account_religious_view) + ": " + helper.getReligiousView(profile.getReligiousView()));
        mProfileSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + helper.getSmokingViews(profile.getViewsOnSmoking()));
        mProfileAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + helper.getAlcoholViews(profile.getViewsOnAlcohol()));
        mProfileProfileLooking.setText(getString(R.string.account_profile_looking) + ": " + helper.getLooking(profile.getYouLooking()));
        mProfileGenderLike.setText(getString(R.string.account_profile_like) + ": " + helper.getGenderLike(profile.getYouLike()));

        mProfileJoinDate.setText(getString(R.string.label_profile_join) + ": " + profile.getCreateDate());

        if (profile.getAge() == 0) {
            mProfileAgeContainer.setVisibility(View.GONE);
        } else {
            mProfileAgeContainer.setVisibility(View.GONE);
        }

        if (profile.getHeight() == 0) {
            mProfileHeightContainer.setVisibility(View.GONE);
        } else {
            mProfileHeightContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getReligiousView() == 0) {
            mProfileReligiousViewContainer.setVisibility(View.GONE);
        }


        if (profile.getViewsOnSmoking() == 0) {
            mProfileSmokingViewsContainer.setVisibility(View.GONE);
        }

        if (profile.getViewsOnAlcohol() == 0) {
            mProfileAlcoholViewsContainer.setVisibility(View.GONE);
        }

        if (profile.getYouLooking() == 0) {
            mProfileProfileLookingContainer.setVisibility(View.GONE);
        }

        if (profile.getYouLike() == 0) {
            mProfileGenderLikeContainer.setVisibility(View.GONE);
        }

        showPhoto(profile.getBigPhotoUrl());

        showContentScreen();

        if (profile.isOnline()) {

            // user Online

            mProfileOnlineIcon.setVisibility(View.VISIBLE);

        } else {

            mProfileOnlineIcon.setVisibility(View.GONE);
        }


            switch (profile.getLevelMode()) {
                case 1:
                    mProfileLevelIcon.setVisibility(View.VISIBLE);
                    mProfileLevelIcon.setImageResource(R.drawable.level_silver);
                    break;
                case 2:
                    mProfileLevelIcon.setVisibility(View.VISIBLE);
                    mProfileLevelIcon.setImageResource(R.drawable.level_gold);
                    break;
                case 3:
                    mProfileLevelIcon.setVisibility(View.VISIBLE);
                    mProfileLevelIcon.setImageResource(R.drawable.level_diamond);
                    break;
                default:
                    mProfileLevelIcon.setVisibility(View.GONE);
                    break;
            }    

        // Profile Info
        mProfileInfoContainer.setVisibility(View.VISIBLE);

    

        if (profile.getImagesCount() > 0 && itemsAdapter.getItemCount() != 0) {

                mRecyclerView.setVisibility(View.VISIBLE);

            } else {

                mRecyclerView.setVisibility(View.GONE);
            }

        if (this.isVisible()) {

            try {

                requireActivity().invalidateOptionsMenu();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void updateFullname() {
        if(profile.getFullname() == null)  {
            profile.setFullname("");
        }
        String fullname = profile.getFullname();
		if (profile.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			fullname = fullname.substring(0, fullname.lastIndexOf(' '));
        }
        
        mProfileFullname.setText(fullname);
        if (!isMainScreen) requireActivity().setTitle(fullname);
    }

    public void getData() {
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded()) {
                            Log.e("ERROR", "ProfileFragment Not Added to Activity");
                            return;
                        } else {
                            requireActivity();
                        }
                        try {
                            if (!response.getBoolean("error")) {
                                profile = new Profile(response);
                                if (profile.getImagesCount() > 0) {
                                    getItems();
                                }
                                if (profile.getState() == ACCOUNT_STATE_ENABLED) {
                                    showContentScreen();
                                    updateProfile();
                                } else {
                                    showDisabledScreen();
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {


                            Log.e("Profile Success",  response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded()) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }

                Log.e("Profile Error",  error.toString() + error.getMessage() + error.getLocalizedMessage());
                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));

                return params;
            }
        };

        jsonReq.setRetryPolicy(new RetryPolicy() {

            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        App.getInstance().addToRequestQueue(jsonReq);
    }


    public void showPhoto(String photoUrl) {
        if (photoUrl != null && photoUrl.length() > 0 && (App.getInstance().getSettings().isAllowShowNotModeratedProfilePhotos() || App.getInstance().getId() == profile.getId() || profile.getPhotoModerateAt() != 0)) {
            Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.profile_default_photo)
                    .error(R.drawable.profile_default_photo)
                    .into(mProfilePhoto);
        }
    }


    public void getItems() {
        if (loadingMore) {
            mProfileRefreshLayout.setRefreshing(true);
        } else{
            itemId = 0;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_GET, null,
                response -> {

                    if (!isAdded()) {
                        Log.e("ERROR", "ProfileFragment Not Added to Activity");
                        return;
                    } else {
                        requireActivity();
                    }

                    try {

                        if (!loadingMore) {
                            itemsList.clear();
                        }

                        arrayLength = 0;
                        if (!response.getBoolean("error")) {
                            itemId = response.getInt("itemId");
                            if (response.has("items")) {
                                JSONArray itemsArray = response.getJSONArray("items");
                                arrayLength = itemsArray.length();
                                if (arrayLength > 0) {
                                    for (int i = 0; i < itemsArray.length(); i++) {
                                        JSONObject itemObj = (JSONObject) itemsArray.get(i);
                                        Image item = new Image(itemObj);
                                        itemsList.add(item);
                                    }
                                }
                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loadingComplete();
                        updateProfile();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isAdded()) {
                    Log.e("ERROR", "ProfileFragment Not Added to Activity");
                    return;
                } else {
                    requireActivity();
                }

                loadingComplete();
                toastWindow.makeText(error.toString(), 2000);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile.getId()));
                params.put("itemId", String.valueOf(itemId));

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadingComplete() {

        viewMore = arrayLength == LIST_ITEMS;

        itemsAdapter.notifyDataSetChanged();

        mProfileRefreshLayout.setRefreshing(false);

        loadingMore = false;

//        if (this.isVisible()) requireActivity().invalidateOptionsMenu();
    }

    public void showLoadingScreen() {

        if (!isMainScreen) requireActivity().setTitle(getText(R.string.title_activity_profile));

        mProfileRefreshLayout.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
        mProfileDisabledScreen.setVisibility(View.GONE);
//
        mProfileLoadingScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showErrorScreen() {

        if (!isMainScreen) requireActivity().setTitle(getText(R.string.title_activity_profile));

        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileDisabledScreen.setVisibility(View.GONE);
        mProfileRefreshLayout.setVisibility(View.GONE);
//
        mProfileErrorScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showDisabledScreen() {

        if (profile.getState() != ACCOUNT_STATE_ENABLED) {

            //mProfileDisabledScreenMsg.setText(getText(R.string.msg_account_blocked));
        }

        requireActivity().setTitle(getText(R.string.label_account_disabled));

        mProfileRefreshLayout.setVisibility(View.GONE);
        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
//
        mProfileDisabledScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showContentScreen() {
        if (!isMainScreen) {
            String fullname = profile.getFullname();
		    if (profile.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			    fullname = fullname.substring(0, fullname.lastIndexOf(' '));
            }
            requireActivity().setTitle(fullname);
        }

        mProfileDisabledScreen.setVisibility(View.GONE);
        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
//
        mProfileRefreshLayout.setVisibility(View.VISIBLE);
        mProfileRefreshLayout.setRefreshing(false);

        loadingComplete = true;
        restore = true;
    }

    public void action(int position) {

        final Image item = itemsList.get(position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (profile.getState() != ACCOUNT_STATE_ENABLED) {

                //hide all menu items
                hideMenuItems(menu, false);
            }

            if (App.getInstance().getId() != profile.getId()) {

                MenuItem menuItem = menu.findItem(R.id.action_profile_block);

                if (profile.isBlocked()) {

                    menuItem.setTitle(getString(R.string.action_unblock));

                } else {

                    menuItem.setTitle(getString(R.string.action_block));
                }

                menu.removeItem(R.id.action_profile_edit_photo);
                menu.removeItem(R.id.action_profile_settings);

            } else {

                // your profile

                menu.removeItem(R.id.action_profile_report);
                menu.removeItem(R.id.action_profile_block);
            }


            //show all menu items
            hideMenuItems(menu, true);

        } else {

            //hide all menu items
            hideMenuItems(menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_profile_refresh) {
            mProfileRefreshLayout.setRefreshing(true);
            onRefresh();

            return true;
        } else if (id == R.id.action_profile_report) {
            profileReport();

            return true;
        } else if (id == R.id.action_profile_block) {
            profileBlock();

            return true;
        } else if (id == R.id.action_profile_edit_photo) {
            ActivityBase activity = (ActivityBase) getActivity();
            if (!activity.checkPermission(READ_EXTERNAL_STORAGE)) {
                activity.requestStoragePermission(storagePermissionLauncher);
            } else {
                mAccountAction = 0;
                choiceImage();
            }

            return true;
        } else if (id == R.id.action_profile_settings) {
            getAccountSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void profileReport() {

        /** Getting the fragment manager */
        FragmentManager fm = requireActivity().getSupportFragmentManager();

        /** Instantiating the DialogFragment class */
        ProfileReportDialog alert = new ProfileReportDialog();

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("position", 0);

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */

        alert.show(fm, "alert_dialog_profile_report");
    }

    public  void onProfileReport(final int position) {

        Api api = new Api(requireActivity());

        api.profileReport(profile.getId(), position);
    }

    public void profileBlock() {

        if (!profile.isBlocked()) {

            /** Getting the fragment manager */
            FragmentManager fm = requireActivity().getSupportFragmentManager();

            /** Instantiating the DialogFragment class */
            ProfileBlockDialog alert = new ProfileBlockDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putString("blockFullname", profile.getFullname());

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_dialog_profile_block");

        } else {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_REMOVE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || requireActivity() == null) {

                                Log.e("ERROR", "ProfileFragment Not Added to Activity");

                                return;
                            }

                            try {

                                if (!response.getBoolean("error")) {

                                    profile.setBlocked(false);

                                    toastWindow.makeText(getString(R.string.msg_profile_removed_from_blacklist), 2000);
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

                    if (!isAdded() || requireActivity() == null) {

                        Log.e("ERROR", "ProfileFragment Not Added to Activity");

                        return;
                    }

                    loading = false;

                    hidepDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("profileId", Long.toString(profile.getId()));

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public  void onProfileBlock() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_ADD, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || requireActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setBlocked(true);

                                toastWindow.makeText(getString(R.string.msg_profile_added_to_blacklist), 2000);
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

                if (!isAdded()) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile.getId()));
                params.put("reason", "example");
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean uploadFile(File file, final int type) {
        loading = true;
        showpDialog();
        final OkHttpClient client = new OkHttpClient();
        client.setProtocols(List.of(Protocol.HTTP_1_1));
        try {
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .addFormDataPart("imgType", String.valueOf(type))
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(METHOD_ACCOUNT_UPLOAD_IMAGE)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    loading = false;
                    hidepDialog();
                    Log.e("Failure", request.toString());
                }
                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);
                    try {
                        JSONObject result = new JSONObject(jsonData);
                        if (!result.getBoolean("error")) {
                            switch (type) {
                                case 0: {
                                    profile.setBigPhotoUrl(result.getString("bigPhotoUrl"));
                                    App.getInstance().setPhotoUrl(result.getString("bigPhotoUrl"));
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                        }
                        Log.d("My App", response.toString());
                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response.body().string() + "\"");
                    } finally {
                        loading = false;
                        hidepDialog();
                        getData();
                    }
                }
            });
            return true;
        } catch (Exception ex) {
            // Handle the error
            loading = false;
            hidepDialog();
        }
        return false;
    }


    public void updateActionButton() {
        if (profile.getId() == App.getInstance().getId()) {
            mProfileActionBtn.setText(R.string.action_profile_edit);
            mProfileActionBtn.setEnabled(true);
        } else {
            mProfileActionBtn.setVisibility(View.GONE);
        }
    }


    public void like(final long profileId) {
        loading = true;
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_LIKE, null,
                response -> {

                    if (!isAdded()) {

                        Log.e("ERROR", "ProfileFragment Not Added to Activity");

                        return;
                    } else {
                        requireActivity();
                    }

                    try {

                        if (!response.getBoolean("error")) {
                            if (response.has("likesCount")) {
                                profile.setLikesCount(response.getInt("likesCount"));
                            }
                            if (response.has("iLiked")) {
                                profile.setILike(response.getBoolean("iLiked"));
                                if (profile.isILike()) {
                                    ((ProfileActivity)requireActivity()).mFabButton.setImageResource(R.drawable.ic_action_liked);
                                }
                                if (!profile.isILike()) {
                                    ((ProfileActivity)requireActivity()).mFabButton.setImageResource(R.drawable.ic_action_like);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded()) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }
                loading = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }



    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }


    public void getAccountSettings() {

        Intent i = new Intent(requireActivity(), AccountSettingsActivity.class);
        i.putExtra("profileId", App.getInstance().getId());
        i.putExtra("gender", profile.getGender());
        i.putExtra("age", profile.getAge());
        i.putExtra("height", profile.getHeight());

        i.putExtra("religiousView", profile.getReligiousView());
        i.putExtra("viewsOnSmoking", profile.getViewsOnSmoking());
        i.putExtra("viewsOnAlcohol", profile.getViewsOnAlcohol());
        i.putExtra("youLooking", profile.getYouLooking());
        i.putExtra("youLike", profile.getYouLike());

        i.putExtra("fullname", profile.getFullname());
        i.putExtra("location", profile.getLocation());
        i.putExtra("interests", profile.getInterests());
        i.putExtra("bio", profile.getBio());
        startActivity(i);
    }

}