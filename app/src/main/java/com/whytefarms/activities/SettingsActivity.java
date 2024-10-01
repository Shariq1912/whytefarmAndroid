package com.whytefarms.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.whytefarms.R;

public class SettingsActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

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
            getSupportActionBar().setTitle(R.string.app_settings);
        }

        CardView langEn = findViewById(R.id.lang_en);
        CardView langHi = findViewById(R.id.lang_hi);

        langEn.setOnClickListener(view -> changeLocale("en"));
        langHi.setOnClickListener(view -> changeLocale("hi"));

        CardView themeLight = findViewById(R.id.theme_light);
        CardView themeDark = findViewById(R.id.theme_dark);
        CardView themeSystem = findViewById(R.id.theme_system);

        themeLight.setOnClickListener(view -> changeTheme("light"));
        themeDark.setOnClickListener(view -> changeTheme("dark"));
        themeSystem.setOnClickListener(view -> changeTheme("system"));

    }

    private void changeLocale(String languageCode) {
        SharedPreferences pref = getSharedPreferences("locale", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("localeCode", languageCode);

        editor.apply();

        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finishAffinity();
    }


    private void changeTheme(String themeCode) {
        SharedPreferences pref = getSharedPreferences("theme", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("themeCode", themeCode);

        editor.apply();

        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finishAffinity();
    }
}
