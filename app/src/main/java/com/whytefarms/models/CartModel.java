package com.whytefarms.models;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CartModel {
    private String customer_id;
    private String customer_name;
    private String customer_phone;
    private String platform;
    private List<CartProduct> products;
    private String total_price;
    private String update_date;
    private Timestamp update_timestamp;

    public CartModel() {
        this.products = new ArrayList<>();
    }

    // Getters and Setters
    public String getCustomer_id() { return customer_id; }
    public void setCustomer_id(String customer_id) { this.customer_id = customer_id; }

    public String getCustomer_name() { return customer_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }

    public String getCustomer_phone() { return customer_phone; }
    public void setCustomer_phone(String customer_phone) { this.customer_phone = customer_phone; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public List<CartProduct> getProducts() { return products; }
    public void setProducts(List<CartProduct> products) { this.products = products; }

    public String getTotal_price() { return total_price; }
    public void setTotal_price(String total_price) { this.total_price = total_price; }

    public String getUpdate_date() { return update_date; }
    public void setUpdate_date(String update_date) { this.update_date = update_date; }

    public Timestamp getUpdate_timestamp() { return update_timestamp; }
    public void setUpdate_timestamp(Timestamp update_timestamp) { this.update_timestamp = update_timestamp; }

    public void addProduct(CartProduct product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
    }

    public void updateProduct(CartProduct product) {
        // Implementation of updateProduct method
    }

    public void removeProduct(CartProduct product) {
        // Implementation of removeProduct method
    }

    public void saveToFirestore() {
        // Implementation of saveToFirestore method
    }

    public void loadFromFirestore() {
        // Implementation of loadFromFirestore method
    }

    public void handleDateConversion(CartProduct product) {
        if (product.getEndDate() != null) {
            if (product.getEndDate().contains("T")) {
                String[] parts = product.getEndDate().split("T");
                String datePart = parts[0];
                String timePart = parts[1];
                String[] dateParts = datePart.split("-");
                String[] timeParts = timePart.split(":");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = Integer.parseInt(timeParts[2].substring(0, 2));
                Date date = new Date(year - 1900, month - 1, day, hour, minute, second);
                product.setEndDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date));
            }
        }
        if (product.getStartDate() != null) {
            if (product.getStartDate().contains("T")) {
                String[] parts = product.getStartDate().split("T");
                String datePart = parts[0];
                String timePart = parts[1];
                String[] dateParts = datePart.split("-");
                String[] timeParts = timePart.split(":");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = Integer.parseInt(timeParts[2].substring(0, 2));
                Date date = new Date(year - 1900, month - 1, day, hour, minute, second);
                product.setStartDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date));
            }
        }
    }
} 