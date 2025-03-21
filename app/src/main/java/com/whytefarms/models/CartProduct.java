package com.whytefarms.models;

public class CartProduct {
    private String Timestamp;  // Capital T as in Firestore
    private String endDate;
    private String startDate;
    private String next_delivery_date;
    private String package_unit;
    private String packaging;
    private String product;
    private String productName;
    private String product_id;
    private String product_name;
    private String reason;
    private String subscriptionType;
    private String image;
    
    // All numeric fields as Long to match Firestore Number type
    private Long friday;
    private Long interval;
    private Long monday;
    private Long price;
    private Long quantity;
    private Long saturday;
    private Long sunday;
    private Long thursday;
    private Long tuesday;
    private Long wednesday;

    public CartProduct() {
        // Empty constructor for Firestore
    }

    // Getters and Setters
    public String getTimestamp() { return Timestamp; }
    public void setTimestamp(String timestamp) { this.Timestamp = timestamp; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getNext_delivery_date() { return next_delivery_date; }
    public void setNext_delivery_date(String next_delivery_date) { this.next_delivery_date = next_delivery_date; }

    public String getPackage_unit() { return package_unit; }
    public void setPackage_unit(String package_unit) { this.package_unit = package_unit; }

    public String getPackaging() { return packaging; }
    public void setPackaging(String packaging) { this.packaging = packaging; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProduct_id() { return product_id; }
    public void setProduct_id(String product_id) { this.product_id = product_id; }

    public String getProduct_name() { return product_name; }
    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    // Numeric field getters and setters
    public Long getFriday() { return friday != null ? friday : 0L; }
    public void setFriday(Long friday) { this.friday = friday; }

    public Long getInterval() { return interval != null ? interval : 0L; }
    public void setInterval(Long interval) { this.interval = interval; }

    public Long getMonday() { return monday != null ? monday : 0L; }
    public void setMonday(Long monday) { this.monday = monday; }

    public Long getPrice() { return price != null ? price : 0L; }
    public void setPrice(Long price) { this.price = price; }

    public Long getQuantity() { return quantity != null ? quantity : 0L; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }

    public Long getSaturday() { return saturday != null ? saturday : 0L; }
    public void setSaturday(Long saturday) { this.saturday = saturday; }

    public Long getSunday() { return sunday != null ? sunday : 0L; }
    public void setSunday(Long sunday) { this.sunday = sunday; }

    public Long getThursday() { return thursday != null ? thursday : 0L; }
    public void setThursday(Long thursday) { this.thursday = thursday; }

    public Long getTuesday() { return tuesday != null ? tuesday : 0L; }
    public void setTuesday(Long tuesday) { this.tuesday = tuesday; }

    public Long getWednesday() { return wednesday != null ? wednesday : 0L; }
    public void setWednesday(Long wednesday) { this.wednesday = wednesday; }
} 