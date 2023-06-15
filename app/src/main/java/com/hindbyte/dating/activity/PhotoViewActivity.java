package com.hindbyte.dating.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.VolleyError;

import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.view.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;


public class PhotoViewActivity extends ActivityBase {

    private static final String TAG = "photo_view_activity";

    Toolbar toolbar;

    TouchImageView photoView;

    LinearLayout mContentScreen;
    RelativeLayout mLoadingScreen;


    String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        Intent i = getIntent();
        imgUrl = i.getStringExtra("imgUrl");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        mContentScreen = findViewById(R.id.PhotoViewContentScreen);
        mLoadingScreen = findViewById(R.id.PhotoViewLoadingScreen);

        photoView = findViewById(R.id.photoImageView);
        photoView.setMaxZoom(10f);

        showLoadingScreen();

        Picasso.get()
                .load(imgUrl)
                .placeholder(R.drawable.profile_default_photo)
                .error(R.drawable.profile_default_photo)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        showContentScreen();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void showLoadingScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        mLoadingScreen.setVisibility(View.GONE);
        mContentScreen.setVisibility(View.VISIBLE);
    }
}
