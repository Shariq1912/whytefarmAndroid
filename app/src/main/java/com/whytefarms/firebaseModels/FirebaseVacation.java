package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.whytefarms.fastModels.Vacation;


@Keep
public class FirebaseVacation {
    public Timestamp created_date;
    public String vacation_id;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public Timestamp end_date;
    public String hub_name;
    public Timestamp start_date;
    public Timestamp updated_date;

    public FirebaseVacation() {

    }

    public static FirebaseVacation getFirebaseVacation(Vacation vacation) {
        FirebaseVacation firebaseVacation = new FirebaseVacation();
        firebaseVacation.created_date = vacation.created_date;
        firebaseVacation.vacation_id = vacation.vacation_id;
        firebaseVacation.customer_id = vacation.customer_id;
        firebaseVacation.customer_name = vacation.customer_name;
        firebaseVacation.customer_phone = vacation.customer_phone;
        firebaseVacation.end_date = vacation.end_date;
        firebaseVacation.hub_name = vacation.hub_name;
        firebaseVacation.start_date = vacation.start_date;
        firebaseVacation.updated_date = vacation.updated_date;

        return firebaseVacation;
    }
}
