package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;

@Keep
public class Location {
    public Timestamp created_date;
    public String delivery_exe_id;
    public String hub_name;
    public String location;
    public String location_id;
    public String route;
    public String status;
    public Timestamp updated_date;
}
