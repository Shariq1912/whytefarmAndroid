package com.whytefarms.fastModels;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;

import java.util.List;

@Keep
public class Item extends AbstractItem<Item.ItemViewHolder> {

    public final String itemType;
    private final String itemTitle;

    public Item(String title, String type) {
        this.itemTitle = title;
        this.itemType = type;
    }

    public String getItemTitle() {
        return this.itemTitle;
    }

    public String getItemType() {
        return this.itemType;
    }

    @Override
    public int getType() {
        return R.id.item_text;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item;
    }

    @NonNull
    @Override
    public ItemViewHolder getViewHolder(@NonNull View view) {
        return new ItemViewHolder(view);
    }

    public static class ItemViewHolder extends FastAdapter.ViewHolder<Item> {

        public final AppCompatTextView itemText;
        private final CardView itemMainLayout;
        private final LinearLayout itemLayout;
        private final Context mContext;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            itemMainLayout = itemView.findViewById(R.id.item_layout);
            itemLayout = itemView.findViewById(R.id.item);
            itemText = itemView.findViewById(R.id.item_text);
        }

        @Override
        public void bindView(@NonNull Item item, @NonNull List<?> list) {
            itemText.setText(item.itemTitle);
            if (item.isSelected()) {
                itemLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_rectangle_selected));
                itemText.setTextColor(ContextCompat.getColor(mContext, R.color.gold));
            } else {
                itemLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_rectangle_unselected));
                itemText.setTextColor(MaterialColors.getColor(itemText, android.R.attr.colorForeground));
            }

            if (item.getItemType().equals("Variant")) {
                itemText.setText(item.itemTitle);
            }

            if (item.getItemType().equals("Interval")) {
                ViewGroup.LayoutParams params = itemMainLayout.getLayoutParams();
                DisplayMetrics dm = itemMainLayout.getContext().getResources().getDisplayMetrics();
                params.width = (int) (150 * dm.scaledDensity);
                itemMainLayout.setLayoutParams(params);
            }


        }

        @Override
        public void unbindView(@NonNull Item item) {

        }
    }
}
