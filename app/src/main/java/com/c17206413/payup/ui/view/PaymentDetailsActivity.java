package com.c17206413.payup.ui.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import com.c17206413.payup.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PaymentDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_details);

        ImageButton backButton= findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView serviceName = findViewById(R.id.service_name_details);
        TextView amountIndicator = findViewById(R.id.amount_indicator_details);
        TextView dateIndicator = findViewById(R.id.date_details);
        TextView isPaid = findViewById(R.id.is_paid);
        TextView userIndicator = findViewById(R.id.user_details);

        Bundle extras = getIntent().getExtras();
        String currency = extras.getString("currency");
        Double amount =extras.getDouble("amount");
        serviceName.setText(extras.getString("serviceName"));

        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMaximumFractionDigits(2);
        format.setCurrency(Currency.getInstance(currency));

        amountIndicator.setText(format.format(amount));
        dateIndicator.setText(extras.getString("date"));
        if (!extras.getBoolean("active")) {
            isPaid.setText(R.string.paid);
            isPaid.setTextColor(getResources().getColor(R.color.colorSuccess));
        }
        userIndicator.setText(extras.getString("user"));
    }
}
