package com.investokar.poppi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.investokar.poppi.fragment.AccountSettingsFragment;
import com.investokar.poppi.R;
import com.investokar.poppi.common.ActivityBase;
import com.investokar.poppi.dialogs.AlcoholViewsSelectDialog;
import com.investokar.poppi.dialogs.GenderSelectDialog;
import com.investokar.poppi.dialogs.SmokingViewsSelectDialog;
import com.investokar.poppi.dialogs.ReligiousViewSelectDialog;
import com.investokar.poppi.dialogs.YouLikeSelectDialog;
import com.investokar.poppi.dialogs.YouLookingSelectDialog;

import java.util.Objects;

public class AccountSettingsActivity extends ActivityBase implements GenderSelectDialog.AlertPositiveListener, ReligiousViewSelectDialog.AlertPositiveListener, SmokingViewsSelectDialog.AlertPositiveListener, AlcoholViewsSelectDialog.AlertPositiveListener, YouLookingSelectDialog.AlertPositiveListener, YouLikeSelectDialog.AlertPositiveListener {

    Toolbar mToolbar;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.investokar.poppi.R.layout.activity_account_settings);

        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");
        } else {
            fragment = new AccountSettingsFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(com.investokar.poppi.R.id.container_body, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.investokar.poppi.R.menu.menu_account_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGenderSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getGender(position);
    }


    @Override
    public void onReligiousViewSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getReligiousView(position);
    }

    @Override
    public void onSmokingViewsSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getSmokingViews(position);
    }

    @Override
    public void onAlcoholViewsSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getAlcoholViews(position);
    }

    @Override
    public void onYouLookingSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getYouLooking(position);
    }

    @Override
    public void onYouLikeSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getYouLike(position);
    }
}