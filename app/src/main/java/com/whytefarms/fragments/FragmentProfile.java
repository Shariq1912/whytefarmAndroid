package com.whytefarms.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whytefarms.R;
import com.whytefarms.activities.AddressActivity;
import com.whytefarms.activities.VacationListActivity;
import com.whytefarms.application.WhyteFarmsApplication;
import com.whytefarms.firebaseModels.Customer;
import com.whytefarms.firebaseModels.DeliveryPreference;
import com.whytefarms.firebaseModels.FirebaseVacation;
import com.whytefarms.firebaseModels.CustomerActivity;
import com.whytefarms.utils.AppConstants;
import com.whytefarms.utils.DateUtils;
import com.whytefarms.utils.Utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FragmentProfile extends BottomSheetDialogFragment implements CompoundButton.OnCheckedChangeListener {

    AppCompatCheckBox chkDoorBell;
    AppCompatCheckBox chkDropAtDoor;
    AppCompatCheckBox chkInBag;
    AppCompatCheckBox chkInHand;
    AppCompatCheckBox chkNoPref;
    TextInputLayout additionalInstructions;
    private View view;
    private AppCompatTextView fullName;
    private AppCompatTextView address;
    private AppCompatTextView addressType;
    private AppCompatTextView statusText;
    private AppCompatTextView addAddressButton;
    private CardView addressCard;
    private View progressBar;
    private FirebaseFirestore database;
    private DatePickerDialog datePickerDialog;
    private Timestamp startDate;
    private Timestamp endDate;
    private LinearLayout deliveryExecutiveLayout;
    private AppCompatImageView profileImage;
    private AppCompatTextView name;
    private AppCompatTextView hub_name;
    private AppCompatTextView phone;
    private AppCompatButton callButton;

    public FragmentProfile() {
        // Required empty public constructor
    }

    public static FragmentProfile newInstance(String param1, long startDate, long endDate, String id) {
        FragmentProfile fragment = new FragmentProfile();
        Bundle args = new Bundle();
        args.putString("param", param1);
        args.putLong("startDate", startDate);
        args.putLong("endDate", endDate);
        args.putString("vacation_id", id);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        database = FirebaseFirestore.getInstance();

        if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Address")) {
            view = inflater.inflate(R.layout.fragment_address, container, false);
            if (view != null && isAdded()) {
                fullName = view.findViewById(R.id.full_name);
                address = view.findViewById(R.id.address);
                progressBar = view.findViewById(R.id.progress_bar);
                addressCard = view.findViewById(R.id.address_card_view);
                statusText = view.findViewById(R.id.status_text);
                addressType = view.findViewById(R.id.address_type);
                addAddressButton = view.findViewById(R.id.add_address);

                AppCompatTextView btnEdit = view.findViewById(R.id.btn_edit);
                // AppCompatImageButton btnDelete = view.findViewById(R.id.btn_delete);

                btnEdit.setOnClickListener(view -> {
                    Intent intent = new Intent(requireActivity(), AddressActivity.class);
                    intent.putExtra("isEditing", true);
                    startActivity(intent);
                });
                addAddressButton.setOnClickListener(view -> {
                    Intent intent = new Intent(requireActivity(), AddressActivity.class);
                    intent.putExtra("isEditing", false);
                    startActivity(intent);
                });
            }

        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "DeliveryPreferences")) {
            view = inflater.inflate(R.layout.fragment_delivery_pref, container, false);

            if (view != null && isAdded()) {
                chkDoorBell = view.findViewById(R.id.chk_doorbell);
                chkDropAtDoor = view.findViewById(R.id.chk_drop_at_door);
                chkInBag = view.findViewById(R.id.chk_in_bag);
                chkInHand = view.findViewById(R.id.chk_in_hand);
                chkNoPref = view.findViewById(R.id.chk_no_pref);

                progressBar = view.findViewById(R.id.progress_bar);

                deliveryExecutiveLayout = view.findViewById(R.id.delivery_executive_layout);
                profileImage = view.findViewById(R.id.profile_image);
                name = view.findViewById(R.id.name);
                hub_name = view.findViewById(R.id.hub_name);
                phone = view.findViewById(R.id.phone);
                callButton = view.findViewById(R.id.call_button);


                AppCompatTextView commonButton = view.findViewById(R.id.common_button);
                commonButton.setText(R.string.save_delivery_preferences);

                additionalInstructions = view.findViewById(R.id.additional_instructions);

                commonButton.setOnClickListener(view -> saveToDB());


                chkDoorBell.setOnCheckedChangeListener(this);
                chkDropAtDoor.setOnCheckedChangeListener(this);
                chkInBag.setOnCheckedChangeListener(this);
                chkInHand.setOnCheckedChangeListener(this);
                chkNoPref.setOnCheckedChangeListener(this);

            }

        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Vacation")) {
            view = inflater.inflate(R.layout.fragment_vacation, container, false);

            LinearLayout startDateLayout;
            LinearLayout endDateLayout;
            AppCompatTextView startDate;
            AppCompatTextView endDate;
            AppCompatImageView removeEndDateButton;

            if (view != null && isAdded()) {

                AppCompatTextView commonButton = view.findViewById(R.id.common_button);
                commonButton.setText(R.string.add_vacation);

                view.findViewById(R.id.tip_text).setVisibility(View.GONE);

                startDateLayout = view.findViewById(R.id.start_date_layout);
                endDateLayout = view.findViewById(R.id.end_date_layout);

                startDate = view.findViewById(R.id.start_date);
                endDate = view.findViewById(R.id.end_date);
                removeEndDateButton = view.findViewById(R.id.remove_end_date);

                startDateLayout.setOnClickListener(view -> {
                    if (datePickerDialog != null && datePickerDialog.isShowing()) {
                        return;
                    }

                    if (datePickerDialog == null) {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH) + 1;


                        datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, y, m, d) -> {
                            c.set(y, m, d);
                            String selectedDateString = DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", false);


                            try {
                                Date selectedStartDate = DateUtils.getDateFromString(selectedDateString, "EEE MMM dd, yyyy", false);


                                if (selectedStartDate.before(this.startDate.toDate())) {
                                    Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                                } else {
                                    startDate.setText(DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", true));
                                    this.startDate = new Timestamp(c.getTime());
                                }
                            } catch (ParseException e) {
                                Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                            }

                        }, year, month, day);

                        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                        datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                        datePickerDialog.show();
                    }
                });

                endDateLayout.setOnClickListener(view -> {
                    if (datePickerDialog != null && datePickerDialog.isShowing()) {
                        return;
                    }

                    if (datePickerDialog == null) {
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH) + 1;


                        datePickerDialog = new DatePickerDialog(requireContext(), (datePicker, y, m, d) -> {
                            c.set(y, m, d);
                            String selectedDateString = DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", false);


                            try {
                                Date selectedEndDate = DateUtils.getDateFromString(selectedDateString, "EEE MMM dd, yyyy", false);


                                if (selectedEndDate.before(this.startDate.toDate())) {
                                    Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                                } else {
                                    endDate.setText(DateUtils.getStringFromMillis(c.getTime().getTime(), "EEE MMM dd, yyyy", true));
                                    this.endDate = new Timestamp(c.getTime());
                                }
                            } catch (ParseException e) {
                                Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_LONG).show();
                            }

                        }, year, month, day);

                        datePickerDialog.getDatePicker().setMinDate(DateUtils.getTimeAfterAddingDays(new Date().getTime(), 1));
                        datePickerDialog.setOnDismissListener(dialogInterface -> datePickerDialog = null);
                        datePickerDialog.show();
                    }
                });

                if (getArguments() != null) {
                    this.startDate = new Timestamp(new Date(getArguments().getLong("startDate")));
                    this.endDate = new Timestamp(new Date(getArguments().getLong("endDate")));

                    startDate.setText(DateUtils.getStringFromMillis(this.startDate.toDate().getTime(), "EEE MMM dd, yyyy", true));
                    endDate.setText(DateUtils.getStringFromMillis(this.endDate.toDate().getTime(), "EEE MMM dd, yyyy", true));

                    commonButton.setOnClickListener(view -> {
                        if (getArguments() != null) {
                            if (!Objects.equals(getArguments().getString("vacation_id"), "")) {
                                updateVacation(getArguments().getString("vacation_id"));
                            } else {
                                addVacation();
                            }
                        }
                    });

                }


            }
        }

        return view;
    }

    private void updateVacation(String id) {
        if (DateUtils.getDifference(startDate.toDate(), endDate.toDate()).getElapsedDays() < 1) {
            Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_SHORT).show();
            return;
        }

        toggleProgressBar(true);
        String customerID = (((WhyteFarmsApplication) requireContext().getApplicationContext()).getCustomerIDFromLoginState());

        database.collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                .where(Filter.and(Filter.equalTo("customer_id", customerID),
                        Filter.equalTo("vacation_id", id)))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            FirebaseVacation vacation = task.getResult().getDocuments().get(0).toObject(FirebaseVacation.class);

                            if (vacation != null) {
                                vacation.end_date = endDate;
                                vacation.start_date = startDate;
                                vacation.updated_date = Timestamp.now();

                                task.getResult().getDocuments().get(0).getReference()
                                        .set(vacation)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                logVacationActivity(vacation.customer_id, vacation, true);
                                                handleVacationUpdated();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void handleVacationUpdated() {
        if (isAdded()) { // isAdded() added wrf Crash IllegalStateException in v6.0
            Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
            ((VacationListActivity) requireActivity()).updateOnVacationAdded(this);
        }
    }


    private void addVacation() {
        if (DateUtils.getDifference(startDate.toDate(), endDate.toDate()).getElapsedDays() < 1) {
            Toast.makeText(requireContext(), R.string.start_end_date_error, Toast.LENGTH_SHORT).show();
            return;
        }

        toggleProgressBar(true);
        String customerID = (((WhyteFarmsApplication) requireContext().getApplicationContext()).getCustomerIDFromLoginState());

        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", customerID)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                            if (customer != null) {
                                FirebaseVacation vacation = new FirebaseVacation();

                                vacation.vacation_id = Objects.requireNonNull(Utils.getSha256Hash(customerID + "::::" + vacation.start_date + "::::" + vacation.end_date)).substring(0, 6);
                                vacation.created_date = Timestamp.now();
                                vacation.customer_id = customer.customer_id;
                                vacation.customer_name = customer.customer_name;
                                vacation.customer_phone = customer.customer_phone;
                                vacation.end_date = endDate;
                                vacation.hub_name = customer.hub_name;
                                vacation.start_date = startDate;
                                vacation.updated_date = Timestamp.now();

                                database.collection(AppConstants.CUSTOMER_VACATION_COLLECTION)
                                        .add(vacation)
                                        .addOnCompleteListener(task1 -> {

                                            if (task1.isSuccessful()) {
                                                logVacationActivity(customerID, vacation, false);
                                                handleVacationAdded();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void handleVacationAdded() {
        if (isAdded()) { // isAdded() added wrf Crash IllegalStateException in v6.0
            Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
            ((VacationListActivity) requireActivity()).updateOnVacationAdded(this);
        }
    }

    private void logVacationActivity(String customerID, FirebaseVacation vacation, boolean isUpdate) {
    database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
            .whereEqualTo("customer_id", customerID)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                        if (customer != null) {
                            CustomerActivity customerActivity = new CustomerActivity();
                            customerActivity.action = isUpdate ? "Vacation Updated" : "Vacation Added";
                            customerActivity.created_date = Timestamp.now();
                            customerActivity.customer_id = customer.customer_id;
                            customerActivity.customer_name = customer.customer_name;
                            customerActivity.customer_phone = customer.customer_phone;
                            customerActivity.description = (isUpdate ? "Vacation updated from " : "Vacation added from ") 
                                + DateUtils.getStringFromMillis(vacation.start_date.toDate().getTime(), "yyyy-MM-dd", false)
                                + " to " + DateUtils.getStringFromMillis(vacation.end_date.toDate().getTime(), "yyyy-MM-dd", false);
                            customerActivity.object = "Vacation";
                            customerActivity.user = customer.customer_name;
                            customerActivity.platform = "Android";

                            database.collection("customer_activities")
                                    .add(customerActivity);
                        }
                    }
                }
            });
    }



//    private void checkSubscriptionsUpdated() {
//        if (subscriptionsToEdit == ++counter && isAdded()) { //isAdded() added wrf Crash IllegalStateException in v6.0
//            Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
//            ((VacationListActivity) requireActivity()).updateOnVacationAdded(this);
//        }
//    }

    private void saveToDB() {
        toggleProgressBar(true);

        String customerId = ((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState();
        database.collection(AppConstants.DELIVERY_PREFERENCE_COLLECTION)
                .whereEqualTo("customer_id", customerId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded() && task.isSuccessful() && task.getResult() != null) {
                        DeliveryPreference deliveryPreference = new DeliveryPreference();
                        deliveryPreference.created_date = Timestamp.now();
                        deliveryPreference.status = "1";
                        if (!task.getResult().isEmpty()) {
                            deliveryPreference = task.getResult().getDocuments().get(0).toObject(DeliveryPreference.class);
                        }

                        DeliveryPreference finalDeliveryPreference = deliveryPreference;

                        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                            .whereEqualTo("customer_id", customerId)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(task1 -> {
                                if (isAdded() && task1.isSuccessful() && task1.getResult() != null && !task1.getResult().isEmpty()) {
                                    Customer customer = task1.getResult().getDocuments().get(0).toObject(Customer.class);

                                    if (customer != null) {
                                        if (finalDeliveryPreference != null) {
                                            // Set delivery preference based on checked options
                                            finalDeliveryPreference.delivery_mode = chkDoorBell.isChecked() ? AppConstants.DELIVERY_PREFERENCE_RING_DOOR_BELL :
                                                    chkInBag.isChecked() ? AppConstants.DELIVERY_PREFERENCE_KEEP_IN_BAG :
                                                            chkInHand.isChecked() ? AppConstants.DELIVERY_PREFERENCE_IN_HAND_DELIVERY :
                                                                    chkDropAtDoor.isChecked() ? AppConstants.DELIVERY_PREFERENCE_DROP_AT_THE_DOOR :
                                                                            AppConstants.DELIVERY_PREFERENCE_NO_PREFERENCE;

                                            // Set additional instruction
                                            if (additionalInstructions != null && additionalInstructions.getEditText() != null) {
                                                finalDeliveryPreference.additional_instruction = String.valueOf(additionalInstructions.getEditText().getText());
                                            } else {
                                                finalDeliveryPreference.additional_instruction = "";
                                            }

                                            // Set customer details
                                            finalDeliveryPreference.customer_name = customer.customer_name;
                                            finalDeliveryPreference.customer_id = customer.customer_id;
                                            finalDeliveryPreference.customer_phone = customer.customer_phone;
                                            finalDeliveryPreference.updated_date = Timestamp.now();

                                            // Save or update delivery preference
                                            if (!task.getResult().getDocuments().isEmpty()) {
                                                task.getResult().getDocuments().get(0).getReference()
                                                        .set(finalDeliveryPreference)
                                                        .addOnCompleteListener(task2 -> {
                                                            if (isAdded() && task2.isSuccessful()) {
                                                                logCustomerActivity(customer, "Delivery preference changed");
                                                                Toast.makeText(requireActivity(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(requireActivity(), getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
                                                            }
                                                            toggleProgressBar(false);
                                                        });
                                            } else {
                                                database.collection(AppConstants.DELIVERY_PREFERENCE_COLLECTION).add(finalDeliveryPreference)
                                                        .addOnCompleteListener(task2 -> {
                                                            if (isAdded() && task2.isSuccessful()) {
                                                                logCustomerActivity(customer, "Delivery preference changed");
                                                                Toast.makeText(requireActivity(), R.string.saved_successfully, Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(requireActivity(), getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
                                                            }
                                                            toggleProgressBar(false);
                                                        });
                                            }
                                        }
                                    } else {
                                        if (isAdded()) {
                                            Toast.makeText(requireActivity(), getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
                                            toggleProgressBar(false);
                                        }
                                    }
                                } else {
                                    if (isAdded()) {
                                        Toast.makeText(requireActivity(), getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
                                        toggleProgressBar(false);
                                    }
                                }

                                if (isAdded()) {
                                    toggleProgressBar(false);
                                }
                            });
                } else {
                    if (isAdded()) {
                        Toast.makeText(requireActivity(), getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
                        toggleProgressBar(false);
                    }
                }

                if (isAdded()) {
                    toggleProgressBar(false);
                }
            });
    }

    private void logCustomerActivity(Customer customer, String action) {
        CustomerActivity customerActivity = new CustomerActivity();
        customerActivity.action = action;
        customerActivity.created_date = Timestamp.now();
        customerActivity.customer_id = customer.customer_id;
        customerActivity.customer_name = customer.customer_name;
        customerActivity.customer_phone = customer.customer_phone;
        customerActivity.description = String.format("%s by %s", action, customer.customer_name);
        customerActivity.object = "Delivery Preference";
        customerActivity.user = customer.customer_name;
        customerActivity.platform = "Android";

        database.collection("customer_activities")
                .add(customerActivity);
    }

    private void getFromDB() {
        toggleProgressBar(true);
        String customerID = (((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState());
        database.collection(AppConstants.DELIVERY_PREFERENCE_COLLECTION)
                .whereEqualTo("customer_id", customerID)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded() && task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            DeliveryPreference deliveryPreference = task.getResult().getDocuments().get(0).toObject(DeliveryPreference.class);

                            if (deliveryPreference != null) {
                                selectDeliveryMode(deliveryPreference.delivery_mode);

                                if (additionalInstructions != null && additionalInstructions.getEditText() != null) {
                                    additionalInstructions.getEditText().setText(deliveryPreference.additional_instruction);
                                }
                            }
                        }
                    }
                });

        database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                .whereEqualTo("customer_id", customerID)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded() && task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);

                            if (customer != null) {
                                String deliveryExecID = customer.delivery_exe_id;

                                database.collection(AppConstants.HUBS_USERS_COLLECTION)
                                        .where(Filter.and(
                                                Filter.equalTo("hub_user_id", deliveryExecID),
                                                Filter.equalTo("role", "Delivery Executive")
                                        ))
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (isAdded() && task1.isSuccessful()) {
                                                if (task1.getResult() != null && !task1.getResult().isEmpty()) {
                                                    DocumentSnapshot document = task1.getResult().getDocuments().get(0);

                                                    if (document != null) {

                                                        deliveryExecutiveLayout.setVisibility(View.VISIBLE);
                                                        Glide.with(profileImage.getContext())
                                                                .load(document.getString("image"))
                                                                .skipMemoryCache(true)
                                                                .centerCrop()
                                                                .dontAnimate()
                                                                .dontTransform()
                                                                .priority(Priority.IMMEDIATE)
                                                                .encodeFormat(Bitmap.CompressFormat.PNG)
                                                                .format(DecodeFormat.DEFAULT)
                                                                .into(profileImage);


                                                        name.setText(String.format(Locale.getDefault(), "%s %s",
                                                                document.getString("first_name"),
                                                                document.getString("last_name")
                                                        ));
                                                        hub_name.setText(String.format(Locale.getDefault(), "Hub Name: %s",
                                                                document.getString("hub_name")
                                                        ));
                                                        phone.setText(String.format(Locale.getDefault(), "Phone: %s",
                                                                document.getString("phone_no")
                                                        ));

                                                        callButton.setOnClickListener(v -> {
                                                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                                            callIntent.setData(Uri.parse("tel:" + document.getString("phone_no")));
                                                            startActivity(callIntent);
                                                        });
                                                    } else {
                                                        deliveryExecutiveLayout.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    deliveryExecutiveLayout.setVisibility(View.GONE);
                                                }
                                            }

                                            toggleProgressBar(false);
                                        });
                            }
                        }
                    }
                });

    }

    private void selectDeliveryMode(String deliveryMode) {
        chkDoorBell.setChecked(false);
        chkDropAtDoor.setChecked(false);
        chkInBag.setChecked(false);
        chkInHand.setChecked(false);
        chkNoPref.setChecked(false);

        switch (deliveryMode) {
            default:
            case AppConstants.DELIVERY_PREFERENCE_RING_DOOR_BELL:
                chkDoorBell.setChecked(true);
                break;

            case AppConstants.DELIVERY_PREFERENCE_DROP_AT_THE_DOOR:
                chkDropAtDoor.setChecked(true);
                break;


            case AppConstants.DELIVERY_PREFERENCE_IN_HAND_DELIVERY:
                chkInHand.setChecked(true);
                break;


            case AppConstants.DELIVERY_PREFERENCE_KEEP_IN_BAG:
                chkInBag.setChecked(true);
                break;


            case AppConstants.DELIVERY_PREFERENCE_NO_PREFERENCE:
                chkNoPref.setChecked(true);
                break;
        }
    }

    private void toggleProgressBar(boolean shouldShow) {
        if (progressBar != null) {
            progressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null && Objects.equals(getArguments().getString("param"), "Address")) {

            toggleProgressBar(true);
            addressCard.setVisibility(View.GONE);
            statusText.setText(R.string.loading);
            addAddressButton.setVisibility(View.GONE);
            database.collection(AppConstants.CUSTOMER_DATA_COLLECTION)
                    .whereEqualTo("customer_id", (((WhyteFarmsApplication) requireActivity().getApplicationContext()).getCustomerIDFromLoginState()))
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (isAdded() && task.isSuccessful()) {
                            if (task.getResult() != null) {
                                Customer customer = task.getResult().getDocuments().get(0).toObject(Customer.class);
                                if (customer != null) {
                                    if (!customer.customer_address.isEmpty()) {
                                        fullName.setText(customer.customer_name);
                                        address.setText(customer.customer_address);
                                        addressType.setText(customer.addressType);
                                        statusText.setVisibility(View.GONE);
                                        addressCard.setVisibility(View.VISIBLE);
                                    } else {
                                        addressCard.setVisibility(View.GONE);
                                        statusText.setVisibility(View.VISIBLE);
                                        statusText.setText(R.string.no_address_added);
                                        addAddressButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                addressCard.setVisibility(View.GONE);
                                statusText.setVisibility(View.VISIBLE);
                                statusText.setText(R.string.no_address_added);
                                addAddressButton.setVisibility(View.VISIBLE);
                            }
                        }
                        toggleProgressBar(false);
                    });

        } else if (getArguments() != null && Objects.equals(getArguments().getString("param"), "DeliveryPreferences")) {
            getFromDB();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        chkDoorBell.setChecked(false);
        chkDropAtDoor.setChecked(false);
        chkInBag.setChecked(false);
        chkInHand.setChecked(false);
        chkNoPref.setChecked(false);
        int id = compoundButton.getId();

        if (id == R.id.chk_doorbell) {
            chkDoorBell.setChecked(b);
        } else if (id == R.id.chk_drop_at_door) {
            chkDropAtDoor.setChecked(b);
        } else if (id == R.id.chk_in_bag) {
            chkInBag.setChecked(b);
        } else if (id == R.id.chk_in_hand) {
            chkInHand.setChecked(b);
        } else if (id == R.id.chk_no_pref) {
            chkNoPref.setChecked(b);
        }
    }

}
