package com.c17206413.payup.ui.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.c17206413.payup.MainActivity;
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
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class SignIn extends AppCompatActivity {

    private static final String TAG = "Sign In";

    private ProgressBar progressBar;
    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private LinearLayout signIn;
    private LinearLayout registerLayout;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showHashKey(this);

        setContentView(com.c17206413.payup.R.layout.activity_signin);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar1);

        signIn = findViewById(R.id.signIn_Layout);
        registerLayout = findViewById(R.id.registerLayout);

        emailInput = findViewById(R.id.emailLayout);
        passwordInput = findViewById(R.id.passwordLayout);

        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
            if (mAuth.getCurrentUser() != null){
                setResult(RESULT_OK);
                finish();
            }
        };

        mAuth.addAuthStateListener(authStateListener);

        //email sign in
        Button emailLogin= findViewById(R.id.login_with_password);
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
        Button googleButton = findViewById(R.id.login_google);
        googleButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            googleActivityResultLauncher.launch(mGoogleSignInClient.getSignInIntent());
        });

        //Facebook login
        Button facebookButton = findViewById(R.id.login_facebook);
        facebookButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            LoginManager.getInstance().logInWithReadPermissions(SignIn.this, Arrays.asList("public_profile", "email"));
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
        Button showRegisterButton = findViewById(R.id.link_signup);
        showRegisterButton.setOnClickListener(v -> {
            signIn.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        });

        //email registration
        Button registerButton= findViewById(R.id.register);
        registerButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String emailString =  Objects.requireNonNull(emailInput.getEditText()).getText().toString();
            String passwordString = Objects.requireNonNull(passwordInput.getEditText()).getText().toString();
            createAccount(emailString, passwordString);
        });

        //enable social media sign in buttons
        Button backToSingInButton= findViewById(R.id.create_payment_button);
        backToSingInButton.setOnClickListener(v -> {
            registerLayout.setVisibility(View.GONE);
            signIn.setVisibility(View.VISIBLE);
        });

        MainActivity.checkInternetConnection(this);
    }

    public void showHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.example.tryitonjewelry", PackageManager.GET_SIGNATURES); //Your            package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Snackbar.make(findViewById(android.R.id.content), Base64.encodeToString(md.digest(), Base64.DEFAULT), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
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
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
        // [END sign_in_with_email]
    }

    //email registration method
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateLoginInForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        Intent data = new Intent();
                        data.putExtra("result", "Register");
                        setResult(RESULT_OK, data);
                        finish();
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateLoginInForm() {
        String emailString = Objects.requireNonNull(emailInput.getEditText()).getText().toString();
        if (emailString.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            emailInput.setError("Required.");
            return false;
        } else {
            emailInput.setError(null);
            progressBar.setVisibility(View.VISIBLE);
        }

        String passwordString = Objects.requireNonNull(passwordInput.getEditText()).getText().toString();
        if (passwordString.isEmpty()) {
            passwordInput.setError("Required.");
            return false;
        } else {
            passwordInput.setError(null);
            progressBar.setVisibility(View.VISIBLE);
        }
        return true;
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