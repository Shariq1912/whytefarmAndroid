package com.whytefarms.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends BaseActivity {

    private String generatedOtp = "";
    private FrameLayout progressBar;
    private AppCompatTextView loginResult;
    private TextInputLayout otpLayoutInput;
    private TextInputLayout phoneLayoutInput;
    private FirebaseFirestore database;
    private LinearLayout phoneLayout;
    private LinearLayout otpLayout;

    private AppCompatTextView resendOtp;
    private AppCompatTextView resendOtpCountdown;

    private String phoneNo = "";

    @NonNull
    private static JSONObject getJsonObject(HttpURLConnection conn) throws IOException, JSONException {
        final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        final StringBuilder stringBuffer = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            stringBuffer.append(line);
        }
        rd.close();


        return new JSONObject(stringBuffer.toString());
    }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

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
            getSupportActionBar().setTitle(R.string.login);

        }

        progressBar = findViewById(R.id.progress_bar);
        phoneLayoutInput = findViewById(R.id.phone_layout_input);
        otpLayoutInput = findViewById(R.id.otp_layout_input);

        phoneLayout = findViewById(R.id.phone_layout);
        otpLayout = findViewById(R.id.otp_layout);


        Button generateOtpButton = findViewById(R.id.generate_otp_btn);

        Button submitOtpButton = findViewById(R.id.submit_otp_btn);

        loginResult = findViewById(R.id.login_result);

        resendOtp = findViewById(R.id.resend_otp);
        resendOtpCountdown = findViewById(R.id.resend_otp_countdown);

        resendOtp.setPaintFlags(resendOtp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        phoneLayoutInput.requestFocus();


        database = FirebaseFirestore.getInstance();

        generateOtpButton.setOnClickListener(view -> sendOtp());

        submitOtpButton.setOnClickListener(view -> checkOtp(generatedOtp));

        resendOtp.setOnClickListener(view -> sendOtp());

    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }


    private void checkOtp(String generatedOtp) {
        runOnUiThread(() -> {
            toggleProgressBar(true);

            if (otpLayoutInput.getEditText() != null && otpLayoutInput.getEditText().getText() != null) {
                String enteredOtp = otpLayoutInput.getEditText().getText().toString();

                if (!enteredOtp.isEmpty()) {
                    if (enteredOtp.length() == 4 && generatedOtp.equals(enteredOtp)) {
                        showLoginResult(getString(R.string.otp_matched), true);

                        savePreferencesAndLoginUser();
                    } else {
                        toggleProgressBar(false);
                        showLoginResult(getString(R.string.invalid_otp_error), false);
                    }
                } else {
                    toggleProgressBar(false);
                    showLoginResult(getString(R.string.empty_otp_error), false);
                }
            } else {
                toggleProgressBar(false);
                showLoginResult(getString(R.string.invalid_otp_error), false);
            }
        });
    }

    private void savePreferencesAndLoginUser() {

        database.collection(AppConstants.SETTINGS_COLLECTION)
                .document(AppConstants.APP_OPS_DOCUMENT)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.getString("app_key") != null) {

                            if (!phoneNo.isEmpty()) {
                                String appKey = documentSnapshot.getString("app_key");
                                String userHash = Utils.getSha256Hash(phoneNo + "::::" + appKey);

                                database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                                        .where(Filter.equalTo("customer_phone", phoneNo))
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                createOrUpdateUserAndAllowLogin(userHash, null);
                                            } else {
                                                Customer customer = queryDocumentSnapshots.getDocuments().get(0).toObject(Customer.class);

                                                if (customer != null) {
                                                    if (customer.customer_phone.equals(phoneNo)) {
                                                        if (customer.userHash.equals(userHash)) {
                                                            ((WhyteFarmsApplication) getApplicationContext()).saveLoginStateToLocal(customer.customer_id, customer.userHash);
                                                            moveToRequestedActivity();
                                                        } else {
                                                            createOrUpdateUserAndAllowLogin(userHash, customer);
                                                        }
                                                    } else {
                                                        createOrUpdateUserAndAllowLogin(userHash, null);
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    } else {
                        toggleProgressBar(false);
                        showLoginResult(getString(R.string.login_failed_error), false);
                    }
                });


    }

    private void moveToRequestedActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }


    private void createOrUpdateUserAndAllowLogin(String hash, @Nullable Customer existingCustomer) {

        if (existingCustomer == null) {
            Customer customer = new Customer("",
                    new Timestamp(new Date(0)),
                    Timestamp.now(),
                    0L,
                    "",
                    "",
                    "",
                    "",
                    AppConstants.DEFAULT_USER_PROFILE_IMAGE,
                    "User",
                    phoneNo,
                    false,
                    "",
                    new Timestamp(new Date(0)),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "android",
                    "",
                    Timestamp.now(),
                    "1",
                    Timestamp.now(),
                    0L,
                    hash,
                    AppConstants.ADDRESS_TYPE_HOME,
                    "",
                    ""
            );
            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION).add(customer)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ((WhyteFarmsApplication) getApplicationContext()).saveLoginStateToLocal(customer.customer_id, hash);
                            moveToRequestedActivity();
                        }
                    });

        } else {
            existingCustomer.userHash = hash;
            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                    .whereEqualTo("customer_id", existingCustomer.customer_id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            queryDocumentSnapshots.getDocuments().get(0).getReference().set(existingCustomer)
                                    .addOnSuccessListener(unused -> {
                                        ((WhyteFarmsApplication) getApplicationContext()).saveLoginStateToLocal(existingCustomer.customer_id, hash);
                                        moveToRequestedActivity();
                                    });
                        }
                    });
        }
    }

    private void startCountdownToResend() {

        runOnUiThread(() -> {
            resendOtpCountdown.setVisibility(View.VISIBLE);
            resendOtp.setVisibility(View.GONE);
        });


        runOnUiThread(
                () -> {
                    CountDownTimer timer = new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            runOnUiThread(() -> {
                                resendOtpCountdown.setText(String.format(getString(R.string.resend_otp_countdown), millisUntilFinished / 1000));
                            });
                        }

                        @Override
                        public void onFinish() {
                            runOnUiThread(() -> {
                                resendOtpCountdown.setVisibility(View.GONE);
                                resendOtp.setVisibility(View.VISIBLE);
                            });
                        }
                    };
                    timer.start();
                });
    }


    private void sendOtp() {
        if (phoneLayoutInput.getEditText() != null && phoneLayoutInput.getEditText().getText() != null) {
            toggleProgressBar(true);
            phoneLayoutInput.clearFocus();
            phoneNo = phoneLayoutInput.getEditText().getText().toString();

            if (phoneNo.length() == 10 && isValidMobile(phoneNo)) {
                database.collection(AppConstants.SETTINGS_COLLECTION).document(AppConstants.APP_OPS_DOCUMENT)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                generatedOtp = String.format(Locale.US, "%04d", generateOtp());


                                try {
                                    String data = "apikey=" +
                                            documentSnapshot.getString("txt_local_api_key") +
                                            "&sender=" + documentSnapshot.getString("txt_local_sender") +
                                            "&numbers=91" + phoneNo + "&test=" + (AppConstants.APP_ENVIRONMENT.equals("_test_") ? "1" : "0") + "&message=" +
                                            URLEncoder.encode(String.format(getString(R.string.txt_local_message_placeholder),
                                                    generatedOtp,
                                                    documentSnapshot.getString("support_phone"),
                                                    documentSnapshot.getString("support_toll_free")), "UTF-8");


                                    if (AppConstants.APP_ENVIRONMENT.equals("_test_")) {
                                        Log.d("TAG", "sending OTP: " + data);
                                    }


                                    Executor executor = Executors.newSingleThreadExecutor();

                                    executor.execute(() -> {
                                        try {

                                            HttpURLConnection conn = (HttpURLConnection) new URL(AppConstants.txtLocalURL).openConnection();
                                            conn.setDoOutput(true);
                                            conn.setRequestMethod("POST");
                                            conn.setRequestProperty("Content-Length", Long.toString(data.getBytes(StandardCharsets.UTF_8).length));
                                            conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));


                                            JSONObject result = getJsonObject(conn);

                                            if (result.get("status").toString().equals("success")) {
                                                runOnUiThread(() -> {
                                                    showLoginResult(getString(R.string.otp_sent_successfully), true);
                                                    phoneLayout.setVisibility(View.GONE);
                                                    phoneLayoutInput.clearFocus();
                                                    otpLayout.setVisibility(View.VISIBLE);
                                                    otpLayoutInput.requestFocus();
                                                    toggleProgressBar(false);
                                                });
                                            } else {
                                                // Log.d("TAG", result.toString());
                                                runOnUiThread(() -> {
                                                    showLoginResult(getString(R.string.otp_send_error) + "[Error Code: 1]", false);
                                                    toggleProgressBar(false);
                                                });
                                            }
                                        } catch (Exception e) {
                                            runOnUiThread(() -> {
                                                showLoginResult(getString(R.string.otp_send_error) + "[Error Code: 2]" + e.getMessage(), false);
                                                toggleProgressBar(false);
                                            });

                                        } finally {
                                            startCountdownToResend();
                                        }
                                    });
                                } catch (UnsupportedEncodingException e) {
                                    showLoginResult(getString(R.string.otp_send_error) + "[Error Code: 3]" + e.getMessage(), false);
                                    toggleProgressBar(false);
                                }
                            }
                        });
            } else {
                toggleProgressBar(false);
                showLoginResult("Invalid Phone Number", false);
                phoneLayoutInput.requestFocus();
            }

        } else {
            toggleProgressBar(false);
            showLoginResult("Invalid Phone Number", false);
            phoneLayoutInput.requestFocus();
        }
    }


    private void showLoginResult(String result, boolean isSuccessful) {
        loginResult.setVisibility(View.VISIBLE);
        loginResult.setText(result);

        loginResult.setBackgroundColor(isSuccessful ?
                ContextCompat.getColor(LoginActivity.this, R.color.green) :
                ContextCompat.getColor(LoginActivity.this, android.R.color.holo_red_dark));

        new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                runOnUiThread(() -> {
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);

                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            loginResult.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    loginResult.setAnimation(animation);
                    loginResult.animate();
                });
            }
        }.start();

    }

    private int generateOtp() {
        return new Random().nextInt(9999);
    }
}