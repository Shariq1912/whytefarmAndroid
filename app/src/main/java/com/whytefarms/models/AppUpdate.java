package com.whytefarms.models;

public class AppUpdate {
    private String current_version;
    private String download_url;
    private boolean mandatory;
    private String message;
    private String min_supported_version;

    public AppUpdate() {
        // Empty constructor needed for Firestore
    }

    public String getCurrentVersion() {
        return current_version;
    }

    public void setCurrentVersion(String currentVersion) {
        this.current_version = currentVersion;
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.download_url = downloadUrl;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMinSupportedVersion() {
        return min_supported_version;
    }

    public void setMinSupportedVersion(String minSupportedVersion) {
        this.min_supported_version = minSupportedVersion;
    }
} 