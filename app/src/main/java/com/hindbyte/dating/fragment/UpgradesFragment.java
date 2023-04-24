package com.hindbyte.dating.fragment;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.BalanceActivity;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.util.CustomRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class UpgradesFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;
    Button mGetCreditsButton, mProModeButton, mMessagePackageButton;
    TextView mLabelCredits, mLabelProModeStatus, mLabelProModeTitle, mLabelMessagePackageStatus;
    LinearLayout mMessagePackageContainer;

    private Boolean loading = false;
    public UpgradesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_upgrades, container, false);
        if (loading) {
            showpDialog();
        }

        mLabelCredits = rootView.findViewById(R.id.labelCredits);
        mMessagePackageContainer = rootView.findViewById(R.id.messagePackageContainer);
        mLabelProModeStatus = rootView.findViewById(R.id.labelProModeStatus);
        mLabelProModeTitle = rootView.findViewById(R.id.labelProMode);
        mLabelMessagePackageStatus = rootView.findViewById(R.id.labelMessagePackageStatus);
        mProModeButton = rootView.findViewById(R.id.proModeBtn);
        mMessagePackageButton = rootView.findViewById(R.id.messagePackageBtn);
        mGetCreditsButton = rootView.findViewById(R.id.getCreditsBtn);

        mGetCreditsButton.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), BalanceActivity.class);
            startActivityForResult(i, 1945);
        });


        mProModeButton.setOnClickListener(v -> {
            if (App.getInstance().getBalance() >= App.getInstance().getSettings().getProModeCost()) {
                upgrade(PA_BUY_PRO_MODE, App.getInstance().getSettings().getProModeCost());
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_credits), Toast.LENGTH_SHORT).show();
            }
        });

        mMessagePackageButton.setOnClickListener(v -> {
            if (App.getInstance().getBalance() >= App.getInstance().getSettings().getMessagePackageCost()) {
                upgrade(PA_BUY_MESSAGE_PACKAGE, App.getInstance().getSettings().getMessagePackageCost());
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_credits), Toast.LENGTH_SHORT).show();
            }
        });

        update();
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1945 && resultCode == Activity.RESULT_OK && null != data) {
            update();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onDestroyView() {
        super.onDestroyView();
        hidepDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    @SuppressLint("SetTextI18n")
    public void update() {
        mLabelCredits.setText(getString(R.string.label_credits) + " (" + App.getInstance().getBalance() + ")");
        mProModeButton.setText(getString(R.string.action_enable) + " (" + App.getInstance().getSettings().getProModeCost() + ")" + " " + getString(R.string.label_life_time));
        mMessagePackageButton.setText(getString(R.string.action_purchase) + " (" + App.getInstance().getSettings().getMessagePackageCost() + ")");
        if (App.getInstance().getLevelMode() == 0) {
            mLabelProModeStatus.setVisibility(View.GONE);
            mProModeButton.setEnabled(true);
            mProModeButton.setVisibility(View.VISIBLE);
            mLabelProModeTitle.setText(requireActivity().getString(R.string.label_upgrades_pro_mode));
            mLabelMessagePackageStatus.setText(String.format(Locale.getDefault(), getString(R.string.free_messages_of), App.getInstance().getFreeMessagesCount()));
        } else {
            mLabelProModeStatus.setVisibility(View.VISIBLE);
            mProModeButton.setEnabled(false);
            mProModeButton.setVisibility(View.GONE);
            mLabelProModeTitle.setText(requireActivity().getString(R.string.label_upgrades_pro_mode));
            mMessagePackageContainer.setVisibility(View.GONE);
        }
    }

    public void upgrade(final int upgradeType, final int credits) {
        loading = true;
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_UPGRADE, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            switch (upgradeType) {
                                case PA_BUY_PRO_MODE: {
                                    App.getInstance().setBalance(App.getInstance().getBalance() - credits);
                                    App.getInstance().setLevelMode(1);
                                    Toast.makeText(getActivity(), getString(R.string.msg_success_pro_mode), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                case PA_BUY_MESSAGE_PACKAGE: {
                                    App.getInstance().setBalance(App.getInstance().getBalance() - credits);
                                    App.getInstance().setFreeMessagesCount(App.getInstance().getFreeMessagesCount() + 100);
                                    Toast.makeText(getActivity(), getString(R.string.msg_success_buy_message_package), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        hidepDialog();
                        update();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading = false;
                update();
                hidepDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("upgradeType", String.valueOf(upgradeType));
                params.put("credits", String.valueOf(credits));
                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
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
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}