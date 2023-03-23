package com.hindbyte.dating.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.hindbyte.dating.fragment.DialogsFragment;
import com.hindbyte.dating.fragment.HotgameFragment;
import com.hindbyte.dating.fragment.MainFragment;
import com.hindbyte.dating.fragment.MenuFragment;
import com.hindbyte.dating.fragment.NotificationsFragment;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.dialogs.FriendRequestActionDialog;
import com.hindbyte.dating.fragment.PeopleNearbyFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActivityBase implements FriendRequestActionDialog.AlertPositiveListener {

    private Menu mMainMenu;

    private AppBarLayout mAppBarLayout;

    private BottomNavigationView mNavBottomView;
    private Menu mNavMenu;

    Toolbar mToolbar;

    // used to store app title
    private CharSequence mTitle;

    Fragment fragment;
    Boolean action = false;
    int page = 0;

    private int pageId = PAGE_MAIN;

    private Boolean restore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Get intent data

        Intent i = getIntent();

        pageId = i.getIntExtra("pageId", PAGE_MAIN);

        // Send user geolocation data to server

        App.getInstance().setLocation();


        if (savedInstanceState != null) {

            //Restore the fragment's instance
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

            restore = savedInstanceState.getBoolean("restore");
            mTitle = savedInstanceState.getString("mTitle");

        } else {

            fragment = new Fragment();

            restore = false;
            mTitle = getString(R.string.app_name);
        }

        if (fragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        }

        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(mTitle);
        //

        mToolbar.setBackgroundResource(R.drawable.background_login);

        mAppBarLayout = findViewById(R.id.appbar_layout);

        // Bottom Navigation

        mNavBottomView = findViewById(R.id.nav_bottom_view);
//        BottomNavigationViewHelper.disableShiftMode(mNavBottomView);


        mNavBottomView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                hideKeyboard();
                displayFragment(item.getItemId(), item.getTitle().toString());
                return true;
            }
        });

        mNavMenu = mNavBottomView.getMenu();


        if (!restore) {

            switch (pageId) {

                case PAGE_NOTIFICATIONS: {
                    displayFragment(mNavMenu.findItem(R.id.nav_notifications).getItemId(), getString(R.string.title_activity_notifications));
                    break;
                }

                case PAGE_MESSAGES: {
                    displayFragment(mNavMenu.findItem(R.id.nav_messages).getItemId(), getString(R.string.title_activity_dialogs));
                    break;
                }

                default: {
                    // Show default section "Media"
                    displayFragment(mNavMenu.findItem(R.id.nav_finder).getItemId(), getString(R.string.title_activity_media));
                    break;
                }
            }
        }
    }

    public void showBadge(Context context, BottomNavigationView bottomNavigationView, @IdRes int itemId, int count) {
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 2) {
            View badge = LayoutInflater.from(context).inflate(R.layout.bottom_nav_notifications_badge, bottomNavigationView, false);
            itemView.addView(badge);
        }
    }

    public void removeBadge(BottomNavigationView bottomNavigationView, @IdRes int itemId) {
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }
    }


    public void refreshBadges() {
        if (App.getInstance().getNotificationsCount() != 0) {
            showBadge(this, mNavBottomView, R.id.nav_notifications, App.getInstance().getNotificationsCount());
        } else {
            removeBadge(mNavBottomView, R.id.nav_notifications);
        }

        if (App.getInstance().getMessagesCount() != 0) {
            showBadge(this, mNavBottomView, R.id.nav_messages, App.getInstance().getMessagesCount());
        } else {
            removeBadge(mNavBottomView, R.id.nav_messages);
        }

        if (App.getInstance().getNewFriendsCount() != 0) {
            showBadge(this, mNavBottomView, R.id.nav_menu, App.getInstance().getNotificationsCount());
        } else {
            removeBadge(mNavBottomView, R.id.nav_menu);
        }
    }

    private void displayFragment(int id, String title) {
        action = false;
        switch (id) {
            case R.id.nav_menu: {
                page = PAGE_MENU;
                mNavBottomView.getMenu().findItem(R.id.nav_menu).setChecked(true);
                fragment = new MenuFragment();
                action = true;
                break;
            }

            case R.id.nav_messages: {
                page = PAGE_MESSAGES;
                mNavBottomView.getMenu().findItem(R.id.nav_messages).setChecked(true);
                fragment = new DialogsFragment();
                action = true;
                break;
            }

            case R.id.nav_notifications: {
                page = PAGE_NOTIFICATIONS;
                mNavBottomView.getMenu().findItem(R.id.nav_notifications).setChecked(true);
                fragment = new NotificationsFragment();
                action = true;
                break;
            }

            case R.id.nav_finder: {
                page = PAGE_HOTGAME;
                mNavBottomView.getMenu().findItem(R.id.nav_finder).setChecked(true);
                fragment = new HotgameFragment();
                action = true;
                break;
            }

            /*
            case R.id.nav_stream: {
                page = PAGE_MAIN;
                mNavBottomView.getMenu().findItem(R.id.nav_stream).setChecked(true);
                fragment = new MainFragment();
                action = true;
                break;
            }*/
        }

        if (action) {
            getSupportActionBar().setDisplayShowCustomEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("restore", true);
        outState.putString("mTitle", getSupportActionBar().getTitle().toString());
        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onAcceptRequest(int position) {
        NotificationsFragment p = (NotificationsFragment) fragment;
        p.onAcceptRequest(position);
    }

    @Override
    public void onRejectRequest(int position) {
        NotificationsFragment p = (NotificationsFragment) fragment;
        p.onRejectRequest(position);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMainMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home: {
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshBadges();
        registerReceiver(mMessageReceiver, new IntentFilter(TAG_UPDATE_BADGES));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    //This is the handler that will manager to process the broadcast intent
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            // String message = intent.getStringExtra("message");
            refreshBadges();
        }
    };
}
