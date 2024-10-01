package com.whytefarms.interfaces;

public interface FirebaseFirestoreResultListener {

    void onResultReceived(Object result, String resultType, String action);

    void onError(Throwable error);

}
