package com.c17206413.payup.ui.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.c17206413.payup.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Arrays;
import java.util.Objects;

public class SignIn extends AppCompatActivity {

    private static final String TAG = "Sign In";

    private ProgressBar progressBar;
    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private TextInputLayout nameInput;
    private LinearLayout signIn;
    private LinearLayout registerLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.c17206413.payup.R.layout.activity_signin);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        signIn = (LinearLayout) findViewById(R.id.signIn_Layout);
        registerLayout = (LinearLayout) findViewById(R.id.registerLayout);

        emailInput = (TextInputLayout) findViewById(R.id.emailLayout);
        nameInput = (TextInputLayout) findViewById(R.id.nameLayout);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordLayout);

        //email sign in
        Button emailLogin= (Button) findViewById(R.id.login_with_password);
        emailLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String emailString = Objects.requireNonNull(emailInput.getEditText()).getText().toString();
            String passwordString = Objects.requireNonNull(passwordInput.getEditText()).getText().toString();
            loginSignIn(emailString, passwordString);
        });

        //Google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Button googleButton = (Button) findViewById(R.id.login_google);
        googleButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            googleActivityResultLauncher.launch(mGoogleSignInClient.getSignInIntent());
        });

        //Facebook login
        Button facebookButton = (Button)findViewById(R.id.login_facebook);
        facebookButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            LoginManager.getInstance().logInWithReadPermissions(SignIn.this, Arrays.asList("email","name"));
        });

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Snackbar.make(findViewById(android.R.id.content), "Authentication Cancelled.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        //enable registration form
        Button showRegisterButton = (Button) findViewById(R.id.link_signup);
        showRegisterButton.setOnClickListener(v -> {
            signIn.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        });

        //email registration
        Button registerButton= (Button) findViewById(R.id.register);
        registerButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String emailString =  Objects.requireNonNull(emailInput.getEditText()).getText().toString();
            String passwordString = Objects.requireNonNull(passwordInput.getEditText()).getText().toString();
            String nameString =  Objects.requireNonNull(nameInput.getEditText()).getText().toString();
            createAccount(emailString, passwordString, nameString);
        });

        //enable social media sign in buttons
        Button backToSingInButton= (Button) findViewById(R.id.backToSignIn);
        backToSingInButton.setOnClickListener(v -> {
            registerLayout.setVisibility(View.GONE);
            signIn.setVisibility(View.VISIBLE);
        });
    }

    //for facebook activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //handle google activity result
    ActivityResultLauncher<Intent> googleActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        assert account != null;
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e);
                        Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        // ...
                    }
                }
            });

    //handle google authentication
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        socialAuthentication(credential);
    }

    private void socialAuthentication(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        exitActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        socialAuthentication(credential);
    }


    //email login method
    private void loginSignIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateLoginInForm()) {
            return;
        }
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        exitActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
        // [END sign_in_with_email]
    }

    //email registration method
    private void createAccount(String email, String password, String name) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateRegisterForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        user = mAuth.getCurrentUser();
                        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                        builder.setDisplayName(name);
                        if (user !=null){
                            user.updateProfile(builder.build()).addOnCompleteListener(task1 -> {
                                if (!task1.isSuccessful()){
                                    Snackbar.make(findViewById(android.R.id.content), name, Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            });
                        }
                        exitActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Registration Failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
        // [END create_user_with_email]
    }

    //validate login strings correctness
    private boolean validateLoginInForm() {
        boolean valid = true;

        String emailString = Objects.requireNonNull(emailInput.getEditText()).getText().toString();
        if (emailString.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            emailInput.setError("Required.");
            valid = false;
        } else {
            emailInput.setError(null);
        }

        String passwordString = Objects.requireNonNull(passwordInput.getEditText()).getText().toString();
        if (passwordString.isEmpty()) {
            passwordInput.setError("Required.");
            valid = false;
        } else {
            passwordInput.setError(null);
        }
        return valid;
    }

    //validate login strings correctness
    private boolean validateRegisterForm() {
        boolean valid = true;

        String emailString = Objects.requireNonNull(emailInput.getEditText()).getText().toString();
        if (emailString.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            emailInput.setError("Required.");
            valid = false;
        } else {
            emailInput.setError(null);
        }

        String passwordString = Objects.requireNonNull(passwordInput.getEditText()).getText().toString();
        if (passwordString.isEmpty()) {
            passwordInput.setError("Required.");
            valid = false;
        } else {
            passwordInput.setError(null);

        }

        String nameString = Objects.requireNonNull(nameInput.getEditText()).getText().toString();
        if (nameString.isEmpty()) {
            nameInput.setError("Required.");
            valid = false;
        } else {
            nameInput.setError(null);
        }
        return valid;

    }

    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    private void exitActivity() {
        setResult(RESULT_OK);
        finish();
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