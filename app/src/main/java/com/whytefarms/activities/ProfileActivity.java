package com.whytefarms.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fragments.FragmentProfile;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.DateUtils;

import com.whytefarms.firebaseModels.CustomerActivity;
import com.whytefarms.firebaseModels.Customer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout firstName;
    private TextInputLayout lastName;
    private TextInputLayout email;
    private AppCompatTextView male;
    private AppCompatTextView female;
    private AppCompatTextView other;
    private AppCompatTextView dobInput;
    private AppCompatImageView dobInputCal;
    private AppCompatTextView anniversaryInput;
    private AppCompatImageView anniversaryInputCal;
    private DatePickerDialog datePickerDialog;

    private FragmentProfile fragmentProfile;
    private FirebaseFirestore database;

    private LinearLayout genderLayout;

    private View progressBar;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.my_profile);

        }

        database = FirebaseFirestore.getInstance();

        firstName = findViewById(R.id.first_name_input);
        lastName = findViewById(R.id.last_name_input);
        email = findViewById(R.id.email_input);

        if (firstName.getEditText() != null) {
            firstName.getEditText().addTextChangedListener(this);
        }

        if (lastName.getEditText() != null) {
            lastName.getEditText().addTextChangedListener(this);
        }

        if (email.getEditText() != null) {
            email.getEditText().addTextChangedListener(this);
        }


        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        other = findViewById(R.id.other);

        male.setOnClickListener(this);
        female.setOnClickListener(this);
        other.setOnClickListener(this);

        genderLayout = findViewById(R.id.gender_layout);

        dobInput = findViewById(R.id.dob_input);
        dobInputCal = findViewById(R.id.dob_input_cal);
        anniversaryInput = findViewById(R.id.anniversary_input);
        anniversaryInputCal = findViewById(R.id.anniversary_input_cal);

        CardView addressCard = findViewById(R.id.address_card);
        CardView deliveryPrefCard = findViewById(R.id.delivery_pref_card);
        CardView ordersCard = findViewById(R.id.orders_card);
        CardView vacationCard = findViewById(R.id.vacation_card);

        View loginNeeded = findViewById(R.id.login_needed);

        progressBar = findViewById(R.id.progress_bar);


        AppCompatButton saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(view -> {
            toggleProgressBar(true);
            if (isValidInfo()) {
                saveToDB();
            } else {
                Toast.makeText(ProfileActivity.this, getString(R.string.please_check_the_entered_details), Toast.LENGTH_SHORT).show();
                toggleProgressBar(false);
            }
        });


        dobInput.setOnTouchListener((view, motionEvent) -> {
            dobInputCal.performClick();
            return false;
        });

        dobInputCal.setOnClickListener(view -> {
            if (datePickerDialog != null && datePickerDialog.isShowing()) {
                return;
            }

            if (datePickerDialog == null) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                        (view1, year1, monthOfYear, dayOfMonth) -> dobInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year1)),
                        year, month, day);



                datePickerDialog.getDatePicker().setMaxDate(DateUtils.getTimeAfterAddingDays(new Date().getTime(), -1));
                datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                datePickerDialog.show();
            }
        });


        /*anniversaryInput.setOnTouchListener((view, motionEvent) -> {
            anniversaryInputCal.performClick();
            return false;
        });*/

        anniversaryInputCal.setOnClickListener(view -> {
            if (datePickerDialog != null && datePickerDialog.isShowing()) {
                return;
            }

            if (datePickerDialog == null) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                        (view1, year1, monthOfYear, dayOfMonth) -> anniversaryInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year1)),
                        year, month, day);

                datePickerDialog.getDatePicker().setMaxDate(DateUtils.getTimeAfterAddingDays(new Date().getTime(), -1));
                datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                datePickerDialog.show();
            }
        });

        anniversaryInputCal.setVisibility(View.GONE);

        deliveryPrefCard.setOnClickListener(view -> {
            fragmentProfile = FragmentProfile.newInstance("DeliveryPreferences", 0, 0, "");
            fragmentProfile.show(getSupportFragmentManager(), "DeliveryPreferences");
        });


        addressCard.setOnClickListener(view -> {
            fragmentProfile = FragmentProfile.newInstance("Address", 0, 0, "");
            fragmentProfile.show(getSupportFragmentManager(), "Address");
        });


        vacationCard.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, VacationListActivity.class);
            startActivity(intent);
        });

        ordersCard.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, OrderListActivity.class);
            startActivity(intent);
        });

        AppCompatTextView loginNowButton = loginNeeded.findViewById(R.id.login_now_button);

        if (loginNowButton != null) {
            loginNowButton.setOnClickListener(view -> startLoginFlow("LoginActivity"));
        }

        if (((WhyteFarmsApplication) getApplicationContext()).isLoggedIn()) {
            fillDetailsFromDB();
            loginNeeded.setVisibility(View.GONE);
        } else {
            loginNeeded.setVisibility(View.VISIBLE);

            if (loginNowButton != null) {
                loginNowButton.requestFocus();
            }

            hideKeyboard();
        }

        checkVacation();
    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }


    public boolean isGenderSelected(AppCompatTextView gender) {
        return gender.getBackground().getConstantState() ==
                Objects.requireNonNull(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_selected)).getConstantState();
    }

    private void saveToDB() {
        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                        if (customer != null) {
                            
                            customer.customer_name = Objects.requireNonNull(firstName.getEditText()).getText() + " " + Objects.requireNonNull(lastName.getEditText()).getText();
                            customer.customer_email = String.valueOf(Objects.requireNonNull(email.getEditText()).getText());
                            customer.gender = isGenderSelected(male) ? "male" : isGenderSelected(female) ? "female" : isGenderSelected(other) ? "other" : "";
                            try {
                                customer.dob = dobInput.getText().toString().isEmpty() ? new Timestamp(new Date(0)) :
                                        new Timestamp(DateUtils.getDateFromString(dobInput.getText().toString(), "dd-mm-yyyy", false));
                            } catch (ParseException e) {
                                customer.dob = new Timestamp(new Date(0));
                            }

                            try {
                                customer.anniversary_date = anniversaryInput.getText().toString().isEmpty() ? new Timestamp(new Date(0)) :
                                        new Timestamp(DateUtils.getDateFromString(anniversaryInput.getText().toString(), "dd-mm-yyyy", false));
                            } catch (ParseException e) {
                                customer.anniversary_date = new Timestamp(new Date(0));
                            }

                            task.getResult().getDocuments().get(0).getReference()
                                    .set(customer)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            logCustomerActivity(customer);
                                            Toast.makeText(ProfileActivity.this, R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ProfileActivity.this, R.string.not_saved, Toast.LENGTH_SHORT).show();
                                        }
                                        toggleProgressBar(false);
                                    });
                        }
                    }
                });
    }

    private void logCustomerActivity(Customer newCustomer) {
        CustomerActivity customerActivity = new CustomerActivity();
        customerActivity.action = "Profile Updated";
        customerActivity.created_date = Timestamp.now();
        customerActivity.customer_id = newCustomer.customer_id;
        customerActivity.customer_name = newCustomer.customer_name;
        customerActivity.customer_phone = newCustomer.customer_phone;
        customerActivity.description = "Profile details updated by " + newCustomer.customer_name;
        customerActivity.object = "Customer Profile";
        customerActivity.user = newCustomer.customer_name;
        customerActivity.platform = "Android";

        database.collection("customer_activities")
                .add(customerActivity);
    }


    private boolean isValidInfo() {
        boolean isValid = true;

        if (firstName != null && firstName.getEditText() != null &&
                firstName.getEditText().getText() != null &&
                firstName.getEditText().getText().toString().isEmpty()) {
            firstName.setError(getString(R.string.first_name_cannot_be_empty));
            isValid = false;
        }

        if (lastName != null && lastName.getEditText() != null &&
                lastName.getEditText().getText() != null
                && lastName.getEditText().getText().toString().isEmpty()) {
            lastName.setError(getString(R.string.last_name_cannot_be_empty));
            isValid = false;
        }


        if (email != null && email.getEditText() != null &&
                email.getEditText().getText() != null &&
                email.getEditText().getText().toString().isEmpty()) {
            email.setError(getString(R.string.email_address_cannot_be_empty));
            isValid = false;
        }


        if (email != null && email.getEditText() != null && email.getEditText().getText() != null
                && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getEditText().getText().toString()).matches()) {
            email.setError(getString(R.string.enter_valid_email_address));
            isValid = false;
        }

        if (!isGenderSelected(male) && !isGenderSelected(female) && !isGenderSelected(other)) {
            genderLayout.setBackgroundColor(ActivityCompat.getColor(ProfileActivity.this, R.color.inactive_subscription_bg));
            isValid = false;
        }


        return isValid;
    }

    private void fillDetailsFromDB() {

        toggleProgressBar(true);

        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .where(Filter.equalTo("customer_id", ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState()))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Customer customer = queryDocumentSnapshots.getDocuments().get(0).toObject(Customer.class);

                        if (customer != null) {
                            Objects.requireNonNull(firstName.getEditText()).setText(customer.customer_name.split(" ")[0]);

                            Objects.requireNonNull(lastName.getEditText()).setText(customer.customer_name.split(" ").length > 1 ? customer.customer_name.split(" ")[1] : "");
                            Objects.requireNonNull(email.getEditText()).setText(customer.customer_email);
                            if (customer.dob instanceof Timestamp) {
                                dobInput.setText(DateUtils.getStringFromMillis(((Timestamp) customer.dob).toDate().getTime(), "dd-MM-yyyy", true));
                            } else if (customer.dob instanceof String) {
                                try {
                                    dobInput.setText(DateUtils.getStringFromMillis(DateUtils.getDateFromString((String) customer.dob, "yyyy-MM-dd", false).getTime(), "dd-MM-yyyy", true));
                                } catch (ParseException e) {
                                    dobInput.setText((String) customer.dob);
                                }

                            }

                            if (customer.anniversary_date instanceof Timestamp) {
                                anniversaryInput.setText(DateUtils.getStringFromMillis(((Timestamp) customer.anniversary_date).toDate().getTime(), "dd-MM-yyyy", true));
                            } else if (customer.anniversary_date instanceof String) {
                                try {
                                    anniversaryInput.setText(DateUtils.getStringFromMillis(DateUtils.getDateFromString((String) customer.anniversary_date, "yyyy-MM-dd", false).getTime(), "dd-MM-yyyy", true));
                                } catch (ParseException e) {
                                    anniversaryInput.setText((String) customer.anniversary_date);
                                }

                            }

                            if (customer.gender.equalsIgnoreCase("male")) {
                                male.setBackground(ContextCompat.getDrawable(male.getContext(), R.drawable.ic_rectangle_selected));
                                male.setTextColor(ContextCompat.getColor(male.getContext(), R.color.white));

                                female.setBackground(ContextCompat.getDrawable(female.getContext(), R.drawable.ic_rectangle_unselected));
                                female.setTextColor(MaterialColors.getColor(female.getContext(), android.R.attr.textColorPrimary, Color.BLACK));

                                other.setBackground(ContextCompat.getDrawable(other.getContext(), R.drawable.ic_rectangle_unselected));
                                other.setTextColor(MaterialColors.getColor(other.getContext(), android.R.attr.textColorPrimary, Color.BLACK));
                            } else if (customer.gender.equalsIgnoreCase("female")) {
                                female.setBackground(ContextCompat.getDrawable(female.getContext(), R.drawable.ic_rectangle_selected));
                                female.setTextColor(ContextCompat.getColor(female.getContext(), R.color.white));

                                male.setBackground(ContextCompat.getDrawable(male.getContext(), R.drawable.ic_rectangle_unselected));
                                male.setTextColor(MaterialColors.getColor(male.getContext(), android.R.attr.textColorPrimary, Color.BLACK));

                                other.setBackground(ContextCompat.getDrawable(other.getContext(), R.drawable.ic_rectangle_unselected));
                                other.setTextColor(MaterialColors.getColor(other.getContext(), android.R.attr.textColorPrimary, Color.BLACK));
                            } else if (customer.gender.equalsIgnoreCase("other")) {
                                other.setBackground(ContextCompat.getDrawable(other.getContext(), R.drawable.ic_rectangle_selected));
                                other.setTextColor(ContextCompat.getColor(other.getContext(), R.color.white));

                                female.setBackground(ContextCompat.getDrawable(female.getContext(), R.drawable.ic_rectangle_unselected));
                                female.setTextColor(MaterialColors.getColor(female.getContext(), android.R.attr.textColorPrimary, Color.BLACK));

                                male.setBackground(ContextCompat.getDrawable(male.getContext(), R.drawable.ic_rectangle_unselected));
                                male.setTextColor(MaterialColors.getColor(male.getContext(), android.R.attr.textColorPrimary, Color.BLACK));
                            }

                        }

                        toggleProgressBar(false);
                    }
                });


    }

    @Override
    public void onClick(View view) {
        male.setBackground(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_unselected));
        female.setBackground(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_unselected));
        other.setBackground(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_unselected));
        genderLayout.setBackground(null);

        int id = view.getId();
        if (id == R.id.male) {
            male.setBackground(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_selected));
        } else if (id == R.id.female) {
            female.setBackground(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_selected));
        } else if (id == R.id.other) {
            other.setBackground(ActivityCompat.getDrawable(ProfileActivity.this, R.drawable.ic_rectangle_selected));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (email.getEditText() != null && email.getEditText().getText().length() > 0) {
            email.setError(null);
        }

        if (firstName.getEditText() != null && firstName.getEditText().getText().length() > 0) {
            firstName.setError(null);
        }


        if (lastName.getEditText() != null && lastName.getEditText().getText().length() > 0) {
            lastName.setError(null);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();

        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(ProfileActivity.this);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}


