package au.edu.utas.jc101.aflstatsapp

import android.R
import android.content.Intent
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

    // All saved teams in Firestore
    private val teams = mutableListOf<Team>()
    // Players in the selected team
    private var playerList = mutableListOf<Player>()
    private lateinit var playerAdapter: PlayerAdapter
    // Tracks original name for rename operation
    private var originalTeamName: String? = null
    private var activePlayerDialog: PlayerAddEditDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityTeamManagementBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to TeamManagementActivity")

        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView for players
        playerAdapter = PlayerAdapter(
            playerList,
            onPlayerClicked = { clickedPlayer -> editPlayer(clickedPlayer) },
            onPlayerLongClicked = { longClickedPlayer ->
                playerList.removeIf { it.id == longClickedPlayer.id }
                sortPlayerList()
                Toast.makeText(this, "${longClickedPlayer.name} deleted", Toast.LENGTH_SHORT).show()
                Log.d("DEBUG", "Player ${longClickedPlayer.name} deleted")
            }
        )
        ui.recyclerPlayers.layoutManager = LinearLayoutManager(this)
        ui.recyclerPlayers.adapter = playerAdapter

        // Set up Spinner for team selection
        val sortOptions = listOf("Name A-Z", "Name Z-A", "Jersy Number")
        val sortAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        ui.spinnerSortPlayers.adapter = sortAdapter

        // Apply sorting to players
        ui.spinnerSortPlayers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d("DEBUG", "Sort option selected: ${sortOptions[position]}")
                sortPlayerList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Load existing teams from Firestore
        loadTeams()

        // Create new player
        ui.btnAddPlayer.setOnClickListener {
            activePlayerDialog = PlayerAddEditDialog(
                context = this,
                player = null,
                onPlayerSaved = { savedPlayer ->
                    playerList.add(savedPlayer)
                    sortPlayerList()
                }
            )
            activePlayerDialog?.show()
        }

        // Save Team
        ui.btnSaveTeam.setOnClickListener {
            saveTeam()
        }

        // DEBUG: Add a debug team
        val DebugMode = false
        if (DebugMode) {
            addDebugTeam()
        }
    }

    /**
     * Load teams from Firestore and populate the spinner.
     */
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
                            team = teamName,
                            photoUri = it["photoUri"] as? String
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

    /**
     * Update the team spinner with the list of teams.
     * Also handles the selection of a team.
     */
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

    /**
     * Load the currently selected team's data into the UI.
     *
     * @param selectedTeam The team to load data for.
     */
    private fun loadTeamData(selectedTeam: Team) {
        ui.edtTeamName.setText(selectedTeam.name)

        playerList.clear()
        playerList.addAll(selectedTeam.players)

        playerAdapter.notifyDataSetChanged()
        originalTeamName = selectedTeam.name
        Log.d("DEBUG", "Loaded team data for: ${selectedTeam.name}")
    }

    /**
     * Launches a dialog to edit a player's details.
     *
     * @param player The player to edit.
     */
    private fun editPlayer(player: Player) {
        activePlayerDialog = PlayerAddEditDialog(
            context = this,
            player = player,
            onPlayerSaved = { updatedPlayer ->
                val index = playerList.indexOfFirst { it.id == updatedPlayer.id }
                if (index != -1) {
                    playerList[index] = updatedPlayer
                    sortPlayerList()
                    Log.d("DEBUG", "Player ${updatedPlayer.name} updated")
                }
            }
        )
        activePlayerDialog?.show()
    }

    /**
     * Sorts the player list based on the selected option in the spinner.
     */
    private fun sortPlayerList() {
        when (ui.spinnerSortPlayers.selectedItemPosition) {
            0 -> {
                playerList.sortBy { it.name }
                playerAdapter.notifyDataSetChanged()
                Log.d("DEBUG", "Sorted players A-Z")
            }
            1 -> {
                playerList.sortByDescending { it.name }
                playerAdapter.notifyDataSetChanged()
                Log.d("DEBUG", "Sorted players Z-A")
            }
            2 -> {
                playerList.sortBy { it.number }
                playerAdapter.notifyDataSetChanged()
                Log.d("DEBUG", "Sorted players by jersey number")
            }
            else -> {
                Log.w("DEBUG", "Unknown sort option selected")
            }
        }
        playerAdapter.notifyDataSetChanged()
    }

    /**
     * Saves the current team to Firestore.
     */
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
                    "number" to player.number,
                    "photoUri" to player.photoUri
                )
            }
        )

        val batch = db.batch()

        if (originalTeamName != null && originalTeamName != teamName) {
            val oldDocument = db.collection("teams").document(originalTeamName!!)
            batch.delete(oldDocument)
            Log.d("DEBUG", "Deleted old team document: $originalTeamName")
        }

        val newDocument = db.collection("teams").document(teamName)
        batch.set(newDocument, teamData)

        batch.commit()
            .addOnSuccessListener {
                Log.d("DEBUG", "Team $teamName saved successfully")
                loadTeams()
                originalTeamName = teamName
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

    //COPILOT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activePlayerDialog?.handleResult(requestCode, resultCode, data)
    }
}