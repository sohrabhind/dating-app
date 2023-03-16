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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import uk.co.senab.photoview.PhotoViewAttacher;


public class PhotoViewActivity extends ActivityBase {

    private static final String TAG = "photo_view_activity";

    Toolbar toolbar;

    ImageView photoView;

    LinearLayout mContentScreen;
    RelativeLayout mLoadingScreen;

    PhotoViewAttacher mAttacher;

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

        mContentScreen = findViewById(R.id.PhotoViewContentScreen);
        mLoadingScreen = findViewById(R.id.PhotoViewLoadingScreen);

        photoView = findViewById(R.id.photoImageView);
        getSupportActionBar().setTitle("");

        showLoadingScreen();

        Picasso.get()
                .load(imgUrl)
                .placeholder(R.drawable.img_loading)
                .error(R.drawable.img_loading)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mAttacher = new PhotoViewAttacher(photoView);
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
