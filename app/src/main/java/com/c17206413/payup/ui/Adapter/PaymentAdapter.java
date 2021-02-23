package com.c17206413.payup.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.Model.PaymentDetails;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private final Context mContext;
    private final List<PaymentDetails> mPayments;
    private final PaymentDetailsListener paymentDetailsListener;

    public PaymentAdapter(Context mContext, List<PaymentDetails> mPayments, PaymentDetailsListener paymentDetailsListener) {
        this.mPayments = mPayments;
        this.mContext = mContext;
        this.paymentDetailsListener = paymentDetailsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.payment_item, parent, false);
        return new ViewHolder(view, paymentDetailsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentDetails paymentDetails = mPayments.get(position);
        holder.price.setText(paymentDetails.getAmount());
        holder.userName.setText(paymentDetails.getUsername());
        holder.serviceName.setText(paymentDetails.getServiceName());
    }

    @Override
    public int getItemCount() {
        return mPayments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView serviceName;
        public TextView price;
        public TextView userName;
        public PaymentDetailsListener PaymentDetailsListener;

        public ViewHolder(View itemView, PaymentDetailsListener paymentDetailsListener) {
            super(itemView);

            this.PaymentDetailsListener = paymentDetailsListener;

            serviceName = itemView.findViewById(R.id.service_name);
            price = itemView.findViewById(R.id.price);
            userName = itemView.findViewById(R.id.user_id);

            itemView.setOnClickListener(this);

            Button payButton = (Button) itemView.findViewById(R.id.pay_button);
            payButton.setOnClickListener(v -> PaymentDetailsListener.payButtonOnClick(v, getAdapterPosition()));
        }

        @Override
        public void onClick(View v) {
            PaymentDetailsListener.onPaymentDetailsClick(getAdapterPosition());
        }
    }

    public interface PaymentDetailsListener {
        void onPaymentDetailsClick(int position);

        void payButtonOnClick(View v, int adapterPosition);
    }
}
