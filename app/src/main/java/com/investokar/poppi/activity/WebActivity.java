package com.investokar.poppi.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;

import com.investokar.poppi.R;
import com.investokar.poppi.app.App;
import com.investokar.poppi.common.ActivityBase;


public class WebActivity extends ActivityBase {

    FrameLayout frameLayout;
    String title = "";
    String checkoutURL = "";
    CheckoutWebView myWebView;
    Toolbar toolbar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra("TITLE");
            checkoutURL = intent.getStringExtra("URL");
        }
        
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(title);
        
        frameLayout = findViewById(R.id.frameLayout);
        myWebView = new CheckoutWebView(this);
        frameLayout.addView(myWebView);
        myWebView.loadUrl(checkoutURL);
        showpDialog();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public class CheckoutWebView extends WebView {
        @SuppressLint("SetJavaScriptEnabled")
        public CheckoutWebView(Context context) {
            super(context);
            WebSettings webSettings = getSettings();
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);

            setWebViewClient(new CheckoutWebViewClient());
            setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, final int progress) {
                    if ((myWebView.getOriginalUrl() != null && myWebView.getOriginalUrl().contains("verify.php")) || (myWebView.getUrl() != null && myWebView.getUrl().contains("verify.php"))){
                        if (App.getInstance().getId() != 0) {
                            App.getInstance().loadSettings();
                        }
                    }
                    if (progress == 100) {
                        hidepDialog();
                    } else {
                        showpDialog();
                    }
                }

                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                    CheckoutWebView CheckoutWebView = new CheckoutWebView(context);
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(CheckoutWebView);
                    resultMsg.sendToTarget();
                    frameLayout.addView(CheckoutWebView);
                    return true;
                }

                @Override
                public void onCloseWindow(WebView view) {
                    frameLayout.removeViewAt(frameLayout.getChildCount()-1);
                }
            });
        }
    }

    private class CheckoutWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && onKeyCodeBack();
    }

    private boolean onKeyCodeBack() {
        if (frameLayout.getChildCount() > 1) {
            frameLayout.removeViewAt(frameLayout.getChildCount()-1);
        } else if ((myWebView.getOriginalUrl() != null && myWebView.getOriginalUrl().contains("verify.php")) || (myWebView.getUrl() != null && myWebView.getUrl().contains("verify.php"))){
            finish();
        } else if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            finish();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here. The action bar will
        if (item.getItemId() == android.R.id.home) {
            onKeyCodeBack();
        }
        return super.onOptionsItemSelected(item);
    }

}