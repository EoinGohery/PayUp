package com.c17206413.payup.ui.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.c17206413.payup.MainActivity;
import com.c17206413.payup.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = USER_SERVICE;

    private FirebaseFirestore db;
    private FirebaseUser user;

    private TextView stripeAccount;
    private TextView emailText;
    private EditText nameEdit;

    private SwitchCompat darkSwitch;

    private ImageView profile_image;

    private StorageReference mStorageRef;

    // user details
    private String providerId, email, language, account_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        nameEdit = findViewById(R.id.nameEdit);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        profile_image = findViewById(R.id.profileImage);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // initiate Dark Mode Switch
        darkSwitch = findViewById(R.id.darkModeSwitch);
        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setIsNightModeEnabled(isChecked));

        ImageButton backButton= findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button saveButton= findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> updateName());

        Button logOutButton= findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(v -> logOut());

        stripeAccount= findViewById(R.id.stripeAccountButton);
        stripeAccount.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SripeOnboardingView.class)));

        emailText= findViewById(R.id.emailText);
        LinearLayout emailLayout= findViewById(R.id.emailLayout);
        LinearLayout nameLayout= findViewById(R.id.nameLayout);


        Button accountButton= findViewById(R.id.accountsButton);
        accountButton.setOnClickListener(v -> {
            if (stripeAccount.getVisibility() == View.GONE) {
                stripeAccount.setVisibility(View.VISIBLE);
            } else if(stripeAccount.getVisibility()==View.VISIBLE) {
                stripeAccount.setVisibility(View.GONE);
            }
        });

        Button userButton= findViewById(R.id.userDetails);
        userButton.setOnClickListener(v -> {
            if (nameLayout.getVisibility() == View.GONE) {
                nameLayout.setVisibility(View.VISIBLE);
            } else if(nameLayout.getVisibility()==View.VISIBLE) {
                nameLayout.setVisibility(View.GONE);
            }
            if (emailLayout.getVisibility() == View.GONE) {
                emailLayout.setVisibility(View.VISIBLE);
            } else if(emailLayout.getVisibility()==View.VISIBLE) {
                emailLayout.setVisibility(View.GONE);
            }
        });

        profile_image.setOnClickListener(view -> {
            //open gallery
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResultLauncher.launch(openGalleryIntent);
        });

        MainActivity.checkInternetConnection(this);
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
            email = user.getEmail();
            for (UserInfo profile : user.getProviderData()) {
                providerId = profile.getProviderId();
            }
            DocumentReference docRef = db.collection("users").document(MainActivity.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        String account = document.getString("connected_account_id");
                        String docName = document.getString("name");
                        String profile = document.getString("profileUrl");
                        Boolean darkMode = document.getBoolean("darkMode");
                        Uri profileUri = null;
                        if (profile != null) {
                            profileUri = Uri.parse(profile);
                        }
                        setFields(docName, account, email, profileUri, darkMode);
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

    public void setFields(String name, String account_id, String email, Uri photoUri, Boolean darkMode) {
        //User user = new User(uid, name, "default");
        nameEdit.setText(name);
        emailText.setText(email);
        this.account_id = account_id;
        if (darkMode == null) {
            setIsNightModeEnabled(false);
            darkSwitch.setChecked(false);
        } else {
            setIsNightModeEnabled(darkMode);
            darkSwitch.setChecked(darkMode);
        }


        if (photoUri == null) {
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(MenuActivity.this).load(photoUri).into(profile_image);
        }

        updateUI();
    }

    @SuppressLint("ApplySharedPref")
    public void setIsNightModeEnabled(boolean NightModeEnabled) {
        if (NightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(MainActivity.getUid());
            userRef.update("darkMode", NightModeEnabled)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        }
    }

    private void logOut() {
        Intent data = new Intent();
        data.putExtra("result", "logOut");
        setResult(RESULT_OK, data);
        finish();
    }

    ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri image = data.getData();
                        uploadImage(image);
                    }
                }
            });


    private void updateName() {
        String name = nameEdit.getText().toString();
        if (!name.matches("")) {
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

    private void uploadImage(Uri image) {
        StorageReference fileRef = mStorageRef.child(MainActivity.getUid() + "/profile.jpg");
        fileRef.putFile(image).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Snackbar.make(findViewById(android.R.id.content), "Image Uploaded", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Glide.with(MenuActivity.this).load(uri).into(profile_image);
            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
            builder.setPhotoUri(uri);
            user.updateProfile(builder.build()).addOnCompleteListener(task1 -> {
                if (!task1.isSuccessful()) {
                    Log.d(TAG, "Profile image successfully updated");
                }
            });
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.update("profileUrl", uri.toString())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        })).addOnFailureListener(e -> {
            Log.d(TAG, "Profile image failed to update ", e);
            Snackbar.make(findViewById(android.R.id.content), "Image not Uploaded", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }).addOnProgressListener(snapshot -> Snackbar.make(findViewById(android.R.id.content), "Uploading", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }
}