package com.c17206413.payup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.c17206413.payup.ui.main.SignIn;
import com.c17206413.payup.ui.main.UserActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.c17206413.payup.ui.main.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // user details
    String providerId;
    String uid;
    String name;
    String email;
    Uri photoUrl;

    public static final String NIGHT_MODE = "NIGHT_MODE";
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        checkCurrentUser();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        setTheme();
        NestedScrollView scrollView = (NestedScrollView) findViewById (R.id.nestedScroll);
        scrollView.setFillViewport (true);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Button userButton= (Button) findViewById(R.id.userButton);
        userButton.setOnClickListener(v -> openUser());
    }

    private void openUser() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivityForResult(intent,1);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        signInUser();
    }

    private void setTheme() {
        boolean isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);
        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTheme();
        getUserProfile();

        //checkCurrentUser();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setTheme();

    }

    public void checkCurrentUser() {
        // [START check_current_user]
        user = mAuth.getCurrentUser();
        if (user != null) {
            getUserProfile();
        } else {
            signInUser();
        }
        // [END check_current_user]
    }

    public void  signInUser() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Intent intent = new Intent(this, SignIn.class);
        startActivityForResult(intent,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 0) {
            try {
                getUserProfile();
            } catch (Exception e) {
                signInUser();
            }
        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String returnedResult = data.getDataString();
                if (returnedResult.equals("LogOut")){
                    signOut();
                }
            }
        }
    }

    public void getUserProfile() {
        // [START get_user_profile]
        user = mAuth.getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            name = user.getDisplayName();
            email = user.getEmail();
            photoUrl = user.getPhotoUrl();
            //boolean emailVerified = user.isEmailVerified();
            uid = user.getUid();
        }
        // [END get_user_profile]
    }

    public void getProviderData() {
        // [START get_provider_data]
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                providerId = profile.getProviderId();

                // UID specific to the provider
                uid = profile.getUid();

                // Name, email address, and profile photo Url
                name = profile.getDisplayName();
                email = profile.getEmail();
                photoUrl = profile.getPhotoUrl();
            }
        }
    }
}