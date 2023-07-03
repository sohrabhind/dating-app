package com.hindbyte.dating.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hindbyte.dating.R;
import com.hindbyte.dating.animation.Attention;
import com.hindbyte.dating.animation.Bounce;
import com.hindbyte.dating.animation.Rotate;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
import com.hindbyte.dating.animation.Render;
import com.hindbyte.dating.animation.Slide;
import com.hindbyte.dating.util.ToastWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends ActivityBase {

    protected Location mLastLocation;

    ToastWindow toastWindow = new ToastWindow();
    Button signupBtn;
    Button mGoogleSignInButton;

    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    private ProgressDialog pDialog;

    String uid = "";
    private int oauth_type = 0;

    private Boolean loading = false;


    private EditText mUsername, mFullname, mPassword, mEmail;
    private String username = "";
    private String password = "";
    private String email = "";
    private String fullname = "";

    private boolean isEmailTaken = false;

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
        setContentView(R.layout.activity_home);
        // Get Firebase token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();
            App.getInstance().setGcmToken(token);
            Log.d("FCM Token", token);
        });


        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult();
                    // Set geo data to App class
                    App.getInstance().setLat(mLastLocation.getLatitude());
                    App.getInstance().setLng(mLastLocation.getLongitude());
                } else {
                    Log.d("GPS", "AppActivity getLastLocation:exception", task.getException());
                }
            });
        }


        signupBtn = findViewById(R.id.signupBtn);

        mUsername = findViewById(R.id.username_edit);
        mPassword = findViewById(R.id.password_edit);
        mEmail = findViewById(R.id.email_edit);


        TextView mButtonTerms = findViewById(R.id.button_terms);

        mButtonTerms.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, WebViewActivity.class);
            i.putExtra("url", METHOD_APP_TERMS);
            i.putExtra("title", getText(R.string.signup_label_terms_and_policies));
            startActivity(i);
        });


        mEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (App.getInstance().isConnected() && check_email()) {
                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECK_EMAIL, null,
                            response -> {
                                try {
                                    if (response.getBoolean("error")) {
                                        isEmailTaken = true;
                                    } else {
                                        isEmailTaken = false;
                                    }
                                } catch (JSONException e) {
                                    isEmailTaken = true;
                                }
                            }, error -> {
                        isEmailTaken = true;
                        Log.e("Email()", error.toString());
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("email", email);
                            return params;
                        }
                    };
                    App.getInstance().addToRequestQueue(jsonReq);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        Button loginBtn = findViewById(R.id.loginBtn);


        loginBtn.setOnClickListener(view -> {
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(i);
        });


        signupBtn.setOnClickListener(view -> {
            hideKeyboard();
            username = generateRandomChars("abcdefghijklmnopqrstuvwxyz", 32);
            mUsername.setText(username);
            if (verifyRegForm()) {
                Intent i = new Intent(HomeActivity.this, RegisterActivity.class);
                i.putExtra("email", email);
                i.putExtra("username", username);
                i.putExtra("password", password);
                startActivity(i);
            }


            Render renderEmail = new Render();
            renderEmail.setDuration(1000);
            renderEmail.setAnimation(Attention.Shake(mEmail));
            renderEmail.start();

            Render renderPassword = new Render();
            renderPassword.setDuration(1000);
            renderPassword.setAnimation(Attention.Shake(mPassword));
            renderPassword.start();
        });


        LinearLayout bounceLinearLayout = findViewById(R.id.bounceLinearLayout);
        Render renderScrollView= new Render();
        renderScrollView.setDuration(1000);
        renderScrollView.setAnimation(Bounce.InRight(bounceLinearLayout));
        renderScrollView.start();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();

        googleSigninActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    showpDialog();
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    uid = account.getId();
                    fullname = account.getDisplayName();
                    email = account.getEmail();
                    oauth_type = OAUTH_TYPE_GOOGLE;

                    // Signed in successfully, show authenticated UI.
                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GOOGLE_AUTH, null,
                            response -> {
                                if (App.getInstance().authorize(response)) {
                                    if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {
                                        App.getInstance().updateGeoLocation();
                                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {
                                            App.getInstance().logout();
                                            toastWindow.makeText(getText(R.string.msg_account_blocked), 2000);
                                        } else {
                                            App.getInstance().updateGeoLocation();
                                            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    }
                                } else {
                                    if (uid.length() != 0) {
                                        Intent i = new Intent(HomeActivity.this, RegisterActivity.class);
                                        i.putExtra("uid", uid);
                                        i.putExtra("fullname", fullname);
                                        i.putExtra("email", email);
                                        i.putExtra("oauth_type", oauth_type);
                                        startActivity(i);
                                    } else {
                                        toastWindow.makeText(getString(R.string.error_signin), 2000);
                                    }
                                }

                                loading = false;
                                hidepDialog();
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Google", "signInWithCredential:failure");
                            toastWindow.makeText(getText(R.string.error_data_loading), 2000);
                            loading = false;
                            hidepDialog();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("uid", uid);
                            params.put("email", email);
                            params.put("app_type", String.valueOf(APP_TYPE_ANDROID));
                            params.put("fcm_regId", App.getInstance().getGcmToken());
                            return params;
                        }
                    };
                    App.getInstance().addToRequestQueue(jsonReq);
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    // Please refer to the GoogleSignInStatusCodes class reference for more information.
                    Log.e("Google", "Google sign in failed", e);
                }
            }
        });

        initpDialog();
        if (loading) {
            showpDialog();
        }

        // Google Button

        mGoogleSignInButton = findViewById(R.id.google_sign_in_button);

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSigninActivityResultLauncher.launch(signInIntent);
            }
        });

        // Facebook button
        if (!GOOGLE_AUTHORIZATION) {
            mGoogleSignInButton.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {
            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_AUTHORIZE, null,
                    response -> {
                        if (App.getInstance().authorize(response)) {
                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {
                                App.getInstance().updateGeoLocation();
                                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                App.getInstance().logout();
                            }
                        }
                        Log.e("Auth", response.toString());
                    }, error -> {
                        Log.e("Auth", "Error");
                    }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("appType", String.valueOf(APP_TYPE_ANDROID));
                    params.put("fcm_regId", App.getInstance().getGcmToken());
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    return params;
                }
            };
            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonReq.setRetryPolicy(policy);
            App.getInstance().addToRequestQueue(jsonReq);
        }
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public Boolean check_email() {
        email = mEmail.getText().toString();
        Helper helper = new Helper(this);
        if (email.length() == 0) {
            return false;
        }

        if (!helper.isValidEmail(email)) {
            return false;
        }
        return true;
    }

    public Boolean verifyRegForm() {
        email = mEmail.getText().toString();
        username = mUsername.getText().toString();
        password = mPassword.getText().toString();

        Helper helper = new Helper(this);

        if(isEmailTaken) {
            toastWindow.makeText(R.string.error_email_taken, 2000);
            return false;
        }

        if (email.length() == 0) {
            toastWindow.makeText(R.string.error_field_empty, 2000);
            return false;
        }

        if (!helper.isValidEmail(email)) {
            toastWindow.makeText(R.string.error_wrong_format, 2000);
            return false;
        }


        if (username.length() == 0) {
            toastWindow.makeText(R.string.error_field_empty, 2000);
            return false;
        }

        if (username.length() < 5) {
            toastWindow.makeText(R.string.error_small_username, 2000);
            return false;
        }

        if (!helper.isValidLogin(username)) {
            toastWindow.makeText(R.string.error_wrong_format, 2000);
            return false;
        }



        if (password.length() == 0) {
            toastWindow.makeText(R.string.error_field_empty, 2000);
            return false;
        }

        if (password.length() < 6) {
            toastWindow.makeText(R.string.error_small_password, 2000);
            return false;
        }

        if (!helper.isValidPassword(password)) {
            toastWindow.makeText(R.string.error_wrong_format, 2000);
            return false;
        }

        return true;
    }


    public void onDestroyView() {
        hidepDialog();
    }

    protected void initpDialog() {
        pDialog = new ProgressDialog(HomeActivity.this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}