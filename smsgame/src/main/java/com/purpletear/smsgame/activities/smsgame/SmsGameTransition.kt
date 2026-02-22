package com.purpletear.smsgame.activities.smsgame

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.sharedelements.Data
import com.purpletear.sutoko.game.model.Game

class SmsGameTransition : AppCompatActivity() {
    private var isGranted: Boolean = false
    private lateinit var card: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isGranted = intent.getBooleanExtra("isGranted", false)
        // TODO 
        // card = intent.getParcelableExtra(Data.Companion.Extra.ITEM.id)!!
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val intent = Intent(this, SmsGameActivity::class.java)
            intent.putExtra(Data.Companion.Extra.ITEM.id, card as Parcelable)
            startActivityForResult(intent, 123)
            overridePendingTransition(0, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_CANCELED) {
            finish()
            return
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isGranted", isGranted)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isGranted = (savedInstanceState.getBoolean("isGranted", false))
    }
}
