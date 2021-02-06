package com.c17206413.payup.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.c17206413.payup.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;

public class UserActivity extends AppCompatActivity {

    private static final String NIGHT_MODE = "NIGHT_MODE";
    private boolean isNightModeEnabled = false;

    private FirebaseFirestore db;

    private static UserActivity singleton = null;
    public static UserActivity getInstance() {
        if(singleton == null)
        {
            singleton = new UserActivity();
        }
        return singleton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleton = this;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();

        // initiate Dark Mode Switch
        SwitchCompat darkSwitch = (SwitchCompat) findViewById(R.id.darkModeSwitch);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            darkSwitch.setChecked(true);
        }
        darkSwitch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) (buttonView, isChecked) -> setIsNightModeEnabled(isChecked));

                // check current state of a Switch (true or false).
        ImageButton backButton= (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button saveButton= (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> killActivity());

        Button logOutButton= (Button) findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(v -> logOut());

        Button stripeAccountButton= (Button) findViewById(R.id.stripeAccountButton);
        stripeAccountButton.setOnClickListener(v -> startActivity(new Intent(UserActivity.this, SripeOnboardingView.class)));

    }

    private void checkDocument(String Uid, String name) {
        DocumentReference docIdRef = db.collection("stripe_customers").document(Uid);
        docIdRef.update("name", name)
                .addOnSuccessListener(aVoid -> Log.d("Document", "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w("Document", "Error updating document", e));
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
        killActivity();
    }

    private void killActivity() {
        finish();
    }

}