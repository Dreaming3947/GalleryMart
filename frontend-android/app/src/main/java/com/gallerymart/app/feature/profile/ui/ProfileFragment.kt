package com.gallerymart.app.feature.profile.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.load
import coil.transform.CircleCropTransformation
import com.gallerymart.app.AuthActivity
import com.gallerymart.app.R
import com.gallerymart.app.core.network.SessionManager
import com.gallerymart.app.data.repository.ArtworkRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * ProfileFragment quản lý hồ sơ người dùng và chức năng đăng tác phẩm.
 * Giao diện đã được tối ưu hóa cho mục đích demo chuyên nghiệp.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ArtworkUploadViewModel by viewModels()
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { convertUriToFile(it) }
    }

    private var tabLayout: TabLayout? = null
    private var fragmentContainer: FrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainTab = view.findViewById<TabLayout>(R.id.profileTabLayout)
        if (mainTab != null) {
            setupTabNavigation(view)
        } else {
            setupUploadControls(view)
        }
    }

    private fun setupTabNavigation(view: View) {
        tabLayout = view.findViewById(R.id.profileTabLayout)
        fragmentContainer = view.findViewById(R.id.fragmentContainer)

        tabLayout?.apply {
            removeAllTabs()
            addTab(newTab().setText("Hồ sơ"))
            addTab(newTab().setText("Đăng tác phẩm"))
        }

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

        showProfileView()
    }

    /**
     * Hiển thị giao diện Hồ sơ người dùng với thiết kế Card hiện đại.
     */
    private fun showProfileView() {
        fragmentContainer?.removeAllViews()
        val context = requireContext()
        
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setPadding(spToPx(context, 16), spToPx(context, 24), spToPx(context, 16), spToPx(context, 16))
            setBackgroundColor(ContextCompat.getColor(context, R.color.gm_bg))
        }

        // --- Card thông tin cá nhân ---
        val headerCard = MaterialCardView(context).apply {
            radius = 48f
            cardElevation = 0f
            setCardBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = spToPx(context, 16)
            }
        }

        val headerContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(spToPx(context, 24), spToPx(context, 32), spToPx(context, 24), spToPx(context, 32))
        }

        val avatar = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(spToPx(context, 80), spToPx(context, 80)).apply { bottomMargin = spToPx(context, 12) }
            // Sử dụng icon có sẵn trong project
            load(R.drawable.ic_account_circle) {
                transformations(CircleCropTransformation())
            }
        }

        val userName = TextView(context).apply {
            text = SessionManager.userEmail?.substringBefore("@")?.uppercase() ?: "NGƯỜI DÙNG"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.gm_text_primary))
        }

        val userEmail = TextView(context).apply {
            text = SessionManager.userEmail ?: "email@example.com"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.gm_text_secondary))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = spToPx(context, 4)
            }
        }

        headerContent.addView(avatar)
        headerContent.addView(userName)
        headerContent.addView(userEmail)
        headerCard.addView(headerContent)

        // --- Card thông tin hệ thống ---
        val infoCard = MaterialCardView(context).apply {
            radius = 32f
            cardElevation = 0f
            setCardBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val infoContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(spToPx(context, 20), spToPx(context, 16), spToPx(context, 20), spToPx(context, 16))
        }

        infoContent.addView(createLabelValueView(context, "VAI TRÒ", SessionManager.userRoles ?: "BUYER"))
        infoCard.addView(infoContent)

        // --- Nút Đăng xuất nổi bật ---
        val logoutButton = MaterialButton(context).apply {
            text = "ĐĂNG XUẤT"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.gm_error)
            cornerRadius = spToPx(context, 28)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, spToPx(context, 56)).apply {
                topMargin = spToPx(context, 40)
            }
            setOnClickListener {
                SessionManager.clear()
                val intent = Intent(context, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                activity?.finish()
            }
        }

        rootLayout.addView(headerCard)
        rootLayout.addView(infoCard)
        rootLayout.addView(logoutButton)

        fragmentContainer?.addView(rootLayout)
    }

    private fun createLabelValueView(context: Context, label: String, value: String): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, spToPx(context, 8), 0, spToPx(context, 8))
            
            val labelTv = TextView(context).apply {
                text = label
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, R.color.gm_text_secondary))
                setAllCaps(true)
            }
            
            val valueTv = TextView(context).apply {
                text = value
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.gm_text_primary))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = spToPx(context, 2)
                }
            }
            
            addView(labelTv)
            addView(valueTv)
        }
    }

    /**
     * Chuyển đổi đơn vị SP/DP sang Pixels để đảm bảo hiển thị đúng trên các mật độ màn hình khác nhau.
     */
    private fun spToPx(context: Context, sp: Int): Int {
        return (sp * context.resources.displayMetrics.density).toInt()
    }

    private fun showUploadView() {
        fragmentContainer?.removeAllViews()
        val uploadView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_upload_artwork, fragmentContainer, false)
        fragmentContainer?.addView(uploadView)
        setupUploadControls(uploadView)
    }

    private fun setupUploadControls(view: View) {
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

        val categories = arrayOf("PAINTING", "SCULPTURE", "PHOTOGRAPHY", "DIGITAL_ART", "OTHER")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory?.adapter = adapter

        cardImageContainer?.setOnClickListener { imagePickerLauncher.launch("image/*") }
        btnUploadImage?.setOnClickListener { viewModel.uploadImage() }

        btnCreateArtwork?.setOnClickListener {
            viewModel.updateTitle(etTitle?.text.toString())
            viewModel.updateDescription(etDescription?.text.toString())
            viewModel.updatePrice(etPrice?.text.toString())
            viewModel.updateCategory(spinnerCategory?.selectedItem.toString())
            viewModel.createArtwork()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.imageFile != null) {
                    imagePreview?.load(state.imageFile)
                    imagePreview?.visibility = View.VISIBLE
                    placeholderLayout?.visibility = View.GONE
                    btnUploadImage?.visibility = if (state.imageUrl == null) View.VISIBLE else View.GONE
                } else {
                    imagePreview?.visibility = View.GONE
                    placeholderLayout?.visibility = View.VISIBLE
                    btnUploadImage?.visibility = View.GONE
                }
                uploadProgressBar?.visibility = if (state.isUploadingImage) View.VISIBLE else View.GONE
                createProgressBar?.visibility = if (state.isCreatingArtwork) View.VISIBLE else View.GONE
                btnCreateArtwork?.isEnabled = !state.isCreatingArtwork && state.imageUrl != null
                
                uploadStatusMessage?.apply {
                    visibility = if (!state.successMessage.isNullOrEmpty() && state.imageUrl != null) View.VISIBLE else View.GONE
                    text = state.successMessage
                }

                val errorText = state.uploadErrorMessage ?: state.createErrorMessage
                tvErrorMessage?.apply {
                    visibility = if (!errorText.isNullOrEmpty()) View.VISIBLE else View.GONE
                    text = errorText
                }

                if (state.successMessage?.contains("thành công") == true && state.imageUrl == null) {
                    Toast.makeText(requireContext(), "Thao tác thành công!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertUriToFile(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return
            val file = File(requireContext().cacheDir, "artwork_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { inputStream.copyTo(it) }
            viewModel.selectImage(file)
        } catch (e: Exception) {}
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
    val uiState = _uiState

    fun selectImage(file: File) {
        _uiState.value = _uiState.value.copy(imageFile = file, imageUrl = null)
    }
    fun updateTitle(v: String) { _uiState.value = _uiState.value.copy(title = v) }
    fun updateDescription(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun updatePrice(v: String) { _uiState.value = _uiState.value.copy(price = v) }
    fun updateCategory(v: String) { _uiState.value = _uiState.value.copy(category = v) }

    fun uploadImage() {
        val selectedFile = _uiState.value.imageFile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true)
            runCatching {
                val body = selectedFile.asRequestBody("image/*".toMediaType())
                val part = MultipartBody.Part.createFormData("file", selectedFile.name, body)
                repository.uploadArtworkImage(part)
            }.onSuccess { url ->
                _uiState.value = _uiState.value.copy(isUploadingImage = false, imageUrl = url, successMessage = "Tải ảnh thành công")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isUploadingImage = false, uploadErrorMessage = e.message)
            }
        }
    }

    fun createArtwork() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingArtwork = true)
            runCatching {
                repository.createArtwork(state.title, state.description, state.price, state.category, state.imageUrl!!)
            }.onSuccess {
                _uiState.value = ArtworkUploadUiState(successMessage = "Tạo tác phẩm thành công!")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isCreatingArtwork = false, createErrorMessage = e.message)
            }
        }
    }

    fun resetForm() { _uiState.value = ArtworkUploadUiState() }
}
