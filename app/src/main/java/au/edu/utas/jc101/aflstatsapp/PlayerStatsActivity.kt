package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityPlayerStatsBinding
import com.google.firebase.firestore.FirebaseFirestore

class PlayerStatsActivity : AppCompatActivity() {
    private lateinit var ui: ActivityPlayerStatsBinding
    private lateinit var db: FirebaseFirestore

    private var allPlayers = mutableListOf<Player>()
    private var selectedPlayers = mutableListOf<Player>()
    private var selectedTeam: String = "Both"
    private var selectedMatch: String? = null
    private val matchList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlayerStatsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to PlayerStatsActivity")

        db = FirebaseFirestore.getInstance()
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
                if (selectedMatch != null) {
                    loadPlayers()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMatch = null
            }
        }

        // Set up team filter spinner
        val teamOptions = listOf("Both", "Team A", "Team B")
        val teamAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teamOptions)
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
                filterTeams()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTeam = "Both"
                filterTeams()
            }
        }

        // Return to Main Menu Button
        ui.btnReturnMainMenu.setOnClickListener {
            finish()
        }

        // Load match list from Firestore
        loadMatchList()
    }

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

    private fun loadPlayers() {
        db.collection("matches")
            .document(selectedMatch!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rawPlayerStats = document.get("playerStats") as? Map<String, Any>
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

    private fun filterTeams() {
        val filtered = when (selectedTeam) {
            "Team A" -> allPlayers.filter { it.team == "Team A" }
            "Team B" -> allPlayers.filter { it.team == "Team B" }
            else -> allPlayers
        }

        ui.playerStatsRecyclerView.adapter = PlayerStatsAdapter(filtered)
    }
}