package com.c17206413.payup.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.c17206413.payup.MainActivity;
import com.c17206413.payup.R;
import com.c17206413.payup.ui.adapter.PaymentAdapter;
import com.c17206413.payup.ui.model.Payment;
import com.c17206413.payup.ui.payment.PaymentDetailsActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class IncomingFragment extends Fragment implements PaymentAdapter.PaymentListener{

    private static final String TAG = "Incoming:";

    private List<Payment> mPayments;
    private RecyclerView incomingRecycler;
    private View root;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout pullToRefresh;

    private PaymentAdapter paymentAdapter;

    public static IncomingFragment newInstance() {
        return new IncomingFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment, container, false);

        mPayments = new ArrayList<>();

        incomingRecycler = root.findViewById(R.id.paymentRecycler);
        incomingRecycler.setHasFixedSize(true);
        incomingRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAuth = FirebaseAuth.getInstance();

        pullToRefresh = root.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            pullToRefresh.setRefreshing(true);
            readPayments();
            pullToRefresh.setRefreshing(false);
        });
        return root;
    }

    public void onResume() {
        super.onResume();
        readPayments();
    }

    private void readPayments() {
        mPayments.clear();
        if ( mAuth.getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = MainActivity.getCurrentUser().getId();
            if (uid != null) {
                db.collection("users").document(uid).collection("incoming")
                        .whereEqualTo("active", true)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    String serviceName = document.getString("service_name");
                                    Currency currency = Currency.getInstance(document.getString("currency"));
                                    String name = document.getString("user_name");
                                    String clientSecret = document.getString("clientSecret");
                                    Double amount = Double.parseDouble(Objects.requireNonNull(document.getString("amount"))) / 100;
                                    String id = document.getId();
                                    String dateCreated = document.getString("date_created");
                                    String datePaid = document.getString("date_paid");
                                    String paymentMethod = document.getString("payment_method");
                                    Payment paymentDetails = new Payment(id, serviceName, currency, name, amount, clientSecret, "incoming", true, dateCreated, datePaid, paymentMethod);
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
        }
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
        intent.putExtra("dateCreated", paymentDetail.getDateCreated());
        intent.putExtra("datePaid", paymentDetail.getDatePaid());
        intent.putExtra("paymentMethod", paymentDetail.getPaymentMethod());
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
        Payment paymentDetail = mPayments.get(adapterPosition);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document( MainActivity.getCurrentUser().getId()).collection("incoming").document(paymentDetail.getId());
        userRef.update("active", false)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy\nHH:mm z");
        String currentDateAndTime = sdf.format(new Date());
        userRef.update("date_paid", currentDateAndTime)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

        userRef.update("payment_method", "Outside App")
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        readPayments();
    }

}