package com.whytefarms.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koushikdutta.ion.Ion;
import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUPaymentParams;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.models.PayUCheckoutProConfig;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.WalletHistory;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.DateUtils;
import com.whytefarms.utils.Utils;
import com.whytefarms.firebaseModels.CustomerActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Date;

public class RechargeActivity extends BaseActivity implements PayUCheckoutProListener, PayUHashGenerationListener {


    private PayUPaymentParams.Builder builder;


    private View progressBar;

    private FirebaseFirestore database;

    private String name;
    private String email;
    private String phone;

    private String key;

    private String txnID;

    private Long currentWalletBalance;

    private String hub_name;
    private String delivery_exe_id;

    private String userCredentials;


    private TextInputLayout rechargeAmountLayout;

    private AppCompatTextView rechargeResult;

    private PayUCheckoutProConfig payUCheckoutProConfig;

    private int rechargeValue;

    private int rechargeAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.top_up_wallet);
        }

        database = FirebaseFirestore.getInstance();
        getCredentials();
        progressBar = findViewById(R.id.progress_bar);
        rechargeAmountLayout = findViewById(R.id.recharge_amount_layout);

        AppCompatTextView minReqd = findViewById(R.id.min_reqd);
        AppCompatTextView plus100 = findViewById(R.id.plus_100);
        AppCompatTextView plus500 = findViewById(R.id.plus_500);
        AppCompatTextView plus2000 = findViewById(R.id.plus_2000);
        AppCompatTextView rechargeNowBtn = findViewById(R.id.recharge_now_btn);
        AppCompatTextView cashCollectionBtn = findViewById(R.id.cash_collection_btn);

        rechargeResult = findViewById(R.id.recharge_result);


        payUCheckoutProConfig = new PayUCheckoutProConfig();

        payUCheckoutProConfig.setShowCbToolbar(false);
        payUCheckoutProConfig.setEnableSavedCard(true);


        builder = new PayUPaymentParams.Builder();
        builder.setIsProduction(AppConstants.APP_ENVIRONMENT.equals("_prod_"))
                .setProductInfo("Via Payment Gateway")
                .setSurl(AppConstants.PAYMENT_SUCCESS_URL)
                .setFurl(AppConstants.PAYMENT_FAILURE_URL);

        if (getIntent().getExtras() != null) {

            if (getIntent().getBooleanExtra("fromCart", false)) {
                minReqd.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.minimum_required_amount),
                        getIntent().getIntExtra("value", 500)));
            }

            minReqd.setVisibility(getIntent().getBooleanExtra("fromCart", false) ? View.VISIBLE : View.GONE);


            String rechargeType = getIntent().getStringExtra("type");
            rechargeValue = getIntent().getIntExtra("value", 500);
            rechargeAmount = rechargeValue;

            rechargeNowBtn.setText(R.string.recharge_now);
            rechargeNowBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(rechargeNowBtn.getContext(), R.drawable.ic_online_recharge), null, null, null);
            rechargeNowBtn.setOnClickListener(view -> generateHashAndStartPayment());

            cashCollectionBtn.setVisibility(View.VISIBLE);
            cashCollectionBtn.setText(R.string.cash_collection);
            cashCollectionBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(cashCollectionBtn.getContext(), R.drawable.ic_cash), null, null, null);
            cashCollectionBtn.setOnClickListener(view -> startCashCollectionActivity());

            if (rechargeAmountLayout != null && rechargeAmountLayout.getEditText() != null) {
                rechargeAmountLayout.getEditText().setText(String.format(Locale.getDefault(), "%d", rechargeValue));
            }
        }

        if (rechargeAmountLayout != null) {
            rechargeAmountLayout.requestFocus();
        }

        minReqd.setOnClickListener(view -> {
            if (rechargeAmountLayout != null && rechargeAmountLayout.getEditText() != null) {
                rechargeAmountLayout.getEditText().setText(String.format(Locale.getDefault(), "%d", rechargeValue));

                rechargeAmountLayout.getEditText().setSelection(rechargeAmountLayout.getEditText().getText().length());
            }

            rechargeAmount = rechargeValue;
        });

        plus100.setOnClickListener(view -> addValueToRechargeAmount(100));
        plus500.setOnClickListener(view -> addValueToRechargeAmount(500));
        plus2000.setOnClickListener(view -> addValueToRechargeAmount(2000));


        if (rechargeAmountLayout != null && rechargeAmountLayout.getEditText() != null) {
            rechargeAmountLayout.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence != null && charSequence.length() > 0) {
                        rechargeAmount = Integer.parseInt(charSequence.toString());
                        rechargeAmountLayout.getEditText().setSelection(charSequence.length());
                    } else {
                        rechargeAmount = rechargeValue;
                        rechargeAmountLayout.getEditText().setSelection(0);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        rechargeNowBtn.setText(R.string.recharge_now);
        rechargeNowBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(rechargeNowBtn.getContext(), R.drawable.ic_online_recharge), null, null, null);
        rechargeNowBtn.setOnClickListener(view -> generateHashAndStartPayment());

        cashCollectionBtn.setText(R.string.cash_collection);
        cashCollectionBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(cashCollectionBtn.getContext(), R.drawable.ic_cash), null, null, null);
        cashCollectionBtn.setOnClickListener(view -> startCashCollectionActivity());

        if (rechargeAmountLayout != null && rechargeAmountLayout.getEditText() != null) {
            rechargeAmountLayout.getEditText().setText(String.format(Locale.getDefault(), "%d", rechargeValue));
        }
    }

    private void addValueToRechargeAmount(int value) {
        if (rechargeAmountLayout != null && rechargeAmountLayout.getEditText() != null) {
            rechargeAmount = Integer.parseInt(rechargeAmountLayout.getEditText().getText().toString());
            rechargeAmount = rechargeAmount + value;

            rechargeAmountLayout.getEditText().setText(String.format(Locale.getDefault(), "%d", rechargeAmount));
        }
    }

    private void startCashCollectionActivity() {
        Intent intent = new Intent(this, CashCollectionActivity.class);
        intent.putExtra("amount", rechargeAmount);
        startActivity(intent);
    }

    private void processPayment(String payUResponse) {
        try {
            JSONObject jsonObject = new JSONObject(payUResponse);

            if (jsonObject.has("result")) {
                jsonObject = jsonObject.getJSONObject("result");
            }

            if (jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("success")) {

                WalletHistory walletHistory = new WalletHistory();

                walletHistory.amount = Long.parseLong(jsonObject.getString("amount").split("\\.")[0]);
                walletHistory.created_date = new Timestamp(DateUtils.getDateFromString(jsonObject.getString("addedon"), "yyyy-MM-dd HH:mm:ss", false));
                walletHistory.current_wallet_balance = currentWalletBalance + Math.round(Double.parseDouble(jsonObject.getString("amount")));
                walletHistory.customer_id = ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState();
                walletHistory.customer_name = name;
                walletHistory.customer_phone = phone;
                walletHistory.delivery_executive = delivery_exe_id;
                walletHistory.description = jsonObject.getString("mode") + ", PayU";
                walletHistory.hub_name = hub_name;
                walletHistory.reason = "Via Payment Gateway";
                walletHistory.source = "Online";
                walletHistory.status = "1";
                walletHistory.txn_id = jsonObject.getString("txnid");
                walletHistory.type = "Credit";
                walletHistory.user = "";

                database.collection(AppConstants.WALLET_HISTORY_COLLECTION)
                        .add(walletHistory)
                        .addOnSuccessListener(documentReference -> {
                            CustomerActivity customerActivity = new CustomerActivity();
                            customerActivity.action = "Payment";
                            customerActivity.created_date = new Timestamp(new Date());
                            customerActivity.customer_id = walletHistory.customer_id;
                            customerActivity.customer_name = walletHistory.customer_name;
                            customerActivity.customer_phone = walletHistory.customer_phone;
                            customerActivity.description = walletHistory.amount + " was added by " + walletHistory.customer_name;
                            customerActivity.object = "Payment Successful";
                            customerActivity.user = walletHistory.customer_name;
                            customerActivity.platform = "Android";
                            database.collection("customer_activities").add(customerActivity);
                                database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                                        .whereEqualTo("customer_id", ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState())
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (task1.getResult() != null && !task1.getResult().isEmpty()) {
                                                    Customer customer = task1.getResult().getDocuments().get(0).toObject(Customer.class);

                                                    if (customer != null) {
                                                        customer.wallet_balance = walletHistory.current_wallet_balance;

                                                        task1.getResult().getDocuments().get(0).getReference().set(customer)
                                                                .addOnCompleteListener(task2 -> {
                                                                    if (task2.isSuccessful()) {
                                                                        updateSubscriptions(customer.customer_id);
                                                                        Toast.makeText(RechargeActivity.this, R.string.recharge_successful, Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(RechargeActivity.this, R.string.recharge_failed, Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                        });

            } else if (jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("failure")) {
                WalletHistory walletHistory = new WalletHistory();

                walletHistory.amount = Long.parseLong(jsonObject.getString("amount").split("\\.")[0]);
                walletHistory.created_date = new Timestamp(DateUtils.getDateFromString(jsonObject.getString("addedon"), "yyyy-MM-dd HH:mm:ss", false));
                walletHistory.current_wallet_balance = currentWalletBalance + Long.parseLong(jsonObject.getString("amount").split("\\.")[0]);
                walletHistory.customer_id = ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState();
                walletHistory.customer_name = name;
                walletHistory.customer_phone = phone;
                walletHistory.delivery_executive = delivery_exe_id;
                walletHistory.description = "Failed to add";
                walletHistory.hub_name = hub_name;
                walletHistory.reason = "Via Payment Gateway";
                walletHistory.source = "Online";
                walletHistory.status = "0";
                walletHistory.txn_id = jsonObject.getString("txnid");
                walletHistory.type = "Credit";
                walletHistory.user = "";

                database.collection(AppConstants.WALLET_HISTORY_COLLECTION)
                        .add(walletHistory)
                        .addOnCompleteListener(task -> Toast.makeText(RechargeActivity.this, R.string.recharge_failed, Toast.LENGTH_SHORT).show());
            }
        } catch (JSONException e) {
            Toast.makeText(RechargeActivity.this, "Processing Payment Error1" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            Toast.makeText(RechargeActivity.this, "Processing Payment Error2" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void updateSubscriptions(String customerId) {
        database.collection("subscriptions_data")
                .whereEqualTo("customer_id", customerId)
                .whereEqualTo("reason", "cron")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String subscriptionId = document.getString("subscription_id");
                                document.getReference().update("status", "1", "reason", "")
                                        .addOnSuccessListener(aVoid -> {
                                            Customer  customerDetails = document.toObject(Customer.class);
                                            CustomerActivity customerActivity = new CustomerActivity();
                                            customerActivity.action = "Subscription Resumed";
                                            customerActivity.created_date = new Timestamp(new Date());
                                            customerActivity.customer_id = customerDetails.customer_id;
                                            customerActivity.customer_name = customerDetails.customer_name;
                                            customerActivity.customer_phone = customerDetails.customer_phone;
                                            customerActivity.description = "Subscription resumed for subscription Id: " + subscriptionId + " after wallet was recharged by android app";
                                            customerActivity.object = "Subscription Resumed";
                                            customerActivity.user = "";

                                            database.collection("customer_activities").add(customerActivity)
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(RechargeActivity.this, "Failed to add customer activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RechargeActivity.this, "Failed to update subscription: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(RechargeActivity.this, "Error getting subscriptions: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    private void getCredentials() {
        toggleProgressBar(true);
        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                            if (customer != null) {
                                name = customer.customer_name;
                                email = customer.customer_email;
                                phone = customer.customer_phone;
                                currentWalletBalance = customer.wallet_balance;
                                hub_name = customer.hub_name;
                                delivery_exe_id = customer.delivery_exe_id;

                                builder.setFirstName(name)
                                        .setEmail(email)
                                        .setPhone(phone);


                                txnID = "WFWR_" + Objects.requireNonNull(Utils.getSha256Hash("" + System.currentTimeMillis())).substring(15, 21);

                                // generateHashAndStartPayment();

                            } else {
                                rechargeResult.setText("error1");

                            }
                        }
                    } else {
                        rechargeResult.setText("error2");

                    }

                });

    }

    private void generateHashAndStartPayment() {
        toggleProgressBar(true);
        database.collection(AppConstants.SETTINGS_COLLECTION)
                .document(AppConstants.APP_OPS_DOCUMENT)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() &&
                            documentSnapshot.contains("app_key") &&
                            documentSnapshot.getString("app_key") != null &&
                            !Objects.equals(documentSnapshot.getString("app_key"), "")
                    ) {

                        key = documentSnapshot.getString("payU" + AppConstants.APP_ENVIRONMENT + "key");
                        userCredentials = key + ":" + ((WhyteFarmsApplication) getApplicationContext()).getUserHashFromLoginState().substring(31, 40);

                        Ion.with(RechargeActivity.this)
                                .load(AppConstants.GENERATE_STATIC_HASH_URL)
                                .setMultipartParameter("api_secret", documentSnapshot.getString("app_key"))
                                .setMultipartParameter("app_env", AppConstants.APP_ENVIRONMENT)
                                .setMultipartParameter("txnid", txnID)
                                .setMultipartParameter("amount", "" + rechargeAmount)
                                .setMultipartParameter("productinfo", "Via Payment Gateway")
                                .setMultipartParameter("firstname", name)
                                .setMultipartParameter("email", email)
                                .setMultipartParameter("user_credentials", userCredentials)
                                .asJsonObject()
                                .setCallback((e, result) -> {
                                    if (e == null) {
                                        try {
                                            JSONObject resultObj = new JSONObject(result.toString());

                                            if (resultObj.getString("code").equals("200")) {


                                                rechargeResult.setText(resultObj.toString(4));

                                                builder.setAmount("" + rechargeAmount)
                                                        .setKey(key)
                                                        .setPhone(phone)
                                                        .setTransactionId(txnID)
                                                        .setFirstName(name)
                                                        .setEmail(email)
                                                        .setSurl(AppConstants.PAYMENT_SUCCESS_URL)
                                                        .setFurl(AppConstants.PAYMENT_FAILURE_URL)
                                                        .setUserCredential(userCredentials);

                                                HashMap<String, Object> additionalParams = new HashMap<>();
                                                additionalParams.put(PayUCheckoutProConstants.CP_PAYMENT_HASH, resultObj.getString("payment_hash"));
                                                additionalParams.put(PayUCheckoutProConstants.CP_VAS_FOR_MOBILE_SDK, resultObj.getString("vas_for_mobile_sdk_hash"));
                                                additionalParams.put(PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, resultObj.getString("payment_related_details_for_mobile_sdk_hash"));

                                                builder.setAdditionalParams(additionalParams);
                                                toggleProgressBar(false);
                                                PayUCheckoutPro.open(
                                                        RechargeActivity.this,
                                                        builder.build(),
                                                        payUCheckoutProConfig,
                                                        this);


                                            } else {
                                                Toast.makeText(RechargeActivity.this, "Some Error in starting payment. Please try again later.", Toast.LENGTH_SHORT).show();
                                                toggleProgressBar(false);
                                            }

                                        } catch (JSONException ignored) {
                                            toggleProgressBar(false);
                                        }
                                    } else {
                                        rechargeResult.setText(String.format("error3: %s", e.getMessage() + " : " + (result != null ? result.toString() : "empty_result")));
                                        toggleProgressBar(false);
                                    }
                                });
                    } else {
                        rechargeResult.setText("error4");
                        toggleProgressBar(false);
                    }

                });
    }


    @Override
    public void generateHash(@NonNull HashMap<String, String> hashMap, @NonNull PayUHashGenerationListener payUHashGenerationListener) {

        String hashName = hashMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
        String hashData = hashMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
            database.collection(AppConstants.SETTINGS_COLLECTION)
                    .document(AppConstants.APP_OPS_DOCUMENT)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() &&
                                documentSnapshot.contains("app_key") &&
                                documentSnapshot.getString("app_key") != null &&
                                !Objects.equals(documentSnapshot.getString("app_key"), "")) {

                            Ion.with(RechargeActivity.this)
                                    .load(AppConstants.GENERATE_DYNAMIC_HASH_URL)
                                    .setMultipartParameter("api_secret", documentSnapshot.getString("app_key"))
                                    .setMultipartParameter("app_env", AppConstants.APP_ENVIRONMENT)
                                    .setMultipartParameter("hashData", hashData)
                                    .asJsonObject()
                                    .setCallback((e, result) -> {
                                        if (e == null) {
                                            try {
                                                JSONObject resultObj = new JSONObject(result.toString());

                                                if (resultObj.getString("code").equals("200")) {

                                                    String hash = resultObj.getString("hashString");
                                                    HashMap<String, String> dataMap = new HashMap<>();
                                                    dataMap.put(hashName, hash);
                                                    payUHashGenerationListener.onHashGenerated(dataMap);
                                                } else {
                                                    Toast.makeText(RechargeActivity.this, "Some Error in starting payment. Please try again later." + resultObj, Toast.LENGTH_SHORT).show();
                                                }

                                            } catch (JSONException ignored) {
                                            }
                                        } else {
                                            rechargeResult.setText(String.format("error5: %s", e.getMessage() + " : " + (result != null ? result.toString() : "empty_result")));
                                        }
                                    });
                        } else {
                            rechargeResult.setText("error6");
                        }

                        toggleProgressBar(false);
                    });

        }
    }


    @Override
    public void onPaymentSuccess(@NonNull Object response) {
        //Cast response object to HashMap
        //noinspection unchecked
        HashMap<String, Object> result = (HashMap<String, Object>) response;

        String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
        String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);


        processPayment(payuResponse);
    }

    @Override
    public void onPaymentFailure(@NonNull Object response) {
        //Cast response object to HashMap
        //noinspection unchecked
        HashMap<String, Object> result = (HashMap<String, Object>) response;
        String payuResponse = (String) result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
        String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);

        processPayment(payuResponse);
    }

    @Override
    public void onPaymentCancel(boolean isTxnInitiated) {
        Toast.makeText(RechargeActivity.this, R.string.payment_cancelled, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(ErrorResponse errorResponse) {
        String errorMessage = errorResponse.getErrorMessage();
        rechargeResult.setText(errorMessage);
    }


    @Override
    public void onHashGenerated(@NonNull HashMap<String, String> hashMap) {
    }

    @Override
    public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {


    }
}