package com.whytefarms.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.R;
import com.whytefarms.utils.AppConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;


public class TextActivity extends BaseActivity {

    private FirebaseFirestore database;
    private WebView webView;
    private FrameLayout progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
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

        if (getIntent().getExtras() != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(
                    Objects.requireNonNull(getIntent().getExtras().getString("what")).equals("help_n_support") ?
                            "FAQs" : "Legal & About Us");
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
                            Log.d("TAG", "loadDetails: " + text);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(Throwable::printStackTrace);
    }
}
