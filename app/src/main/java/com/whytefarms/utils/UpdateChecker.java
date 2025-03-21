package com.whytefarms.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.models.AppUpdate;

public class UpdateChecker {
    private static final String TAG = "UpdateChecker";
    private final Activity activity;
    private final FirebaseFirestore db;

    public UpdateChecker(Activity activity) {
        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
    }

    public void checkForUpdates(UpdateCheckListener listener) {
        String currentVersion = getCurrentVersion();
        Log.d(TAG, "Current installed version: " + currentVersion);
        
        db.collection("app_updates")
                .document("android")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Raw Firestore data: " + documentSnapshot.getData());
                        
                        String minVersion = documentSnapshot.getString("min_supported_version");
                        String message = documentSnapshot.getString("message");
                        Boolean mandatory = documentSnapshot.getBoolean("mandatory");
                        String downloadUrl = documentSnapshot.getString("download_url");
                        
                        Log.d(TAG, "Directly accessed min_supported_version: " + minVersion);
                        
                        if (minVersion != null) {
                            boolean updateRequired = isUpdateRequired(currentVersion, minVersion);
                            
                            // Show dialog only if mandatory OR if update is required
                            if (mandatory == Boolean.TRUE ) {
                                AppUpdate update = new AppUpdate();
                                update.setMinSupportedVersion(minVersion);
                                update.setMessage(message != null ? message : "Please update to continue using the app.");
                                update.setMandatory(updateRequired || (mandatory != null && mandatory)); // Force mandatory if version is old
                                update.setDownloadUrl(downloadUrl != null ? downloadUrl : 
                                    "https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                                
                                listener.onUpdateRequired(update);
                            } else {
                                listener.onUpdateNotRequired();
                            }
                        } else {
                            listener.onUpdateNotRequired();
                        }
                    } else {
                        Log.d(TAG, "No update document found");
                        listener.onUpdateNotRequired();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for updates", e);
                    listener.onError(e.getMessage());
                });
    }

    private String getCurrentVersion() {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting package info", e);
            return "0.0.0";
        }
    }

    private boolean isUpdateRequired(String currentVersion, String minVersion) {
        if (currentVersion == null || minVersion == null) {
            Log.e(TAG, "Version comparison failed - null values detected");
            return false;
        }

        try {
            String[] current = currentVersion.split("\\.");
            String[] minimum = minVersion.split("\\.");

            for (int i = 0; i < Math.min(current.length, minimum.length); i++) {
                int currentPart = Integer.parseInt(current[i]);
                int minimumPart = Integer.parseInt(minimum[i]);

                if (currentPart < minimumPart) return true;
                if (currentPart > minimumPart) return false;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error comparing versions", e);
            return false;
        }
    }

    public interface UpdateCheckListener {
        void onUpdateRequired(AppUpdate update);
        void onUpdateNotRequired();
        void onError(String error);
    }
} 