package com.c17206413.payup.ui.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.c17206413.payup.R
import com.google.firebase.auth.*
import com.stripe.android.*
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.view.BillingAddressFields
import kotlinx.android.synthetic.main.activity_checkout.*
import java.text.NumberFormat
import java.util.*


class CheckoutActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null
    private lateinit var paymentSession: PaymentSession
    private lateinit var selectedPaymentMethod: PaymentMethod
    private val stripe: Stripe by lazy { Stripe(applicationContext, "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        currentUser = FirebaseAuth.getInstance().currentUser

        val extras = intent.extras
        val clientSecret = extras!!.getString("clientSecret")
        val serviceName = extras.getString("serviceName")
        val currency = extras.getString("currency")
        val amount = extras.getDouble("amount")

        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(currency)

        amount_indicator.text = format.format(amount)

        service_name_checkout.text = serviceName
        payButton.isEnabled = false

        setupPaymentSession()

        payButton.setOnClickListener {
            confirmPayment(selectedPaymentMethod.id!!, clientSecret!!)
        }

        paymentmethod.setOnClickListener {
            // Create the customer session and kick start the payment flow
            paymentSession.presentPaymentMethodSelection()
        }
    }

    private fun confirmPayment(paymentMethodId: String, paymentIntentClientSecret: String) {
        payButton.isEnabled = false

        stripe.confirmPayment(this, ConfirmPaymentIntentParams.createWithPaymentMethodId(
                paymentMethodId, paymentIntentClientSecret))
    }

    private fun setupPaymentSession () {
        // Setup Customer Session
        CustomerSession.initCustomerSession(this, FirebaseEphemeralKeyProvider())
        // Setup a payment session
        paymentSession = PaymentSession(this, PaymentSessionConfig.Builder()
                .setShippingInfoRequired(false)
                .setShippingMethodsRequired(false)
                .setBillingAddressFields(BillingAddressFields.None)
                .setPaymentMethodTypes(listOf(PaymentMethod.Type.Card))
                .setShouldShowGooglePay(false)
                .build())

        paymentSession.init(
                object : PaymentSession.PaymentSessionListener {
                    @SuppressLint("SetTextI18n")
                    override fun onPaymentSessionDataChanged(data: PaymentSessionData) {

                        if (data.useGooglePay) {
                            Log.d("PaymentSession", "PaymentMethod GooglePay selected")
                            paymentmethod.text = getString(R.string.pay_with_google)


                        } else {
                            data.paymentMethod?.let {
                                Log.d("PaymentSession", "PaymentMethod $it selected")
                                paymentmethod.text = "${it.card?.brand} card ends with ${it.card?.last4}"
                                selectedPaymentMethod = it
                            }
                        }

                        if (data.isPaymentReadyToCharge) {
                            Log.d("PaymentSession", "Ready to charge")
                            payButton.isEnabled = true
                        }

                        Log.d("PaymentSession", "PaymentSession has changed: $data")
                        Log.d("PaymentSession", "${data.isPaymentReadyToCharge} <> ${data.paymentMethod}")
                    }

                    override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
                        Log.d("PaymentSession", "isCommunicating $isCommunicating")
                    }

                    override fun onError(errorCode: Int, errorMessage: String) {
                        Log.e("PaymentSession", "onError: $errorCode, $errorMessage")
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        paymentSession.handlePaymentData(requestCode, resultCode, data ?: Intent())
    }
}


