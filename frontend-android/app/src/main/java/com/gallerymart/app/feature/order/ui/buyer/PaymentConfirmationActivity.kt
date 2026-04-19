package com.gallerymart.app.feature.order.ui.buyer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gallerymart.app.databinding.ActivityPaymentConfirmationBinding

class PaymentConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1)
        val amount = intent.getStringExtra(EXTRA_AMOUNT) ?: ""

        setupUI(orderId, amount)
    }

    private fun setupUI(orderId: Long, amount: String) {
        binding.tvOrderInfo.text = "Mã đơn hàng: #$orderId"
        binding.tvPaymentAmount.text = amount

        binding.btnDone.setOnClickListener {
            startActivity(OrderListActivity.newIntent(this))
            finishAffinity() 
        }
        
        binding.btnViewDetail.setOnClickListener {
            // Logic to view order detail
            finish()
        }
    }

    companion object {
        private const val EXTRA_ORDER_ID = "extra_order_id"
        private const val EXTRA_AMOUNT = "extra_amount"

        fun newIntent(context: Context, orderId: Long, amount: String): Intent {
            return Intent(context, PaymentConfirmationActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
                putExtra(EXTRA_AMOUNT, amount)
            }
        }
    }
}
