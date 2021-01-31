package com.c17206413.payup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.c17206413.payup.ui.main.ExpenseActivity;
import com.c17206413.payup.ui.main.SectionsPagerAdapter;
import com.c17206413.payup.ui.main.SignIn;
import com.c17206413.payup.ui.main.UserActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //Firestore Initialisation
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    // user details
    String providerId, uid, name, email, language;

    public static final String NIGHT_MODE = "NIGHT_MODE";
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF"
        );


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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

        Button newExpenseButton= (Button) findViewById(R.id.newExpenseButton);
        newExpenseButton.setOnClickListener(v -> openPayment());
    }

    private void openUser() {
        Intent intent = new Intent(this, UserActivity.class);
        resumeActivityResultLauncher.launch(intent);
    }

    private void openPayment() {
        Intent intent = new Intent(this, ExpenseActivity.class);
        resumeActivityResultLauncher.launch(intent);
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
        resumeActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> resumeActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String returnedResult = data.getDataString();
                    if (returnedResult.equals("LogOut")) {
                        signOut();
                    } else if (returnedResult.equals("SignIn")) {
                        try {
                            checkCurrentUser();
                        } catch (Exception e) {
                            signInUser();
                        }
                    }
                }
            });


    public void getUserProfile() {
        // [START get_user_profile]
        user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
            DocumentReference docIdRef = db.collection("users").document(uid);
            docIdRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        try {
                            name = Objects.requireNonNull(document.get("name")).toString();
                            language = Objects.requireNonNull(document.get("language")).toString();
                            Configuration config = new Configuration();
                            Locale locale = new Locale(language);
                            Locale.setDefault(locale);
                            config.setLocale(locale);
                            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                            recreate();
                        } catch (RuntimeException e) {
                            Log.d("Get user data", "Failed with: ", e);
                        }
                    } else {
                        Log.d("Receive user info", "Failed with: ", task.getException());
                    }
                }
            });
        }
    }

    public void getProviderData() {
        // [START get_provider_data]
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                providerId = profile.getProviderId();
                uid = profile.getUid();
                name = profile.getDisplayName();
                email = profile.getEmail();
            }
        }
    }
}