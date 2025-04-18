package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchTrackingBinding
import com.google.firebase.firestore.FirebaseFirestore

class MatchTrackingActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchTrackingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var matchId: String

    // List to store all players loaded from Firestore
    private val players = mutableListOf<Player>()
    // Currently selected player from the spinner
    private var selectedPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMatchTrackingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to MatchTrackingActivity")

        // Get the match ID from the intent
        matchId = intent.getStringExtra("matchId") ?: ""
        Log.d("DEBUG", "Loaded Match ID: $matchId")

        // Initialize Firestore database
        db = FirebaseFirestore.getInstance()

        // Load match and player data if match ID is valid
        if (matchId.isNotEmpty()) {
            loadMatchData()
        } else {
            Log.e("DEBUG", "Match ID is empty, cannot load match data")
        }

        // Set action button listeners
        ui.btnKick.setOnClickListener { recordAction("kick") }
        ui.btnHandball.setOnClickListener { recordAction("handball") }
        ui.btnMark.setOnClickListener { recordAction("mark") }
        ui.btnTackle.setOnClickListener { recordAction("tackle") }
        ui.btnGoal.setOnClickListener { recordAction("goal") }
        ui.btnBehind.setOnClickListener { recordAction("behind") }
        ui.btnEndMatch.setOnClickListener {
            // TODO End match logic here
            Log.d("DEBUG", "End Match button clicked")
        }
    }

    /**
     * Loads match data from Firestore and populates the player spinner.
     */
    private fun loadMatchData() {
        db.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rawPlayerStats = document.get("playerStats") as? Map<String, Any>

                    // Clear the old player data
                    players.clear()

                    // Parse each player entry and add it to the local list
                    rawPlayerStats?.forEach { (playerId, playerData) ->
                        val playerDataMap = playerData as? Map<String, Any>
                        if (playerDataMap != null) {
                            val player = Player(
                                id = playerId,
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
                            Log.d("DEBUG", "Loaded player: $player")
                        }
                    }

                    // Update spinner with player names
                    val playerNames = players.map { it.name }
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        playerNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    ui.spinnerPlayers.adapter = adapter

                    // Set the spinner listener to update the selected player
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

    /**
     * Records the action for the selected player locally and updates Firestore.
     * @param actionType The type of action to record (kick, handball, mark, tackle, goal, behind).
     */
    private fun recordAction(actionType: String) {
        if (selectedPlayer == null) {
            Log.w("DEBUG", "No player selected to record action to")
            return
        }

        val player = selectedPlayer!!

        // Update the local player' stat based on the action type
        when (actionType) {
            "kick" -> {
                player.kicks++
                Log.d("DEBUG", "Kick recorded for player: ${player.name}")
            }
            "handball" -> {
                player.handballs++
                Log.d("DEBUG", "Handball recorded for player: ${player.name}")
            }
            "mark" -> {
                player.marks++
                Log.d("DEBUG", "Mark recorded for player: ${player.name}")
            }
            "tackle" -> {
                player.tackles++
                Log.d("DEBUG", "Tackle recorded for player: ${player.name}")
            }
            "goal" -> {
                player.goals++
                Log.d("DEBUG", "Goal recorded for player: ${player.name}")
            }
            "behind" -> {
                player.behinds++
                Log.d("DEBUG", "Behind recorded for player: ${player.name}")
            }
            else -> {
                Log.w("DEBUG", "Unknown action type: $actionType")
                return
            }
        }

        // Prepare updated player data for Firestore
        val playerData = hashMapOf(
            "name" to player.name,
            "number" to player.number,
            "team" to player.team,
            "kicks" to player.kicks,
            "handballs" to player.handballs,
            "marks" to player.marks,
            "tackles" to player.tackles,
            "goals" to player.goals,
            "behinds" to player.behinds
        )

        // Update the player data in Firestore
        db.collection("matches")
            .document(matchId)
            .update("playerStats.${player.id}", playerData)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Action recorded for player: ${player.name}")
                Toast.makeText(this, "${actionType.replaceFirstChar { it.uppercase() }} recorded for ${player.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE", "Error recording action", exception)
            }
    }
}
