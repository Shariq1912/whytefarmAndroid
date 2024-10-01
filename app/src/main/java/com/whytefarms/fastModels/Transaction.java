package com.whytefarms.fastModels;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.firebase.Timestamp;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;
import com.whytefarms.utils.DateUtils;

import java.text.DecimalFormat;
import java.util.List;


@Keep
public class Transaction extends AbstractItem<Transaction.TransactionViewHolder> {

    public Object amount;
    public Timestamp created_date;
    public String customer_id;
    public String customer_name;
    public String customer_phone;
    public String description;
    public String hub_name;
    public String reason;
    public String source;
    public String status;
    public String type;


    public Transaction() {

    }

    @Override
    public int getType() {
        return R.id.txn_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_transaction;
    }

    @NonNull
    @Override
    public TransactionViewHolder getViewHolder(@NonNull View view) {
        return new TransactionViewHolder(view);
    }

    public static class TransactionViewHolder extends FastAdapter.ViewHolder<Transaction> {


        private final AppCompatImageView txnImage;
        private final AppCompatTextView txnName;
        private final AppCompatTextView txnSubHeading;
        private final AppCompatTextView txnTime;
        private final AppCompatTextView txnValue;


        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txnImage = itemView.findViewById(R.id.txn_image);
            txnName = itemView.findViewById(R.id.txn_name);
            txnSubHeading = itemView.findViewById(R.id.txn_sub_heading);
            txnTime = itemView.findViewById(R.id.txn_time);
            txnValue = itemView.findViewById(R.id.txn_value);
        }

        @Override
        public void bindView(@NonNull Transaction transaction, @NonNull List<?> list) {
            Glide.with(txnImage.getContext())
                    .load(transaction.source.equalsIgnoreCase("Online") ? R.drawable.rupee : R.drawable.rupee)
                    .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .skipMemoryCache(true)
                    .centerCrop()
                    .dontAnimate()
                    .dontTransform()
                    .priority(Priority.IMMEDIATE)
                    .encodeFormat(Bitmap.CompressFormat.PNG)
                    .format(DecodeFormat.DEFAULT)
                    .into(txnImage);

            txnName.setText(!transaction.reason.isEmpty() ? transaction.reason :
                    transaction.type.equalsIgnoreCase("Credit") ?
                            txnName.getContext().getString(R.string.wallet_credited) :
                            txnName.getContext().getString(R.string.wallet_debited));

            txnSubHeading.setText(!transaction.description.isEmpty() ? transaction.description :
                    txnName.getContext().getString(R.string.by_system));

            txnTime.setText(DateUtils.getStringFromMillis(transaction.created_date.toDate().getTime(), "EEE MMM dd, yyyy hh:mm a", true));


            if (transaction.amount instanceof Long) {
                txnValue.setText(transaction.type.equalsIgnoreCase("Credit") ?
                        "+₹" + new DecimalFormat("0.00").format(transaction.amount) :
                        "-₹" + new DecimalFormat("0.00").format(transaction.amount));

            } else if (transaction.amount instanceof String) {
                if(((String) transaction.amount).contains(".")) {
                    txnValue.setText(transaction.type.equalsIgnoreCase("Credit") ?
                            "+₹" + new DecimalFormat("0.00").format(Long.parseLong(String.valueOf(((String) transaction.amount).split("\\.")[0]))) :
                            "-₹" + new DecimalFormat("0.00").format(Long.parseLong(String.valueOf(((String) transaction.amount).split("\\.")[0]))));
                } else {
                    txnValue.setText(transaction.type.equalsIgnoreCase("Credit") ?
                            "+₹" + new DecimalFormat("0.00").format(Long.parseLong(String.valueOf(transaction.amount))) :
                            "-₹" + new DecimalFormat("0.00").format(Long.parseLong(String.valueOf(transaction.amount))));
                }
            }

            txnValue.setTextColor(transaction.type.equalsIgnoreCase("Credit") ?
                    ContextCompat.getColor(txnValue.getContext(), R.color.green) :
                    ContextCompat.getColor(txnValue.getContext(), android.R.color.holo_red_light));

            if (transaction.status.equals("0")) {
                txnImage.setBackgroundTintList(ContextCompat.getColorStateList(txnImage.getContext(), R.color.gray));
                txnName.setTextColor(ContextCompat.getColor(txnValue.getContext(), R.color.gray));
                txnSubHeading.setTextColor(ContextCompat.getColor(txnValue.getContext(), R.color.gray));
                txnTime.setTextColor(ContextCompat.getColor(txnValue.getContext(), R.color.gray));
                txnValue.setTextColor(ContextCompat.getColor(txnValue.getContext(), R.color.gray));

                if (transaction.amount instanceof Long) {
                    txnValue.setText(String.format("₹%s", new DecimalFormat("0.00").format(transaction.amount)));

                } else if (transaction.amount instanceof String) {
                    txnValue.setText(String.format("₹%s", new DecimalFormat("0.00").format(Long.parseLong(String.valueOf(transaction.amount)))));
                }
            } else {
                txnImage.setBackgroundTintList(null);
            }
        }

        @Override
        public void unbindView(@NonNull Transaction transaction) {

        }
    }
}
