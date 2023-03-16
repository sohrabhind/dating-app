package com.hindbyte.dating.fragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.snackbar.Snackbar;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.AccountSettingsActivity;
import com.hindbyte.dating.activity.AddPhotoActivity;
import com.hindbyte.dating.activity.BalanceActivity;
import com.hindbyte.dating.activity.ChatActivity;
import com.hindbyte.dating.activity.FriendsActivity;
import com.hindbyte.dating.activity.GiftsActivity;
import com.hindbyte.dating.activity.LikesActivity;
import com.hindbyte.dating.activity.PhotoViewActivity;
import com.hindbyte.dating.activity.ProfileActivity;
import com.hindbyte.dating.activity.SendGiftActivity;
import com.hindbyte.dating.activity.ViewImageActivity;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.hindbyte.dating.adapter.GalleryListAdapter;
import com.hindbyte.dating.adapter.GiftsSelectListAdapter;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.dialogs.ProfileBlockDialog;
import com.hindbyte.dating.dialogs.ProfileReportDialog;
import com.hindbyte.dating.model.BaseGift;
import com.hindbyte.dating.model.Friend;
import com.hindbyte.dating.model.Gift;
import com.hindbyte.dating.model.Image;
import com.hindbyte.dating.model.Profile;
import com.hindbyte.dating.util.Api;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final int PROFILE_NEW_GIFT = 30001;

    private static final String STATE_LIST = "State Adapter Data";

    private ProgressDialog pDialog;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 1;
    private static final int PROFILE_EDIT = 3;
    private static final int PROFILE_NEW_POST = 4;
    private static final int CREATE_PHOTO = 5;
    private static final int PROFILE_CHAT = 7;


    private int mAccountAction = 0; // 0 = choicePhoto

    RelativeLayout mProfileLoadingScreen, mProfileErrorScreen, mProfileDisabledScreen;

    LinearLayout mLocationContainer, mProfileInfoContainer, mProfileCountersContainer;

    LinearLayout mProfileAgeContainer, mProfileHeightContainer, mProfileWeightContainer, mProfileStatusContainer, mProfileJoinDateContainer, mProfileBirthDateContainer, mProfileGenderContainer, mProfileRelationshipStatusContainer, mProfilePoliticalViewsContainer, mProfileWorldViewContainer, mProfilePersonalPriorityContainer, mProfileImportantInOthersContainer, mProfileSiteContainer;
    LinearLayout mProfileSmokingViewsContainer, mProfileAlcoholViewsContainer, mProfileProfileLookingContainer, mProfileGenderLikeContainer;
    TextView mProfileSmokingViews, mProfileAlcoholViews, mProfileProfileLooking, mProfileGenderLike;
    TextView mProfileAge, mProfileHeight, mProfileWeight, mProfileStatus, mProfileJoinDate, mProfileBirthDate, mProfileGender, mProfileRelationshipStatus, mProfilePoliticalViews, mProfileWorldView, mProfilePersonalPriority, mProfileImportantInOthers, mProfileInstagramUrl;

    SwipeRefreshLayout mProfileRefreshLayout;
    NestedScrollView mNestedScrollView;

    CircularImageView mProfileProIcon;
    ImageView mProfilePhoto, mProfileOnlineIcon;
    TextView mProfileLocation, mProfileFullname, mProfileUsername;
    RecyclerView mRecyclerView;
    TextView mProfileItemsCount, mProfileFriendsCount, mProfileLikesCount, mProfileGiftsCount;
    MaterialRippleLayout mProfileItemsBtn, mProfileFriendsBtn, mProfileLikesBtn, mProfileGiftsBtn;

    Button mProfileMessageBtn, mProfileActionBtn;

    Toolbar mToolbar;

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
    int accessMode = 0;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;

    private Boolean isMainScreen = false;

    //

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        profile_id = i.getLongExtra("profileId", 0);
        profile_mention = i.getStringExtra("profileMention");

        if (profile_id == 0 && (profile_mention == null || profile_mention.length() == 0)) {
            profile_id = App.getInstance().getId();
            isMainScreen = true;
        }

        profile = new Profile();
        profile.setId(profile_id);

        itemsList = new ArrayList<>();
        itemsAdapter = new GalleryListAdapter(getActivity(), itemsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new GalleryListAdapter(getActivity(), itemsList);

            itemId = savedInstanceState.getInt("itemId");

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            profile = savedInstanceState.getParcelable("profileObj");

        } else {

            itemsList = new ArrayList<>();
            itemsAdapter = new GalleryListAdapter(getActivity(), itemsList);


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

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;
                    File f = new File(selectedImagePath);
                    uploadFile(f, mAccountAction);
                }
            }
        });

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {

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

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        //

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                choiceImage();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //


        mProfileRefreshLayout = rootView.findViewById(R.id.profileRefreshLayout);
        mProfileRefreshLayout.setOnRefreshListener(this);

        mProfileLoadingScreen = rootView.findViewById(R.id.profileLoadingScreen);
        mProfileErrorScreen = rootView.findViewById(R.id.profileErrorScreen);
        mProfileDisabledScreen = rootView.findViewById(R.id.profileDisabledScreen);

        mProfileInfoContainer = rootView.findViewById(R.id.profileInfoContainer);
        mProfileCountersContainer = rootView.findViewById(R.id.profileCountersContainer);

        mProfileAgeContainer = rootView.findViewById(R.id.profileAgeContainer);
        mProfileHeightContainer = rootView.findViewById(R.id.profileHeightContainer);
        mProfileWeightContainer = rootView.findViewById(R.id.profileWeightContainer);

        mProfileAge = rootView.findViewById(R.id.profileAge);
        mProfileHeight = rootView.findViewById(R.id.profileHeight);
        mProfileWeight = rootView.findViewById(R.id.profileWeight);


        mProfileStatusContainer = rootView.findViewById(R.id.profileStatusContainer);
        mProfileJoinDateContainer = rootView.findViewById(R.id.profileJoinDateContainer);
        mProfileBirthDateContainer = rootView.findViewById(R.id.profileBirthDateContainer);
        mProfileGenderContainer = rootView.findViewById(R.id.profileGenderContainer);
        mProfileRelationshipStatusContainer = rootView.findViewById(R.id.profileRelationshipStatusContainer);
        mProfilePoliticalViewsContainer = rootView.findViewById(R.id.profilePoliticalViewsContainer);
        mProfileWorldViewContainer = rootView.findViewById(R.id.profileWorldViewContainer);
        mProfilePersonalPriorityContainer = rootView.findViewById(R.id.profilePersonalPriorityContainer);
        mProfileImportantInOthersContainer = rootView.findViewById(R.id.profileImportantInOthersContainer);
        mProfileSmokingViewsContainer = rootView.findViewById(R.id.profileSmokingViewsContainer);
        mProfileAlcoholViewsContainer = rootView.findViewById(R.id.profileAlcoholViewsContainer);
        mProfileProfileLookingContainer = rootView.findViewById(R.id.profileProfileLookingContainer);
        mProfileGenderLikeContainer = rootView.findViewById(R.id.profileGenderLikeContainer);

        mProfileSiteContainer = rootView.findViewById(R.id.profileSiteContainer);

        mProfileStatus = rootView.findViewById(R.id.profileStatus);
        mProfileJoinDate = rootView.findViewById(R.id.profileJoinDate);
        mProfileBirthDate = rootView.findViewById(R.id.profileBirthDate);
        mProfileGender = rootView.findViewById(R.id.profileGender);
        mProfileRelationshipStatus = rootView.findViewById(R.id.profileRelationshipStatus);
        mProfilePoliticalViews = rootView.findViewById(R.id.profilePoliticalViews);
        mProfileWorldView = rootView.findViewById(R.id.profileWorldView);
        mProfilePersonalPriority = rootView.findViewById(R.id.profilePersonalPriority);
        mProfileImportantInOthers = rootView.findViewById(R.id.profileImportantInOthers);
        mProfileSmokingViews = rootView.findViewById(R.id.profileSmokingViews);
        mProfileAlcoholViews = rootView.findViewById(R.id.profileAlcoholViews);
        mProfileProfileLooking = rootView.findViewById(R.id.profileProfileLooking);
        mProfileGenderLike = rootView.findViewById(R.id.profileGenderLike);

        mProfileInstagramUrl = rootView.findViewById(R.id.profileInstagramUrl);

        ((ProfileActivity)getActivity()).mFabButton.hide();




        mNestedScrollView = rootView.findViewById(R.id.nestedScrollView);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getGalleryGridCount(getActivity()));
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

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new FriendsFragment.RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Image img = itemsList.get(position);
                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.putExtra("itemId", img.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));

        mProfileActionBtn = rootView.findViewById(R.id.profileActionBtn);
        mProfileMessageBtn = rootView.findViewById(R.id.profileMessageBtn);

        mProfileFullname = rootView.findViewById(R.id.profileFullname);
        mProfileUsername = rootView.findViewById(R.id.profileUsername);

        mProfileOnlineIcon = rootView.findViewById(R.id.profileOnlineIcon);

        mProfileItemsCount = rootView.findViewById(R.id.profileItemsCount);
        mProfileFriendsCount = rootView.findViewById(R.id.profileFriendsCount);
        mProfileLikesCount = rootView.findViewById(R.id.profileLikesCount);
        mProfileGiftsCount = rootView.findViewById(R.id.profileGiftsCount);

        mProfileItemsBtn = rootView.findViewById(R.id.profileItemsBtn);
        mProfileFriendsBtn = rootView.findViewById(R.id.profileFriendsBtn);
        mProfileLikesBtn = rootView.findViewById(R.id.profileLikesBtn);
        mProfileGiftsBtn = rootView.findViewById(R.id.profileGiftsBtn);

        mLocationContainer = rootView.findViewById(R.id.profileLocationContainer);
        mProfileLocation = rootView.findViewById(R.id.profileLocation);


        mProfileSiteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!profile.getInstagramPage().startsWith("https://") && !profile.getInstagramPage().startsWith("http://")){

                    profile.setInstagramPage("http://" + profile.getInstagramPage());
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(profile.getInstagramPage()));
                startActivity(i);
            }
        });

        mProfileFriendsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileFriends(profile.getId());
            }
        });

        mProfileLikesBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showProfileLikes(profile.getId());
            }
        });

        mProfilePhoto = rootView.findViewById(R.id.profilePhoto);
        mProfileProIcon = rootView.findViewById(R.id.profileProIcon);

        mProfileActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() == profile.getId()) {

                    getAccountSettings();

                } else {

                    if (profile.isFriend()) {

                        removeFromFriends();

                    } else {

                        friendsRequest();
                    }
                }
            }
        });

        ((ProfileActivity)getActivity()).mFabButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (profile.getId() == App.getInstance().getId()) {

                    Intent intent = new Intent(getActivity(), AddPhotoActivity.class);
                    startActivityForResult(intent, STREAM_NEW_POST);

                } else {

                    if (!profile.isInBlackList()) {

                        like(profile.getId());

                    } else {

                        Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mProfileMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().isPro() || App.getInstance().getFreeMessagesCount() > 0) {

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

                    Toast.makeText(getActivity(), getString(R.string.msg_pro_mode_alert), Toast.LENGTH_LONG).show();
                }
            }
        });

        mProfileGiftsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showProfileGifts(profile.getId());
            }
        });


        mProfilePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (profile.getNormalPhotoUrl().length() > 0 &&
                        (App.getInstance().getSettings().isAllowShowNotModeratedProfilePhotos() || App.getInstance().getId() == profile.getId() || profile.getPhotoModerateAt() != 0)) {

                    Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                    i.putExtra("imgUrl", profile.getNormalPhotoUrl());
                    startActivity(i);
                }
            }
        });

        if (profile.getFullname() == null || profile.getFullname().length() == 0) {

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

    @Override
    public void onSaveInstanceState(Bundle outState) {

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

    private static int exifToDegrees(int exifOrientation) {

        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {

            return 90;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {

            return 180;

        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {

            return 270;
        }

        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STREAM_NEW_POST && resultCode == Activity.RESULT_OK && null != data) {
            profile.setPhotosCount(profile.getPhotosCount() + 1);
            itemId = 0;
            getItems();
        } else if (requestCode == PROFILE_EDIT && resultCode == Activity.RESULT_OK) {

            profile.setFullname(data.getStringExtra("fullname"));
            profile.setLocation(data.getStringExtra("location"));
            profile.setInstagramPage(data.getStringExtra("instagramPage"));
            profile.setBio(data.getStringExtra("bio"));

            profile.setSex(data.getIntExtra("sex", 0));

            profile.setAge(data.getIntExtra("age", 0));
            profile.setHeight(data.getIntExtra("height", 0));
            profile.setWeight(data.getIntExtra("weight", 0));

            profile.setYear(data.getIntExtra("year", 0));
            profile.setMonth(data.getIntExtra("month", 0));
            profile.setDay(data.getIntExtra("day", 0));

            profile.setRelationshipStatus(data.getIntExtra("relationshipStatus", 0));
            profile.setPoliticalViews(data.getIntExtra("politicalViews", 0));
            profile.setWorldView(data.getIntExtra("worldView", 0));
            profile.setPersonalPriority(data.getIntExtra("personalPriority", 0));
            profile.setImportantInOthers(data.getIntExtra("importantInOthers", 0));
            profile.setViewsOnSmoking(data.getIntExtra("viewsOnSmoking", 0));
            profile.setViewsOnAlcohol(data.getIntExtra("viewsOnAlcohol", 0));
            profile.setYouLooking(data.getIntExtra("youLooking", 0));
            profile.setYouLike(data.getIntExtra("youLike", 0));

            profile.setAllowShowMyBirthday(data.getIntExtra("allowShowMyBirthday", 0));

            updateProfile();

        } else if (requestCode == PROFILE_NEW_POST && resultCode == getActivity().RESULT_OK) {

            getData();

        }
    }

    public void choiceImage() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        imgFromGalleryActivityResultLauncher.launch(intent);
                        break;
                    }
                    default: {
                        if (checkPermission(Manifest.permission.CAMERA)) {
                            try {
                                newImageFileName = Helper.randomString(6) + ".jpg";
                                selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName));
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                imgFromCameraActivityResultLauncher.launch(cameraIntent);
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            requestCameraPermission();
                        }
                        break;
                    }
                }

            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            getData();

        } else {

            mProfileRefreshLayout.setRefreshing(false);
        }
    }


    public void updateProfile() {

        updateFullname();
        updateGiftsCount();
        updateFriendsCount();
        updateLikesCount();
        updateActionButton();

        mProfileUsername.setText("@" + profile.getUsername());
        mProfileLocation.setText(profile.getLocation());

        mProfileItemsCount.setText(String.valueOf(profile.getPhotosCount()));
        mProfileFriendsCount.setText(String.valueOf(profile.getFriendsCount()));
        mProfileLikesCount.setText(String.valueOf(profile.getLikesCount()));
        mProfileGiftsCount.setText(String.valueOf(profile.getGiftsCount()));

        // Show settings button is your profile
        if (profile.getId() == App.getInstance().getId()) {

            ((ProfileActivity)getActivity()).mFabButton.show();
            ((ProfileActivity)getActivity()).mFabButton.setImageResource(R.drawable.ic_action_new);

            mProfileActionBtn.setText(R.string.action_profile_edit);

            mProfileActionBtn.setVisibility(View.VISIBLE);

            mProfileMessageBtn.setVisibility(View.GONE);

        } else {

            ((ProfileActivity)getActivity()).mFabButton.hide();

            if (!profile.isMyLike()) {

                ((ProfileActivity)getActivity()).mFabButton.setImageResource(R.drawable.ic_action_like);

                ((ProfileActivity)getActivity()).mFabButton.show();
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


        if (profile.getInstagramPage() != null && profile.getInstagramPage().length() != 0) {

            mProfileSiteContainer.setVisibility(View.VISIBLE);
            mProfileInstagramUrl.setText(profile.getInstagramPage());

        } else {

            mProfileSiteContainer.setVisibility(View.GONE);
        }

        if (profile.getBio() != null && profile.getBio().length() != 0) {

            mProfileStatusContainer.setVisibility(View.VISIBLE);
            mProfileStatus.setText(profile.getBio());

        } else {
            mProfileStatusContainer.setVisibility(View.GONE);
        }

        if (profile.getSex() == 0) {
            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_male));
        } else if (profile.getSex() == 1) {
            mProfileGender.setText(getString(R.string.label_sex) + ": " + getString(R.string.label_female));
        }

        mProfileAge.setText(getString(R.string.label_age) + ": " + profile.getAge());
        mProfileHeight.setText(getString(R.string.label_height) + ": " + profile.getHeight() + " (" + getString(R.string.label_cm) + ")");
        mProfileWeight.setText(getString(R.string.label_weight) + ": " + profile.getWeight() + " (" + getString(R.string.label_kg) + ")");

        Helper helper = new Helper(getContext());

        mProfileRelationshipStatus.setText(getString(R.string.account_relationship_status) + ": " + helper.getRelationshipStatus(profile.getRelationshipStatus()));
        mProfilePoliticalViews.setText(getString(R.string.account_political_views) + ": " + helper.getPoliticalViews(profile.getPoliticalViews()));
        mProfileWorldView.setText(getString(R.string.account_world_view) + ": " + helper.getWorldView(profile.getWorldView()));
        mProfilePersonalPriority.setText(getString(R.string.account_personal_priority) + ": " + helper.getPersonalPriority(profile.getPersonalPriority()));
        mProfileImportantInOthers.setText(getString(R.string.account_important_in_others) + ": " + helper.getImportantInOthers(profile.getImportantInOthers()));
        mProfileSmokingViews.setText(getString(R.string.account_smoking_views) + ": " + helper.getSmokingViews(profile.getViewsOnSmoking()));
        mProfileAlcoholViews.setText(getString(R.string.account_alcohol_views) + ": " + helper.getAlcoholViews(profile.getViewsOnAlcohol()));
        mProfileProfileLooking.setText(getString(R.string.account_profile_looking) + ": " + helper.getLooking(profile.getYouLooking()));
        mProfileGenderLike.setText(getString(R.string.account_profile_like) + ": " + helper.getGenderLike(profile.getYouLike()));

        mProfileJoinDate.setText(getString(R.string.label_profile_join) + ": " + profile.getCreateDate());

        if (profile.getAllowShowMyBirthday() == 1) {
            mProfileBirthDate.setVisibility(View.VISIBLE);
            mProfileBirthDateContainer.setVisibility(View.VISIBLE);
            mProfileBirthDate.setText(getString(R.string.label_profile_birth) + ": " + profile.getBirthDate());
        } else {
            mProfileBirthDate.setVisibility(View.GONE);
            mProfileBirthDateContainer.setVisibility(View.GONE);
        }

        if (profile.getAge() == 0) {
            mProfileAgeContainer.setVisibility(View.GONE);
        } else {
            mProfileAgeContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getHeight() == 0) {
            mProfileHeightContainer.setVisibility(View.GONE);
        } else {
            mProfileHeightContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getWeight() == 0) {
            mProfileWeightContainer.setVisibility(View.GONE);
        } else {
            mProfileWeightContainer.setVisibility(View.VISIBLE);
        }

        if (profile.getRelationshipStatus() == 0) {
            mProfileRelationshipStatusContainer.setVisibility(View.GONE);
        }

        if (profile.getPoliticalViews() == 0) {
            mProfilePoliticalViewsContainer.setVisibility(View.GONE);
        }

        if (profile.getWorldView() == 0) {
            mProfileWorldViewContainer.setVisibility(View.GONE);
        }

        if (profile.getPersonalPriority() == 0) {
            mProfilePersonalPriorityContainer.setVisibility(View.GONE);
        }

        if (profile.getImportantInOthers() == 0) {
            mProfileImportantInOthersContainer.setVisibility(View.GONE);
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

        showPhoto(profile.getLowPhotoUrl());

        showContentScreen();

        if (profile.isOnline()) {

            // user Online

            mProfileOnlineIcon.setVisibility(View.VISIBLE);

        } else {

            mProfileOnlineIcon.setVisibility(View.GONE);
        }

        if (profile.isProMode()) {

            mProfileProIcon.setVisibility(View.VISIBLE);

        } else {

            mProfileProIcon.setVisibility(View.GONE);
        }

        // Profile Info

        mProfileInfoContainer.setVisibility(View.GONE);

        if (profile.getAllowShowMyInfo() == 0 || App.getInstance().getId() == profile.getId()) {

            mProfileInfoContainer.setVisibility(View.VISIBLE);

        } else {

            if (profile.getAllowShowMyInfo() == 1 && profile.isFriend()) {

                mProfileInfoContainer.setVisibility(View.VISIBLE);
            }
        }

        if (profile.getAllowShowMyGallery() == 0 || App.getInstance().getId() == profile.getId()) {

            if (profile.getPhotosCount() > 0 && itemsAdapter.getItemCount() != 0) {

                mRecyclerView.setVisibility(View.VISIBLE);

            } else {

                mRecyclerView.setVisibility(View.GONE);
            }

        } else {

            if (profile.getAllowShowMyGallery() == 1 && profile.isFriend()) {

                if (profile.getPhotosCount() > 0 && itemsAdapter.getItemCount() != 0) {

                    mRecyclerView.setVisibility(View.VISIBLE);

                } else {

                    mRecyclerView.setVisibility(View.GONE);
                }

            } else {

                mRecyclerView.setVisibility(View.GONE);
            }
        }

        mProfileCountersContainer.setVisibility(View.VISIBLE);

        if (this.isVisible()) {

            try {

                getActivity().invalidateOptionsMenu();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void updateFullname() {

        if (profile.getFullname() == null || profile.getFullname().length() == 0) {

            mProfileFullname.setText(profile.getUsername());
            if (!isMainScreen) getActivity().setTitle(profile.getUsername());

        } else {

            mProfileFullname.setText(profile.getFullname());
            if (!isMainScreen) getActivity().setTitle(profile.getFullname());
        }
    }

    public void getData() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded() || getActivity() == null) {
                            Log.e("ERROR", "ProfileFragment Not Added to Activity");
                            return;
                        }
                        try {
                            if (!response.getBoolean("error")) {
                                profile = new Profile(response);
                                if (profile.getPhotosCount() > 0) {
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

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
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
        if (App.getInstance().getId() == profile.getId() || profile.getAllowShowMyGallery() == 0) {
            // All right. Load items
        } else {
            if (profile.getAllowShowMyGallery() == 1 && profile.isFriend()) {
                // All right. Load items
            } else {
                return;
            }
        }
        if (loadingMore) {
            mProfileRefreshLayout.setRefreshing(true);
        } else{
            itemId = 0;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {
                            Log.e("ERROR", "ProfileFragment Not Added to Activity");
                            return;
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isAdded() || getActivity() == null) {
                    Log.e("ERROR", "ProfileFragment Not Added to Activity");
                    return;
                }

                loadingComplete();
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
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

    public void loadingComplete() {

        viewMore = arrayLength == LIST_ITEMS;

        itemsAdapter.notifyDataSetChanged();

        mProfileRefreshLayout.setRefreshing(false);

        loadingMore = false;

//        if (this.isVisible()) getActivity().invalidateOptionsMenu();
    }

    public void showLoadingScreen() {

        if (!isMainScreen) getActivity().setTitle(getText(R.string.title_activity_profile));

        mProfileRefreshLayout.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
        mProfileDisabledScreen.setVisibility(View.GONE);
//
        mProfileLoadingScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showErrorScreen() {

        if (!isMainScreen) getActivity().setTitle(getText(R.string.title_activity_profile));

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

        getActivity().setTitle(getText(R.string.label_account_disabled));

        mProfileRefreshLayout.setVisibility(View.GONE);
        mProfileLoadingScreen.setVisibility(View.GONE);
        mProfileErrorScreen.setVisibility(View.GONE);
//
        mProfileDisabledScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showContentScreen() {

        if (!isMainScreen) {

            getActivity().setTitle(profile.getFullname());
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

                menu.removeItem(R.id.action_new_gift);
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

        switch (item.getItemId()) {

            case R.id.action_new_gift: {

                selectGift(profile.getId());

                return true;
            }

            case R.id.action_profile_refresh: {

                mProfileRefreshLayout.setRefreshing(true);
                onRefresh();

                return true;
            }

            case R.id.action_profile_report: {

                profileReport();

                return true;
            }

            case R.id.action_profile_block: {

                profileBlock();

                return true;
            }

            case R.id.action_profile_edit_photo: {

                if (!checkPermission(READ_EXTERNAL_STORAGE)) {

                    requestPermission();

                } else {

                    mAccountAction = 0;

                    choiceImage();
                }

                return true;
            }

            case R.id.action_profile_settings: {

                getAccountSettings();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
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
        FragmentManager fm = getActivity().getSupportFragmentManager();

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

        Api api = new Api(getActivity());

        api.profileReport(profile.getId(), position);
    }

    public void profileBlock() {

        if (!profile.isBlocked()) {

            /** Getting the fragment manager */
            FragmentManager fm = getActivity().getSupportFragmentManager();

            /** Instantiating the DialogFragment class */
            ProfileBlockDialog alert = new ProfileBlockDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putString("blockUsername", profile.getUsername());

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

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ProfileFragment Not Added to Activity");

                                return;
                            }

                            try {

                                if (!response.getBoolean("error")) {

                                    profile.setBlocked(false);

                                    Toast.makeText(getActivity(), getString(R.string.msg_profile_removed_from_blacklist), Toast.LENGTH_SHORT).show();
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

                    if (!isAdded() || getActivity() == null) {

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

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setBlocked(true);

                                Toast.makeText(getActivity(), getString(R.string.msg_profile_added_to_blacklist), Toast.LENGTH_SHORT).show();
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

                if (!isAdded() || getActivity() == null) {

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
                                    profile.setLowPhotoUrl(result.getString("lowPhotoUrl"));
                                    profile.setBigPhotoUrl(result.getString("bigPhotoUrl"));
                                    profile.setNormalPhotoUrl(result.getString("normalPhotoUrl"));
                                    App.getInstance().setPhotoUrl(result.getString("lowPhotoUrl"));
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

    public void removeFromFriends() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FRIENDS_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setFriend(false);
                                profile.setFriendsCount(profile.getFriendsCount() - 1);

                                updateProfile();
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

                if (!isAdded() || getActivity() == null) {

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
                params.put("friendId", Long.toString(profile.getId()));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void friendsRequest() {

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FRIENDS_REQUEST, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile.setFollow(response.getBoolean("follow"));
                                profile.setFollowersCount(response.getInt("followersCount"));

                                updateProfile();

                                changeAccessMode();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                hidepDialog();
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

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void changeAccessMode() {

        if (App.getInstance().getId() == profile.getId() || profile.isFollow()) {

            accessMode = 1;

        } else {

            accessMode = 0;
        }
    }

    private void updateFriendsCount() {
        mProfileFriendsCount.setText(String.valueOf(profile.getFriendsCount()));
    }

    private void updateGiftsCount() {

        mProfileGiftsCount.setText(String.valueOf(profile.getGiftsCount()));
    }

    private void updateLikesCount() {
        mProfileLikesCount.setText(String.valueOf(profile.getLikesCount()));
    }

    public void updateActionButton() {

        if (profile.getId() == App.getInstance().getId()) {

            mProfileActionBtn.setText(R.string.action_profile_edit);
            mProfileActionBtn.setEnabled(true);

        } else {

            if (profile.isFriend()) {

                mProfileActionBtn.setText(R.string.action_remove_from_friends);
                mProfileActionBtn.setEnabled(true);

            } else {

                if (profile.isFollow()) {

                    mProfileActionBtn.setText(R.string.action_cancel_friends_request);
                    mProfileActionBtn.setEnabled(true);

                } else {

                    mProfileActionBtn.setText(R.string.action_add_to_friends);
                    mProfileActionBtn.setEnabled(true);
                }
            }
        }
    }

    public void showProfileGifts(long profileId) {

        if (profile.getAllowShowMyGifts() == 0 || App.getInstance().getId() == profile.getId()) {

            Intent intent = new Intent(getActivity(), GiftsActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            if (profile.getAllowShowMyGifts() == 1 && profile.isFriend()) {

                Intent intent = new Intent(getActivity(), GiftsActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);

            }
        }
    }

    public void showProfileLikes(long profileId) {

        if (profile.getAllowShowMyLikes() == 0 || App.getInstance().getId() == profile.getId()) {

            Intent intent = new Intent(getActivity(), LikesActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            if (profile.getAllowShowMyLikes() == 1 && profile.isFriend()) {

                Intent intent = new Intent(getActivity(), LikesActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);

            }
        }
    }

    public void showProfileFriends(long profileId) {

        if (profile.getAllowShowMyFriends() == 0 || App.getInstance().getId() == profile.getId()) {

            Intent intent = new Intent(getActivity(), FriendsActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);

        } else {

            if (profile.getAllowShowMyFriends() == 1 && profile.isFriend()) {

                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                intent.putExtra("profileId", profileId);
                startActivity(intent);

            }
        }
    }

    public void selectGift(long profileId) {

        if (!profile.isInBlackList()) {

            choiceGiftDialog();

        } else {

            Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
        }
    }

    public void like(final long profileId) {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_LIKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("likesCount")) {

                                    profile.setLikesCount(response.getInt("likesCount"));

                                    updateLikesCount();
                                }

                                if (response.has("myLike")) {

                                    profile.setMyLike(response.getBoolean("myLike"));
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            ((ProfileActivity)getActivity()).mFabButton.hide();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

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
                params.put("profileId", Long.toString(profileId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void choiceGiftDialog() {

        final GiftsSelectListAdapter giftsAdapter;

        giftsAdapter = new GiftsSelectListAdapter(getActivity(), App.getInstance().getGiftsList());

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_gifts);
        dialog.setCancelable(true);

        final ProgressBar mProgressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        TextView mDlgTitle = (TextView) dialog.findViewById(R.id.title_label);
        mDlgTitle.setText(R.string.dlg_choice_gift_title);

        TextView mDlgSubtitle = (TextView) dialog.findViewById(R.id.subtitle_label);
        mDlgSubtitle.setText(String.format(Locale.getDefault(), getString(R.string.account_balance_label), App.getInstance().getBalance()));

        AppCompatButton mDlgBalanceButton = (AppCompatButton) dialog.findViewById(R.id.balance_button);
        mDlgBalanceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), BalanceActivity.class);
                startActivityForResult(i, 1945);

                dialog.dismiss();
            }
        });

        AppCompatButton mDlgCancelButton = (AppCompatButton) dialog.findViewById(R.id.cancel_button);
        mDlgCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        final RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getStickersGridSpanCount(getActivity()));
        mDlgRecyclerView.setLayoutManager(mLayoutManager);
        mDlgRecyclerView.setHasFixedSize(true);
        mDlgRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDlgRecyclerView.setAdapter(giftsAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        giftsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                super.onChanged();

                if (App.getInstance().getGiftsList().size() != 0) {

                    mDlgRecyclerView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        giftsAdapter.setOnItemClickListener(new GiftsSelectListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, BaseGift obj, int position) {

                if (App.getInstance().getBalance() >= obj.getCost()) {

                    Intent intent = new Intent(getActivity(), SendGiftActivity.class);
                    intent.putExtra("giftId", obj.getId());
                    intent.putExtra("giftTo", profile.getId());
                    intent.putExtra("giftCost", obj.getCost());
                    intent.putExtra("imgUrl", obj.getImgUrl());
                    startActivityForResult(intent, PROFILE_NEW_GIFT);

                    dialog.dismiss();

                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_credits), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (App.getInstance().getGiftsList().size() == 0) {

            mDlgRecyclerView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            Api api = new Api(getActivity());
            api.getGifts(giftsAdapter);
        }

        dialog.show();

        doKeepDialog(dialog);
    }


    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        public interface OnItemClickListener {

            void onItemClick(View view, int position);

            void onItemLongClick(View view, int position);
        }

        private FriendsFragment.RecyclerItemClickListener.OnItemClickListener mListener;

        private GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, FriendsFragment.RecyclerItemClickListener.OnItemClickListener listener) {

            mListener = listener;

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (childView != null && mListener != null) {

                        mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {

            View childView = view.findChildViewUnder(e.getX(), e.getY());

            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {

                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public void getAccountSettings() {

        Intent i = new Intent(getActivity(), AccountSettingsActivity.class);
        i.putExtra("profileId", App.getInstance().getId());
        i.putExtra("sex", profile.getSex());
        i.putExtra("year", profile.getYear());
        i.putExtra("month", profile.getMonth());
        i.putExtra("day", profile.getDay());
        i.putExtra("age", profile.getAge());
        i.putExtra("height", profile.getHeight());
        i.putExtra("weight", profile.getWeight());

        i.putExtra("relationshipStatus", profile.getRelationshipStatus());
        i.putExtra("politicalViews", profile.getPoliticalViews());
        i.putExtra("worldView", profile.getWorldView());
        i.putExtra("personalPriority", profile.getPersonalPriority());
        i.putExtra("importantInOthers", profile.getImportantInOthers());
        i.putExtra("viewsOnSmoking", profile.getViewsOnSmoking());
        i.putExtra("viewsOnAlcohol", profile.getViewsOnAlcohol());
        i.putExtra("youLooking", profile.getYouLooking());
        i.putExtra("youLike", profile.getYouLike());

        i.putExtra("allowShowMyBirthday", profile.getAllowShowMyBirthday());

        i.putExtra("fullname", profile.getFullname());
        i.putExtra("location", profile.getLocation());
        i.putExtra("instagramPage", profile.getInstagramPage());
        i.putExtra("bio", profile.getBio());
        startActivityForResult(i, PROFILE_EDIT);
    }

    private boolean checkPermission(String permission) {

        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {

            return true;
        }

        return false;
    }

    private void requestPermission() {

        storagePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE});
    }

    private void requestCameraPermission() {

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
}