package com.hindbyte.dating.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.HomeActivity;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
import com.hindbyte.dating.util.ToastWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeactivateFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;

    EditText mCurrentPassword;

    TextView mDeactivate;

    String sCurrentPassword;

    private Boolean loading = false;

    ToastWindow toastWindow = new ToastWindow();
    public DeactivateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
setHasOptionsMenu(true);

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_deactivate, container, false);

        if (loading) {

            showpDialog();
        }

        mCurrentPassword = rootView.findViewById(R.id.currentPassword);
        mDeactivate = rootView.findViewById(R.id.deactivateBtn);

        mDeactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sCurrentPassword = mCurrentPassword.getText().toString();

                if (checkCurrentPassword(sCurrentPassword)) {

                    if (App.getInstance().isConnected()) {

                        deactivate();

                    } else {

                        toastWindow.makeText(getText(R.string.msg_network_error), 2000);
                    }
                }
            }
        });


        // Inflate the layout for this fragment
        return rootView;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void deactivate() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_DEACTIVATE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("error")) {

                                if (!response.getBoolean("error")) {

                                    logout();

                                } else {

                                    toastWindow.makeText(getText(R.string.error_password), 2000);
                                }
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

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("currentPassword", sCurrentPassword);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void logout() {

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    App.getInstance().removeData();
                                    App.getInstance().readData();

                                    Intent intent = new Intent(requireActivity(), HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
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

                    loading = false;

                    hidepDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public Boolean checkCurrentPassword(String password) {

        Helper helper = new Helper();

        if (password.length() == 0) {

            mCurrentPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mCurrentPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mCurrentPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mCurrentPassword.setError(null);

        return true;
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