package com.gallerymart.app.feature.artwork.ui.seller

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavDirections
import com.gallerymart.app.R
import com.gallerymart.app.feature.artwork.model.Artwork
import java.io.Serializable
import kotlin.Int
import kotlin.Suppress

public class SellerInventoryFragmentDirections private constructor() {
  private data class ActionSellerInventoryFragmentToAddArtworkFragment(
    public val artwork: Artwork? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_sellerInventoryFragment_to_addArtworkFragment

    public override val arguments: Bundle
      @Suppress("CAST_NEVER_SUCCEEDS")
      get() {
        val result = Bundle()
        if (Parcelable::class.java.isAssignableFrom(Artwork::class.java)) {
          result.putParcelable("artwork", this.artwork as Parcelable?)
        } else if (Serializable::class.java.isAssignableFrom(Artwork::class.java)) {
          result.putSerializable("artwork", this.artwork as Serializable?)
        }
        return result
      }
  }

  public companion object {
    public fun actionSellerInventoryFragmentToAddArtworkFragment(artwork: Artwork? = null):
        NavDirections = ActionSellerInventoryFragmentToAddArtworkFragment(artwork)
  }
}
