package com.whytefarms.activities;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.whytefarms.R;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.Hub;
import com.whytefarms.firebaseModels.Location;
import com.whytefarms.firebaseModels.CustomerActivity;
import com.whytefarms.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddressActivity extends BaseActivity implements TextWatcher {

    double lat = 0, lon = 0;
    private TextInputLayout flatNumber, floor, landmark, city, state;
    private RadioButton isHome, isWork;
    private View progressBar;
    private FirebaseFirestore database;
    private FusedLocationProviderClient fusedLocationClient;
    private AppCompatTextView latitude, longitude;

    private RadioGroup addressType;
    private AppCompatSpinner hubSpinner;

    private FrameLayout hubLayout, locationLayout;

    private LinearLayout geolocation;
    private List<Hub> hubList;
    private List<Location> locationList;
    private ArrayAdapter<String> hubAdapter;
    private ArrayAdapter<String> locationAdapter;

    private String selectedHubName = "";
    private String selectedLocationName = "";

    private Customer customer;

    private AutoCompleteTextView locationAutoComplete;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

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
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        AppCompatImageButton geoLocate = findViewById(R.id.geo_locate);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);

        geoLocate.setOnClickListener(view -> getCurrentLocation());

        AppCompatTextView saveAddressButton = findViewById(R.id.btn_save_address);

        saveAddressButton.setOnClickListener(view -> saveAddress());

        database = FirebaseFirestore.getInstance();


        flatNumber = findViewById(R.id.flat_number);
        floor = findViewById(R.id.floor);
        city = findViewById(R.id.city);
        landmark = findViewById(R.id.landmark);
        state = findViewById(R.id.state);
        isHome = findViewById(R.id.is_home);
        isWork = findViewById(R.id.is_work);
        progressBar = findViewById(R.id.progress_bar);

        addressType = findViewById(R.id.address_type);

        geolocation = findViewById(R.id.geolocation);

        latitude.setText(String.format(getString(R.string.latitude_placeholder), lat));
        longitude.setText(String.format(getString(R.string.longitude_placeholder), lon));

        hubSpinner = findViewById(R.id.hub_spinner);
        // locationSpinner = findViewById(R.id.location_spinner);
        locationAutoComplete = findViewById(R.id.location_auto_complete);

        hubLayout = findViewById(R.id.hub_layout);
        locationLayout = findViewById(R.id.location_layout);
        if (getIntent().getExtras() != null &&
                getIntent().getExtras().getBoolean("isEditing")) {
            getAddress();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.edit_address);
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.add_a_new_address);
            }
            getHubAndLocations("", "", false);
        }


        if (flatNumber.getEditText() != null) {
            flatNumber.getEditText().addTextChangedListener(this);
        }

        if (floor.getEditText() != null) {
            floor.getEditText().addTextChangedListener(this);
        }

        if (city.getEditText() != null) {
            city.getEditText().addTextChangedListener(this);
        }

        if (landmark.getEditText() != null) {
            landmark.getEditText().addTextChangedListener(this);
        }


        if (state.getEditText() != null) {
            state.getEditText().addTextChangedListener(this);
        }


    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }


    private void saveAddress() {
        toggleProgressBar(true);
        if (isValidAddress()) {
            saveCustomerToDB();
        } else {
            Toast.makeText(this, getString(R.string.please_check_the_entered_details), Toast.LENGTH_SHORT).show();
            toggleProgressBar(false);
        }
    }

    private boolean isEverythingSame() {
        int pos = 0;

        for (Location location : locationList) {
            if (location.location.equalsIgnoreCase(selectedLocationName)) {
                pos = locationList.indexOf(location);
                break;
            }
        }
        return ((customer != null && customer.addressType.equals(AppConstants.ADDRESS_TYPE_HOME) && isHome.isChecked()) ||
                (customer != null && customer.addressType.equals(AppConstants.ADDRESS_TYPE_WORK) && isWork.isChecked())) &&
                customer.customer_address.equals(String.format(getString(R.string.address_placeholder),
                        Objects.requireNonNull(flatNumber.getEditText()).getText().toString(),
                        Objects.requireNonNull(floor.getEditText()).getText().toString(),
                        Objects.requireNonNull(landmark.getEditText()).getText().toString(),
                        selectedLocationName,
                        Objects.requireNonNull(city.getEditText()).getText().toString(),
                        Objects.requireNonNull(state.getEditText()).getText().toString())) &&
                customer.delivery_exe_id.equals(locationList.get(pos).delivery_exe_id) &&
                customer.flat_villa_no.equals(Objects.requireNonNull(flatNumber.getEditText()).getText().toString()) &&
                customer.floor.equals(Objects.requireNonNull(floor.getEditText()).getText().toString()) &&
                customer.hub_name.equals(hubList.get(hubSpinner.getSelectedItemPosition()).hub_name) &&
                customer.landmark.equals(Objects.requireNonNull(landmark.getEditText()).getText().toString()) &&
                customer.latitude.equals("" + lat) && customer.longitude.equals("" + lon);
    }

    private void saveCustomerToDB() {
        if (((WhyteFarmsApplication) getApplication()).isLoggedIn()) {
            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                    .whereEqualTo("customer_id", ((WhyteFarmsApplication) getApplication()).getCustomerIDFromLoginState())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Customer existingCustomer = queryDocumentSnapshots.getDocuments().get(0).toObject(Customer.class);

                            if (existingCustomer != null) {
                                if (!isEverythingSame()) {

                                    String oldAddress = existingCustomer.customer_address;
                                    String oldLocation = existingCustomer.location;
                                    String oldLandmark = existingCustomer.landmark;
                                    String oldFlatVillaNo = existingCustomer.flat_villa_no;
                                    String oldHubName = existingCustomer.hub_name;


                                    existingCustomer.addressType = isHome.isChecked() ? AppConstants.ADDRESS_TYPE_HOME : AppConstants.ADDRESS_TYPE_WORK;
                                    existingCustomer.customer_address = String.format(getString(R.string.address_placeholder),
                                            Objects.requireNonNull(flatNumber.getEditText()).getText().toString(),
                                            Objects.requireNonNull(floor.getEditText()).getText().toString(),
                                            Objects.requireNonNull(landmark.getEditText()).getText().toString(),
                                            selectedLocationName,
                                            Objects.requireNonNull(city.getEditText()).getText().toString(),
                                            Objects.requireNonNull(state.getEditText()).getText().toString());

                                    int pos = 0;

                                    for (Location location : locationList) {
                                        if (location.location.equalsIgnoreCase(selectedLocationName)) {
                                            pos = locationList.indexOf(location);
                                            break;
                                        }
                                    }

                                    existingCustomer.delivery_exe_id = locationList.get(pos).delivery_exe_id;
                                    existingCustomer.flat_villa_no = Objects.requireNonNull(flatNumber.getEditText()).getText().toString();
                                    existingCustomer.floor = Objects.requireNonNull(floor.getEditText()).getText().toString();
                                    existingCustomer.hub_name = hubList.get(hubSpinner.getSelectedItemPosition()).hub_name;
                                    existingCustomer.landmark = Objects.requireNonNull(landmark.getEditText()).getText().toString();
                                    existingCustomer.latitude = "" + lat;
                                    existingCustomer.longitude = "" + lon;
                                    existingCustomer.location = locationList.get(pos).location;
                                    existingCustomer.updated_date = Timestamp.now();

                                    queryDocumentSnapshots.getDocuments().get(0).getReference()
                                            .set(existingCustomer)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    logCustomerAddressChange(existingCustomer);
                                                    Toast.makeText(AddressActivity.this, getString(R.string.saved_successfully), Toast.LENGTH_SHORT)
                                                            .show();
                                                    toggleProgressBar(false);
                                                } else {
                                                    Toast.makeText(AddressActivity.this, getString(R.string.not_saved), Toast.LENGTH_SHORT)
                                                            .show();
                                                    toggleProgressBar(false);
                                                }
                                            });
                                } else {
                                    Toast.makeText(AddressActivity.this, getString(R.string.no_change), Toast.LENGTH_SHORT)
                                            .show();
                                    toggleProgressBar(false);
                                }
                            } else {
                                toggleProgressBar(false);
                            }
                        } else {
                            toggleProgressBar(false);
                        }
                    });
        } else {
            toggleProgressBar(false);
            startLoginFlow("LoginActivity");

        }
    }

    private void logCustomerAddressChange(Customer customer) {
        CustomerActivity customerActivity = new CustomerActivity();
        customerActivity.action = "Address Changed";
        customerActivity.created_date = Timestamp.now();
        customerActivity.customer_id = customer.customer_id;
        customerActivity.customer_name = customer.customer_name;
        customerActivity.customer_phone = customer.customer_phone;
        customerActivity.description = "Address changed by " + customer.customer_name;
        customerActivity.object = "Customer Address";
        customerActivity.user = customer.customer_name;
        customerActivity.platform = "Android";

        database.collection("customer_activities")
                .add(customerActivity);
    }

    private void getHubAndLocations(String hubName, String locationName, boolean shouldSet) {

        hubList = new ArrayList<>();

        locationList = new ArrayList<>();

        hubAdapter = new ArrayAdapter<>(this, R.layout.item_hub, R.id.name);

        locationAdapter = new ArrayAdapter<>(this, R.layout.item_location, R.id.name);
        hubSpinner.setAdapter(hubAdapter);
        hubAdapter.setNotifyOnChange(true);

        // locationSpinner.setAdapter(locationAdapter);
        locationAdapter.setNotifyOnChange(true);

        View hubProgressBar = findViewById(R.id.hub_spinner_progress);
        View locationProgressBar = findViewById(R.id.location_spinner_progress);

        hubProgressBar.setVisibility(View.VISIBLE);
        hubSpinner.setEnabled(false);
        //locationSpinner.setEnabled(false);
        hubAdapter.clear();

        database.collection(AppConstants.HUBS_DATA_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Hub hub = snapshot.toObject(Hub.class);

                                if (hub.status.equals("1")) {
                                    hubList.add(hub);
                                    hubAdapter.add(hub.hub_name);
                                }
                            }


                            if (!hubList.isEmpty()) {
                                hubAdapter.notifyDataSetChanged();
                                if (city.getEditText() != null) {
                                    city.getEditText().setText(hubList.get(0).city);
                                }

                                if (state.getEditText() != null) {
                                    state.getEditText().setText(hubList.get(0).state);
                                }

                                if (shouldSet) {
                                    int position = 0;

                                    for (Hub hub : hubList) {
                                        if (hub.hub_name.equals(hubName)) {
                                            position = hubList.indexOf(hub);
                                            break;
                                        }
                                    }
                                    hubSpinner.setSelection(position, true);
                                }

                                hubLayout.setBackgroundTintList(null);

                            }


                        }
                    }
                    hubProgressBar.setVisibility(GONE);
                    hubSpinner.setEnabled(true);
                });

        hubSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                selectedHubName = (String) adapterView.getAdapter().getItem(position);
                if (city.getEditText() != null) {
                    city.getEditText().setText(hubList.get(position).city);
                }

                if (state.getEditText() != null) {
                    state.getEditText().setText(hubList.get(position).state);
                }

                locationProgressBar.setVisibility(View.VISIBLE);
                // locationSpinner.setEnabled(false);
                locationList.clear();
                locationAdapter.clear();

                database.collection(AppConstants.HUBS_LOCATIONS_COLLECTION)
                        .whereEqualTo("hub_name", hubAdapter.getItem(position))
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        Location location = snapshot.toObject(Location.class);

                                        if (location.status.equals("1")) {
                                            locationList.add(location);
                                            locationAdapter.add(location.location);
                                        }
                                    }

                                    if (!locationList.isEmpty()) {
                                        locationAdapter.notifyDataSetChanged();
                                        if (shouldSet) {
                                            int pos = 0;

                                            for (Location location : locationList) {
                                                if (location.location.equals(locationName)) {
                                                    pos = locationList.indexOf(location);
                                                    break;
                                                }
                                            }
                                            //  locationSpinner.setSelection(pos, true);

                                            locationLayout.setBackgroundTintList(null);


                                            if (!locationList.isEmpty()) {
                                                locationAdapter.notifyDataSetChanged();

                                                locationAutoComplete.setAdapter(locationAdapter);

                                                locationAutoComplete.addTextChangedListener(new TextWatcher() {
                                                    @Override
                                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                    }

                                                    @Override
                                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                        selectedLocationName = s.toString();
                                                    }

                                                    @Override
                                                    public void afterTextChanged(Editable s) {

                                                    }
                                                });
                                            }
                                            locationAutoComplete.setText(locationList.get(pos).location);

                                        }
                                    }
                                }
                            }

                            locationProgressBar.setVisibility(GONE);
                            // locationSpinner.setEnabled(true);
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedHubName = "";
            }
        });

    }

    private boolean isValidAddress() {
        boolean isValid = true;

        if (selectedHubName.isEmpty()) {
            hubLayout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.inactive_track));
            isValid = false;
        } else {
            hubLayout.setBackgroundTintList(null);
        }

        if (selectedLocationName.isEmpty()) {
            locationLayout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.inactive_track));
            isValid = false;
        } else {
            locationLayout.setBackgroundTintList(null);
        }

        int pos = -1;

        for (Location location : locationList) {
            if (location.location.equalsIgnoreCase(locationAutoComplete.getText().toString())) {
                pos = locationList.indexOf(location);
                break;
            }
        }

        if (pos > -1) {
            locationLayout.setBackgroundTintList(null);
        } else {
            locationLayout.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.inactive_track));
            isValid = false;
        }

        if (flatNumber.getEditText() == null) {
            flatNumber.setError(getString(R.string.flat_number_cannot_be_empty));
            isValid = false;
        }

        if (flatNumber.getEditText() != null && flatNumber.getEditText().getText().toString().isEmpty()) {
            flatNumber.setError(getString(R.string.flat_number_cannot_be_empty));
            isValid = false;
        }

        if (floor.getEditText() == null) {
            floor.setError(getString(R.string.floor_cannot_be_empty));
            isValid = false;
        }

        if (floor.getEditText() != null && floor.getEditText().getText().toString().isEmpty()) {
            floor.setError(getString(R.string.floor_cannot_be_empty));
            isValid = false;
        }

        /*if (landmark.getEditText() == null) {
            landmark.setError(getString(R.string.landmark_cannot_be_empty));
            isValid = false;
        }

        if (landmark.getEditText() != null && landmark.getEditText().getText().toString().isEmpty()) {
            landmark.setError(getString(R.string.landmark_cannot_be_empty));
            isValid = false;
        }*/

        if (city.getEditText() == null) {
            city.setError(getString(R.string.city_cannot_be_empty));
            isValid = false;
        }

        if (city.getEditText() != null && city.getEditText().getText().toString().isEmpty()) {
            city.setError(getString(R.string.city_cannot_be_empty));
            isValid = false;
        }

        if (state.getEditText() == null) {
            state.setError(getString(R.string.state_cannot_be_empty));
            isValid = false;
        }

        if (state.getEditText() != null && state.getEditText().getText().toString().isEmpty()) {
            state.setError(getString(R.string.state_cannot_be_empty));
            isValid = false;
        }

        /*if (lat == 0.0 || lon == 0.0) {
            geolocation.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.inactive_track));
            isValid = false;
        }*/

        if (!isHome.isChecked() && !isWork.isChecked()) {
            addressType.setBackgroundColor(ContextCompat.getColor(AddressActivity.this, R.color.inactive_track));
            isValid = false;
        }

        return isValid;
    }

    private void getAddress() {
        toggleProgressBar(true);

        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", ((WhyteFarmsApplication) getApplicationContext()).getCustomerIDFromLoginState())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        customer = queryDocumentSnapshots.getDocuments().get(0).toObject(Customer.class);

                        if (customer != null) {
                            Objects.requireNonNull(flatNumber.getEditText()).setText(customer.flat_villa_no);
                            Objects.requireNonNull(floor.getEditText()).setText(customer.floor);
                            Objects.requireNonNull(landmark.getEditText()).setText(customer.landmark);
                            isHome.setChecked(customer.addressType.equals(AppConstants.ADDRESS_TYPE_HOME));
                            isWork.setChecked(customer.addressType.equals(AppConstants.ADDRESS_TYPE_WORK));
                            getHubAndLocations(customer.hub_name, customer.location, true);
                            lat = customer.latitude != null && !customer.latitude.isEmpty() ? Double.parseDouble(customer.latitude) : 0.0;
                            lon = customer.longitude != null && !customer.longitude.isEmpty() ? Double.parseDouble(customer.longitude) : 0.0;
                            latitude.setText(String.format(getString(R.string.latitude_placeholder), lat));
                            longitude.setText(String.format(getString(R.string.longitude_placeholder), lon));
                            isHome.setChecked(customer.addressType.equals(AppConstants.ADDRESS_TYPE_HOME));

                        }
                    }
                    toggleProgressBar(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddressActivity.this,
                                    getString(R.string.address_edit_failed), Toast.LENGTH_LONG)
                            .show();
                    toggleProgressBar(false);
                });
    }


    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            resultLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                latitude.setText(String.format(getString(R.string.latitude_placeholder), lat));
                longitude.setText(String.format(getString(R.string.longitude_placeholder), lon));


                if (lat != 0.0 && lon != 0.0) {
                    geolocation.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.active_track));
                } else {
                    geolocation.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.inactive_track));
                }
            }
        });
    }

    /*private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();

        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(AddressActivity.this);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }*/

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (flatNumber.getEditText() != null && flatNumber.getEditText().getText().length() > 0) {
            flatNumber.setError(null);
        }

        if (floor.getEditText() != null && floor.getEditText().getText().length() > 0) {
            floor.setError(null);
        }


        /*if (landmark.getEditText() != null && landmark.getEditText().getText().length() > 0) {
            landmark.setError(null);
        }*/


        if (city.getEditText() != null && city.getEditText().getText().length() > 0) {
            city.setError(null);
        }


        if (state.getEditText() != null && state.getEditText().getText().length() > 0) {
            state.setError(null);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    });
}