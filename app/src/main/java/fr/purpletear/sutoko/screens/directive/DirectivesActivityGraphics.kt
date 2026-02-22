package fr.purpletear.sutoko.screens.directive

import com.bumptech.glide.RequestManager
import com.example.sharedelements.GraphicsPreference
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.databinding.ActivityDirectivesBinding

object DirectivesActivityGraphics {


    fun set(binding: ActivityDirectivesBinding, requestManager: RequestManager) {
        requestManager.load(R.drawable.sutoko_ic_add).apply(
            GraphicsPreference.getRequestOptions(
                GraphicsPreference.Level.CACHE
            )
        ).into(binding.sutokoDirectivesImagesStar1)
        requestManager.load(R.drawable.sutoko_ic_add).apply(
            GraphicsPreference.getRequestOptions(
                GraphicsPreference.Level.CACHE
            )
        ).into(binding.sutokoDirectivesImagesStar2)
    }
}