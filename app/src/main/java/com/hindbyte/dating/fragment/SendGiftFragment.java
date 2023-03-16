package com.hindbyte.dating.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.util.CustomRequest;
import com.squareup.picasso.Picasso;

public class SendGiftFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    EditText mMessageEdit;
    ImageView mGiftImg;

    String messageText = "", imgUrl = "";


    private int giftAnonymous = 0, giftCost = 0;

    private long giftTo = 0, giftId = 0;

    private Boolean loading = false;

    public SendGiftFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        giftId = i.getLongExtra("giftId", 0);
        giftTo = i.getLongExtra("giftTo", 0);

        giftCost = i.getIntExtra("giftCost", 0);

        imgUrl = i.getStringExtra("imgUrl");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_send_gift, container, false);


        if (loading) {

            showpDialog();
        }

        mMessageEdit = rootView.findViewById(R.id.messageEdit);
        mGiftImg = rootView.findViewById(R.id.profilePhoto);

        Picasso.get()
                    .load(imgUrl)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(mGiftImg);


        mMessageEdit.addTextChangedListener(new TextWatcher() {

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

                    getActivity().setTitle(getText(R.string.title_activity_send_gift));

                } else {

                    getActivity().setTitle(String.valueOf(140 - cnt));
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_post: {

                if (App.getInstance().isConnected()) {

                    messageText = mMessageEdit.getText().toString();
                    messageText = messageText.trim();

                    loading = true;

                    showpDialog();

                    sendGift();

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendGift() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GIFTS_SEND, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setBalance(App.getInstance().getBalance() - giftCost);
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            sendGiftSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendGiftSuccess();

//                     Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("giftAnonymous", String.valueOf(giftAnonymous));
                params.put("message", messageText);
                params.put("giftId", Long.toString(giftId));
                params.put("giftTo", Long.toString(giftTo));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendGiftSuccess() {

        loading = false;

        hidepDialog();

        Intent i = new Intent();
        getActivity().setResult(RESULT_OK, i);

        Toast.makeText(getActivity(), getText(R.string.msg_gift_sent), Toast.LENGTH_SHORT).show();

        getActivity().finish();
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