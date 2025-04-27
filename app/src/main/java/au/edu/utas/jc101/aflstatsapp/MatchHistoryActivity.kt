package au.edu.utas.jc101.aflstatsapp

import android.R
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class MatchHistoryActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchHistoryBinding
    private lateinit var db: FirebaseFirestore

    // List of matches from Firestore
    private val matchList = mutableListOf<String>()

    // Stores the currently selected match ID
    private var selectedMatch: String? = null

    // Stores the currently filtered quarter
    private var selectedQuarter = 0

    // List of all actions performed
    private val allActionsList = mutableListOf<Map<String, Any>>()

    private var teamAName: String = "Team A"
    private var teamBName: String = "Team B"
    private var allPlayers = mutableListOf<Player>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMatchHistoryBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to MatchHistoryActivity")

        db = FirebaseFirestore.getInstance()

        // Set up match selection spinner
        val matchAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, matchList)
        ui.dropdownMatchSelection.setAdapter(matchAdapter)

        ui.dropdownMatchSelection.setOnItemClickListener { parent, view, position, id ->
            selectedMatch = matchList.getOrNull(position)
            if (selectedMatch != null) {
                Log.d("MATCH_SELECTION", "Selected match: $selectedMatch")
                loadMatchDetails(selectedMatch!!)
            }
        }

        loadMatchList()

        var teamStatsExpanded = false
        ui.teamStatsContent.visibility = View.GONE

        ui.teamStatsTitle.setOnClickListener {
            teamStatsExpanded = !teamStatsExpanded
            ui.teamStatsContent.visibility = if (teamStatsExpanded) View.VISIBLE else View.GONE
        }

        // Set up the spinner to filter stats by quarters
        val quarterAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listOf("Full Match", "Q1", "Q2", "Q3", "Q4")
        )

        // Set the adapter to the dropdown
        ui.dropdownQuarterFilter.setAdapter(quarterAdapter)

        // Set the item click listener
        ui.dropdownQuarterFilter.setOnItemClickListener { parent, view, position, id ->
            selectedQuarter = position
            updateTeamStats()
        }

        var playerStatsExpanded = false
        ui.playerStatsContent.visibility = View.GONE

        ui.txtPlayerStatsTitle.setOnClickListener {
            playerStatsExpanded = !playerStatsExpanded
            ui.playerStatsContent.visibility = if (playerStatsExpanded) View.VISIBLE else View.GONE
        }

        // Set up the button to compare players
        ui.btnComparePlayers.setOnClickListener {
            if (allPlayers.isNotEmpty()) {
                PlayerSelectionDialog(
                    context = this,
                    players = allPlayers
                ) { player1, player2 ->
                    val intent = Intent(this, PlayerComparisonActivity::class.java)
                    intent.putExtra("player1", player1)
                    intent.putExtra("player2", player2)
                    Log.d(
                        "DEBUG",
                        "Starting PlayerComparisonActivity with players: $player1 and $player2"
                    )
                    startActivity(intent)
                }.show()
            }
        }

        // Set up share list button
        ui.actionsListView.visibility = View.GONE
        var actionsExpanded = false

        ui.txtActionsTitle.setOnClickListener {
            actionsExpanded = !actionsExpanded
            ui.actionsListView.visibility = if (actionsExpanded) View.VISIBLE else View.GONE
        }

        ui.btnShareActions.setOnClickListener {
            shareActionList()
        }
    }

    /**
     * Loads the list of matches from Firestore and populates the spinner.
     */
    private fun loadMatchList() {
        db.collection("matches")
            .get()
            .addOnSuccessListener { documents ->
                matchList.clear()
                for (document in documents) {
                    matchList.add(document.id)
                }
                val matchAdapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, matchList)
                ui.dropdownMatchSelection.setAdapter(matchAdapter)
                Log.d("FIRESTORE", "Match list loaded: $matchList")
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE", "Error loading match list: ", exception)
            }
    }

    /**
     * Retrieves all player and score data for a specific match from Firestore.
     * Also updates the UI with the relevant data.
     *
     * @param matchId The ID of the match to load.
     */
    private fun loadMatchDetails(matchId: String) {
        db.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("FIRESTORE", "Loading match details for $matchId")

                    // Extract team names and player stats from the document
                    teamAName = document.getString("teamAName") ?: "Team A"
                    teamBName = document.getString("teamBName") ?: "Team B"
                    val rawPlayerStats = document.get("playerStats") as? Map<String, Any>

                    // Clear the existing player list
                    allPlayers.clear()

                    // Populate the player list with data from Firestore
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
                                actionTimestamps = (playerDataMap["actionTimestamps"] as? List<Map<String, String>>)?.toMutableList()
                                    ?: mutableListOf(),
                                photoUri = playerDataMap["photoUri"] as? String
                            )
                            allPlayers.add(player)
                        }
                    }

                    Log.d("DEBUG", "Loaded ${allPlayers.size} players for match $matchId")

                    // Collect actions per quarter
                    val actionsByQuarter = mutableMapOf<Int, MutableList<Pair<String, Any>>>()

                    // Loop through all players and their action timestamps and find which quarter each action occurred in
                    for (player in allPlayers) {
                        for (action in player.actionTimestamps) {
                            val actionType = action["action"] ?: continue
                            if (actionType != "goal" && actionType != "behind") continue

                            val quarter = (action["quarter"] as? Long)?.toInt() ?: continue
                            val team = player.team

                            if (!actionsByQuarter.containsKey(quarter)) {
                                actionsByQuarter[quarter] = mutableListOf()
                            }

                            actionsByQuarter[quarter]?.add(Pair(team, actionType))
                        }
                    }

                    Log.d("DEBUG", "Grouped actions by quarter: $actionsByQuarter")
                    updateQuarterlyScore(actionsByQuarter)

                    updateTeamStats()
                    updatePlayerStats()
                    updateActionList()
                } else {
                    Log.w("FIRESTORE", "No such match document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE", "Error loading match details: ", exception)
            }
    }

    /**
     * Calculates, updates and displays team stats for the selected match and quarter.
     * Highlights the winning team and better-performing stats with colour.
     */
    private fun updateTeamStats() {
        // Update the team names in the UI
        val finalScoreA =
            allPlayers.filter { it.team == teamAName }.sumOf { it.goals * 6 + it.behinds }
        val finalScoreB =
            allPlayers.filter { it.team == teamBName }.sumOf { it.goals * 6 + it.behinds }

        ui.txtTeamAName.text = teamAName
        ui.txtTeamBName.text = teamBName

        // Apply color to highlight the winning team
        if (finalScoreA > finalScoreB) {
            ui.txtTeamAName.setTextColor(Color.parseColor("#00FFFF"))
            ui.txtTeamBName.setTextColor(Color.WHITE)
        } else if (finalScoreB > finalScoreA) {
            ui.txtTeamAName.setTextColor(Color.WHITE)
            ui.txtTeamBName.setTextColor(Color.parseColor("#00FFFF"))
        } else {
            ui.txtTeamAName.setTextColor(Color.parseColor("#FFA500"))
            ui.txtTeamBName.setTextColor(Color.parseColor("#FFA500"))
        }

        val filteredPlayers = if (selectedQuarter == 0) {
            allPlayers // All quarters (no filter)
        } else {
            allPlayers.map { player ->
                val quarter = selectedQuarter
                val filteredActions = player.actionTimestamps.filter {
                    (it["quarter"] as? Long)?.toInt() == quarter
                }

                val kicks = filteredActions.count { it["action"] == "kick" }
                val handballs = filteredActions.count { it["action"] == "handball" }
                val marks = filteredActions.count { it["action"] == "mark" }
                val tackles = filteredActions.count { it["action"] == "tackle" }

                player.copy(
                    kicks = kicks,
                    handballs = handballs,
                    marks = marks,
                    tackles = tackles
                )
            }
        }

        val teamAPlayers = filteredPlayers.filter { it.team == teamAName }
        val teamBPlayers = filteredPlayers.filter { it.team == teamBName }

        // Calculate stats for both teams
        val disposalsA = teamAPlayers.sumOf { it.kicks + it.handballs }
        val disposalsB = teamBPlayers.sumOf { it.kicks + it.handballs }
        val marksA = teamAPlayers.sumOf { it.marks }
        val marksB = teamBPlayers.sumOf { it.marks }
        val tacklesA = teamAPlayers.sumOf { it.tackles }
        val tacklesB = teamBPlayers.sumOf { it.tackles }

        // Update UI with team stats
        ui.txtTeamADisposals.text = disposalsA.toString()
        ui.txtTeamBDisposals.text = disposalsB.toString()
        ui.txtTeamAMarks.text = marksA.toString()
        ui.txtTeamBMarks.text = marksB.toString()
        ui.txtTeamATackles.text = tacklesA.toString()
        ui.txtTeamBTackles.text = tacklesB.toString()

        // Highlight the better team's stats
        statHighlighting(ui.txtTeamADisposals, ui.txtTeamBDisposals, disposalsA, disposalsB)
        statHighlighting(ui.txtTeamAMarks, ui.txtTeamBMarks, marksA, marksB)
        statHighlighting(ui.txtTeamATackles, ui.txtTeamBTackles, tacklesA, tacklesB)
    }

    /**
     * Calculates score values based on goal and behind actions and
     * updates the quarterly score display in the UI.
     *
     * @param actionsByQuarter The actions grouped by quarter.
     */
    private fun updateQuarterlyScore(actionsByQuarter: Map<Int, List<Pair<String, Any>>>) {
        val quarters = listOf(1, 2, 3, 4)
        val uiRows = listOf(
            Triple(ui.txtQ1TeamA, ui.txtQ1Label, ui.txtQ1TeamB),
            Triple(ui.txtQ2TeamA, ui.txtQ2Label, ui.txtQ2TeamB),
            Triple(ui.txtQ3TeamA, ui.txtQ3Label, ui.txtQ3TeamB),
            Triple(ui.txtQFinalTeamA, ui.txtQFinalLabel, ui.txtQFinalTeamB)
        )

        var cumulativeGoalsA = 0
        var cumulativeBehindsA = 0
        var cumulativeGoalsB = 0
        var cumulativeBehindsB = 0

        for ((index, quarter) in quarters.withIndex()) {
            val actions = actionsByQuarter[quarter] ?: emptyList()

            var goalsA = actions.count { it.first == teamAName && it.second == "goal" }
            var behindsA = actions.count { it.first == teamAName && it.second == "behind" }
            var goalsB = actions.count { it.first == teamBName && it.second == "goal" }
            var behindsB = actions.count { it.first == teamBName && it.second == "behind" }

            cumulativeGoalsA += goalsA
            cumulativeBehindsA += behindsA
            cumulativeGoalsB += goalsB
            cumulativeBehindsB += behindsB

            val totalA = cumulativeGoalsA * 6 + cumulativeBehindsA
            val totalB = cumulativeGoalsB * 6 + cumulativeBehindsB

            val txtTeamA = uiRows[index].first
            val txtLabel = uiRows[index].second
            val txtTeamB = uiRows[index].third

            txtTeamA.text = "$cumulativeGoalsA.$cumulativeBehindsA ($totalA)"
            txtLabel.text = if (quarter == 4) "Final" else "Q$quarter"
            txtTeamB.text = "$cumulativeGoalsB.$cumulativeBehindsB ($totalB)"

            statHighlighting(txtTeamA, txtTeamB, totalA, totalB)
        }
    }

    /**
     * Updates the player stats displayed in the RecyclerView.
     */
    private fun updatePlayerStats() {
        ui.playerStatsRecyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        ui.playerStatsRecyclerView.adapter = PlayerStatsAdapter(allPlayers)
    }

    /**
     * Updates the action list displayed in the ListView.
     * It sorts the actions by timestamp and displays them in the ListView.
     */
    private fun updateActionList() {
        val actionTriples = mutableListOf<Triple<Int, String, String>>() // quarter, sort key, text
        allActionsList.clear()

        // Loop through all players and their action timestamps
        for (player in allPlayers) {
            // Loop through each player's actions
            for (action in player.actionTimestamps) {
                val actionType = action["action"] ?: "Unknown action"
                val timestamp = action["timestamp"] as? String ?: "Unknown timestamp"
                val quarterTime = action["quarterTime"] as? String ?: "Unknown quarter time"
                val quarter = (action["quarter"] as? Long)?.toInt() ?: -1

                val text =
                    "${player.name} #${player.number}: $actionType | Q$quarter @ $quarterTime (game: $timestamp)"
                val sortKey = "$quarterTime-$timestamp"
                actionTriples.add(Triple(quarter, sortKey, text))

                allActionsList.add(
                    mapOf(
                        "playerName" to player.name,
                        "playerNumber" to player.number,
                        "actionType" to actionType,
                        "quarter" to quarter.toString(),
                        "timestamp" to timestamp,
                        "quarterTime" to quarterTime
                    )
                )
            }
        }

        // Sort the actions by timestamp
        val sortedActions = actionTriples.sortedWith(compareBy({ it.first }, { it.second }))
        val actionTexts = sortedActions.map { it.third }

        Log.d("DEBUG", "Action texts: $actionTexts")

        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, actionTexts)
        ui.actionsListView.adapter = adapter

        // Expand the ListView to show all items (COPILOT)
        val density = resources.displayMetrics.density
        val itemHeightDp = 80 // you can tweak this if you want
        val itemHeightPx = (itemHeightDp * density).toInt()
        val totalHeight = itemHeightPx * adapter.count

        val params = ui.actionsListView.layoutParams
        params.height = totalHeight
        ui.actionsListView.layoutParams = params

        ui.actionsListView.requestLayout()
    }

    /**
     * Shares the action list in either plain text or JSON format.
     */
    private fun shareActionList() {
        val option = arrayOf("Share as Plain Text", "Share as JSON")

        val alertBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        alertBuilder.setTitle("Choose Format")
        alertBuilder.setItems(option) { dialog, which ->
            val shareText = when (which) {
                0 -> { // Plain text format
                    allActionsList.joinToString(separator = "\n") { action ->
                        "${action["playerName"]} #${action["playerNumber"]}: " +
                                "${action["actionType"]} | Q${action["quarter"]} " +
                                "@ ${action["quarterTime"]} (game: ${action["timestamp"]})"
                    }
                }

                1 -> { // JSON format
                    allActionsList.joinToString(
                        prefix = "[", postfix = "]", separator = ",\n"
                    ) { action ->
                        """
                            {
                                "playerName": "${action["playerName"]}",
                                "playerNumber": ${action["playerNumber"]},
                                "actionType": "${action["actionType"]}",
                                "quarter": ${action["quarter"]},
                                "timestamp": "${action["timestamp"]}",
                                "quarterTime": "${action["quarterTime"]}"
                            }
                        """.trimIndent()
                    }
                }

                else -> ""
            }

            val subject = if (which == 0) {
                "Match Actions (Plain Text)"
            } else {
                "Match Actions (JSON)"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Actions via"))
        }
        alertBuilder.show()
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

    /**
     * Formats the score for display, converting goals and behinds into a string
     * in the Goals.Behinds (Total) format.
     *
     * @param players The list of players to calculate the score from.
     */
    private fun formatScore(players: List<Player>): String {
        val goals = players.sumOf { it.goals }
        val behinds = players.sumOf { it.behinds }
        val total = goals * 6 + behinds
        return "$goals.$behinds ($total)"
    }
}