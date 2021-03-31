package com.c17206413.payup.ui.payment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.c17206413.payup.R;

import java.text.NumberFormat;
import java.util.Currency;


public class PaymentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_details);

        ImageButton backButton= findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView serviceName = findViewById(R.id.serviceNameDetails);
        TextView amountIndicator = findViewById(R.id.amountIndicatorDetails);
        TextView dateCreated = findViewById(R.id.dateCreatedDetail);
        TextView datePaid = findViewById(R.id.datePaidDetail);
        TextView isPaid = findViewById(R.id.is_paid);
        TextView userIndicator = findViewById(R.id.userDetail);
        TextView paymentMethod = findViewById(R.id.methodDetail);

        Bundle extras = getIntent().getExtras();
        String currency = extras.getString("currency");
        Double amount =extras.getDouble("amount");
        String service = extras.getString("serviceName");
        serviceName.setText(service);
        //String clientSecret = extras.getString("clientSecret")

        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMaximumFractionDigits(2);
        format.setCurrency(Currency.getInstance(currency));

        amountIndicator.setText(format.format(amount));
        dateCreated.setText(extras.getString("dateCreated"));
        datePaid.setText(extras.getString("datePaid"));
        paymentMethod.setText(extras.getString("paymentMethod"));
        if (!extras.getBoolean("active")) {
            isPaid.setText(R.string.paid);
            isPaid.setTextColor(getResources().getColor(R.color.colorSuccess));
            datePaid.setVisibility(View.INVISIBLE);
            paymentMethod.setVisibility(View.INVISIBLE);
        }
        userIndicator.setText(extras.getString("user"));
    }

}
