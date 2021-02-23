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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IncomingFragment extends Fragment implements PaymentAdapter.PaymentListener{

    private FirebaseFirestore db;

    private List<Payment> mPayments;
    private RecyclerView incomingRecycler;
    private View root;

    private PaymentAdapter paymentAdapter;

    public static IncomingFragment newInstance() {
        return new IncomingFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_incoming, container, false);

        mPayments = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        incomingRecycler = (RecyclerView) root.findViewById(R.id.incomingRecycler);
        incomingRecycler.setHasFixedSize(true);
        incomingRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        readPayments();

        return root;
    }

    private void readPayments() {
        String uid = MainActivity.getUid();
        db.collection("users").document(uid).collection("incoming")
                .whereEqualTo("active", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mPayments.clear();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String serviceName = document.getString("service_name");
                            String currency = document.getString("currency");
                            String name = document.getString("user_name");
                            String clientSecret = document.getString("clientSecret");
                            //TODO (check currency of transaction and convert appropriately)
                            String amount = NumberFormat.getCurrencyInstance().format((Integer.parseInt(document.getString("amount"))/100));
                            String id = document.getId();
                            Payment paymentDetails = new Payment(id, serviceName, currency, name, amount, clientSecret);
                            mPayments.add(paymentDetails);
                        }
                        paymentAdapter = new PaymentAdapter(getActivity(), mPayments, this);
                        incomingRecycler.setAdapter(paymentAdapter);
                    } else {
                        Snackbar.make(root.findViewById(android.R.id.content), "Failed to receive payments incoming.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
    }

    private void launchPayment(Payment paymentDetail) {
        Intent intent = new Intent(getActivity(), PaymentDetailsActivity.class);
        intent.putExtra("clientSecret", paymentDetail.getClientSecret());
        paymentDetailScreenLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> paymentDetailScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String returnedResult = data.getDataString();
                    if (returnedResult.equals("result")) {
                        //TODO (add result)
                    }
                }
            });

    @Override
    public void onPaymentDetailsClick(int position) {
        Payment paymentDetail = mPayments.get(position);
        //TODO (add payment details screen)
    }

    @Override
    public void payButtonOnClick(View v, int adapterPosition) {
        Payment paymentDetail = mPayments.get(adapterPosition);
        launchPayment(paymentDetail);
    }

}