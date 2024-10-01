package com.whytefarms.firebaseModels;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;


@Keep
public class WalletHistory {

    public Long amount;
    public Timestamp created_date;
    public Long current_wallet_balance;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public String delivery_executive;
    public String description;
    public String hub_name;
    public String reason;
    public String source;
    public String status;
    public String txn_id;
    public String type;
    public String user;

    public WalletHistory() {

    }
}
