package com.whytefarms.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.whytefarms.R;
import com.whytefarms.adapters.TabAdapter;
import com.whytefarms.application.WhyteFarmsApplication;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public FrameLayout progressBar;
    public ViewPager2 viewPager;
    private DrawerLayout navDrawer;
    private TabLayout tabs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        NavigationView navigationView = findViewById(R.id.navigation);


        progressBar = findViewById(R.id.progress_bar);

        navDrawer = findViewById(R.id.nav_drawer_layout);


        navigationView.setNavigationItemSelectedListener(this);


        Menu menu = navigationView.getMenu();


        if (((WhyteFarmsApplication) getApplication()).isLoggedIn()) {
            menu.findItem(R.id.nav_login).setTitle(R.string.logout);
            menu.findItem(R.id.nav_login).setIcon(R.drawable.ic_logout);
            menu.findItem(R.id.nav_login).setOnMenuItemClickListener(menuItem -> {
                startLoginFlow(MainActivity.class.getSimpleName());
                return true;
            });
        } else {
            menu.findItem(R.id.nav_login).setTitle(R.string.login);
            menu.findItem(R.id.nav_login).setIcon(R.drawable.ic_key);
            menu.findItem(R.id.nav_login).setOnMenuItemClickListener(menuItem -> {
                startLoginFlow(LoginActivity.class.getSimpleName());
                return true;
            });
        }

        AppCompatImageView facebook = navigationView.getHeaderView(0).findViewById(R.id.facebook);
        AppCompatImageView instagram = navigationView.getHeaderView(0).findViewById(R.id.instagram);

        facebook.setOnClickListener(this);
        instagram.setOnClickListener(this);

        getTabLayoutMediator().attach();

        LinearLayout layout = (LinearLayout) ((LinearLayout) tabs.getChildAt(0)).getChildAt(1);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.weight = 0.34f;
        layout.setLayoutParams(params);

        layout = (LinearLayout) ((LinearLayout) tabs.getChildAt(0)).getChildAt(0);
        params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.weight = 0.22f;
        layout.setLayoutParams(params);


        layout = (LinearLayout) ((LinearLayout) tabs.getChildAt(0)).getChildAt(2);
        params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.weight = 0.22f;
        layout.setLayoutParams(params);

        layout = (LinearLayout) ((LinearLayout) tabs.getChildAt(0)).getChildAt(3);
        params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.weight = 0.22f;
        layout.setLayoutParams(params);

        if (getIntent().getExtras() != null) {
            if (getIntent().getBooleanExtra("from_checkout", false)) {
                viewPager.setCurrentItem(1);
            }
        }
    }

    @NonNull
    private TabLayoutMediator getTabLayoutMediator() {
        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);
        TabAdapter tabAdapter = new TabAdapter(this, "", "");

        viewPager.setAdapter(tabAdapter);

        tabs = findViewById(R.id.tabs);


        return new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.ic_shop);
                    tab.setText(R.string.shop);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_subscription);
                    tab.setText(R.string.subscriptions);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_calendar);
                    tab.setText(R.string.calendar);
                    break;
                case 3:
                    tab.setIcon(R.drawable.ic_wallet);
                    tab.setText(R.string.wallet);
                    break;
            }
        });
    }

    public void openDrawer() {
        navDrawer.openDrawer(GravityCompat.END);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        navDrawer.closeDrawer(GravityCompat.END, true);

        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_language:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.nav_rate_us:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store" +
                                "/apps/details?id=" + getApplicationContext().getPackageName())));
                break;

            case R.id.nav_help:
                Intent intentHelp = new Intent(this, HelpActivity.class);
                intentHelp.putExtra("what", "help_n_support");
                startActivity(intentHelp);
                break;


            case R.id.nav_refer:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                share.putExtra(Intent.EXTRA_TEXT,
                        "Check out this cool app!! I received farm fresh dairy at my doorstep!! " +
                                "Download using this link: https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                startActivity(Intent.createChooser(share, "Whyte Farms"));
                break;

            case R.id.nav_about:
                Intent intentAbout = new Intent(this, TextActivity.class);
                intentAbout.putExtra("what", "legal_n_about_us");
                startActivity(intentAbout);
                break;

            case R.id.nav_login:
                startLoginFlow(LoginActivity.class.getSimpleName());
                break;

        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent webIntent = null;
        switch (v.getId()) {
            case R.id.facebook:
                webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/thewhytefarms"));
                break;
            case R.id.instagram:
                webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/thewhytefarms"));
                break;
        }

        if (webIntent != null) {
            startActivity(webIntent);
        }
    }


}