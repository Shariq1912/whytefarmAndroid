package com.whytefarms.utils;

public class AppConstants {

    public static final String APP_ENVIRONMENT = "_prod_";
    public static final String txtLocalURL = "https://api.textlocal.in/send/";
    public static final String DEFAULT_USER_PROFILE_IMAGE = "https://t4.ftcdn.net/jpg/02/29/75/83/360_F_229758328_7x8jwCwjtBMmC6rgFzLFhZoEpLobB6L8.jpg";

    /* DB COLLECTIONS AND DOCUMENTS PATHS */
    public static final String CUSTOMER_DATA_COLLECTION = "customers_data";
    public static final String CUSTOMER_VACATION_COLLECTION = "customers_vacation";
    public static final String SUBSCRIPTION_DATA_COLLECTION = "subscriptions_data";
    public static final String HUBS_DATA_COLLECTION = "hubs_data";
    public static final String HUBS_LOCATIONS_COLLECTION = "hubs_locations_data";
    public static final String HUBS_USERS_COLLECTION = "hubs_users_data";
    public static final String BULK_QUANTITY_UPDATE_COLLECTION = "bulk_update_quantity";
    public static final String ORDER_HISTORY_COLLECTION = "order_history";
    public static final String SETTINGS_COLLECTION = "settings";
    public static final String APP_OPS_DOCUMENT = "app_ops";
    public static final String TEXT_DOCUMENT = "text";
    public static final String PRODUCTS_DATA_COLLECTION = "products_data";
    public static final String WALLET_HISTORY_COLLECTION = "wallet_history";
    public static final String DELIVERY_PREFERENCE_COLLECTION = "delivery_preference";

    public static final String BANNERS_COLLECTION = "banners";


    /*Firebase Result Types */
    public static final String RESULT_TYPE_CUSTOMER_DATA_FETCH = "Customer Data Fetch";
    public static final String RESULT_TYPE_SUBSCRIPTION_FETCH_FOR_ORDERS = "Subscription Fetch";
    public static final String RESULT_TYPE_VACATIONS_FETCH = "Vacations Fetch";

    /*Actions Based on Result */
    public static final String ACTION_VALIDATE_LOGIN = "Validate Login";
    public static final String ACTION_INVALIDATE_LOGIN = "Invalidate Login";
    public static final String ACTION_NO_VACATIONS = "No Vacations Found";
    public static final String ACTION_CREATE_ORDER = "Create Order";
    public static final String ACTION_CHECK_BULK_UPDATE_QUANTITY = "Check Bulk Quantity";


    public static final String DELIVERY_PREFERENCE_RING_DOOR_BELL = "Ring Door Bell";
    public static final String DELIVERY_PREFERENCE_DROP_AT_THE_DOOR = "Drop at the Door";
    public static final String DELIVERY_PREFERENCE_IN_HAND_DELIVERY = "In Hand Delivery";
    public static final String DELIVERY_PREFERENCE_KEEP_IN_BAG = "Keep in Bag";
    public static final String DELIVERY_PREFERENCE_NO_PREFERENCE = "No Preference";


    public static final String SUBSCRIPTION_TYPE_EVERYDAY = "Everyday";
    public static final String SUBSCRIPTION_TYPE_ON_INTERVAL = "On-Interval";
    public static final String SUBSCRIPTION_TYPE_CUSTOMIZE = "Custom";
    public static final String SUBSCRIPTION_TYPE_ONE_TIME = "One Time";

    public static final String ADDRESS_TYPE_HOME = "HOME";
    public static final String ADDRESS_TYPE_WORK = "WORK";
    public static final long EPOCH_TO_END_DATE = 32503680000000L;


    /*WEEKDAYS */
    public static final String WEEKDAY_SUNDAY = "Sunday";
    public static final String WEEKDAY_MONDAY = "Monday";
    public static final String WEEKDAY_TUESDAY = "Tuesday";
    public static final String WEEKDAY_WEDNESDAY = "Wednesday";
    public static final String WEEKDAY_THURSDAY = "Thursday";
    public static final String WEEKDAY_FRIDAY = "Friday";
    public static final String WEEKDAY_SATURDAY = "Saturday";



    /* API URL */
    private static final String API_BASE_URL = "http://18.191.16.122/";
    public static final String GENERATE_STATIC_HASH_URL = API_BASE_URL + "generate-static-hash.php";
    public static final String GENERATE_DYNAMIC_HASH_URL = API_BASE_URL + "generate-dynamic-hash.php";
    public static final String PAYMENT_SUCCESS_URL = API_BASE_URL + "payment-success.php";
    public static final String PAYMENT_FAILURE_URL = API_BASE_URL + "payment-failure.php";
    
}


