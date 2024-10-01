package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;

@Keep
public class DeliveryPreference {
    public String additional_instruction = "";
    public Timestamp created_date;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public String delivery_mode;
    public String status;
    public Timestamp updated_date;


    public DeliveryPreference() {

    }

}
