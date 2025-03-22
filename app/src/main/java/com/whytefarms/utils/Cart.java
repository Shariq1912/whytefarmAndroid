package com.whytefarms.utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.whytefarms.R;
import com.whytefarms.activities.BaseActivity;
import com.whytefarms.activities.CheckoutActivity;
import com.whytefarms.activities.DescriptionActivity;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.fastModels.Subscription;
import com.whytefarms.models.CartModel;
import com.whytefarms.models.CartProduct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class Cart {
    private static final String TAG = "Cart";
    private final Object context;
    private final FirebaseFirestore db;

    public Cart(Object context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
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

        if (isLoggedIn()) {
            // Get cart count from Firestore
            String customerId = getCustomerId();
            if (customerId != null) {
                AppCompatTextView finalNumCartItems = numCartItems;
                FrameLayout finalMenuCart = menuCart;
                AppCompatTextView finalNumCartItems1 = numCartItems;
                FrameLayout finalMenuCart1 = menuCart;
                db.collection("cart_data")
                        .whereEqualTo("customer_id", customerId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                CartModel cart = document.toObject(CartModel.class);
                                if (cart != null && cart.getProducts() != null) {
                                    int numItems = cart.getProducts().size();
                                    updateCartUI(finalNumCartItems, finalMenuCart, numItems);
                                } else {
                                    updateCartUI(finalNumCartItems, finalMenuCart, 0);
                                }
                            } else {
                                updateCartUI(finalNumCartItems, finalMenuCart, 0);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error getting cart count from Firestore", e);
                            updateCartUI(finalNumCartItems1, finalMenuCart1, 0);
                        });
            } else {
                updateCartUI(numCartItems, menuCart, 0);
            }
        } else {
            // Use existing SharedPreferences logic
            SharedPreferences pref = getSharedPreference();
            if (pref != null) {
                try {
                    if (!pref.getString("cart_items", "").isEmpty()) {
                        JSONArray items = new JSONArray(pref.getString("cart_items", ""));
                        updateCartUI(numCartItems, menuCart, items.length());
                    } else {
                        updateCartUI(numCartItems, menuCart, 0);
                    }
                } catch (JSONException e) {
                    updateCartUI(numCartItems, menuCart, 0);
                }
            }
        }
    }

    private void updateCartUI(AppCompatTextView numCartItems, FrameLayout menuCart, int numItems) {
        if (numCartItems != null && menuCart != null) {
            if (numItems > 0) {
                numCartItems.setVisibility(View.VISIBLE);
                menuCart.setVisibility(View.VISIBLE);
                numCartItems.setText(String.format(Locale.ENGLISH, "%d", numItems));
            } else {
                menuCart.setVisibility(View.GONE);
                numCartItems.setVisibility(View.GONE);
            }
        }
    }

    public void saveItemToCart(Subscription item) {
        if (isLoggedIn()) {
            saveToFirestore(item);
        } else {
            saveToPreferences(item);
        }
        setCartValue();
    }

    private void saveToFirestore(Subscription item) {
        String customerId = getCustomerId();
        if (customerId == null) return;

        // First query to check if user already has a cart
        db.collection("cart_data")
                .whereEqualTo("customer_id", customerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String docId;
                        CartModel cart;
                        
                        if (!task.getResult().isEmpty()) {
                            // Use existing cart document
                            docId = task.getResult().getDocuments().get(0).getId();
                            cart = task.getResult().getDocuments().get(0).toObject(CartModel.class);
                        } else {
                            // Create new cart with auto-generated ID
                            docId = db.collection("cart_data").document().getId();
                            cart = new CartModel();
                            cart.setCustomer_id(customerId);
                            cart.setCustomer_name(getCustomerName());
                            cart.setCustomer_phone(getCustomerPhone());
                            cart.setPlatform("Android");
                        }

                        // Rest of your existing cart update logic
                        List<CartProduct> products = cart.getProducts() != null ? cart.getProducts() : new ArrayList<>();
                        CartProduct product = createCartProduct(item);
                        
                        // Update or add product
                        boolean exists = false;
                        for (int i = 0; i < products.size(); i++) {
                            if (products.get(i).getProduct_id().equals(item.subscription_id)) {
                                products.set(i, product);
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            products.add(product);
                        }

                        cart.setProducts(products);
                        updateCartTotals(cart);

                        // Save to Firestore using the docId
                        String finalDocId = docId;
                        db.collection("cart_data")
                                .document(docId)
                                .set(cart)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cart updated successfully in Firestore document: " + finalDocId);
                                    Log.d(TAG, "Cart data: " + cart.getProducts().size() + " products, total price: " + cart.getTotal_price());
                                    handleSuccessfulAdd(item);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating cart in Firestore document: " + finalDocId, e);
                                    saveToPreferences(item);
                                });
                    }
                });
    }

    private CartProduct createCartProduct(Subscription item) {
        CartProduct product = new CartProduct();
        
        // Set current timestamp
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        product.setTimestamp(timestampFormat.format(new Date()));
        
        // Handle dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000ZZ", Locale.US);
        if (item.end_date != null) {
            if (item.end_date instanceof Timestamp) {
                Timestamp ts = (Timestamp) item.end_date;
                Date endDate = ts.toDate();
                product.setEndDate(dateFormat.format(endDate));
            }
        }
        if (item.start_date != null) {
            if (item.start_date instanceof Timestamp) {
                Timestamp ts = (Timestamp) item.start_date;
                Date startDate = ts.toDate();
                product.setStartDate(dateFormat.format(startDate));
            }
        }
        product.setNext_delivery_date(item.next_delivery_date);
        
        // Product details
        product.setProduct_name(item.product_name);
        product.setProductName(item.product_name);
        product.setProduct(item.product_name);
        product.setProduct_id(item.subscription_id);
        product.setPackage_unit(item.package_unit);
        product.setPackaging("1");
        
        // Handle numeric values - directly use the long values
        product.setPrice(item.price);
        product.setQuantity(item.quantity);
        product.setSubscriptionType(item.subscription_type);
        product.setReason(item.reason != null ? item.reason : "");
        
        // Weekly schedule - directly use the long values
        product.setMonday(item.monday);
        product.setTuesday(item.tuesday);
        product.setWednesday(item.wednesday);
        product.setThursday(item.thursday);
        product.setFriday(item.friday);
        product.setSaturday(item.saturday);
        product.setSunday(item.sunday);
        product.setInterval((Long) item.interval); // Remove the String cast

        return product;
    }

    private void handleSuccessfulAdd(Subscription item) {
        if (context instanceof DescriptionActivity) {
            DescriptionActivity activity = (DescriptionActivity) context;
            activity.getSupportFragmentManager().beginTransaction()
                    .remove(activity.getSupportFragmentManager().findFragmentByTag("add_subscription"))
                    .commitNow();

            String message = item.subscription_type.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME) 
                    ? activity.getString(R.string.added_to_cart_successfully)
                    : activity.getString(R.string.subscription_added_to_cart_successfully);
            
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            activity.startActivity(new Intent(activity, CheckoutActivity.class));
        }
    }

    private void saveToPreferences(Subscription item) {
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
    }

    private boolean isLoggedIn() {
        Context appContext = getApplicationContext();
        if (appContext instanceof WhyteFarmsApplication) {
            return ((WhyteFarmsApplication) appContext).isLoggedIn();
        }
        return false;
    }

    private String getCustomerId() {
        Context appContext = getApplicationContext();
        if (appContext instanceof WhyteFarmsApplication) {
            return ((WhyteFarmsApplication) appContext).getCustomerIDFromLoginState();
        }
        return null;
    }

    private String getCustomerName() {
        Context appContext = getApplicationContext();
        if (appContext instanceof WhyteFarmsApplication) {
            // Get customer name from your stored customer data
            return ""; // Implement this based on where you store customer data
        }
        return "";
    }

    private String getCustomerPhone() {
        Context appContext = getApplicationContext();
        if (appContext instanceof WhyteFarmsApplication) {
            // Get customer phone from your stored customer data
            return ""; // Implement this based on where you store customer data
        }
        return "";
    }

    private Context getApplicationContext() {
        if (context instanceof Activity) {
            return ((Activity) context).getApplicationContext();
        } else if (context instanceof Fragment) {
            return ((Fragment) context).requireContext().getApplicationContext();
        } else if (context instanceof Context) {
            return (Context) context;
        }
        return null;
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
        if (isLoggedIn()) {
            deleteFromFirestore(itemID);
        } else {
            deleteFromPreferences(itemID);
        }
    }

    private void deleteFromFirestore(String itemID) {
        String customerId = getCustomerId();
        if (customerId == null) return;

        // First find the cart document by customer_id
        db.collection("cart_data")
                .whereEqualTo("customer_id", customerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the document ID
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String docId = document.getId();
                        CartModel cart = document.toObject(CartModel.class);
                        
                        if (cart != null && cart.getProducts() != null) {
                            List<CartProduct> products = cart.getProducts();
                            // Find and remove the product with matching ID
                            boolean removed = products.removeIf(product -> 
                                product.getProduct_id() != null && 
                                product.getProduct_id().equals(itemID));
                            
                            if (removed) {
                                // Update cart totals
                                updateCartTotals(cart);
                                
                                // Update Firestore
                                db.collection("cart_data")
                                        .document(docId)
                                        .set(cart)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Item deleted from Firestore cart");
                                            checkEmptyOrder(itemID);
                                            setCartValue();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error deleting item from Firestore cart", e);
                                            deleteFromPreferences(itemID);
                                        });
                            } else {
                                Log.d(TAG, "Item not found in cart");
                                checkEmptyOrder(itemID);
                            }
                        } else {
                            Log.d(TAG, "Cart is empty");
                            checkEmptyOrder(itemID);
                        }
                    } else {
                        Log.e(TAG, "No cart found for customer");
                        deleteFromPreferences(itemID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting cart from Firestore", e);
                    deleteFromPreferences(itemID);
                });
    }

    private void deleteFromPreferences(String itemID) {
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
        if (isLoggedIn()) {
            clearFirestoreCart();
        } else {
            clearPreferencesCart();
        }
    }

    private void clearFirestoreCart() {
        String customerId = getCustomerId();
        if (customerId == null) return;

        db.collection("cart_data")
                .whereEqualTo("customer_id", customerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        
                        // Delete the document
                        db.collection("cart_data")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cart cleared from Firestore");
                                    setCartValue();
                                })
                                .addOnFailureListener(e -> 
                                    Log.e(TAG, "Error clearing cart from Firestore", e));
                    }
                });
    }

    private void clearPreferencesCart() {
        SharedPreferences pref = getSharedPreference();
        if (pref != null) {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
        }
    }

    // Add this method to migrate preferences cart to Firestore on login
    public void migrateCartToFirestore() {
        if (!isLoggedIn()) return;
        
        SharedPreferences pref = getSharedPreference();
        if (pref == null || !pref.contains("cart_items")) return;

        try {
            String cartString = pref.getString("cart_items", "");
            if (cartString.isEmpty()) return;
            
            JSONArray items = new JSONArray(cartString);
            if (items.length() == 0) return;

            // Create cart items from preferences
            List<CartProduct> products = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                
                // Create Subscription from JSON
                Subscription subscription = new Subscription();
                subscription.sunday = item.optLong("sunday", 0);
                subscription.monday = item.optLong("monday", 0);
                subscription.tuesday = item.optLong("tuesday", 0);
                subscription.wednesday = item.optLong("wednesday", 0);
                subscription.thursday = item.optLong("thursday", 0);
                subscription.friday = item.optLong("friday", 0);
                subscription.saturday = item.optLong("saturday", 0);
                subscription.interval = item.optLong("interval", 0);
                subscription.package_unit = item.optString("package_unit", "");
                subscription.price = item.optLong("price", 0);
                subscription.product_name = item.optString("product_name", "");
                subscription.quantity = item.optLong("quantity", 0);
                subscription.subscription_id = item.optString("subscription_id", "");
                subscription.subscription_type = item.optString("subscription_type", "");
                subscription.next_delivery_date = item.optString("next_delivery_date", "");
                
                // Handle dates
                long startDateMillis = item.optLong("start_date", 0);
                if (startDateMillis > 0) {
                    subscription.start_date = new Timestamp(new Date(startDateMillis));
                }
                
                long endDateMillis = item.optLong("end_date", 0);
                if (endDateMillis > 0) {
                    subscription.end_date = new Timestamp(new Date(endDateMillis));
                }
                
                // Convert to CartProduct
                CartProduct product = createCartProduct(subscription);
                products.add(product);
            }

            // Create new cart or update existing
            String customerId = getCustomerId();
            if (customerId == null) return;
            
            // Check if cart already exists
            db.collection("cart_data")
                    .whereEqualTo("customer_id", customerId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            CartModel cart;
                            String docId;
                            
                            if (!task.getResult().isEmpty()) {
                                // Update existing cart
                                docId = task.getResult().getDocuments().get(0).getId();
                                cart = task.getResult().getDocuments().get(0).toObject(CartModel.class);
                                
                                // Merge products
                                List<CartProduct> existingProducts = cart.getProducts();
                                if (existingProducts == null) {
                                    existingProducts = new ArrayList<>();
                                }
                                
                                // Add new products, avoiding duplicates
                                for (CartProduct newProduct : products) {
                                    boolean exists = false;
                                    for (int i = 0; i < existingProducts.size(); i++) {
                                        if (existingProducts.get(i).getProduct_id().equals(newProduct.getProduct_id())) {
                                            // Replace with newer version
                                            existingProducts.set(i, newProduct);
                                            exists = true;
                                            break;
                                        }
                                    }
                                    
                                    if (!exists) {
                                        existingProducts.add(newProduct);
                                    }
                                }
                                
                                cart.setProducts(existingProducts);
                            } else {
                                // Create new cart
                                docId = db.collection("cart_data").document().getId();
                                cart = new CartModel();
                                cart.setCustomer_id(customerId);
                                cart.setCustomer_name(getCustomerName());
                                cart.setCustomer_phone(getCustomerPhone());
                                cart.setPlatform("Android");
                                cart.setProducts(products);
                            }
                            
                            // Update cart totals
                            updateCartTotals(cart);
                            
                            // Save to Firestore
                            String finalDocId = docId;
                            db.collection("cart_data")
                                    .document(docId)
                                    .set(cart)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Cart migrated to Firestore successfully: " + finalDocId);
                                        // Clear preferences cart after successful migration
                                        pref.edit().remove("cart_items").apply();
                                        setCartValue();
                                    })
                                    .addOnFailureListener(e -> 
                                        Log.e(TAG, "Failed to migrate cart to Firestore", e));
                        }
                    });

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing cart items from preferences", e);
        }
    }

    private void updateCartTotals(CartModel cart) {
        // Calculate total price
        int totalPrice = 0;
        for (CartProduct p : cart.getProducts()) {
            totalPrice += p.getPrice() * p.getQuantity();
        }
        cart.setTotal_price(String.valueOf(totalPrice));

        // Update timestamps
        SimpleDateFormat updateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US);
        cart.setUpdate_date(updateFormat.format(new Date()));
        cart.setUpdate_timestamp(Timestamp.now());
    }

    public void getCartItems(CartItemsCallback callback) {
        if (isLoggedIn()) {
            // Fetch from Firestore
            String customerId = getCustomerId();
            if (customerId == null) {
                callback.onCartItemsLoaded(new ArrayList<>());
                return;
            }

            db.collection("cart_data")
                    .whereEqualTo("customer_id", customerId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            CartModel cart = document.toObject(CartModel.class);
                            if (cart != null && cart.getProducts() != null) {
                                Log.d(TAG, "Firestore cart loaded: " + cart.getProducts().size() + " items");
                                callback.onCartItemsLoaded(cart.getProducts());
                            } else {
                                callback.onCartItemsLoaded(new ArrayList<>());
                            }
                        } else {
                            callback.onCartItemsLoaded(new ArrayList<>());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading cart from Firestore", e);
                        callback.onCartItemsLoaded(new ArrayList<>());
                    });
        } else {
            // Fetch from SharedPreferences (existing implementation)
            SharedPreferences pref = getSharedPreference();
            List<CartProduct> cartProducts = new ArrayList<>();

            if (pref != null) {
                try {
                    JSONArray items = new JSONArray(pref.getString("cart_items", "[]"));
                    for (int i = 0; i < items.length(); i++) {
                        // Convert JSONObject to CartProduct
                        JSONObject item = items.getJSONObject(i);
                        CartProduct product = convertJSONToCartProduct(item);
                        cartProducts.add(product);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing cart items from preferences", e);
                }
            }
            callback.onCartItemsLoaded(cartProducts);
        }
    }

    private CartProduct convertJSONToCartProduct(JSONObject json) throws JSONException {
        CartProduct product = new CartProduct();
        
        // Set basic product details
        product.setProduct_name(json.optString("product_name"));
        product.setProductName(json.optString("product_name"));
        product.setProduct(json.optString("product_name"));
        product.setProduct_id(json.optString("subscription_id"));
        product.setPackage_unit(json.optString("package_unit"));
        product.setPackaging("1");
        
        // Set numeric values
        product.setPrice(json.optLong("price"));
        product.setQuantity(json.optLong("quantity"));
        product.setInterval(json.optLong("interval"));
        
        // Set dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000ZZ", Locale.US);
        product.setStartDate(dateFormat.format(new Date(json.optLong("start_date"))));
        product.setEndDate(dateFormat.format(new Date(json.optLong("end_date"))));
        product.setNext_delivery_date(json.optString("next_delivery_date"));
        
        // Set other fields
        product.setSubscriptionType(json.optString("subscription_type"));
        product.setReason(json.optString("reason", ""));
        
        // Set weekly schedule
        product.setMonday(json.optLong("monday"));
        product.setTuesday(json.optLong("tuesday"));
        product.setWednesday(json.optLong("wednesday"));
        product.setThursday(json.optLong("thursday"));
        product.setFriday(json.optLong("friday"));
        product.setSaturday(json.optLong("saturday"));
        product.setSunday(json.optLong("sunday"));
        
        return product;
    }

    // Callback interface for cart items
    public interface CartItemsCallback {
        void onCartItemsLoaded(List<CartProduct> cartProducts);
    }
}