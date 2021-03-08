package com.c17206413.payup;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.c17206413.payup.ui.adapter.SectionsPagerAdapter;
import com.c17206413.payup.ui.accounts.MenuActivity;
import com.c17206413.payup.ui.accounts.SignIn;
import com.c17206413.payup.ui.accounts.SripeOnboardingView;
import com.c17206413.payup.ui.payment.CreatePaymentActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    //Firestore Initialisation
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    // user details
    private static String providerId, uid, name, email, language, customer_id, account_id, username;;


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

        db = FirebaseFirestore.getInstance();
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
        if (account_id==null) {
            startActivity(new Intent(MainActivity.this, SripeOnboardingView.class));
        } else {
            Intent intent = new Intent(this, CreatePaymentActivity.class);
            createPaymentResultLauncher.launch(intent);
        }
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
                    Intent data = result.getData();
                    if (data != null) {
                        String returnedResult = data.getStringExtra("result");
                        if (returnedResult.equals("Register")) {
                            askName();
                        }
                    }
                }
            });

    private void askName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name:");

        // Set up the input
        final EditText input = new EditText(MainActivity.this);
        input.setHint("Username");
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> setName(input.getText().toString()));

        builder.show();
    }

    private void setName(String name) {
        username = name;
        if (username!=null) {
            FirebaseUser user = mAuth.getCurrentUser();
            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
            builder.setDisplayName(username);
            if (user != null) {
                user.updateProfile(builder.build()).addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        Snackbar.make(findViewById(android.R.id.content), "Name not Set", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(user.getUid());
                userRef.update("name", username)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
            }
        }
    }

    ActivityResultLauncher<Intent> userResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String returnedResult = data.getStringExtra("result");
                        if (returnedResult.equals("logOut")) {
                            signOut();
                        }
                    }

                }
            });

    ActivityResultLauncher<Intent> createPaymentResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Snackbar.make(findViewById(android.R.id.content), "Expense created", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

    public void getUserProfile() {
        // [START get_user_profile]
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
            for (UserInfo profile : user.getProviderData()) {
                providerId = profile.getProviderId();
            }
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        String account = document.getString("connected_account_id");
                        String customer = document.getString("customer_id");
                        String docName = document.getString("name");
                        setFields(uid, docName, customer, account);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    finish();
                }
            });
        }
    }

    public void setFields(String uid, String name, String customer_id, String account_id) {
        MainActivity.name = name;
        MainActivity.account_id = account_id;
        MainActivity.customer_id = customer_id;
        MainActivity.uid = uid;
    }
}
