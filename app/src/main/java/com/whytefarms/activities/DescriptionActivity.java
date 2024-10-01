package com.whytefarms.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.whytefarms.R;
import com.whytefarms.fastModels.Item;
import com.whytefarms.fastModels.Product;
import com.whytefarms.fragments.FragmentSubscribe;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.Cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DescriptionActivity extends BaseActivity {


    private final FastItemAdapter<Item> variantAdapter = new FastItemAdapter<>();
    private final List<Item> variantList = new ArrayList<>();
    private final List<String> priceList = new ArrayList<>();
    private final List<String> priceBeforeDiscountList = new ArrayList<>();
    public FrameLayout progressBar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppCompatImageView productImage;
    private AppCompatTextView productDescription;
    private AppCompatTextView productRating;
    private AppCompatTextView productPrice;
    private AppCompatTextView productPriceBeforeDiscount;
    private AppCompatTextView productTitle;
    private String productName = "";

    private Cart cart;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish();
            }
        });


        FirebaseFirestore database = FirebaseFirestore.getInstance();
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        collapsingToolbarLayout.setTitleEnabled(false);
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
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        progressBar = findViewById(R.id.progress_bar);

        productImage = findViewById(R.id.product_image);
        RecyclerView variantRecycler = findViewById(R.id.variant_recycler);
        productDescription = findViewById(R.id.product_description);
        productRating = findViewById(R.id.product_rating);
        productPrice = findViewById(R.id.product_price);
        productPriceBeforeDiscount = findViewById(R.id.product_price_before_discount);
        AppCompatTextView buyOnceButton = findViewById(R.id.buy_once);
        AppCompatTextView subscribeButton = findViewById(R.id.subscribe);


        variantRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) variantRecycler.getItemAnimator();

        if (itemAnimator != null) {
            itemAnimator.setSupportsChangeAnimations(false);
        }
        variantRecycler.setAdapter(variantAdapter);

       // AppCompatTextView numCartItems = findViewById(R.id.num_cart_items);
        FrameLayout menuCart = findViewById(R.id.menu_cart);

        menuCart.setOnClickListener(view -> startActivity(new Intent(DescriptionActivity.this, CheckoutActivity.class)));


        buyOnceButton.setOnClickListener(view -> {
            FragmentSubscribe fragmentSubscribe = FragmentSubscribe.newInstance("", AppConstants.SUBSCRIPTION_TYPE_ONE_TIME);
            fragmentSubscribe.show(getSupportFragmentManager(), "add_subscription");
        });

        subscribeButton.setOnClickListener(view -> {
            FragmentSubscribe fragmentSubscribe = FragmentSubscribe.newInstance("", "");
            fragmentSubscribe.show(getSupportFragmentManager(), "add_subscription");
        });

        productTitle = findViewById(R.id.product_title);


        Bundle b = getIntent().getExtras();

        if (b != null) {
            String productID = b.getString("productID");

            if (productID != null && !productID.isEmpty()) {
                toggleProgressBar(true);
                database.collection(AppConstants.PRODUCTS_DATA_COLLECTION).where(Filter.equalTo("productId", productID))
                        .limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                if (documentSnapshot.exists()) {
                                    Product product = documentSnapshot.toObject(Product.class);

                                    if (product != null) {
                                        productTitle.setText(product.productName);
                                        Glide.with(productImage.getContext())
                                                .load(product.image_transparent_bg)
                                                .skipMemoryCache(true)
                                                .centerCrop()
                                                .dontAnimate()
                                                .dontTransform()
                                                .priority(Priority.IMMEDIATE)
                                                .into(productImage);
                                        collapsingToolbarLayout.setTitleEnabled(true);
                                        collapsingToolbarLayout.setTitle(product.productName);
                                        productName = product.productName;
                                        productDescription.setText(product.productDescription);

                                        productRating.setText(String.format(Locale.US, "%.1f", product.rating));
                                        productPriceBeforeDiscount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                                        final String[] priceBeforeDisc = {product.packagingOptions.get(0).get("priceBeforeDiscount")};
                                        final String[] price = {product.packagingOptions.get(0).get("price")};

                                        if (priceBeforeDisc[0] != null) {
                                            productPriceBeforeDiscount.setText(productPrice.getResources().getString(R.string.price_placeholder, Long.parseLong(priceBeforeDisc[0])));
                                        }

                                        if (price[0] != null) {
                                            productPrice.setText(productPrice.getResources().getString(R.string.price_placeholder, Long.parseLong(price[0])));
                                        }


                                        variantAdapter.clear();

                                        List<Map<String, String>> packagingOptions = product.packagingOptions;

                                        packagingOptions.forEach(option -> {
                                            priceBeforeDisc[0] = option.get("priceBeforeDiscount");
                                            price[0] = option.get("price");
                                            Item newCat = new Item(option.get("packaging") + " " + option.get("pkgUnit"), "Variant");

                                            variantList.add(newCat);

                                            priceBeforeDiscountList.add(priceBeforeDisc[0]);
                                            priceList.add(price[0]);
                                        });

                                        if (!variantList.isEmpty()) {
                                            variantList.get(0).setSelected(true);

                                            variantList.sort((c1, c2) -> {
                                                if (c1.getItemTitle() != null && c2.getItemTitle() != null) {
                                                    return c2.getItemTitle().compareTo(c1.getItemTitle());
                                                }
                                                return 0;
                                            });

                                            variantAdapter.add(variantList);
                                            variantAdapter.notifyAdapterDataSetChanged();
                                        }
                                    }
                                }
                            }
                            toggleProgressBar(false);
                        });
            }
        }

        variantAdapter.addEventHook(new ClickEventHook<Item>() {
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
                variantAdapter.getAdapterItems().forEach(itemModel -> {
                    itemModel.setSelected(false);
                    variantAdapter.notifyAdapterItemChanged(variantAdapter.getAdapterPosition(itemModel));
                });

                item.setSelected(true);
                variantAdapter.notifyAdapterItemChanged(variantAdapter.getAdapterPosition(item));
                productPrice.setText(String.format(getString(R.string.price_placeholder), Long.parseLong(priceList.get(variantAdapter.getAdapterPosition(item)))));
                productPriceBeforeDiscount.setText(String.format(getString(R.string.price_placeholder), Long.parseLong(priceBeforeDiscountList.get(variantAdapter.getAdapterPosition(item)))));
            }
        });

        toggleProgressBar(true);

        cart = new Cart(this);
    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }


    public String getProductName() {
        return this.productName;
    }


    public String getSelectedPackagingUnit() {
        if(!variantAdapter.getAdapterItems().isEmpty()) {

            //Crash Mitigation IndexOutOfBoundsException - Index 0 out of bounds for length 0

            Item variant = variantAdapter.getAdapterItem(0);
            if(variant != null) {
                final String[] selectedPackagingUnit = {variant.getItemTitle()};

                for (Item item : variantAdapter.getAdapterItems()) {
                    if (item.isSelected()) {
                        selectedPackagingUnit[0] = item.getItemTitle();
                        break;
                    }
                }

                return selectedPackagingUnit[0];
            } else {
                return "";
            }
        } else {
            return "";
        }

    }


    public Long getSelectedPackagingUnitPrice() {
        final Long[] selectedPackagingUnitPrice = {Long.parseLong(priceList.get(0))};

        for (Item item : variantAdapter.getAdapterItems()) {
            if (item.isSelected()) {
                selectedPackagingUnitPrice[0] = Long.parseLong(priceList.get(variantAdapter.getAdapterPosition(item)));
                break;
            }
        }

        return selectedPackagingUnitPrice[0];


    }

    @Override
    protected void onStart() {
        super.onStart();
        cart.setCartValue();
        checkVacation();
    }
}