package au.edu.utas.jc101.aflstatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchTrackingBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class MatchTrackingActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchTrackingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var matchId: String

    // List to store all players loaded from Firestore
    private val players = mutableListOf<Player>()
    // Currently selected player from the spinner
    private var selectedPlayer: Player? = null
    // Store the most recent action
    private var lastAction: String? = "No previous action"
    // Store the player that made the last kick
    private var lastKicker: Player? = null
    // Store the match start time
    private var startTime: Long = 0L
    // Score variable for both teams
    private var teamAGoals = 0
    private var teamABehinds = 0
    private var teamBGoals = 0
    private var teamBBehinds = 0
    private var teamAName: String = ""
    private var teamBName: String = ""

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

        // Set action and end button listeners
        ui.btnKick.setOnClickListener { recordAction("kick") }
        ui.btnHandball.setOnClickListener { recordAction("handball") }
        ui.btnMark.setOnClickListener { recordAction("mark") }
        ui.btnTackle.setOnClickListener { recordAction("tackle") }
        ui.btnGoal.setOnClickListener { recordAction("goal") }
        ui.btnBehind.setOnClickListener { recordAction("behind") }
        ui.btnEndMatch.setOnClickListener {
            val timestamp = com.google.firebase.Timestamp.now()

            // Update the match document in Firestore to set the end time
            db.collection("matches")
                .document(matchId)
                .update("endedAt", timestamp)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "Match ended successfully at $timestamp")
                    Toast.makeText(this, "Match ended successfully", Toast.LENGTH_SHORT).show()

                    // Return to the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e("FIREBASE", "Error ending match", exception)
                    Toast.makeText(this, "Error ending match", Toast.LENGTH_SHORT).show()
                }
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
                    val scoreData = document.get("score") as? Map<String, Any>
                    startTime = document.getTimestamp("startedAt")?.toDate()?.time ?: 0L

                    teamAName = document.getString("teamAName") ?: ""
                    teamBName = document.getString("teamBName") ?: ""

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
                                behinds = (playerDataMap["behinds"] as? Long)?.toInt() ?: 0,
                                actionTimestamps = mutableListOf()
                            )
                            players.add(player)
                            Log.d("DEBUG", "Loaded player: $player")
                        }
                    }

                    // Load inital team scores
                    teamAGoals = ((scoreData?.get("teamA") as? Map<*, *>)?.get("goals") as? Long)?.toInt() ?: 0
                    teamABehinds = ((scoreData?.get("teamA") as? Map<*, *>)?.get("behinds") as? Long)?.toInt() ?: 0
                    teamBGoals = ((scoreData?.get("teamB") as? Map<*, *>)?.get("goals") as? Long)?.toInt() ?: 0
                    teamBBehinds = ((scoreData?.get("teamB") as? Map<*, *>)?.get("behinds") as? Long)?.toInt() ?: 0

                    // Update the score display UI
                    updateScoreDisplay()

                    // Update spinner with player names
                    val playerNames = players.map { "${it.name} #${it.number}" }
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
                                ui.txtSelectedPlayer.text = "Selected Player: ${selectedPlayer?.name} #${selectedPlayer?.number}"
                                Log.d("DEBUG", "Selected Player: ${selectedPlayer?.name} #${selectedPlayer?.number}")
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
        val currentTime = Timestamp.now()
        val elapsedTime = currentTime.toDate().time - startTime
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60
        val formattedTime = String.format("%d:%02d", minutes, seconds)

        // Update the local player's stat based on the action type and whether the action is valid
        when (actionType) {
            "kick" -> {
                player.kicks++
                lastAction = "kick"
                lastKicker = player
                Log.d("DEBUG", "Kick recorded for player: ${player.name}")
            }
            "handball" -> {
                player.handballs++
                lastAction = "handball"
                Log.d("DEBUG", "Handball recorded for player: ${player.name}")
            }
            "mark" -> {
                player.marks++
                lastAction = "mark"
                Log.d("DEBUG", "Mark recorded for player: ${player.name}")
            }
            "tackle" -> {
                player.tackles++
                lastAction = "tackle"
                Log.d("DEBUG", "Tackle recorded for player: ${player.name}")
            }
            "goal" -> {
                if (lastAction != "kick") {
                    Log.w("DEBUG", "Goal action not allowed after ${lastAction}")
                    Toast.makeText(
                        this,
                        "Goal action not allowed after ${lastAction}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                if (lastKicker == null || lastKicker?.team != player.team) {
                    Log.w(
                        "DEBUG",
                        "Goal action not allowed: last kicker, ${lastKicker?.name} is not on ${lastKicker?.team} team",
                    )
                    Toast.makeText(
                        this,
                        "Goal action not allowed: last kicker, ${lastKicker?.name} is not on ${lastKicker?.team} team",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }
            "behind" -> {
                if (lastAction == "kick" || lastAction == "handball") {
                    player.behinds++
                    lastAction = "behind"
                    if (player.team == teamAName) teamABehinds++ else if (player.team == teamBName) teamBBehinds++
                    Log.d("DEBUG", "Behind recorded for player: ${player.name}")
                } else {
                    Log.w("DEBUG", "Behind action not allowed after ${lastAction}")
                    Toast.makeText(
                        this,
                        "Behind action not allowed after ${lastAction}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }
            else -> {
                Log.w("DEBUG", "Unknown action type: $actionType")
                return
            }
        }

        // Record the action timestamp and update Firestore
        player.actionTimestamps.add(mapOf("action" to actionType, "timestamp" to formattedTime))
        Log.d("DEBUG", "Action recorded at $formattedTime for player: ${player.name}")
        updateFirestore(player, actionType)
        updateScoreDisplay()
    }

    /**
     * Updates the Firestore database with the player's action and the current score.
     * @param player The player whose action is being recorded.
     * @param actionType The type of action performed by the player.
     */
    private fun updateFirestore(player: Player, actionType: String) {
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
            "behinds" to player.behinds,
            "actionTimestamps" to player.actionTimestamps
        )

        // Prepare updated team score data
        val scoreData = hashMapOf(
            "score.teamA.goals" to teamAGoals,
            "score.teamA.behinds" to teamABehinds,
            "score.teamB.goals" to teamBGoals,
            "score.teamB.behinds" to teamBBehinds,
            "score.teamA.score" to (teamAGoals * 6 + teamABehinds),
            "score.teamB.score" to (teamBGoals * 6 + teamBBehinds)
        )

        // Update the player data in Firestore
        db.collection("matches")
            .document(matchId)
            .update(mapOf("playerStats.${player.id}" to playerData) + scoreData)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Action recorded for player: ${player.name}")
                Toast.makeText(this, "${actionType.replaceFirstChar { it.uppercase() }} recorded for ${player.name}", Toast.LENGTH_SHORT).show()

                // Update the score display UI
                updateScoreDisplay()
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE", "Error recording action", exception)
            }
    }

    /**
    * Updates the score display on the UI with the current scores for both teams.
    */
    private fun updateScoreDisplay() {
        val teamAScore = "$teamAGoals.$teamABehinds (${teamAGoals * 6 + teamABehinds})"
        val teamBScore = "$teamBGoals.$teamBBehinds (${teamBGoals * 6 + teamBBehinds})"

        ui.txtScore.text = "$teamAName: $teamAScore vs $teamBName: $teamBScore"
    }
}
