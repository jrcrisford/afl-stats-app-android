package au.edu.utas.jc101.aflstatsapp

import android.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMatchHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class MatchHistoryActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMatchHistoryBinding
    private lateinit var db: FirebaseFirestore

    private val matchList = mutableListOf<String>()
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

        val matchAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, matchList)
        matchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ui.spinnerMatchSelection.adapter = matchAdapter

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
                Log.d("FIRESTORE", "Match list loaded: $matchList")
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE", "Error loading match list: ", exception)
            }
    }

    private fun loadMatchDetails(matchId: String) {
        db.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("FIRESTORE", "Loading match details for $matchId")

                    teamAName = document.getString("teamAName") ?: "Team A"
                    teamBName = document.getString("teamBName") ?: "Team B"
                    val rawPlayerStats = document.get("playerStats") as? Map<String, Any>
                    val scoreData = document.get("score") as? Map<String, Any>

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
                                actionTimestamps = (playerDataMap["actionTimestamps"] as? List<Map<String, String>>)?.toMutableList()
                                    ?: mutableListOf()
                            )
                            allPlayers.add(player)
                        }
                    }

                    Log.d("DEBUG", "Loaded ${allPlayers.size} players for match $matchId")

                    //updateTeamStats(scoreData)
                    //updatePlayerStats()
                    //updateActionList()
                } else {
                    Log.w("FIRESTORE", "No such match document")
                }
            }
            .addOnFailureListener() { exception ->
                Log.e("FIRESTORE", "Error loading match details: ", exception)
            }
    }





}