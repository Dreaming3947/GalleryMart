package com.gallerymart.app.feature.detail.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.load
import com.gallerymart.app.MainActivity
import com.gallerymart.app.R
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.data.remote.dto.ArtworkResponseDto
import com.gallerymart.app.data.remote.dto.response.OrderResponseDto
import com.gallerymart.app.data.repository.ArtworkRepository
import com.gallerymart.app.data.repository.OrderRepository
import com.gallerymart.app.databinding.ActivityArtworkDetailBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Hoạt động hiển thị chi tiết một tác phẩm nghệ thuật.
 * Cho phép người dùng xem thông tin, phóng to ảnh, và đặt mua (nếu là người mua).
 */
class ArtworkDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_AUTHOR = "extra_author"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_YEAR = "extra_year"
        const val EXTRA_MATERIAL = "extra_material"
        const val EXTRA_SIZE = "extra_size"
    }

    private lateinit var binding: ActivityArtworkDetailBinding
    private val repository = ArtworkRepository()
    private val orderViewModel = OrderViewModel()
    private var artworkId: Long? = null
    private var artworkSellerId: Long? = null
    private var artworkStatus: String? = null
    private var currentArtworkTitle = ""
    private var currentArtworkPrice = ""
    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtworkDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy ID tác phẩm từ intent truyền vào
        artworkId = extractArtworkIdFromIntent()

        if (artworkId != null) {
            fetchArtworkDetails(artworkId!!)
        }

        setupUIWithIntentData()
        setupRecommendations()
        setupListeners()
    }

    /**
     * Hiển thị dữ liệu sơ bộ từ Intent để giao diện hiện lên ngay lập tức.
     */
    private fun setupUIWithIntentData() {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Tác phẩm" }
        val author = intent.getStringExtra(EXTRA_AUTHOR).orEmpty().ifBlank { "Tác giả" }
        val price = formatPrice(intent.getStringExtra(EXTRA_PRICE).orEmpty())
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty().ifBlank {
            getString(R.string.detail_default_description)
        }
        val year = intent.getStringExtra(EXTRA_YEAR).orEmpty().ifBlank { "1980" }
        val material = intent.getStringExtra(EXTRA_MATERIAL).orEmpty().ifBlank { "Sơn mài" }
        val size = intent.getStringExtra(EXTRA_SIZE).orEmpty().ifBlank { "70x90 cm" }

        currentArtworkTitle = title
        currentArtworkPrice = price
        currentImageUrl = imageUrl

        binding.detailTitle.text = title
        binding.detailMeta.text = getString(R.string.detail_meta, author.uppercase(Locale.getDefault()), year)
        binding.detailArtist.text = author
        binding.detailArtistBio.text = getString(R.string.detail_artist_bio)
        binding.detailArtistStats.text = getString(R.string.detail_artist_stats)
        binding.detailDescription.text = description
        binding.detailMaterialValue.text = material
        binding.detailSizeValue.text = size
        binding.detailBottomPrice.text = price
        binding.detailImage.load(currentImageUrl)
        binding.detailArtistAvatar.load("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80")
    }

    /**
     * Thiết lập các sự kiện tương tác của người dùng.
     */
    private fun setupListeners() {
        // Phóng to ảnh khi nhấn vào
        binding.detailImage.setOnClickListener {
            showFullScreenImage(currentImageUrl)
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnViewAll.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("NAVIGATE_TO", R.id.nav_explore)
            }
            startActivity(intent)
            finish()
        }

        binding.btnTopFavorite.setOnClickListener {
            pressBounce(it)
            showComingSoon()
        }
        
        // Nút mua hàng - chỉ dành cho vai trò BUYER
        binding.btnOwnNow.setOnClickListener {
            pressBounce(it)
            val userRoles = SessionManager.userRoles ?: ""
            val currentUserId = SessionManager.userId
            
            if (artworkId == null) {
                Toast.makeText(this, "Không tìm thấy ID tác phẩm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ngăn chặn tự mua tranh của chính mình
            if (currentUserId != null && artworkSellerId != null && currentUserId == artworkSellerId) {
                Toast.makeText(this, "Bạn không thể mua chính artwork của mình", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (artworkStatus == "SOLD") {
                Toast.makeText(this, "Tác phẩm này đã được bán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!canCurrentUserBuy(userRoles)) {
                Toast.makeText(this, "Chỉ tài khoản người mua mới có thể đặt hàng", Toast.LENGTH_SHORT).show()
            } else {
                // Chuyển sang màn hình Thanh toán
                startActivity(
                    com.gallerymart.app.feature.order.ui.buyer.CheckoutActivity.newIntent(
                        this,
                        artworkId.toString(),
                        currentArtworkTitle,
                        currentArtworkPrice
                    )
                )
            }
        }

        // Lắng nghe trạng thái đặt hàng từ ViewModel
        lifecycleScope.launch {
            orderViewModel.uiState.collect { state ->
                if (!state.orderCreatedMessage.isNullOrBlank()) {
                    Toast.makeText(this@ArtworkDetailActivity, state.orderCreatedMessage, Toast.LENGTH_LONG).show()
                }
                if (!state.errorMessage.isNullOrBlank()) {
                    Toast.makeText(this@ArtworkDetailActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Gọi API lấy thông tin chi tiết đầy đủ của tác phẩm từ server.
     */
    private fun fetchArtworkDetails(id: Long) {
        lifecycleScope.launch {
            try {
                val artwork = repository.getArtworkById(id)
                updateUiFromArtwork(artwork)
            } catch (_: Exception) {
                Toast.makeText(
                    this@ArtworkDetailActivity,
                    "Không tải được chi tiết từ backend, đang giữ dữ liệu hiện tại",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Cập nhật UI sau khi đã lấy được dữ liệu chính xác từ server.
     */
    private fun updateUiFromArtwork(artwork: ArtworkResponseDto) {
        val author = artwork.sellerName.orEmpty().ifBlank { "Unknown artist" }
        val priceText = artwork.price.stripTrailingZeros().toPlainString()
        artworkId = artwork.id
        artworkSellerId = artwork.sellerId
        artworkStatus = artwork.status
        currentImageUrl = artwork.imageUrl

        binding.detailTitle.text = artwork.title
        binding.detailMeta.text = getString(R.string.detail_meta, author.uppercase(Locale.getDefault()), artworkStatus ?: "AVAILABLE")
        binding.detailArtist.text = author
        binding.detailDescription.text = artwork.description.orEmpty().ifBlank {
            getString(R.string.detail_default_description)
        }
        binding.detailBottomPrice.text = formatPrice(priceText)
        binding.detailMaterialValue.text = artwork.category.orEmpty().ifBlank { "Không rõ" }
        binding.detailImage.load(currentImageUrl)

        // Hiển thị thông tin người sở hữu nếu đã bán
        if (artworkStatus == "SOLD" && !artwork.buyerName.isNullOrBlank()) {
            binding.ownerSection.visibility = View.VISIBLE
            binding.tvOwnerName.text = "Đã sở hữu bởi: ${artwork.buyerName}"
            binding.btnOwnNow.isEnabled = false
            binding.btnOwnNow.text = "ĐÃ BÁN"
            binding.btnOwnNow.alpha = 0.5f
        } else {
            binding.ownerSection.visibility = View.GONE
            binding.btnOwnNow.isEnabled = true
            binding.btnOwnNow.text = "MUA NGAY"
            binding.btnOwnNow.alpha = 1.0f
        }
    }

    /**
     * Trích xuất ID từ Intent (hỗ trợ nhiều kiểu dữ liệu).
     */
    private fun extractArtworkIdFromIntent(): Long? {
        val raw = intent.extras?.get(EXTRA_ID)
        return when (raw) {
            is Long -> raw.takeIf { it > 0L }
            is Int -> raw.toLong().takeIf { it > 0L }
            is String -> raw.toLongOrNull()?.takeIf { it > 0L }
            else -> {
                val idFromLongExtra = intent.getLongExtra(EXTRA_ID, -1L)
                if (idFromLongExtra > 0L) idFromLongExtra else null
            }
        }
    }

    /**
     * Kiểm tra người dùng hiện tại có quyền mua hay không.
     */
    private fun canCurrentUserBuy(rolesRaw: String): Boolean {
        if (rolesRaw.isBlank()) return true

        val normalizedRoles = rolesRaw
            .replace("[", "")
            .replace("]", "")
            .split(',', ';', ' ')
            .map { it.trim().uppercase(Locale.getDefault()) }
            .filter { it.isNotBlank() }

        val hasBuyerRole = normalizedRoles.any { it == "BUYER" || it == "ROLE_BUYER" }
        val hasSellerRole = normalizedRoles.any { it == "SELLER" || it == "ROLE_SELLER" }

        if (!hasSellerRole) return true
        return hasBuyerRole
    }

    /**
     * Mở Dialog hiển thị ảnh toàn màn hình.
     */
    private fun showFullScreenImage(url: String?) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialogBinding = com.gallerymart.app.databinding.DialogFullImageBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.fullImageView.load(url)
        
        dialogBinding.fullImageRoot.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Hiển thị danh sách gợi ý (Mock data cho demo).
     */
    private fun setupRecommendations() {
        val titles = listOf("Góc Phố Vắng", "Chiều Đông", "Nắng Thủy Tinh", "Mùa Thu Cho Em", "Hạ Trắng")
        val prices = listOf("18.000.000 đ", "22.500.000 đ", "15.500.000 đ", "30.000.000 đ", "12.000.000 đ")
        val imageUrls = listOf(
            "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&w=600&q=80",
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80",
            "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?auto=format&fit=crop&w=600&q=80",
            "https://images.unsplash.com/photo-1578301978693-85fa9c0320b9?auto=format&fit=crop&w=600&q=80"
        )

        val shuffled = titles.indices.shuffled()
        
        binding.recommendTitle1.text = titles[shuffled[0]]
        binding.recommendPrice1.text = prices[shuffled[0]]
        binding.recommendImage1.load(imageUrls.random())

        binding.recommendTitle2.text = titles[shuffled[1]]
        binding.recommendPrice2.text = prices[shuffled[1]]
        binding.recommendImage2.load(imageUrls.random())
    }

    private fun formatPrice(rawPrice: String): String {
        if (rawPrice.isBlank()) return getString(R.string.detail_default_price)
        if (rawPrice.contains("đ")) return rawPrice
        return getString(R.string.detail_price_suffix, rawPrice)
    }

    private fun showComingSoon() {
        Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
    }

    private fun pressBounce(view: View) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()
    }
}

/**
 * ViewModel quản lý logic đặt hàng tại chỗ (Nếu không dùng CheckoutActivity).
 */
data class OrderUiState(
    val artworkId: Long? = null,
    val note: String = "",
    val order: OrderResponseDto? = null,
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
    val orderCreatedMessage: String? = null,
    val paymentMarkedMessage: String? = null
)

class OrderViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    fun initOrderForm(artworkId: Long, title: String, priceText: String) {
        _uiState.update {
            it.copy(
                artworkId = artworkId,
                note = "",
                errorMessage = null,
                orderCreatedMessage = null,
                paymentMarkedMessage = null
            )
        }
    }

    fun placeOrder() {
        val selectedArtworkId = _uiState.value.artworkId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null, orderCreatedMessage = null) }
            runCatching { repository.createOrder(selectedArtworkId, _uiState.value.note.ifBlank { null }) }
                .onSuccess { order ->
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            order = order,
                            orderCreatedMessage = "Đơn hàng đã được tạo thành công"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isPlacingOrder = false, errorMessage = error.message ?: "Đặt hàng thất bại") }
                }
        }
    }
}
