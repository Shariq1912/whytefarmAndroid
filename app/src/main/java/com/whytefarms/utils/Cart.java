package com.whytefarms.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.whytefarms.R;
import com.whytefarms.activities.BaseActivity;
import com.whytefarms.activities.CheckoutActivity;
import com.whytefarms.activities.DescriptionActivity;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fastModels.Subscription;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public class Cart {
    private final Object context;

    public Cart(Object context) {
        this.context = context;
    }

    public void setCartValue() {
        AppCompatTextView numCartItems = null;
        FrameLayout menuCart = null;
        try {
            if (context instanceof Fragment) {
                numCartItems = ((Fragment) context).requireView().findViewById(R.id.num_cart_items);
                menuCart = ((Fragment) context).requireView().findViewById(R.id.menu_cart);
                if (numCartItems != null) {
                    menuCart.setVisibility(View.GONE);
                }
            } else if (context instanceof BaseActivity) {
                numCartItems = ((BaseActivity) context).findViewById(R.id.num_cart_items);
                menuCart = ((BaseActivity) context).findViewById(R.id.menu_cart);
                if (numCartItems != null) {
                    menuCart.setVisibility(View.GONE);
                }
            }
        } catch (Exception ignored) {
        }


        SharedPreferences pref = getSharedPreference();

        if (pref != null) {
            JSONArray items;
            try {
                if (!pref.getString("cart_items", "").isEmpty()) {
                    items = new JSONArray(pref.getString("cart_items", ""));
                } else {
                    items = new JSONArray();
                }

                if (numCartItems != null && menuCart != null) {
                    int numItems = items.length();
                    if (numItems > 0) {
                        numCartItems.setVisibility(View.VISIBLE);
                        menuCart.setVisibility(View.VISIBLE);
                        numCartItems.setText(String.format(Locale.ENGLISH, "%d", numItems));
                    } else {
                        menuCart.setVisibility(View.GONE);
                        numCartItems.setVisibility(View.GONE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveItemToCart(Subscription item) {
        SharedPreferences pref = getSharedPreference();

        if (pref != null) {
            SharedPreferences.Editor editor = pref.edit();
            JSONArray items;
            try {
                if (pref.contains("cart_items") && !pref.getString("cart_items", "").isEmpty()) {
                    items = new JSONArray(pref.getString("cart_items", ""));
                } else {
                    items = new JSONArray();
                }
                int exists = -1;

                for (int i = 0; i < items.length(); i++) {
                    if (items.get(i) == null || items.get(i) == JSONObject.NULL) {
                        items.remove(i--);
                    }

                    if (item.subscription_id.equals(((JSONObject) items.get(i)).getString("subscription_id"))) {
                        exists = i;
                        break;
                    }
                }

                JSONObject jsonObject = new JSONObject(item.toString());
                if (exists > -1) {
                    items.put(exists, jsonObject);
                } else {
                    items.put(jsonObject);
                }

                editor.putString("cart_items", String.valueOf(items));
                editor.apply();

                if (context instanceof DescriptionActivity) {
                    ((DescriptionActivity) context).getSupportFragmentManager().beginTransaction()
                            .remove(Objects.requireNonNull(((DescriptionActivity) context).getSupportFragmentManager().findFragmentByTag("add_subscription"))).commitNow();

                    if (item.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
                        Toast.makeText(((DescriptionActivity) context), R.string.added_to_cart_successfully, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(((DescriptionActivity) context), R.string.subscription_added_to_cart_successfully, Toast.LENGTH_SHORT).show();
                    }

                    ((DescriptionActivity) context).startActivity(new Intent(((DescriptionActivity) context), CheckoutActivity.class));
                }
            } catch (JSONException e) {
                Toast.makeText(((DescriptionActivity) context), R.string.subscription_added_to_cart_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("TAG", "saveItemToCart: pref is null");
        }

        setCartValue();
    }

    private SharedPreferences getSharedPreference() {
        SharedPreferences pref = null;
        if (context instanceof WhyteFarmsApplication) {
            pref = ((WhyteFarmsApplication) context).getSharedPreferences("cart", MODE_PRIVATE);
        } else if (context instanceof Fragment) {
            try {
                pref = ((Fragment) context).requireContext().getSharedPreferences("cart", MODE_PRIVATE);
            } catch (Exception ignored) {

            }
        } else if (context instanceof BaseActivity) {
            pref = ((BaseActivity) context).getSharedPreferences("cart", Context.MODE_PRIVATE);
        } else if (context instanceof Context) {
            pref = ((Context) context).getSharedPreferences("cart", Context.MODE_PRIVATE);
        }

        return pref;
    }

    public void deleteItemFromCart(String itemID) {
        SharedPreferences pref = getSharedPreference();

        if (pref != null) {
            SharedPreferences.Editor editor = pref.edit();
            JSONArray items;
            try {
                if (!pref.getString("cart_items", "").isEmpty()) {
                    items = new JSONArray(pref.getString("cart_items", ""));
                } else {
                    items = new JSONArray();
                }

                int index = -1;
                for (int i = 0; i < items.length(); i++) {
                    if (items.get(i) != null && items.get(i) instanceof JSONObject) {
                        JSONObject item = items.getJSONObject(i);
                        if (item != null) {
                            if (item.getString("subscription_id").equals(itemID)) {
                                index = i;
                                break;
                            }
                        }
                    }
                }

                if (index > -1) {
                    items.remove(index);
                    editor.putString("cart_items", String.valueOf(items));
                    editor.apply();
                }

                checkEmptyOrder(itemID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setCartValue();
        }
    }

    private void checkEmptyOrder(String itemID) {
        SharedPreferences pref = getSharedPreference();

        if (pref != null) {
            JSONArray items;
            try {
                if (!pref.getString("cart_items", "").isEmpty()) {
                    items = new JSONArray(pref.getString("cart_items", ""));
                } else {
                    items = new JSONArray();
                }

                if (items.length() == 0) {
                    if (context instanceof CheckoutActivity) {
                        ((CheckoutActivity) context).setUpCartRecycler();
                    }
                } else {
                    if (context instanceof CheckoutActivity) {
                        ((CheckoutActivity) context).removeFromAdapter(itemID);
                        ((CheckoutActivity) context).toggleProgressBar(false);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (context instanceof CheckoutActivity) {
                ((CheckoutActivity) context).removeFromAdapter(itemID);
                ((CheckoutActivity) context).toggleProgressBar(false);
            }
        }
    }

    public void clearCart() {
        SharedPreferences pref = getSharedPreference();

        if (pref != null) {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
        }
    }
}