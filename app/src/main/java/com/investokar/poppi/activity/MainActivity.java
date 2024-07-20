package com.investokar.poppi.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.common.ActivityBase;
import com.investokar.poppi.fragment.DialogsFragment;
import com.investokar.poppi.fragment.HotGameFragment;
import com.investokar.poppi.fragment.MenuFragment;
import com.investokar.poppi.fragment.NotificationsFragment;
import com.investokar.poppi.util.CustomViewPager;

import java.util.Map;
import java.util.Objects;

public class MainActivity extends ActivityBase {

    private TabLayout tabLayout;
    ViewPagerAdapter adapter;
    CustomViewPager viewPager;
    Toolbar mToolbar;

    int tab_id = 0;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.container_body);
        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        setTitle(R.string.app_name);

        // Bottom Navigation
        tabLayout = findViewById(R.id.nav_bottom_view);

        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(adapter);
        viewPager.setPagingEnabled(false);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_swipe_cards).setContentDescription("Home");
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_notification).setContentDescription("Notifications");
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_messages).setContentDescription("Messages");
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_menu).setContentDescription("Menu");

        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_balck_3), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_white_3), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_white_3), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(3).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_white_3), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tab.getIcon().setColorFilter(getResources().getColor(R.color.md_theme_balck_3), PorterDuff.Mode.SRC_IN);
                setTitle(tab.getContentDescription());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.md_theme_white_3), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setTitle(tab.getContentDescription());
            }
        });

        onNewIntent(getIntent());


        notificationsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {
            boolean granted = false;
            String notifications_permission = Manifest.permission.POST_NOTIFICATIONS;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notifications_permission = Manifest.permission.POST_NOTIFICATIONS;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                if (x.getKey().equals( notifications_permission)) {
                    if (x.getValue()) {
                        granted = true;
                    }
                }
            }

            if (granted) {
                // Permission is granted
                Log.e("Push Permission", "Permission is granted");
            } else {

                // Permission is denied

                Log.e("Push Permission", "denied");

                Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_notifications_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);
                    }

                }).show();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra("tab_id")) {
            tab_id = intent.getIntExtra("tab_id", 0);
            if (viewPager != null && tabLayout != null) {
                tabLayout.getTabAt(tab_id).select();
            }
        }
    }




    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(@NonNull FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new NotificationsFragment();
                case 2:
                    return new DialogsFragment();
                case 3:
                    return new MenuFragment();
                default:
                    return new HotGameFragment();
            }
        }

        @Override
        public int getCount() {
            return tabLayout.getTabCount();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void setTitle(CharSequence title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }


    private ActivityResultLauncher<String[]>  notificationsPermissionLauncher;

    @Override
    public void onResume() {
        super.onResume();
        onNewIntent(getIntent());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                notificationsPermissionLauncher.launch(new String[]{Manifest.permission.POST_NOTIFICATIONS});
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
