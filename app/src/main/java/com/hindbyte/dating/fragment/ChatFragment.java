package com.hindbyte.dating.fragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.hindbyte.dating.R;
import com.hindbyte.dating.activity.UpgradeActivity;
import com.hindbyte.dating.adapter.ChatListAdapter;
import com.hindbyte.dating.adapter.StickerListAdapter;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.ChatItem;
import com.hindbyte.dating.model.Sticker;
import com.hindbyte.dating.util.CustomRequest;
import com.hindbyte.dating.util.Helper;
import com.hindbyte.dating.util.ToastWindow;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class ChatFragment extends Fragment implements Constants {

    private static final String STATE_LIST = "State Adapter Data";

    public final static int STATUS_START = 100;

    public final static String PARAM_TASK = "task";
    public final static String PARAM_STATUS = "status";

    public final static String BROADCAST_ACTION = "ru.ifsoft.chat.chat";
    public final static String BROADCAST_ACTION_SEEN = "ru.ifsoft.chat.seen";
    public final static String BROADCAST_ACTION_TYPING_START = "ru.ifsoft.chat.typing_start";
    public final static String BROADCAST_ACTION_TYPING_END = "ru.ifsoft.chat.typing_end";

    final String LOG_TAG = "myLogs";

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    Menu MainMenu;

    View mListViewHeader;

    RelativeLayout mLoadingScreen, mErrorScreen;
    LinearLayout mContentScreen, mTypingContainer, mContainerImg, mChatListViewHeaderContainer;

    ImageView mSendMessage, mActionContainerImg, mDeleteImg, mPreviewImg;
    EditText mMessageText;

    ListView listView;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    private ArrayList<Sticker> stickersList;
    private StickerListAdapter stickersAdapter;

    BroadcastReceiver br, br_seen, br_typing_start, br_typing_end;

    private ArrayList<ChatItem> chatList;

    private ChatListAdapter chatAdapter;

    String withProfile = "", messageText = "", messageImg = "", stickerImg = "";
    int chatId = 0, lastMessageId = 0, messagesCount = 0, position = 0;
    long profileId = 0, stickerId = 0, lStickerId = 0;

    String lMessage = "", lStickerImg = "";

    Boolean blocked = false;

    Boolean img_container_visible = false;

    long fromUserId = 0, toUserId = 0;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    int arrayLength = 0;
    Boolean loadingMore = false;
    Boolean viewMore = false;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean visible = true;

    ToastWindow toastWindow = new ToastWindow();

    private Boolean inboxTyping = false, outboxTyping = false;

    private String with_user_username = "", with_user_fullname = "", with_user_photo_url = "";
    private int with_user_state = 0, with_user_verified = 0;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = requireActivity().getIntent();
        position = i.getIntExtra("position", 0);
        chatId = i.getIntExtra("chatId", 0);
        profileId = i.getLongExtra("profileId", 0);
        withProfile = i.getStringExtra("withProfile");

        with_user_username = i.getStringExtra("with_user_username");
        with_user_fullname = i.getStringExtra("with_user_fullname");
        with_user_photo_url = i.getStringExtra("with_user_photo_url");

        with_user_state = i.getIntExtra("with_user_state", 0);
        with_user_verified = i.getIntExtra("with_user_verified", 0);

        blocked = i.getBooleanExtra("blocked", false);

        fromUserId = i.getLongExtra("fromUserId", 0);
        toUserId = i.getLongExtra("toUserId", 0);

        chatList = new ArrayList<ChatItem>();
        chatAdapter = new ChatListAdapter(requireActivity(), chatList);
    }

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        //

        //

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                showMoreDialog();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);
                        toastWindow.makeText(getString(R.string.label_grant_camera_permission), 2000);
                    }
                }).show();
            }
        });

        //

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

                        mPreviewImg.setImageURI(null);
                        mPreviewImg.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                        showImageContainer();
                    }
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                    mPreviewImg.setImageURI(null);
                    mPreviewImg.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                    showImageContainer();
                }
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

                showMoreDialog();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);
                        toastWindow.makeText(getString(R.string.label_grant_storage_permission), 2000);
                    }
                }).show();
            }

        });


        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            img_container_visible = savedInstanceState.getBoolean("img_container_visible");

            stickersList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            stickersAdapter = new StickerListAdapter(requireActivity(), stickersList);

        } else {

            stickersList = new ArrayList<>();
            stickersAdapter = new StickerListAdapter(requireActivity(), stickersList);

            App.getInstance().setCurrentChatId(chatId);

            restore = false;
            loading = false;
            preload = false;

            img_container_visible = false;
        }

        br_typing_start = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_start();
            }
        };

        IntentFilter intFilt4 = new IntentFilter(BROADCAST_ACTION_TYPING_START);
        requireActivity().registerReceiver(br_typing_start, intFilt4);

        br_typing_end = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                typing_end();
            }
        };

        IntentFilter intFilt3 = new IntentFilter(BROADCAST_ACTION_TYPING_END);
        requireActivity().registerReceiver(br_typing_end, intFilt3);

        br_seen = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                seen();
            }
        };

        IntentFilter intFilt2 = new IntentFilter(BROADCAST_ACTION_SEEN);
        requireActivity().registerReceiver(br_seen, intFilt2);

        br = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                int lastMessageId = intent.getIntExtra("lastMessageId", 0);
                long msgFromUserId = intent.getLongExtra("msgFromUserId", 0);
                int msgFromUserState = intent.getIntExtra("msgFromUserState", 0);
                String msgFromUserUsername = intent.getStringExtra("msgFromUserUsername");
                String msgFromUserFullname = intent.getStringExtra("msgFromUserFullname");
                String msgFromUserPhotoUrl = intent.getStringExtra("msgFromUserPhotoUrl");
                String msgMessage = intent.getStringExtra("msgMessage");
                String msgImgUrl = intent.getStringExtra("msgImgUrl");
                String stickerImgUrl = intent.getStringExtra("stickerImgUrl");
                int stickerId = intent.getIntExtra("stickerId", 0);
                int msgCreateAt = intent.getIntExtra("msgCreateAt", 0);
                String msgDate = intent.getStringExtra("msgDate");
                String msgTimeAgo = intent.getStringExtra("msgTimeAgo");

                ChatItem c = new ChatItem();
                c.setId(lastMessageId);
                c.setFromUserId(msgFromUserId);

                if (msgFromUserId == App.getInstance().getId()) {

                    c.setFromUserState(App.getInstance().getState());
                    c.setFromUserUsername(App.getInstance().getUsername());
                    c.setFromUserFullname(App.getInstance().getFullname());
                    c.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());

                } else {

                    c.setFromUserState(with_user_state);
                    c.setFromUserUsername(with_user_username);
                    c.setFromUserFullname(with_user_fullname);
                    c.setFromUserPhotoUrl(with_user_photo_url);
                }

                c.setMessage(msgMessage);
                c.setImgUrl(msgImgUrl);
                c.setStickerImgUrl(stickerImgUrl);
                c.setStickerId(stickerId);
                c.setCreateAt(msgCreateAt);
                c.setDate(msgDate);
                c.setTimeAgo(msgTimeAgo);

                Log.e(LOG_TAG, "onReceive: task = " + task + ", status = " + status + " " + c.getMessage() + " " + c.getId());



                final ChatItem lastItem = (ChatItem) listView.getAdapter().getItem(listView.getAdapter().getCount() - 1);

                messagesCount = messagesCount + 1;

                chatList.add(c);

                if (!visible) {

                    try {

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(requireActivity(), notification);
                        r.play();

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                chatAdapter.notifyDataSetChanged();

                scrollListViewToBottom();

                if (inboxTyping) typing_end();

                seen();

                sendNotify(GCM_NOTIFY_SEEN);
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        requireActivity().registerReceiver(br, intFilt);

        if (loading) {

            showpDialog();
        }

        mLoadingScreen = rootView.findViewById(R.id.loadingScreen);
        mErrorScreen = rootView.findViewById(R.id.errorScreen);

        mContentScreen = rootView.findViewById(R.id.contentScreen);

        mSendMessage = rootView.findViewById(R.id.sendMessage);
        mMessageText = rootView.findViewById(R.id.messageText);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessage();
            }
        });

        listView = rootView.findViewById(R.id.listView);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        mListViewHeader = requireActivity().getLayoutInflater().inflate(R.layout.chat_listview_header, null);
        mChatListViewHeaderContainer = mListViewHeader.findViewById(R.id.chatListViewHeaderContainer);

        listView.addHeaderView(mListViewHeader);

        mListViewHeader.setVisibility(View.GONE);

        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (position == 0 && mListViewHeader.getVisibility() == View.VISIBLE) {

                getPreviousMessages();
            }
        });

        mActionContainerImg = rootView.findViewById(R.id.actionContainerImg);

        mTypingContainer = rootView.findViewById(R.id.container_typing);

        mTypingContainer.setVisibility(View.GONE);

        mDeleteImg = rootView.findViewById(R.id.deleteImg);
        mPreviewImg = rootView.findViewById(R.id.previewImg);

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        mContainerImg = rootView.findViewById(R.id.container_img);
        mContainerImg.setVisibility(View.GONE);

        mDeleteImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                selectedImage = null;
                selectedImagePath = "";

                hideImageContainer();
            }
        });

        mActionContainerImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showMoreDialog();
            }
        });

        if (selectedImagePath != null && selectedImagePath.length() > 0) {

            mPreviewImg.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

            showImageContainer();
        }


        mMessageText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                String txt = mMessageText.getText().toString();

                if (txt.length() == 0 && outboxTyping) {

                    outboxTyping = false;

                    sendNotify(GCM_NOTIFY_TYPING_END);

                } else {

                    if (!outboxTyping && txt.length() > 0) {
                        outboxTyping = true;
                        sendNotify(GCM_NOTIFY_TYPING_START);
                    }
                }

                Log.e("", "afterTextChanged");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.e("", "beforeTextChanged");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.e("", "onTextChanged");
            }
        });

        if (inboxTyping) {

            mTypingContainer.setVisibility(View.VISIBLE);

        } else {

            mTypingContainer.setVisibility(View.GONE);
        }

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getChat();

            } else {

                showErrorScreen();
            }

        } else {
            if (App.getInstance().isConnected()) {
                if (!preload) {
                    showContentScreen();
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

    public void initiatePopupWindow(String popup) {
        Intent intent = new Intent(requireActivity(), UpgradeActivity.class);
        intent.putExtra("popup_string", popup);
        startActivity(intent);
    }

    public void typing_start() {

        inboxTyping = true;

        mTypingContainer.setVisibility(View.VISIBLE);
    }

    public void typing_end() {

        mTypingContainer.setVisibility(View.GONE);

        inboxTyping = false;
    }

    public void seen() {

        if (chatAdapter.getCount() > 0) {

            for (int i = 0; i < chatAdapter.getCount(); i++) {

                ChatItem item = chatList.get(i);

                if (item.getFromUserId() == App.getInstance().getId()) {

                    chatList.get(i).setSeenAt(1);
                }
            }
        }

        chatAdapter.notifyDataSetChanged();
    }

    public void sendNotify(final int notifyId) {

        if (App.getInstance().getSeenTyping() != 1) {

            return;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_NOTIFY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || requireActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("send fcm", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded()) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }

                Log.e("send fcm error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("chatId", String.valueOf(chatId));
                params.put("notifyId", String.valueOf(notifyId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }


    public void onDestroyView() {

        super.onDestroyView();

        requireActivity().unregisterReceiver(br);

        requireActivity().unregisterReceiver(br_seen);

        requireActivity().unregisterReceiver(br_typing_start);

        requireActivity().unregisterReceiver(br_typing_end);

        hidepDialog();
    }

    @Override
    public void onResume() {

        super.onResume();

        visible = true;
    }

    @Override
    public void onPause() {

        super.onPause();

        visible = false;
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

        outState.putBoolean("img_container_visible", img_container_visible);

        outState.putParcelableArrayList(STATE_LIST, stickersList);
    }

    private void scrollListViewToBottom() {

        listView.smoothScrollToPosition(chatAdapter.getCount());

        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    public void updateChat() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_UPDATE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || requireActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("TAG", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded()) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                } else {
                    requireActivity();
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("chatId", String.valueOf(chatId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getChat() {

        preload = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET, null,
                response -> {

                    if (!isAdded()) {

                        Log.e("ERROR", "ChatFragment Not Added to Activity");

                        return;
                    } else {
                        requireActivity();
                    }

                    try {

                        if (!response.getBoolean("error")) {
                            lastMessageId = response.getInt("lastMessageId");
                            chatId = response.getInt("chatId");
                            messagesCount = response.getInt("messagesCount");
                            App.getInstance().setCurrentChatId(chatId);
                            fromUserId = response.getLong("chatFromUserId");
                            toUserId = response.getLong("chatToUserId");

                            if (messagesCount > 20) {
                                mListViewHeader.setVisibility(View.VISIBLE);
                            }

                            if (response.has("messages")) {
                                JSONArray messagesArray = response.getJSONArray("messages");
                                arrayLength = messagesArray.length();
                                if (arrayLength > 0) {
                                    for (int i = messagesArray.length() - 1; i > -1; i--) {
                                        JSONObject msgObj = (JSONObject) messagesArray.get(i);
                                        ChatItem item = new ChatItem(msgObj);
                                        chatList.add(item);
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        showContentScreen();
                        chatAdapter.notifyDataSetChanged();
                        scrollListViewToBottom();
                        updateChat();
                    }
                }, error -> {
                    if (!isAdded() || requireActivity() == null) {
                        Log.e("ERROR", "ChatFragment Not Added to Activity");
                        return;
                    }
                    preload = false;
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("lastMessageId", String.valueOf(lastMessageId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getPreviousMessages() {
        loading = true;
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET_PREVIOUS, null,
                response -> {
                    if (!isAdded() || requireActivity() == null) {
                        Log.e("ERROR", "ChatFragment Not Added to Activity");
                        return;
                    }

                    try {

                        if (!response.getBoolean("error")) {

                            lastMessageId = response.getInt("lastMessageId");
                            chatId = response.getInt("chatId");

                            if (response.has("messages")) {

                                JSONArray messagesArray = response.getJSONArray("messages");

                                arrayLength = messagesArray.length();

                                if (arrayLength > 0) {

                                    for (int i = 0; i < messagesArray.length(); i++) {

                                        JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                        ChatItem item = new ChatItem(msgObj);

                                        chatList.add(0, item);
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();

                    } finally {

                        loading = false;

                        hidepDialog();

                        chatAdapter.notifyDataSetChanged();

                        if (messagesCount <= listView.getAdapter().getCount() - 1) {

                            mListViewHeader.setVisibility(View.GONE);

                        } else {

                            mListViewHeader.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || requireActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("chatId", String.valueOf(chatId));
                params.put("lastMessageId", String.valueOf(lastMessageId));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void newMessage() {
        if (App.getInstance().isConnected()) {
            messageText = mMessageText.getText().toString();
            messageText = messageText.trim();
            if (selectedImagePath.length() != 0) {
                loading = true;
                showpDialog();
                File f = new File(selectedImagePath);
                uploadFile(METHOD_MSG_UPLOAD_IMG, f);
            } else {
                if (messageText.length() > 0) {
                    loading = true;
                    send();
                } else {
                    toastWindow.makeText(getText(R.string.msg_enter_msg), 2000);
                }
            }
        } else {
            toastWindow.makeText(getText(R.string.msg_network_error), 2000);
        }
    }

    public void send() {
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_MSG_NEW, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            chatId = response.getInt("chatId");
                            App.getInstance().setCurrentChatId(chatId);
                            if (response.has("message")) {
                                JSONObject msgObj = response.getJSONObject("message");
                                ChatItem item = new ChatItem(msgObj);
                                item.setListId(response.getInt("listId"));
                                chatList.add(item);
                                chatAdapter.notifyDataSetChanged();
                                scrollListViewToBottom();
                            }
                        } else {
                            if (response.getInt("error_code") == 402) {
                                initiatePopupWindow("Subscribe to send more messages.");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        hidepDialog();
                        messageText = "";
                        messageImg = "";
                        Log.e("Chat", response.toString());
                    }
                }, error -> {
                    messageText = "";
                    messageImg = "";
                    loading = false;
                    hidepDialog();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profileId));
                params.put("chatId", String.valueOf(chatId));
                params.put("messageText", lMessage);
                params.put("listId", String.valueOf(listView.getAdapter().getCount()));
                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));
                params.put("stickerImgUrl", lStickerImg);
                params.put("stickerId", Long.toString(lStickerId));
                return params;
            }
        };


        lMessage = messageText;
        lStickerImg = stickerImg;
        lStickerId = stickerId;
        if (stickerId != 0) {
            messageImg = stickerImg;
            lMessage = "";
            messageText = "";
        }

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);
        App.getInstance().addToRequestQueue(jsonReq);
        outboxTyping = false;

        mContainerImg.setVisibility(View.GONE);
        selectedImagePath = "";
        selectedImage = null;
        messageImg = "";
        mMessageText.setText("");
        messagesCount++;

        stickerImg = "";
        stickerId = 0;
        hideImageContainer();
    }


    public Boolean uploadFile(String serverURL, File file) {
        lMessage = messageText;
        lStickerImg = stickerImg;
        lStickerId = stickerId;
        if (stickerId != 0) {
            messageImg = stickerImg;
            lMessage = "";
            messageText = "";
        }

        outboxTyping = false;

        mContainerImg.setVisibility(View.GONE);
        selectedImagePath = "";
        selectedImage = null;
        messageImg = "";
        mMessageText.setText("");
        messagesCount++;

        stickerImg = "";
        stickerId = 0;
        hideImageContainer();

        final OkHttpClient client = new OkHttpClient();
        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        try {
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .addFormDataPart("profileId", Long.toString(profileId))
                    .addFormDataPart("chatId", String.valueOf(chatId))
                    .addFormDataPart("messageText", lMessage)
                    .addFormDataPart("listId", String.valueOf(listView.getAdapter().getCount()))
                    .addFormDataPart("chatFromUserId", Long.toString(fromUserId))
                    .addFormDataPart("chatToUserId", Long.toString(toUserId))
                    .addFormDataPart("stickerImgUrl", lStickerImg)
                    .addFormDataPart("stickerId", Long.toString(lStickerId))
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    messageText = "";
                    messageImg = "";
                    loading = false;
                    hidepDialog();
                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);
                    try {
                        JSONObject result = new JSONObject(jsonData);
                        if (!result.getBoolean("error")) {
                            chatId = result.getInt("chatId");
                            App.getInstance().setCurrentChatId(chatId);
                            if (result.has("message")) {
                                JSONObject msgObj = result.getJSONObject("message");
                                ChatItem item = new ChatItem(msgObj);
                                item.setListId(result.getInt("listId"));
                                chatList.add(item);
                                chatAdapter.notifyDataSetChanged();
                                scrollListViewToBottom();
                            }
                        } else {
                            if (result.getInt("error_code") == 402) {
                                initiatePopupWindow("Subscribe to send more messages.");
                            }
                        }
                        Log.d("My App", response.toString());
                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");
                    } finally {
                        loading = false;
                        hidepDialog();
                        messageText = "";
                        messageImg = "";
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

    public void deleteChat() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_REMOVE, null,
                new Response.Listener<>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded()) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        } else {
                            requireActivity();
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                Intent i = new Intent();
                                i.putExtra("action", "Delete");
                                i.putExtra("position", position);
                                i.putExtra("chatId", chatId);
                                requireActivity().setResult(RESULT_OK, i);

                                requireActivity().finish();

//                                toastWindow.makeText(getString(R.string.msg_send_msg_error), 2000);
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

                if (!isAdded() || requireActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));
                params.put("chatId", String.valueOf(chatId));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showLoadingScreen() {

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        preload = false;

        requireActivity().invalidateOptionsMenu();
    }

    private void showMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (App.getInstance().isConnected()) {

            if (!preload) {

                requireActivity().setTitle(withProfile);

                showMenuItems(menu, true);

            } else {

                showMenuItems(menu, false);
            }

        } else {

            showMenuItems(menu, false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_chat, menu);
        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_chat_delete) {
            deleteChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        updateChat();
        if (outboxTyping) {
            sendNotify(GCM_NOTIFY_TYPING_END);
        }
    }


    public void loadStickers() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GET_STICKERS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!isAdded() || requireActivity() == null) {

                                Log.e("ERROR", "ChatFragment Not Added to Activity");

                                return;
                            }

                            if (!loadingMore) {

                                stickersList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

//                                stickerId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray stickersArray = response.getJSONArray("items");

                                    arrayLength = stickersArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < stickersArray.length(); i++) {

                                            JSONObject stickerObj = (JSONObject) stickersArray.get(i);

                                            Sticker u = new Sticker(stickerObj);

                                            stickersList.add(u);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("SUCCESS", "ChatFragment Success Load Stickers");

                            stickersAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || requireActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                Log.e("ERROR", "ChatFragment Not Load Stickers");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", String.valueOf(0));

                return params;
            }
        };

        jsonReq.setRetryPolicy(new RetryPolicy() {

            @Override
            public int getCurrentTimeout() {

                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {

                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void showMoreDialog() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.chat_sheet_list, null);

        MaterialRippleLayout mStickersButton = (MaterialRippleLayout) view.findViewById(R.id.stickers_button);
        MaterialRippleLayout mGalleryButton = (MaterialRippleLayout) view.findViewById(R.id.gallery_button);
        MaterialRippleLayout mCameraButton = (MaterialRippleLayout) view.findViewById(R.id.camera_button);

        mStickersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                choiceStickerDialog();
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                if (!checkPermission(READ_EXTERNAL_STORAGE)) {

                    requestPermission();

                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    imgFromGalleryActivityResultLauncher.launch(intent);
                }
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                if (!checkPermission(READ_EXTERNAL_STORAGE)) {

                    requestPermission();

                } else {

                    if (checkPermission(Manifest.permission.CAMERA)) {

                        try {

                            newImageFileName = Helper.randomString(6) + ".jpg";

                            selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName));

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            imgFromCameraActivityResultLauncher.launch(cameraIntent);

                        } catch (Exception e) {

                            toastWindow.makeText("Error occured. Please try again later.", 2000);
                        }

                    } else {

                        requestCameraPermission();
                    }
                }
            }
        });


        mBottomSheetDialog = new BottomSheetDialog(requireActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    private void choiceStickerDialog() {

        final Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_stickers);
        dialog.setCancelable(true);

        final ProgressBar mProgressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        TextView mDlgTitle = (TextView) dialog.findViewById(R.id.title_label);
        mDlgTitle.setText(R.string.label_chat_stickers);

        AppCompatButton mDlgCancelButton = (AppCompatButton) dialog.findViewById(R.id.cancel_button);
        mDlgCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        final RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(requireActivity(), Helper.getStickersGridSpanCount(requireActivity()));
        mDlgRecyclerView.setLayoutManager(mLayoutManager);
        mDlgRecyclerView.setHasFixedSize(true);
        mDlgRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDlgRecyclerView.setAdapter(stickersAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        stickersAdapter.setOnItemClickListener(new StickerListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Sticker obj, int position) {

                stickerId = obj.getId();
                stickerImg = obj.getImgUrl();

                send();

                dialog.dismiss();
            }
        });

        if (stickersList.size() == 0) {

            mDlgRecyclerView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GET_STICKERS, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!isAdded() || requireActivity() == null) {

                                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                                    return;
                                }

                                if (!loadingMore) {

                                    stickersList.clear();
                                }

                                arrayLength = 0;

                                if (!response.getBoolean("error")) {

//                                stickerId = response.getInt("itemId");

                                    if (response.has("items")) {

                                        JSONArray stickersArray = response.getJSONArray("items");

                                        arrayLength = stickersArray.length();

                                        if (arrayLength > 0) {

                                            for (int i = 0; i < stickersArray.length(); i++) {

                                                JSONObject stickerObj = (JSONObject) stickersArray.get(i);

                                                Sticker u = new Sticker(stickerObj);

                                                stickersList.add(u);
                                            }
                                        }
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                Log.d("SUCCESS", "ChatFragment Success Load Stickers");

                                stickersAdapter.notifyDataSetChanged();

                                if (stickersAdapter.getItemCount() != 0) {

                                    mDlgRecyclerView.setVisibility(View.VISIBLE);
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || requireActivity() == null) {

                        Log.e("ERROR", "ChatFragment Not Added to Activity");

                        return;
                    }

                    Log.e("ERROR", "ChatFragment Not Load Stickers");
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("itemId", String.valueOf(0));

                    return params;
                }
            };

            jsonReq.setRetryPolicy(new RetryPolicy() {

                @Override
                public int getCurrentTimeout() {

                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {

                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            App.getInstance().addToRequestQueue(jsonReq);
        }

        dialog.show();

        doKeepDialog(dialog);
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    public void showImageContainer() {

        img_container_visible = true;

        mContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setVisibility(View.GONE);
    }

    public void hideImageContainer() {

        img_container_visible = false;

        mContainerImg.setVisibility(View.GONE);

        mActionContainerImg.setVisibility(View.VISIBLE);

        mActionContainerImg.setBackgroundResource(R.drawable.ic_action_new);
    }

    private boolean checkPermission(String permission) {

        if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED) {

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