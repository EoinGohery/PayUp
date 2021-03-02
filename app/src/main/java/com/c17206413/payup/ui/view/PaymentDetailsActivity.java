package com.c17206413.payup.ui.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.c17206413.payup.R;

public class PaymentDetailsActivity extends Activity {

    private TextView serviceName;
    private TextView amountIndicator;
    private TextView dateIndicator;
    private TextView userIndicator;
    private TextView isPaid;
    private String currency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_details);

        serviceName =findViewById(R.id.service_name_details);
        amountIndicator =findViewById(R.id.amount_indicator_details);
        dateIndicator =findViewById(R.id.date_details);
        isPaid =findViewById(R.id.is_paid);
        userIndicator =findViewById(R.id.user_details);

        Bundle extras = getIntent().getExtras();
        serviceName.setText(extras.getString("serviceName"));
        amountIndicator.setText(extras.getString("amount"));
        dateIndicator.setText(extras.getString("date"));
        if (!extras.getBoolean("active")) {
            isPaid.setText(R.string.paid);
            isPaid.setTextColor(getResources().getColor(R.color.colorSuccess));
        }
        userIndicator.setText(extras.getString("user"));

        currency = extras.getString("currency");
    }
}
