package au.edu.utas.jc101.aflstatsapp

import android.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityTeamManagementBinding
import com.google.firebase.firestore.FirebaseFirestore

class TeamManagementActivity : AppCompatActivity() {
    private lateinit var ui: ActivityTeamManagementBinding
    private lateinit var db: FirebaseFirestore

    private val teams = mutableListOf<Team>()
    private var playerList = mutableListOf<Player>()
    private lateinit var playerAdapter: PlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityTeamManagementBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to TeamManagementActivity")

        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView for players
        playerAdapter = PlayerAdapter(playerList) { clickedplayer ->
            editPlayer(clickedplayer)
        }
        ui.recyclerPlayers.layoutManager = LinearLayoutManager(this)
        ui.recyclerPlayers.adapter = playerAdapter

        loadTeams()

        // Create new player
        ui.btnAddPlayer.setOnClickListener {
            PlayerAddEditDialog(
                context = this,
                player = null,
                onPlayerSaved = { newPlayer ->
                    playerList.add(newPlayer)
                    playerAdapter.notifyDataSetChanged()
                }
            ).show()
        }

        // SaveTeam
        ui.btnSaveTeam.setOnClickListener {
            saveTeam()
        }

        val DebugMode = true
        if (DebugMode) {
            addDebugTeam()
        }
    }

    private fun loadTeams() {
        db.collection("teams")
            .get()
            .addOnSuccessListener { documents ->
                teams.clear()

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

        ui.spinnerSelectTeam.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selectedName = parent.getItemAtPosition(position).toString()
                Log.d("DEBUG", "Selected team: $selectedName")

                if (selectedName == "New Team") {
                    ui.edtTeamName.setText("")
                    playerList.clear()
                    playerAdapter.notifyDataSetChanged()
                } else {
                    val selectedTeam = teams.find { it.name == selectedName }
                    if (selectedTeam != null) {
                        loadTeamData(selectedTeam)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })

    }

    private fun loadTeamData(selectedTeam: Team) {
        ui.edtTeamName.setText(selectedTeam.name)

        playerList.clear()
        playerList.addAll(selectedTeam.players)

        playerAdapter.notifyDataSetChanged()
    }

    private fun editPlayer(player: Player) {
        PlayerAddEditDialog(
            context = this,
            player = player,
            onPlayerSaved = { updatedPlayer ->
                val index = playerList.indexOfFirst { it.id == updatedPlayer.id }
                if (index != -1) {
                    playerList[index] = updatedPlayer
                    playerAdapter.notifyDataSetChanged()
                }
            }
        ).show()
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

    //COPILOT
    private fun addDebugTeam() {
        val debugPlayers = mutableListOf<Player>()
        for (i in 1..8) {
            debugPlayers.add(
                Player(
                    id = "debug_player_$i",
                    name = "Debug Player $i",
                    number = i,
                    team = "DebugTeam"
                )
            )
        }

        val debugTeam = Team(name = "DebugTeam", players = debugPlayers)

        // Save to Firestore
        val teamData = hashMapOf(
            "name" to debugTeam.name,
            "players" to debugTeam.players.map { player ->
                hashMapOf(
                    "id" to player.id,
                    "name" to player.name,
                    "number" to player.number
                )
            }
        )

        db.collection("teams")
            .document(debugTeam.name)
            .set(teamData)
            .addOnSuccessListener {
                Log.d("DEBUG", "Debug team uploaded successfully to Firestore")
                // Only update UI after successful upload
                loadTeams()
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE", "Failed to upload debug team: ", exception)
            }
    }
}