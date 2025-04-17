package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchTrackingBinding

class MatchTrackingActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMatchTrackingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to MatchTrackingActivity")
    }
}
