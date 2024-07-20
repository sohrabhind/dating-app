package com.investokar.poppi.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.common.ActivityBase;
import com.investokar.poppi.util.ToastWindow;
import com.investokar.poppi.view.GradientTextView;

import java.util.Objects;


public class UpgradeActivity extends ActivityBase {

    Toolbar mToolbar;
    int productType;

    static String ITEM_PRODUCT_1_AMOUNT = "300";// in usd cents | 3 usd = 300 cents
    static String ITEM_PRODUCT_2_AMOUNT = "600";// in usd cents | 6 usd = 600 cents
    static String ITEM_PRODUCT_3_AMOUNT = "900";// in usd cents | 9 usd = 900 cents

    LinearLayout mBuy1Button, mBuy2Button, mBuy3Button;
    TextView mBuyButton, totalMessages, totalLikes, profileBoost, iap1_google_price, iap2_google_price, iap3_google_price;
    GradientTextView badgeTitle;
    ImageView levelIcon;

    private Boolean loading = false;

    ToastWindow toastWindow = new ToastWindow();
    private String packageName = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        if (savedInstanceState != null) {
            loading = savedInstanceState.getBoolean("loading");
        } else {
            loading = false;
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (loading) {
            showpDialog();
        }

        mBuy1Button = findViewById(R.id.iap1_google_btn);
        mBuy2Button = findViewById(R.id.iap2_google_btn);
        mBuy3Button = findViewById(R.id.iap3_google_btn);
        mBuyButton = findViewById(R.id.iap_google_btn);
        badgeTitle = findViewById(R.id.get_badge_title);
        levelIcon = findViewById(R.id.level_icon);
        totalMessages = findViewById(R.id.total_messages);
        totalLikes = findViewById(R.id.total_likes);
        profileBoost = findViewById(R.id.profile_boost);
        iap1_google_price = findViewById(R.id.iap1_google_price);
        iap2_google_price = findViewById(R.id.iap2_google_price);
        iap3_google_price = findViewById(R.id.iap3_google_price);

        badgeTitle.setText("Unlock premium-only benefits today!");
        totalMessages.setText("1500 Messages");
        totalLikes.setText("Unlimted Likes");
        profileBoost.setText("Open 20 Chats Daily");
        levelIcon.setImageResource(R.drawable.level_gold);
        productType = 2;
        mBuy1Button.setBackgroundResource(R.color.md_theme_white_3);
        mBuy2Button.setBackgroundResource(R.drawable.gradation_primary);
        mBuy3Button.setBackgroundResource(R.color.md_theme_white_3);

        mBuy1Button.setOnClickListener(v -> {
            totalMessages.setText("500 Messages");
            totalLikes.setText("Unlimted Likes");
            profileBoost.setText("Open 10 Chats Daily");
            levelIcon.setImageResource(R.drawable.level_silver);
            mBuy1Button.setBackgroundResource(R.drawable.gradation_primary);
            mBuy2Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy3Button.setBackgroundResource(R.color.md_theme_white_3);
            productType = 1;
        });

        mBuy2Button.setOnClickListener(v -> {
            totalMessages.setText("1500 Messages");
            totalLikes.setText("Unlimted Likes");
            profileBoost.setText("Open 20 Chats Daily");
            levelIcon.setImageResource(R.drawable.level_gold);
            mBuy1Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy2Button.setBackgroundResource(R.drawable.gradation_primary);
            mBuy3Button.setBackgroundResource(R.color.md_theme_white_3);
            productType = 2;
        });

        mBuy3Button.setOnClickListener(v -> {
            totalMessages.setText("3000 Messages");
            totalLikes.setText("Unlimted Likes");
            profileBoost.setText("Open 30 Chats Daily");
            levelIcon.setImageResource(R.drawable.level_diamond);
            mBuy1Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy2Button.setBackgroundResource(R.color.md_theme_white_3);
            mBuy3Button.setBackgroundResource(R.drawable.gradation_primary);
            productType = 3;
        });

        mBuyButton.setOnClickListener(v -> {
            launchBilling(productType);
        });

        Intent i = getIntent();
        String popupString = i.getStringExtra("popup_string");
        if (popupString != null) {
            toastWindow.makeText(popupString, 5000);
        }
    }

    private void launchBilling(int productType) {
        Intent i = new Intent(this, WebActivity.class);
        if (productType == 1) {
            i.putExtra("TITLE", "Upgrade Level");
            i.putExtra("URL", "https://hindbyte.com/payment/upgrade.php?level=1&user_id="+String.valueOf(App.getInstance().getId()));
            startActivity(i);
        } else if (productType == 2) {
            i.putExtra("TITLE", "Upgrade Level");
            i.putExtra("URL", "https://hindbyte.com/payment/upgrade.php?level=2&user_id="+String.valueOf(App.getInstance().getId()));
            startActivity(i);
        } else if (productType == 3) {
            i.putExtra("TITLE", "Upgrade Level");
            i.putExtra("URL", "https://hindbyte.com/payment/upgrade.php?level=3&user_id="+String.valueOf(App.getInstance().getId()));
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void success() {
        toastWindow.makeText(getString(R.string.msg_success_purchase), 2000);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("loading", loading);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hidepDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here. The action bar will
        if (item.getItemId() == android.R.id.home) {
            finish();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

}