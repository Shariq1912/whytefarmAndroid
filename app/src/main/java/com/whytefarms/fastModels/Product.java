package com.whytefarms.fastModels;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.google.firebase.Timestamp;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.whytefarms.R;
import com.whytefarms.utils.DateUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;


@Keep
public class Product extends AbstractItem<Product.ShopProductViewHolder> {

    public String productId;
    public String brand;
    public String category;
    public String gst;

    public String image_transparent_bg;

    public Boolean inStock;

    public List<Map<String, String>> packagingOptions;

    public String productDescription;

    public String productName;

    public Double rating;

    public Timestamp launchDate;


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_product;
    }


    @NonNull
    @Override
    public Product clone() {
        Product clone;
        try {
            clone = (Product) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new Product();
        }


        clone.productId = this.productId;
        clone.brand = this.brand;
        clone.category = this.category;
        clone.gst = this.gst;
        clone.image_transparent_bg = this.image_transparent_bg;
        clone.inStock = this.inStock;
        clone.packagingOptions = this.packagingOptions;
        clone.productDescription = this.productDescription;
        clone.productName = this.productName;
        clone.rating = this.rating;
        clone.launchDate = this.launchDate;

        return clone;
    }

    @NonNull
    @Override
    public ShopProductViewHolder getViewHolder(@NonNull View view) {
        return new ShopProductViewHolder(view);
    }

    public static class ShopProductViewHolder extends FastAdapter.ViewHolder<Product> {

        public final CardView productCard;
        private final AppCompatImageView productImage;
        private final AppCompatTextView productTitle;
        private final AppCompatTextView productVariant;
        private final AppCompatTextView priceBeforeDiscount;
        private final AppCompatTextView productPrice;
        private final AppCompatTextView productRating;
        private final AppCompatTextView newLaunch;

        public ShopProductViewHolder(@NonNull View itemView) {
            super(itemView);

            productCard = itemView.findViewById(R.id.product_card);
            productImage = itemView.findViewById(R.id.product_image);
            productTitle = itemView.findViewById(R.id.product_title);
            productVariant = itemView.findViewById(R.id.product_variant);
            priceBeforeDiscount = itemView.findViewById(R.id.price_before_discount);
            productPrice = itemView.findViewById(R.id.product_price);
            productRating = itemView.findViewById(R.id.product_rating);
            newLaunch = itemView.findViewById(R.id.new_launch);
        }

        @Override
        public void bindView(@NonNull Product item, @NonNull List<?> list) {

            Glide.with(productImage.getContext())
                    .load(item.image_transparent_bg)
                    // .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .skipMemoryCache(true)
                    .centerCrop()
                    .dontAnimate()
                    .dontTransform()
                    .placeholder(R.drawable.ic_picture_placeholder)
                    .priority(Priority.IMMEDIATE)
                    .encodeFormat(Bitmap.CompressFormat.PNG)
                    .format(DecodeFormat.DEFAULT)
                    .into(productImage);

            //productImage.setRotation(new Random().nextInt(15 + 15) - 15);

            productTitle.setText(item.productName);

            StringBuilder variantString = new StringBuilder();

            item.packagingOptions.forEach(option -> variantString.append(option.get("packaging"))
                    .append(" ")
                    .append(option.get("pkgUnit"))
                    .append(", "));

            variantString.replace(variantString.length() - 2, variantString.length(), "");

            productVariant.setText(variantString.toString());

            priceBeforeDiscount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            String priceBeforeDisc = item.packagingOptions.get(0).get("priceBeforeDiscount");
            String price = item.packagingOptions.get(0).get("price");

            if (priceBeforeDisc != null) {
                priceBeforeDiscount.setText(productPrice.getResources().getString(R.string.price_placeholder, Long.parseLong(priceBeforeDisc)));
            }

            if (price != null) {
                productPrice.setText(productPrice.getResources().getString(R.string.price_placeholder, Long.parseLong(price)));
            }


            productRating.setText(String.format(Locale.US, "%.1f", item.rating));

            DisplayMetrics displaymetrics = productCard.getContext().getResources().getDisplayMetrics();
            int deviceWidth = displaymetrics.widthPixels / 2;
            productCard.getLayoutParams().height = (int) (deviceWidth * 1.2);
            newLaunch.setVisibility(
                    (Timestamp.now().toDate().getTime() - DateUtils.getTimeAfterAddingDays(item.launchDate.toDate().getTime(), 0)) < 0 ?
                            View.VISIBLE : View.GONE);
        }

        @Override
        public void unbindView(@NonNull Product item) {

        }
    }
}
