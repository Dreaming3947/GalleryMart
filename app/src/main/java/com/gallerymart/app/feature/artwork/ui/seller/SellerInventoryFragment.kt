package com.gallerymart.app.feature.artwork.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gallerymart.app.R
import com.gallerymart.app.core.network.RetrofitProvider
import com.gallerymart.app.core.util.Resource
import com.gallerymart.app.data.remote.api.ArtworkApi
import com.gallerymart.app.data.repository.ArtworkRepositoryImpl
import com.gallerymart.app.databinding.FragmentSellerInventoryBinding
import com.gallerymart.app.feature.artwork.vm.seller.SellerArtworkViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SellerInventoryFragment : Fragment() {

    private var _binding: FragmentSellerInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SellerArtworkViewModel
    private lateinit var adapter: SellerInventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        
        viewModel.getMyArtworks()

        binding.fabAddArtwork.setOnClickListener {
            findNavController().navigate(R.id.action_sellerInventoryFragment_to_addArtworkFragment)
        }
    }

    private fun setupViewModel() {
        // Khởi tạo manual cho mục đích demo (Thực tế nên dùng Hilt)
        val api = RetrofitProvider.createService(ArtworkApi::class.java)
        val repository = ArtworkRepositoryImpl(api)
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SellerArtworkViewModel(repository) as T
            }
        })[SellerArtworkViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = SellerInventoryAdapter(
            onEditClick = { artwork ->
                val bundle = Bundle().apply {
                    putParcelable("artwork", artwork)
                }
                findNavController().navigate(R.id.action_sellerInventoryFragment_to_addArtworkFragment, bundle)
            },
            onDeleteClick = { artwork ->
                artwork.id?.let { viewModel.deleteArtwork(it) }
            }
        )
        binding.rvArtworks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SellerInventoryFragment.adapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.myArtworks.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        adapter.submitList(resource.data)
                        binding.tvTotalArtworks.text = resource.data?.size.toString()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
