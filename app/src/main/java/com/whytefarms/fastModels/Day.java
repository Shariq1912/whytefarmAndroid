package com.whytefarms.fastModels;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;

import java.util.List;
import java.util.Locale;

@Keep
public class Day extends AbstractItem<Day.DayViewHolder> {

    private final String dayName;
    private Long quantity;

    public Day(String dayName, Long quantity) {
        this.dayName = dayName;
        this.quantity = quantity;
    }

    public String getDayName() {
        return this.dayName;
    }

    public Long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    @Override
    public int getType() {
        return R.id.day_layout_main;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_day;
    }

    @NonNull
    @Override
    public Day.DayViewHolder getViewHolder(@NonNull View view) {
        return new DayViewHolder(view);
    }

    public static class DayViewHolder extends FastAdapter.ViewHolder<Day> {


        public final AppCompatImageButton minusButton;
        public final AppCompatImageButton plusButton;
        private final AppCompatTextView dayName;
        private final AppCompatTextView quantity;


        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayName = itemView.findViewById(R.id.day_name);
            quantity = itemView.findViewById(R.id.quantity);
            minusButton = itemView.findViewById(R.id.minus_button);
            plusButton = itemView.findViewById(R.id.plus_button);
        }

        @Override
        public void bindView(@NonNull Day item, @NonNull List<?> list) {
            dayName.setText(item.getDayName());
            quantity.setText(String.format(Locale.getDefault(), "%d", item.getQuantity()));

            toggleButtons(item.getQuantity());

            quantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.toString().isEmpty()) {
                        quantity.setText("0");
                    }

                    toggleButtons(!editable.toString().isEmpty() ? Long.parseLong(editable.toString()) : 0);
                }
            });

        }

        public void toggleButtons(Long qty) {
            if (qty == 0) {
                minusButton.setBackgroundTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.gray));
                minusButton.setImageTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.white));
                minusButton.setEnabled(false);
                dayName.setTextColor(ContextCompat.getColorStateList(quantity.getContext(), R.color.gray));
            } else {
                minusButton.setBackgroundTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.black));
                minusButton.setImageTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.gold));
                minusButton.setEnabled(true);
                dayName.setTextColor(MaterialColors.getColor(dayName.getContext(), android.R.attr.textColorPrimary, Color.BLACK));
            }

            if (qty == 99999) {
                plusButton.setBackgroundTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.gray));
                plusButton.setImageTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.white));
                plusButton.setEnabled(false);
            } else {
                plusButton.setBackgroundTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.black));
                plusButton.setImageTintList(ContextCompat.getColorStateList(quantity.getContext(), R.color.gold));
                plusButton.setEnabled(true);
            }
        }

        @Override
        public void unbindView(@NonNull Day item) {

        }
    }
}
