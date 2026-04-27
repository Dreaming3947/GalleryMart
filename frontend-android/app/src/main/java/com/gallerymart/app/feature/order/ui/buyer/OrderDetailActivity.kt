package com.gallerymart.app.feature.order.ui.buyer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gallerymart.app.R
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import com.gallerymart.app.databinding.ActivityOrderDetailBinding
import com.gallerymart.app.databinding.LayoutPaymentConfirmationBottomSheetBinding
import com.gallerymart.app.feature.order.vm.OrderUiState
import com.gallerymart.app.feature.order.vm.OrderViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private val viewModel: OrderViewModel = OrderViewModel()
    private var orderId: Long = -1
    private var paymentDialog: BottomSheetDialog? = null
    private var bottomSheetBinding: LayoutPaymentConfirmationBottomSheetBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1)

        setupListeners()
        observeViewModel()

        viewModel.getOrderDetails(orderId)
    }

    private fun setupListeners() {
        binding.btnCallShipper.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789"))
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailState.collect { state ->
                    // Handle main order details
                    when (val orderState = state.orderState) {
                        is OrderUiState.Success -> {
                            displayOrderDetails(orderState.data)
                        }
                        is OrderUiState.Error -> {
                            Toast.makeText(this@OrderDetailActivity, orderState.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }

                    // Handle payment action progress and success
                    val paymentState = state.paymentActionState
                    bottomSheetBinding?.let { bs ->
                        when (paymentState) {
                            is OrderUiState.Loading -> {
                                bs.btnConfirmPayment.visibility = View.GONE
                                bs.paymentProgressBar.visibility = View.VISIBLE
                            }
                            is OrderUiState.Success -> {
                                bs.paymentProgressBar.visibility = View.GONE
                                bs.layoutConfirmation.visibility = View.GONE
                                bs.layoutSuccess.visibility = View.VISIBLE
                                // Update status in background activity
                                viewModel.getOrderDetails(orderId)
                            }
                            is OrderUiState.Error -> {
                                bs.btnConfirmPayment.visibility = View.VISIBLE
                                bs.paymentProgressBar.visibility = View.GONE
                                Toast.makeText(this@OrderDetailActivity, paymentState.message, Toast.LENGTH_LONG).show()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun displayOrderDetails(order: OrderResponseDto) {
        binding.tvOrderDetailId.text = "Đơn hàng #ORD-${order.id}"
        binding.tvOrderDate.text = "Ngày đặt: ${order.createdAt?.take(10) ?: "N/A"}"
        
        updateTimeline(order.status)

        // Only show payment sheet if order is pending and not already shown
        if (order.status.uppercase() == "PENDING" && paymentDialog == null) {
            showPaymentConfirmationBottomSheet()
        }
    }

    private fun updateTimeline(status: String) {
        // Reset colors
        val colorDivider = getColor(R.color.divider)
        val colorBlack = getColor(R.color.black)

        binding.dot1.setBackgroundResource(R.drawable.shape_dot_black)
        binding.line1.setBackgroundColor(colorDivider)
        binding.dot2.setBackgroundResource(R.drawable.shape_dot_gray)
        binding.line2.setBackgroundColor(colorDivider)
        binding.dot3.setBackgroundResource(R.drawable.shape_dot_gray)

        when (status.uppercase()) {
            "PENDING" -> {}
            "CONFIRMED", "PAYMENT_SENT" -> {
                binding.line1.setBackgroundColor(colorBlack)
                binding.dot2.setBackgroundResource(R.drawable.shape_dot_black)
            }
            "DELIVERING", "COMPLETED", "SOLD" -> {
                binding.line1.setBackgroundColor(colorBlack)
                binding.dot2.setBackgroundResource(R.drawable.shape_dot_black)
                binding.line2.setBackgroundColor(colorBlack)
                binding.dot3.setBackgroundResource(R.drawable.shape_dot_black)
            }
        }
    }

    private fun showPaymentConfirmationBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val bsBinding = LayoutPaymentConfirmationBottomSheetBinding.inflate(layoutInflater)
        this.bottomSheetBinding = bsBinding
        this.paymentDialog = dialog
        
        dialog.setContentView(bsBinding.root)

        bsBinding.btnConfirmPayment.setOnClickListener {
            viewModel.markPaymentSent(orderId)
        }

        bsBinding.btnBackToHome.setOnClickListener {
            dialog.dismiss()
            navigateToHome()
        }

        bsBinding.btnFinish.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        bsBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            this.paymentDialog = null
            this.bottomSheetBinding = null
        }

        dialog.show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, com.gallerymart.app.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val EXTRA_ORDER_ID = "extra_order_id"

        fun newIntent(context: Context, orderId: Long): Intent {
            return Intent(context, OrderDetailActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }
}
