package com.whytefarms.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fastModels.Subscription;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.FirebaseSubscription;
import com.whytefarms.interfaces.FirebaseFirestoreResultListener;
import com.whytefarms.models.CartModel;
import com.whytefarms.models.CartProduct;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.Cart;
import com.whytefarms.firebaseModels.CustomerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends BaseActivity implements FirebaseFirestoreResultListener {

    private final FastItemAdapter<Subscription> cartAdapter = new FastItemAdapter<>();
    private final List<Subscription> cartItems = new ArrayList<>();
    private final List<String> uploadedSubscriptions = new ArrayList<>();
    private AppCompatTextView priceTitle;
    private AppCompatTextView totalValue;
    private AppCompatTextView reqdWalletBalance;
    private AppCompatTextView walletBalance;
    private LinearLayout proceedToPay;
    private AppCompatTextView proceedToPayText;
    private LinearLayout noItemLayout;
    private CardView addAddressLayout;
    private LinearLayout addressContainerLayout;
    private View progressBar;
    private AppCompatTextView fullName;
    private AppCompatTextView address;
    private AppCompatTextView addressType;
    private FirebaseFirestore database;
    private Customer customer;
    private Cart cart;
    private int availableWalletBal;
    private int reqdWalletBal;
    private AppCompatTextView availableWalletBalanceHeading;

    private boolean isUploading = false;

    private boolean isAddressAvailable = false;

    private RecyclerView cartRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish();
            }
        });

        AppCompatTextView goToShop = findViewById(R.id.go_to_shop);

        goToShop.setOnClickListener(view -> {
            Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.cart);
        }

        database = FirebaseFirestore.getInstance();

        addAddressLayout = findViewById(R.id.add_address_layout);
        CardView loginLayout = findViewById(R.id.login_layout);
        addressContainerLayout = findViewById(R.id.address_container_layout);
        addAddressLayout.setVisibility(View.GONE);
        addressContainerLayout.setVisibility(View.GONE);


        cartRecycler = findViewById(R.id.cart_recycler);
        priceTitle = findViewById(R.id.price_title);
        totalValue = findViewById(R.id.total_value);
        //AppCompatTextView deliveryCharge = findViewById(R.id.delivery_charge);
        reqdWalletBalance = findViewById(R.id.reqd_wallet_balance);
        walletBalance = findViewById(R.id.wallet_balance);
        proceedToPay = findViewById(R.id.proceed_to_pay);
        proceedToPayText = findViewById(R.id.proceed_to_pay_text);
        noItemLayout = findViewById(R.id.no_item_layout);
        progressBar = findViewById(R.id.progress_bar);
        fullName = findViewById(R.id.full_name);
        address = findViewById(R.id.address);
        addressType = findViewById(R.id.address_type);
        availableWalletBalanceHeading = findViewById(R.id.available_wallet_balance_heading);

        cartRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) cartRecycler.getItemAnimator();

        if (itemAnimator != null) {
            itemAnimator.setSupportsChangeAnimations(false);
        }
        cartAdapter.add(cartItems);
        cartRecycler.setAdapter(cartAdapter);

        addAddressLayout.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddressActivity.class);
            intent.putExtra("isEditing", false);
            startActivity(intent);
        });


        cart = new Cart(this);
        if (((WhyteFarmsApplication) getApplicationContext()).isLoggedIn()) {
            getCustomerDetails();
            loginLayout.setVisibility(View.GONE);
        } else {
            proceedToPay.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
            loginLayout.setOnClickListener(view -> startLoginFlow("LoginActivity"));
            walletBalance.setText(String.format(getString(R.string.price_placeholder), 0));
            availableWalletBalanceHeading.setText(R.string.login_to_see_wallet_balance);

        }
        setUpCartRecycler();

        checkVacation();

    }


    public void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }


    private void getCustomerDetails() {
        toggleProgressBar(true);
        proceedToPay.setVisibility(View.GONE);
        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", (((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState()))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                            if (customer != null) {
                                if (customer.customer_address.isEmpty()) {
                                    addAddressLayout.setVisibility(View.VISIBLE);
                                    addressContainerLayout.setVisibility(View.GONE);
                                    isAddressAvailable = false;
                                } else {
                                    addAddressLayout.setVisibility(View.GONE);
                                    addressContainerLayout.setVisibility(View.VISIBLE);
                                    fullName.setText(customer.customer_name);
                                    address.setText(customer.customer_address);
                                    addressType.setText(customer.addressType);
                                    isAddressAvailable = true;
                                }

                                availableWalletBal = Integer.parseInt(String.valueOf(customer.wallet_balance));
                                walletBalance.setText(String.format(Locale.getDefault(), getString(R.string.cart_wallet_balance_placeholder), availableWalletBal));
                                availableWalletBalanceHeading.setText(R.string.available_wallet_balance);
                            }
                        }

                        calculateOrderValue();
                        toggleProgressBar(false);
                    }
                });

    }

    public void setUpCartRecycler() {
        toggleProgressBar(true);
        cartItems.clear();
        cartAdapter.clear();
        
        if (cartRecycler == null) {
            cartRecycler = findViewById(R.id.cart_recycler);
            ((SimpleItemAnimator) cartRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
            cartRecycler.setLayoutManager(new LinearLayoutManager(this));
            cartRecycler.setAdapter(cartAdapter);
        }
        
        if (isLoggedIn()) {
            // Fetch from Firestore for logged-in users
            fetchCartFromFirestore();
        } else {
            // Use existing SharedPreferences logic for non-logged in users
            fetchCartFromPreferences();
        }
        
        cart.setCartValue();

        cartAdapter.addEventHook(new ClickEventHook<Subscription>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof Subscription.SubscriptionViewHolder) {
                    return ((Subscription.SubscriptionViewHolder) viewHolder).deleteCartItem;
                }
                return null;
            }

            @Override
            public void onClick(@NonNull View view, int i, @NonNull FastAdapter<Subscription> fastAdapter, @NonNull Subscription item) {
                cart.deleteItemFromCart(item.subscription_id);
            }
        });
    }

    private boolean isLoggedIn() {
        return getApplicationContext() instanceof WhyteFarmsApplication && 
               ((WhyteFarmsApplication) getApplicationContext()).isLoggedIn();
    }

    private void fetchCartFromFirestore() {
        String customerId = ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState();
        if (customerId == null) {
            showEmptyCart();
            toggleProgressBar(false);
            return;
        }

        FirebaseFirestore.getInstance().collection("cart_data")
                .whereEqualTo("customer_id", customerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        try {
                            // Get cart document
                            CartModel cart = task.getResult().getDocuments().get(0).toObject(CartModel.class);
                            if (cart != null && cart.getProducts() != null && !cart.getProducts().isEmpty()) {
                                // Convert CartProduct to Subscription
                                for (CartProduct product : cart.getProducts()) {
                                    Subscription cartItem = convertCartProductToSubscription(product);
                                    cartItems.add(cartItem);
                                    cartAdapter.add(cartItem);
                                }
                                
                                noItemLayout.setVisibility(View.GONE);
                                proceedToPay.setVisibility(View.VISIBLE);
                                calculateOrderValue();
                            } else {
                                showEmptyCart();
                            }
                        } catch (Exception e) {
                            Log.e("CheckoutActivity", "Error processing cart data", e);
                            showEmptyCart();
                        }
                    } else {
                        showEmptyCart();
                    }
                    toggleProgressBar(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("CheckoutActivity", "Error fetching cart from Firestore", e);
                    showEmptyCart();
                    toggleProgressBar(false);
                });
    }

    private Subscription convertCartProductToSubscription(CartProduct product) {
        Subscription cartItem = new Subscription();
        
        cartItem.mContext = CheckoutActivity.this;
        cartItem.sunday = product.getSunday();
        cartItem.monday = product.getMonday();
        cartItem.tuesday = product.getTuesday();
        cartItem.wednesday = product.getWednesday();
        cartItem.thursday = product.getThursday();
        cartItem.friday = product.getFriday();
        cartItem.saturday = product.getSaturday();
        cartItem.interval = product.getInterval();
        cartItem.package_unit = product.getPackage_unit();
        cartItem.price = product.getPrice();
        cartItem.product_name = product.getProduct_name();
        cartItem.quantity = product.getQuantity();
        
        // Handle dates
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000ZZ", Locale.US);
            if (product.getStartDate() != null) {
                Date startDate = dateFormat.parse(product.getStartDate());
                cartItem.start_date = new Timestamp(startDate);
            }
            if (product.getEndDate() != null) {
                Date endDate = dateFormat.parse(product.getEndDate());
                cartItem.end_date = new Timestamp(endDate);
            }
        } catch (Exception e) {
            Log.e("CheckoutActivity", "Error parsing dates", e);
        }
        
        cartItem.subscription_id = product.getProduct_id();
        cartItem.subscription_type = product.getSubscriptionType();
        cartItem.next_delivery_date = product.getNext_delivery_date();
        cartItem.hub_name = ""; // Default value
        cartItem.status = ""; // Default value
        cartItem.resume_date = new Timestamp(new Date()); // Default value
        cartItem.isCartItem = true;
        cartItem.reason = product.getReason();

        return cartItem;
    }

    private void showEmptyCart() {
        noItemLayout.setVisibility(View.VISIBLE);
        proceedToPay.setVisibility(View.GONE);
    }

    private void calculateOrderValue() {
        priceTitle.setText(String.format(Locale.getDefault(), getString(R.string.price_title_placeholder), cartAdapter.getAdapterItems().size()));

        int price = getPrice();


        reqdWalletBal = price - availableWalletBal;
        totalValue.setText(String.format(Locale.getDefault(), getString(R.string.price_placeholder), price));

        if (reqdWalletBal > 0) {
            reqdWalletBalance.setVisibility(View.VISIBLE);
            reqdWalletBalance.setText(String.format(Locale.getDefault(), getString(R.string.item_value_placeholder), reqdWalletBal));
        } else {
            reqdWalletBalance.setText(String.format(Locale.getDefault(), getString(R.string.item_value_placeholder), 0));
        }

        if (((WhyteFarmsApplication) getApplicationContext()).isLoggedIn()) {
            if (reqdWalletBal > 0) {
                proceedToPayText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(proceedToPayText.getContext(), R.drawable.ic_plus), null, null, null);
                proceedToPayText.setText(String.format(Locale.getDefault(), getString(R.string.add_balance_placeholder), reqdWalletBal));
                proceedToPay.setOnClickListener(view -> {
                    Intent intent = new Intent(CheckoutActivity.this, RechargeActivity.class);
                    intent.putExtra("fromCart", true);
                    intent.putExtra("type", "online");
                    intent.putExtra("value", reqdWalletBal);
                    startActivity(intent);
                });
                proceedToPay.setVisibility(isAddressAvailable ? View.VISIBLE : View.GONE);
            } else {
                proceedToPayText.setText(R.string.start_subscription);
                proceedToPayText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(proceedToPayText.getContext(), R.drawable.ic_check_circle), null, null, null);
                proceedToPay.setOnClickListener(view -> updateSubscriptionToDB());
                proceedToPay.setVisibility(isAddressAvailable ? View.VISIBLE : View.GONE);
            }
        }
    }

    private int getPrice() {
        int price = 0;
        for (Subscription item : cartAdapter.getAdapterItems()) {
            if (item.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
                if (item.sunday > 0) {
                    price += (int) (item.price * item.sunday);
                } else if (item.monday > 0) {
                    price += (int) (item.price * item.monday);
                } else if (item.tuesday > 0) {
                    price += (int) (item.price * item.tuesday);
                } else if (item.wednesday > 0) {
                    price += (int) (item.price * item.wednesday);
                } else if (item.thursday > 0) {
                    price += (int) (item.price * item.thursday);
                } else if (item.friday > 0) {
                    price += (int) (item.price * item.friday);
                } else if (item.saturday > 0) {
                    price += (int) (item.price * item.saturday);
                }
            } else {
                price += (int) (item.price * item.quantity);
            }
        }
        return price;
    }

    private void updateSubscriptionToDB() {
        if (isUploading) {
            Toast.makeText(this, R.string.please_wait_while_subscription_adding, Toast.LENGTH_SHORT).show();
            return;
        }


        toggleProgressBar(true);
        isUploading = true;
        for (Subscription subscription : cartAdapter.getAdapterItems()) {
            subscription.customer_id = customer.customer_id;
            subscription.customer_phone = customer.customer_phone;
            subscription.customer_name = customer.customer_name;
            subscription.delivered_by = customer.hub_name;
            subscription.delivering_to = customer.customer_address;
            subscription.customer_address = customer.customer_address;
            subscription.hub_name = customer.hub_name;
            subscription.created_date = Timestamp.now();
            subscription.updated_date = Timestamp.now();
            subscription.status = "1";
            subscription.reason = "";

            database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
                    .add(FirebaseSubscription.getFirebaseSubscription(subscription))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            uploadedSubscriptions.add(subscription.subscription_id);

                            // Add customer activity
                            CustomerActivity customerActivity = new CustomerActivity();
                            customerActivity.action = "Subscription Added";
                            customerActivity.created_date = Timestamp.now();
                            customerActivity.customer_id = customer.customer_id;
                            customerActivity.customer_name = customer.customer_name;
                            customerActivity.customer_phone = customer.customer_phone;
                            customerActivity.description = "Subscription added for " + customer.customer_name + " - " + subscription.subscription_id + " by " + customer.customer_name;
                            customerActivity.object = "Subscription Added";
                            customerActivity.user = customer.customer_name;
                            customerActivity.platform = "Android";

                            database.collection("customer_activities").add(customerActivity)
                                    .addOnSuccessListener(activityDocumentReference -> {


                                        checkAllUploaded();
                                    });
                        } else {
                            Toast.makeText(this, R.string.subscriptions_created_successfully, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        calculateOrderValue();
    }

    private void checkAllUploaded() {
        if (uploadedSubscriptions.size() == cartAdapter.getAdapterItemCount()) {
            toggleProgressBar(false);
            Toast.makeText(this, R.string.subscriptions_created_successfully, Toast.LENGTH_SHORT).show();
            cart.clearCart();
            setUpCartRecycler();
            uploadedSubscriptions.clear();
            isUploading = false;

            Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
            intent.putExtra("from_checkout", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
        }
    }

    public void removeFromAdapter(String itemID) {
        // Find the item to remove
        Subscription itemToRemove = null;
        for (Subscription subscription : cartAdapter.getAdapterItems()) {
            if (subscription.subscription_id.equalsIgnoreCase(itemID)) {
                itemToRemove = subscription;
                break;
            }
        }

        if (itemToRemove != null) {
            // First remove from our data list
            cartItems.remove(itemToRemove);
            
            // Then safely remove from adapter
            final Subscription finalItemToRemove = itemToRemove;
            runOnUiThread(() -> {
                int position = cartAdapter.getAdapterPosition(finalItemToRemove);
                if (position >= 0) {
                    cartAdapter.remove(position);
                    
                    // Check if cart is now empty
                    if (cartAdapter.getItemCount() == 0) {
                        showEmptyCart();
                    } else {
                        calculateOrderValue();
                    }
                }
            });
        }
        
        toggleProgressBar(false);
    }

    @Override
    public void onResultReceived(Object result, String resultType, String action) {

    }

    @Override
    public void onError(Throwable error) {

    }

    private void fetchCartFromPreferences() {
        SharedPreferences pref = getSharedPreferences("cart", MODE_PRIVATE);

        if (pref != null) {
            try {
                String cartString = pref.getString("cart_items", "");
                JSONArray items;
                if (!cartString.isEmpty()) {
                    items = new JSONArray(cartString);

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);

                        Subscription cartItem = new Subscription();

                        cartItem.mContext = CheckoutActivity.this;
                        cartItem.sunday = item.getLong("sunday");
                        cartItem.monday = item.getLong("monday");
                        cartItem.tuesday = item.getLong("tuesday");
                        cartItem.wednesday = item.getLong("wednesday");
                        cartItem.thursday = item.getLong("thursday");
                        cartItem.friday = item.getLong("friday");
                        cartItem.saturday = item.getLong("saturday");
                        cartItem.interval = item.getLong("interval");
                        cartItem.package_unit = item.getString("package_unit");
                        cartItem.price = item.getLong("price");
                        cartItem.product_name = item.getString("product_name");
                        cartItem.quantity = item.getLong("quantity");
                        cartItem.start_date = new Timestamp(new Date(item.getLong("start_date")));
                        cartItem.end_date = new Timestamp(new Date(item.getLong("end_date")));
                        cartItem.subscription_id = item.getString("subscription_id");
                        cartItem.subscription_type = item.getString("subscription_type");
                        cartItem.next_delivery_date = item.getString("next_delivery_date");
                        cartItem.hub_name = item.getString("hub_name");
                        cartItem.status = item.getString("status");
                        cartItem.resume_date = new Timestamp(new Date(item.getLong("resume_date")));
                        cartItem.isCartItem = true;
                        cartItem.reason = "";

                        cartItems.add(cartItem);
                        cartAdapter.add(cartItem);
                    }
                }

                if (!cartItems.isEmpty()) {
                    noItemLayout.setVisibility(View.GONE);
                    proceedToPay.setVisibility(View.VISIBLE);
                } else {
                    showEmptyCart();
                }

                toggleProgressBar(false);
            } catch (JSONException e) {
                Log.e("CheckoutActivity", "Error parsing cart from preferences", e);
                showEmptyCart();
                toggleProgressBar(false);
            }
        } else {
            showEmptyCart();
            toggleProgressBar(false);
        }
    }
}