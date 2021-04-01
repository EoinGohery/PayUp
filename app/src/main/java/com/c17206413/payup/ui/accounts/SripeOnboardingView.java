package com.c17206413.payup.ui.accounts;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.c17206413.payup.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SripeOnboardingView extends AppCompatActivity {

    //firebase functions
    private FirebaseFunctions mFunctions;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_onboarding_view);

        //firebase functions initialise
        mFunctions = FirebaseFunctions.getInstance();

        //WebView UI element
        WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings ws = webView.getSettings();
        ws.setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new Callback());
        //Load stripe Onboarding url for this application
        webView.loadUrl("https://connect.stripe.com/oauth/authorize?response_type=code&client_id=ca_IOBii6j7E2TGUDjLMBChG5j65L8sq8GN&scope=read_write");
    }

    //callback handler for webview
    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //redirect string (meant to be company website)
            if (url.startsWith("https://github.com/EoinGohery?scope=read_write&code=")) {
                //removes redirect string and receives the authentication code
                String code = url.replace("https://github.com/EoinGohery?scope=read_write&code=" ,"");

                //authorize new account using code
                authorizeStripeAccount(code)
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                //Log error
                                Exception e = task.getException();
                                if (e instanceof FirebaseFunctionsException) {
                                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                    FirebaseFunctionsException.Code code1 = ffe.getCode();
                                    Log.w("FunctionException", (code1).toString());
                                }
                            } else {
                                //if successful, end Onboarding activity
                                finish();
                            }
                        });
            }
            return(false);
        }

        //Authorize the account on the server using returned code
        private Task<String> authorizeStripeAccount(String code) {
            // Create the arguments to the callable function.
            Map<String, Object> data = new HashMap<>();
            data.put("code", code);
            //initiate authorizeStripeAccount function on server
            return mFunctions
                    .getHttpsCallable("authorizeStripeAccount")
                    .call(data)
                    .continueWith(task -> (String) Objects.requireNonNull(task.getResult()).getData());
        }
    }
}
