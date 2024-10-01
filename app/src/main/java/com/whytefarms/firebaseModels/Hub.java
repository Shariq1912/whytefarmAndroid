package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;


@Keep
public class Hub {
    public String address;
    public String city;
    public Timestamp created_date;
    public String hub_name;
    public String mobile_no;
    public String state;
    public String status;
    public Timestamp updated_date;
}
