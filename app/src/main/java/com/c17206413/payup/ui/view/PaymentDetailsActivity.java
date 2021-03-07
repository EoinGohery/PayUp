package com.c17206413.payup.ui.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.c17206413.payup.R;

import java.text.NumberFormat;
import java.util.Currency;


public class PaymentDetailsActivity extends Activity {

    //private Button payButton;

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
        String service = extras.getString("serviceName");
        serviceName.setText(service);
        //String clientSecret = extras.getString("clientSecret");

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

//        payButton= findViewById(R.id.pay_button);
//        payButton.setOnClickListener(v -> launchPayment(currency, amount, service, clientSecret));
    }

//    private void launchPayment(String currency, Double amount, String service, String clientSecret) {
//        Intent intent = new Intent(this, CheckoutActivity.class);
//        intent.putExtra("clientSecret", clientSecret);
//        intent.putExtra("amount", amount);
//        intent.putExtra("serviceName", service);
//        intent.putExtra("currency", currency);
//        paymentResultLauncher.launch(intent);
//    }
//
//    ActivityResultLauncher<Intent> paymentResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    Intent data = result.getData();
//                    assert data != null;
//                    String returnedResult = data.getDataString();
//
//                    if (returnedResult.equals("result")) {
//                        Snackbar.make(findViewById(android.R.id.content), "Payment Succeeded.", Snackbar.LENGTH_LONG)
//                                .setAction("Action", null).show();
//                        payButton.setVisibility(View.INVISIBLE);
//                    } else {
//                        Snackbar.make(findViewById(android.R.id.content), "Payment Uncompleted.", Snackbar.LENGTH_LONG)
//                                .setAction("Action", null).show();
//                    }
//                }
//            });
}
