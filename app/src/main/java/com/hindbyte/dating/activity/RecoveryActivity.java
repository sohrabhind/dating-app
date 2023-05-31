package com.hindbyte.dating.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
import com.hindbyte.dating.util.ToastWindow;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RecoveryActivity extends ActivityBase {

    private static final String TAG = "password_recovery_activity";

    private ProgressDialog pDialog;

    TextView mContinueBtn;
    EditText mEmail;
    String email;

    private Boolean loading = false;

    ToastWindow toastWindow = new ToastWindow();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);
        initpDialog();

        if (loading) {
            showpDialog();
        }

        mEmail = findViewById(R.id.recoveryEmail);
        mContinueBtn = findViewById(R.id.recoveryBtn);
        mContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                if (!App.getInstance().isConnected()) {
                    toastWindow.makeText(RecoveryActivity.this, R.string.msg_network_error, 2000);
                } else {
                    Helper helper = new Helper(RecoveryActivity.this);
                    if (helper.isValidEmail(email)) {
                        recovery();
                    } else {
                        toastWindow.makeText(RecoveryActivity.this, getText(R.string.error_email), 2000);
                    }
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    protected void initpDialog() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    @Override
    public void onBackPressed(){
        hidepDialog();
        finish();
    }


    protected void showpDialog() {
        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {
        if (pDialog.isShowing()) pDialog.dismiss();
    }



    public void recovery() {
        loading = true;
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_RECOVERY, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            toastWindow.makeText(RecoveryActivity.this, getText(R.string.msg_password_reset_link_sent), 2000);
                            finish();
                        } else {
                            toastWindow.makeText(RecoveryActivity.this, getText(R.string.msg_no_such_user_in_bd), 2000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        hidepDialog();
                    }
                }, error -> {
                    loading = false;
                    hidepDialog();
                    toastWindow.makeText(RecoveryActivity.this, getText(R.string.error_data_loading), 2000);
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
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
}