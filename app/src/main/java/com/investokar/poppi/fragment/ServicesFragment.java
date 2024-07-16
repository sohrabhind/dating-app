package com.investokar.poppi.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.util.CustomRequest;
import com.investokar.poppi.util.ToastWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ServicesFragment extends Fragment implements Constants {



    private ProgressDialog pDialog;

    CardView mGoogleCard;
    TextView mGooglePrompt;

    TextView mGoogleDisconnectBtn;
    TextView mGoogleSignInButton;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    String uid = "";

    private Boolean loading = false;

    public ServicesFragment() {
        // Required empty public constructor
    }

    ToastWindow toastWindow = new ToastWindow();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleSigninActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // There are no request codes
                    Intent data = result.getData();

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {

                        GoogleSignInAccount account = task.getResult(ApiException.class);

                        uid = account.getId();

                        // Signed in successfully, show authenticated UI.

                        googleOauth("connect");

                    } catch (ApiException e) {

                        // The ApiException status code indicates the detailed failure reason.
                        // Please refer to the GoogleSignInStatusCodes class reference for more information.
                        Log.e("Google", "Google sign in failed", e);
                    }
                }
            }
        });

        //

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_services, container, false);

        if (loading) {

            showpDialog();
        }


        mGoogleCard = rootView.findViewById(R.id.google_card);

        mGooglePrompt = rootView.findViewById(R.id.google_sub_label);
        mGoogleDisconnectBtn = rootView.findViewById(R.id.google_disconnect_button);

        mGoogleDisconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut();
                googleOauth("disconnect");
            }
        });

        // Google Button

        mGoogleSignInButton = rootView.findViewById(R.id.google_sign_in_button);

        mGoogleSignInButton.setOnClickListener(v -> {
            mGoogleSignInClient.signOut();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSigninActivityResultLauncher.launch(signInIntent);
        });


        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateView() {

        if (GOOGLE_AUTHORIZATION) {

            mGoogleCard.setVisibility(View.VISIBLE);

            if (App.getInstance().getGoogleFirebaseId().length() > 0) {

                mGoogleSignInButton.setVisibility(View.GONE);
                mGoogleDisconnectBtn.setVisibility(View.VISIBLE);
                mGooglePrompt.setText(getString(R.string.label_account_connected_to_google));

            } else {

                mGoogleSignInButton.setVisibility(View.VISIBLE);
                mGoogleDisconnectBtn.setVisibility(View.GONE);
                mGooglePrompt.setText(getString(R.string.label_account_connect_to_google));
            }

        } else {

            mGoogleCard.setVisibility(View.GONE);
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {

        for (int i = 0; i < signInButton.getChildCount(); i++) {

            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {

                TextView tv = (TextView) v;
                tv.setTextSize(15);
                tv.setTypeface(null, Typeface.NORMAL);
                tv.setText(buttonText);

                return;
            }
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
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
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }


    public void googleOauth(String action) {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GOOGLE_AUTH, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("error")) {

                                if (!response.getBoolean("error")) {

                                    if (action.equals("connect")) {

                                        toastWindow.makeText(getString(R.string.msg_connect_to_google_success), 2000);
                                        App.getInstance().setGoogleFirebaseId(uid);
                                    }

                                    if (action.equals("disconnect")) {

                                        toastWindow.makeText(getString(R.string.msg_connect_to_google_removed), 2000);
                                        App.getInstance().setGoogleFirebaseId("");
                                    }

                                } else {

                                    toastWindow.makeText(getString(R.string.msg_connect_to_google_error), 2000);
                                }
                            }

                        } catch (JSONException e) {
                          
                            e.printStackTrace();

                        } finally {

                            uid = "";

                            updateView();

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                uid = "";

                toastWindow.makeText(getText(R.string.error_data_loading), 2000);

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", App.getInstance().getAccessToken());
                params.put("account_id", Long.toString(App.getInstance().getId()));
                params.put("app_type", String.valueOf(APP_TYPE_ANDROID));
                params.put("action", action);
                params.put("uid", uid);
                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}