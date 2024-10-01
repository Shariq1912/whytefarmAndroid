package com.whytefarms.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.whytefarms.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.CustomerActivity;
import com.whytefarms.utils.AppConstants;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import android.app.DatePickerDialog;
import android.widget.ImageView;
import java.util.Calendar;
import java.util.Locale;

public class CashCollectionActivity extends AppCompatActivity {

    private TextInputEditText amountInput;
    private TextInputEditText dateInput;
    private MaterialButton submitButton;
    private FirebaseFirestore database;
    private Customer customer;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_collection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.cash_collection_title);
        }

        database = FirebaseFirestore.getInstance();
        fetchCustomerData();

        amountInput = findViewById(R.id.amount_input);
        dateInput = findViewById(R.id.date_input);

        ImageView dateInputCal = findViewById(R.id.date_input_cal);

        dateInput.setOnTouchListener((view, motionEvent) -> {
            dateInputCal.performClick();
            return false;
        });
        dateInput.setOnClickListener(view -> showDatePickerDialog());
        dateInputCal.setOnClickListener(view -> showDatePickerDialog());
        submitButton = findViewById(R.id.submit_button);

        int amount = getIntent().getIntExtra("amount", 0);
        amountInput.setText(String.valueOf(amount));

        submitButton.setOnClickListener(v -> submitCashCollection());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showDatePickerDialog() {
    if (datePickerDialog != null && datePickerDialog.isShowing()) {
        return;
    }

    if (datePickerDialog == null) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(CashCollectionActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> dateInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year1)),
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
    }
    datePickerDialog.show();
}

    private void fetchCustomerData() {
        String customerId = ((WhyteFarmsApplication) getApplication()).getCustomerIDFromLoginState();
        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", customerId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        customer = queryDocumentSnapshots.getDocuments().get(0).toObject(Customer.class);
                    }
                });
    }

    private void submitCashCollection() {
        if (customer == null) {
            Toast.makeText(this, "Customer data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = amountInput.getText().toString();
        String dateStr = dateInput.getText().toString();

        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount = Integer.parseInt(amountStr);

        Map<String, Object> cashCollection = new HashMap<>();
        cashCollection.put("amount", amount);
        cashCollection.put("collected_amount", 0);
        cashCollection.put("created_date", Timestamp.now());
        cashCollection.put("customer_id", customer.customer_id);
        cashCollection.put("customer_name", customer.customer_name);
        cashCollection.put("customer_phone", customer.customer_phone);
        cashCollection.put("customer_address", customer.customer_address);
        cashCollection.put("date", dateStr);
        cashCollection.put("delivery_exe_id", "");
        cashCollection.put("delivery_executive_name", "");
        cashCollection.put("delivery_executive_phone", "");
        cashCollection.put("description", "");
        cashCollection.put("status", "0");
        cashCollection.put("time", "");
        cashCollection.put("updated_date", Timestamp.now());

        database.collection("cash_collection")
                .add(cashCollection)
                .addOnSuccessListener(documentReference -> {
                    CustomerActivity customerActivity = new CustomerActivity();
                    customerActivity.action = "Cash Collection";
                    customerActivity.created_date = new Timestamp(new Date());
                    customerActivity.customer_id = customer.customer_id;
                    customerActivity.customer_name = customer.customer_name;
                    customerActivity.customer_phone = customer.customer_phone;
                    customerActivity.description = "Cash collected requested by " + customer.customer_name + " on " + dateStr;
                    customerActivity.object = "Cash Collection Requested";
                    customerActivity.user = customer.customer_name;
                    customerActivity.platform = "Android";

                    database.collection("customer_activities").add(customerActivity)
                        .addOnSuccessListener(activityDocumentReference -> {
                        Toast.makeText(this, "Cash collection request submitted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit cash collection request", Toast.LENGTH_SHORT).show();
                    });
                });
    }
}