package com.whytefarms.fastModels;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static java.lang.Double.NaN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;
import com.whytefarms.fragments.FragmentHome;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.DateUtils;

import java.util.List;
import java.util.Locale;

@Keep
public class Subscription extends AbstractItem<Subscription.SubscriptionViewHolder> {
    public Object mContext;
    public String coupon_code = "";
    public Timestamp created_date;
    public String customer_address;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public String delivered_by;
    public String delivering_to;
    public Object end_date; //Mitigating for possibility of end_date being either Timestamp or String
    public Long sunday;
    public Long monday;
    public Long tuesday;
    public Long wednesday;
    public Long thursday;
    public Long friday;
    public Long saturday;
    public String hub_name;
    public Object interval;
    public String next_delivery_date = "";
    public String package_unit;
    public Long price;
    public String product_name;
    public Long quantity;
    public Timestamp resume_date;
    public Timestamp start_date;
    public String status = "0";
    public String subscription_id;
    public final CompoundButton.OnCheckedChangeListener listener = (compoundButton, b) -> {
        if (this.mContext instanceof FragmentHome) {
            ((FragmentHome) this.mContext).showConfirmDialog(this);
        }
    };
    public String subscription_type;
    public Timestamp updated_date = Timestamp.now();
    public boolean isCartItem = false;
    public String reason = "";

    public Subscription() {

    }

    @NonNull
    @Override
    public String toString() {
        return ("{" + "\"sunday\":" + sunday + ", " +
                "\"monday\":" + monday + ", " +
                "\"tuesday\":" + tuesday + ", " +
                "\"wednesday\":" + wednesday + ", " +
                "\"thursday\":" + thursday + ", " +
                "\"friday\":" + friday + ", " +
                "\"saturday\":" + saturday + ", " +
                "\"interval\":" + (interval.equals(NaN) ? 0 : interval) + ", " +
                "\"package_unit\":\"" + package_unit + "\", " +
                "\"price\":" + price + ", " +
                "\"product_name\":\"" + product_name + "\", " +
                "\"quantity\":" + quantity + ", " +
                "\"start_date\":" + start_date.toDate().getTime() + ", " +
                "\"resume_date\":" + resume_date.toDate().getTime() + ", " +
                "\"end_date\":" + (end_date instanceof Timestamp ? ((Timestamp) end_date).toDate().getTime() : AppConstants.EPOCH_TO_END_DATE) + ", " +
                "\"subscription_id\":\"" + subscription_id + "\", " +
                "\"subscription_type\":\"" + subscription_type + "\"," +
                "\"customer_id\":\"" + customer_id + "\"," +
                "\"customer_name\":\"" + customer_name + "\"," +
                "\"customer_phone\":\"" + customer_phone + "\"," +
                "\"customer_address\":\"" + customer_address + "\"," +
                "\"delivered_by\":\"" + delivered_by + "\"," +
                "\"delivering_to\":\"" + delivering_to + "\"," +
                "\"next_delivery_date\":\"" + next_delivery_date + "\"," +
                "\"status\":\"" + status + "\"," +
                "\"hub_name\":\"" + hub_name + "\"," +
                "\"reason\":\"" + reason + "\"" +
                "}");
    }


    @Override
    public int getType() {
        return R.id.main_layout;
    }

    @Override
    public int getLayoutRes() {
        return this.isCartItem ? R.layout.item_cart : R.layout.item_subscription;
    }


    @NonNull
    @Override
    public Subscription.SubscriptionViewHolder getViewHolder(@NonNull View view) {
        return new SubscriptionViewHolder(view);
    }

    public static class SubscriptionViewHolder extends FastAdapter.ViewHolder<Subscription> {

        public final View mainSubscriptionLayout;
        public final AppCompatTextView subscriptionStatus;
        public final AppCompatImageView productImage;
        public final AppCompatTextView productName;
        public final AppCompatTextView productVariant;
        public final AppCompatTextView subscriptionStartDate;
        public final AppCompatTextView subscriptionEndDate;
        public final AppCompatTextView productPrice;
        public final AppCompatTextView editSubscription;
        public final AppCompatTextView deleteSubscription;
        public final AppCompatTextView deleteCartItem;
        public final SwitchCompat subscriptionStatusToggle;
        public final AppCompatTextView productQuantity;
        public final AppCompatTextView subscriptionType;
        public final AppCompatTextView otherDetails;
        private final FirebaseFirestore database;


        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);

            database = FirebaseFirestore.getInstance();

            mainSubscriptionLayout = itemView.findViewById(R.id.main_layout);
            subscriptionStatus = itemView.findViewById(R.id.subscription_status);
            productImage = itemView.findViewById(R.id.product_image);
            productPrice = itemView.findViewById(R.id.product_price);
            productName = itemView.findViewById(R.id.product_name);
            productVariant = itemView.findViewById(R.id.product_variant);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            subscriptionStartDate = itemView.findViewById(R.id.subscription_start_date);
            subscriptionEndDate = itemView.findViewById(R.id.subscription_end_date);
            editSubscription = itemView.findViewById(R.id.edit_subscription);
            deleteSubscription = itemView.findViewById(R.id.delete_subscription);
            subscriptionStatusToggle = itemView.findViewById(R.id.subscription_status_toggle);
            subscriptionType = itemView.findViewById(R.id.subscription_type);
            otherDetails = itemView.findViewById(R.id.other_details);
            deleteCartItem = itemView.findViewById(R.id.delete_cart_item);
        }

        private static String getString(long interval) {
            String intrvl;
            if (interval % 10 == 1 && interval != 11) {
                intrvl = interval + "st";
            } else if (interval % 10 == 2 && interval != 12) {
                intrvl = interval + "nd";
            } else if (interval % 10 == 3 && interval != 13) {
                intrvl = interval + "rd";
            } else {
                intrvl = interval + "th";
            }
            return intrvl;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void bindView(@NonNull Subscription item, @NonNull List<?> list) {
            // Fix for Glide crash - check if context is still valid
            if (item.mContext instanceof Activity) {
                Activity activity = (Activity) item.mContext;
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
            }

            subscriptionType.setText(item.subscription_type.toUpperCase());

            database.collection(AppConstants.PRODUCTS_DATA_COLLECTION).where(Filter.equalTo("productName", item.product_name)).limit(1).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        if (documentSnapshot.exists()) {
                            Product product = documentSnapshot.toObject(Product.class);

                            if (product != null) {
                                // Check if context is still valid before loading image
                                if (item.mContext instanceof Activity) {
                                    Activity activity = (Activity) item.mContext;
                                    if (activity.isFinishing() || activity.isDestroyed()) {
                                        return;
                                    }
                                }
                                
                                try {
                                    // Use the product's image URL directly
                                    Glide.with(productImage.getContext())
                                        .load(product.image_transparent_bg)
                                        .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                                        .skipMemoryCache(true)
                                        .centerCrop()
                                        .dontAnimate()
                                        .dontTransform()
                                        .priority(Priority.IMMEDIATE)
                                        .encodeFormat(Bitmap.CompressFormat.PNG)
                                        .format(DecodeFormat.DEFAULT)
                                        .error(R.drawable.color_bg)
                                        .into(productImage);
                                } catch (Exception e) {
                                    Log.e("Subscription", "Error loading product image", e);
                                }
                            }
                        }
                    }
                }
            });

            productName.setText(item.product_name);
            productPrice.setText(productPrice.getContext().getResources().getString(R.string.price_placeholder, item.price));

            productQuantity.setText(productQuantity.getContext().getResources().getString(R.string.quantity_placeholder, item.quantity));

            productVariant.setText(item.package_unit);
            subscriptionStartDate.setText(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_start_date), DateUtils.getStringFromMillis(item.start_date.toDate().getTime(), "EEE MMM dd, yyyy", true)));

            if (item.end_date instanceof Timestamp) {
                subscriptionEndDate.setText(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_end_date), item.end_date != null &&
                        ((Timestamp) item.end_date).toDate().getTime() < AppConstants.EPOCH_TO_END_DATE ?
                        DateUtils.getStringFromMillis(((Timestamp) item.end_date).toDate().getTime(),
                                "EEE MMM dd, yyyy", true) : subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled)));

            } else if (item.end_date instanceof String) {
                subscriptionEndDate.setText(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_end_date), item.end_date != null &&
                        !((String) item.end_date).isEmpty() && !item.end_date.equals("1970-01-01") ?
                        item.end_date : subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled)));
            }


            if (!item.isCartItem) {
                if (item.status.equals("1")) {
                    if (item.end_date instanceof Timestamp) {
                        subscriptionEndDate.setText(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_end_date), item.end_date != null &&
                                ((Timestamp) item.end_date).toDate().getTime() < AppConstants.EPOCH_TO_END_DATE ?
                                DateUtils.getStringFromMillis(((Timestamp) item.end_date).toDate().getTime(),
                                        "EEE MMM dd, yyyy", true) : subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled)));

                    } else if (item.end_date instanceof String) {
                        subscriptionEndDate.setText(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_end_date), item.end_date != null &&
                                !((String) item.end_date).isEmpty() && !item.end_date.equals("1970-01-01") ?
                                item.end_date : subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled)));
                    }

                } else if (item.status.equals("0")) {
                    if (item.resume_date != null) {
                        subscriptionEndDate.setVisibility(View.VISIBLE);
                        subscriptionEndDate.setText(item.resume_date.toDate().getTime() > item.start_date.toDate().getTime() ?
                                String.format(subscriptionStartDate.getContext().getString(R.string.subscription_resume_date),
                                        DateUtils.getStringFromMillis(item.resume_date.toDate().getTime(),
                                                "EEE MMM dd, yyyy", true)) :
                                String.format(subscriptionStartDate.getContext().getString(R.string.subscription_end_date), item.end_date != null ?
                                        item.end_date instanceof Timestamp ? DateUtils.getStringFromMillis(((Timestamp) item.end_date).toDate().getTime(), "EEE MMM dd, yyyy", true)
                                                : subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled) : subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled)));

                    }
                }

                subscriptionStatus.setText(item.status.equals("1") ? subscriptionStatus.getContext().getString(R.string.active_vertical) : subscriptionStatus.getContext().getString(R.string.paused_vertical));
                mainSubscriptionLayout.setBackgroundColor(item.status.equals("1") ? ContextCompat.getColor(mainSubscriptionLayout.getContext(), R.color.active_subscription_bg) : ContextCompat.getColor(mainSubscriptionLayout.getContext(), R.color.inactive_subscription_bg));

                subscriptionStatus.setBackgroundDrawable(item.status.equals("1") ? ContextCompat.getDrawable(subscriptionStatus.getContext(), R.drawable.active_sub_gradient_bg) : ContextCompat.getDrawable(subscriptionStatus.getContext(), R.drawable.paused_sub_gradient_bg));

                subscriptionStatusToggle.setThumbTintList(item.status.equals("1") ? ContextCompat.getColorStateList(subscriptionStatusToggle.getContext(), R.color.active_thumb) : ContextCompat.getColorStateList(subscriptionStatusToggle.getContext(), R.color.inactive_thumb));

                subscriptionStatusToggle.setTrackTintList(item.status.equals("1") ? ContextCompat.getColorStateList(subscriptionStatusToggle.getContext(), R.color.active_track) : ContextCompat.getColorStateList(subscriptionStatusToggle.getContext(), R.color.inactive_track));

                subscriptionStatusToggle.setOnCheckedChangeListener(null);
                subscriptionStatusToggle.setChecked(item.status.equals("1"));
                subscriptionStatusToggle.setOnCheckedChangeListener(item.listener);

                deleteSubscription.setVisibility(View.GONE);
            }
            if (item.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
                otherDetails.setText(String.format("%s%s%s%s%s%s%s", (item.sunday > 0 ? "SU: " + item.sunday + " " : ""),
                        (item.monday > 0 ? " M: " + item.monday + " " : ""),
                        (item.tuesday > 0 ? " TU: " + item.tuesday + " " : ""),
                        (item.wednesday > 0 ? " W: " + item.wednesday + " " : ""),
                        (item.thursday > 0 ? " TH: " + item.thursday + " " : ""),
                        (item.friday > 0 ? " F: " + item.friday + " " : ""),
                        (item.saturday > 0 ? " SA: " + item.saturday + " " : "")));
                productQuantity.setVisibility(View.GONE);
            } else if (item.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
                otherDetails.setVisibility(View.VISIBLE);
                otherDetails.setText(String.format(Locale.getDefault(), otherDetails.getContext().getString(R.string.every_nth_day),
                        getString(item.interval.equals(NaN) ? 2 : (Long) item.interval)));
            } else {
                otherDetails.setVisibility(View.GONE);
            }

           /* if(item.status.equals("1")) {
                if (item.end_date instanceof Timestamp) {
                    subscriptionEndDate.setText(((Timestamp) item.end_date).toDate().getTime() < AppConstants.EPOCH_TO_END_DATE ?
                            String.format(subscriptionEndDate.getContext().getString(R.string.subscription_end_date),
                                    DateUtils.getStringFromMillis(((Timestamp) item.end_date).toDate().getTime(),
                                            "EEE MMM dd, yyyy", true)) :
                            String.format(subscriptionEndDate.getContext().getString(R.string.subscription_end_date),
                                    subscriptionEndDate.getContext().getString(R.string.until_cancelled)));

                }

            }*/

            if (item.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
                if (!item.isCartItem) {
                    editSubscription.setVisibility(View.GONE);
                    deleteSubscription.setVisibility(View.VISIBLE);
                    subscriptionStatusToggle.setVisibility(View.GONE);
                }

                subscriptionEndDate.setVisibility(View.GONE);
                subscriptionStartDate.setText(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_delivery_date),
                        DateUtils.getStringFromMillis(item.start_date.toDate().getTime(),
                                "EEE MMM dd, yyyy", true)));
            } else {
                if (!item.isCartItem) {
                    editSubscription.setVisibility(View.VISIBLE);
                    deleteSubscription.setVisibility(View.GONE);
                    subscriptionStatusToggle.setVisibility(View.VISIBLE);
                }
            }

            if (subscriptionEndDate.getText().equals(String.format(subscriptionStartDate.getContext().getString(R.string.subscription_end_date), subscriptionEndDate.getContext().getResources().getString(R.string.until_cancelled))) ||
                    (item.end_date instanceof Timestamp && ((Timestamp) item.end_date).toDate().getTime() >= AppConstants.EPOCH_TO_END_DATE)) {
                subscriptionEndDate.setVisibility(View.GONE);
            } else {
                subscriptionEndDate.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void unbindView(@NonNull Subscription item) {
            // Cancel any pending Glide requests
            if (productImage != null) {
                Glide.with(itemView.getContext()).clear(productImage);
            }
        }
    }
}
