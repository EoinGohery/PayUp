package com.c17206413.payup.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.c17206413.payup.R;

public class UserActivity extends AppCompatActivity {

    private static final String NIGHT_MODE = "NIGHT_MODE";
    private boolean isNightModeEnabled = false;

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