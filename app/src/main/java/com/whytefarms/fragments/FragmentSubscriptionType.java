package com.whytefarms.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.color.MaterialColors;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.whytefarms.R;
import com.whytefarms.activities.BaseActivity;
import com.whytefarms.activities.DescriptionActivity;
import com.whytefarms.fastModels.Day;
import com.whytefarms.fastModels.Item;
import com.whytefarms.fastModels.Subscription;
import com.whytefarms.firebaseModels.FirebaseSubscription;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.Cart;
import com.whytefarms.utils.DateUtils;

import com.whytefarms.firebaseModels.CustomerActivity; 

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FragmentSubscriptionType extends Fragment {

    private final FastItemAdapter<Item> intervalAdapter = new FastItemAdapter<>();
    private final FastItemAdapter<Day> dayAdapter = new FastItemAdapter<>();
    private final List<Item> intervalList = new ArrayList<>();
    private final List<Day> dayList = new ArrayList<>();
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private AppCompatImageButton intervalMinusButton, intervalPlusButton, qtyMinusButton, qtyPlusButton;
    private String intervalCountString = "2nd";
    private View view;
    private AppCompatTextView subscriptionTip, customIntervalTitle, customIntervalSubtitle;
    private AppCompatTextView quantity;
    private AppCompatTextView intervalCount;
    private AppCompatTextView customIntervalLayout;
    private View progressBar;
    private DatePickerDialog datePickerDialog;
    private Subscription subscription = null;
    private String deliveryDate;
    private boolean isEditing = false;

    private AppCompatTextView startDate;
    private AppCompatTextView endDate;
    private AppCompatImageView removeEndDateButton;
    private AppCompatTextView commonButton = null;

    private AppCompatTextView startDateHeading;
    private AppCompatTextView changeDateHint;

    public static FragmentSubscriptionType newInstance(String param1, String param2) {
        FragmentSubscriptionType fragment = new FragmentSubscriptionType();
        Bundle args = new Bundle();
        args.putString("subscriptionString", param1);
        args.putString("subscriptionType", param2);
        fragment.setArguments(args);
        return fragment;
    }


    private String generateSubscriptionID() {
        Date timestamp = new Date();
        String random4Digits = String.format(Locale.US, "%04d", (int) Math.floor(Math.random() * 10000));
        return "SUB" + String.format(Locale.US, "%04d", timestamp.getTime() % 10000) + random4Digits;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
            view = inflater.inflate(R.layout.fragment_subscribe_on_interval, container, false);
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_EVERYDAY)) {
            view = inflater.inflate(R.layout.fragment_subscribe_everyday, container, false);
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
            view = inflater.inflate(R.layout.fragment_subscribe_on_interval, container, false);
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
            view = inflater.inflate(R.layout.fragment_subscribe_customize, container, false);
        }

        if (getArguments() != null) {
            initializeFragment(getArguments().getString("subscriptionString"), getArguments().getString("subscriptionType"));
        }


        return view != null ? view : super.onCreateView(inflater, container, savedInstanceState);
    }


    private void initializeFragment(@Nullable String subscriptionString, String subscriptionType) {
        if (subscriptionString != null && !subscriptionString.isEmpty()) {
            try {
                JSONObject subscriptionObject = new JSONObject(subscriptionString);
                subscription = new Subscription();
                subscription.sunday = subscriptionObject.getLong("sunday");
                subscription.monday = subscriptionObject.getLong("monday");
                subscription.tuesday = subscriptionObject.getLong("tuesday");
                subscription.wednesday = subscriptionObject.getLong("wednesday");
                subscription.thursday = subscriptionObject.getLong("thursday");
                subscription.friday = subscriptionObject.getLong("friday");
                subscription.saturday = subscriptionObject.getLong("saturday");
                subscription.package_unit = subscriptionObject.getString("package_unit");
                subscription.price = subscriptionObject.getLong("price");
                subscription.product_name = subscriptionObject.getString("product_name");
                subscription.subscription_id = subscriptionObject.getString("subscription_id");
                subscription.customer_id = subscriptionObject.getString("customer_id");
                subscription.customer_phone = subscriptionObject.getString("customer_phone");
                subscription.customer_name = subscriptionObject.getString("customer_name");
                subscription.customer_address = subscriptionObject.getString("customer_address");
                subscription.quantity = subscriptionObject.getLong("quantity");
                subscription.start_date = new Timestamp(new Date(subscriptionObject.getLong("start_date")));
                subscription.end_date = new Timestamp(new Date(subscriptionObject.getLong("end_date")));
                subscription.resume_date = new Timestamp(new Date(subscriptionObject.getLong("resume_date")));
                subscription.hub_name = subscriptionObject.getString("hub_name");
                subscription.subscription_type = subscriptionType;
                subscription.status = subscriptionObject.getString("status");

                subscription.interval = subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL) ?
                        subscriptionObject.getLong("interval") : 2L;
                subscription.isCartItem = false;
                subscription.next_delivery_date = subscriptionObject.getString("next_delivery_date");

                isEditing = true;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (requireActivity() instanceof DescriptionActivity) {
                subscription = new Subscription();
                subscription.sunday = 0L;
                subscription.monday = 0L;
                subscription.tuesday = 0L;
                subscription.wednesday = 0L;
                subscription.thursday = 0L;
                subscription.friday = 0L;
                subscription.saturday = 0L;
                subscription.interval = 1L;
                subscription.package_unit = ((DescriptionActivity) requireActivity()).getSelectedPackagingUnit();
                subscription.price = ((DescriptionActivity) requireActivity()).getSelectedPackagingUnitPrice();
                subscription.product_name = ((DescriptionActivity) requireActivity()).getProductName();
                subscription.subscription_id = generateSubscriptionID();
                subscription.subscription_type = subscriptionType;
                subscription.quantity = subscriptionType.equals(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE) ? 0L : 1L;
                subscription.status = "1";
                subscription.isCartItem = true;
                isEditing = false;
            }
        }


        LinearLayout endDateLayout;
        if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
            if (view != null && isAdded()) {
                getSubscriptionDeadline();
                AppCompatTextView tipText = view.findViewById(R.id.tip_text);
                subscriptionTip = view.findViewById(R.id.subscription_tip);
                quantity = view.findViewById(R.id.quantity);
                qtyMinusButton = view.findViewById(R.id.qty_minus_button);
                qtyPlusButton = view.findViewById(R.id.qty_plus_button);

                AppCompatImageView arrow = view.findViewById(R.id.arrow);
                startDateHeading = view.findViewById(R.id.start_date_heading);
                changeDateHint = view.findViewById(R.id.change_date_hint);
                endDateLayout = view.findViewById(R.id.end_date_layout);

                LinearLayout customIntervalMainLayout = view.findViewById(R.id.custom_interval_main_layout);

                tipText.setVisibility(View.GONE);
                endDateLayout.setVisibility(View.GONE);
                arrow.setVisibility(View.GONE);
                customIntervalMainLayout.setVisibility(View.GONE);
                startDateHeading.setText(R.string.delivery_date);
                deliveryDate = DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "dd-MMM-yyyy", false);


                qtyMinusButton.setOnClickListener(view -> {
                    int ic = Integer.parseInt(String.valueOf(quantity.getText()));

                    if (ic == 1) {
                        return;
                    }

                    if (ic > 1) {
                        ic = ic - 1;


                        quantity.setText(String.format(Locale.getDefault(), "%d", ic));
                    }


                    toggleQtyButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                    setQuantity(ic);
                });

                qtyPlusButton.setOnClickListener(view -> {
                    int ic = Integer.parseInt(String.valueOf(quantity.getText()));

                    if (ic == 99999) {
                        return;
                    }

                    if (ic < 99999) {
                        ic = ic + 1;


                        quantity.setText(String.format(Locale.getDefault(), "%d", ic));
                    }

                    toggleQtyButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                    setQuantity(ic);
                });

            }
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_EVERYDAY)) {

            if (view != null && isAdded()) {
                getSubscriptionDeadline();

                subscriptionTip = view.findViewById(R.id.subscription_tip);
                quantity = view.findViewById(R.id.quantity);
                startDateHeading = view.findViewById(R.id.start_date_heading);
                changeDateHint = view.findViewById(R.id.change_date_hint);
                qtyMinusButton = view.findViewById(R.id.qty_minus_button);
                qtyPlusButton = view.findViewById(R.id.qty_plus_button);

                qtyMinusButton.setOnClickListener(view -> {
                    int ic = Integer.parseInt(String.valueOf(quantity.getText()));

                    if (ic == 1) {
                        return;
                    }

                    if (ic > 1) {
                        ic = ic - 1;


                        quantity.setText(String.format(Locale.getDefault(), "%d", ic));
                    }


                    toggleQtyButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                    setQuantity(ic);
                });

                qtyPlusButton.setOnClickListener(view -> {
                    int ic = Integer.parseInt(String.valueOf(quantity.getText()));

                    if (ic == 99999) {
                        return;
                    }

                    if (ic < 99999) {
                        ic = ic + 1;


                        quantity.setText(String.format(Locale.getDefault(), "%d", ic));
                    }

                    toggleQtyButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                    setQuantity(ic);
                });


            }
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {


            if (view != null && isAdded()) {
                getSubscriptionDeadline();

                subscriptionTip = view.findViewById(R.id.subscription_tip);
                quantity = view.findViewById(R.id.quantity);
                startDateHeading = view.findViewById(R.id.start_date_heading);
                changeDateHint = view.findViewById(R.id.change_date_hint);
                RecyclerView intervalRecycler = view.findViewById(R.id.interval_recycler);
                intervalMinusButton = view.findViewById(R.id.minus_button);
                intervalPlusButton = view.findViewById(R.id.plus_button);
                qtyMinusButton = view.findViewById(R.id.qty_minus_button);
                qtyPlusButton = view.findViewById(R.id.qty_plus_button);
                intervalCount = view.findViewById(R.id.interval_count);
                customIntervalLayout = view.findViewById(R.id.custom_interval_layout);
                customIntervalTitle = view.findViewById(R.id.custom_interval_title);
                customIntervalSubtitle = view.findViewById(R.id.custom_interval_subtitle);


                intervalRecycler.setAdapter(intervalAdapter);
                intervalAdapter.add(intervalList);

                DisplayMetrics dm = getResources().getDisplayMetrics();
                int spanCount = (int) ((dm.xdpi / 150) - (dm.densityDpi / 320) + 1);

                intervalRecycler.setLayoutManager(new GridLayoutManager(intervalRecycler.getContext(), Math.max(spanCount, 2), GridLayoutManager.VERTICAL, false));

                SimpleItemAnimator itemAnimator = (SimpleItemAnimator) intervalRecycler.getItemAnimator();

                if (itemAnimator != null) {
                    itemAnimator.setSupportsChangeAnimations(false);
                }

                intervalList.add(new Item(String.format(getString(R.string.every_nth_day), getResources().getStringArray(R.array.days_array)[0]), "Interval"));
                intervalList.add(new Item(String.format(getString(R.string.every_nth_day), getResources().getStringArray(R.array.days_array)[1]), "Interval"));
                intervalList.add(new Item(String.format(getString(R.string.every_nth_day), getResources().getStringArray(R.array.days_array)[2]), "Interval"));
                intervalList.add(new Item(String.format(getString(R.string.every_nth_day), getResources().getStringArray(R.array.days_array)[3]), "Interval"));
                intervalList.add(new Item(String.format(getString(R.string.every_nth_day), getResources().getStringArray(R.array.days_array)[4]), "Interval"));
                intervalList.add(new Item(String.format(getString(R.string.every_nth_day), getResources().getStringArray(R.array.days_array)[5]), "Interval"));


                intervalAdapter.add(intervalList);


                intervalMinusButton.setOnClickListener(view -> {

                    int ic = Integer.parseInt(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2));

                    if (ic == 8) {
                        return;
                    }

                    if (ic > 8) {
                        ic = ic - 1;


                        intervalCount.setText(String.format(Locale.getDefault(), getStringFormat(ic), ic));


                        FragmentSubscriptionType.this.intervalCountString = String.format(Locale.getDefault(), getStringFormat(ic), ic);
                    }


                    toggleIntervalButtons();
                    updateSubscriptionTip(subscription.subscription_type);

                    setSubscriptionInterval();
                });


                intervalPlusButton.setOnClickListener(view -> {

                    int ic = Integer.parseInt(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2));

                    if (ic == 365) {
                        return;
                    }

                    if (ic < 365) {
                        ic = ic + 1;


                        intervalCount.setText(String.format(Locale.getDefault(), getStringFormat(ic), ic));
                        FragmentSubscriptionType.this.intervalCountString = String.format(Locale.getDefault(), getStringFormat(ic), ic);
                    }
                    toggleIntervalButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));

                    setSubscriptionInterval();
                });


                qtyMinusButton.setOnClickListener(view -> {
                    int ic = Integer.parseInt(String.valueOf(quantity.getText()));

                    if (ic == 1) {
                        return;
                    }

                    if (ic > 1) {
                        ic = ic - 1;


                        quantity.setText(String.format(Locale.getDefault(), "%d", ic));
                    }


                    toggleQtyButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                    setQuantity(ic);
                });

                qtyPlusButton.setOnClickListener(view -> {
                    int ic = Integer.parseInt(String.valueOf(quantity.getText()));

                    if (ic == 99999) {
                        return;
                    }

                    if (ic < 99999) {
                        ic = ic + 1;


                        quantity.setText(String.format(Locale.getDefault(), "%d", ic));
                    }

                    toggleQtyButtons();
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                    setQuantity(ic);
                });

                intervalAdapter.addEventHook(new ClickEventHook<Item>() {
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

                        toggleCustomInterval(true);

                        intervalAdapter.getAdapterItems().forEach(itemModel -> {
                            itemModel.setSelected(false);
                            intervalAdapter.notifyAdapterItemChanged(intervalAdapter.getAdapterPosition(itemModel));
                        });

                        item.setSelected(true);
                        intervalAdapter.notifyAdapterItemChanged(intervalAdapter.getAdapterPosition(item));
                        FragmentSubscriptionType.this.intervalCountString = item.getItemTitle().split(" ")[1];
                        updateSubscriptionTip(requireArguments().getString("subscriptionType"));


                        setSubscriptionInterval();
                    }
                });


                customIntervalLayout.setOnClickListener(view -> {
                    intervalAdapter.getAdapterItems().forEach(item -> {
                        item.setSelected(false);
                        intervalAdapter.notifyAdapterItemChanged(intervalAdapter.getAdapterPosition(item));
                    });

                    intervalCountString = intervalCount.getText() != null ? intervalCount.getText().toString() : "8th";
                    toggleCustomInterval(false);
                    updateSubscriptionTip(requireArguments().getString("subscriptionType"));

                });

            }
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
            if (view != null && isAdded()) {
                getSubscriptionDeadline();

                RecyclerView dayRecycler = view.findViewById(R.id.day_recycler);
                startDateHeading = view.findViewById(R.id.start_date_heading);
                changeDateHint = view.findViewById(R.id.change_date_hint);
                dayRecycler.setAdapter(dayAdapter);
                dayAdapter.add(dayList);

                dayRecycler.setLayoutManager(new LinearLayoutManager(dayRecycler.getContext(), LinearLayoutManager.VERTICAL, false));

                SimpleItemAnimator itemAnimator = (SimpleItemAnimator) dayRecycler.getItemAnimator();

                if (itemAnimator != null) {
                    itemAnimator.setSupportsChangeAnimations(false);
                }

                dayList.add(new Day(getString(R.string.sunday), 0L));
                dayList.add(new Day(getString(R.string.monday), 0L));
                dayList.add(new Day(getString(R.string.tuesday), 0L));
                dayList.add(new Day(getString(R.string.wednesday), 0L));
                dayList.add(new Day(getString(R.string.thursday), 0L));
                dayList.add(new Day(getString(R.string.friday), 0L));
                dayList.add(new Day(getString(R.string.saturday), 0L));


                dayAdapter.add(dayList);

                dayAdapter.addEventHook(new ClickEventHook<Day>() {
                    @Nullable
                    @Override
                    public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                        if (viewHolder instanceof Day.DayViewHolder) {
                            return ((Day.DayViewHolder) viewHolder).minusButton;
                        }
                        return null;
                    }

                    @Override
                    public void onClick(@NonNull View view, int position, @NonNull FastAdapter<Day> fastAdapter, @NonNull Day item) {
                        Long quantity = item.getQuantity();

                        if (quantity > 0) {
                            quantity = quantity - 1;
                            item.setQuantity(quantity);

                            fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(item));
                        }

                        setCustomQuantity(position, quantity);
                    }
                });

                dayAdapter.addEventHook(new ClickEventHook<Day>() {
                    @Nullable
                    @Override
                    public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                        if (viewHolder instanceof Day.DayViewHolder) {
                            return ((Day.DayViewHolder) viewHolder).plusButton;
                        }
                        return null;
                    }

                    @Override
                    public void onClick(@NonNull View view, int position, @NonNull FastAdapter<Day> fastAdapter, @NonNull Day item) {
                        Long quantity = item.getQuantity();

                        if (quantity < 99999) {
                            quantity = quantity + 1;
                            item.setQuantity(quantity);

                            fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(item));
                        }
                        setCustomQuantity(position, quantity);
                    }
                });
            }
        }

        if (view != null) {
            progressBar = view.findViewById(R.id.progress_bar);
            LinearLayout startDateLayout = view.findViewById(R.id.start_date_layout);
            endDateLayout = view.findViewById(R.id.end_date_layout);

            startDate = view.findViewById(R.id.start_date);
            endDate = view.findViewById(R.id.end_date);
            removeEndDateButton = view.findViewById(R.id.remove_end_date);
            commonButton = view.findViewById(R.id.common_button);
            startDateHeading = view.findViewById(R.id.start_date_heading);
            changeDateHint = view.findViewById(R.id.change_date_hint);


            if (!isEditing) {
                startDate.setText(DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "EEE MMM dd, yyyy", true));
                deliveryDate = DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "MMM dd, yyyy", true);
                subscription.start_date = new Timestamp(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1)));
                subscription.next_delivery_date = DateUtils.getStringFromMillis(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1)).getTime(), "yyyy-MM-dd", false);
                subscription.resume_date = new Timestamp(new Date(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1)));
                subscription.end_date = new Timestamp(new Date(AppConstants.EPOCH_TO_END_DATE));
                endDate.setText(R.string.until_cancelled);
            }

            updateSubscriptionTip(requireArguments().getString("subscriptionType"));

            startDateLayout.setOnClickListener(view -> {
                if (isEditing) {
                    if (isAdded()) {
                        Toast.makeText(requireActivity(), R.string.cannot_change_start_date, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (datePickerDialog != null && datePickerDialog.isShowing()) {
                    return;
                }

                if (datePickerDialog == null) {
                    final Calendar c = Calendar.getInstance();

                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH) + 1;

                    datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, i, i1, i2) -> {
                        c.set(i, i1, i2);
                        String selectedDateString = DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", false);


                        try {
                            Date selectedStartDate = DateUtils.getDateFromString(selectedDateString, "EEE MMM dd, yyyy", false);
                            Date selectedEndDate = endDate.getText().equals(getString(R.string.until_cancelled)) ? new Date(AppConstants.EPOCH_TO_END_DATE) : DateUtils.getDateFromString(String.valueOf(endDate.getText()), "EEE MMM dd, yyyy", false);


                            if (selectedStartDate.after(selectedEndDate)) {
                                Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                            } else {
                                startDate.setText(DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", true));
                                subscription.start_date = new Timestamp(new Date(c.getTime().getTime()));
                                subscription.next_delivery_date = DateUtils.getStringFromMillis(c.getTime().getTime(), "yyyy-MM-dd", false);
                                subscription.resume_date = new Timestamp(new Date(c.getTime().getTime()));
                                subscription.end_date = new Timestamp(new Date(AppConstants.EPOCH_TO_END_DATE));
                                deliveryDate = DateUtils.getStringFromMillis(c.getTime().getTime(), "MMM dd, yyyy", true);

                                updateSubscriptionTip(requireArguments().getString("subscriptionType"));
                            }
                        } catch (ParseException e) {
                            Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                        }
                    }, year, month, day);

                    if (isEditing) {
                        datePickerDialog.getDatePicker().setMinDate(subscription.start_date.toDate().getTime());
                    } else {
                        datePickerDialog.getDatePicker().setMinDate(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1));
                    }
                    datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                    datePickerDialog.show();
                }
            });


            endDateLayout.setOnClickListener(view -> {
                if (datePickerDialog != null && datePickerDialog.isShowing()) {
                    return;
                }

                if (datePickerDialog == null) {
                    final Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH) + 1;

                    datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, y, m, d) -> {
                        c.set(y, m, d);
                        String selectedDateString = DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", false);


                        try {
                            Date selectedEndDate = DateUtils.getDateFromString(selectedDateString, "EEE MMM dd, yyyy", false);
                            Date selectedStartDate = DateUtils.getDateFromString(String.valueOf(startDate.getText()), "EEE MMM dd, yyyy", false);

                            if (selectedEndDate.before(selectedStartDate)) {
                                Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                            } else {
                                endDate.setText(selectedDateString);
                                removeEndDateButton.setVisibility(View.VISIBLE);
                                subscription.end_date = new Timestamp(new Date(c.getTime().getTime()));
                            }
                        } catch (ParseException e) {
                            Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                        }
                    }, year, month, day);


                    datePickerDialog.getDatePicker().setMinDate(DateUtils.getTimeAfterAddingDays(subscription.start_date.toDate().getTime(), 1));
                    datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                    datePickerDialog.show();

                    removeEndDateButton.setOnClickListener(view1 -> {
                        endDate.setText(R.string.until_cancelled);
                        removeEndDateButton.setVisibility(View.GONE);
                        subscription.end_date = new Timestamp(new Date(AppConstants.EPOCH_TO_END_DATE));
                    });
                }
            });
        }


        commonButton.setText(subscriptionType.equals(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME) ? R.string.add_to_cart : R.string.add_subscription_to_cart);

        if (commonButton != null) {
            commonButton.setOnClickListener(view -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (getArguments() != null && isAdded()) {
                    if (subscriptionType.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE) &&
                            subscription.sunday == 0 &&
                            subscription.monday == 0 &&
                            subscription.tuesday == 0 &&
                            subscription.wednesday == 0 &&
                            subscription.thursday == 0 &&
                            subscription.friday == 0 &&
                            subscription.saturday == 0) {
                        Toast.makeText(requireActivity(), R.string.quantity_error, Toast.LENGTH_SHORT).show();
                    } else {
                        getRequiredDetailsAndCreateCartItem();
                    }
                }
            });
        }

        if (isEditing) {
            fillInDetails();
            updateSubscriptionTip(subscriptionType);

            startDate.setTextColor(ContextCompat.getColor(startDate.getContext(), R.color.gray));
            if (startDateHeading != null) {
                startDateHeading.setTextColor(ContextCompat.getColor(startDateHeading.getContext(), R.color.gray));
            }

            if (changeDateHint != null) {
                changeDateHint.setText(R.string.cannot_change_start_date_short);
            }

            if (subscription.end_date instanceof Timestamp) {
                endDate.setText(((Timestamp) subscription.end_date).toDate().getTime() < AppConstants.EPOCH_TO_END_DATE ?
                        DateUtils.getStringFromMillis(((Timestamp) subscription.end_date).toDate().getTime(),
                                "EEE MMM dd, yyyy", true) :
                        getString(R.string.until_cancelled));

            }
        } else {
            startDate.setTextColor(MaterialColors.getColor(startDate.getContext(), android.R.attr.colorForeground,
                    ContextCompat.getColor(startDate.getContext(), R.color.dark_gray)));
            if (startDateHeading != null) {
                startDateHeading.setTextColor(ContextCompat.getColor(startDateHeading.getContext(), R.color.gold));
            }

            if (changeDateHint != null) {
                changeDateHint.setText(R.string.tap_to_change_date);
            }
        }
    }


    private void fillInDetails() {
        if (view != null && isAdded()) {
            if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
                startDate.setText(subscription.start_date != null ?
                        DateUtils.getStringFromMillis(subscription.start_date.toDate().getTime(), "EEE MMM dd, yyyy", true) :
                        DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "EEE MMM dd, yyyy", true));
                deliveryDate = DateUtils.getStringFromMillis(subscription.start_date.toDate().getTime(), "dd-MMM-yyyy", false);
                quantity.setText(String.format(Locale.getDefault(), "%d", subscription.quantity));
            } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_EVERYDAY)) {
                startDate.setText(subscription.start_date != null ?
                        DateUtils.getStringFromMillis(subscription.start_date.toDate().getTime(), "EEE MMM dd, yyyy", true) :
                        DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "EEE MMM dd, yyyy", true));
                endDate.setText(subscription.end_date != null && subscription.end_date instanceof Timestamp ?
                        ((Timestamp) subscription.end_date).toDate().getTime() != AppConstants.EPOCH_TO_END_DATE ? DateUtils.getStringFromMillis(((Timestamp) subscription.end_date).toDate().getTime(), "EEE MMM dd, yyyy", true) :
                                getString(R.string.until_cancelled) : getString(R.string.until_cancelled));
                quantity.setText(String.format(Locale.getDefault(), "%d", subscription.quantity));
            } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
                quantity.setText(String.format(Locale.getDefault(), "%d", subscription.quantity));
                startDate.setText(subscription.start_date != null ?
                        DateUtils.getStringFromMillis(subscription.start_date.toDate().getTime(), "EEE MMM dd, yyyy", true) :
                        DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "EEE MMM dd, yyyy", true));
                endDate.setText(subscription.end_date != null && subscription.end_date instanceof Timestamp ?
                        ((Timestamp) subscription.end_date).toDate().getTime() != AppConstants.EPOCH_TO_END_DATE ? DateUtils.getStringFromMillis(((Timestamp) subscription.end_date).toDate().getTime(), "EEE MMM dd, yyyy", true) :
                                getString(R.string.until_cancelled) : getString(R.string.until_cancelled));


                int interval = Integer.parseInt(subscription.interval.toString());

                if (interval % 10 == 1 && interval != 11) {
                    intervalCountString = interval + "st";
                } else if (interval % 10 == 2 && interval != 12) {
                    intervalCountString = interval + "nd";
                } else if (interval % 10 == 3 && interval != 13) {
                    intervalCountString = interval + "rd";
                } else {
                    intervalCountString = interval + "th";
                }

                if (interval > 7) {
                    toggleCustomInterval(false);
                    for (Item item : intervalAdapter.getAdapterItems()) {
                        item.setSelected(false);
                        intervalAdapter.notifyAdapterItemChanged(intervalAdapter.getAdapterPosition(item));
                    }

                    intervalCount.setText(String.format(Locale.getDefault(), getStringFormat(interval), interval));

                } else {
                    toggleCustomInterval(true);
                    for (Item item : intervalAdapter.getAdapterItems()) {
                        //  Log.d("TAG", "fillInDetails: " + item.getItemTitle() + "::" + (intervalAdapter.getAdapterPosition(item) == interval - 2));
                        item.setSelected(intervalAdapter.getAdapterPosition(item) == interval - 2);
                        intervalAdapter.notifyAdapterItemChanged(intervalAdapter.getAdapterPosition(item));
                    }
                }

            } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
                startDate.setText(subscription.start_date != null ?
                        DateUtils.getStringFromMillis(subscription.start_date.toDate().getTime(), "EEE MMM dd, yyyy", true) :
                        DateUtils.getStringFromMillis(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1), "EEE MMM dd, yyyy", true));
                endDate.setText(subscription.end_date != null && subscription.end_date instanceof Timestamp ?
                        DateUtils.getStringFromMillis(((Timestamp) subscription.end_date).toDate().getTime(), "EEE MMM dd, yyyy", true) :
                        getString(R.string.until_cancelled));

                dayAdapter.getAdapterItem(0).setQuantity(subscription.sunday);
                dayAdapter.getAdapterItem(1).setQuantity(subscription.monday);
                dayAdapter.getAdapterItem(2).setQuantity(subscription.tuesday);
                dayAdapter.getAdapterItem(3).setQuantity(subscription.wednesday);
                dayAdapter.getAdapterItem(4).setQuantity(subscription.thursday);
                dayAdapter.getAdapterItem(5).setQuantity(subscription.friday);
                dayAdapter.getAdapterItem(6).setQuantity(subscription.saturday);

                dayAdapter.notifyAdapterItemRangeChanged(0, 7);
            }

            commonButton.setText(getString(R.string.update_subscription));
            commonButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(commonButton.getContext(), R.drawable.ic_update), null, null, null);

            commonButton.setOnClickListener(view -> updateSubscriptionToDB());


            if (subscription.end_date != null &&
                    subscription.end_date instanceof Timestamp &&
                    ((Timestamp) subscription.end_date).toDate().getTime() != AppConstants.EPOCH_TO_END_DATE) {
                removeEndDateButton.setVisibility(View.VISIBLE);
            } else {
                removeEndDateButton.setVisibility(View.GONE);
            }
        }
    }

    private void updateSubscriptionToDB() {
        database.collection(AppConstants.SUBSCRIPTION_DATA_COLLECTION)
                .where(Filter.and(
                        Filter.equalTo("customer_id", subscription.customer_id),
                        Filter.equalTo("subscription_id", subscription.subscription_id)
                ))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            task.getResult().getDocuments().get(0).getReference()
                                    .set(FirebaseSubscription.getFirebaseSubscription(subscription))
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            if (isAdded()) {
                                                CustomerActivity customerActivity = new CustomerActivity();
                                                customerActivity.action = "Subscription Update";
                                                customerActivity.created_date = new Timestamp(new Date());
                                                customerActivity.customer_id = subscription.customer_id;
                                                customerActivity.customer_name = subscription.customer_name;
                                                customerActivity.customer_phone = subscription.customer_phone;
                                                customerActivity.description = "Subscription updated for " + subscription.subscription_id + " subscription type: " + subscription.subscription_type + " quantity: " + subscription.quantity + " status: " + subscription.status + " on " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + " by " + subscription.customer_name;
                                                customerActivity.object = "Subscription Updated";
                                                customerActivity.user = subscription.customer_name;
                                                customerActivity.platform = "Android";

                                                database.collection("customer_activities").add(customerActivity)
                                                    .addOnSuccessListener(activityDocumentReference -> {
                                                        Toast.makeText(requireActivity(), R.string.update_successful, Toast.LENGTH_SHORT).show();
                                                    });


                                                //Crash Mitigation NullPointerException while finding Fragment
                                                Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag("edit_subscription");
                                                if(fragment != null) {
                                                    requireActivity().getSupportFragmentManager().beginTransaction()
                                                            .remove(fragment)
                                                            .commitNow();
                                                }
                                            }
                                        } else {
                                            Toast.makeText(requireActivity(), getString(R.string.update_error) + "1:" + task1.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(requireActivity(), getString(R.string.update_error) + "2:" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireActivity(), getString(R.string.update_error) + "3:" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getRequiredDetailsAndCreateCartItem() {
        Cart cart = new Cart(requireActivity());
        cart.saveItemToCart(subscription);
    }


    private void updateSubscriptionTip(@Nullable String subscriptionType) {
        if (subscriptionType != null && subscriptionType.equals(AppConstants.SUBSCRIPTION_TYPE_EVERYDAY)) {
            if (((BaseActivity) requireActivity()).getCurrentLocale().equals("hi")) {
                subscriptionTip.setText(String.format(Locale.getDefault(), getString(R.string.subscription_delivery_tip_everyday), subscription.product_name, quantity.getText() != null ? quantity.getText() : 0));
            } else {
                subscriptionTip.setText(String.format(Locale.getDefault(), getString(R.string.subscription_delivery_tip_everyday), quantity.getText() != null ? quantity.getText() : 0, subscription.product_name));
            }
        } else if (subscriptionType != null && subscriptionType.equals(AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
            if (((BaseActivity) requireActivity()).getCurrentLocale().equals("hi")) {
                subscriptionTip.setText(String.format(Locale.getDefault(), getString(R.string.subscription_delivery_tip), intervalCountString, subscription.product_name, quantity.getText() != null ? quantity.getText() : 0));
            } else {
                subscriptionTip.setText(String.format(Locale.getDefault(), getString(R.string.subscription_delivery_tip), quantity.getText() != null ? quantity.getText() : 0, subscription.product_name, intervalCountString));
            }
        } else if (subscriptionType != null && subscriptionType.equals(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
            if (((BaseActivity) requireActivity()).getCurrentLocale().equals("hi")) {
                subscriptionTip.setText(String.format(Locale.getDefault(), getString(R.string.subscription_delivery_tip_one_time), this.deliveryDate, subscription.product_name, quantity.getText() != null ? quantity.getText() : 0));
            } else {
                subscriptionTip.setText(String.format(Locale.getDefault(), getString(R.string.subscription_delivery_tip_one_time), quantity.getText() != null ? quantity.getText() : 0, subscription.product_name, this.deliveryDate));
            }
        }
    }


    private void toggleCustomInterval(boolean shouldDisable) {
        if (shouldDisable) {
            customIntervalTitle.setTextColor(ContextCompat.getColorStateList(customIntervalTitle.getContext(), R.color.gray));
            customIntervalSubtitle.setTextColor(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gray));
            intervalCount.setTextColor(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gray));
            intervalCount.setBackgroundTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gray));


            intervalMinusButton.setBackgroundTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gray));
            intervalMinusButton.setImageTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.white));
            intervalPlusButton.setBackgroundTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gray));
            intervalPlusButton.setImageTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.white));

        } else {
            customIntervalTitle.setTextColor(MaterialColors.getColor(requireContext(), android.R.attr.textColorPrimary, Color.BLACK));
            customIntervalSubtitle.setTextColor(MaterialColors.getColor(requireContext(), android.R.attr.textColorPrimary, Color.BLACK));
            intervalCount.setTextColor(MaterialColors.getColor(requireContext(), android.R.attr.textColorPrimary, Color.BLACK));
            intervalCount.setBackgroundTintList(null);
            intervalCount.setBackgroundResource(R.drawable.ic_rounded_rectangle_unselected);

            intervalMinusButton.setBackgroundTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.black));
            intervalMinusButton.setImageTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gold));
            intervalPlusButton.setBackgroundTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.black));
            intervalPlusButton.setImageTintList(ContextCompat.getColorStateList(customIntervalSubtitle.getContext(), R.color.gold));
        }

        intervalCount.setEnabled(!shouldDisable);
        intervalMinusButton.setEnabled(!shouldDisable);
        intervalPlusButton.setEnabled(!shouldDisable);
        customIntervalLayout.setVisibility(shouldDisable ? View.VISIBLE : View.GONE);

        updateSubscriptionTip(subscription.subscription_type);
        setSubscriptionInterval();
    }

    public void setSubscriptionInterval() {
        int interval = 0;
        for (Item item : intervalAdapter.getAdapterItems()) {
            if (item.isSelected()) {
                interval = Integer.parseInt(String.valueOf(item.getItemTitle().split(" ")[1].toCharArray()[0]));
                break;
            }
        }

        if (subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
            if (interval == 0) {
                interval = Integer.parseInt(intervalCount.getText().toString().substring(0, intervalCount.getText().toString().length() - 2));
            }
        } else if (subscription.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_EVERYDAY)) {
            if (interval == 0) {
                interval = 1;
            }
        }

        subscription.interval = Long.parseLong(String.valueOf(interval));
    }

    private void setQuantity(int ic) {
        subscription.quantity = Long.parseLong(String.valueOf(ic));
    }


    private void setCustomQuantity(int position, Long quantity) {
        switch (position) {
            case 0:
                subscription.sunday = quantity;
                break;
            case 1:
                subscription.monday = quantity;
                break;
            case 2:
                subscription.tuesday = quantity;
                break;
            case 3:
                subscription.wednesday = quantity;
                break;
            case 4:
                subscription.thursday = quantity;
                break;
            case 5:
                subscription.friday = quantity;
                break;
            case 6:
                subscription.saturday = quantity;
                break;

        }
    }

    private void toggleIntervalButtons() {

        intervalMinusButton.setEnabled(intervalCount.getText() != null && Long.parseLong(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2)) > 8);

        intervalMinusButton.setBackgroundTintList(intervalCount.getText() != null && Long.parseLong(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2)) > 8 ? ContextCompat.getColorStateList(intervalMinusButton.getContext(), R.color.black) : ContextCompat.getColorStateList(intervalMinusButton.getContext(), R.color.gray));
        intervalMinusButton.setImageTintList(intervalCount.getText() != null && Long.parseLong(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2)) > 8 ? ContextCompat.getColorStateList(intervalMinusButton.getContext(), R.color.gold) : ContextCompat.getColorStateList(intervalMinusButton.getContext(), R.color.white));


        intervalPlusButton.setEnabled(intervalCount.getText() != null && Long.parseLong(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2)) < 365);

        intervalPlusButton.setBackgroundTintList(intervalCount.getText() != null && Long.parseLong(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2)) < 365 ? ContextCompat.getColorStateList(intervalPlusButton.getContext(), R.color.black) : ContextCompat.getColorStateList(intervalPlusButton.getContext(), R.color.gray));
        intervalPlusButton.setImageTintList(intervalCount.getText() != null && Long.parseLong(String.valueOf(intervalCount.getText()).substring(0, String.valueOf(intervalCount.getText()).length() - 2)) < 365 ? ContextCompat.getColorStateList(intervalPlusButton.getContext(), R.color.gold) : ContextCompat.getColorStateList(intervalPlusButton.getContext(), R.color.white));

    }

    private void toggleQtyButtons() {
        qtyMinusButton.setEnabled(Long.parseLong(String.valueOf(quantity.getText())) > 1);
        qtyMinusButton.setBackgroundTintList(Long.parseLong(String.valueOf(quantity.getText())) > 1 ? ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.black) : ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.gray));
        qtyMinusButton.setImageTintList(Long.parseLong(String.valueOf(quantity.getText())) > 1 ? ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.gold) : ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.white));


        qtyPlusButton.setEnabled(Long.parseLong(String.valueOf(quantity.getText())) < 9999);
        qtyPlusButton.setBackgroundTintList(Long.parseLong(String.valueOf(quantity.getText())) < 9999 ? ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.black) : ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.gray));
        qtyPlusButton.setImageTintList(Long.parseLong(String.valueOf(quantity.getText())) < 9999 ? ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.gold) : ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.white));
    }

    private String getStringFormat(int ic) {
        String format = "%dth";

        if (ic > 20) {
            if (ic % 10 == 1) {
                format = "%dst";
            } else if (ic % 10 == 2) {
                format = "%dnd";
            } else if (ic % 10 == 3) {
                format = "%drd";
            }
        }

        return format;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_EVERYDAY)) {
            toggleQtyButtons();
            updateSubscriptionTip(requireArguments().getString("subscriptionType"));
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
            if (!isEditing) {
                intervalAdapter.getAdapterItem(0).setSelected(true);
                intervalAdapter.notifyAdapterDataSetChanged();
            }
            toggleCustomInterval(true);
            toggleIntervalButtons();
            toggleQtyButtons();
            updateSubscriptionTip(requireArguments().getString("subscriptionType"));
        } else if (getArguments() != null && Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
            dayAdapter.notifyAdapterDataSetChanged();
        }
    }


    private void getSubscriptionDeadline() {
        database.collection(AppConstants.SETTINGS_COLLECTION)
                .document(AppConstants.APP_OPS_DOCUMENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String deadline = task.getResult().getString("subscription_deadline");

                        if (deadline != null && view != null && isAdded()) {
                            AppCompatTextView subscriptionDeadlineHint = view.findViewById(R.id.date_selection_hint);

                            if (getArguments() != null) {
                                subscriptionDeadlineHint.setText(String.format(Locale.getDefault(), getString(R.string.subscription_deadline_hint), deadline));
                            }
                        }
                    }
                });
    }
}


