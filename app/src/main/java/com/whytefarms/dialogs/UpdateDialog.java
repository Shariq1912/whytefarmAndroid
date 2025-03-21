package com.whytefarms.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.whytefarms.models.AppUpdate;

public class UpdateDialog {
    private static final String TAG = "UpdateDialog";
    private final Activity activity;

    public UpdateDialog(Activity activity) {
        this.activity = activity;
    }

    public void show(AppUpdate update) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Update Required")
                .setMessage(update.getMessage())
                .setCancelable(false)
                .setPositiveButton("Update Now", (dialog, which) -> {
                    try {
                        String url = update.getDownloadUrl();
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        
                        if (update.isMandatory()) {
                            activity.finish();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening Play Store", e);
                        try {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + activity.getPackageName())));
                            if (update.isMandatory()) {
                                activity.finish();
                            }
                        } catch (android.content.ActivityNotFoundException anfe) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
                            if (update.isMandatory()) {
                                activity.finish();
                            }
                        }
                    }
                });

        if (!update.isMandatory()) {
            builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());
        }

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
} 