package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;


@Keep
public class CustomerActivity {

    public Timestamp date;
    public Timestamp created_date;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public String customer_address;
    public String action;
    public String description;
    public String hub_name;
    public String object;
    public String user;
    public String platform;

    public CustomerActivity() {

    }
}
