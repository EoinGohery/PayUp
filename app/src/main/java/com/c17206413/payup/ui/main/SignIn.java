package com.c17206413.payup.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.widget.Button;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.c17206413.payup.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignIn extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    ProgressBar progressBar;
    TextInputLayout email;
    TextInputLayout password;
    TextInputLayout lastName;
    TextInputLayout firstName;
    LinearLayout signIn;
    LinearLayout register;
    EditText passwordInput;
    EditText emailInput;
    EditText firstNameInput;
    EditText lastNameInput;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.c17206413.payup.R.layout.activity_signin);
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        signIn = (LinearLayout) findViewById(R.id.signIn_Layout);
        email = (TextInputLayout) findViewById(R.id.emailLayout);
        register = (LinearLayout) findViewById(R.id.registerLayout);
        firstName = (TextInputLayout) findViewById(R.id.firstNameLayout);
        lastName = (TextInputLayout) findViewById(R.id.lastNameLayout);
        password = (TextInputLayout) findViewById(R.id.passwordLayout);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        Button googleButton = (Button) findViewById(R.id.login_google);
        googleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                googleSignIn();
            }
        });

        Button showRegisterButton = (Button) findViewById(R.id.link_signup);
        showRegisterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn.setVisibility(View.GONE);
                register.setVisibility(View.VISIBLE);
            }
        });

        Button facebookButton = (Button) findViewById(R.id.login_facebook);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
            }
        });

        Button emailLogin= (Button) findViewById(R.id.login_with_password);
        emailLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String emailString =findViewById(R.id.input_first_name).toString();
                String passwordString =findViewById(R.id.input_password).toString();
                loginSignIn(emailString, passwordString);
            }
        });

        Button resisterButton= (Button) findViewById(R.id.register);
        resisterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        Button backToSingInButton= (Button) findViewById(R.id.backToSignIn);
        backToSingInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                register.setVisibility(View.GONE);
                signIn.setVisibility(View.VISIBLE);
            }
        });
    }


    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                        // ...
                    }
                });
    }

    private void loginSignIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            // [START_EXCLUDE]
//                            checkForMultiFactorFailure(task.getException());
                            // [END_EXCLUDE]
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void sendEmailVerification() {
        // Disable button
//        mBinding.verifyEmailButton.setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
//                        mBinding.verifyEmailButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content), "Verification email sent to " + user.getEmail(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Failed to send verification email", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

//    private void checkForMultiFactorFailure(Exception e) {
//        // Multi-factor authentication with SMS is currently only available for
//        // Google Cloud Identity Platform projects. For more information:
//        // https://cloud.google.com/identity-platform/docs/android/mfa
//        if (e instanceof FirebaseAuthMultiFactorException) {
//            Log.w(TAG, "multiFactorFailure", e);
//            Intent intent = new Intent();
//            MultiFactorResolver resolver = ((FirebaseAuthMultiFactorException) e).getResolver();
//            intent.putExtra("EXTRA_MFA_RESOLVER", resolver);
//            setResult(MultiFactorActivity.RESULT_NEEDS_MFA_SIGN_IN, intent);
//            finish();
//        }
//    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                        // [START_EXCLUDE]
                        progressBar.setVisibility(View.INVISIBLE);
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        emailInput = findViewById(R.id.input_first_name);
        String emailString = emailInput.getText().toString();
        if (emailString.isEmpty()) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        passwordInput = findViewById(R.id.input_password);
        String passwordString = passwordInput.getText().toString();
        if (passwordString.isEmpty()) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);

        }

        return valid;
    }

    public void checkCurrentUser() {
        // [START check_current_user]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Login Failed, Try Again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
        // [END check_current_user]
    }

    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }


//    public void privacyAndTerms() {
//        List<AuthUI.IdpConfig> providers = Collections.emptyList();
//        // [START auth_fui_pp_tos]
//        startActivityForResult(
//                AuthUI.getInstance()
//                        .createSignInIntentBuilder()
//                        .setAvailableProviders(providers)
//                        .setTosAndPrivacyPolicyUrls(
//                                "https://example.com/terms.html",
//                                "https://example.com/privacy.html")
//                        .build(),
//                RC_SIGN_IN);
//        // [END auth_fui_pp_tos]
//    }

}