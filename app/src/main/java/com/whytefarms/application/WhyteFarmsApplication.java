package com.whytefarms.application;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class WhyteFarmsApplication extends Application {
    private AppCompatActivity mCurrentActivity = null;


    public AppCompatActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(AppCompatActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }


    public void saveLoginStateToLocal(String customerID, String userHash) {
        SharedPreferences sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("customerID", customerID);
        editor.putString("userHash", userHash);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return (getCustomerIDFromLoginState() != null &&
                !getCustomerIDFromLoginState().isEmpty()) &&
                (getUserHashFromLoginState() != null &&
                        !getUserHashFromLoginState().isEmpty());
    }

    public String getCustomerIDFromLoginState() {
        return getSharedPreferences("userDetails", MODE_PRIVATE).getString("customerID", null);
    }

    public String getUserHashFromLoginState() {
        return getSharedPreferences("userDetails", MODE_PRIVATE).getString("userHash", null);
    }
}
