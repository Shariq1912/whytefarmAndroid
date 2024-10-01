package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.whytefarms.utils.AppConstants;

import java.util.Date;
import java.util.Locale;

@Keep
public class Customer {
    public String alt_phone = "";
    public Object anniversary_date = Timestamp.now();
    public Timestamp created_date = Timestamp.now();
    public Long credit_limit = 0L;
    public String customer_address = "";

    public String customer_category = "";

    public String customer_email = "";
    public String customer_id = "";

    public String customer_image = AppConstants.DEFAULT_USER_PROFILE_IMAGE;

    public String customer_name = "";
    public String customer_phone = "";
    public Boolean customer_type = false;

    public String delivery_exe_id = "";

    public Object dob;

    public String flat_villa_no = "";

    public String floor = "";

    public String gender = "";

    public String hub_name = "";
    public String landmark = "";
    public String latitude = "";
    public String location = "";

    public String longitude = "";

    public String platform = "android";

    public String referral_code = "";
    public Timestamp registered_date = Timestamp.now();

    public String status = "1";

    public Timestamp updated_date = Timestamp.now();

    public Long wallet_balance = 0L;

    public String userHash = "";
    public String addressType = AppConstants.ADDRESS_TYPE_HOME;

    public String pincode = "";
    public String source = "";


    public Customer() {

    }

    public Customer(String alt_phone,
                    Object anniversary_date,
                    Timestamp created_date,
                    Long credit_limit,
                    String customer_address,
                    String customer_category,
                    String customer_email,
                    String customer_id,
                    String customer_image,
                    String customer_name,
                    String customer_phone,
                    Boolean customer_type,
                    String delivery_exe_id,
                    Object dob,
                    String flat_villa_no,
                    String floor,
                    String gender,
                    String hub_name,
                    String landmark,
                    String latitude,
                    String location,
                    String longitude,
                    String platform,
                    String referral_code,
                    Timestamp registered_date,
                    String status,
                    Timestamp updated_date,
                    Long wallet_balance,
                    String userHash,
                    String addressType,
                    String pincode,
                    String source
                    ) {
        this.alt_phone = alt_phone;
        this.anniversary_date = anniversary_date;
        this.created_date = created_date;
        this.credit_limit = credit_limit;
        this.customer_address = customer_address;
        this.customer_category = customer_category;
        this.customer_email = customer_email;
        this.customer_id = customer_id.isEmpty() ? generateCustomerID() : customer_id;
        this.customer_image = customer_image;
        this.customer_name = customer_name;
        this.customer_phone = customer_phone;
        this.customer_type = customer_type;
        this.delivery_exe_id = delivery_exe_id;
        this.dob = dob;
        this.flat_villa_no = flat_villa_no;
        this.floor = floor;
        this.gender = gender;
        this.hub_name = hub_name;
        this.landmark = landmark;
        this.latitude = latitude;
        this.location = location;
        this.longitude = longitude;
        this.platform = platform;
        this.referral_code = referral_code;
        this.registered_date = registered_date;
        this.status = status;
        this.updated_date = updated_date;
        this.wallet_balance = wallet_balance;
        this.userHash = userHash;
        this.addressType = addressType;
        this.pincode = pincode;
        this.source = source;
    }

    private String generateCustomerID() {
        Date timestamp = new Date();
        String random4Digits = String.format(Locale.US, "%04d", (int) Math.floor(Math.random() * 10000));

        return String.format(Locale.US, "%04d", timestamp.getTime() % 10000) + random4Digits;
    }

}
