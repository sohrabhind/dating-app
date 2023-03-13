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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import android.widget.Toast;

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
import com.hindbyte.dating.BuildConfig;
import com.hindbyte.dating.R;
import com.hindbyte.dating.util.CustomViewPager;
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
import java.util.Arrays;
import java.util.HashMap;
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

    //

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    // Google

    // Screen 0

    private EditText mFullname;

    private TextView mButtonContinue;

    // Screen 1

    private TextView mButtonChoosePhoto;
    private CircularImageView mPhoto;

    // Screen 2

    private TextView mButtonChooseAge;

    // Screen 3

    private TextView mButtonChooseGender;


    // Screen 3, 4 and 5

    private ImageView mImage;

    // Screen 4

    private TextView mButtonGrantLocationPermission;

    //

    private int age = 0, gender = 2; // gender: 0 - male; 1 = female; 2 = secret
    private String username = "";
    private String password = "";
    private String email = "";
    private final String language = "en";
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
            username = generateRandomChars("abcdefghijklmnopqrstuvwxyz", 32);
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
            selectedImagePath = savedInstanceState.getString("selectedImagePath");
            newImageFileName = savedInstanceState.getString("newImageFileName");
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

                animateIcon(mImage);

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
                    selectedImagePath = cursor.getString(columnIndex);
                    cursor.close();

                    mPhoto.setImageURI(null);
                    mPhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));
                    updateView();
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                    mPhoto.setImageURI(null);
                    mPhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedImagePath)));

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

                        Toast.makeText(RegisterActivity.this, getString(R.string.label_grant_camera_permission), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(RegisterActivity.this, getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_3_msg), Toast.LENGTH_SHORT).show();
                            animateIcon(mImage);
                        }

                        break;
                    }
                    case 2: {
                        if (selectedImagePath.length() != 0) {
                            mViewPager.setCurrentItem(current + 1);
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_2_msg), Toast.LENGTH_SHORT).show();
                            animateIcon(mPhoto);
                        }
                        break;
                    }
                    default: {
                        next();
                        break;
                    }
                }
                updateView();
            } else {
                register();
            }
        });
    }



    private void exitSignUp(){
        new AlertDialog.Builder(this)
                .setTitle("Signup")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
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
        outState.putString("selectedImagePath", selectedImagePath);
        outState.putString("newImageFileName", newImageFileName);
    }

    @SuppressLint("SetTextI18n")
    private void updateView() {
        int current = mViewPager.getCurrentItem();
        switch (current) {
            case 0: {
                if (mFullname != null && fullname.length() != 0) {
                    mFullname.setText(fullname);
                }
                if (mButtonChooseGender != null) {
                    mButtonChooseGender.setText(Helper.getGenderTitle(this, gender));
                }
                break;
            }
            case 1: {
                if (age != 0) {
                    mButtonChooseAge.setText(getString(R.string.action_choose_age) + ": " + age);
                } else {
                    mButtonChooseAge.setText(getString(R.string.action_choose_age));
                }
                break;
            }


            case 3: {
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
            markers[i].setTextColor(getResources().getColor(R.color.overlay_light_30));
            mMarkersLayout.addView(markers[i]);
        }

        if (markers.length > 0)

            markers[currentPage].setTextColor(getResources().getColor(R.color.white));
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
                    ImageView genderImage = view.findViewById(R.id.gender_image);
                    mButtonChooseGender = view.findViewById(R.id.button_choose_gender);
                    genderImage.setOnClickListener(v -> {
                        choiceGender();
                    });
                    mButtonChooseGender.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            choiceGender();
                        }
                    });

                    mFullname.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            check_fullname();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    break;
                }


                case 1: {
                    mImage = view.findViewById(R.id.age_image);
                    mButtonChooseAge = view.findViewById(R.id.button_choose_age);

                    mImage.setOnClickListener(v -> {
                        choiceAge();
                    });
                    mButtonChooseAge.setOnClickListener(v -> choiceAge());
                    break;
                }

                case 2: {
                    mPhoto = view.findViewById(R.id.photo_image);
                    if (newImageFileName != null && newImageFileName.length() > 0) {
                        mPhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));
                    }
                    mButtonChoosePhoto = view.findViewById(R.id.button_choose_photo);
                    mPhoto.setOnClickListener(v -> {
                        if (!checkPermission(READ_EXTERNAL_STORAGE)) {
                            requestPermission();
                        } else {
                            choiceImage();
                        }
                    } );
                    mButtonChoosePhoto.setOnClickListener(v -> {
                        if (!checkPermission(READ_EXTERNAL_STORAGE)) {
                            requestPermission();
                        } else {
                            choiceImage();
                        }
                    });
                    break;
                }


                case 3: {

                    mImage = view.findViewById(R.id.image);
                    mButtonGrantLocationPermission = view.findViewById(R.id.button_grant_location_permission);

                    mButtonGrantLocationPermission.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            grantLocationPermission();
                        }
                    });
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
                            newImageFileName = Helper.randomString(6) + ".jpg";
                            selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName));
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            imgFromCameraActivityResultLauncher.launch(cameraIntent);
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
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

            arrayAdapter.add(Integer.toString(i));
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

    private void choiceGender() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.label_male));
        arrayAdapter.add(getString(R.string.label_female));

        builderSingle.setTitle(getText(R.string.action_choose_gender));

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            gender = which;
            updateView();
        });

        builderSingle.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

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
            if (gender == 2) {
                Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_4_msg), Toast.LENGTH_SHORT).show();
            } else {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                updateView();
            }
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

            mFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            mFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        mFullname.setError(null);

        return  true;
    }


    public void register() {
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SIGNUP, null, response -> {
            Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");
            if (App.getInstance().authorize(response)) {
                Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                // Upload profile photo

                        File file = new File(selectedImagePath);

                        final OkHttpClient client = new OkHttpClient();

                        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

                        try {

                            RequestBody requestBody = new MultipartBuilder()
                                    .type(MultipartBuilder.FORM)
                                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                                    .addFormDataPart("imgType", Integer.toString(UPLOAD_TYPE_PHOTO))
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

                                            if (result.has("lowPhotoUrl")) {

                                                App.getInstance().setPhotoUrl(result.getString("lowPhotoUrl"));
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

                            case ERROR_CLIENT_ID: {

                                Toast.makeText(RegisterActivity.this, getString(R.string.error_client_id), Toast.LENGTH_SHORT).show();
                            }

                            case ERROR_CLIENT_SECRET: {

                                Toast.makeText(RegisterActivity.this, getString(R.string.error_client_secret), Toast.LENGTH_SHORT).show();
                            }

                            case 300: {

                                mViewPager.setCurrentItem(0);

                                Toast.makeText(RegisterActivity.this, getString(R.string.error_login_taken), Toast.LENGTH_SHORT).show();

                                break;
                            }

                            case 301: {
                                mViewPager.setCurrentItem(0);
                                Toast.makeText(RegisterActivity.this, getString(R.string.error_email_taken), Toast.LENGTH_SHORT).show();
                                break;
                            }

                            case 500: {
                                Toast.makeText(RegisterActivity.this, getString(R.string.label_multi_account_msg), Toast.LENGTH_SHORT).show();
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
                params.put("language", language);
                params.put("uid", uid);
                params.put("oauth_type", Integer.toString(oauth_type));
                params.put("sex", Integer.toString(gender));
                params.put("age", Integer.toString(age));
                params.put("clientId", CLIENT_ID);
                params.put("hash", Helper.md5(Helper.md5(username) + CLIENT_SECRET));
                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("fcm_regId", App.getInstance().getGcmToken());
                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
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
