package fr.purpletear.sutoko.ads

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

/** Isolated host for Google test-inventory smoke tests; absent from release APKs. */
class AdPlaybackTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Ad integration test host" })
    }
}
