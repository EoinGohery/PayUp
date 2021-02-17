package com.c17206413.payup.ui.payment

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.c17206413.payup.R
import com.c17206413.payup.ui.Adapter.UserAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stripe.android.Stripe
import kotlinx.android.synthetic.main.activity_create_payment.*

class OldCreatePaymentActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null
    private val stripe: Stripe by lazy { Stripe(applicationContext, "pk_test_51HnPJaAXocUznruHqwf1wdNuZeIEEkX9ODwT0yuhtsv9nFPoghcpWbRLDcq3GU0k7g3RlPwCQGhCHVcMPe9nmoqB00JWK66tDF") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_payment)

        currentUser = FirebaseAuth.getInstance().currentUser


    }

    private fun createPayment(currency: String, amount: Float) {

        val paymentCollection = Firebase.firestore
                .collection("users")
                .document(currentUser?.uid ?: "")
                .collection("payments")

        // Add a new document with a generated ID
        paymentCollection.add(hashMapOf(
                "amount" to amount,
                "currency" to currency
            )).addOnSuccessListener { documentReference ->
                Log.d("payment", "DocumentSnapshot added with ID: ${documentReference.id}")
                documentReference.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("payment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d("payment", "Current data: ${snapshot.data}")
                        val clientSecret = snapshot.data?.get("client_secret")
                        Log.d("payment", "Create paymentIntent returns $clientSecret")
                        clientSecret?.let {
                        //TODO Create Document with payment details and client secret on beneficiary side
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Snackbar.make(findViewById(android.R.id.content), "Payment Failed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                Log.w("payment", "Error adding document", e)
            }
    }

}