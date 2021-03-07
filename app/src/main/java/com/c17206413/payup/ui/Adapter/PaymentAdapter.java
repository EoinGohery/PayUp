package com.c17206413.payup.ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.Model.Payment;

import java.text.NumberFormat;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Payment> mPayments;
    private final PaymentListener paymentListener;

    public PaymentAdapter(Context mContext, List<Payment> mPayments, PaymentListener paymentListener) {
        this.mPayments = mPayments;
        this.mContext = mContext;
        this.paymentListener = paymentListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.payment_item, parent, false);
        return new ViewHolder(view, paymentListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment paymentDetails = mPayments.get(position);
        holder.price.setText(NumberFormat.getCurrencyInstance().format(paymentDetails.getAmount()));
        holder.userName.setText(paymentDetails.getUsername());
        holder.serviceName.setText(paymentDetails.getServiceName());
        if (paymentDetails.getType().equals("due")) {
            holder.payButton.setText(R.string.app_name);
            holder.indicator.setVisibility(View.INVISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_arrow_down);
        } else if (paymentDetails.getType().equals("incoming")) {
            holder.payButton.setText(R.string.cancel);
            holder.indicator.setVisibility(View.INVISIBLE);
            holder.indicator.setImageResource(R.drawable.ic_arrow_up);
        }
        if (!paymentDetails.getActive()) {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.payButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mPayments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView serviceName;
        public TextView price;
        public TextView userName;
        public PaymentListener PaymentListener;
        public Button payButton;
        public ImageView indicator;

        public ViewHolder(View itemView, PaymentListener paymentListener) {
            super(itemView);

            this.PaymentListener = paymentListener;

            serviceName = itemView.findViewById(R.id.service_name);
            price = itemView.findViewById(R.id.price);
            userName = itemView.findViewById(R.id.user_id);
            indicator = itemView.findViewById(R.id.in_out_indicator);

            itemView.setOnClickListener(this);

            payButton = itemView.findViewById(R.id.pay_button);
            payButton.setOnClickListener(v -> PaymentListener.payButtonOnClick(v, getAdapterPosition()));

        }

        @Override
        public void onClick(View v) {
            PaymentListener.onPaymentDetailsClick(getAdapterPosition());
        }
    }

    public interface PaymentListener {
        void onPaymentDetailsClick(int position);

        void payButtonOnClick(View v, int adapterPosition);
    }
}
