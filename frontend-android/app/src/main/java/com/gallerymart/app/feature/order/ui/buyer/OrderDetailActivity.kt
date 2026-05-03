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

/**
 * Màn hình chi tiết đơn hàng cho Người mua.
 * Xử lý hiển thị trạng thái đơn hàng và quy trình xác nhận thanh toán.
 */
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

        // Lấy ID đơn hàng được truyền từ màn hình trước
        orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1)

        setupListeners()
        observeViewModel()

        // Khởi tạo tải dữ liệu chi tiết đơn hàng
        viewModel.getOrderDetails(orderId)
    }

    private fun setupListeners() {
        binding.btnCallShipper.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789"))
            startActivity(intent)
        }
    }

    /**
     * Lắng nghe sự thay đổi trạng thái từ ViewModel.
     * Sử dụng StateFlow để cập nhật UI thời gian thực.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailState.collect { state ->
                    // Xử lý dữ liệu đơn hàng thành công
                    when (val orderState = state.orderState) {
                        is OrderUiState.Success -> {
                            displayOrderDetails(orderState.data)
                        }
                        is OrderUiState.Error -> {
                            Toast.makeText(this@OrderDetailActivity, orderState.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }

                    // Xử lý trạng thái khi người dùng nhấn "Xác nhận thanh toán"
                    val paymentState = state.paymentActionState
                    bottomSheetBinding?.let { bs ->
                        when (paymentState) {
                            is OrderUiState.Loading -> {
                                // Hiển thị vòng xoay chờ khi đang gọi API
                                bs.btnConfirmPayment.visibility = View.GONE
                                bs.paymentProgressBar.visibility = View.VISIBLE
                            }
                            is OrderUiState.Success -> {
                                // Ẩn loading và hiện màn hình thành công
                                bs.paymentProgressBar.visibility = View.GONE
                                bs.layoutConfirmation.visibility = View.GONE
                                bs.layoutSuccess.visibility = View.VISIBLE
                                // Cập nhật lại dữ liệu nền sau khi thanh toán thành công
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

    /**
     * Hiển thị thông tin lên giao diện.
     */
    private fun displayOrderDetails(order: OrderResponseDto) {
        binding.tvOrderDetailId.text = "Đơn hàng #ORD-${order.id}"
        binding.tvOrderDate.text = "Ngày đặt: ${order.createdAt?.take(10) ?: "N/A"}"
        
        updateTimeline(order.status)

        // Tự động mở BottomSheet thanh toán nếu đơn hàng đang chờ (Pending)
        if (order.status.uppercase() == "PENDING" && paymentDialog == null) {
            showPaymentConfirmationBottomSheet()
        }
    }

    /**
     * Cập nhật tiến trình (Timeline) của đơn hàng dựa trên trạng thái.
     */
    private fun updateTimeline(status: String) {
        val colorDivider = getColor(R.color.divider)
        val colorBlack = getColor(R.color.black)

        // Trạng thái mặc định: Đã đặt hàng
        binding.dot1.setBackgroundResource(R.drawable.shape_dot_black)
        
        when (status.uppercase()) {
            "PENDING" -> {
                binding.line1.setBackgroundColor(colorDivider)
                binding.dot2.setBackgroundResource(R.drawable.shape_dot_gray)
            }
            "CONFIRMED", "PAYMENT_SENT" -> {
                // Đã thanh toán hoặc đã xác nhận
                binding.line1.setBackgroundColor(colorBlack)
                binding.dot2.setBackgroundResource(R.drawable.shape_dot_black)
            }
            "DELIVERING", "COMPLETED", "SOLD" -> {
                // Đang giao hoặc hoàn tất
                binding.line1.setBackgroundColor(colorBlack)
                binding.dot2.setBackgroundResource(R.drawable.shape_dot_black)
                binding.line2.setBackgroundColor(colorBlack)
                binding.dot3.setBackgroundResource(R.drawable.shape_dot_black)
            }
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận thanh toán dưới dạng BottomSheet.
     */
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
