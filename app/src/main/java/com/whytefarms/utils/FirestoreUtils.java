package com.whytefarms.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreUtils {

    public interface FirestoreCallback {
        void onSuccess(Map<String, String> userData);
        void onFailure(Exception e);
    }

    public static void getDeliveryExecutiveDetails(String hubUserId, FirestoreCallback callback) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(AppConstants.HUBS_USERS_COLLECTION)
                .whereEqualTo("hub_user_id", hubUserId)
                .whereEqualTo("role", "Delivery Executive")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        Map<String, String> userData = new HashMap<>();
                        userData.put("first_name", document.getString("first_name"));
                        userData.put("last_name", document.getString("last_name"));
                        userData.put("phone_no", document.getString("phone_no"));
                        callback.onSuccess(userData);
                    } else {
                        callback.onSuccess(null); // No matching data found
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}

