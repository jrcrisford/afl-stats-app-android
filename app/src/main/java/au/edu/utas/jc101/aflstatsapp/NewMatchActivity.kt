package au.edu.utas.jc101.aflstatsapp

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityNewMatchBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class NewMatchActivity : AppCompatActivity() {
    private lateinit var ui: ActivityNewMatchBinding
    private val db = FirebaseFirestore.getInstance()

    private val teams = mutableListOf<Team>()
    private var selectedTeamA: Team? = null
    private var selectedTeamB: Team? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityNewMatchBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to NewMatchActivity")

        loadTeams()

        ui.btnStartMatch.setOnClickListener {
            startMatch()
        }
    }

    private fun loadTeams() {
        db.collection("teams")
            .get()
            .addOnSuccessListener { documents ->
                teams.clear()
                for (document in documents) {
                    // Copilot helped debug this
                    val teamName = document.getString("name") ?: continue
                    val playersData = document.get("players") as? List<Map<String, Any>> ?: listOf()
                    val players = playersData.map {
                        Player(
                            id = it["id"] as String,
                            name = it["name"] as String,
                            number = (it["number"] as? Long)?.toInt() ?: 0,
                            team = teamName,
                            photoUri = it["photoUri"] as? String
                        )
                    }.toMutableList()

                    teams.add(Team(name = teamName, players = players))
                }
                setupSpinners()
            }
            .addOnFailureListener { exception ->
                Log.w("FIREBASE", "Error getting teams: ", exception)
                Toast.makeText(this, "Failed to load teams", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSpinners() {
        val teamNames = teams.map { it.name }

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, teamNames)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        (ui.dropdownTeamA as? AutoCompleteTextView)?.setAdapter(adapter)
        (ui.dropdownTeamB as? AutoCompleteTextView)?.setAdapter(adapter)

        ui.dropdownTeamA.setOnItemClickListener { parent, view, position, id ->
            selectedTeamA = teams[position]
            updatePlayersUI(teamA = true)
        }

        ui.dropdownTeamB.setOnItemClickListener { parent, view, position, id ->
            selectedTeamB = teams[position]
            updatePlayersUI(teamA = false)
        }
    }

    private fun updatePlayersUI(teamA: Boolean) {
        if (teamA) {
            val playersText =
                selectedTeamA?.players?.joinToString("\n") { "${it.name} (${it.number})" }
            ui.txtTeamAPlayers.text = playersText
        } else {
            val playersText =
                selectedTeamB?.players?.joinToString("\n") { "${it.name} (${it.number})" }
            ui.txtTeamBPlayers.text = playersText
        }
    }

    private fun startMatch() {
        if (selectedTeamA == null || selectedTeamB == null) {
            Toast.makeText(this, "You must select two teams", Toast.LENGTH_SHORT).show()
            return
        }

        val teamAName = selectedTeamA!!.name
        val teamBName = selectedTeamB!!.name

        if (teamAName == teamBName) {
            Toast.makeText(this, "You must select two different teams", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTeamA!!.players.size < 2 || selectedTeamB!!.players.size <2) {
            Toast.makeText(this, "Each team must have at least 2 players", Toast.LENGTH_SHORT).show()
            return
        }

        val matchId = "${teamAName} vs ${teamBName}"
        val timestamp = Timestamp.now()

        val allPlayers = selectedTeamA!!.players + selectedTeamB!!.players
        val playerStats = mutableMapOf<String, Any>()
        allPlayers.forEach { player ->
            playerStats[player.id] = hashMapOf(
                "id" to player.id,
                "name" to player.name,
                "number" to player.number,
                "team" to player.team,
                "kicks" to 0,
                "handballs" to 0,
                "marks" to 0,
                "tackles" to 0,
                "goals" to 0,
                "behinds" to 0,
                "actionTimestamps" to listOf<Map<String, Any>>(),
                "photoUri" to (if (player.photoUri.isNullOrBlank()) null else player.photoUri)
            )
        }

        // Copilot helped debug these hashmaps
        val matchData = hashMapOf(
            "teamAName" to teamAName,
            "teamBName" to teamBName,
            "startedAt" to timestamp,
            "score" to mapOf(
                "teamA" to mapOf(
                    "goals" to 0,
                    "behinds" to 0,
                    "totalScore" to 0),
                "teamB" to mapOf(
                    "goals" to 0,
                    "behinds" to 0,
                    "totalScore" to 0)),
                "playerStats" to playerStats
            )

        db.collection("matches")
            .document(matchId)
            .set(matchData)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Match $matchId started successfully")

                val intent = Intent(this, MatchTrackingActivity::class.java)
                intent.putExtra("matchId", matchId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.w("FIREBASE", "Error starting match: ", exception)
                Toast.makeText(this, "Failed to start match", Toast.LENGTH_SHORT).show()
            }
    }
}