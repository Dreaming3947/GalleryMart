package com.gallerymart.app.feature.detail.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.gallerymart.app.MainActivity
import com.gallerymart.app.R
import com.gallerymart.app.databinding.ActivityArtworkDetailBinding
import com.gallerymart.app.feature.detail.vm.DetailViewModel
import kotlinx.coroutines.launch
import java.util.Locale

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
    private var artworkId: String? = null
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtworkDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artworkId = intent.getStringExtra(EXTRA_ID)
        
        // Goi API gia dinh (Structure for real API call)
        if (artworkId != null) {
            fetchArtworkDetails(artworkId!!)
        }

        setupUIWithIntentData()
        setupRecommendations()
        setupListeners()
        observeViewModel()
    }

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

        binding.detailTitle.text = title
        binding.detailMeta.text = getString(R.string.detail_meta, author.uppercase(Locale.getDefault()), year)
        binding.detailArtist.text = author
        binding.detailArtistBio.text = getString(R.string.detail_artist_bio)
        binding.detailArtistStats.text = getString(R.string.detail_artist_stats)
        binding.detailDescription.text = description
        binding.detailMaterialValue.text = material
        binding.detailSizeValue.text = size
        binding.detailBottomPrice.text = price
        binding.detailImage.load(imageUrl)
        binding.detailArtistAvatar.load("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80")
    }

    private fun setupListeners() {
        // Zoom image feature
        binding.detailImage.setOnClickListener {
            val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
            showFullScreenImage(imageUrl)
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnViewAll.setOnClickListener {
            // Quay lai MainActivity va chuyen sang tab Explore
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
        binding.btnFollowArtist.setOnClickListener {
            pressBounce(it)
            showComingSoon()
        }
        binding.btnArtistProfile.setOnClickListener {
            pressBounce(it)
            showComingSoon()
        }
        binding.btnOwnNow.setOnClickListener {
            pressBounce(it)
            artworkId?.let { id ->
                viewModel.confirmOrder(id.toLong())
            } ?: showComingSoon()
        }
    }

    private fun fetchArtworkDetails(id: String) {
        // MAU GOI API (Gia dinh su dung Retrofit da cau hinh san)
        lifecycleScope.launch {
            try {
                // val response = RetrofitClient.artworkApi.getArtworkById(id)
                // if (response.isSuccessful) { updateUI(response.body()) }
            } catch (e: Exception) {
                // Xu ly loi
            }
        }
    }

    private fun showFullScreenImage(url: String?) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialogBinding = com.gallerymart.app.databinding.DialogFullImageBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.fullImageView.load(url)
        
        // CHI NHAN VAO VUNG DEN MOI THOAT
        dialogBinding.fullImageRoot.setOnClickListener {
            dialog.dismiss()
        }
        
        // NHAN VAO ANH THI KHONG THOAT
        dialogBinding.fullImageView.setOnClickListener {
            // Co the them logic Double Tap Zoom o day
            // Toast.makeText(this, "Zoom feature enabled", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

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
        
        // Item 1
        binding.recommendTitle1.text = titles[shuffled[0]]
        binding.recommendPrice1.text = prices[shuffled[0]]
        val img1 = imageUrls.random()
        binding.recommendImage1.load(img1)
        binding.recommendItem1.setOnClickListener {
            navigateToDetail(titles[shuffled[0]], img1, prices[shuffled[0]])
        }

        // Item 2
        binding.recommendTitle2.text = titles[shuffled[1]]
        binding.recommendPrice2.text = prices[shuffled[1]]
        val img2 = imageUrls.random()
        binding.recommendImage2.load(img2)
        binding.recommendItem2.setOnClickListener {
            navigateToDetail(titles[shuffled[1]], img2, prices[shuffled[1]])
        }
    }

    private fun navigateToDetail(title: String, url: String, price: String) {
        val intent = Intent(this, ArtworkDetailActivity::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_IMAGE_URL, url)
            putExtra(EXTRA_PRICE, price)
            putExtra(EXTRA_AUTHOR, "Nghệ sĩ ngẫu nhiên")
        }
        startActivity(intent)
        // Co the finish() neu muon quay lai trang truoc do
    }

    private fun formatPrice(rawPrice: String): String {
        if (rawPrice.isBlank()) return getString(R.string.detail_default_price)
        if (rawPrice.contains("đ") || rawPrice.contains("dTr") || rawPrice.contains("dM")) return rawPrice
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Vo hieu hoa nut bam khi dang xu ly de tranh double click
            viewModel.isLoading.collect { isLoading ->
                binding.btnOwnNow.isEnabled = !isLoading
                binding.btnOwnNow.alpha = if (isLoading) 0.5f else 1.0f
            }
        }

        lifecycleScope.launch {
            viewModel.actionResult.collect { (isSuccess, message) ->
                Toast.makeText(this@ArtworkDetailActivity, message, Toast.LENGTH_SHORT).show()
                if (isSuccess) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}