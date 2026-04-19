package com.gallerymart.app.feature.explore.ui

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
import com.gallerymart.app.R
import com.gallerymart.app.MainActivity
import com.gallerymart.app.core.ui.recycler.GridSpacingItemDecoration
import com.gallerymart.app.databinding.FragmentExploreBinding
import com.gallerymart.app.feature.detail.ui.ArtworkDetailActivity
import com.gallerymart.app.feature.explore.ui.adapter.ExploreArtworkAdapter
import com.gallerymart.app.feature.home.model.ArtworkUiModel
import com.gallerymart.app.feature.home.vm.HomeViewModel
import kotlinx.coroutines.launch

class ExploreFragment : Fragment(R.layout.fragment_explore) {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var artworkAdapter: ExploreArtworkAdapter
    private var allArtworks: List<ArtworkUiModel> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExploreBinding.bind(view)

        artworkAdapter = ExploreArtworkAdapter { item ->
            startActivity(Intent(requireContext(), ArtworkDetailActivity::class.java).apply {
                putExtra(ArtworkDetailActivity.EXTRA_TITLE, item.title)
                putExtra(ArtworkDetailActivity.EXTRA_AUTHOR, item.author)
                putExtra(ArtworkDetailActivity.EXTRA_PRICE, item.priceText)
                putExtra(ArtworkDetailActivity.EXTRA_IMAGE_URL, item.imageUrl)
                putExtra(ArtworkDetailActivity.EXTRA_DESCRIPTION, "Chi tiet tac pham tu man Kham pha")
                putExtra(ArtworkDetailActivity.EXTRA_YEAR, "1980")
                putExtra(ArtworkDetailActivity.EXTRA_MATERIAL, "Son mai")
                putExtra(ArtworkDetailActivity.EXTRA_SIZE, "70x90 cm")
            })
        }

        binding.exploreRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = artworkAdapter
            itemAnimator = null
            if (itemDecorationCount == 0) {
                val spacing = resources.getDimensionPixelSize(R.dimen.gm_grid_spacing)
                addItemDecoration(GridSpacingItemDecoration(2, spacing, includeEdge = false))
            }
        }

        binding.exploreSearchInput.addTextChangedListener { editable ->
            applyFilter(editable?.toString().orEmpty())
        }

        binding.btnMenu.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_home)
        }
        binding.btnCart.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_profile)
        }
        binding.btnTune.setOnClickListener { showComingSoon() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.exploreLoadingFooter.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    allArtworks = state.artworks
                    applyFilter(binding.exploreSearchInput.text?.toString().orEmpty())
                }
            }
        }

        viewModel.loadArtworks()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
}

