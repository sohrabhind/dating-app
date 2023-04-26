package com.hindbyte.dating.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.volley.Request;
import com.google.common.collect.ImmutableList;
import com.hindbyte.dating.R;
import com.hindbyte.dating.app.App;
import com.hindbyte.dating.common.ActivityBase;
import com.hindbyte.dating.util.CustomRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class UpgradeActivity extends ActivityBase {

    Toolbar mToolbar;
    static final String ITEM_PRODUCT_1 = "dating.hindbyte.com.iaps";    // Change to: yourdomain.com.iap1
    static final String ITEM_PRODUCT_2 = "dating.hindbyte.com.iapg";    // Change to: yourdomain.com.iap2
    static final String ITEM_PRODUCT_3 = "dating.hindbyte.com.iapd";    // Change to: yourdomain.com.iap3

    static final int ITEM_PRODUCT_1_AMOUNT = 300;// in usd cents | 3 usd = 300 cents
    static final int ITEM_PRODUCT_2_AMOUNT = 600;// in usd cents | 6 usd = 600 cents
    static final int ITEM_PRODUCT_3_AMOUNT = 900;// in usd cents | 9 usd = 900 cents

    Button mBuy1Button, mBuy2Button, mBuy3Button;

    private Boolean loading = false;

    private BillingClient mBillingClient;
    private final Map<String, ProductDetails> mProductDetailsMap = new HashMap<>();
    private String packageName;

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

        mBuy1Button.setOnClickListener(v -> launchBilling(ITEM_PRODUCT_1));

        mBuy2Button.setOnClickListener(v -> launchBilling(ITEM_PRODUCT_2));

        mBuy3Button.setOnClickListener(v -> launchBilling(ITEM_PRODUCT_3));

        setupBilling();

        Intent i = getIntent();
        packageName = i.getStringExtra("package");
        if (packageName != null) {
            if (!loading) {
                loading = true;
                showpDialog();
                launchIntentBilling();
            }
        }
    }

    private void launchIntentBilling() {
        new Handler().postDelayed(() -> {
            if (mProductDetailsMap.get(ITEM_PRODUCT_3) != null) {
                loading = false;
                hidepDialog();
                switch (packageName) {
                    case "silver":
                        launchBilling(ITEM_PRODUCT_1);
                        break;
                    case "gold":
                        launchBilling(ITEM_PRODUCT_2);
                        break;
                    case "diamond":
                        launchBilling(ITEM_PRODUCT_3);
                        break;
                }
            } else {
                launchIntentBilling();
            }
        }, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupBilling() {
        mBillingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        try {
                            JSONObject obj = new JSONObject(purchase.getOriginalJson());
                            consume(purchase.getPurchaseToken(), obj.getString("productId"));
                            Log.e("Billing", obj.toString());
                        } catch (Throwable t) {
                            Log.e("Billing", "Could not parse malformed JSON: \"" + purchase.toString() + "\"");
                        }
                    }
                }
            }
        }).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.e("onBillingSetupFinished", "NOT WORKS");
            }
        });
    }

    private void consume(String purchaseToken, String product) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build();
        ConsumeResponseListener listener = (billingResult, outToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
                // For example, increase the number of coins inside the user's basket.
                switch (product) {
                    case ITEM_PRODUCT_1:
                        App.getInstance().setBalance(App.getInstance().getBalance() + 300);
                        payment(1, ITEM_PRODUCT_1_AMOUNT, PT_GOOGLE_PURCHASE,true);
                        break;
                    case ITEM_PRODUCT_2:
                        App.getInstance().setBalance(App.getInstance().getBalance() + 600);
                        payment(2, ITEM_PRODUCT_2_AMOUNT, PT_GOOGLE_PURCHASE,true);
                        break;
                    case ITEM_PRODUCT_3:
                        App.getInstance().setBalance(App.getInstance().getBalance() + 900);
                        payment(3, ITEM_PRODUCT_3_AMOUNT, PT_GOOGLE_PURCHASE,true);
                        break;
                    default:
                        break;
                }
            }
        };
        mBillingClient.consumeAsync(consumeParams, listener);
    }


    private void queryProductDetails() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(ITEM_PRODUCT_1).setProductType(BillingClient.ProductType.INAPP).build());
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(ITEM_PRODUCT_2).setProductType(BillingClient.ProductType.INAPP).build());
        productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(ITEM_PRODUCT_3).setProductType(BillingClient.ProductType.INAPP).build());

        QueryProductDetailsParams productDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(productList).build();
        mBillingClient.queryProductDetailsAsync(productDetailsParams, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (ProductDetails productDetails : list) {
                    mProductDetailsMap.put(productDetails.getProductId(), productDetails);
                }
            }
        });
    }

    public void launchBilling(String productId) {
       ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(Objects.requireNonNull(mProductDetailsMap.get(productId)))
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build();
        mBillingClient.launchBillingFlow(this, billingFlowParams);
    }


    public void payment(final int level, final int amount, final int paymentType, final Boolean showSuccess) {
        loading = true;
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PAYMENTS_NEW, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            if (response.has("level")) {
                                App.getInstance().setLevelMode(level);
                            }
                            if (showSuccess) {
                                success();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        loading = false;
                        hidepDialog();
                    }
                }, error -> {
                    loading = false;
                    hidepDialog();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("level", String.valueOf(level));
                params.put("paymentType", String.valueOf(paymentType));
                params.put("amount", String.valueOf(amount));
                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void success() {
        Toast.makeText(UpgradeActivity.this, getString(R.string.msg_success_purchase), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("loading", loading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBillingClient.isReady()) {
            setupBilling();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hidepDialog();
        mBillingClient.endConnection();
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