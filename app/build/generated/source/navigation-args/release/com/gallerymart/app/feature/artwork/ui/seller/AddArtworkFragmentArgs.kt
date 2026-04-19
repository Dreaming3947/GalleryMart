package com.gallerymart.app.feature.artwork.ui.seller

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.gallerymart.app.feature.artwork.model.Artwork
import java.io.Serializable
import java.lang.UnsupportedOperationException
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class AddArtworkFragmentArgs(
  public val artwork: Artwork? = null,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    if (Parcelable::class.java.isAssignableFrom(Artwork::class.java)) {
      result.putParcelable("artwork", this.artwork as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(Artwork::class.java)) {
      result.putSerializable("artwork", this.artwork as Serializable?)
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    if (Parcelable::class.java.isAssignableFrom(Artwork::class.java)) {
      result.set("artwork", this.artwork as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(Artwork::class.java)) {
      result.set("artwork", this.artwork as Serializable?)
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): AddArtworkFragmentArgs {
      bundle.setClassLoader(AddArtworkFragmentArgs::class.java.classLoader)
      val __artwork : Artwork?
      if (bundle.containsKey("artwork")) {
        if (Parcelable::class.java.isAssignableFrom(Artwork::class.java) ||
            Serializable::class.java.isAssignableFrom(Artwork::class.java)) {
          __artwork = bundle.get("artwork") as Artwork?
        } else {
          throw UnsupportedOperationException(Artwork::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __artwork = null
      }
      return AddArtworkFragmentArgs(__artwork)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): AddArtworkFragmentArgs {
      val __artwork : Artwork?
      if (savedStateHandle.contains("artwork")) {
        if (Parcelable::class.java.isAssignableFrom(Artwork::class.java) ||
            Serializable::class.java.isAssignableFrom(Artwork::class.java)) {
          __artwork = savedStateHandle.get<Artwork?>("artwork")
        } else {
          throw UnsupportedOperationException(Artwork::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __artwork = null
      }
      return AddArtworkFragmentArgs(__artwork)
    }
  }
}
