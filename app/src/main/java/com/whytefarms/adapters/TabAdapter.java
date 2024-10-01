package com.whytefarms.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.whytefarms.fragments.FragmentHome;
import com.whytefarms.fragments.FragmentSubscriptionType;
import com.whytefarms.utils.AppConstants;

public class TabAdapter extends FragmentStateAdapter {

    private final String parentActivity;
    private final String subscriptionString;
    private final String subscriptionType;
    Fragment returnFragment;

    public TabAdapter(@NonNull FragmentActivity fragmentActivity, String subscriptionString, String subscriptionType) {
        super(fragmentActivity);
        this.parentActivity = fragmentActivity.getClass().getSimpleName();
        this.subscriptionString = subscriptionString;
        this.subscriptionType = subscriptionType;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (parentActivity) {
            case "MainActivity":
            default:
                if (subscriptionString.isEmpty()) {
                    switch (position) {
                        case 3:
                            returnFragment = FragmentHome.newInstance("Wallet");
                            break;
                        case 2:
                            returnFragment = FragmentHome.newInstance("Calendar");
                            break;
                        case 1:
                            returnFragment = FragmentHome.newInstance("Subscriptions");
                            break;
                        case 0:
                        default:
                            returnFragment = FragmentHome.newInstance("Shop");
                            break;
                    }
                } else {
                    switch (position) {
                        case 2:
                            returnFragment = FragmentSubscriptionType.newInstance(subscriptionString, AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE);
                            break;
                        case 1:
                            returnFragment = FragmentSubscriptionType.newInstance(subscriptionString, AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL);
                            break;
                        case 0:
                        default:
                            returnFragment = FragmentSubscriptionType.newInstance(subscriptionString, AppConstants.SUBSCRIPTION_TYPE_EVERYDAY);
                            break;

                    }
                }
                break;
            case "DescriptionActivity":
                switch (position) {
                    case 2:
                        returnFragment = FragmentSubscriptionType.newInstance("", AppConstants.SUBSCRIPTION_TYPE_CUSTOMIZE);
                        break;
                    case 1:
                        returnFragment = FragmentSubscriptionType.newInstance("", AppConstants.SUBSCRIPTION_TYPE_ON_INTERVAL);
                        break;
                    case 0:
                    default:
                        if (subscriptionType.equalsIgnoreCase(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME)) {
                            returnFragment = FragmentSubscriptionType.newInstance("", AppConstants.SUBSCRIPTION_TYPE_ONE_TIME);
                        } else {
                            returnFragment = FragmentSubscriptionType.newInstance("", AppConstants.SUBSCRIPTION_TYPE_EVERYDAY);
                        }
                        break;

                }
                break;
        }
        return returnFragment;
    }

    public Fragment getFragment() {
        return returnFragment;
    }

    @Override
    public int getItemCount() {
        return parentActivity.equals("MainActivity") && subscriptionString.isEmpty() ? 4 :
                parentActivity.equals("MainActivity") ? 3
                        : subscriptionType.equals(AppConstants.SUBSCRIPTION_TYPE_ONE_TIME) ? 1 : 3;
    }
}