package com.investokar.poppi.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.dialogs.MyPhotoActionDialog;
import com.investokar.poppi.dialogs.PhotoActionDialog;
import com.investokar.poppi.dialogs.PhotoDeleteDialog;
import com.investokar.poppi.dialogs.PhotoReportDialog;
import com.investokar.poppi.model.Image;
import com.investokar.poppi.util.Api;
import com.investokar.poppi.util.CustomRequest;
import com.investokar.poppi.util.ToastWindow;
import com.investokar.poppi.view.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ViewImageFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;

    ToastWindow toastWindow = new ToastWindow();
    RelativeLayout mLoadingScreen;


    TouchImageView mItemImg;


    Image item = new Image();

    long itemId = 0, replyToUserId = 0;
    int arrayLength = 0;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;

    private Boolean loadingComplete = false;

    public ViewImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initpDialog();
        Intent i = requireActivity().getIntent();
        itemId = i.getLongExtra("itemId", 0);
        Log.e("ERROR_TEST", "No Error");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_image, container, false);
        if (savedInstanceState != null) {
            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");
            replyToUserId = savedInstanceState.getLong("replyToUserId");
        } else {
            restore = false;
            loading = false;
            preload = false;
            replyToUserId = 0;
        }

        if (loading) {
            showpDialog();
        }

        final GridLayoutManager mLayoutManager = new GridLayoutManager(requireActivity(), 1);
        mLoadingScreen = rootView.findViewById(R.id.PhotoViewLoadingScreen);

        mItemImg = rootView.findViewById(R.id.itemImage);
        mItemImg.setMaxZoom(10f);

        if (!restore) {
            if (App.getInstance().isConnected()) {
                showLoadingScreen();
                loading = true;
                getItem();
            } else {
                showErrorScreen();
            }
        } else {
            if (App.getInstance().isConnected()) {
                if (!preload) {
                    loadingComplete();
                    updateItem();
                } else {
                    showLoadingScreen();
                }

            } else {
                showErrorScreen();
            }
        }

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

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putLong("replyToUserId", replyToUserId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getItemModeText(int postMode) {

        switch (postMode) {

            case 0: {

                return getString(R.string.label_image_for_public);
            }

            default: {

                return getString(R.string.label_image_for_friends);
            }
        }
    }

    public void updateItem() {


        if (item.getItemType() == Constants.GALLERY_ITEM_TYPE_IMAGE) {
            String imageUrl = item.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = String.valueOf(R.drawable.profile_default_photo);
            }
            Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.profile_default_photo)
            .error(R.drawable.profile_default_photo)
            .into(mItemImg);

            mItemImg.setVisibility(View.VISIBLE);
        }

    }


    public void getItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_GET_ITEM, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || requireActivity() == null) {

                            Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                            return;
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            item = new Image(itemObj);

                                            updateItem();
                                        }
                                    }
                                }

                                loadingComplete();

                            } else {

                                showErrorScreen();
                            }

                        } catch (JSONException e) {

                            showErrorScreen();

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || requireActivity() == null) {

                    Log.e("ERROR", "ViewImageFragment Not Added to Activity");

                    return;
                }

                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }


    public void onPhotoDelete(final int position) {

        Api api = new Api(requireActivity());

        api.photoDelete(item.getId());

        requireActivity().finish();
    }

    public void onPhotoReport(int position, int reasonId) {

        if (App.getInstance().isConnected()) {

            Api api = new Api(requireActivity());

            api.photoReport(item.getId(), reasonId);

        } else {

            toastWindow.makeText(getText(R.string.msg_network_error), 2000);
        }
    }

    public void remove(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        PhotoDeleteDialog alert = new PhotoDeleteDialog();

        Bundle b = new Bundle();
        b.putInt("position", 0);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_photo_delete");
    }

    public void report(int position) {

        FragmentManager fm = requireActivity().getSupportFragmentManager();

        PhotoReportDialog alert = new PhotoReportDialog();

        Bundle b  = new Bundle();
        b.putInt("position", position);
        b.putInt("reason", 0);

        alert.setArguments(b);
        alert.show(fm, "alert_dialog_photo_report");
    }

    public void action(int position) {

        if (item.getOwner().getId() == App.getInstance().getId()) {

            /** Getting the fragment manager */
            FragmentManager fm = requireActivity().getSupportFragmentManager();

            /** Instantiating the DialogFragment class */
            MyPhotoActionDialog alert = new MyPhotoActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_my_post_action");

        } else {

            /** Getting the fragment manager */
            FragmentManager fm = requireActivity().getSupportFragmentManager();

            /** Instantiating the DialogFragment class */
            PhotoActionDialog alert = new PhotoActionDialog();

            /** Creating a bundle object to store the selected item's index */
            Bundle b  = new Bundle();

            /** Storing the selected item's index in the bundle object */
            b.putInt("position", position);

            /** Setting the bundle object to the dialog fragment object */
            alert.setArguments(b);

            /** Creating the dialog fragment object, which will in turn open the alert dialog window */

            alert.show(fm, "alert_post_action");
        }
    }

    public void loadingComplete() {
        showContentScreen();
        if (loading) {
            loading = false;
        }
    }

    public void showLoadingScreen() {
        preload = true;
        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showEmptyScreen() {
        mLoadingScreen.setVisibility(View.GONE);
    }

    public void showErrorScreen() {
        mLoadingScreen.setVisibility(View.GONE);
    }

    public void showContentScreen() {
        preload = false;
        mLoadingScreen.setVisibility(View.GONE);
        loadingComplete = true;
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_view_item, menu);

//        MainMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (App.getInstance().getId() != item.getOwner().getId()) {

                menu.removeItem(R.id.action_delete);

            } else {

                menu.removeItem(R.id.action_report);
            }

            //show all menu items
            hideMenuItems(menu, true);

        } else {

            //hide all menu items
            hideMenuItems(menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            if (App.getInstance().isConnected()) {
                showLoadingScreen();
                loading = true;
                getItem();
            } else {
                loading = false;
            }
            return true;
        } else if (id == R.id.action_delete) {
            remove(0);
            return true;
        } else if (id == R.id.action_report) {
            report(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }


}