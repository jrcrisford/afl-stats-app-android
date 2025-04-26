package au.edu.utas.jc101.aflstatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityPlayerStatsBinding
import com.google.firebase.firestore.FirebaseFirestore

class PlayerStatsActivity : AppCompatActivity() {
    private lateinit var ui: ActivityPlayerStatsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var teamAdapter: ArrayAdapter<String>
    private var teamOptions = mutableListOf<String>()
    private var allPlayers = mutableListOf<Player>()
    private var selectedTeam: String = "Both"
    private var selectedMatch: String? = null
    private val matchList = mutableListOf<String>()
    private var teamAName: String = "Team A"
    private var teamBName: String = "Team B"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlayerStatsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to PlayerStatsActivity")

        // Initialise Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        ui.playerStatsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up match selection spinner
        val matchAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, matchList)
        matchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ui.spinnerMatchSelection.adapter = matchAdapter

        ui.spinnerMatchSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMatch = matchList.getOrNull(position)
                Log.d("DEBUG", "Selected match: $selectedMatch")
                if (selectedMatch != null) {
                    loadPlayers()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMatch = null
                Log.w("DEBUG", "No match selected")
            }
        }

        // Set up team filter spinner
        teamAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teamOptions)
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ui.spinnerTeamFilter.adapter = teamAdapter

        ui.spinnerTeamFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTeam = parent.getItemAtPosition(position) as String
                Log.d("DEBUG", "Selected team filter: $selectedTeam")
                filterTeams()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTeam = "Both"
                Log.w("DEBUG", "No team filter selected, defaulting to both teams")
                filterTeams()
            }
        }

        // Start PlayerSelectionDialog and send data to PlayerComparisonActivity
        ui.btnComparePlayers.setOnClickListener {
            Log.d("DEBUG", "Compare players button clicked")
            PlayerSelectionDialog(
                context = this,
                players = allPlayers
            ) { player1, player2 ->
                val intent = Intent(this, PlayerComparisonActivity::class.java)
                intent.putExtra("player1", player1)
                intent.putExtra("player2", player2)
                Log.d("DEBUG", "Starting PlayerComparisonActivity with players: $player1 and $player2")
                startActivity(intent)
            }.show()
        }

        // Start TeamComparisonActivity and send data to it
        ui.btnCompareTeams.setOnClickListener {
            Log.d("DEBUG", "Compare teams button clicked")
            val intent = Intent(this, TeamComparisonActivity::class.java)
            val playerArrayList = ArrayList(allPlayers) as ArrayList<out android.os.Parcelable>
            intent.putParcelableArrayListExtra("players", playerArrayList)
            intent.putExtra("teamAName", teamAName)
            intent.putExtra("teamBName", teamBName)
            Log.d("DEBUG", "Starting TeamComparisonActivity with players: $allPlayers")
            startActivity(intent)
        }

        // Load match list from Firestore
        loadMatchList()
    }

    /**
     * Load the list of matches from Firestore and populate the spinner.
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
                Log.d("DEBUG", "Match list loaded: $matchList")
            }
            .addOnFailureListener { exception ->
                Log.e("DEBUG", "Failed loading matches", exception)
            }
    }

    /**
     * Load player statistics for the selected match from Firestore.
     */
    private fun loadPlayers() {
        db.collection("matches")
            .document(selectedMatch!!)
            .get()

            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rawPlayerStats = document.get("playerStats") as? Map<String, Any>
                    teamAName = document.getString("teamAName") ?: "Team A"
                    teamBName = document.getString("teamBName") ?: "Team B"

                    // Update team options and refresh team filter spinner
                    teamOptions.clear()
                    teamOptions.add("Both")
                    teamOptions.add(teamAName)
                    teamOptions.add(teamBName)
                    teamAdapter.notifyDataSetChanged()

                    // Clear previous player data and load new data
                    allPlayers.clear()
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
                            allPlayers.add(player)
                        }
                    }
                    Log.d("DEBUG", "Players loaded for match $selectedMatch: $allPlayers")
                    filterTeams()
                } else {
                    Log.w("DEBUG", "No such match document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE", "Failed loading players", exception)
            }
    }

    /**
     * Filter the players based on the selected team and update the RecyclerView.
     */
    private fun filterTeams() {
        val filtered = when (selectedTeam) {
            teamAName -> allPlayers.filter { it.team == teamAName }
            teamBName -> allPlayers.filter { it.team == teamBName }
            else -> allPlayers
        }

        Log.d("DEBUG", "Filtered players for team $selectedTeam: {$filtered.size} players")
        ui.playerStatsRecyclerView.adapter = PlayerStatsAdapter(filtered)
    }
}