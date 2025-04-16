package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class NewMatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_match)
        Log.d("NAVIGATION", "Navigated to NewMatchActivity")
    }
}