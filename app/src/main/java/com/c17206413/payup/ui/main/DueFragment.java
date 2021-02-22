package com.c17206413.payup.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.payment.CheckoutActivity;

public class DueFragment extends Fragment {

    private String paymentIntentClientSecret = "";

    public static DueFragment newInstance() {
        DueFragment fragment = new DueFragment();

        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_due, container, false);

        Button expenseActivityButton = root.findViewById(R.id.button2);
        expenseActivityButton.setOnClickListener(v -> launchPayment());


        return root;
    }

    private void launchPayment() {
        Intent intent = new Intent(getActivity(), CheckoutActivity.class);
        intent.putExtra("paymentIntentClientSecret", paymentIntentClientSecret);
        paymentResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> paymentResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String returnedResult = data.getDataString();
                    if (returnedResult.equals("result")) {
                        //TODO
                    }
                }
            });
}