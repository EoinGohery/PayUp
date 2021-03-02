package com.c17206413.payup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.c17206413.payup.ui.Adapter.SectionsPagerAdapter;
import com.c17206413.payup.ui.accounts.MenuActivity;
import com.c17206413.payup.ui.accounts.SignIn;
import com.c17206413.payup.ui.payment.CreatePaymentActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.stripe.android.PaymentConfiguration;

public class MainActivity extends AppCompatActivity {

    //Firestore Initialisation
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // user details
    private static String providerId, providerUid, uid, name, email, language;

    public static final String NIGHT_MODE = "NIGHT_MODE";
    private static SharedPreferences mPrefs;

    public static String getUid() {
        return uid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF"
        );

        mAuth = FirebaseAuth.getInstance();
        checkCurrentUser();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        setTheme();
        NestedScrollView scrollView = findViewById(R.id.nestedScroll);
        scrollView.setFillViewport(true);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Button userButton = findViewById(R.id.userButton);
        userButton.setOnClickListener(v -> openUser());

        Button newExpenseButton = findViewById(R.id.newExpenseButton);
        newExpenseButton.setOnClickListener(v -> createPayment());
    }

    private void openUser() {
        Intent intent = new Intent(this, MenuActivity.class);
        userResultLauncher.launch(intent);
    }

    private void createPayment() {
        Intent intent = new Intent(this, CreatePaymentActivity.class);
        createPaymentResultLauncher.launch(intent);
    }


    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        signInUser();
    }

    public static void setTheme() {
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

    public void signInUser() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Intent intent = new Intent(this, SignIn.class);
        loginResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> loginResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    checkCurrentUser();
                }
            });


    ActivityResultLauncher<Intent> userResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String returnedResult = data.getDataString();
                        if (returnedResult.equals("LogOut")) {
                            signOut();
                        }
                    }

                }
            });

    ActivityResultLauncher<Intent> createPaymentResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    //TODO
                }
            });


    public void getUserProfile() {
        // [START get_user_profile]
        user = mAuth.getCurrentUser();
        if (user != null) {
            name = user.getDisplayName();
            email = user.getEmail();
            uid = user.getUid();
            for (UserInfo profile : user.getProviderData()) {
                providerId = profile.getProviderId();
                name = profile.getDisplayName();
                email = profile.getEmail();
            }
        }
    }
}
