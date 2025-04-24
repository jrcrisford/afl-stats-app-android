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
        val matchAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, matchList)
        matchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ui.spinnerMatchSelection.adapter = matchAdapter

        // Set up the spinner to show the match list
        ui.spinnerMatchSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    selectedMatch = matchList.getOrNull(position)
                    if (selectedMatch != null) {
                        Log.d("MATCH_SELECTION", "Selected match: $selectedMatch")
                        loadMatchDetails(selectedMatch!!)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedMatch = null
                }
            }

        loadMatchList()

        // Set up the button to add a new player
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

        // Setup toggle for action list visibility
        var actionsCollapsed = true
        ui.actionsListView.visibility = View.GONE
        ui.btnToggleActions.text = "Show Actions"

        ui.btnToggleActions.setOnClickListener {
            if (!actionsCollapsed) {
                ui.actionsListView.visibility = View.GONE
                ui.btnToggleActions.text = "Show Actions"
            } else {
                ui.actionsListView.visibility = View.VISIBLE
                ui.btnToggleActions.text = "Hide Actions"
            }
            actionsCollapsed = !actionsCollapsed
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
                (ui.spinnerMatchSelection.adapter as ArrayAdapter<*>).notifyDataSetChanged()
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
                    val scoreData = document.get("score") as? Map<String, Any>

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
                                    ?: mutableListOf()
                            )
                            allPlayers.add(player)
                        }
                    }

                    Log.d("DEBUG", "Loaded ${allPlayers.size} players for match $matchId")

                    updateTeamStats(scoreData)
                    updatePlayerStats()
                    updateActionList()
                } else {
                    Log.w("FIRESTORE", "No such match document")
                }
            }
            .addOnFailureListener() { exception ->
                Log.e("FIRESTORE", "Error loading match details: ", exception)
            }
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
            view1.setTextColor(Color.GREEN)
            view2.setTextColor(Color.WHITE)
        } else if (stat2 > stat1) {
            view2.setTextColor(Color.GREEN)
            view1.setTextColor(Color.WHITE)
        } else {
            view1.setTextColor(Color.BLUE)
            view2.setTextColor(Color.BLUE)
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

    /**
     * Updates the team stats displayed in the UI.
     *
     * @param scoreData The score data retrieved from Firestore.
     */
    private fun updateTeamStats(scoreData: Map<String, Any>?) {
        // Load Goals and Behinds
        val teamAGoals =
            ((scoreData?.get("teamA") as? Map<*, *>)?.get("goals") as? Long)?.toInt() ?: 0
        val teamABehinds =
            ((scoreData?.get("teamA") as? Map<*, *>)?.get("behinds") as? Long)?.toInt() ?: 0
        val teamBGoals =
            ((scoreData?.get("teamB") as? Map<*, *>)?.get("goals") as? Long)?.toInt() ?: 0
        val teamBBehinds =
            ((scoreData?.get("teamB") as? Map<*, *>)?.get("behinds") as? Long)?.toInt() ?: 0

        // Calculate total scores
        val teamATotal = teamBGoals * 6 + teamABehinds
        val teamBTotal = teamBGoals * 6 + teamBBehinds

        // Update UI with team names and scores
        ui.txtFinalScoreSummary.text =
            "$teamAName $teamAGoals.$teamABehinds ($teamATotal) vs $teamBName $teamBGoals.$teamBBehinds ($teamBTotal)"

        // Find players for each team and calculate stats
        val disposalsA =
            allPlayers.filter { it.team == teamAName }.sumOf { it.kicks + it.handballs }
        val disposalsB =
            allPlayers.filter { it.team == teamBName }.sumOf { it.kicks + it.handballs }
        val marksA = allPlayers.filter { it.team == teamAName }.sumOf { it.marks }
        val marksB = allPlayers.filter { it.team == teamBName }.sumOf { it.marks }
        val tacklesA = allPlayers.filter { it.team == teamAName }.sumOf { it.tackles }
        val tacklesB = allPlayers.filter { it.team == teamBName }.sumOf { it.tackles }

        // Update UI with team stats
        ui.txtTeamADisposals.text = disposalsA.toString()
        ui.txtTeamBDisposals.text = disposalsB.toString()
        ui.txtTeamAMarks.text = marksA.toString()
        ui.txtTeamBMarks.text = marksB.toString()
        ui.txtTeamATackles.text = tacklesA.toString()
        ui.txtTeamBTackles.text = tacklesB.toString()
        ui.txtTeamAScore.text = formatScore(allPlayers.filter { it.team == teamAName })
        ui.txtTeamBScore.text = formatScore(allPlayers.filter { it.team == teamBName })

        // Highlight the better team's stats
        statHighlighting(ui.txtTeamADisposals, ui.txtTeamBDisposals, disposalsA, disposalsB)
        statHighlighting(ui.txtTeamAMarks, ui.txtTeamBMarks, marksA, marksB)
        statHighlighting(ui.txtTeamATackles, ui.txtTeamBTackles, tacklesA, tacklesB)
        statHighlighting(ui.txtTeamAScore, ui.txtTeamBScore, teamATotal, teamBTotal)
    }

    /**
     * Updates the player stats displayed in the RecyclerView.
     */
    private fun updatePlayerStats() {
        ui.playerStatsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        ui.playerStatsRecyclerView.adapter = PlayerStatsAdapter(allPlayers)
    }

    /**
     * Updates the action list displayed in the ListView.
     * It sorts the actions by timestamp and displays them in the ListView.
     */
    private fun updateActionList() {
        val actionPairs = mutableListOf<Pair<String, String>>()

        // Loop through all players and their action timestamps
        for (player in allPlayers) {
            // Loop through each player's actions
            for (action in player.actionTimestamps) {
                val actionType = action["action"] ?: "Unknown action"
                val timestamp = action["timestamp"] as? String ?: "Unknown timestamp"
                val text = "${player.name} #${player.number}: $actionType at $timestamp"
                actionPairs.add(Pair(timestamp, text))
            }
        }

        // Sort the actions by timestamp
        val sortedActions = actionPairs.sortedBy { it.first }
        val actionTexts = sortedActions.map { it.second }

        Log.d("DEBUG", "Action texts: $actionTexts")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            actionTexts
        )
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
}