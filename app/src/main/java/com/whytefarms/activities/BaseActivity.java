package com.whytefarms.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.utils.AppConstants;

import java.util.Locale;


public class BaseActivity extends AppCompatActivity {
    protected WhyteFarmsApplication whyteFarmsApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setLocale();
        whyteFarmsApplication = (WhyteFarmsApplication) this.getApplicationContext();
    }

    protected void checkVacation() {
        if (((WhyteFarmsApplication) getApplication()).isLoggedIn()) {
            View vacationsMode = findViewById(R.id.vacation_mode_layout);
            FirebaseFirestore.getInstance()
                    .collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                    .where(Filter.and(Filter.equalTo("customer_id", ((WhyteFarmsApplication) getApplication()).getCustomerIDFromLoginState()),
                            Filter.lessThanOrEqualTo("start_date", Timestamp.now()),
                            Filter.greaterThanOrEqualTo("end_date", Timestamp.now())))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (vacationsMode != null) {
                            vacationsMode.setVisibility(task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });

        }
    }

    protected void onResume() {
        super.onResume();
        whyteFarmsApplication.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        AppCompatActivity currentActivity = whyteFarmsApplication.getCurrentActivity();
        if (this.equals(currentActivity)) {
            whyteFarmsApplication.setCurrentActivity(null);
        }
    }

    private void setLocale() {
        Locale locale = new Locale(getCurrentLocale());
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void setTheme() {
        AppCompatDelegate.setDefaultNightMode(getCurrentTheme().equals("light") ?
                AppCompatDelegate.MODE_NIGHT_NO : getCurrentTheme().equals("dark") ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }


    public String getCurrentTheme() {
        return getSharedPreferences("theme", MODE_PRIVATE).getString("themeCode", "system");
    }


    public String getCurrentLocale() {
        return getSharedPreferences("locale", MODE_PRIVATE).getString("localeCode", "en");
    }

    public void startLoginFlow(String continueActivity) {
        SharedPreferences pref1 = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref1.edit();
        editor.clear();
        editor.apply();


        Intent intent;

        switch (continueActivity) {
            case "MainActivity":
                intent = new Intent(getApplicationContext(), MainActivity.class);
                break;

            case "CheckoutActivity":
                intent = new Intent(getApplicationContext(), CheckoutActivity.class);
                break;

            case "LoginActivity":
            default:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
