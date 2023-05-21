package com.hindbyte.dating.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.hindbyte.dating.fragment.AccountSettingsFragment;
import com.hindbyte.dating.R;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.dialogs.AlcoholViewsSelectDialog;
import com.hindbyte.dating.dialogs.GenderSelectDialog;
import com.hindbyte.dating.dialogs.RelationshipStatusSelectDialog;
import com.hindbyte.dating.dialogs.SmokingViewsSelectDialog;
import com.hindbyte.dating.dialogs.ReligiousViewSelectDialog;
import com.hindbyte.dating.dialogs.YouLikeSelectDialog;
import com.hindbyte.dating.dialogs.YouLookingSelectDialog;

import java.util.Objects;

public class AccountSettingsActivity extends ActivityBase implements GenderSelectDialog.AlertPositiveListener, RelationshipStatusSelectDialog.AlertPositiveListener, ReligiousViewSelectDialog.AlertPositiveListener, SmokingViewsSelectDialog.AlertPositiveListener, AlcoholViewsSelectDialog.AlertPositiveListener, YouLookingSelectDialog.AlertPositiveListener, YouLikeSelectDialog.AlertPositiveListener {

    Toolbar mToolbar;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.hindbyte.dating.R.layout.activity_account_settings);

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
        fragmentManager.beginTransaction().replace(com.hindbyte.dating.R.id.container_body, fragment).commit();
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
        getMenuInflater().inflate(com.hindbyte.dating.R.menu.menu_account_settings, menu);
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
    public void onRelationshipStatusSelect(int position) {
        AccountSettingsFragment p = (AccountSettingsFragment) fragment;
        p.getRelationshipStatus(position);
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