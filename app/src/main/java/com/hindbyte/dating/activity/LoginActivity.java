package com.hindbyte.dating.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends ActivityBase {

    private ProgressDialog pDialog;

    TextView mForgotPassword;
    TextView signInBtn;
    EditText signInEmail, signInPassword;
    String email = "", password = "";

    private Boolean loading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

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


        initpDialog();
        if (loading) {
            showpDialog();
        }

        signInEmail = findViewById(R.id.signInEmail);
        signInPassword = findViewById(R.id.signInPassword);
        mForgotPassword = findViewById(R.id.forgotPassword);
        mForgotPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RecoveryActivity.class);
                startActivity(i);
            }
        });

        signInBtn = findViewById(R.id.signInBtn);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = signInEmail.getText().toString();
                password = signInPassword.getText().toString();
                if (!App.getInstance().isConnected()) {
                    Toast.makeText(LoginActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
                } else if (checkUsername() && checkPassword()) {
                    signIn();
                }
            }
        });
    }

    @Override
    protected void  onStart() {
        super.onStart();
    }


    public void onDestroyView() {
        hidepDialog();
    }

    protected void initpDialog() {
        pDialog = new ProgressDialog(LoginActivity.this);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    public void signIn() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGIN, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                App.getInstance().updateGeoLocation();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {

                                if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                    App.getInstance().logout();
                                    Toast.makeText(LoginActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                } else if (App.getInstance().getState() == ACCOUNT_STATE_DEACTIVATED) {

                                    App.getInstance().logout();
                                    Toast.makeText(LoginActivity.this, getText(R.string.msg_account_deactivated), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {

                            Toast.makeText(LoginActivity.this, getString(R.string.error_signin), Toast.LENGTH_SHORT).show();

                            Log.e("response", response.toString());
                        }

                        loading = false;

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", error.toString());
                Toast.makeText(LoginActivity.this, getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
                loading = false;
                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("clientId", CLIENT_ID);
                params.put("hash", Helper.md5(Helper.md5(email) + CLIENT_SECRET));
                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("fcm_regId", App.getInstance().getGcmToken());
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean checkUsername() {
        email = signInEmail.getText().toString();
        Helper helper = new Helper(LoginActivity.this);
        if (email.length() == 0) {
            Toast.makeText(LoginActivity.this, getString(R.string.error_field_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (email.length() < 5) {
            Toast.makeText(LoginActivity.this, getString(R.string.error_small_username), Toast.LENGTH_LONG).show();
            return false;
        }

        if (!helper.isValidLogin(email) && !helper.isValidEmail(email)) {
            Toast.makeText(LoginActivity.this, getString(R.string.error_wrong_format), Toast.LENGTH_LONG).show();
            return false;
        }
        return  true;
    }

    public Boolean checkPassword() {
        password = signInPassword.getText().toString();
        Helper helper = new Helper(LoginActivity.this);
        if (password.length() == 0) {
            Toast.makeText(LoginActivity.this, getString(R.string.error_field_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(LoginActivity.this, getString(R.string.error_small_password), Toast.LENGTH_LONG).show();
            return false;
        }

        if (!helper.isValidPassword(password)) {
            Toast.makeText(LoginActivity.this, getString(R.string.error_wrong_format), Toast.LENGTH_LONG).show();
            return false;
        }
        return  true;
    }

}