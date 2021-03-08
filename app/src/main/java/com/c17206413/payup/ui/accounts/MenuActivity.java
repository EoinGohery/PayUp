package com.c17206413.payup.ui.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.c17206413.payup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuActivity extends AppCompatActivity {

    private static final String NIGHT_MODE = "NIGHT_MODE";
    private static final String TAG = USER_SERVICE;
    private boolean isNightModeEnabled = false;

    private FirebaseFirestore db;
    private FirebaseUser user;

    private TextView stripeAccount;

    // user details
    private String providerId, uid, name, email, language, customer_id, account_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);
        setContentView(R.layout.activity_menu);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // initiate Dark Mode Switch
        SwitchCompat darkSwitch = findViewById(R.id.darkModeSwitch);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            darkSwitch.setChecked(true);
        }
        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setIsNightModeEnabled(isChecked));

        ImageButton backButton= findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button saveButton= findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> finish());

        Button logOutButton= findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(v -> logOut());

        stripeAccount= findViewById(R.id.stripeAccountButton);
        stripeAccount.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SripeOnboardingView.class)));

        Button accountButton= findViewById(R.id.accountsButton);
        accountButton.setOnClickListener(v -> {
            if (stripeAccount.getVisibility() == View.GONE) {
                stripeAccount.setVisibility(View.VISIBLE);
            } else if(stripeAccount.getVisibility()==View.VISIBLE) {
                stripeAccount.setVisibility(View.GONE);
            }
        });

        getUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserProfile();
    }

    public void updateUI() {
        if (account_id==null) {
            stripeAccount.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_delete, 0);
        } else {
            stripeAccount.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.com_facebook_button_like_icon_selected, 0);
        }
    }

    public void getUserProfile() {
        // [START get_user_profile]
        if (user != null) {
            name = user.getDisplayName();
            email = user.getEmail();
            uid = user.getUid();
            for (UserInfo profile : user.getProviderData()) {
                providerId = profile.getProviderId();
                name = profile.getDisplayName();
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
        //User user = new User(uid, name, "default");
        this.name = name;
        this.account_id = account_id;
        this.customer_id = customer_id;
        updateUI();
    }

    @SuppressLint("ApplySharedPref")
    public void setIsNightModeEnabled(boolean NightModeEnabled) {
        isNightModeEnabled = NightModeEnabled;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled);
        editor.commit();
        if (NightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void logOut() {
        Intent data = new Intent();
        String text = "LogOut";
        data.setData(Uri.parse(text));
        setResult(RESULT_OK, data);
        finish();
    }
}