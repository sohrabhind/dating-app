package com.hindbyte.dating.fragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddPhotoFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    private EditText mTextEditor;
    private ImageView mThumbnail, mActionIcon;
    private ProgressBar mProgressView;
    private ImageButton mDeleteButton;
    private Button mPublishButton;

    private SwitchCompat mModeSwitch;

    String commentText = "", imgUrl = "",  postArea = "", postCountry = "", postCity = "", postLat = "", postLng = "";

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "", newThumbFileName = "";

    private int postMode = 0;
    private int itemType = 0;
    private int itemShowInStream = 1;

    private Boolean loading = false;

    //

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;

    public AddPhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_photo, container, false);
        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

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

                        mThumbnail.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                        itemType = GALLERY_ITEM_TYPE_IMAGE;

                        updateView();
                    }
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                    mThumbnail.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                    itemType = GALLERY_ITEM_TYPE_IMAGE;

                    updateView();
                }
            }
        });

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

        if (loading) {

            showpDialog();
        }

        mTextEditor = rootView.findViewById(R.id.text_editor);
        mActionIcon = rootView.findViewById(R.id.action_add);

        //


        mThumbnail = rootView.findViewById(R.id.thumbnail);

        mThumbnail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (selectedImagePath.length() == 0) {

                    if (!checkPermission(READ_EXTERNAL_STORAGE)) {

                        requestPermission();

                    } else {

                        choiceImage();
                    }
                }
            }
        });

        //

        mProgressView = rootView.findViewById(R.id.progress_view);
        mProgressView.setVisibility(View.GONE);

        //

        mDeleteButton = rootView.findViewById(R.id.delete_button);
        mDeleteButton.setVisibility(View.GONE);

        mDeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(getText(R.string.action_remove));

                alertDialog.setMessage(getText(R.string.label_delete_item));
                alertDialog.setCancelable(true);

                alertDialog.setNeutralButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.setPositiveButton(getText(R.string.action_remove), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        mThumbnail.setImageURI(null);

                        selectedImagePath = "";
                        newImageFileName = "";
                        newThumbFileName = "";

                        mDeleteButton.setVisibility(View.GONE);
                        mActionIcon.setVisibility(View.VISIBLE);
                        mActionIcon.setVisibility(View.VISIBLE);

                        updateView();

                        dialog.cancel();

                        itemType = GALLERY_ITEM_TYPE_IMAGE;
                    }
                });

                alertDialog.show();
            }
        });

        //

        mPublishButton = rootView.findViewById(R.id.publish_button);

        mPublishButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().isConnected()) {

                    commentText = mTextEditor.getText().toString();
                    commentText = commentText.trim();

                    itemShowInStream = 1;

                    if (!mModeSwitch.isChecked()) {

                        itemShowInStream = 0;
                    }

                    if (selectedImagePath != null && selectedImagePath.length() > 0) {

                        loading = true;

                        showpDialog();

                        if (itemType == GALLERY_ITEM_TYPE_IMAGE) {
                            File f = new File(selectedImagePath);
                            uploadFile(METHOD_GALLERY_UPLOAD_IMG, f);
                        }
                    } else {

                        Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_enter_photo), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        //

        mModeSwitch = rootView.findViewById(R.id.mode_switch);

        mModeSwitch.setOnCheckedChangeListener(null);

        mModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    itemShowInStream = 1;

                } else {

                    itemShowInStream = 0;
                }
            }
        });

        //

        mTextEditor.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int cnt = s.length();

                if (cnt == 0) {

                    getActivity().setTitle(getText(R.string.title_activity_new_photo));

                } else {

                    getActivity().setTitle(String.valueOf(140 - cnt));
                }
            }

        });

        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateView() {

        mDeleteButton.setVisibility(View.GONE);
        mActionIcon.setVisibility(View.VISIBLE);
        mActionIcon.setImageResource(R.drawable.ic_plus);

        if (itemShowInStream == 1) {

            mModeSwitch.setChecked(true);

        } else {

            mModeSwitch.setChecked(false);
        }

        if (selectedImagePath != null && selectedImagePath.length() > 0) {

            mActionIcon.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);

            if (itemType == GALLERY_ITEM_TYPE_IMAGE) {

                mThumbnail.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

            } else{

                mThumbnail.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newThumbFileName)));
            }

            if (newThumbFileName.length() != 0) {

                mActionIcon.setImageResource(R.drawable.ic_play);
                mActionIcon.setVisibility(View.VISIBLE);
            }

        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
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
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void choiceImage() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));
        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            switch (which) {
                case 0: {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    imgFromGalleryActivityResultLauncher.launch(intent);
                    break;
                }
                case 1: {
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
                            Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        requestCameraPermission();
                    }
                    break;
                }
            }

        });

        /** Creating the alert dialog window using the builder class */
        AlertDialog d = builderSingle.create();

        /** Return the alert dialog window */
        d.show();
    }

    public void sendPost() {

        imgUrl = imgUrl.replace("../","");

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            sendSuccess();

                            Log.e("Result", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendSuccess();

//                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("accessMode", String.valueOf(postMode));
                params.put("itemType", String.valueOf(itemType));
                params.put("itemShowInStream", String.valueOf(itemShowInStream));
                params.put("comment", commentText);
                params.put("imgUrl", imgUrl);
                params.put("postArea", postArea);
                params.put("postCountry", postCountry);
                params.put("postCity", postCity);
                params.put("postLat", postLat);
                params.put("postLng", postLng);

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendSuccess() {
        loading = false;
        hidepDialog();
        Intent i = new Intent();
        requireActivity().setResult(RESULT_OK, i);
        requireActivity().finish();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    public Boolean uploadFile(String serverURL, File file) {
        final OkHttpClient client = new OkHttpClient();
        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        try {
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    loading = false;
                    hidepDialog();
                    Log.e("failure", request.toString() + "|" + e.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);
                    try {
                        JSONObject result = new JSONObject(jsonData);
                        if (!result.getBoolean("error")) {
                            imgUrl = result.getString("normalPhotoUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        sendPost();
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