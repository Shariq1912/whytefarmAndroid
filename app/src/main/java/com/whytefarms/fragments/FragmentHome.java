package com.whytefarms.fragments;

import static java.lang.Double.NaN;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.whytefarms.R;
import com.whytefarms.activities.BaseActivity;
import com.whytefarms.activities.CheckoutActivity;
import com.whytefarms.activities.DescriptionActivity;
import com.whytefarms.activities.MainActivity;
import com.whytefarms.activities.ProfileActivity;
import com.whytefarms.activities.RechargeActivity;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fastModels.Item;
import com.whytefarms.fastModels.Order;
import com.whytefarms.fastModels.Product;
import com.whytefarms.fastModels.Subscription;
import com.whytefarms.fastModels.Transaction;
import com.whytefarms.fastModels.Vacation;
import com.whytefarms.firebaseModels.BulkUpdateQuantity;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.FirebaseSubscription;
import com.whytefarms.firebaseModels.CustomerActivity;
import com.whytefarms.interfaces.FirebaseFirestoreResultListener;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.BannerSliderAdapter;
import com.whytefarms.utils.Cart;
import com.whytefarms.utils.DateUtils;
import com.whytefarms.utils.ViewPagerItemImage;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator3;


public class FragmentHome extends Fragment implements FirebaseFirestoreResultListener, TextWatcher {

    private final FastItemAdapter<Product> productAdapter = new FastItemAdapter<>();
    private final FastItemAdapter<Item> productFilterAdapter = new FastItemAdapter<>();
    private final FastItemAdapter<Item> subscriptionFilterAdapter = new FastItemAdapter<>();
    private final FastItemAdapter<Order> orderAdapter = new FastItemAdapter<>();
    private final FastItemAdapter<Transaction> transactionAdapter = new FastItemAdapter<>();
    private final FastItemAdapter<Subscription> subscriptionAdapter = new FastItemAdapter<>();
    public AppCompatTextView numCartItems;
    private List<Product> tempFilterList;
    private List<Product> productList = new ArrayList<>();
    private List<Subscription> subscriptionList = new ArrayList<>();
    private List<Order> orderList = new ArrayList<>();
    private List<Item> productFilterList = new ArrayList<>();
    private List<Item> subscriptionFilterList = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private RecyclerView subscriptionRecycler;
    private RecyclerView orderRecycler;
    private LinearLayout subscriptionFilters;
    private LinearLayout notFoundLayout;
    private LinearLayout vacationLayout;
    private FirebaseFirestore database;
    private AppCompatTextView selectedFilterText;
    private View view = null;
    private DatePickerDialog datePickerDialog;
    private AppCompatTextView walletBalance;
    private AppCompatTextView onlineRecharge;
    private AppCompatTextView cashRecharge;
    private AppCompatImageView filterButton;
    private FrameLayout menuCart;

    private LinearLayout bannerLayout;
    private ViewPager2 bannerViewPager;
    private List<ViewPagerItemImage> viewPagerItemImageList;
    private CircleIndicator3 circleIndicator;
    private BannerSliderAdapter viewPagerAdapter;

    private AppCompatTextView orderUpdateTip;

    private CircleImageView profileImage;

    private String selectedDate;

    private AlertDialog deleteConfirmDialog;

    public static FragmentHome newInstance(String param1) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString("param", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        database = FirebaseFirestore.getInstance();

        if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Shop")) {
            view = inflater.inflate(R.layout.fragment_shop, container, false);
            AppCompatTextView greeting = view.findViewById(R.id.greeting);

            greeting.setText(requireActivity().getString(R.string.welcome_greeting));

            RecyclerView productRecycler = view.findViewById(R.id.product_recycler);
            RecyclerView productFilterRecycler = view.findViewById(R.id.product_filter_recycler);
            notFoundLayout = view.findViewById(R.id.not_found_layout);
            notFoundLayout.setVisibility(View.GONE);


            bannerLayout = view.findViewById(R.id.banner_layout);
            bannerViewPager = view.findViewById(R.id.banner_view_pager);
            circleIndicator = view.findViewById(R.id.circle_indicator);


            DisplayMetrics metrics = getResources().getDisplayMetrics();

            if (metrics != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bannerViewPager.getLayoutParams();

                params.height = (metrics.widthPixels * 9) / 16;

                Log.d("TAG", params.width + "::" + params.height);

                bannerViewPager.setLayoutParams(params);
            }

            viewPagerItemImageList = new ArrayList<>();

            viewPagerAdapter = new BannerSliderAdapter(viewPagerItemImageList);

            bannerViewPager.setClipToPadding(false);
            bannerViewPager.setClipChildren(false);
            bannerViewPager.setOffscreenPageLimit(3);
            bannerViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            circleIndicator.setViewPager(bannerViewPager);

            bannerLayout.setVisibility(View.GONE);

            productRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
            productFilterRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            SimpleItemAnimator itemAnimator = (SimpleItemAnimator) productFilterRecycler.getItemAnimator();

            if (itemAnimator != null) {
                itemAnimator.setSupportsChangeAnimations(false);
            }

            SimpleItemAnimator itemAnimator1 = (SimpleItemAnimator) productRecycler.getItemAnimator();

            if (itemAnimator1 != null) {
                itemAnimator1.setSupportsChangeAnimations(false);
            }
            initializeFilterList();


            productList = new ArrayList<>();
            productAdapter.add(productList);
            productRecycler.setAdapter(productAdapter);

            productFilterAdapter.add(productFilterList);
            productFilterRecycler.setAdapter(productFilterAdapter);

            filterButton = view.findViewById(R.id.filter_button);


            AppCompatEditText searchText = view.findViewById(R.id.product_search_text);


            selectedFilterText = view.findViewById(R.id.selected_filter);
            searchText.addTextChangedListener(this);

            productFilterRecycler.setVisibility(View.GONE);
            selectedFilterText.setVisibility(View.GONE);
            filterButton.setOnClickListener(view1 -> {
                if (productFilterRecycler.getVisibility() == View.VISIBLE) {
                    productFilterRecycler.setVisibility(View.GONE);
                    selectedFilterText.setVisibility(View.GONE);
                    filterButton.setBackgroundColor(ContextCompat.getColor(filterButton.getContext(), android.R.color.transparent));
                } else {
                    productFilterRecycler.setVisibility(View.VISIBLE);
                    selectedFilterText.setVisibility(View.VISIBLE);
                    filterButton.setBackgroundColor(ContextCompat.getColor(filterButton.getContext(), R.color.gold_translucent));
                }
            });

        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Subscriptions")) {
            view = inflater.inflate(R.layout.fragment_subscription, container, false);
            subscriptionFilters = view.findViewById(R.id.subscription_filters);
            subscriptionFilters.setVisibility(View.GONE);

            notFoundLayout = view.findViewById(R.id.not_found_layout);
            notFoundLayout.setVisibility(View.GONE);

            subscriptionRecycler = view.findViewById(R.id.subscription_recycler);
            subscriptionRecycler.setLayoutManager(new LinearLayoutManager(subscriptionRecycler.getContext(), LinearLayoutManager.VERTICAL, false));
            SimpleItemAnimator itemAnimator = (SimpleItemAnimator) subscriptionRecycler.getItemAnimator();

            if (itemAnimator != null) {
                itemAnimator.setSupportsChangeAnimations(false);
            }
            RecyclerView subscriptionFilterRecycler = view.findViewById(R.id.subscription_filter_recycler);
            subscriptionFilterRecycler.setLayoutManager(new LinearLayoutManager(subscriptionFilterRecycler.getContext(), LinearLayoutManager.HORIZONTAL, false));

            SimpleItemAnimator itemAnimator1 = (SimpleItemAnimator) subscriptionFilterRecycler.getItemAnimator();

            if (itemAnimator1 != null) {
                itemAnimator1.setSupportsChangeAnimations(false);
            }
            subscriptionList = new ArrayList<>();
            subscriptionAdapter.add(subscriptionList);
            subscriptionRecycler.setAdapter(subscriptionAdapter);


            subscriptionFilterAdapter.add(subscriptionFilterList);
            subscriptionFilterRecycler.setAdapter(subscriptionFilterAdapter);

        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Calendar")) {
            view = inflater.inflate(R.layout.fragment_calendar, container, false);
            CalendarView calendar = view.findViewById(R.id.calendar);
            AppCompatTextView ordersTitle = view.findViewById(R.id.orders_title);

            orderUpdateTip = view.findViewById(R.id.order_update_tip);

            notFoundLayout = view.findViewById(R.id.not_found_layout);
            notFoundLayout.setVisibility(View.GONE);

            vacationLayout = view.findViewById(R.id.vacation_layout);
            vacationLayout.setVisibility(View.GONE);

            orderList = new ArrayList<>();


            orderRecycler = view.findViewById(R.id.order_recycler);
            orderRecycler.setLayoutManager(new LinearLayoutManager(orderRecycler.getContext(), LinearLayoutManager.VERTICAL, false));

            orderAdapter.add(orderList);
            orderRecycler.setAdapter(orderAdapter);
            SimpleItemAnimator itemAnimator = (SimpleItemAnimator) orderRecycler.getItemAnimator();

            if (itemAnimator != null) {
                itemAnimator.setSupportsChangeAnimations(false);
            }

            ordersTitle.setText(getString(R.string.orders_title, DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(Timestamp.now().toDate().getTime(), 1), "EEE MMM dd, yyyy", true)));


            calendar.setMinDate(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1));
            calendar.setOnDateChangeListener((calendarView, year, month, dayOfMonth) -> {

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(year, month, dayOfMonth);

                ordersTitle.setText(getString(R.string.orders_title, DateUtils.getStringFromMillis(calendar1.getTime().getTime(), "EEE MMM dd, yyyy", true)));
                selectedDate = DateUtils.getStringFromMillis(calendar1.getTime().getTime(), "yyyy-MM-dd", false);
                toggleLayouts(false, false);

                startFetchingOrders();
            });

            orderAdapter.addEventHook(new ClickEventHook<Order>() {
                @Nullable
                @Override
                public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder instanceof Order.OrderViewHolder) {
                        return ((Order.OrderViewHolder) viewHolder).qtyMinusButton;
                    }
                    return null;
                }

                @Override
                public void onClick(@NonNull View view, int position, @NonNull FastAdapter<Order> fastAdapter, @NonNull Order item) {
                    Long quantity = item.getQuantity();

                    if (quantity > 0) {
                        quantity = quantity - 1;
                        item.isOrderUpdated = !quantity.equals(item.getQuantity());
                        item.setQuantity(quantity);
                        fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(item));
                    }
                }
            });

            orderAdapter.addEventHook(new ClickEventHook<Order>() {
                @Nullable
                @Override
                public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder instanceof Order.OrderViewHolder) {
                        return ((Order.OrderViewHolder) viewHolder).qtyPlusButton;
                    }
                    return null;
                }

                @Override
                public void onClick(@NonNull View view, int position, @NonNull FastAdapter<Order> fastAdapter, @NonNull Order item) {
                    Long quantity = item.getQuantity();

                    if (quantity < 99999) {
                        quantity = quantity + 1;
                        item.isOrderUpdated = !quantity.equals(item.getQuantity());
                        item.setQuantity(quantity);
                        fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(item));
                    }

                }
            });


            orderAdapter.addEventHook(new ClickEventHook<Order>() {
                @Nullable
                @Override
                public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder instanceof Order.OrderViewHolder) {
                        return ((Order.OrderViewHolder) viewHolder).updateOrderButton;
                    }
                    return null;
                }

                @Override
                public void onClick(@NonNull View view, int position, @NonNull FastAdapter<Order> fastAdapter, @NonNull Order item) {
                    database.collection(AppConstants.BULK_QUANTITY_UPDATE_COLLECTION)
                            .where(Filter.and(Filter.equalTo("customer_id", item.customer_id),
                                    Filter.equalTo("subscription_id", item.subscription_id),
                                    Filter.equalTo("delivery_date", selectedDate)))
                            .limit(1)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult() != null && !task.getResult().isEmpty()) {

                                        BulkUpdateQuantity bulkUpdateQuantity = task.getResult().getDocuments().get(0).toObject(BulkUpdateQuantity.class);

                                        if (bulkUpdateQuantity != null) {
                                            bulkUpdateQuantity.quantity = item.quantity;

                                            task.getResult().getDocuments().get(0).getReference().set(bulkUpdateQuantity)
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(requireActivity(), R.string.update_successful, Toast.LENGTH_SHORT).show();
                                                            item.isOrderUpdated = false;
                                                            fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(item));
                                                            logCustomerActivity(item, "Quantity Updated", bulkUpdateQuantity.delivery_date);
                                                            startFetchingOrders();
                                                        } else {
                                                            Toast.makeText(requireActivity(), R.string.update_error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    } else {
                                        BulkUpdateQuantity bulkUpdateQuantity = new BulkUpdateQuantity();

                                        bulkUpdateQuantity.customer_id = item.customer_id;
                                        try {
                                            bulkUpdateQuantity.date = new Timestamp(DateUtils.getDateFromString(selectedDate, "yyyy-MM-dd", false));
                                        } catch (ParseException e) {
                                            bulkUpdateQuantity.date = new Timestamp(new Date(calendar.getDate()));
                                        }
                                        bulkUpdateQuantity.delivery_date = selectedDate;
                                        bulkUpdateQuantity.product_name = item.product_name;
                                        bulkUpdateQuantity.quantity = item.quantity;
                                        bulkUpdateQuantity.status = "1";
                                        bulkUpdateQuantity.subscription_id = item.subscription_id;

                                        database.collection(AppConstants.BULK_QUANTITY_UPDATE_COLLECTION).add(bulkUpdateQuantity)
                                                .addOnCompleteListener(task12 -> {
                                                    if (task12.isSuccessful()) {
                                                        Toast.makeText(requireActivity(), R.string.update_successful, Toast.LENGTH_SHORT).show();
                                                        item.isOrderUpdated = false;
                                                        fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(item));
                                                        logCustomerActivity(item, "Quantity Updated", bulkUpdateQuantity.delivery_date);
                                                        startFetchingOrders();
                                                    } else {
                                                        Toast.makeText(requireActivity(), R.string.update_error, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(requireActivity(), R.string.update_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });


        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Wallet")) {
            view = inflater.inflate(R.layout.fragment_wallet, container, false);

            walletBalance = view.findViewById(R.id.wallet_balance);
            LinearLayout rechargeLayout = view.findViewById(R.id.recharge_layout);

            rechargeLayout.setOnClickListener(view -> {
                Intent intent = new Intent(requireActivity(), RechargeActivity.class);
                intent.putExtra("type", "online");
                startActivity(intent);
            });

            transactions = new ArrayList<>();


            RecyclerView transactionRecycler = view.findViewById(R.id.transaction_recycler);
            transactionRecycler.setLayoutManager(new LinearLayoutManager(transactionRecycler.getContext(), LinearLayoutManager.VERTICAL, false));

            transactionAdapter.add(transactions);
            transactionRecycler.setAdapter(transactionAdapter);


            SimpleItemAnimator itemAnimator = (SimpleItemAnimator) transactionRecycler.getItemAnimator();

            if (itemAnimator != null) {
                itemAnimator.setSupportsChangeAnimations(false);
            }


        }

        AppCompatTextView loginNowButton = view.findViewById(R.id.login_now_button);

        if (loginNowButton != null) {
            loginNowButton.setOnClickListener(view1 -> ((BaseActivity) requireActivity()).startLoginFlow("LoginActivity"));
        }


        ImageView menuDrawerButton = view.findViewById(R.id.menu_drawer);
        menuDrawerButton.setOnClickListener(view1 -> ((MainActivity) requireActivity()).openDrawer());
        if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Shop")) {
            getInventory();
            getBanners();
        }

        if (view != null && isAdded()) {
            profileImage = view.findViewById(R.id.profile_image);
            numCartItems = view.findViewById(R.id.num_cart_items);
            menuCart = view.findViewById(R.id.menu_cart);
        }

        return view;
    }

    private void logCustomerActivity(Order item, String action, String deliveryDate) {
        CustomerActivity customerActivity = new CustomerActivity();
        customerActivity.action = action;
        customerActivity.created_date = Timestamp.now();
        customerActivity.customer_id = item.customer_id;
        customerActivity.customer_name = item.customer_name;
        customerActivity.customer_phone = item.customer_phone;
        customerActivity.description = action + " for order: " + item.subscription_id + " quantity changed to " + item.quantity + " for " + deliveryDate + " by " + item.customer_name;
        customerActivity.object = "Bulk Quantity Update";
        customerActivity.user = item.customer_name;
        customerActivity.platform = "Android";

        database.collection("customer_activities")
                .add(customerActivity);
    }

    private void getBanners() {
        viewPagerItemImageList.clear();
        database.collection(AppConstants.BANNERS_COLLECTION)
                .where(Filter.and(
                        Filter.lessThanOrEqualTo("start_date_time", Timestamp.now()),
                        Filter.greaterThanOrEqualTo("end_date_time", Timestamp.now()),
                        Filter.or(Filter.equalTo("platform", "Both"),
                                Filter.equalTo("platform", "Mobile App")),
                        Filter.equalTo("status", "Active"))
                )
                .orderBy("banner_order")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                viewPagerItemImageList.add(new ViewPagerItemImage(snapshot.getString("image")));
                            }

                            Log.d("TAG", "getBanners: got some");

                            if (!viewPagerItemImageList.isEmpty()) {
                                viewPagerAdapter.notifyDataSetChanged();
                                bannerViewPager.setAdapter(viewPagerAdapter);
                                circleIndicator.setViewPager(bannerViewPager);
                                bannerLayout.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Log.d("TAG", "getBanners: got nothing 1" + task.getException());

                            bannerLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d("TAG", "getBanners: got nothing 2" + task.getException());

                        bannerLayout.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Shop")) {
            getBanners();
        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Subscriptions")) {
            getSubscriptions();
        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Calendar")) {
            selectedDate = DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "yyyy-MM-dd", false);
            startFetchingOrders();
        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Wallet")) {
            getWalletBalanceAndTxns();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (view != null && isAdded()) {

            checkLogin();
            menuCart.setOnClickListener(view -> startActivity(new Intent(requireActivity(), CheckoutActivity.class)));

            profileImage.setOnClickListener(view -> startActivity(new Intent(requireActivity(), ProfileActivity.class)));
            new Cart(this).setCartValue();
            checkVacation();
        }
    }

    /**
     * @noinspection DataFlowIssue
     */
    public void getSubscriptions() {

        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState();
        if (customerID != null && !customerID.isEmpty()) {

            subscriptionAdapter.clear();
            subscriptionList.clear();
            subscriptionAdapter.notifyAdapterDataSetChanged();

            database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
                    .where(Filter.equalTo("customer_id", customerID))
                    .orderBy("start_date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && isAdded()) {

                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Subscription subscription = new Subscription();

                                    if (document.get("interval") != null &&
                                            document.get("interval") instanceof Long && !document.get("interval").equals(NaN)) { //Mitigating for interval as NaN in CUSTOM subscription type
                                        subscription = document.toObject(Subscription.class);
                                    } else {
                                        subscription.coupon_code = (String) document.get("coupon_code");
                                        subscription.created_date = (Timestamp) document.get("created_date");
                                        subscription.customer_address = (String) document.get("customer_address");
                                        subscription.customer_id = (String) document.get("customer_id");
                                        subscription.customer_name = (String) document.get("customer_name");
                                        subscription.customer_phone = (String) document.get("customer_phone");
                                        subscription.end_date = document.get("end_date"); //Mitigating for possibility of end_date being either Timestamp or String
                                        subscription.sunday = document.get("sunday") != null ? (Long) document.get("sunday") : 0;
                                        subscription.monday = document.get("monday") != null ? (Long) document.get("monday") : 0;
                                        subscription.tuesday = document.get("tuesday") != null ? (Long) document.get("tuesday") : 0;
                                        subscription.wednesday = document.get("wednesday") != null ? (Long) document.get("wednesday") : 0;
                                        subscription.thursday = document.get("thursday") != null ? (Long) document.get("thursday") : 0;
                                        subscription.friday = document.get("friday") != null ? (Long) document.get("friday") : 0;
                                        subscription.saturday = document.get("saturday") != null ? (Long) document.get("saturday") : 0;
                                        subscription.hub_name = (String) document.get("hub_name");
                                        subscription.delivering_to = (String) document.get("delivering_to");
                                        subscription.delivered_by = (String) document.get("delivered_by");
                                        subscription.interval = document.get("interval");
                                        subscription.next_delivery_date = (String) document.get("next_delivery_date");
                                        subscription.package_unit = (String) document.get("package_unit");
                                        subscription.price = (Long) document.get("price");
                                        subscription.product_name = (String) document.get("product_name");
                                        subscription.quantity = (Long) document.get("quantity");
                                        subscription.resume_date = (Timestamp) document.get("resume_date");
                                        subscription.start_date = (Timestamp) document.get("start_date");
                                        subscription.status = (String) document.get("status");
                                        subscription.subscription_id = (String) document.get("subscription_id");
                                        subscription.subscription_type = (String) document.get("subscription_type");
                                        subscription.updated_date = (Timestamp) document.get("updated_date");
                                        subscription.reason = (String) document.get("reason");
                                    }

                                    subscription.mContext = FragmentHome.this;
                                    subscriptionList.add(subscription);
                                    subscriptionAdapter.add(subscription);
                                    subscriptionAdapter.notifyAdapterItemChanged(subscriptionAdapter.getAdapterItemCount() - 1);

                                }

                                if (!subscriptionAdapter.getAdapterItems().isEmpty()) {
                                    subscriptionFilterList = new ArrayList<>();


                                    subscriptionFilterList.add(new Item(ContextCompat.getString(requireContext(), R.string.all), "Subscription"));
                                    subscriptionFilterList.add(new Item(ContextCompat.getString(requireActivity(), R.string.active), "Subscription"));
                                    subscriptionFilterList.add(new Item(ContextCompat.getString(requireActivity(), R.string.paused), "Subscription"));
                                    //subscriptionFilterList.add(new Item(ContextCompat.getString(requireActivity(), R.string.ended), "Subscription"));


                                    subscriptionFilterList.get(0).setSelected(true);
                                    subscriptionFilterAdapter.clear();
                                    subscriptionFilterAdapter.add(subscriptionFilterList);

                                    subscriptionFilterAdapter.notifyAdapterDataSetChanged();

                                    subscriptionFilterAdapter.addEventHook(new ClickEventHook<Item>() {

                                        @Nullable
                                        @Override
                                        public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                                            if (viewHolder instanceof Item.ItemViewHolder) {
                                                return ((Item.ItemViewHolder) viewHolder).itemText;
                                            }
                                            return null;
                                        }

                                        @Override
                                        public void onClick(@NonNull View view, int i, @NonNull FastAdapter<Item> fastAdapter, @NonNull Item item) {
                                            subscriptionFilterAdapter.getAdapterItems().forEach(itemModel -> {
                                                itemModel.setSelected(false);
                                                subscriptionFilterAdapter.notifyAdapterItemChanged(subscriptionFilterAdapter.getAdapterPosition(itemModel));
                                            });
                                            item.setSelected(true);
                                            subscriptionFilterAdapter.notifyAdapterItemChanged(subscriptionFilterAdapter.getAdapterPosition(item));
                                            filterSubscriptionByStatus(item.getItemTitle());
                                        }
                                    });


                                    subscriptionFilters.setVisibility(View.VISIBLE);
                                    notFoundLayout.setVisibility(View.GONE);
                                }


                            } else {
                                subscriptionFilters.setVisibility(View.GONE);
                                notFoundLayout.setVisibility(View.VISIBLE);

                            }
                            toggleProgressBar(false);

                        } else {
                            toggleProgressBar(false);
                        }
                    });


            subscriptionAdapter.addEventHook(new ClickEventHook<Subscription>() {
                @Nullable
                @Override
                public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder instanceof Subscription.SubscriptionViewHolder) {
                        return ((Subscription.SubscriptionViewHolder) viewHolder).editSubscription;
                    }

                    return null;
                }

                @Override
                public void onClick(@NonNull View view, int i, @NonNull FastAdapter<Subscription> fastAdapter, @NonNull Subscription item) {
                    editSubscription(item.toString(), item.subscription_type);
                }
            });

            subscriptionAdapter.addEventHook(new ClickEventHook<Subscription>() {
                @Nullable
                @Override
                public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder instanceof Subscription.SubscriptionViewHolder) {
                        return ((Subscription.SubscriptionViewHolder) viewHolder).deleteSubscription;
                    }

                    return null;
                }

                @Override
                public void onClick(@NonNull View view, int i, @NonNull FastAdapter<Subscription> fastAdapter, @NonNull Subscription item) {
                    if (isAdded()) {
                        if (deleteConfirmDialog == null || !deleteConfirmDialog.isShowing()) {
                            deleteConfirmDialog = new AlertDialog.Builder(requireActivity(), R.style.AlertDialogStyle)
                                    .setTitle(R.string.delte_subscription)
                                    .setMessage(String.format(getString(R.string.delete_subscription_warning),
                                            item.subscription_type, item.product_name, item.package_unit))
                                    .setCancelable(true)
                                    .setPositiveButton(android.R.string.yes, (dialogInterface, i1) -> {
                                        deleteSubscription(item.subscription_id);
                                    })
                                    .setNegativeButton(android.R.string.no, (dialogInterface, i12) -> {
                                        dialogInterface.dismiss();
                                    })
                                    .create();

                            deleteConfirmDialog.show();
                        }
                    }
                }
            });

        } else {
            checkLogin();
        }
    }

    private void editSubscription(String subscriptionString, String subscriptionType) {
        FragmentSubscribe fragmentSubscribe = FragmentSubscribe.newInstance(subscriptionString, subscriptionType);
        fragmentSubscribe.getLifecycle().addObserver((LifecycleEventObserver) (lifecycleOwner, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Subscriptions")) {
                    getSubscriptions();
                }
            }
        });


        fragmentSubscribe.show(requireActivity().getSupportFragmentManager(), "edit_subscription");
    }

    private void deleteSubscription(@NonNull String subscriptionID) {
        if (!subscriptionID.isEmpty() && isAdded()) {
            database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
                    .where(Filter.and(
                            Filter.equalTo("customer_id", ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState()),
                            Filter.equalTo("subscription_id", subscriptionID)
                    ))
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                document.getReference()
                                        .delete()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {

                                                 // Log customer activity
                                                CustomerActivity customerActivity = new CustomerActivity();
                                                customerActivity.action = "Subscription Deletion";
                                                customerActivity.created_date = Timestamp.now();
                                                customerActivity.customer_id = document.getString("customer_id");
                                                customerActivity.customer_name = document.getString("customer_name");
                                                customerActivity.customer_phone = document.getString("customer_phone");
                                                customerActivity.description = "Subscription deleted: " + document.getString("subscription_id") + " subsctiption type: " + document.getString("subscription_type") + " by " + document.getString("customer_name");
                                                customerActivity.object = "Subscription Deleted";
                                                customerActivity.user = document.getString("customer_name");
                                                customerActivity.platform = "Android";

                                                database.collection("customer_activities")
                                                    .add(customerActivity)
                                                    .addOnSuccessListener(activityDocumentReference -> {
                                                        Toast.makeText(requireActivity(), R.string.subscription_delete_success, Toast.LENGTH_SHORT).show();
                                                        getSubscriptions();
                                                    });
                                            } else {
                                                Toast.makeText(requireActivity(), R.string.subscription_delete_error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(requireActivity(), R.string.subscription_delete_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireActivity(), R.string.subscription_delete_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void showConfirmDialog(Object item) {
        if (item instanceof Subscription) {
            View[] dialogView = {null};

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle);

            dialogView[0] = LayoutInflater.from(requireContext()).inflate(R.layout.layout_select_resume_date,
                    requireActivity().findViewById(R.id.subscription_recycler), false);

            AppCompatTextView newStatusHeading = dialogView[0].findViewById(R.id.new_status_heading);
            AppCompatTextView statusChangeWarning = dialogView[0].findViewById(R.id.status_change_warning);
            FrameLayout datePicker = dialogView[0].findViewById(R.id.date_picker);
            AppCompatTextView resumeDateInput = dialogView[0].findViewById(R.id.resume_date_input);
            AppCompatImageView resumeDateInputCal = dialogView[0].findViewById(R.id.resume_date_input_cal);


            if (((Subscription) item).status.equals("1")) {
                newStatusHeading.setText(getString(R.string.pause_subscription));
                statusChangeWarning.setText(getString(R.string.pause_subscription_alert));
                datePicker.setVisibility(View.VISIBLE);

                resumeDateInput.setOnTouchListener((view, motionEvent) -> {
                    resumeDateInputCal.performClick();
                    return false;
                });

                resumeDateInputCal.setOnClickListener(view -> {
                    if (datePickerDialog != null && datePickerDialog.isShowing()) {
                        return;
                    }

                    if (datePickerDialog == null) {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);


                        datePickerDialog = new DatePickerDialog(requireActivity(),
                                (view1, year1, monthOfYear, dayOfMonth) -> resumeDateInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year1)),
                                year, month, day);

                        datePickerDialog.getDatePicker().setMinDate(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1));
                        datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                        datePickerDialog.show();

                    }
                });
            } else {
                newStatusHeading.setText(getString(R.string.resume_subscription));
                statusChangeWarning.setText(getString(R.string.resume_subscription_alert));
                datePicker.setVisibility(View.GONE);
            }

            alertDialog.setPositiveButton(((Subscription) item).status.equals("1") ? R.string.pause_subscription : R.string.resume_subscription,
                            (dialog, whichButton) -> {
                                if (((Subscription) item).status.equals("1")) {
                                    if (dialogView[0] != null) {
                                        ((Subscription) item).status = "0";
                                        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                        try {
                                            String text = resumeDateInput.getText().toString();

                                            if (!text.isEmpty()) {
                                                Date date = format.parse(text);
                                                if (date != null) {
                                                    ((Subscription) item).resume_date = new Timestamp(date);

                                                    if (((Subscription) item).resume_date.toDate().getTime() >
                                                            ((Subscription) item).start_date.toDate().getTime()) {
                                                        ((Subscription) item).next_delivery_date = DateUtils.getStringFromMillis(((Subscription) item).resume_date.toDate().getTime(), "yyyy-MM-dd", false);
                                                    } else {
                                                        ((Subscription) item).next_delivery_date = DateUtils.getStringFromMillis(((Subscription) item).start_date.toDate().getTime(), "yyyy-MM-dd", false);
                                                    }
                                                }
                                            } else {
                                                ((Subscription) item).resume_date = ((Subscription) item).start_date;
                                            }
                                            updateAdapterWithItem(item, true);
                                        } catch (ParseException ignored) {
                                        }
                                    }
                                } else {
                                    if (dialogView[0] != null) {
                                        ((Subscription) item).status = "1";
                                        ((Subscription) item).resume_date = Timestamp.now();
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(((Subscription) item).resume_date.toDate());
                                        int hour = calendar.get(Calendar.HOUR_OF_DAY);

                                        if (hour < 23) {
                                            // Set to tomorrow's date
                                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                                        } else {
                                            // Set to the day after tomorrow
                                            calendar.add(Calendar.DAY_OF_MONTH, 2);
                                        }
                                        ((Subscription) item).next_delivery_date = DateUtils.getStringFromMillis(calendar.getTimeInMillis(), "yyyy-MM-dd", false);
                                        updateAdapterWithItem(item, true);
                                    }
                                }
                            })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> updateAdapterWithItem(item, false));

            alertDialog.setView(dialogView[0]);

            alertDialog.show();

        }
    }

    private void updateSubscriptionToDB(Subscription subscription) {
        database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
                .whereEqualTo("subscription_id", subscription.subscription_id)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            document.getReference().set(FirebaseSubscription.getFirebaseSubscription(subscription))
                            .addOnSuccessListener(aVoid -> {
                                // Log customer activity
                                CustomerActivity customerActivity = new CustomerActivity();
                                customerActivity.action = subscription.status.equals("1") ? "Subscription Resumed" : "Subscription Paused";
                                customerActivity.created_date = Timestamp.now();
                                customerActivity.customer_id = subscription.customer_id;
                                customerActivity.customer_name = subscription.customer_name;
                                customerActivity.customer_phone = subscription.customer_phone;
                                customerActivity.description = subscription.status.equals("1") 
                                    ? "Subscription resumed for subscriotion: " + subscription.subscription_id + " by " + subscription.customer_name
                                    : "Subscription paused for subscriotion: " + subscription.subscription_id + " by " + subscription.customer_name;
                                customerActivity.object = "Subscription Status Updated";
                                customerActivity.user = subscription.customer_name;
                                customerActivity.platform = "Android";

                                database.collection("customer_activities")
                                    .add(customerActivity);
                            });
                    }
                }
            });
    }

    private void filterProductsBySearch(String searchString) {
        productAdapter.clear();
        for (Item item : productFilterAdapter.getAdapterItems()) {
            if (item.isSelected()) {
                filterProductsByCategory(item.getItemTitle());
                break;
            }
        }
        if (searchString != null && !searchString.isEmpty()) {
            tempFilterList = new ArrayList<>();

            for (Product product : productAdapter.getAdapterItems()) {
                if (product.productName.toLowerCase().contains(searchString.toLowerCase()))
                    tempFilterList.add(product);
            }


            productAdapter.clear();
            if (!tempFilterList.isEmpty()) {
                productAdapter.add(tempFilterList);
                notFoundLayout.setVisibility(View.GONE);
            } else {
                notFoundLayout.setVisibility(View.VISIBLE);
            }
        }
    }


    private void filterProductsByCategory(@NonNull String category) {
        if (category.equals(getString(R.string.all))) {
            productAdapter.clear();
            if (productList != null && !productList.isEmpty()) {
                productAdapter.add(productList);
                notFoundLayout.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        } else if (category.equals(getString(R.string.recommended))) {

            tempFilterList = new ArrayList<>();

            for (Product product : productList) {
                if (product.rating >= 4.7)
                    tempFilterList.add(product);
            }


            productAdapter.clear();
            if (!tempFilterList.isEmpty()) {
                productAdapter.add(tempFilterList);
                notFoundLayout.setVisibility(View.GONE);
            } else {
                notFoundLayout.setVisibility(View.VISIBLE);
            }
        } else {
            tempFilterList = new ArrayList<>();

            for (Product product : productList) {
                if (category.equals(product.category))
                    tempFilterList.add(product);
            }

            productAdapter.clear();

            if (!tempFilterList.isEmpty()) {
                productAdapter.add(tempFilterList);
                notFoundLayout.setVisibility(View.GONE);
            } else {
                notFoundLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void filterSubscriptionByStatus(String status) {
        if (status.equals(getString(R.string.all))) {
            subscriptionAdapter.clear();
            subscriptionAdapter.add(subscriptionList);
            notFoundLayout.setVisibility(subscriptionFilterList.isEmpty() ? View.VISIBLE : View.GONE);

        } else {
            List<Subscription> temp = new ArrayList<>();
            for (Subscription subscription : subscriptionList) {
                if (status.equals(getString(R.string.active))) {
                    if (subscription.status.equals("1")) {
                        temp.add(subscription);
                    }
                } else if (status.equals(getString(R.string.paused))) {
                    if (subscription.status.equals("0") && (subscription.start_date.compareTo(subscription.resume_date) == 0 || subscription.resume_date.compareTo(Timestamp.now()) > 0)) {
                        temp.add(subscription);
                    }
                } else if (status.equals(getString(R.string.ended))) {
                    if (subscription.status.equals("0") && subscription.end_date != null &&
                            (subscription.end_date instanceof Timestamp && ((Timestamp) subscription.end_date).compareTo(Timestamp.now()) < 0)) {
                        temp.add(subscription);
                    }
                }
            }

            subscriptionAdapter.clear();
            if (!temp.isEmpty()) {
                subscriptionAdapter.add(temp);
                notFoundLayout.setVisibility(View.GONE);
            } else {
                notFoundLayout.setVisibility(View.VISIBLE);
            }
        }
    }


    public void updateAdapterWithItem(Object item, boolean shouldUpdateDB) {
        subscriptionRecycler.post(() -> {
            if (item instanceof Subscription) {
                subscriptionAdapter.notifyAdapterItemChanged(subscriptionAdapter.getAdapterPosition((Subscription) item));
            }

            final String[] selected = {subscriptionFilterAdapter.getAdapterItem(0).getItemTitle()};
            subscriptionFilterAdapter.getAdapterItems().forEach(item1 -> {
                if (item1.isSelected()) {
                    selected[0] = item1.getItemTitle();
                }
            });

            filterSubscriptionByStatus(selected[0]);

            if (shouldUpdateDB && item instanceof Subscription) {
                updateSubscriptionToDB(((Subscription) item));
            }
        });


    }


    private void getInventory() {
        toggleProgressBar(true);
        productAdapter.clear();
        productList = new ArrayList<>();
        productFilterAdapter.clear();
        initializeFilterList();

        database.collection(AppConstants.PRODUCTS_DATA_COLLECTION)
                .whereEqualTo("inStock", true)
                .orderBy("productName")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);

                            Item newCat = new Item(product.category, "Category");
                            productFilterList.add(newCat);

                            productList.add(product);
                            productAdapter.add(product);
                            productAdapter.notifyAdapterItemChanged(productAdapter.getAdapterPosition(product));
                        }

                        if (!productList.isEmpty()) {

                            List<Item> tempList = productFilterList.subList(2, productFilterList.size() - 1);
                            tempList.sort((c1, c2) -> {
                                if (c1.getItemTitle() != null && c2.getItemTitle() != null) {
                                    return c1.getItemTitle().compareTo(c2.getItemTitle());
                                }
                                return 0;
                            });

                            productFilterList = productFilterList.subList(0, 2);
                            productFilterList.addAll(tempList);

                            final String[] filterText = {""};


                            productFilterList.removeIf(item -> {
                                if (filterText[0].equals(item.getItemTitle())) {
                                    return true;
                                } else {
                                    filterText[0] = item.getItemTitle();
                                    return false;
                                }
                            });


                            productFilterList.get(0).setSelected(true);
                            productFilterAdapter.add(productFilterList);
                            productFilterAdapter.notifyAdapterDataSetChanged();

                            setSelectedFilterText();

                            productFilterAdapter.addEventHook(new ClickEventHook<Item>() {
                                @Nullable
                                @Override
                                public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                                    if (viewHolder instanceof Item.ItemViewHolder) {
                                        return ((Item.ItemViewHolder) viewHolder).itemText;
                                    }
                                    return null;
                                }

                                @Override
                                public void onClick(@NonNull View view, int position, @NonNull FastAdapter<Item> fastAdapter, @NonNull Item item) {
                                    productFilterAdapter.getAdapterItems().forEach(itemModel -> {
                                        itemModel.setSelected(false);
                                        productFilterAdapter.notifyAdapterItemChanged(productFilterAdapter.getAdapterPosition(itemModel));
                                    });

                                    item.setSelected(true);
                                    productFilterAdapter.notifyAdapterItemChanged(productFilterAdapter.getAdapterPosition(item));
                                    setSelectedFilterText();
                                    filterProductsByCategory(item.getItemTitle());
                                }
                            });
                            toggleProgressBar(false);
                        }
                    } else {
                        //   Log.d("TAG", "getInventory: " + task.getException());
                        toggleProgressBar(false);
                    }
                });


        productAdapter.addEventHook(new ClickEventHook<Product>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof Product.ShopProductViewHolder) {
                    return ((Product.ShopProductViewHolder) viewHolder).productCard;
                }
                return null;
            }

            @Override
            public void onClick(@NonNull View view, int i, @NonNull FastAdapter<Product> fastAdapter, @NonNull Product item) {
                Intent intent = new Intent(FragmentHome.this.requireActivity(), DescriptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("productID", item.productId);

                startActivity(intent);
            }
        });
    }

    private void setSelectedFilterText() {
        final String[] selectedFilter = {""};

        productFilterList.forEach(item -> {
            if (item.isSelected()) {
                selectedFilter[0] = item.getItemTitle();
                selectedFilterText.setText(selectedFilter[0].equals("Recommended") ? selectedFilter[0] + " (Based on Popularity)" : selectedFilter[0]);
            }
        });
    }

    private void toggleProgressBar(boolean shouldShow) {
        try {
            requireActivity().runOnUiThread(() -> {
                if (requireActivity() instanceof MainActivity) {
                    if (((MainActivity) requireActivity()).progressBar != null) {
                        ((MainActivity) requireActivity()).progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                    }
                }
            });
        } catch (IllegalStateException ignored) {
        }
    }

    private void initializeFilterList() {
        productFilterList.clear();
        Item allCat = new Item(requireActivity().getString(R.string.all), "Category");
        allCat.setSelected(true);
        productFilterList.add(allCat);
        Item recommendedCat = new Item(requireActivity().getString(R.string.recommended), "Category");
        productFilterList.add(recommendedCat);
    }

    public void checkLogin() {
        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState();
        String userHash = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getUserHashFromLoginState();
        if (((WhyteFarmsApplication) requireActivity().getApplicationContext()).isLoggedIn()) {
            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                    .where(Filter.and(Filter.equalTo("customer_id", customerID), Filter.equalTo("userHash", userHash)))
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && isAdded()) {
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
                            if (isAdded()) {
                                onError(new Exception(getString(R.string.cannot_fetch_customer_data)));
                            }
                        }
                    });

        } else {
            onResultReceived(null, AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH, AppConstants.ACTION_INVALIDATE_LOGIN);
        }
    }


    private void startFetchingOrders() {
        orderUpdateTip.setVisibility(View.GONE);
        orderList.clear();
        orderAdapter.clear();
        orderAdapter.notifyAdapterDataSetChanged();

        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState();
        if (customerID != null && !customerID.isEmpty()) {
            database.collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                    .whereEqualTo("customer_id", customerID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                boolean isOnVacation = false;
                                Date today = new Date();
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    Vacation vacation = documentSnapshot.toObject(Vacation.class);
                                    if (vacation != null) {
                                        Date startDate = vacation.start_date.toDate();
                                        Date endDate = vacation.end_date.toDate();

                                        if (startDate.compareTo(today) <= 0 && endDate.compareTo(today) >= 0) {
                                            isOnVacation = true;
                                            break;
                                        }
                                    }
                                }

                                if (isOnVacation) {
                                    toggleLayouts(false, true);
                                    toggleProgressBar(false);
                                } else {
                                    onResultReceived(null, AppConstants.RESULT_TYPE_VACATIONS_FETCH, AppConstants.ACTION_NO_VACATIONS);
                                }

                            } else {
                                onResultReceived(null, AppConstants.RESULT_TYPE_VACATIONS_FETCH, AppConstants.ACTION_NO_VACATIONS);
                            }
                        }
                    });
        } else {
            checkLogin();
        }

    }


    private void getWalletBalanceAndTxns() {
        toggleProgressBar(true);
        String customerID = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState();
        if (customerID != null && !customerID.isEmpty()) {

            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                    .whereEqualTo("customer_id", customerID)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && isAdded()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);
                                if (customer != null) {
                                    walletBalance.setText(String.format(Locale.getDefault(), "₹%s", new DecimalFormat("0.00").format(customer.wallet_balance)));
                                }
                                toggleProgressBar(false);


                            } else {
                                toggleProgressBar(false);
                            }

                        }
                    });


            transactions.clear();
            transactionAdapter.clear();
            transactionAdapter.notifyAdapterDataSetChanged();

            toggleProgressBar(true);

            database.collection(AppConstants.WALLET_HISTORY_COLLECTION)
                    .whereEqualTo("customer_id", customerID)
                    .orderBy("created_date", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && isAdded()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {

                                for (DocumentSnapshot document : task.getResult()) {
                                    Transaction transaction = document.toObject(Transaction.class);
                                    if (transaction != null) {
                                        transactions.add(transaction);
                                        transactionAdapter.add(transaction);
                                    }
                                }
                                transactionAdapter.notifyAdapterDataSetChanged();
                            } else {
                                if (isAdded()) {
                                    Toast.makeText(requireActivity(), R.string.failed_to_fetch_transactions, Toast.LENGTH_SHORT).show();
                                }
                            }
                            toggleProgressBar(false);
                        }
                    });
        } else {
            checkLogin();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        filterProductsBySearch(String.valueOf(charSequence));
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }


    private void toggleLayouts(boolean hasNoOrders, boolean isOnVacation) {
        notFoundLayout.setVisibility(hasNoOrders ? View.VISIBLE : View.GONE);
        vacationLayout.setVisibility(isOnVacation ? View.VISIBLE : View.GONE);
        orderRecycler.setVisibility(!hasNoOrders && !isOnVacation ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onResultReceived(@Nullable Object result, String resultType, String action) {
        if (AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH.equals(resultType) && AppConstants.ACTION_VALIDATE_LOGIN.equals(action)) {
            if (view != null) {
                View loginNeeded = view.findViewById(R.id.login_needed);
                if (loginNeeded != null) {
                    loginNeeded.setVisibility(View.GONE);
                }
                if (result != null) {
                    Customer customer = ((DocumentSnapshot) result).toObject(Customer.class);
                    if (customer != null) {
                        ((AppCompatTextView) view.findViewById(R.id.greeting)).setText(String.format(Locale.getDefault(),
                                requireActivity().getString(R.string.welcome_greeting) + ", %s", !customer.customer_name.isEmpty() ?
                                        customer.customer_name : requireActivity().getString(R.string.user)));


                        Glide.with(view.getContext())
                                .load(!customer.customer_image.isEmpty() ? customer.customer_image : AppConstants.DEFAULT_USER_PROFILE_IMAGE)
                                .dontAnimate()
                                .skipMemoryCache(true)
                                .priority(Priority.IMMEDIATE)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .placeholder(R.drawable.ic_picture_placeholder)
                                .into(profileImage);

                    }
                }
            }
            toggleProgressBar(false);
        } else if (AppConstants.RESULT_TYPE_CUSTOMER_DATA_FETCH.equals(resultType) && AppConstants.ACTION_INVALIDATE_LOGIN.equals(action)) {
            if (view != null) {
                View loginNeeded = view.findViewById(R.id.login_needed);
                if (loginNeeded != null) {
                    loginNeeded.setVisibility(View.VISIBLE);
                }
            }
            toggleProgressBar(false);
        } else if (AppConstants.RESULT_TYPE_VACATIONS_FETCH.equals(resultType) && AppConstants.ACTION_NO_VACATIONS.equals(action)) {
            String customerID = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState();
            // Timestamp selectedTimestamp;
            //selectedTimestamp = new Timestamp(DateUtils.getDateFromString(selectedDate, "yyyy-MM-dd", false));
            toggleProgressBar(true);


            database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
                    .where(Filter.and(
                            Filter.equalTo("customer_id", customerID),
                            // Filter.lessThanOrEqualTo("start_date", selectedTimestamp),
                            //Filter.greaterThanOrEqualTo("end_date", selectedTimestamp),
                            Filter.equalTo("status", "1")))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    Subscription subscription = documentSnapshot.toObject(Subscription.class);
                                    if (subscription != null) {
                                        onResultReceived(subscription, AppConstants.RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS, AppConstants.ACTION_CHECK_BULK_UPDATE_QUANTITY);
                                    }
                                }
                            } else {
                                toggleLayouts(true, false);
                                toggleProgressBar(false);
                            }
                        } else {
                            toggleLayouts(true, false);
                            toggleProgressBar(false);
                        }
                    });
        } else if (AppConstants.RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS.equals(resultType) && AppConstants.ACTION_CHECK_BULK_UPDATE_QUANTITY.equals(action)) {
            if (result != null) {
                Subscription subscription = (Subscription) result;

                database.collection(AppConstants.BULK_QUANTITY_UPDATE_COLLECTION)
                        .where(Filter.and(
                                Filter.equalTo("customer_id", subscription.customer_id),
                                Filter.equalTo("subscription_id", subscription.subscription_id),
                                Filter.equalTo("delivery_date", selectedDate)
                        ))
                        .limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                        BulkUpdateQuantity bulkUpdateQuantity = documentSnapshot.toObject(BulkUpdateQuantity.class);

                                        if (bulkUpdateQuantity != null) {
                                            if (subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
                                                try {
                                                    String weekday = DateUtils.getWeekDayFromDate(selectedDate, "yyyy-MM-dd", false);

                                                    switch (weekday) {
                                                        case AppConstants.WEEKDAY_SUNDAY:
                                                            subscription.sunday = bulkUpdateQuantity.quantity;
                                                            break;
                                                        case AppConstants.WEEKDAY_MONDAY:
                                                            subscription.monday = bulkUpdateQuantity.quantity;
                                                            break;
                                                        case AppConstants.WEEKDAY_TUESDAY:
                                                            subscription.tuesday = bulkUpdateQuantity.quantity;
                                                            break;
                                                        case AppConstants.WEEKDAY_WEDNESDAY:
                                                            subscription.wednesday = bulkUpdateQuantity.quantity;
                                                            break;
                                                        case AppConstants.WEEKDAY_THURSDAY:
                                                            subscription.thursday = bulkUpdateQuantity.quantity;
                                                            break;
                                                        case AppConstants.WEEKDAY_FRIDAY:
                                                            subscription.friday = bulkUpdateQuantity.quantity;
                                                            break;
                                                        case AppConstants.WEEKDAY_SATURDAY:
                                                            subscription.saturday = bulkUpdateQuantity.quantity;
                                                            break;
                                                    }
                                                } catch (ParseException e) {
                                                    onResultReceived(subscription, AppConstants.RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS, AppConstants.ACTION_CREATE_ORDER);
                                                }
                                            } else {
                                                subscription.quantity = bulkUpdateQuantity.quantity;
                                            }

                                            onResultReceived(subscription, AppConstants.RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS, AppConstants.ACTION_CREATE_ORDER);
                                            break;
                                        }
                                    }
                                } else {
                                    onResultReceived(subscription, AppConstants.RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS, AppConstants.ACTION_CREATE_ORDER);
                                }
                            } else {
                                toggleProgressBar(false);
                            }
                        });

            }
        } else if (AppConstants.RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS.equals(resultType) && AppConstants.ACTION_CREATE_ORDER.equals(action)) {
            if (result != null) {
                Subscription subscription = (Subscription) result;

                switch (subscription.subscription_type) {
                    case AppConstants.SUBSCRIPTION_TYPE_ONE_TIME:
                        if (selectedDate.equals(subscription.next_delivery_date)) {
                            createOrderFromSubscription(subscription);
                        } else {
                            toggleLayouts(true, false);
                            toggleProgressBar(false);
                        }
                        break;
                    case AppConstants.SUBSCRIPTION_TYPE_EVERYDAY:
                        createOrderFromSubscription(subscription);
                        break;
                    case AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL:
                        try {
                            DateUtils.DateDifference dateDifference = DateUtils.getDifference(DateUtils.getDateFromString(subscription.next_delivery_date, "yyyy-MM-dd", false),
                                    DateUtils.getDateFromString(selectedDate, "yyyy-MM-dd", false));

                            if (subscription.interval instanceof Long && dateDifference.getElapsedDays() % (Long) subscription.interval == 0) {
                                createOrderFromSubscription(subscription);
                            } else {
                                toggleLayouts(true, false);
                                toggleProgressBar(false);
                            }

                        } catch (ParseException e) {
                            toggleLayouts(true, false);
                        }
                        break;
                    case AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE:
                        try {
                            String weekday = DateUtils.getWeekDayFromDate(selectedDate, "yyyy-MM-dd", false);

                            switch (weekday) {
                                case AppConstants.WEEKDAY_SUNDAY:
                                    if (subscription.sunday > 0) {
                                        subscription.quantity = subscription.sunday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                                case AppConstants.WEEKDAY_MONDAY:
                                    if (subscription.monday > 0) {
                                        subscription.quantity = subscription.monday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                                case AppConstants.WEEKDAY_TUESDAY:
                                    if (subscription.tuesday > 0) {
                                        subscription.quantity = subscription.tuesday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                                case AppConstants.WEEKDAY_WEDNESDAY:
                                    if (subscription.wednesday > 0) {
                                        subscription.quantity = subscription.wednesday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                                case AppConstants.WEEKDAY_THURSDAY:
                                    if (subscription.thursday > 0) {
                                        subscription.quantity = subscription.thursday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                                case AppConstants.WEEKDAY_FRIDAY:
                                    if (subscription.friday > 0) {
                                        subscription.quantity = subscription.friday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                                case AppConstants.WEEKDAY_SATURDAY:
                                    if (subscription.saturday > 0) {
                                        subscription.quantity = subscription.saturday;
                                        createOrderFromSubscription(subscription);
                                    }
                                    break;
                            }

                        } catch (ParseException e) {
                            toggleLayouts(true, false);
                        }
                        break;
                }
            }
        }
    }

    private void createOrderFromSubscription(Subscription subscription) {
        toggleProgressBar(true);
        database.collection(AppConstants.SETTINGS_COLLECTION)
                .document(AppConstants.APP_OPS_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String deadline = task.getResult().getString("subscription_deadline");

                        if (deadline != null) {

                            orderUpdateTip.setVisibility(View.VISIBLE);
                            try {
                                orderUpdateTip.setText(String.format(getString(R.string.order_update_tip),
                                        DateUtils.getStringFromMillis(
                                                DateUtils.getTimeAfterAddingDays(
                                                        DateUtils.getDateFromString(selectedDate, "yyyy-MM-dd", false).getTime(),
                                                        -1), "dd-MMM-yyy ", true) + deadline));
                            } catch (ParseException e) {
                                orderUpdateTip.setVisibility(View.GONE);
                            }

                            Order order = new Order();
                            order.created_date = Timestamp.now();
                            order.customer_id = subscription.customer_id;
                            order.customer_name = subscription.customer_name;
                            order.customer_phone = subscription.customer_phone;
                            order.delivering_to = subscription.customer_address;
                            order.package_unit = subscription.package_unit;
                            order.price = subscription.price;
                            order.product_name = subscription.product_name;
                            if (subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
                                try {
                                    String weekday = DateUtils.getWeekDayFromDate(selectedDate, "yyyy-MM-dd", false);

                                    switch (weekday) {
                                        case AppConstants.WEEKDAY_SUNDAY:
                                            order.quantity = subscription.sunday;
                                            break;
                                        case AppConstants.WEEKDAY_MONDAY:
                                            order.quantity = subscription.monday;
                                            break;
                                        case AppConstants.WEEKDAY_TUESDAY:
                                            order.quantity = subscription.tuesday;
                                            break;
                                        case AppConstants.WEEKDAY_WEDNESDAY:
                                            order.quantity = subscription.wednesday;
                                            break;
                                        case AppConstants.WEEKDAY_THURSDAY:
                                            order.quantity = subscription.thursday;
                                            break;
                                        case AppConstants.WEEKDAY_FRIDAY:
                                            order.quantity = subscription.friday;
                                            break;
                                        case AppConstants.WEEKDAY_SATURDAY:
                                            order.quantity = subscription.saturday;
                                            break;
                                    }
                                } catch (ParseException ignored) {
                                    order.quantity = subscription.quantity;
                                }
                            } else {
                                order.quantity = subscription.quantity;
                            }
                            order.status = "0";
                            order.updated_date = subscription.updated_date;
                            order.isFutureOrder = true;
                            order.subscriptionType = subscription.subscription_type;
                            order.subscription_id = subscription.subscription_id;

                            try {
                                order.shouldShowMutableQty = Timestamp.now().compareTo(
                                        new Timestamp(new Date())) < 0 ||
                                        LocalTime.now(ZoneId.of("Asia/Kolkata")).isBefore(LocalTime.parse(deadline, DateTimeFormatter.ofPattern("hh:mm a")));

                            } catch (Exception e) {
                                order.shouldShowMutableQty = false;
                            }
                            orderList.add(order);
                            orderAdapter.add(order);

                            toggleLayouts(false, false);
                            orderAdapter.notifyAdapterItemChanged(orderAdapter.getAdapterPosition(order));
                        }
                    } else {
                        toggleLayouts(true, false);
                    }
                    toggleProgressBar(false);
                });
    }

    @Override
    public void onError(Throwable error) {
        if (view != null) {
            View loginNeeded = view.findViewById(R.id.login_needed);
            if (loginNeeded != null) {
                loginNeeded.setVisibility(View.VISIBLE);
            }

            Toast.makeText(requireContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            toggleProgressBar(false);
        }
    }

    private void checkVacation() {
        if (view != null && isAdded()) {
            if (((WhyteFarmsApplication) requireActivity().getApplication()).isLoggedIn()) {
                View vacationsMode = view.findViewById(R.id.vacation_mode_layout);
                FirebaseFirestore.getInstance()
                        .collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                        .where(Filter.and(Filter.equalTo("customer_id", ((WhyteFarmsApplication) requireActivity().getApplication()).getCustomerIDFromLoginState()),
                                Filter.lessThanOrEqualTo("start_date", Timestamp.now()),
                                Filter.greaterThanOrEqualTo("end_date", Timestamp.now())))
                        .get()
                        .addOnCompleteListener(task -> {
                            if (vacationsMode != null) {
                                vacationsMode.setVisibility(task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        });

            }
        }
    }
}