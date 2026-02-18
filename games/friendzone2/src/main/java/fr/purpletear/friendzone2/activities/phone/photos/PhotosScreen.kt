package fr.purpletear.friendzone2.activities.phone.photos

import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import fr.purpletear.friendzone2.R
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import purpletear.fr.purpleteartools.Measure


class PhotosScreen : AppCompatActivity() {

    /**
     * Handles the model settings
     * @see PhotosScreenModel
     */
    private lateinit var model: PhotosScreenModel

    /**
     * Handles the graphic settings
     * @see PhotosScreenGraphics
     */
    private lateinit var graphics: PhotosScreenGraphics

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_photos)
        load()
        setRecyclerView(model.adapter)
    }

    override fun onStart() {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onStart")
        super.onStart()
    }

    override fun onBackPressed() {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onBackPressed")
        super.onBackPressed()
    }

    override fun onResume() {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onDestroy")
        super.onDestroy()
    }

    override fun onPause() {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onPause")
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.d("purpletearDebug", "[STATE] PhotosScreen : onWindowFocusChanged ()")
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && model.isFirstStart()) {
            graphics()
        }
    }

    /**
    * Inits the Activity's vars
    */
    private fun load() {
        model = PhotosScreenModel(this, Glide.with(this))
        graphics = PhotosScreenGraphics()
    }

    /**
     * Sets initial graphics settings
     */
    private fun graphics() {
        graphics.setImages(this, model.requestManager)
    }


    /**
     * Sets the recyclerView
     * @param adapter : Adapter
     */
    private fun setRecyclerView(adapter : Adapter) {
        val recyclerView = findViewById<View>(R.id.phone_photos_recyclerview) as RecyclerView
        adapter.fill()
        val lLayout = GridLayoutManager(this, 3)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = lLayout
        recyclerView.adapter = adapter
        recyclerView.setItemViewCacheSize(20)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, Measure.px(5, this), true, 0))
    }
}
