package com.whytefarms.fastModels;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.DateUtils;

import java.util.List;
import java.util.Locale;

@Keep
public class Order extends AbstractItem<Order.OrderViewHolder> {


    public String cancelled_reason;
    public String cancelled_time;
    public Timestamp created_date;
    public String customer_id;

    public String customer_name;
    public String customer_phone;
    public String delivering_to;
    public String delivery_date;
    public String delivery_exe_id;
    public String delivery_time;
    public Timestamp delivery_timestamp;
    //public long handling_charges;
    public String hub_name;
    public String latitude;
    public String location;
    public String longitude;
    public String marked_delivered_by;
    public String order_id;
    public String order_type;
    public String package_unit;
    public Long price;
    public String product_name;
    public Long quantity;
    public String status;
    public String subscription_id;
    public Long tax;
    public Long total_amount;
    public Timestamp updated_date;
    public Boolean shouldShowMutableQty = false;
    public Boolean isFutureOrder = false;
    public String subscriptionType = "Everyday";
    public Boolean isOrderUpdated = false;

    public Order() {

    }

    public Long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }


    @Override
    public int getType() {
        return R.id.main_order_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_order;
    }

    @NonNull
    @Override
    public OrderViewHolder getViewHolder(@NonNull View view) {
        return new OrderViewHolder(view);
    }

    public static class OrderViewHolder extends FastAdapter.ViewHolder<Order> {

        private final FirebaseFirestore database;
        public final ConstraintLayout mainOrderLayout;
        public final AppCompatImageView productImage;
        public final AppCompatTextView orderTimestamp;
        public final AppCompatTextView productName;
        public final AppCompatTextView productVariant;
        public final AppCompatTextView immutableQty;
        public final AppCompatTextView editableQuantity;
        public final ConstraintLayout mutableQty;
        public final AppCompatTextView productPrice;
        public final AppCompatTextView cancellationReason;
        public final AppCompatTextView status;
        public final AppCompatTextView subscriptionType;
        public final LinearLayout updateOrderButton;
        public final AppCompatImageButton qtyMinusButton;
        public final AppCompatImageButton qtyPlusButton;


        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            database = FirebaseFirestore.getInstance();

            mainOrderLayout = itemView.findViewById(R.id.main_order_layout);
            productImage = itemView.findViewById(R.id.product_image);
            orderTimestamp = itemView.findViewById(R.id.order_timestamp);
            productName = itemView.findViewById(R.id.product_name);
            productVariant = itemView.findViewById(R.id.product_variant);
            immutableQty = itemView.findViewById(R.id.immutable_qty);
            mutableQty = itemView.findViewById(R.id.mutable_qty);
            editableQuantity = itemView.findViewById(R.id.editable_quantity);
            productPrice = itemView.findViewById(R.id.product_price);
            cancellationReason = itemView.findViewById(R.id.cancellation_reason);
            status = itemView.findViewById(R.id.status);
            subscriptionType = itemView.findViewById(R.id.subscription_type);
            updateOrderButton = itemView.findViewById(R.id.update_order);
            qtyMinusButton = itemView.findViewById(R.id.minus_button);
            qtyPlusButton = itemView.findViewById(R.id.plus_button);

        }

        @Override
        public void bindView(@NonNull Order item, @NonNull List<?> list) {

            if (item.status.equals("1")) {
                status.setText(status.getContext().getString(R.string.delivered));
            } else if (item.status.equals("2")) {
                status.setText(status.getContext().getString(R.string.cancelled));
            }

            if (item.status.equals("1")) {
                status.setBackgroundDrawable(ContextCompat.getDrawable(status.getContext(), R.drawable.active_sub_gradient_bg));
            } else if (item.status.equals("2")) {
                status.setBackgroundDrawable(ContextCompat.getDrawable(status.getContext(), R.drawable.paused_sub_gradient_bg));
            } else {
                status.setVisibility(View.GONE);
            }


            database.collection(AppConstants.PRODUCTS_DATA_COLLECTION).where(Filter.equalTo("productName", item.product_name)).limit(1).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        if (documentSnapshot.exists()) {
                            Product product = documentSnapshot.toObject(Product.class);

                            if (product != null) {
                                Glide.with(productImage.getContext())
                                        .load(product.image_transparent_bg)
                                        .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                                        .skipMemoryCache(false)
                                        .centerCrop()
                                        .dontAnimate()
                                        .dontTransform()
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .priority(Priority.IMMEDIATE)
                                        .encodeFormat(Bitmap.CompressFormat.PNG)
                                        .format(DecodeFormat.DEFAULT)
                                        .into(productImage);
                            }

                        }
                    }
                }
            });

            productName.setText(item.product_name);
            productPrice.setText(productPrice.getContext().getResources().getString(R.string.price_placeholder, item.price));

            immutableQty.setText(String.format(Locale.getDefault(), "%d", item.quantity));
            editableQuantity.setText(String.format(Locale.getDefault(), "%d", item.quantity));

            productVariant.setText(item.package_unit);


            if (item.status.equals("2")) {
                if (!item.cancelled_reason.isEmpty()) {
                    cancellationReason.setVisibility(View.VISIBLE);
                    cancellationReason.setText(String.format(ContextCompat.getString(cancellationReason.getContext(), R.string.cancellation_reason_placeholder), item.cancelled_reason));
                } else {
                    cancellationReason.setVisibility(View.GONE);
                }
                if (item.updated_date != null) {
                    orderTimestamp.setText(String.format("Cancelled On: %s", DateUtils.getStringFromMillis(item.updated_date.toDate().getTime(), "dd-MM-yyyy hh:mm a", true)));
                } else {
                    orderTimestamp.setVisibility(View.GONE);
                }
            } else {
                cancellationReason.setVisibility(View.GONE);

                if (item.status.equals("0")) {
                    orderTimestamp.setText(String.format("Created On: %s", DateUtils.getStringFromMillis(item.created_date.toDate().getTime(), "dd-MM-yyyy hh:mm a", true)));
                } else if (item.status.equals("1")) {
                    orderTimestamp.setText(String.format("Delivered On: %s", DateUtils.getStringFromMillis(item.delivery_timestamp.toDate().getTime(), "dd-MM-yyyy hh:mm a", true)));
                }
            }

            immutableQty.setVisibility(!item.shouldShowMutableQty ? View.VISIBLE : View.GONE);
            mutableQty.setVisibility(item.shouldShowMutableQty ? View.VISIBLE : View.GONE);

            orderTimestamp.setVisibility(item.isFutureOrder ? View.GONE : View.VISIBLE);

            subscriptionType.setVisibility(item.isFutureOrder ? View.VISIBLE : View.GONE);
            subscriptionType.setText(String.format(subscriptionType.getContext().getString(R.string.subscription_type_placeholder), item.subscriptionType));
            updateOrderButton.setVisibility(item.isOrderUpdated ? View.VISIBLE : View.GONE);

            toggleQtyButtons(editableQuantity);
        }


        private void toggleQtyButtons(AppCompatTextView quantity) {
            qtyMinusButton.setEnabled(Long.parseLong(String.valueOf(quantity.getText())) > 0);
            qtyMinusButton.setBackgroundTintList(Long.parseLong(String.valueOf(quantity.getText())) > 0 ?
                    ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.black) :
                    ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.gray));
            qtyMinusButton.setImageTintList(Long.parseLong(String.valueOf(quantity.getText())) > 0 ?
                    ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.gold) :
                    ContextCompat.getColorStateList(qtyMinusButton.getContext(), R.color.white));

            qtyPlusButton.setEnabled(Long.parseLong(String.valueOf(quantity.getText())) < 9999);
            qtyPlusButton.setBackgroundTintList(Long.parseLong(String.valueOf(quantity.getText())) < 9999 ?
                    ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.black) :
                    ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.gray));
            qtyPlusButton.setImageTintList(Long.parseLong(String.valueOf(quantity.getText())) < 9999 ?
                    ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.gold) :
                    ContextCompat.getColorStateList(qtyPlusButton.getContext(), R.color.white));
        }

        @Override
        public void unbindView(@NonNull Order item) {

        }
    }
}
