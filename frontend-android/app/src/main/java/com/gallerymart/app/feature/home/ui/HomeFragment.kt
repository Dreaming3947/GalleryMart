package com.gallerymart.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.gallerymart.app.R
import com.gallerymart.app.MainActivity
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.core.ui.recycler.GridSpacingItemDecoration
import com.gallerymart.app.data.remote.dto.NotificationResponseDto
import com.gallerymart.app.data.repository.AuthRepository
import com.gallerymart.app.data.repository.NotificationRepository
import com.gallerymart.app.databinding.FragmentHomeBinding
import com.gallerymart.app.feature.detail.ui.ArtworkDetailActivity
import com.gallerymart.app.feature.home.model.ArtworkUiModel
import com.gallerymart.app.feature.home.ui.adapter.ArtworkAdapter
import com.gallerymart.app.feature.home.vm.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private lateinit var artworkAdapter: ArtworkAdapter
    private var allArtworks: List<ArtworkUiModel> = emptyList()
    private val authRepository = AuthRepository()

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
            binding.btnUploadArtwork.setOnClickListener {
                (activity as? MainActivity)?.navigateToTab(R.id.nav_profile)
            }
            binding.btnBecomeSeller.setOnClickListener {
                enableSellerRole()
            }
            binding.btnHomeTune.setOnClickListener { showComingSoon() }

            binding.heroImage.load("https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?auto=format&fit=crop&w=1200&q=80")

            // Setup notification bell
            val notificationBellContainer = view.findViewById<FrameLayout>(R.id.notificationBellContainer)
            val notificationBadge = view.findViewById<TextView>(R.id.notificationBadge)
            
            notificationBellContainer?.setOnClickListener {
                // Navigate to notification center
                (activity as? MainActivity)?.navigateToNotificationCenter()
            }

            // Observe notification unread count
            viewLifecycleOwner.lifecycleScope.launch {
                notificationViewModel.uiState.collect { state ->
                    // Update badge visibility and text
                    if (state.unreadCount > 0) {
                        notificationBadge?.text = state.unreadCount.toString()
                        notificationBadge?.visibility = View.VISIBLE
                    } else {
                        notificationBadge?.visibility = View.GONE
                    }
                }
            }

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

    private fun enableSellerRole() {
        if (SessionManager.accessToken.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { authRepository.enableSellerRole() }
                .onSuccess {
                    Toast.makeText(requireContext(), "Seller role enabled", Toast.LENGTH_SHORT).show()
                }
                .onFailure { error ->
                    Toast.makeText(requireContext(), error.message ?: "Enable seller failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

data class NotificationCenterUiState(
    val notifications: List<NotificationResponseDto> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationCenterUiState(isLoading = true))
    val uiState: StateFlow<NotificationCenterUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            runCatching { repository.getMyNotifications() }
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = list,
                            unreadCount = list.count { item -> !item.isRead }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Load notifications failed"
                        )
                    }
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAllRead = true, errorMessage = null, successMessage = null) }
            runCatching { repository.markAllRead() }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isMarkingAllRead = false,
                            notifications = it.notifications.map { n -> n.copy(isRead = true) },
                            unreadCount = 0,
                            successMessage = "Marked $updated notifications as read"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isMarkingAllRead = false,
                            errorMessage = error.message ?: "Mark all read failed"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

class NotificationCenterFragment : Fragment() {
    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val backButton = Button(context).apply { text = "Back" }
        val markReadButton = Button(context).apply { text = "Mark all read" }
        val unreadText = TextView(context).apply { text = "Unread: 0" }
        val loading = ProgressBar(context).apply { visibility = View.GONE }
        val error = TextView(context).apply { visibility = View.GONE }
        val listView = ListView(context)
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        root.addView(backButton)
        root.addView(markReadButton)
        root.addView(unreadText)
        root.addView(loading)
        root.addView(error)
        root.addView(listView)

        backButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_home)
        }

        markReadButton.setOnClickListener {
            viewModel.markAllAsRead()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    unreadText.text = "Unread: ${state.unreadCount}"
                    loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    error.visibility = if (state.errorMessage.isNullOrBlank()) View.GONE else View.VISIBLE
                    error.text = state.errorMessage
                    adapter.clear()
                    adapter.addAll(state.notifications.map { n -> "${if (n.isRead) "" else "• "}${n.title}: ${n.message}" })
                    adapter.notifyDataSetChanged()
                    state.successMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessages()
                    }
                }
            }
        }

        return root
    }
}

