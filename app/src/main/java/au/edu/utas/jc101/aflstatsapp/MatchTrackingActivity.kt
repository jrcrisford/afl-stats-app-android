package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchTrackingBinding
import com.google.firebase.firestore.FirebaseFirestore

class MatchTrackingActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMatchTrackingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to MatchTrackingActivity")

        val matchId = intent.getStringExtra("matchId")
        Log.d("DEBUG", "Loaded Match ID: $matchId")

        val db = FirebaseFirestore.getInstance()

        if (matchId != null) {
            db.collection("matches")
                .document(matchId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val playerStats = document.get("playerStats") as? Map<String, Any>
                        val playerNames = mutableListOf<String>()

                        playerStats?.forEach { (_, playerData) ->
                            val playerDataMap = playerData as? Map<String, Any>
                            val playerName = playerDataMap?.get("name") as? String
                            if (playerName != null) {
                                playerNames.add(playerName)
                            }
                        }

                        Log.d("DEBUG", "Players Loaded: $playerNames")

                        val adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_spinner_item,
                            playerNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        ui.spinnerPlayers.adapter = adapter
                    } else {
                        Log.d("DEBUG", "Couldn't find match document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DEBUG", "Error loading match", exception)
                    }
        } else {
            Log.e("DEBUG", "Match ID is null")
        }
    }
}
