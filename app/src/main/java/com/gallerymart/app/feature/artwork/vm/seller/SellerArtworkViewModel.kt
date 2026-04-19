package com.gallerymart.app.feature.artwork.vm.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gallerymart.app.core.util.Resource
import com.gallerymart.app.domain.repository.ArtworkRepository
import com.gallerymart.app.feature.artwork.model.Artwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SellerArtworkViewModel(
    private val repository: ArtworkRepository
) : ViewModel() {

    private val _myArtworks = MutableStateFlow<Resource<List<Artwork>>>(Resource.Loading())
    val myArtworks: StateFlow<Resource<List<Artwork>>> = _myArtworks.asStateFlow()

    private val _addArtworkStatus = MutableStateFlow<Resource<Artwork>?>(null)
    val addArtworkStatus: StateFlow<Resource<Artwork>?> = _addArtworkStatus.asStateFlow()

    private val _enableSellerStatus = MutableStateFlow<Resource<Unit>?>(null)
    val enableSellerStatus: StateFlow<Resource<Unit>?> = _enableSellerStatus.asStateFlow()

    fun getMyArtworks() {
        viewModelScope.launch {
            _myArtworks.value = Resource.Loading()
            _myArtworks.value = repository.getMyArtworks()
        }
    }

    fun addArtwork(artwork: Artwork, imageFile: File?) {
        viewModelScope.launch {
            _addArtworkStatus.value = Resource.Loading()
            _addArtworkStatus.value = repository.createArtwork(artwork, imageFile)
        }
    }

    fun updateArtwork(id: Long, artwork: Artwork, imageFile: File?) {
        viewModelScope.launch {
            _addArtworkStatus.value = Resource.Loading()
            _addArtworkStatus.value = repository.updateArtwork(id, artwork, imageFile)
        }
    }

    fun deleteArtwork(id: Long) {
        viewModelScope.launch {
            val result = repository.deleteArtwork(id)
            if (result is Resource.Success) {
                getMyArtworks() // Refresh list
            }
        }
    }

    fun enableSeller() {
        viewModelScope.launch {
            _enableSellerStatus.value = Resource.Loading()
            _enableSellerStatus.value = repository.enableSeller()
        }
    }

    fun resetAddStatus() {
        _addArtworkStatus.value = null
    }
}
