package com.whytefarms.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.whytefarms.R;
import com.whytefarms.adapters.TabAdapter;
import com.whytefarms.utils.AppConstants;

import java.util.Objects;

public class FragmentSubscribe extends BottomSheetDialogFragment {

    public static FragmentSubscribe newInstance(String param1, String param2) {
        FragmentSubscribe fragment = new FragmentSubscribe();
        Bundle args = new Bundle();
        args.putString("subscriptionString", param1);
        args.putString("subscriptionType", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscribe, container, false);


        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);
        if (getArguments() != null) {
            TabAdapter tabAdapter = new TabAdapter(requireActivity(),
                    getArguments().getString("subscriptionString"),
                    getArguments().getString("subscriptionType"));
            TabLayout tabs = view.findViewById(R.id.tabs);
            TabLayoutMediator tabLayoutMediator;
            viewPager.setAdapter(tabAdapter);


            if (requireArguments().getString("subscriptionType") != null &&
                    Objects.requireNonNull(requireArguments().getString("subscriptionType"))
                            .equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
                tabLayoutMediator = new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
                    if (position == 0) {
                        tab.setIcon(R.drawable.ic_cart_24dp);
                        tab.setText(R.string.buy_once);
                    }
                });
            } else {
                tabLayoutMediator = new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setIcon(R.drawable.ic_everyday);
                            tab.setText(R.string.everyday);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.ic_on_interval);
                            tab.setText(R.string.on_interval);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.ic_custom_sub);
                            tab.setText(R.string.custom);
                            break;
                    }
                });
            }

            tabLayoutMediator.attach();


            if (getArguments().getString("subscriptionType") != null) {
                if (Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL)) {
                    viewPager.setCurrentItem(1);
                } else if (Objects.equals(getArguments().getString("subscriptionType"), AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE)) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(0);
                }
            }
        }
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            setupFullHeight(bottomSheetDialog);
        });
        return dialog;
    }


    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        ConstraintLayout bottomSheet = bottomSheetDialog.findViewById(R.id.bottom_sheet_layout);
        BottomSheetBehavior<ConstraintLayout> behavior = null;
        if (bottomSheet != null) {
            behavior = BottomSheetBehavior.from(bottomSheet);
        }
        ViewGroup.LayoutParams layoutParams = null;
        if (bottomSheet != null) {
            layoutParams = bottomSheet.getLayoutParams();
        }

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        if (bottomSheet != null) {
            bottomSheet.setLayoutParams(layoutParams);
        }
        if (behavior != null) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setDraggable(false);
        }


    }

    private int getWindowHeight() {
        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
}