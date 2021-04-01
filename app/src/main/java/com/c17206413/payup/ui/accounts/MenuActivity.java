package com.c17206413.payup.ui.accounts;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.c17206413.payup.MainActivity.currentUser;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MENU";

    //Firebase elements
    private FirebaseFirestore db;
    private StorageReference mStorageRef;

    //UI elements
    private TextView stripeAccount;
    private TextView emailText;
    private EditText nameEdit;
    private SwitchCompat darkSwitch;
    private ImageView profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Firebase initialise
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        //Profile Image
        profile_image = findViewById(R.id.profileImage);
        profile_image.setOnClickListener(view -> openGallery());

        //EditText to change display name
        LinearLayout nameLayout= findViewById(R.id.nameLayout);
        nameEdit = findViewById(R.id.nameEdit);

        // initiate Dark Mode Switch
        darkSwitch = findViewById(R.id.darkModeSwitch);
        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setDarkModeEnabled(isChecked));

        //Back button to return to main
        ImageButton backButton= findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        //Save button to change name
        Button saveButton= findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> updateName());

        //Logout button
        Button logOutButton= findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(v -> logOut());

        //button to initialise Stripe Onboarding
        stripeAccount= findViewById(R.id.stripeAccountButton);
        stripeAccount.setOnClickListener(v -> openStripeOnboarding());

        //User email
        LinearLayout emailLayout= findViewById(R.id.emailLayout);
        emailText= findViewById(R.id.emailText);

        //Button to display connected accounts
        Button accountButton= findViewById(R.id.accountsButton);
        accountButton.setOnClickListener(v -> {
            if (stripeAccount.getVisibility() == View.GONE) {
                stripeAccount.setVisibility(View.VISIBLE);
            } else if(stripeAccount.getVisibility()==View.VISIBLE) {
                stripeAccount.setVisibility(View.GONE);
            }
        });

        //Button to display user info
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

        //check internet and set variable UI elements
        MainActivity.checkInternetConnection(this);
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        //set user info
        nameEdit.setText(currentUser.getUsername());
        emailText.setText(currentUser.getEmail());

        //set darkmode switch
        if (currentUser.getDarkMode() == null) {
            setDarkModeEnabled(false);
            darkSwitch.setChecked(false);
        } else {
            setDarkModeEnabled(currentUser.getDarkMode());
            darkSwitch.setChecked(currentUser.getDarkMode());
        }
        //set profile image
        if (currentUser.getImageUrl() == null) {
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(MenuActivity.this).load(currentUser.getImageUrl()).into(profile_image);
        }
        //set current stripe account indicator
        if (currentUser.getAccount_id()==null) {
            stripeAccount.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_delete, 0);
        } else {
            stripeAccount.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.com_facebook_button_like_icon_selected, 0);
        }
    }

    //Enable/Disable darkMode
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        //set darkmode in user
        currentUser.setDarkMode(darkModeEnabled);
        //set app theme
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        //update darkmode in db
        if (currentUser.getId() != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getId());
            userRef.update("darkMode", darkModeEnabled)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
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
                        DocumentReference docRef = db.collection("users").document(currentUser.getId());
                        docRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                assert document != null;
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
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

    //open gallery to select profile image
    private void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryResultLauncher.launch(openGalleryIntent);
    }

    //activity handler for gallery
    ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        //set teh new image
                        Uri image = data.getData();
                        uploadImage(image);
                    }
                }
            });

    private void uploadImage(Uri image) {
        //set storage refernece for profile image of current user
        StorageReference fileRef = mStorageRef.child(currentUser.getId() + "/profile.jpg");
        //upload new file
        fileRef.putFile(image).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            //set user object to new profile uri in firebase storage
            currentUser.setImageUrl(uri);
            Snackbar.make(findViewById(android.R.id.content), "Image Uploaded", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            //place image in priofile image layout location
            Glide.with(MenuActivity.this).load(uri).into(profile_image);

            //add the storage location uri to user db
            DocumentReference userRef = db.collection("users").document(currentUser.getId());
            userRef.update("profileUrl", uri.toString())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        })).addOnFailureListener(e -> {
            //if image upload fails, log error and display popup
            Log.d(TAG, "Profile image failed to update ", e);
            Snackbar.make(findViewById(android.R.id.content), "Image not Uploaded", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }).addOnProgressListener(snapshot -> {
            //display uploading popup
            Snackbar.make(findViewById(android.R.id.content), "Uploading", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        });
    }

    //update the user name
    private void updateName() {
        //get new name
        String name = nameEdit.getText().toString();
        if (!name.matches("")) {
            //set user name on user object
            currentUser.setUsername(name);
            //set username in db
            if (currentUser.getId() != null) {
                DocumentReference userRef = db.collection("users").document(currentUser.getId());
                userRef.update("name", name)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
            }
        }
    }

    private void logOut() {
        //create return intent
        Intent data = new Intent();
        data.putExtra("result", "logOut");
        setResult(RESULT_OK, data);
        finish();
    }
}