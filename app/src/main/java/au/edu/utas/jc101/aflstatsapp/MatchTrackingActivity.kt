package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchTrackingBinding
import com.google.firebase.firestore.FirebaseFirestore

class MatchTrackingActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchTrackingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var matchId: String

    private val players = mutableListOf<Player>()
    private var selectedPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMatchTrackingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to MatchTrackingActivity")

        matchId = intent.getStringExtra("matchId") ?: ""
        Log.d("DEBUG", "Loaded Match ID: $matchId")

        db = FirebaseFirestore.getInstance()

        if (matchId.isNotEmpty()) {
            loadMatchData()
        } else {
            Log.e("DEBUG", "Match ID is empty")
        }
    }

    private fun loadMatchData() {
        db.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rawPlayerStats = document.get("playerStats") as? Map<String, Any>

                    players.clear()
                    rawPlayerStats?.forEach { (_, playerData) ->
                        val playerDataMap = playerData as? Map<String, Any>
                        if (playerDataMap != null) {
                            val player = Player(
                                name = playerDataMap["name"] as? String ?: "",
                                number = (playerDataMap["number"] as? Long)?.toInt() ?: 0,
                                team = playerDataMap["team"] as? String ?: "",
                                kicks = (playerDataMap["kicks"] as? Long)?.toInt() ?: 0,
                                handballs = (playerDataMap["handballs"] as? Long)?.toInt() ?: 0,
                                marks = (playerDataMap["marks"] as? Long)?.toInt() ?: 0,
                                tackles = (playerDataMap["tackles"] as? Long)?.toInt() ?: 0,
                                goals = (playerDataMap["goals"] as? Long)?.toInt() ?: 0,
                                behinds = (playerDataMap["behinds"] as? Long)?.toInt() ?: 0
                            )
                            players.add(player)
                        }
                    }

                    Log.d("DEBUG", "Players Loaded: $players")

                    val playerNames = players.map { it.name }
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        playerNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    ui.spinnerPlayers.adapter = adapter

                    ui.spinnerPlayers.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                selectedPlayer = players[position]
                                ui.txtSelectedPlayer.text = "Selected Player: $selectedPlayer"
                                Log.d("DEBUG", "Selected Player: $selectedPlayer")
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                selectedPlayer = null
                                ui.txtSelectedPlayer.text = "No player selected"
                                Log.d("DEBUG", "No player selected")
                            }
                        }
                } else {
                    Log.w("DEBUG", "Couldn't find match document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DEBUG", "Error loading match", exception)
            }

    }

    // Record action function TODO
}
