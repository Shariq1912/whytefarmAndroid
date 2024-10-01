package com.whytefarms.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fastModels.Vacation;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.FirebaseSubscription;
import com.whytefarms.firebaseModels.FirebaseVacation;
import com.whytefarms.firebaseModels.CustomerActivity;
import com.whytefarms.fragments.FragmentProfile;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VacationListActivity extends BaseActivity {

    private final FastItemAdapter<Vacation> vacationAdapter = new FastItemAdapter<>();
    private final List<Vacation> vacations = new ArrayList<>();
    private int subscriptionsToEdit = 0;
    private AppCompatTextView vacationStatus;
    private LinearLayout vacationStatusLayout;
    private AppCompatTextView addVacationButton;
    private FirebaseFirestore database;
    private View progressBar;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_list);

        progressBar = findViewById(R.id.progress_bar);
        vacationStatus = findViewById(R.id.vacation_status);
        vacationStatusLayout = findViewById(R.id.no_vacation_layout);
        RecyclerView vacationRecycler = findViewById(R.id.vacation_recycler);
        addVacationButton = findViewById(R.id.add_vacation_button);

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
            getSupportActionBar().setTitle(R.string.my_vacations);
        }

        database = FirebaseFirestore.getInstance();

        addVacationButton.setOnClickListener(view -> addVacation(new Timestamp(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1))),
                new Timestamp(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 2))), ""));

        vacationRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        vacationRecycler.setAdapter(vacationAdapter);


        vacationAdapter.addEventHook(new ClickEventHook<Vacation>() {

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof Vacation.VacationViewHolder) {
                    return ((Vacation.VacationViewHolder) viewHolder).deleteVacation;
                }
                return null;
            }

            @Override
            public void onClick(@NonNull View view, int i, @NonNull FastAdapter<Vacation> fastAdapter, @NonNull Vacation item) {
                if (item.vacation_id != null && !item.vacation_id.isEmpty()) {
                    deleteVacationById(item.vacation_id);
                } else {
                    deleteVacationByKeys(item.customer_id, item.start_date, item.end_date);
                }
            }
        });

    }

    private void deleteVacationByKeys(String customerId, Timestamp startDate, Timestamp endDate) {
        toggleProgressBar(true);
        database.collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                .where(Filter.and(
                        Filter.equalTo("customer_id", customerId),
                        Filter.equalTo("start_date", startDate),
                        Filter.equalTo("end_date", endDate)))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            FirebaseVacation vacation = task.getResult().getDocuments().get(0).toObject(FirebaseVacation.class);
                            if (vacation != null) {
                                task.getResult().getDocuments().get(0).getReference().delete()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            logVacationActivity(customerId, vacation);
                                            handleVacationDeleted();
                                        } else {
                                            Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                                        }
                                        toggleProgressBar(false);
                                    });
                            } else {
                                Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                                toggleProgressBar(false);
                            }
                        } else {
                            Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                            toggleProgressBar(false);
                        }
                   } else {
                    Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                    toggleProgressBar(false);
                }
            });
}

//    private void updateSubscriptions(Customer customer) {
//        database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
//                .where(Filter.and(
//                        Filter.equalTo("customer_id", customer.customer_id),
//                        Filter.lessThanOrEqualTo("start_date", new Timestamp(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1))))
//                ))
//                .get()
//                .addOnCompleteListener(task2 -> {
//                    if (task2.isSuccessful()) {
//                        if (task2.getResult() != null && !task2.getResult().isEmpty()) {
//                            subscriptionsToEdit = task2.getResult().getDocuments().size();
//
//                            for (DocumentSnapshot snapshot : task2.getResult().getDocuments()) {
//                                FirebaseSubscription subscription = snapshot.toObject(FirebaseSubscription.class);
//                                if (subscription != null) {
//                                    if (subscription.start_date.compareTo(new Timestamp(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1)))) <= 0) {
//                                        subscription.next_delivery_date =
//                                                DateUtils.getStringFromMillis(
//                                                        DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1),
//                                                        "yyyy-MM-dd", false);
//                                        snapshot.getReference()
//                                                .set(subscription)
//                                                .addOnCompleteListener(task3 -> {
//                                                    if (task3.isSuccessful()) {
//                                                        checkSubscriptionsUpdated();
//                                                    }
//                                                });
//                                    }
//                                }
//                            }
//                        }
//                    }
//                });
//    }

//    public void checkSubscriptionsUpdated() {
//        if (subscriptionsToEdit == ++counter) {
//            Toast.makeText(VacationListActivity.this, R.string.vacation_deleted_successfully, Toast.LENGTH_SHORT).show();
//            getVacations();
//        }
//    }

    private void deleteVacationById(String id) {
        toggleProgressBar(true);
        String customerID = (((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState());
        database.collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                .where(Filter.and(
                        Filter.equalTo("customer_id", customerID),
                        Filter.equalTo("vacation_id", id)))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            FirebaseVacation vacation = task.getResult().getDocuments().get(0).toObject(FirebaseVacation.class);

                             if (vacation != null) {
                                task.getResult().getDocuments().get(0).getReference().delete()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                logVacationActivity(customerID, vacation);
                                                handleVacationDeleted();
                                            } else {
                                            Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                                        }
                                        toggleProgressBar(false);
                                    });
                        } else {
                            Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                            toggleProgressBar(false);
                        }
                    } else {
                        Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                        toggleProgressBar(false);
                    }
                } else {
                    Toast.makeText(VacationListActivity.this, R.string.vacation_delete_error, Toast.LENGTH_SHORT).show();
                    toggleProgressBar(false);
                }
            });
    }

    private void handleVacationDeleted() {
        Toast.makeText(VacationListActivity.this, R.string.vacation_deleted_successfully, Toast.LENGTH_SHORT).show();
        getVacations();
    }

    private void logVacationActivity(String customerID, FirebaseVacation vacation) {
    database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
            .whereEqualTo("customer_id", customerID)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                        if (customer != null) {
                            CustomerActivity customerActivity = new CustomerActivity();
                            customerActivity.action = "Vacation Deleted";
                            customerActivity.created_date = Timestamp.now();
                            customerActivity.customer_id = customer.customer_id;
                            customerActivity.customer_name = customer.customer_name;
                            customerActivity.customer_phone = customer.customer_phone;
                            customerActivity.description = "Vacation deleted - vacation start: " 
                                + DateUtils.getStringFromMillis(vacation.start_date.toDate().getTime(), "yyyy-MM-dd", false)
                                + " vacation end: " + DateUtils.getStringFromMillis(vacation.end_date.toDate().getTime(), "yyyy-MM-dd", false) + " by " + customer.customer_name;
                            customerActivity.object = "Vacation";
                            customerActivity.user = customer.customer_name;
                            customerActivity.platform = "Android";

                            database.collection("customer_activities")
                                    .add(customerActivity);
                        }
                    }
                }
            });
}

    public void addVacation(Timestamp startDate, Timestamp endDate, String id) {
        FragmentProfile fragmentProfile = FragmentProfile.newInstance("Vacation", startDate.toDate().getTime(), endDate.toDate().getTime(), id);
        fragmentProfile.show(getSupportFragmentManager(), "Vacation");
    }

    @Override
    protected void onStart() {
        super.onStart();
        getVacations();
        checkVacation();
    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    private void getVacations() {
        vacationStatus.setText(R.string.loading);
        vacations.clear();
        vacationAdapter.clear();
        addVacationButton.setVisibility(View.GONE);

        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState();

        database.collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                .whereEqualTo("customer_id", customerID)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Vacation vacation = document.toObject(Vacation.class);

                                if (vacation != null) {
                                    vacation.activity = VacationListActivity.this;
                                    vacations.add(vacation);
                                    vacationAdapter.add(vacation);
                                    vacationAdapter.notifyAdapterItemChanged(vacationAdapter.getAdapterPosition(vacation));
                                }
                            }

                            if (vacations.isEmpty()) {
                                vacationStatusLayout.setVisibility(View.VISIBLE);
                                vacationStatus.setText(R.string.no_vacations_added_yet);
                                addVacationButton.setVisibility(View.VISIBLE);
                            } else {
                                vacationStatusLayout.setVisibility(View.GONE);
                                vacationAdapter.notifyAdapterDataSetChanged();
                            }
                        } else {
                            vacationStatusLayout.setVisibility(View.VISIBLE);
                            vacationStatus.setText(R.string.no_vacations_added_yet);
                            addVacationButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        vacationStatusLayout.setVisibility(View.VISIBLE);
                        addVacationButton.setVisibility(View.VISIBLE);
                      //  Log.d("TAG", "getVacations: " + task.getException());
                    }
                    toggleProgressBar(false);
                });
    }


    public void updateOnVacationAdded(FragmentProfile fragmentProfile) {
        getSupportFragmentManager().beginTransaction().remove(fragmentProfile).commitNow();
        getVacations();
    }
}