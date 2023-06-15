package com.hindbyte.dating.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.ChatActivity;
import com.hindbyte.dating.adapter.DialogsListAdapter;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.Chat;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.ToastWindow;
import com.hindbyte.dating.view.LineItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DialogsFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    RecyclerView mRecyclerView;
    NestedScrollView mNestedView;

    ToastWindow toastWindow = new ToastWindow();
    TextView mMessage;
    ImageView mSplash;

    SwipeRefreshLayout mItemsContainer;

    private ArrayList<Chat> itemsList;
    private DialogsListAdapter itemsAdapter;

    private int messageCreateAt = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;

    public DialogsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        itemsList = new ArrayList<>();
        itemsAdapter = new DialogsListAdapter(requireActivity(), itemsList);
        restore = false;
        messageCreateAt = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialogs, container, false);

        mItemsContainer = rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        mMessage = rootView.findViewById(R.id.message);
        mSplash = rootView.findViewById(R.id.splash);

        mNestedView = rootView.findViewById(R.id.nested_view);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.addItemDecoration(new LineItemDecoration(requireActivity(), LinearLayout.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new DialogsListAdapter.OnItemClickListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(View view, Chat item, int position) {

                Intent intent = new Intent(requireActivity(), ChatActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("chatId", item.getId());
                intent.putExtra("profileId", item.getWithUserId());
                String fullname = item.getWithUserFullname();
		        if (item.getId() != App.getInstance().getId() && fullname.split("\\w+").length>1) {
			        fullname = fullname.substring(0, fullname.lastIndexOf(' '));
		        }
                intent.putExtra("withProfile", fullname);
                intent.putExtra("with_user_username", item.getWithUserUsername());
                intent.putExtra("with_user_fullname", fullname);
                intent.putExtra("with_user_photo_url", item.getWithUserPhotoUrl());
                intent.putExtra("with_user_state", item.getWithUserState());
                intent.putExtra("blocked", item.getBlocked());
                intent.putExtra("fromUserId", item.getFromUserId());
                intent.putExtra("toUserId", item.getToUserId());

                startActivity(intent);

                if (item.getNewMessagesCount() != 0) {
                    item.setNewMessagesCount(0);
                    if (App.getInstance().getMessagesCount() > 0) {
                        App.getInstance().setMessagesCount(App.getInstance().getMessagesCount() - 1);
                        App.getInstance().saveData();
                    }
                }
                itemsAdapter.notifyDataSetChanged();
            }
        });

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY < oldScrollY) { // up


            }

            if (scrollY > oldScrollY) { // down


            }
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                if (!loadingMore && (viewMore) && !(mItemsContainer.isRefreshing())) {
                    mItemsContainer.setRefreshing(true);
                    loadingMore = true;
                    getItems(true);
                }
            }
        });

        if (itemsAdapter.getItemCount() == 0) {
            showMessage(getText(R.string.label_empty_list).toString());
        } else {
            hideMessage();
        }

        if (!restore) {
            showMessage(getText(R.string.msg_loading_2).toString());
            getItems(true);
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onRefresh();
        }
    }

    public void onRefresh() {
        if (App.getInstance().isConnected()) {
            messageCreateAt = 0;
            getItems(false);
        } else {
            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIEW_CHAT && resultCode == requireActivity().RESULT_OK && null != data) {

            int pos = data.getIntExtra("position", 0);

            toastWindow.makeText(getString(R.string.msg_chat_has_been_removed), 2000);

            itemsList.remove(pos);

            itemsAdapter.notifyDataSetChanged();

            if (itemsAdapter.getItemCount() == 0) {

                showMessage(getText(R.string.label_empty_list).toString());

            } else {

                hideMessage();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_filters);
        item.setVisible(false);
        item = menu.findItem(R.id.action_remove_all);
        item.setVisible(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putInt("messageCreateAt", messageCreateAt);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    public void getItems(boolean refresh) {

        mItemsContainer.setRefreshing(refresh);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_DIALOGS_NEW_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("isError", "Error Not Found");
                        if (!isAdded() || requireActivity() == null) {
                            Log.e("ERROR", "DialogsFragment Not Added to Activity");
                            return;
                        }
                        try {
                            arrayLength = 0;
                            if (!loadingMore) {
                                itemsList.clear();
                            }
                            if (!response.getBoolean("error")) {
                                messageCreateAt = response.getInt("messageCreateAt");
                                if (response.has("chats")) {
                                    JSONArray chatsArray = response.getJSONArray("chats");
                                    arrayLength = chatsArray.length();
                                    if (arrayLength > 0) {
                                        for (int i = 0; i < chatsArray.length(); i++) {
                                            JSONObject chatObj = (JSONObject) chatsArray.get(i);
                                            Chat chat = new Chat(chatObj);
                                            itemsList.add(chat);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            loadingComplete();
                            e.printStackTrace();
                        } finally {
                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isAdded() || requireActivity() == null) {
                    Log.e("ERROR", "DialogsFragment Not Added to Activity");
                    return;
                }
                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("messageCreateAt", String.valueOf(messageCreateAt));
                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadingComplete() {

        viewMore = arrayLength == LIST_ITEMS;

        itemsAdapter.notifyDataSetChanged();

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_list).toString());

        } else {

            hideMessage();
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mMessage.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
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