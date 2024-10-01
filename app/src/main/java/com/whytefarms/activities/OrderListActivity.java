package com.whytefarms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fastModels.Order;
import com.whytefarms.interfaces.FirebaseFirestoreResultListener;
import com.whytefarms.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends BaseActivity implements FirebaseFirestoreResultListener {

    private final FastItemAdapter<Order> orderAdapter = new FastItemAdapter<>();
    private List<Order> orderList = new ArrayList<>();
    private RecyclerView orderRecycler;

    private LinearLayout noOrdersLayout;

    private View progressBar;

    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

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
            getSupportActionBar().setTitle(R.string.my_orders);
        }

        database = FirebaseFirestore.getInstance();

        AppCompatTextView goToShop = findViewById(R.id.go_to_shop);

        goToShop.setOnClickListener(view -> {
            Intent intent = new Intent(OrderListActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();
        });

        noOrdersLayout = findViewById(R.id.no_orders);
        progressBar = findViewById(R.id.progress_bar);

        orderList = new ArrayList<>();


        orderRecycler = findViewById(R.id.order_recycler);
        orderRecycler.setLayoutManager(new LinearLayoutManager(orderRecycler.getContext(), LinearLayoutManager.VERTICAL, false));

        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) orderRecycler.getItemAnimator();

        if (itemAnimator != null) {
            itemAnimator.setSupportsChangeAnimations(false);
        }
        orderAdapter.add(orderList);
        orderRecycler.setAdapter(orderAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getOrders();
    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }


    private void getOrders() {

        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState();
        if (customerID != null && !customerID.isEmpty()) {

            orderList.clear();
            orderAdapter.clear();
            orderAdapter.notifyAdapterDataSetChanged();

            database.collection(AppConstants.ORDER_HISTORY_COLLECTION)
                    .whereEqualTo("customer_id", customerID)
                    .orderBy("delivery_timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            orderAdapter.clear();
                            orderList.clear();

                            orderAdapter.notifyAdapterDataSetChanged();

                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Order order = document.toObject(Order.class);

                                    orderList.add(order);
                                    orderAdapter.add(order);
                                    orderAdapter.notifyAdapterItemChanged(orderAdapter.getAdapterItemCount() - 1);
                                }

                                if (!orderAdapter.getAdapterItems().isEmpty()) {
                                    orderRecycler.setVisibility(View.VISIBLE);
                                    noOrdersLayout.setVisibility(View.GONE);
                                }
                            } else {
                                orderRecycler.setVisibility(View.GONE);
                                noOrdersLayout.setVisibility(View.VISIBLE);
                            }
                            toggleProgressBar(false);
                        } else {
                            toggleProgressBar(false);
                        }
                    });
        } else {
            checkLogin();
        }

    }


    public void checkLogin() {
        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState();
        String userHash = ((WhyteFarmsApplication) getApplicationContext()).getUserHashFromLoginState();
        if (((WhyteFarmsApplication) getApplicationContext()).isLoggedIn()) {
            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                    .where(Filter.and(Filter.equalTo("customer_id", customerID), Filter.equalTo("userHash", userHash)))
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                                if (documentSnapshot.exists()) {
                                    onResultReceived(documentSnapshot, AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH, AppConstants.ACTION_VALIDATE_LOGIN);
                                } else {
                                    onResultReceived(documentSnapshot, AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH, AppConstants.ACTION_INVALIDATE_LOGIN);
                                }
                            } else {
                                onResultReceived(null, AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH, AppConstants.ACTION_INVALIDATE_LOGIN);
                            }
                        } else {
                            onError(new Exception(getString(R.string.cannot_fetch_customer_data)));
                        }
                    });

        } else {
            onResultReceived(null, AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH, AppConstants.ACTION_INVALIDATE_LOGIN);
        }
    }

    @Override
    public void onResultReceived(@Nullable Object result, String resultType, String action) {
        if (AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH.equals(resultType) && AppConstants.ACTION_VALIDATE_LOGIN.equals(action)) {
            View loginNeeded = findViewById(R.id.login_needed);
            if (loginNeeded != null) {
                loginNeeded.setVisibility(View.GONE);
            }
        } else if (AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH.equals(resultType) && AppConstants.ACTION_INVALIDATE_LOGIN.equals(action)) {
            View loginNeeded = findViewById(R.id.login_needed);
            if (loginNeeded != null) {
                loginNeeded.setVisibility(View.VISIBLE);
            }
        }
        toggleProgressBar(false);
    }

    @Override
    public void onError(Throwable error) {
        View loginNeeded = findViewById(R.id.login_needed);
        if (loginNeeded != null) {
            loginNeeded.setVisibility(View.VISIBLE);
        }

        Toast.makeText(OrderListActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

        toggleProgressBar(false);
    }

}