package com.c17206413.payup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import androidx.viewpager.widget.ViewPager;

import com.c17206413.payup.ui.accounts.MenuActivity;
import com.c17206413.payup.ui.accounts.SignIn;
import com.c17206413.payup.ui.accounts.SripeOnboardingView;
import com.c17206413.payup.ui.adapter.SectionsPagerAdapter;
import com.c17206413.payup.ui.model.CurrentUser;
import com.c17206413.payup.ui.payment.CreatePaymentActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN";

    // Firestore Initialisation
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //publicly available current User object
    public static final CurrentUser currentUser = new CurrentUser();

    public static CurrentUser getUser() {
        return currentUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check the internet connection
        checkInternetConnection(this);

        //initialise firebase db
        db = FirebaseFirestore.getInstance();

        //initialise firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //set auth change listener
        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
            if (mAuth.getCurrentUser() == null){
                signInUser();
            }
        };
        mAuth.addAuthStateListener(authStateListener);

        //initialise payment configuration with stripe
        PaymentConfiguration.init(getApplicationContext(), getString(R.string.publish_key));

        //get currently logged in user
        getUserProfile();

        //nested scroll view to contain the fragment adapter
        NestedScrollView scrollView = findViewById(R.id.nestedScroll);
        scrollView.setFillViewport(true);

        //sections adapter to contain the fragments
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        //View pager to display fragments
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //user menu button
        Button userButton = findViewById(R.id.userButton);
        userButton.setOnClickListener(v -> openUser());

        //create expense launch activity button
        Button newExpenseButton = findViewById(R.id.newExpenseButton);
        newExpenseButton.setOnClickListener(v -> createPayment());
    }

    //check the current internet connection (Application must have internet)
    public static void checkInternetConnection(Context mContext) {
        if (!isNetworkAvailable(mContext)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Internet");
            builder.setMessage(R.string.no_internet_connection);

            int pid = android.os.Process.myPid();
            builder.setPositiveButton("Close", (dialog, which) -> android.os.Process.killProcess(pid));

            //show popup indicating no internet
            builder.show();
        }
    }

    //open the user activity
    private void openUser() {
        Intent intent = new Intent(this, MenuActivity.class);
        userResultLauncher.launch(intent);
    }

    //open the create payment activity or onboarding activity
    private void createPayment() {
        if (currentUser.getAccount_id()==null) {
            //no account, launch onboarding
            openStripeOnboarding();
        } else {
            //if account exists, launch create payment
            Intent intent = new Intent(this, CreatePaymentActivity.class);
            createPaymentResultLauncher.launch(intent);
        }
    }

    //open stripe Onboarding activity
    private void openStripeOnboarding() {
        Intent openStripeIntent = new Intent(this, SripeOnboardingView.class);
        OnboardingResultLauncher.launch(openStripeIntent);
    }

    //activity handler for Stripe Onboarding
    ActivityResultLauncher<Intent> OnboardingResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        //get the new connected account status
                        DocumentReference docRef = db.collection("users").document(currentUser.getId());
                        docRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                assert document != null;
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    //set the current users connected account id
                                    currentUser.setAccount_id(document.getString("connected_account_id"));
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        });
                    }
                }
            });


    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
    }

    @Override
    //onResume check internet and get profile data
    public void onResume() {
        super.onResume();
        checkInternetConnection(this);
    }

    //launch sign in activity
    public void signInUser() {
        Intent intent = new Intent(this, SignIn.class);
        loginResultLauncher.launch(intent);
    }

    //handle sign in result
    ActivityResultLauncher<Intent> loginResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    //get current user data
                    getUserProfile();
                }
            });

    //Alert box to ask for username on registration
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

    //check network availability
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
            {
                //search for an internet connection status
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

    //set the name once the user has set one
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
                            currentUser.reset();
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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            //get the document of the user data
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        //get the user info and set to currentUser object
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        currentUser.setAccount_id(document.getString("connected_account_id"));
                        currentUser.setEmail(user.getEmail());
                        currentUser.setId(uid);
                        currentUser.setDarkMode(document.getBoolean("darkMode"));
                        String name = document.getString("name");
                        String profile = document.getString("profileUrl");
                        //only parse profile picture if one exists
                        if (profile != null) {
                            currentUser.setImageUrl(Uri.parse(profile));
                        }
                        //if name is black or null create askNmae popup
                        if (name == null || name.matches("")) {
                            askName();
                        } else {
                            currentUser.setUsername(name);
                        }
                        //set the correct darmode settings
                        if (currentUser.getDarkMode()==null || !currentUser.getDarkMode()) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        }
                    } else {
                        //log error
                        Log.d(TAG, "No such document");
                    }
                } else {
                    //log error
                    Log.d(TAG, "get failed with ", task.getException());
                    finish();
                }
            });
        } else {
            //if user is not found
            signInUser();
        }
    }
}
