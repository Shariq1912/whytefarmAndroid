package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.whytefarms.fastModels.Subscription;
import com.whytefarms.utils.AppConstants;

@Keep
public class FirebaseSubscription {

    public String coupon_code = "";
    public Timestamp created_date;
    public String customer_address;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public String delivered_by;
    public String delivering_to;
    public Object end_date; //Mitigating for possibility of end_date being either Timestamp or String
    public Long sunday = 0L;
    public Long monday = 0L;
    public Long tuesday = 0L;
    public Long wednesday = 0L;
    public Long thursday = 0L;
    public Long friday = 0L;
    public Long saturday = 0L;
    public String hub_name;
    public Object interval = 1L;
    public String next_delivery_date = "";
    public String package_unit;
    public Long price;
    public String product_name;
    public Long quantity = 1L;
    public Timestamp resume_date;
    public Timestamp start_date;
    public String status = "0";
    public String subscription_id;
    public String subscription_type;
    public Timestamp updated_date = Timestamp.now();
    public String reason = "";
    public FirebaseSubscription() {

    }


    @Keep
    public static FirebaseSubscription getFirebaseSubscription(Subscription subscription) {

        FirebaseSubscription firebaseSubscription = new FirebaseSubscription();

        firebaseSubscription.coupon_code = subscription.coupon_code;
        firebaseSubscription.created_date = subscription.created_date;
        firebaseSubscription.customer_address = subscription.customer_address;
        firebaseSubscription.customer_id = subscription.customer_id;
        firebaseSubscription.customer_name = subscription.customer_name;
        firebaseSubscription.customer_phone = subscription.customer_phone;
        firebaseSubscription.delivered_by = subscription.delivered_by;
        firebaseSubscription.delivering_to = subscription.delivering_to;
        firebaseSubscription.end_date = subscription.end_date;
        if (subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
            firebaseSubscription.sunday = subscription.sunday;
            firebaseSubscription.monday = subscription.monday;
            firebaseSubscription.tuesday = subscription.tuesday;
            firebaseSubscription.wednesday = subscription.wednesday;
            firebaseSubscription.thursday = subscription.thursday;
            firebaseSubscription.friday = subscription.friday;
            firebaseSubscription.saturday = subscription.saturday;
        } else {
            firebaseSubscription.sunday = 0L;
            firebaseSubscription.monday = 0L;
            firebaseSubscription.tuesday = 0L;
            firebaseSubscription.wednesday = 0L;
            firebaseSubscription.thursday = 0L;
            firebaseSubscription.friday = 0L;
            firebaseSubscription.saturday = 0L;
        }

        firebaseSubscription.hub_name = subscription.hub_name;
        firebaseSubscription.interval = subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_EVERYDAY) ?
                Long.valueOf(1L) : subscription.interval;
        firebaseSubscription.next_delivery_date = subscription.next_delivery_date;
        firebaseSubscription.package_unit = subscription.package_unit;
        firebaseSubscription.price = subscription.price;
        firebaseSubscription.product_name = subscription.product_name;
        firebaseSubscription.quantity = subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE) ?
                Long.valueOf(0L) : subscription.quantity;
        firebaseSubscription.resume_date = subscription.resume_date;
        firebaseSubscription.start_date = subscription.start_date;
        firebaseSubscription.status = subscription.status;
        firebaseSubscription.subscription_id = subscription.subscription_id;
        firebaseSubscription.subscription_type = subscription.subscription_type;
        firebaseSubscription.reason = subscription.reason;
        return firebaseSubscription;
    }
}
