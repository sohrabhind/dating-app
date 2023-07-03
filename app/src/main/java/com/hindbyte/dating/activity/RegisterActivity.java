package com.hindbyte.dating.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.hindbyte.dating.R;
import com.hindbyte.dating.tagview.TagContainerLayout;
import com.hindbyte.dating.tagview.TagView;
import com.hindbyte.dating.util.CustomViewPager;
import com.hindbyte.dating.util.ToastWindow;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends ActivityBase {

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    private CustomViewPager mViewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout mMarkersLayout;
    private TextView[] markers;
    private int[] screens;
    private Button mButtonBack, mButtonFinish;
    ToastWindow toastWindow = new ToastWindow();

    //

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;

    private Uri selectedImage;

    private String selectedProfileImagePath = "", newProfileImageFileName = "";
    private String selectedImage1Path = "", newImage1FileName = "";
    private String selectedImage2Path = "", newImage2FileName = "";
    private String selectedImage3Path = "", newImage3FileName = "";
    private String selectedImage4Path = "", newImage4FileName = "";
    private String selectedImage5Path = "", newImage5FileName = "";

    // Google

    // Screen 0

    private EditText mFullname;


    private TextView mButtonGenderMale;
    private TextView mButtonGenderFemale;
    private TextView mButtonGenderOther;

    private TextView mButtonContinue;


    // Screen 1
    private TextView mButtonChooseAge;
    private TextView mTextViewChooseAge;

    // Screen 2
    private CircularImageView mProfilePhoto;

    // Screen 3
    private TagContainerLayout mTagContainerLayout;
    StringBuilder tags;


    // Screen 4
    ImageView image_location;
    private TextView mButtonGrantLocationPermission;

    //

    private int age = 18, gender = -1; // gender: 0 = male; 1 = female; 2 = other
    private String username = "";
    private String password = "";
    private String email = "";
    private String fullname = "";
    private final String photo_url = "";
    private String uid = "";
    private int oauth_type = 0;

    public static String generateRandomChars(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        uid = i.getStringExtra("uid");
        fullname = i.getStringExtra("fullname");
        email = i.getStringExtra("email");

        if (uid == null) {
            uid = "";
            password = i.getStringExtra("password");
            username = i.getStringExtra("username");
        } else {
            password = generateRandomChars("abcdefghijklmnopqrstuvwxyz1234567890", 14);
            username = generateRandomChars("abcdefghijklmnopqrstuvwxyz1234567890_", 10);
        }

        if (email == null) {
            email = "";
            finish();
        }
        if (fullname == null) {
            fullname = "";
        }

        oauth_type = i.getIntExtra("oauth_type", 0);

        setContentView(R.layout.activity_register);
        if (savedInstanceState != null) {
            age = savedInstanceState.getInt("age");
            gender = savedInstanceState.getInt("gender");
            username = savedInstanceState.getString("username");
            password = savedInstanceState.getString("password");
            email = savedInstanceState.getString("email");
            fullname = savedInstanceState.getString("fullname");
            uid = savedInstanceState.getString("uid");
            oauth_type = savedInstanceState.getInt("oauth_type");
            selectedProfileImagePath = savedInstanceState.getString("selectedProfileImagePath");
            newProfileImageFileName = savedInstanceState.getString("newProfileImageFileName");
            selectedImage1Path = savedInstanceState.getString("selectedImage1Path");
            newImage1FileName = savedInstanceState.getString("newImage1FileName");
            selectedImage2Path = savedInstanceState.getString("selectedImage1Path");
            newImage2FileName = savedInstanceState.getString("newImage2FileName");
            selectedImage3Path = savedInstanceState.getString("selectedImage3Path");
            newImage3FileName = savedInstanceState.getString("newImage3FileName");
            selectedImage4Path = savedInstanceState.getString("selectedImage4Path");
            newImage4FileName = savedInstanceState.getString("newImage4FileName");
            selectedImage5Path = savedInstanceState.getString("selectedImage5Path");
            newImage5FileName = savedInstanceState.getString("newImage5FileName");
        }

        //
        multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = true;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet())

                if (!x.getValue()) granted = false;

            if (granted) {

                Log.e("Permissions", "granted");

                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                    mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {

                            if (task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();
                                App.getInstance().setLat(mLastLocation.getLatitude());
                                App.getInstance().setLng(mLastLocation.getLongitude());
                            } else {
                                Log.d("GPS", "getLastLocation:exception", task.getException());
                            }
                        }
                    });
                }

                animateIcon(image_location);

                mButtonGrantLocationPermission.setEnabled(false);
                mButtonGrantLocationPermission.setText(R.string.action_grant_access_success);

            } else {

                Log.e("Permissions", "denied");

                mButtonGrantLocationPermission.setEnabled(true);
                mButtonGrantLocationPermission.setText(R.string.action_grant_access);
            }
        });

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    selectedImage = result.getData().getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    selectedProfileImagePath = cursor.getString(columnIndex);
                    cursor.close();

                    mProfilePhoto.setImageURI(null);
                    mProfilePhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedProfileImagePath)));
                    updateView();
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedProfileImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newProfileImageFileName;

                    mProfilePhoto.setImageURI(null);
                    mProfilePhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), getPackageName() + ".provider", new File(selectedProfileImagePath)));

                    updateView();
                }
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

                Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        toastWindow.makeText(getString(R.string.label_grant_camera_permission), 2000);
                    }

                }).show();
            }
        });

        //

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

                Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);
                        toastWindow.makeText(getString(R.string.label_grant_storage_permission), 2000);
                    }
                }).show();
            }

        });

        //


        mViewPager = findViewById(R.id.view_pager);

        mMarkersLayout = findViewById(R.id.layout_markers);

        mButtonBack = findViewById(R.id.button_back);
        mButtonFinish = findViewById(R.id.button_next);

        screens = new int[]{
                R.layout.register_screen_0,
                R.layout.register_screen_1,
                R.layout.register_screen_2,
                R.layout.register_screen_3,
                R.layout.register_screen_4};

        addMarkers(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        mViewPager.setAdapter(myViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        mViewPager.setPagingEnabled(false);

        mButtonBack.setOnClickListener(v -> {
            if (mViewPager.getCurrentItem() > 0) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                updateView();
            } else {
                finish();
            }
        });

        mButtonFinish.setOnClickListener(v -> {
            int current = mViewPager.getCurrentItem();
            if (current < screens.length - 1) {
                switch (current) {
                    case 1: {
                        if (age > 17) {
                            mViewPager.setCurrentItem(current + 1);
                        } else {
                            toastWindow.makeText(getString(R.string.register_screen_3_msg), 2000);
                        }
                        break;
                    }
                    case 2: {
                        if (selectedProfileImagePath.length() != 0) {
                            mViewPager.setCurrentItem(current + 1);
                        } else {
                            toastWindow.makeText(getString(R.string.register_screen_2_msg), 2000);
                            animateIcon(mProfilePhoto);
                        }
                        break;
                    }
                    case 3: {
                        List<String> selectedPositionsText;
                        selectedPositionsText = mTagContainerLayout.getSelectedTagViewText();
                        tags = new StringBuilder();
                        for (String outer : selectedPositionsText) {
                            tags.append(outer.trim()).append(", ");
                        }
                        mViewPager.setCurrentItem(current + 1);
                        break;
                    }
                    default: {
                        next();
                        break;
                    }
                }
                updateView();
            } else {
                register(tags.toString().replaceAll("^[^A-Za-z0-9]*|[^A-Za-z0-9]*$", ""));
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("age", age);
        outState.putInt("gender", gender);
        outState.putString("username", username);
        outState.putString("password", password);
        outState.putString("email", email);
        outState.putString("fullname", fullname);
        outState.putString("uid", uid);
        outState.putInt("oauth_type", oauth_type);
        outState.putString("selectedProfileImagePath", selectedProfileImagePath);
        outState.putString("newProfileImageFileName", newProfileImageFileName);
    }

    @SuppressLint("SetTextI18n")
    private void updateView() {
        int current = mViewPager.getCurrentItem();
        switch (current) {
            case 0: {
                if (mFullname != null && fullname.length() != 0) {
                    mFullname.setText(fullname);
                }
                if (mButtonGenderMale != null && mButtonGenderFemale != null && mButtonGenderOther != null) {
                    if (gender == 0) {
                        mButtonGenderMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_primaryContainer));
                        mButtonGenderFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_tertiaryContainer));
                        mButtonGenderOther.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_tertiaryContainer));
                    } else if (gender == 1) {
                        mButtonGenderMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_tertiaryContainer));
                        mButtonGenderFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_primaryContainer));
                        mButtonGenderOther.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_tertiaryContainer));
                    } else if (gender == 2) {
                        mButtonGenderMale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_tertiaryContainer));
                        mButtonGenderFemale.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.md_theme_light_tertiaryContainer));
                        mButtonGenderOther.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.md_theme_light_primaryContainer));
                    }
                }
                break;
            }
            case 1: {
                if (age != 0) {
                    mTextViewChooseAge.setText(String.valueOf(age));
                    mButtonChooseAge.setText(getString(R.string.action_choose_age) + ": " + age);
                } else {
                    mButtonChooseAge.setText(getString(R.string.action_choose_age));
                }
                break;
            }

            case 4: {
                if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mButtonGrantLocationPermission.setEnabled(false);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access_success);
                } else {
                    mButtonGrantLocationPermission.setEnabled(true);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access);
                }
                break;
            }

            default: {

                break;
            }
        }
    }

    @Override
    public void onResume() {

        super.onResume();
    }


    private void addMarkers(int currentPage) {

        markers = new TextView[screens.length];
        mMarkersLayout.removeAllViews();
        for (int i = 0; i < markers.length; i++) {
            markers[i] = new TextView(this);
            markers[i].setText(Html.fromHtml("&#8226;"));
            markers[i].setTextSize(35);
            markers[i].setTextColor(getResources().getColor(R.color.white));
            mMarkersLayout.addView(markers[i]);
        }
        if (markers.length > 0)
            markers[currentPage].setTextColor(getResources().getColor(R.color.md_theme_light_primary));
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addMarkers(position);
            if (position == screens.length - 1) {
                mButtonFinish.setText(getString(R.string.action_finish));
            } else {
                mButtonFinish.setText(getString(R.string.action_next));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(screens[position], container, false);
            container.addView(view);

            switch (position) {
                case 0: {
                    mFullname = view.findViewById(R.id.fullname_edit);
                    mButtonGenderMale = view.findViewById(R.id.button_gender_male);
                    mButtonGenderFemale = view.findViewById(R.id.button_gender_female);
                    mButtonGenderOther = view.findViewById(R.id.button_gender_other);

                    mButtonGenderMale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gender = 0;
                            fullname = mFullname.getText().toString();
                            updateView();
                        }
                    });

                    mButtonGenderFemale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gender = 1;
                            fullname = mFullname.getText().toString();
                            updateView();
                        }
                    });

                    mButtonGenderOther.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gender = 2;
                            fullname = mFullname.getText().toString();
                            updateView();
                        }
                    });

                    break;
                }

                case 1: {
                    mTextViewChooseAge = view.findViewById(R.id.textview_age);
                    mButtonChooseAge = view.findViewById(R.id.button_choose_age);

                    mTextViewChooseAge.setOnClickListener(v -> {
                        choiceAge();
                    });
                    mButtonChooseAge.setOnClickListener(v -> choiceAge());
                    break;
                }

                case 2: {
                    mProfilePhoto = view.findViewById(R.id.profile_photo);
                    if (newProfileImageFileName != null && newProfileImageFileName.length() > 0) {
                        mProfilePhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedProfileImagePath)));
                    }
                    mProfilePhoto.setOnClickListener(v -> {
                        if (!checkPermission(READ_EXTERNAL_STORAGE)) {
                            requestPermission();
                        } else {
                            choiceImage();
                        }
                    } );
                    break;
                }

                case 3: {
                    List<String> list1 = new ArrayList<>();
                    list1.add("Music");
                    list1.add("Dancing");
                    list1.add("Fashion");
                    list1.add("Photography");
                    list1.add("Beauty and makeup");
                    list1.add("Movies");
                    list1.add("Pets");
                    list1.add("TV Shows");
                    list1.add("Food");
                    list1.add("Yoga");
                    list1.add("Cooking");
                    list1.add("Outdoor activities");
                    list1.add("Traveling");
                    list1.add("Gardening");
                    list1.add("Fitness and exercise");
                    list1.add("Sports");
                    list1.add("Reading");
                    list1.add("Art");
                    list1.add("Gaming");
                    list1.add("Technology");
                    list1.add("Socializing");
                    list1.add("Writing");
                    list1.add("Blogging");
                    list1.add("Biking");
                    list1.add("Skydiving");
                    list1.add("Philosophy");
                    list1.add("Comedy shows");
                    list1.add("Astronomy");
                    list1.add("History and art");
                    list1.add("Food blogging");
                    list1.add("Fashion blogging");
                    list1.add("Self defense");
                    list1.add("Riding");
                    list1.add("Storytelling");
                    list1.add("Pottery");
                    list1.add("Urban exploration");
                    list1.add("Paragliding");

                    mTagContainerLayout = view.findViewById(R.id.tagcontainerLayout);

                    mTagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {
                        @Override
                        public void onTagClick(int position, String text) {
                            mTagContainerLayout.toggleSelectTagView(position);
                        }

                        @Override
                        public void onTagLongClick(final int position, String text) {
               /*
               List<Integer> selectedPositions = mTagContainerLayout.getSelectedTagViewPositions();
                if (selectedPositions.isEmpty() || selectedPositions.contains(position)) {
                    Toast.makeText(BrowserActivity.this, "click-position:" + position + ", text:" + text, Toast.LENGTH_SHORT).show();
                }
                */
                        }

                        @Override
                        public void onSelectedTagDrag(int position, String text) {
                        }

                        @Override
                        public void onTagCrossClick(int position) {
                        }
                    });

                    mTagContainerLayout.setTags(list1);
                    break;
                }


                case 4: {
                    image_location = view.findViewById(R.id.image_location);
                    mButtonGrantLocationPermission = view.findViewById(R.id.button_grant_location_permission);

                    mButtonGrantLocationPermission.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            grantLocationPermission();
                        }
                    });
                    break;
                }
            }

            updateView();
            return view;
        }

        @Override
        public int getCount() {

            return screens.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            View view = (View) object;
            container.removeView(view);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            updateView();
        } else {
            finish();
        }
    }


    private void animateIcon(ImageView icon) {
        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(175);
        scale.setInterpolator(new LinearInterpolator());
        icon.startAnimation(scale);
    }

    private void choiceImage() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    imgFromGalleryActivityResultLauncher.launch(intent);
                } else {
                    if (checkPermission(Manifest.permission.CAMERA)) {
                        try {
                            newProfileImageFileName = Helper.randomString(6) + ".jpg";
                            selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newProfileImageFileName));
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            imgFromCameraActivityResultLauncher.launch(cameraIntent);
                        } catch (Exception e) {
                            toastWindow.makeText("Error occured. Please try again later.", 2000);
                        }
                    } else {
                        requestCameraPermission();
                    }
                }

            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceAge() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        for (int i = 18; i < 101; i++) {

            arrayAdapter.add(String.valueOf(i));
        }

        builderSingle.setTitle(getText(R.string.register_screen_3_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                age = which + 18;

                updateView();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }


    private void grantLocationPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
            } else {
                multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }
    }

    private void next() {
        if (mViewPager.getCurrentItem() != 0 && verifyRegForm()) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            updateView();
        } else if (mViewPager.getCurrentItem() == 0 && check_fullname()) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            updateView();
        }
    }


    public Boolean verifyRegForm() {
        Helper helper = new Helper(this);
        if (username.length() == 0) {
            return false;
        }

        if (username.length() < 5) {
            return false;
        }

        if (!helper.isValidLogin(username)) {
            return false;
        }

        if (password.length() == 0) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        if (!helper.isValidPassword(password)) {
            return false;
        }

        if (email.length() == 0) {
            return false;
        }

        if (!helper.isValidEmail(email)) {
            return false;
        }
        return true;
    }

    private void go() {
        hidepDialog();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }



    public Boolean check_fullname() {
        fullname = mFullname.getText().toString();
        if (fullname.length() == 0) {
            toastWindow.makeText(getString(R.string.error_field_empty), 2000);
            return false;
        }

        if (fullname.length() < 2) {
            toastWindow.makeText(getString(R.string.error_small_fullname), 2000);
            return false;
        }

        if (gender < 0 || gender > 2) {
            toastWindow.makeText(getString(R.string.error_gender), 2000);
            return false;
        }
        return  true;
    }


    public void register(String  interests) {
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SIGNUP, null, response -> {
            Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");
            if (App.getInstance().authorize(response)) {
                Log.e("Profile", "Malformed JSON: \"" + response + "\"");

                // Upload profile photo
                        File profileImageFile = new File(selectedProfileImagePath);
                        final OkHttpClient client = new OkHttpClient();
                        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
                        
                        try {
                            RequestBody requestBody = new MultipartBuilder()
                                    .type(MultipartBuilder.FORM)
                                    .addFormDataPart("uploaded_file", profileImageFile.getName(), RequestBody.create(MediaType.parse("text/csv"), profileImageFile))
                                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                                    .addFormDataPart("imgType", String.valueOf(UPLOAD_TYPE_PHOTO))
                                    .build();

                            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                                    .url(METHOD_ACCOUNT_UPLOAD_IMAGE)
                                    .addHeader("Accept", "application/json;")
                                    .post(requestBody)
                                    .build();

                            client.newCall(request).enqueue(new Callback() {

                                @Override
                                public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                                    go();

                                    Log.e("failure", request.toString());
                                }

                                @Override
                                public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                                    String jsonData = response.body().string();

                                    Log.e("response", jsonData);

                                    try {

                                        JSONObject result = new JSONObject(jsonData);

                                        if (!result.getBoolean("error")) {

                                            if (result.has("bigPhotoUrl")) {

                                                App.getInstance().setPhotoUrl(result.getString("bigPhotoUrl"));
                                            }

                                        }

                                        Log.d("My App", response.toString());

                                    } catch (Throwable t) {

                                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                                    } finally {

                                        go();
                                    }
                                }
                            });

                        } catch (Exception ex) {
                            // Handle the error

                            go();
                        }

                    } else {
                        hidepDialog();
                        switch (App.getInstance().getErrorCode()) {
                            case 300: {
                                mViewPager.setCurrentItem(0);
                                username = generateRandomChars("abcdefghijklmnopqrstuvwxyz1234567890_", 10);
                                register(interests);
                                //toastWindow.makeText(getString(R.string.error_login_taken), 2000);
                                break;
                            }

                            case 301: {
                                mViewPager.setCurrentItem(0);
                                toastWindow.makeText(getString(R.string.error_email_taken), 2000);
                                break;
                            }

                            case 500: {
                                toastWindow.makeText(getString(R.string.label_multi_account_msg), 2000);
                                break;
                            }

                            default: {
                                Log.e("Profile", "Could not parse malformed JSON: \"" + response.toString() + "\"");
                                break;
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("signup()", error.toString());

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("fullname", fullname);
                params.put("password", password);
                params.put("photo", photo_url);
                params.put("email", email);
                params.put("uid", uid);
                params.put("interests", interests);
                params.put("oauth_type", String.valueOf(oauth_type));
                params.put("gender", String.valueOf(gender));
                params.put("age", String.valueOf(age));
                params.put("country", getCountryZipCode(getDeviceCountryCode(getBaseContext()), getBaseContext()));
                params.put("appType", String.valueOf(APP_TYPE_ANDROID));
                params.put("fcm_regId", App.getInstance().getGcmToken());
                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
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

    private boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
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
