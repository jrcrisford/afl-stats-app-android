package au.edu.utas.jc101.aflstatsapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
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
    // Store the current quarter of the match
    private var currentQuarter = 1
    // Flag to indicate if a quarter is on going
    private var isQuarterOngoing = false
    // Store the quarter start time
    private var quarterStartTime: Long = 0L
    // Track the time elapsed in the current quarter
    private var quarterTimer: CountDownTimer? = null
    // Quarter duration in milliseconds (20 seconds for testing)
    private val quarterDuration = 20 * 1000L
    // Store the match total time
    private var totalGameTime: Long = 0L

    // Score variable for both teams
    private var teamAGoals = 0
    private var teamABehinds = 0
    private var teamBGoals = 0
    private var teamBBehinds = 0
    private var teamAName: String = ""
    private var teamBName: String = ""
    private var mvpPlayerID: String? = null

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

        // Update the quarter toggle with the current quarter
        ui.btnQuarterToggle.text = "Start Quarter $currentQuarter"

        // Set action and end button listeners (some were auto completed by Copilot)
        ui.btnKick.setOnClickListener { recordAction("kick") }
        ui.btnHandball.setOnClickListener { recordAction("handball") }
        ui.btnMark.setOnClickListener { recordAction("mark") }
        ui.btnTackle.setOnClickListener { recordAction("tackle") }
        ui.btnGoal.setOnClickListener { recordAction("goal") }
        ui.btnBehind.setOnClickListener { recordAction("behind") }
        ui.btnQuarterToggle.setOnClickListener {
            if (isQuarterOngoing) {
                endQuarter()
            } else {
                startQuarter()
            }
        }

        // Button to compare two players
        ui.btnPlayerComparison.setOnClickListener {
            if (players.size >= 2) {
                PlayerSelectionDialog(
                    context = this,
                    players = players
                ) { player1, player2 ->
                    val intent = Intent(this, PlayerComparisonActivity::class.java)
                    intent.putExtra("player1", player1)
                    intent.putExtra("player2", player2)
                    startActivity(intent)
                }.show()
            } else {
                Toast.makeText(this, "Warning: Select at least two players to compare", Toast.LENGTH_SHORT).show()
            }
        }

        // Button to end the match
        ui.btnEndMatch.setOnClickListener {
            val confirmDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm End Match")
                .setMessage("Are you sure you want to end the match? This action cannot be undone.")
                .setPositiveButton("End Match") { _, _ ->
                    val timestamp = Timestamp.now()

                    // Update the match document in Firestore to set the end time
                    db.collection("matches")
                        .document(matchId)
                        .update("endedAt", timestamp)
                        .addOnSuccessListener {
                            Log.d("FIREBASE", "Match ended successfully at $timestamp")

                            val finalScoreA = teamAGoals * 6 + teamABehinds
                            val finalScoreB = teamBGoals * 6 + teamBBehinds

                            val winnerMsg = when {
                                finalScoreA > finalScoreB -> "The $teamAName win!"
                                finalScoreB > finalScoreA -> "The $teamBName win!"
                                else -> "It's a draw!"
                            }

                            val alertBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
                            alertBuilder.setTitle("Match Finished")
                            alertBuilder.setMessage(
                                "Final Score:\n$teamAName: $teamAGoals.$teamABehinds ($finalScoreA)" +
                                        "\n$teamBName: $teamBGoals.$teamBBehinds ($finalScoreB)\n\n$winnerMsg"
                            )
                            alertBuilder.setPositiveButton("Continue") { dialog, _ ->
                                dialog.dismiss()

                                // Return to the main activity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            alertBuilder.setCancelable(false)
                            alertBuilder.show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FIREBASE", "Error ending match", exception)
                            Toast.makeText(this, "Error ending match", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            confirmDialog.show()
            Log.d("DEBUG", "End Match button clicked")
        }

        // Copilot helped to figure out how to make the team stats and player stats expandable
        ui.teamStatsContent.visibility = View.GONE
        var teamStatsExpanded = false
        ui.teamStatsTitle.setOnClickListener {
            teamStatsExpanded = !teamStatsExpanded
            ui.teamStatsContent.visibility = if (teamStatsExpanded) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        ui.playerStatsContent.visibility = View.GONE
        var playerStatsExpanded = false
        ui.playerStatsTitle.setOnClickListener {
            playerStatsExpanded = !playerStatsExpanded
            ui.playerStatsContent.visibility = if (playerStatsExpanded) {
                View.VISIBLE
            } else {
                View.GONE
            }
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
                                actionTimestamps = mutableListOf(),
                                photoUri = playerDataMap["photoUri"] as? String
                            )
                            players.add(player)
                            Log.d("DEBUG", "Loaded player: $player")
                        }
                    }

                    // Load initial team scores
                    teamAGoals = ((scoreData?.get("teamA") as? Map<*, *>)?.get("goals") as? Long)?.toInt() ?: 0
                    teamABehinds = ((scoreData?.get("teamA") as? Map<*, *>)?.get("behinds") as? Long)?.toInt() ?: 0
                    teamBGoals = ((scoreData?.get("teamB") as? Map<*, *>)?.get("goals") as? Long)?.toInt() ?: 0
                    teamBBehinds = ((scoreData?.get("teamB") as? Map<*, *>)?.get("behinds") as? Long)?.toInt() ?: 0

                    // Update the score display UI
                    updateScoreDisplay()
                    updateQuarterlyScore()
                    updateTeamStats()
                    updatePlayerStats()

                    // Update spinner with player names
                    val playerNames = players.map { "${it.name} #${it.number}" }
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        playerNames
                    )
                    ui.dropdownPlayer.setAdapter(adapter)

                    // Set the spinner listener to update the selected player
                    ui.dropdownPlayer.setOnItemClickListener() { parent, view, position, id ->
                        selectedPlayer = players[position]
                        Log.d(
                            "DEBUG",
                            "Selected player: ${selectedPlayer?.name} #${selectedPlayer?.number}"
                        )
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

        if (!isQuarterOngoing) {
            Log.w("DEBUG", "Action not allowed: quarter is not active")
            Toast.makeText(this, "You must start the quarter before recording an action", Toast.LENGTH_SHORT).show()
            return
        }

        val player = selectedPlayer!!

        val currentTime = System.currentTimeMillis()

        val gameElapsedTime = totalGameTime + (currentTime - quarterStartTime)
        val quarterElapsedTime = currentTime - quarterStartTime

        val gameMinutes = (gameElapsedTime / 1000) / 60
        val gameSeconds = (gameElapsedTime / 1000) % 60
        val quarterMinutes = (quarterElapsedTime / 1000) / 60
        val quarterSeconds = (quarterElapsedTime / 1000) % 60

        val formattedGameTime = String.format("%d:%02d", gameMinutes, gameSeconds)
        val formattedQuarterTime = String.format("%d:%02d", quarterMinutes, quarterSeconds)

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
            // Copilot helped write and debug some of the confusing logic for the goal and behind actions
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
                player.goals++
                if (player.team == teamAName) {
                    teamAGoals++
                } else if (player.team == teamBName) {
                    teamBGoals++
                }
                lastAction = "goal"
                Log.d("DEBUG", "Goal recorded for player: ${player.name}")
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
        player.actionTimestamps.add(
            mapOf(
                "action" to actionType,
                "timestamp" to formattedGameTime,
                "quarterTime" to formattedQuarterTime,
                "quarter" to currentQuarter))
        Log.d("DEBUG", "Action recorded: $actionType for ${player.name} in Q$currentQuarter at $formattedQuarterTime (game: $formattedGameTime)")
        updateFirestore(player, actionType)
        updateScoreDisplay()
        calculateMVP()
        updateQuarterlyScore()
        updateTeamStats()
        updatePlayerStats()
    }

    /**
     * Updates the Firestore database with the player's action and the current score.
     * @param player The player whose action is being recorded.
     * @param actionType The type of action performed by the player.
     */
    private fun updateFirestore(player: Player, actionType: String) {
        // Prepare updated player data for Firestore (autocompleted by Copilot)
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

        // Prepare updated team score data (autocompleted by Copilot)
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

                // Update the score display UI
                updateScoreDisplay()
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE", "Error recording action", exception)
            }
    }

    /**
     * Starts a new quarter of the match.
     */
    private fun startQuarter() {
        isQuarterOngoing = true
        quarterStartTime = System.currentTimeMillis()
        Log.d("DEBUG", "Quarter $currentQuarter started")
        ui.btnQuarterToggle.text = "End Quarter $currentQuarter"

        // Start the quarter timer (Copilot helped to debug issues with the timer)
        quarterTimer?.cancel()
        quarterTimer = object : CountDownTimer(quarterDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                ui.txtQuarterTimer.text =
                    String.format("Q$currentQuarter: %02d:%02d", minutes, seconds)
            }
            override fun onFinish() {
                Toast.makeText(
                    this@MatchTrackingActivity,
                    "Quarter $currentQuarter auto-ended, time limit reached",
                    Toast.LENGTH_SHORT).show()
                Log.d("DEBUG", "Quarter $currentQuarter auto-ended")
                endQuarter()
            }
        }.start()
    }

    /**
     * Ends the current quarter of the match.
     */
    private fun endQuarter() {
        isQuarterOngoing = false
        quarterTimer?.cancel()
        totalGameTime += System.currentTimeMillis() - quarterStartTime

        Log.d("DEBUG", "Quarter $currentQuarter ended")

        if (currentQuarter < 4) {
            currentQuarter++
            ui.btnQuarterToggle.text = "Start Quarter $currentQuarter"
            ui.btnQuarterToggle.setTextColor(Color.WHITE)
        } else {
            ui.btnQuarterToggle.text = "Match Complete"
            ui.btnQuarterToggle.setTextColor(Color.parseColor("#CCCCCC"))
            ui.btnQuarterToggle.isEnabled = false
        }

        ui.txtQuarterTimer.text = "Q$currentQuarter ended"
    }

    /**
     * Updates the score display on the UI with the current scores for both teams.
     */
    private fun updateScoreDisplay() {
        val totalScoreA = teamAGoals * 6 + teamABehinds
        val totalScoreB = teamBGoals * 6 + teamBBehinds

        val teamAScoreText = "$teamAName $teamAGoals.$teamABehinds ($totalScoreA)"
        val teamBScoreText = "$teamBName $teamBGoals.$teamBBehinds ($totalScoreB)"

        ui.txtTeamAScore.text = teamAScoreText
        ui.txtTeamBScore.text = teamBScoreText

        // Copilot showed me how to use Color.parseColor to set the text color
        if (totalScoreA > totalScoreB) {
            ui.txtTeamAScore.setTextColor(Color.parseColor("#00FFFF"))
            ui.txtTeamBScore.setTextColor(Color.WHITE)
        } else if (totalScoreB > totalScoreA) {
            ui.txtTeamBScore.setTextColor(Color.parseColor("#00FFFF"))
            ui.txtTeamAScore.setTextColor(Color.WHITE)
        } else {
            ui.txtTeamAScore.setTextColor(Color.parseColor("#FFA500"))
            ui.txtTeamBScore.setTextColor(Color.parseColor("#FFA500"))
        }
    }

    /**
     * Calculates score values based on goal and behind actions and
     * updates the quarterly score display in the UI.
     *
     * @param actionsByQuarter The actions grouped by quarter.
     */
    private fun updateQuarterlyScore() {
        val quarters = listOf(1, 2, 3, 4)
        val uiRows = listOf(
            Triple(ui.txtQ1TeamA, ui.txtQ1Label, ui.txtQ1TeamB),
            Triple(ui.txtQ2TeamA, ui.txtQ2Label, ui.txtQ2TeamB),
            Triple(ui.txtQ3TeamA, ui.txtQ3Label, ui.txtQ3TeamB),
            Triple(ui.txtQFinalTeamA, ui.txtQFinalLabel, ui.txtQFinalTeamB)
        )

        val actionsByQuarter = mutableMapOf<Int, MutableList<Pair<String, Any>>>()

        for (player in players) {
            for (action in player.actionTimestamps) {
                val type = action["action"] ?: continue
                if (type != "goal" && type != "behind") continue
                val quarter = (action["quarter"] as? Int) ?: (action["quarter"] as? Long)?.toInt() ?: continue
                val team = player.team
                actionsByQuarter.getOrPut(quarter) { mutableListOf() }.add(Pair(team, type))
            }
        }

        var cumulativeGoalsA = 0
        var cumulativeBehindsA = 0
        var cumulativeGoalsB = 0
        var cumulativeBehindsB = 0

        for ((index, quarter) in quarters.withIndex()) {
            val (teamAView, labelView, teamBView) = uiRows[index]
            labelView.text = if (quarter == 4) "Final" else "Q$quarter"

            if (quarter > currentQuarter) {
                teamAView.text = "0.0 (0)"
                teamBView.text = "0.0 (0)"
                teamAView.setTextColor(Color.WHITE)
                teamBView.setTextColor(Color.WHITE)
                continue
            }

            val actions = actionsByQuarter[quarter] ?: emptyList()

            val goalsA = actions.count { it.first == teamAName && it.second == "goal" }
            val behindsA = actions.count { it.first == teamAName && it.second == "behind" }
            val goalsB = actions.count { it.first == teamBName && it.second == "goal" }
            val behindsB = actions.count { it.first == teamBName && it.second == "behind" }

            cumulativeGoalsA += goalsA
            cumulativeBehindsA += behindsA
            cumulativeGoalsB += goalsB
            cumulativeBehindsB += behindsB

            val totalA = cumulativeGoalsA * 6 + cumulativeBehindsA
            val totalB = cumulativeGoalsB * 6 + cumulativeBehindsB

            teamAView.text = "$cumulativeGoalsA.$cumulativeBehindsA ($totalA)"
            teamBView.text = "$cumulativeGoalsB.$cumulativeBehindsB ($totalB)"

            statHighlighting(teamAView, teamBView, totalA, totalB)
        }
    }

    /**
     * Updates the team stats display on the UI.
     */
    private fun updateTeamStats() {
        // Calculate full match totals (no quarter filtering for now)
        val teamAPlayers = players.filter { it.team == teamAName }
        val teamBPlayers = players.filter { it.team == teamBName }

        // Much of the rest of this function was auto completed by Copilot
        val disposalsA = teamAPlayers.sumOf { it.kicks + it.handballs }
        val disposalsB = teamBPlayers.sumOf { it.kicks + it.handballs }
        val marksA = teamAPlayers.sumOf { it.marks }
        val marksB = teamBPlayers.sumOf { it.marks }
        val tacklesA = teamAPlayers.sumOf { it.tackles }
        val tacklesB = teamBPlayers.sumOf { it.tackles }

        ui.txtTeamADisposals.text = disposalsA.toString()
        ui.txtTeamBDisposals.text = disposalsB.toString()
        ui.txtTeamAMarks.text = marksA.toString()
        ui.txtTeamBMarks.text = marksB.toString()
        ui.txtTeamATackles.text = tacklesA.toString()
        ui.txtTeamBTackles.text = tacklesB.toString()

        statHighlighting(ui.txtTeamADisposals, ui.txtTeamBDisposals, disposalsA, disposalsB)
        statHighlighting(ui.txtTeamAMarks, ui.txtTeamBMarks, marksA, marksB)
        statHighlighting(ui.txtTeamATackles, ui.txtTeamBTackles, tacklesA, tacklesB)
    }

    /**
     * Updates the player stats displayed in the RecyclerView.
     */
    private fun updatePlayerStats() {
        ui.playerStatsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        ui.playerStatsRecyclerView.adapter = PlayerStatsAdapter(players, mvpPlayerID)
    }

    private fun calculateMVP() {
        var highestScore = -1
        var bestPlayer: Player? = null

        for (player in players) {
            val playerScore = (player.goals * 6) + player.behinds
            if (playerScore > highestScore) {
                highestScore = playerScore
                bestPlayer = player
            }
        }

        mvpPlayerID = bestPlayer?.id
        Log.d("DEBUG", "MVP Player: ${bestPlayer?.name} with score: $highestScore")
    }


    /**
     * Highlights the better team's stat in green and the other team's stat in white.
     * If both stats are equal, both are highlighted in blue.
     *
     * @param view1 The TextView for the first team.
     * @param view2 The TextView for the second team.
     * @param stat1 The stat value for the first team.
     * @param stat2 The stat value for the second team.
     */
    private fun statHighlighting(view1: TextView, view2: TextView, stat1: Int, stat2: Int) =
        // Copilot showed me how to use Color.parseColor to set the text color
        if (stat1 > stat2) {
            view1.setTextColor(Color.parseColor("#00FFFF"))
            view2.setTextColor(Color.parseColor("#CCCCCC"))
        } else if (stat2 > stat1) {
            view2.setTextColor(Color.parseColor("#00FFFF"))
            view1.setTextColor(Color.parseColor("#CCCCCC"))
        } else {
            view1.setTextColor(Color.parseColor("#FFA500"))
            view2.setTextColor(Color.parseColor("#FFA500"))
        }
}
