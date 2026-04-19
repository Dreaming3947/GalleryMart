package com.gallerymart.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.gallerymart.app.R
import com.gallerymart.app.MainActivity
import com.gallerymart.app.core.ui.recycler.GridSpacingItemDecoration
import com.gallerymart.app.databinding.FragmentHomeBinding
import com.gallerymart.app.feature.detail.ui.ArtworkDetailActivity
import com.gallerymart.app.feature.home.model.ArtworkUiModel
import com.gallerymart.app.feature.home.ui.adapter.ArtworkAdapter
import com.gallerymart.app.feature.home.vm.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var artworkAdapter: ArtworkAdapter
    private var allArtworks: List<ArtworkUiModel> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val boundView = runCatching { FragmentHomeBinding.bind(view) }
            .getOrElse { return }
        _binding = boundView

        runCatching {
            artworkAdapter = ArtworkAdapter { item ->
                startActivity(Intent(requireContext(), ArtworkDetailActivity::class.java).apply {
                    putExtra(ArtworkDetailActivity.EXTRA_ID, item.id)
                    putExtra(ArtworkDetailActivity.EXTRA_TITLE, item.title)
                    putExtra(ArtworkDetailActivity.EXTRA_AUTHOR, item.author)
                    putExtra(ArtworkDetailActivity.EXTRA_PRICE, item.priceText)
                    putExtra(ArtworkDetailActivity.EXTRA_IMAGE_URL, item.imageUrl)
                    putExtra(ArtworkDetailActivity.EXTRA_DESCRIPTION, "Tac pham nghe thuat noi bat tu GalleryMart")
                        putExtra(ArtworkDetailActivity.EXTRA_YEAR, "1980")
                        putExtra(ArtworkDetailActivity.EXTRA_MATERIAL, "Son mai")
                        putExtra(ArtworkDetailActivity.EXTRA_SIZE, "70x90 cm")
                })
            }

            binding.featuredRecycler.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = artworkAdapter
                if (itemDecorationCount == 0) {
                    val spacing = resources.getDimensionPixelSize(R.dimen.gm_grid_spacing)
                    addItemDecoration(GridSpacingItemDecoration(2, spacing, includeEdge = false))
                }
            }

            binding.searchInput.addTextChangedListener { editable ->
                applyFilter(editable?.toString().orEmpty())
            }

            binding.btnExploreGallery.setOnClickListener {
                (activity as? MainActivity)?.navigateToTab(R.id.nav_explore)
            }
            binding.btnSellArt.setOnClickListener {
                (activity as? MainActivity)?.navigateToTab(R.id.nav_profile)
            }
            binding.btnUploadArtwork.setOnClickListener { showComingSoon() }
            binding.btnBecomeSeller.setOnClickListener { showComingSoon() }
            binding.btnHomeTune.setOnClickListener { showComingSoon() }

            binding.heroImage.load("https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?auto=format&fit=crop&w=1200&q=80")

            observeState()
            viewModel.loadArtworks()
        }.onFailure {
            binding.errorView.visibility = View.VISIBLE
            binding.errorView.text = getString(R.string.home_load_failed)
            binding.loadingView.visibility = View.GONE
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loadingView.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.errorView.visibility = if (state.errorMessage.isNullOrBlank()) View.GONE else View.VISIBLE
                    binding.errorView.text = state.errorMessage
                    allArtworks = state.artworks
                    applyFilter(binding.searchInput.text?.toString().orEmpty())
                }
            }
        }
    }

    private fun applyFilter(query: String) {
        val keyword = query.trim().lowercase()
        if (keyword.isBlank()) {
            artworkAdapter.submitList(allArtworks)
            return
        }
        val filtered = allArtworks.filter {
            it.title.lowercase().contains(keyword) || it.author.lowercase().contains(keyword)
        }
        artworkAdapter.submitList(filtered)
    }

    private fun showComingSoon() {
        Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

