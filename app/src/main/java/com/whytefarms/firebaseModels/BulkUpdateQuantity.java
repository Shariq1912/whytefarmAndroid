package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;


@Keep
public class BulkUpdateQuantity {

    public String customer_id;
    public Timestamp date;
    public String delivery_date;
    public String product_name;
    public Long quantity;
    public String status;
    public String subscription_id;

    public BulkUpdateQuantity() {
    }
}
