package com.gallerymart.app.feature.order.ui.buyer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gallerymart.app.databinding.ActivityCheckoutBinding
import com.gallerymart.app.feature.order.vm.OrderUiState
import com.gallerymart.app.feature.order.vm.OrderViewModel
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val viewModel: OrderViewModel = OrderViewModel()

    private var artworkId: String = ""
    private var price: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artworkId = intent.getStringExtra(EXTRA_ARTWORK_ID) ?: ""
        price = intent.getStringExtra(EXTRA_PRICE) ?: "0 đ"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""

        setupUI(title, price)
        observeViewModel()
    }

    private fun setupUI(title: String, price: String) {
        binding.tvArtworkTitle.text = title
        binding.tvArtworkPrice.text = price
        binding.tvTotalAmount.text = price

        binding.btnCheckout.setOnClickListener {
            if (artworkId.isNotEmpty()) {
                val parsedArtworkId = artworkId.toLongOrNull()
                if (parsedArtworkId == null) {
                    Toast.makeText(this, "Lỗi: ID tác phẩm không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                confirmAndCreateOrder(parsedArtworkId)
            } else {
                Toast.makeText(this, "Lỗi: Không tìm thấy ID tác phẩm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmAndCreateOrder(artworkId: Long) {
        val note = binding.etOrderNote.text?.toString()?.trim().orEmpty().ifBlank { null }
        if ((note?.length ?: 0) > 1000) {
            Toast.makeText(this, "Ghi chu toi da 1000 ky tu", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Xác nhận đặt hàng")
            .setMessage("Bạn có chắc muốn đặt đơn cho tác phẩm này?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Đặt ngay") { _, _ ->
                viewModel.createOrder(artworkId, note)
            }
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.checkoutState.collect { state ->
                    when (state) {
                        is OrderUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnCheckout.isEnabled = false
                        }
                        is OrderUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@CheckoutActivity, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                            startActivity(OrderDetailActivity.newIntent(this@CheckoutActivity, state.data.id))
                            finish()
                        }
                        is OrderUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnCheckout.isEnabled = true
                            Toast.makeText(this@CheckoutActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_ARTWORK_ID = "extra_artwork_id"
        private const val EXTRA_PRICE = "extra_price"
        private const val EXTRA_TITLE = "extra_title"

        fun newIntent(context: Context, artworkId: String, title: String, price: String): Intent {
            return Intent(context, CheckoutActivity::class.java).apply {
                putExtra(EXTRA_ARTWORK_ID, artworkId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_PRICE, price)
            }
        }
    }
}
