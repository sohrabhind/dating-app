package com.hindbyte.dating.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.hindbyte.dating.R;
import com.hindbyte.dating.constants.Constants;
import com.hindbyte.dating.model.BaseGift;
import com.hindbyte.dating.model.Settings;
import com.hindbyte.dating.util.CustomRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class App extends Application implements Constants {

	public static final String TAG = App.class.getSimpleName();

	private RequestQueue mRequestQueue;

	private static App mInstance;

    private SharedPreferences sharedPref;

    private List<Map<String, String>> languages = new ArrayList<>();;
    private String language = "";

    private String username, fullname, accessToken, gcmToken = "", google_id = "", photoUrl, area = "", country = "", city = "";
    private Double lat = 0.0, lng = 0.0;
    private long id;
    private int state, balance = 0, pro, freeMessagesCount, allowShowMyAge, allowShowOnline, allowShowMyInfo, allowShowMyGallery, allowShowMyFriends, allowShowMyLikes, allowShowMyGifts, allowPhotosComments, allowComments, allowMessages, allowLikesGCM, allowCommentsGCM, allowFollowersGCM, allowMessagesGCM, allowGiftsGCM, allowCommentReplyGCM, errorCode, currentChatId = 0, notificationsCount = 0, messagesCount = 0, newFriendsCount = 0, seenTyping = 1;
    
    private Settings settings;
    private int feedMode = 0;

    private ArrayList<BaseGift> giftsList;

	@Override
	public void onCreate() {
		super.onCreate();
        mInstance = this;
        FirebaseApp.initializeApp(this);
        sharedPref = this.getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE);
        this.settings = new Settings();
        this.readData();
    }


    public void setLocale(String lang) {

        Locale myLocale;

        if (lang.length() == 0) {

            myLocale = new Locale("");

        } else {

            myLocale = new Locale(lang);
        }

        Resources res = getBaseContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = new Configuration();

        conf.setLocale(myLocale);
        conf.setLayoutDirection(myLocale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            getApplicationContext().createConfigurationContext(conf);

        } else {

            res.updateConfiguration(conf, dm);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

    }

    public void setLocation() {

        if (App.getInstance().isConnected()) {

            if (App.getInstance().isConnected() && App.getInstance().getId() != 0 && App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_GEO_LOCATION, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {

                                    if (!response.getBoolean("error")) {

//                                            Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {

                                    e.printStackTrace();

                                } finally {

                                    Log.d("GEO SAVE SUCCESS", response.toString());
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e("GEO SAVE ERROR", error.toString());
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("accountId", Long.toString(App.getInstance().getId()));
                        params.put("accessToken", App.getInstance().getAccessToken());
                        params.put("lat", Double.toString(App.getInstance().getLat()));
                        params.put("lng", Double.toString(App.getInstance().getLng()));

                        return params;
                    }
                };

                App.getInstance().addToRequestQueue(jsonReq);
            }
        }
    }

    
    public boolean isConnected() {
    	
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public void logout() {

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (!response.getBoolean("error")) {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    App.getInstance().removeData();
                    App.getInstance().readData();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);

        }

        App.getInstance().removeData();
        App.getInstance().readData();
    }

    public void loadSettings() {

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GET_SETTINGS, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {


                                    if (response.has("messagesCount")) {

                                        App.getInstance().setMessagesCount(response.getInt("messagesCount"));
                                    }

                                    if (response.has("seenTyping")) {

                                        App.getInstance().setSeenTyping(response.getInt("seenTyping"));
                                    }

                                    if (response.has("notificationsCount")) {

                                        App.getInstance().setNotificationsCount(response.getInt("notificationsCount"));
                                    }

                                    if (response.has("newFriendsCount")) {

                                        App.getInstance().setNewFriendsCount(response.getInt("newFriendsCount"));
                                    }

                                    if (response.has("allowShowNotModeratedProfilePhotos")) {

                                        if (response.getInt("allowShowNotModeratedProfilePhotos") == 1) {

                                            App.getInstance().getSettings().setAllowShowNotModeratedProfilePhotos(true);

                                        } else {

                                            App.getInstance().getSettings().setAllowShowNotModeratedProfilePhotos(false);
                                        }
                                    }

                                    if (response.has("defaultProModeCost")) {

                                        App.getInstance().getSettings().setProModeCost(response.getInt("defaultProModeCost"));
                                    }

                                    if (response.has("defaultMessagesPackageCost")) {

                                        App.getInstance().getSettings().setMessagePackageCost(response.getInt("defaultMessagesPackageCost"));
                                    }

                                    //

                                    if (response.has("free_messages_count")) {

                                        App.getInstance().setFreeMessagesCount(response.getInt("free_messages_count"));
                                    }

                                    if (response.has("level")) {

                                        App.getInstance().setLevelMode(response.getInt("level"));
                                    }

                                    if (response.has("balance")) {

                                        App.getInstance().setBalance(response.getInt("balance"));
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("loadSettings()", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public Settings getSettings() {

	    return this.settings;
    }

    public ArrayList<BaseGift> getGiftsList() {

        if (this.giftsList == null) {

            giftsList = new ArrayList<BaseGift>();
        }

        return this.giftsList;
    }


    public void updateGeoLocation() {

        // Now it is empty
        // In this application, there is no a web version and this code has been deleted
    }

    public Boolean authorize(JSONObject authObj) {

        try {

            if (authObj.has("error_code")) {

                this.setErrorCode(authObj.getInt("error_code"));
            }

            if (!authObj.has("error")) {

                return false;
            }

            if (authObj.getBoolean("error")) {

                return false;
            }

            if (!authObj.has("account")) {

                return false;
            }

            JSONArray accountArray = authObj.getJSONArray("account");

            if (accountArray.length() > 0) {

                JSONObject accountObj = (JSONObject) accountArray.get(0);

                if (accountObj.has("level")) {

                    this.setLevelMode(accountObj.getInt("level"));

                } else {

                    this.setLevelMode(0);
                }

                if (accountObj.has("free_messages_count")) {

                    this.setFreeMessagesCount(accountObj.getInt("free_messages_count"));

                } else {

                    this.setFreeMessagesCount(0);
                }

                this.setUsername(accountObj.getString("username"));
                this.setFullname(accountObj.getString("fullname"));
                this.setState(accountObj.getInt("state"));
                this.setBalance(accountObj.getInt("balance"));
                this.setAllowPhotosComments(accountObj.getInt("allowPhotosComments"));
                this.setAllowComments(accountObj.getInt("allowComments"));
                this.setAllowMessages(accountObj.getInt("allowMessages"));

                this.setAllowShowMyInfo(accountObj.getInt("allowShowMyInfo"));
                this.setAllowShowMyGallery(accountObj.getInt("allowShowMyGallery"));
                this.setAllowShowMyFriends(accountObj.getInt("allowShowMyFriends"));
                this.setAllowShowMyLikes(accountObj.getInt("allowShowMyLikes"));
                this.setAllowShowMyGifts(accountObj.getInt("allowShowMyGifts"));

                if (accountObj.has("gl_id")) {

                    this.setGoogleFirebaseId(accountObj.getString("gl_id"));
                }

                if (accountObj.has("allowShowMyAge")) {

                    this.setAllowShowMyAge(accountObj.getInt("allowShowMyAge"));
                }

                if (accountObj.has("allowShowOnline")) {

                    this.setAllowShowOnline(accountObj.getInt("allowShowOnline"));
                }

                this.setPhotoUrl(accountObj.getString("lowPhotoUrl"));

                if (App.getInstance().getLat() == 0.000000 && App.getInstance().getLng() == 0.000000) {

                    this.setLat(accountObj.getDouble("lat"));
                    this.setLng(accountObj.getDouble("lng"));
                }

                Log.d("Account", accountObj.toString());
            }

            this.setId(authObj.getLong("accountId"));
            this.setAccessToken(authObj.getString("accessToken"));

            this.saveData();

            this.loadSettings();

            if (getGcmToken().length() != 0) {

                setGcmToken(getGcmToken());
            }

            return true;

        } catch (JSONException e) {

            e.printStackTrace();
            return false;
        }
    }

    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public void setGcmToken(final String gcmToken) {

        if (this.getId() != 0 && this.getAccessToken().length() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_GCM_TOKEN, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

//                                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("setGcmToken()", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("fcm_regId", gcmToken);

                    return params;
                }
            };

            int socketTimeout = 0;//0 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }

        this.gcmToken = gcmToken;
    }

    public String getGcmToken() {

        return this.gcmToken;
    }


    public void setGoogleFirebaseId(String google_id) {

        this.google_id = google_id;
    }

    public String getGoogleFirebaseId() {

        if (this.google_id == null) {

            this.google_id = "";
        }

        return this.google_id;
    }

    public void setState(int state) {

        this.state = state;
    }

    public int getState() {

        return this.state;
    }

    public void setNotificationsCount(int notificationsCount) {

        this.notificationsCount = notificationsCount;

        updateMainActivityBadges(this, "");
    }

    public int getNotificationsCount() {

        return this.notificationsCount;
    }


    public void setMessagesCount(int messagesCount) {

        this.messagesCount = messagesCount;

        updateMainActivityBadges(this, "");
    }

    public int getMessagesCount() {

        return this.messagesCount;
    }

    public void setSeenTyping(int seenTyping) {

        this.seenTyping = seenTyping;
    }

    public int getSeenTyping() {

        return this.seenTyping;
    }

    public void setNewFriendsCount(int newFriendsCount) {

        this.newFriendsCount = newFriendsCount;
    }

    public int getNewFriendsCount() {

        return this.newFriendsCount;
    }

    public void setAllowMessagesGCM(int allowMessagesGCM) {

        this.allowMessagesGCM = allowMessagesGCM;
    }

    public int getAllowMessagesGCM() {

        return this.allowMessagesGCM;
    }

    public void setAllowGiftsGCM(int allowGiftsGCM) {

        this.allowGiftsGCM = allowGiftsGCM;
    }

    public int getAllowGiftsGCM() {

        return this.allowGiftsGCM;
    }

    public void setAllowCommentReplyGCM(int allowCommentReplyGCM) {

        this.allowCommentReplyGCM = allowCommentReplyGCM;
    }

    public int getAllowCommentReplyGCM() {

        return this.allowCommentReplyGCM;
    }

    public void setAllowFollowersGCM(int allowFollowersGCM) {

        this.allowFollowersGCM = allowFollowersGCM;
    }

    public int getAllowFollowersGCM() {

        return this.allowFollowersGCM;
    }

    public void setAllowCommentsGCM(int allowCommentsGCM) {

        this.allowCommentsGCM = allowCommentsGCM;
    }

    public int getAllowCommentsGCM() {

        return this.allowCommentsGCM;
    }

    public void setAllowLikesGCM(int allowLikesGCM) {

        this.allowLikesGCM = allowLikesGCM;
    }

    public int getAllowLikesGCM() {

        return this.allowLikesGCM;
    }

    public void setAllowMessages(int allowMessages) {

        this.allowMessages = allowMessages;
    }

    public int getAllowMessages() {

        return this.allowMessages;
    }

    public void setAllowComments(int allowComments) {

        this.allowComments = allowComments;
    }

    public int getAllowComments() {

        return this.allowComments;
    }

    public void setAllowPhotosComments(int allowPhotosComments) {

        this.allowPhotosComments = allowPhotosComments;
    }

    public int getAllowPhotosComments() {

        return this.allowPhotosComments;
    }

    // Privacy

    public void setAllowShowMyInfo(int allowShowMyInfo) {

        this.allowShowMyInfo = allowShowMyInfo;
    }

    public int getAllowShowMyInfo() {

        return this.allowShowMyInfo;
    }

    public void setAllowShowMyGallery(int allowShowMyGallery) {

        this.allowShowMyGallery = allowShowMyGallery;
    }

    public int getAllowShowMyGallery() {

        return this.allowShowMyGallery;
    }

    public void setAllowShowMyFriends(int allowShowMyFriends) {

        this.allowShowMyFriends = allowShowMyFriends;
    }

    public int getAllowShowMyFriends() {

        return this.allowShowMyFriends;
    }

    public void setAllowShowMyLikes(int allowShowMyLikes) {

        this.allowShowMyLikes = allowShowMyLikes;
    }

    public int getAllowShowMyLikes() {

        return this.allowShowMyLikes;
    }

    public void setAllowShowMyGifts(int allowShowMyGifts) {

        this.allowShowMyGifts = allowShowMyGifts;
    }

    public int getAllowShowMyGifts() {

        return this.allowShowMyGifts;
    }

    public void setAllowShowMyAge(int allowShowMyAge) {

        this.allowShowMyAge = allowShowMyAge;
    }

    public int getAllowShowMyAge() {

        return this.allowShowMyAge;
    }

    public void setAllowShowOnline(int allowShowOnline) {

        this.allowShowOnline = allowShowOnline;
    }

    public int getAllowShowOnline() {

        return this.allowShowOnline;
    }

    //

    public void setFreeMessagesCount(int freeMessagesCount) {

        this.freeMessagesCount = freeMessagesCount;
    }

    public int getFreeMessagesCount() {

        return this.freeMessagesCount;
    }

    public void setLevelMode(int pro) {

        this.pro = pro;
    }

    public int getLevelMode() {

        return this.pro;
    }


    public void setBalance(int balance) {

        this.balance = balance;
    }

    public int getBalance() {

        return this.balance;
    }

    public void setCurrentChatId(int currentChatId) {

        this.currentChatId = currentChatId;
    }

    public int getCurrentChatId() {

        return this.currentChatId;
    }

    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;
    }

    public int getErrorCode() {

        return this.errorCode;
    }

    public String getUsername() {

        if (this.username == null) {

            this.username = "";
        }

        return this.username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getAccessToken() {

        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

    public void setFullname(String fullname) {

        this.fullname = fullname;
    }

    public String getFullname() {

        if (this.fullname == null) {

            this.fullname = "";
        }

        return this.fullname;
    }

    public void setPhotoUrl(String photoUrl) {

        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {

        if (this.photoUrl == null) {

            this.photoUrl = "";
        }

        return this.photoUrl;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCountry() {

        if (this.country == null) {

            this.setCountry("");
        }

        return this.country;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getCity() {

        if (this.city == null) {

            this.setCity("");
        }

        return this.city;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getArea() {

        if (this.area == null) {

            this.setArea("");
        }

        return this.area;
    }


    public void setLat(Double lat) {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        this.lat = lat;
    }

    public Double getLat() {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        return this.lat;
    }

    public void setLng(Double lng) {

        if (this.lng == null) {

            this.lng = 0.000000;
        }

        this.lng = lng;
    }

    public Double getLng() {

        return this.lng;
    }



    public void setFeedMode(int feedMode) {

        this.feedMode = feedMode;
    }

    public int getFeedMode() {

        return this.feedMode;
    }

    public void readData() {
        this.setFeedMode(sharedPref.getInt(getString(R.string.settings_feed_mode), 0));

        this.setId(sharedPref.getLong(getString(R.string.settings_account_id), 0));
        this.setUsername(sharedPref.getString(getString(R.string.settings_account_username), ""));
        this.setAccessToken(sharedPref.getString(getString(R.string.settings_account_access_token), ""));
        this.setAllowMessagesGCM(sharedPref.getInt(getString(R.string.settings_account_allow_messages_gcm), 1));

        this.setFullname(sharedPref.getString(getString(R.string.settings_account_fullname), ""));
        this.setPhotoUrl(sharedPref.getString(getString(R.string.settings_account_photo_url), ""));

        this.setFreeMessagesCount(sharedPref.getInt(getString(R.string.settings_account_free_messages_count), 150));
        this.setLevelMode(sharedPref.getInt(getString(R.string.settings_account_pro_mode), 0));

        this.setAllowLikesGCM(sharedPref.getInt(getString(R.string.settings_account_allow_likes_gcm), 1));

        this.setAllowComments(sharedPref.getInt(getString(R.string.settings_account_allow_comments_gcm), 1));
        this.setAllowGiftsGCM(sharedPref.getInt(getString(R.string.settings_account_allow_gifts_gcm), 1));
        this.setAllowFollowersGCM(sharedPref.getInt(getString(R.string.settings_account_allow_friends_requests_gcm), 1));

        this.setBalance(sharedPref.getInt(getString(R.string.settings_account_balance), 0));

        if (App.getInstance().getLat() == 0.000000 && App.getInstance().getLng() == 0.000000) {

            this.setLat(Double.parseDouble(sharedPref.getString(getString(R.string.settings_account_lat), "0.000000")));
            this.setLng(Double.parseDouble(sharedPref.getString(getString(R.string.settings_account_lng), "0.000000")));
        }
    }

    public void saveData() {
        sharedPref.edit().putInt(getString(R.string.settings_feed_mode), this.getFeedMode()).apply();

        sharedPref.edit().putLong(getString(R.string.settings_account_id), this.getId()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_username), this.getUsername()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_access_token), this.getAccessToken()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_messages_gcm), this.getAllowMessagesGCM()).apply();

        sharedPref.edit().putString(getString(R.string.settings_account_fullname), this.getFullname()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_photo_url), this.getPhotoUrl()).apply();

        sharedPref.edit().putInt(getString(R.string.settings_account_free_messages_count), this.getFreeMessagesCount()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_pro_mode), this.getLevelMode()).apply();

        sharedPref.edit().putInt(getString(R.string.settings_account_allow_likes_gcm), this.getAllowLikesGCM()).apply();

        sharedPref.edit().putInt(getString(R.string.settings_account_allow_comments_gcm), this.getAllowCommentsGCM()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_gifts_gcm), this.getAllowGiftsGCM()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_friends_requests_gcm), this.getAllowFollowersGCM()).apply();


        sharedPref.edit().putInt(getString(R.string.settings_account_balance), this.getBalance()).apply();
        
        sharedPref.edit().putString(getString(R.string.settings_account_lat), Double.toString(this.getLat())).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_lng), Double.toString(this.getLng())).apply();

    }

    public void removeData() {

        sharedPref.edit().putLong(getString(R.string.settings_account_id), 0).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_username), "").apply();
        sharedPref.edit().putString(getString(R.string.settings_account_access_token), "").apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_messages_gcm), 1).apply();
    }

    public static void updateMainActivityBadges(Context context, String message) {

        Intent intent = new Intent(TAG_UPDATE_BADGES);
        intent.putExtra("message", message); // if need message
        context.sendBroadcast(intent);
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public static synchronized App getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {

		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}


	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
}