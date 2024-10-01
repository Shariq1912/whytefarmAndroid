package com.whytefarms.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.R;
import com.whytefarms.utils.AppConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;


public class HelpActivity extends BaseActivity {

    private FirebaseFirestore database;
    private WebView webView;
    private FrameLayout progressBar;

    private CardView call;
    private CardView whatsapp;
    private CardView email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
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
        }

        database = FirebaseFirestore.getInstance();

        webView = findViewById(R.id.web_view);

        progressBar = findViewById(R.id.progress_bar);

        call = findViewById(R.id.call);
        whatsapp = findViewById(R.id.whatsapp);
        email = findViewById(R.id.email);


        if (getIntent().getExtras() != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(
                    Objects.requireNonNull(getIntent().getExtras().getString("what")).equals("help_n_support") ?
                            R.string.help_n_support : R.string.legal_n_about_us);
            loadDetails(getIntent().getExtras().getString("what"));
        }
    }

    private void loadDetails(String what) {
        progressBar.setVisibility(View.VISIBLE);
        database.collection(AppConstants.SETTINGS_COLLECTION)
                .document(AppConstants.TEXT_DOCUMENT)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();

                        if (snapshot.contains(what)) {
                            String text = snapshot.getString(what);
                            if (text != null) {
                                try {
                                    webView.loadData(URLEncoder.encode(text, "utf-8").replaceAll("\\+", " "),
                                            "text/html", "utf-8");
                                } catch (UnsupportedEncodingException e) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }

                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(Throwable::printStackTrace);


        database.collection(AppConstants.SETTINGS_COLLECTION)
                .document(AppConstants.APP_OPS_DOCUMENT)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();

                        if (snapshot.contains("support_phone")) {
                            String tollFree = snapshot.getString("support_phone");
                            if (tollFree != null) {
                                call.setOnClickListener(v -> {
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                    callIntent.setData(Uri.parse("tel:" + tollFree));
                                    startActivity(callIntent);
                                });

                                whatsapp.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/+91" + snapshot.getString("support_phone")))));

                                email.setOnClickListener(v -> {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_SENDTO);
                                    intent.setData(Uri.parse("mailto:" + snapshot.getString("support_email")));
                                    startActivity(intent);
                                });
                            }
                        }
                    }
                }).addOnFailureListener(Throwable::printStackTrace);
    }
}
