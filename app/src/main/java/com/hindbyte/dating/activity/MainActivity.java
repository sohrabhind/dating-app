package com.hindbyte.dating.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.hindbyte.dating.R;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.fragment.DialogsFragment;
import com.hindbyte.dating.fragment.HotGameFragment;
import com.hindbyte.dating.fragment.MenuFragment;
import com.hindbyte.dating.fragment.NotificationsFragment;
import com.hindbyte.dating.util.CustomViewPager;

import java.util.Objects;

public class MainActivity extends ActivityBase {

    private TabLayout tabLayout;
    ViewPagerAdapter adapter;

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomViewPager viewPager = findViewById(R.id.container_body);
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

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_swipe_cards).setContentDescription(R.string.app_name);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_notification).setContentDescription(R.string.nav_notifications);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_messages).setContentDescription(R.string.nav_messages);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_menu).setContentDescription(R.string.nav_menu);

        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_light_primary), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_light_onSurfaceVariant), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_light_onSurfaceVariant), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(3).getIcon().setColorFilter(getResources().getColor(R.color.md_theme_light_onSurfaceVariant), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tab.getIcon().setColorFilter(getResources().getColor(R.color.md_theme_light_primary), PorterDuff.Mode.SRC_IN);
                setTitle(tab.getContentDescription());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.md_theme_light_onSurfaceVariant), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                setTitle(tab.getContentDescription());
            }
        });

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
