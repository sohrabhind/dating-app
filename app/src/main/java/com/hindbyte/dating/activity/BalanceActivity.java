package com.hindbyte.dating.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
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


public class BalanceActivity extends ActivityBase {

    Toolbar mToolbar;
    static final String ITEM_PRODUCT_1 = "dating.hindbyte.com.iap1";    // Change to: yourdomain.com.iap1
    static final String ITEM_PRODUCT_2 = "dating.hindbyte.com.iap2";    // Change to: yourdomain.com.iap2
    static final String ITEM_PRODUCT_3 = "dating.hindbyte.com.iap3";    // Change to: yourdomain.com.iap3
    static final String ITEM_PRODUCT_4 = "android.test.purchased";  // Not edit! For testing - free purchases

    static final int ITEM_PRODUCT_1_AMOUNT = 300;// in usd cents | 3 usd = 300 cents
    static final int ITEM_PRODUCT_2_AMOUNT = 600;// in usd cents | 6 usd = 600 cents
    static final int ITEM_PRODUCT_3_AMOUNT = 900;// in usd cents | 9 usd = 900 cents

    Button mBuy1Button, mBuy2Button, mBuy3Button, mBuy4Button;

    private Boolean loading = false;

    private BillingClient mBillingClient;
    private final Map<String, ProductDetails> mProductDetailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
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
        mBuy4Button = findViewById(R.id.iap4_google_btn);// For test Google Pay Button

        if (!GOOGLE_PAY_TEST_BUTTON) {
            mBuy4Button.setVisibility(View.GONE);
        }

        mBuy1Button.setOnClickListener(v -> launchBilling(ITEM_PRODUCT_1));

        mBuy2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBilling(ITEM_PRODUCT_2);
            }
        });

        mBuy3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBilling(ITEM_PRODUCT_3);
            }
        });

        mBuy4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBilling(ITEM_PRODUCT_4);
            }
        });

        setupBilling();
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
                    // The BillingClient is ready. You can query purchases here.
                    queryProductDetails();
                    Log.e("onBillingSetupFinished", "WORKS");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e("onBillingSetupFinished", "NOT WORKS");
            }
        });
    }

    private void consume(String purchaseToken, String product) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build();
        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, @NonNull String outToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    // For example, increase the number of coins inside the user's basket.
                    switch (product) {

                        case ITEM_PRODUCT_1:
                            App.getInstance().setBalance(App.getInstance().getBalance() + 100);
                            payment(100, ITEM_PRODUCT_1_AMOUNT, PT_GOOGLE_PURCHASE,true);
                            break;
                        case ITEM_PRODUCT_2:
                            App.getInstance().setBalance(App.getInstance().getBalance() + 300);
                            payment(300, ITEM_PRODUCT_2_AMOUNT, PT_GOOGLE_PURCHASE,true);
                            break;
                        case ITEM_PRODUCT_3:
                            App.getInstance().setBalance(App.getInstance().getBalance() + 600);
                            payment(600, ITEM_PRODUCT_3_AMOUNT, PT_GOOGLE_PURCHASE,true);
                            break;
                        case ITEM_PRODUCT_4:
                            Log.e("Payment", "Call");
                            App.getInstance().setBalance(App.getInstance().getBalance() + 100);
                            payment(1000, 0, PT_GOOGLE_PURCHASE,true);
                            break;
                        default:
                            break;
                    }
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
        if (GOOGLE_PAY_TEST_BUTTON) {
            productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(ITEM_PRODUCT_4).setProductType(BillingClient.ProductType.INAPP).build());
        }

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


    public void payment(final int cost, final int amount, final int paymentType, final Boolean showSuccess) {
        loading = true;
        showpDialog();
        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PAYMENTS_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getBoolean("error")) {
                                if (response.has("balance")) {
                                    App.getInstance().setBalance(response.getInt("balance"));
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading = false;
                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("credits", Integer.toString(cost));
                params.put("paymentType", Integer.toString(paymentType));
                params.put("amount", Integer.toString(amount));
                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
    }

    //Tumor related glue-code
    private static final int REQUEST_CODE = 1234; // Can be anything

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void success() {
        Toast.makeText(BalanceActivity.this, getString(R.string.msg_success_purchase), Toast.LENGTH_SHORT).show();
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