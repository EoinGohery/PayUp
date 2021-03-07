package com.c17206413.payup.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.MainActivity;
import com.c17206413.payup.R;
import com.c17206413.payup.ui.Adapter.PaymentAdapter;
import com.c17206413.payup.ui.Model.Payment;
import com.c17206413.payup.ui.view.PaymentDetailsActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment implements PaymentAdapter.PaymentListener{

    private FirebaseFirestore db;

    private List<Payment> mPayments;
    private RecyclerView historyRecycler;
    private View root;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_history, container, false);

        mPayments = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        historyRecycler = root.findViewById(R.id.historyRecycler);
        historyRecycler.setHasFixedSize(true);
        historyRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        readPayments();

        return root;
    }

    private void readPayments() {
        mPayments.clear();
        String uid = MainActivity.getUid();
        db.collection("users").document(uid).collection("due")
                .whereEqualTo("active", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String serviceName = document.getString("service_name");
                            Currency currency = Currency.getInstance(document.getString("currency"));
                            String name = document.getString("user_name");
                            String clientSecret = document.getString("clientSecret");
                            Double amount = Double.parseDouble(document.getString("amount"))/100;
                            String id = document.getId();
                            Payment paymentDetails = new Payment(id, serviceName, currency, name, amount, clientSecret, "due", false);
                            addToRecycler(paymentDetails);
                        }

                    } else {
                        Snackbar.make(root.findViewById(android.R.id.content), "Failed to receive payments due.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
        db.collection("users").document(uid).collection("incoming")
                .whereEqualTo("active", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String serviceName = document.getString("service_name");
                            Currency currency = Currency.getInstance(document.getString("currency"));
                            String name = document.getString("user_name");
                            String clientSecret = document.getString("clientSecret");
                            Double amount = Double.parseDouble(document.getString("amount"))/100;
                            String id = document.getId();
                            Payment paymentDetails = new Payment(id, serviceName, currency, name, amount, clientSecret, "incoming", false);
                            addToRecycler(paymentDetails);
                        }
                    } else {
                        Snackbar.make(root.findViewById(android.R.id.content), "Failed to receive payments incoming.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });

    }

    private void addToRecycler(Payment payment) {
        mPayments.add(payment);
        PaymentAdapter paymentAdapter = new PaymentAdapter(getActivity(), mPayments, this);
        historyRecycler.setAdapter(paymentAdapter);
    }

    private void viewPayment(Payment paymentDetail) {
        Intent intent = new Intent(getActivity(), PaymentDetailsActivity.class);
        intent.putExtra("clientSecret", paymentDetail.getClientSecret());
        intent.putExtra("amount", paymentDetail.getAmount());
        intent.putExtra("serviceName", paymentDetail.getServiceName());
        intent.putExtra("id", paymentDetail.getId());
        intent.putExtra("active", paymentDetail.getActive());
        intent.putExtra("user", paymentDetail.getUsername());
        intent.putExtra("currency", paymentDetail.getCurrency().getCurrencyCode());
        paymentDetailScreenLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> paymentDetailScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    readPayments();
                }
            });

    @Override
    public void onPaymentDetailsClick(int position) {
        Payment paymentDetail = mPayments.get(position);
        viewPayment(paymentDetail);
    }

    @Override
    public void payButtonOnClick(View v, int adapterPosition) {
        //Pay button is disabled as payment has already been complete
    }


}