package com.c17206413.payup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.c17206413.payup.ui.accounts.MenuActivity;
import com.c17206413.payup.ui.accounts.SignIn;
import com.c17206413.payup.ui.accounts.SripeOnboardingView;
import com.c17206413.payup.ui.adapter.SectionsPagerAdapter;
import com.c17206413.payup.ui.payment.CreatePaymentActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    // Firestore Initialisation
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // user details
    private static String uid;
    private static String account_id;


    public static final String NIGHT_MODE = "NIGHT_MODE";
    private static SharedPreferences mPrefs;

    public static String getUid() {
        return uid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkInternetConnection(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
            if (mAuth.getCurrentUser() == null){
                signInUser();
            }
        };

        mAuth.addAuthStateListener(authStateListener);

        setTheme();
        NestedScrollView scrollView = findViewById(R.id.nestedScroll);
        scrollView.setFillViewport(true);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        getUserProfile();

        Button userButton = findViewById(R.id.userButton);
        userButton.setOnClickListener(v -> openUser());

        Button newExpenseButton = findViewById(R.id.newExpenseButton);
        newExpenseButton.setOnClickListener(v -> createPayment());
    }

    public static void checkInternetConnection(Context mContext) {
        if (!isNetworkAvailable(mContext)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Internet");
            builder.setMessage(R.string.no_internet_connection);

            int pid = android.os.Process.myPid();
            builder.setPositiveButton("Close", (dialog, which) -> android.os.Process.killProcess(pid));

            builder.show();
        }
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
        checkInternetConnection(this);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setTheme();
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
                    getUserProfile();
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

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
            {
                for (NetworkInfo networkInfo : info) {
                    Log.i("Class", networkInfo.getState().toString());
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setName(String name) {
        if (name !=null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
            builder.setDisplayName(name);
            if (user != null) {
                user.updateProfile(builder.build()).addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        Snackbar.make(findViewById(android.R.id.content), "Name not Set", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(user.getUid());
                userRef.update("name", name)
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
                    getUserProfile();
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
                    getUserProfile();
                    Snackbar.make(findViewById(android.R.id.content), "Expense created", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

    public void getUserProfile() {
        // [START get_user_profile]
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        String account = document.getString("connected_account_id");
                        String name = document.getString("name");
                        if (name == null || name.matches("")) {
                            askName();
                        }
                        setFields(uid, account);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    finish();
                }
            });
        } else {
            signInUser();
        }
    }

    public void setFields(String uid, String account_id) {
        MainActivity.account_id = account_id;
        MainActivity.uid = uid;
    }
}
