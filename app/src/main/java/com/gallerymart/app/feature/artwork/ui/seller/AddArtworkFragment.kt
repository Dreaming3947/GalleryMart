package com.gallerymart.app.feature.artwork.ui.seller

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.gallerymart.app.R
import com.gallerymart.app.core.network.RetrofitProvider
import com.gallerymart.app.core.util.Resource
import com.gallerymart.app.data.remote.api.ArtworkApi
import com.gallerymart.app.data.repository.ArtworkRepositoryImpl
import com.gallerymart.app.databinding.FragmentAddArtworkBinding
import com.gallerymart.app.feature.artwork.model.Artwork
import com.gallerymart.app.feature.artwork.vm.seller.SellerArtworkViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddArtworkFragment : Fragment() {

    private var _binding: FragmentAddArtworkBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SellerArtworkViewModel
    private val args: AddArtworkFragmentArgs by navArgs()

    private var currentStep = 1
    private val totalSteps = 4
    private var isEditMode = false
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this).load(uri).into(binding.ivPreview)
            // Hiển thị preview ngay tại Step 1
            binding.btnAddImage.visibility = View.GONE
            binding.cvStep1Preview.visibility = View.VISIBLE
            Glide.with(this).load(uri).into(binding.ivStep1Preview)
            Toast.makeText(requireContext(), "Đã chọn ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddArtworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        checkEditMode()
        setupNavigation()
        observeViewModel()
        updateStepUI()
    }

    private fun setupViewModel() {
        val api = RetrofitProvider.createService(ArtworkApi::class.java)
        val repository = ArtworkRepositoryImpl(api)
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SellerArtworkViewModel(repository) as T
            }
        })[SellerArtworkViewModel::class.java]
    }

    private fun checkEditMode() {
        args.artwork?.let { artwork ->
            isEditMode = true
            binding.etTitle.setText(artwork.title)
            binding.etDescription.setText(artwork.description)
            binding.etPrice.setText(artwork.price.toString())
            binding.etDimensions.setText(artwork.dimensions)
            // Trong thuc te se load anh vao ivPreview o step 4
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.addArtworkStatus.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnNext.isEnabled = false
                        binding.btnNext.text = "ĐANG XỬ LÝ..."
                    }
                    is Resource.Success -> {
                        val msg = if (isEditMode) "Cập nhật thành công!" else "Đăng tranh thành công!"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is Resource.Error -> {
                        binding.btnNext.isEnabled = true
                        binding.btnNext.text = if (currentStep == totalSteps) "HOÀN TẤT" else "TIẾP THEO"
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupNavigation() {
        binding.btnAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.cvStep1Preview.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnBack.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPrev.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentStep < totalSteps) {
                if (validateStep(currentStep)) {
                    currentStep++
                    updateStepUI()
                }
            } else {
                submitArtwork()
            }
        }
    }

    private fun validateStep(step: Int): Boolean {
        return when (step) {
            1 -> {
                if (selectedImageUri == null && !isEditMode) {
                    Toast.makeText(requireContext(), "Vui lòng chọn ảnh cho tác phẩm", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            2 -> {
                if (binding.etTitle.text.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập tên tác phẩm", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            3 -> {
                if (binding.etPrice.text.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập giá", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            else -> true
        }
    }

    private fun updateStepUI() {
        val indicators = listOf(
            binding.step1Indicator,
            binding.step2Indicator,
            binding.step3Indicator,
            binding.step4Indicator
        )

        indicators.forEachIndexed { index, view ->
            val stepNumber = index + 1
            if (stepNumber <= currentStep) {
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.stone_900))
            } else {
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.stone_100))
            }
        }

        binding.layoutStep1.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        binding.layoutStep2.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        binding.layoutStep3.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        binding.layoutStep4.visibility = if (currentStep == 4) View.VISIBLE else View.GONE

        if (currentStep == 4) {
            binding.tvReviewTitle.text = binding.etTitle.text
            binding.tvReviewPrice.text = "${binding.etPrice.text} VND"
        }

        binding.btnPrev.visibility = if (currentStep > 1) View.VISIBLE else View.GONE
        binding.btnNext.text = if (currentStep == totalSteps) "HOÀN TẤT" else "TIẾP THEO"
    }

    private fun submitArtwork() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val dimensions = binding.etDimensions.text.toString()

        if (selectedImageUri == null && !isEditMode) {
            Toast.makeText(requireContext(), "Vui lòng chọn ảnh trước", Toast.LENGTH_SHORT).show()
            return
        }

        val artwork = Artwork(
            id = args.artwork?.id,
            title = title,
            description = description,
            price = price,
            imageUrl = args.artwork?.imageUrl ?: "",
            category = "Painting",
            dimensions = dimensions,
            status = args.artwork?.status ?: "AVAILABLE"
        )
        
        val imageFile = selectedImageUri?.let { uriToTempFile(it) }
        
        if (isEditMode && artwork.id != null) {
            viewModel.updateArtwork(artwork.id, artwork, imageFile)
        } else {
            viewModel.addArtwork(artwork, imageFile)
        }
    }

    private fun uriToTempFile(uri: Uri): File? {
        val contentResolver = requireContext().contentResolver
        val fileName = getFileName(uri) ?: "temp_image.jpg"
        val tempFile = File(requireContext().cacheDir, fileName)
        
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
