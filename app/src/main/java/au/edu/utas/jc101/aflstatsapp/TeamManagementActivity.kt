package au.edu.utas.jc101.aflstatsapp

import android.R
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityTeamManagementBinding
import com.google.firebase.firestore.FirebaseFirestore

class TeamManagementActivity : AppCompatActivity() {
    private lateinit var ui: ActivityTeamManagementBinding
    private lateinit var db: FirebaseFirestore

    private val teams = mutableListOf<Team>()
    private var playerList = mutableListOf<Player>()
    //private lateinit var playerAdapter: PlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityTeamManagementBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to TeamManagementActivity")

        db = FirebaseFirestore.getInstance()

        // TODO Set up RecyclerView for players

        loadTeams()

        // Create new player
        ui.btnAddPlayer.setOnClickListener {
            // TODO PlayerEditDialog()
        }

        // SaveTeam
        ui.btnSaveTeam.setOnClickListener {
            saveTeam()
        }
    }

    private fun loadTeams() {
        db.collection("teams")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val teamName = document.getString("name") ?: continue
                    val playersData = document.get("players") as? List<Map<String, Any>> ?: listOf()
                    val players = playersData.map {
                        Player(
                            id = it["id"] as String ?: "",
                            name = it["name"] as String ?: "",
                            number = (it["number"] as? Long)?.toInt() ?: 0,
                            team = teamName
                        )
                    }.toMutableList()

                    teams.add(Team(name = teamName, players = players))
                }
                updateTeamSpinner()
            }
            .addOnFailureListener() { exception ->
                Log.e("FIREBASE", "Failed to load teams: ", exception)
            }
    }

    private fun updateTeamSpinner() {
        val teamNames = mutableListOf<String>("New Team")
        teamNames.addAll(teams.map { it.name })

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, teamNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ui.spinnerSelectTeam.adapter = adapter

        Log.d("DEBUG", "Team spinner updated with teams: $teamNames")
    }

    private fun loadTeamData() {
        // TODO Loads the selected team data into the UI
    }

    private fun editPlayer() {
        // TODO Edit player
    }

    private fun saveTeam() {
        val teamName = ui.edtTeamName.text.toString().trim()

        if (teamName.isEmpty()) {
            Log.w("DEBUG", "Team name is empty")
            return
        }
        if (playerList.size < 2) {
            Log.w("DEBUG", "Team cannot be saved with less than 2 players")
            Toast.makeText(this, "Team must have at least 2 players", Toast.LENGTH_SHORT).show()
            return
        }

        val teamData = hashMapOf(
            "name" to teamName,
            "players" to playerList.map { player ->
                hashMapOf(
                    "id" to player.id,
                    "name" to player.name,
                    "number" to player.number
                )
            }
        )

        db.collection("teams")
            .document(teamName)
            .set(teamData)
            .addOnSuccessListener {
                Log.d("DEBUG", "Team $teamName saved successfully")
                loadTeams()
            }
            .addOnFailureListener() { exception ->
                Log.e("FIREBASE", "Failed to save team: ", exception)
            }
    }
}