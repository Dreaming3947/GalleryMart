package com.gallerymart.app.feature.profile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.load
import com.gallerymart.app.AuthActivity
import com.gallerymart.app.R
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.data.repository.ArtworkRepository
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * ProfileFragment manages seller profile and artwork upload.
 *
 * Responsibilities:
 * - Display seller profile information
 * - Tab navigation between profile and upload views
 * - Image picker and file conversion
 * - Coordinate upload flow with ViewModel
 * - Show upload progress and errors to user
 *
 * Debug note: Check SessionManager.userRoles to verify SELLER role before upload.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ArtworkUploadViewModel by viewModels()
    
    // Upload flow controls
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { convertUriToFile(it) }
    }

    // UI Binding (late init to be set in onCreateView)
    private var tabLayout: TabLayout? = null
    private var fragmentContainer: FrameLayout? = null
    private var imagePreview: ImageView? = null
    private var imagePreviewContainer: FrameLayout? = null
    private var placeholderLayout: LinearLayout? = null
    private var titleInput: EditText? = null
    private var descriptionInput: EditText? = null
    private var priceInput: EditText? = null
    private var categorySpinner: Spinner? = null
    private var selectImageButton: Button? = null
    private var uploadImageButton: Button? = null
    private var createArtworkButton: Button? = null
    private var uploadProgressBar: ProgressBar? = null
    private var createProgressBar: ProgressBar? = null
    private var uploadStatusMessage: TextView? = null
    private var uploadErrorMessage: TextView? = null
    private var createErrorMessage: TextView? = null
    private var successMessage: TextView? = null
    private var resetButton: Button? = null
    
    // Nested placeholder for profile view
    private var profileInfoContainer: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views ONLY if we're in the main Profile tab layout
        val mainTab = view.findViewById<TabLayout>(R.id.profileTabLayout)
        if (mainTab != null) {
            setupTabNavigation(view)
        } else {
            // We're being used as upload-only fragment, bind upload controls directly
            setupUploadControls(view)
        }
    }

    /**
     * Setup tab navigation between Profile and Upload views.
     */
    private fun setupTabNavigation(view: View) {
        tabLayout = view.findViewById(R.id.profileTabLayout)
        fragmentContainer = view.findViewById(R.id.fragmentContainer)

        tabLayout?.addTab(tabLayout!!.newTab().setText("Profile"))
        tabLayout?.addTab(tabLayout!!.newTab().setText("Upload Artwork"))

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showProfileView()
                    1 -> showUploadView()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Show profile by default
        showProfileView()
    }

    /**
     * Display user profile information.
     */
    private fun showProfileView() {
        fragmentContainer?.removeAllViews()
        
        val profileLayout = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(requireContext().getColor(R.color.gm_bg))
        }

        val userEmail = SessionManager.userEmail ?: "Not logged in"
        val userRoles = SessionManager.userRoles ?: "User"

        val emailView = TextView(requireContext()).apply {
            text = "Email: $userEmail"
            textSize = 16f
            setTextColor(requireContext().getColor(R.color.gm_text_primary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        val rolesView = TextView(requireContext()).apply {
            text = "Role: $userRoles"
            textSize = 16f
            setTextColor(requireContext().getColor(R.color.gm_text_primary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }

        val logoutButton = Button(requireContext()).apply {
            text = "Logout"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                48
            ).apply { bottomMargin = 16 }
            setOnClickListener {
                SessionManager.clear()
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                activity?.finish()
            }
        }

        profileLayout.apply {
            addView(emailView)
            addView(rolesView)
            addView(logoutButton)
        }

        profileInfoContainer = profileLayout
        fragmentContainer?.addView(profileLayout)
    }

    /**
     * Display upload artwork form.
     */
    private fun showUploadView() {
        fragmentContainer?.removeAllViews()
        val uploadView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_upload_artwork, fragmentContainer, false)
        fragmentContainer?.addView(uploadView)
        setupUploadControls(uploadView)
    }

    /**
     * Bind upload form controls and Wire ViewModel state.
     */
    private fun setupUploadControls(view: View) {
        // Bind views from XML
        val cardImageContainer = view.findViewById<View>(R.id.cardImageContainer)
        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val placeholderLayout = view.findViewById<View>(R.id.placeholderLayout)
        val uploadProgressBar = view.findViewById<ProgressBar>(R.id.uploadProgressBar)
        val btnUploadImage = view.findViewById<Button>(R.id.btnUploadImage)
        val uploadStatusMessage = view.findViewById<TextView>(R.id.uploadStatusMessage)
        
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        
        val btnCreateArtwork = view.findViewById<Button>(R.id.btnCreateArtwork)
        val createProgressBar = view.findViewById<ProgressBar>(R.id.createProgressBar)
        val tvErrorMessage = view.findViewById<TextView>(R.id.tvErrorMessage)
        val btnReset = view.findViewById<Button>(R.id.btnReset)

        // Setup category spinner
        val categories = arrayOf("PAINTING", "SCULPTURE", "PHOTOGRAPHY", "DIGITAL_ART", "OTHER")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory?.adapter = adapter

        // Wire button listeners
        cardImageContainer?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnUploadImage?.setOnClickListener {
            viewModel.uploadImage()
        }

        btnCreateArtwork?.setOnClickListener {
            viewModel.updateTitle(etTitle?.text.toString())
            viewModel.updateDescription(etDescription?.text.toString())
            viewModel.updatePrice(etPrice?.text.toString())
            viewModel.updateCategory(spinnerCategory?.selectedItem.toString())
            viewModel.createArtwork()
        }

        btnReset?.setOnClickListener {
            viewModel.resetForm()
            etTitle?.text?.clear()
            etDescription?.text?.clear()
            etPrice?.text?.clear()
            spinnerCategory?.setSelection(0)
        }

        // Observe ViewModel state changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Update image preview
                if (state.imageFile != null) {
                    imagePreview?.load(state.imageFile)
                    imagePreview?.visibility = View.VISIBLE
                    placeholderLayout?.visibility = View.GONE
                    
                    // Show upload button if not uploaded yet
                    btnUploadImage?.visibility = if (state.imageUrl == null) View.VISIBLE else View.GONE
                } else {
                    imagePreview?.visibility = View.GONE
                    placeholderLayout?.visibility = View.VISIBLE
                    btnUploadImage?.visibility = View.GONE
                }

                // Update upload progress
                uploadProgressBar?.visibility = if (state.isUploadingImage) View.VISIBLE else View.GONE
                btnUploadImage?.isEnabled = !state.isUploadingImage

                // Update create progress
                createProgressBar?.visibility = if (state.isCreatingArtwork) View.VISIBLE else View.GONE
                btnCreateArtwork?.isEnabled = !state.isCreatingArtwork && state.imageUrl != null

                // Update messages
                uploadStatusMessage?.apply {
                    visibility = if (!state.successMessage.isNullOrEmpty() && state.imageUrl != null) View.VISIBLE else View.GONE
                    text = state.successMessage
                }

                val errorText = state.uploadErrorMessage ?: state.createErrorMessage
                tvErrorMessage?.apply {
                    visibility = if (!errorText.isNullOrEmpty()) View.VISIBLE else View.GONE
                    text = errorText
                }

                // Show reset button after success
                val isSuccess = state.successMessage?.contains("successfully") == true && state.imageUrl == null
                btnReset?.visibility = if (isSuccess) View.VISIBLE else View.GONE
                
                if (isSuccess) {
                    Toast.makeText(requireContext(), "Đăng tác phẩm thành công!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Convert Uri to File for upload.
     *
     * Copies image from content provider to app cache directory.
     */
    private fun convertUriToFile(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return
            val fileName = "artwork_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().cacheDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            viewModel.selectImage(file)
        } catch (e: Exception) {
            uploadErrorMessage?.apply {
                text = "Failed to select image: ${e.message}"
                visibility = View.VISIBLE
            }
        }
    }

    /**
     * Reset upload UI to initial state.
     */
    private fun refreshUploadUI() {
        titleInput?.text?.clear()
        descriptionInput?.text?.clear()
        priceInput?.text?.clear()
        categorySpinner?.setSelection(0)
        imagePreview?.visibility = View.GONE
        placeholderLayout?.visibility = View.VISIBLE
        uploadStatusMessage?.visibility = View.GONE
        uploadErrorMessage?.visibility = View.GONE
        createErrorMessage?.visibility = View.GONE
        successMessage?.visibility = View.GONE
        resetButton?.visibility = View.GONE
    }
}

data class ArtworkUploadUiState(
    val imageFile: File? = null,
    val imageUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "PAINTING",
    val isUploadingImage: Boolean = false,
    val isCreatingArtwork: Boolean = false,
    val uploadErrorMessage: String? = null,
    val createErrorMessage: String? = null,
    val successMessage: String? = null
)

class ArtworkUploadViewModel(
    private val repository: ArtworkRepository = ArtworkRepository()
) : androidx.lifecycle.ViewModel() {

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(ArtworkUploadUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<ArtworkUploadUiState> = _uiState

    fun selectImage(file: File) {
        _uiState.value = _uiState.value.copy(
            imageFile = file,
            imageUrl = null,
            uploadErrorMessage = null,
            createErrorMessage = null,
            successMessage = null
        )
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updatePrice(value: String) {
        _uiState.value = _uiState.value.copy(price = value)
    }

    fun updateCategory(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun uploadImage() {
        val selectedFile = _uiState.value.imageFile ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingImage = true,
                uploadErrorMessage = null,
                successMessage = null
            )

            runCatching {
                val body = selectedFile.asRequestBody("image/*".toMediaType())
                val part = MultipartBody.Part.createFormData("file", selectedFile.name, body)
                repository.uploadArtworkImage(part)
            }.onSuccess { imageUrl ->
                _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    imageUrl = imageUrl,
                    successMessage = "Image uploaded successfully"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    uploadErrorMessage = error.message ?: "Image upload failed"
                )
            }
        }
    }

    fun createArtwork() {
        val state = _uiState.value
        if (state.imageUrl.isNullOrBlank()) {
            _uiState.value = state.copy(createErrorMessage = "Please upload image before creating artwork")
            return
        }
        if (state.title.isBlank() || state.price.isBlank()) {
            _uiState.value = state.copy(createErrorMessage = "Title and price are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreatingArtwork = true,
                createErrorMessage = null,
                successMessage = null
            )

            runCatching {
                repository.createArtwork(
                    title = state.title.trim(),
                    description = state.description.trim(),
                    price = state.price.trim(),
                    category = state.category,
                    imageUrl = state.imageUrl
                )
            }.onSuccess {
                _uiState.value = ArtworkUploadUiState(successMessage = "Artwork created successfully")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCreatingArtwork = false,
                    createErrorMessage = error.message ?: "Create artwork failed"
                )
            }
        }
    }

    fun resetForm() {
        _uiState.value = ArtworkUploadUiState()
    }
}


