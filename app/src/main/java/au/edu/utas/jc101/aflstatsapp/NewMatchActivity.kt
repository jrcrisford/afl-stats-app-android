package au.edu.utas.jc101.aflstatsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityNewMatchBinding
import com.google.firebase.firestore.FirebaseFirestore

class NewMatchActivity : AppCompatActivity() {
    private lateinit var ui: ActivityNewMatchBinding
    private val teamAPlayers = mutableListOf<Player>()
    private val teamBPlayers = mutableListOf<Player>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityNewMatchBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to NewMatchActivity")

        // DEBUG: Prefill test data
        val debugMode = true
        if (debugMode) {
            prefillData()
        }

        // Add player to Team A and update UI
        ui.btnAddPlayerA.setOnClickListener {
            val name = ui.txtPlayerAName.text.toString().trim()
            val number = ui.txtPlayerANumber.text.toString().toIntOrNull()
            val teamName = ui.txtTeamAName.text.toString().trim()

            if (name.isNotEmpty() && number != null && teamName.isNotEmpty()) {
                // Add player to Team A
                val newPlayer = createPlayer(name, number, teamName)
                teamAPlayers.add(newPlayer)
                Log.d("DEBUG", "Added player: $name (#$number) to $teamName")

                // Update UI for Team A
                ui.btnAddPlayerA.text = "Add Player to $teamName"
                if (teamAPlayers.size == 1) {
                    ui.lblTeamAPlayers.text = "$teamName:\n$name (#$number)"
                } else {
                    ui.lblTeamAPlayers.append("\n$name (#$number)")
                }

                // Clear input fields for Team A
                ui.txtPlayerAName.text.clear()
                ui.txtPlayerANumber.text.clear()

                // Check if both teams have enough players to start the match
                checkIfReadyToStart()
            } else {
                Toast.makeText(this, "Please enter a valid name and number", Toast.LENGTH_SHORT).show()
            }
        }

        // Add player to Team B and update UI
        ui.btnAddPlayerB.setOnClickListener {
            val name = ui.txtPlayerBName.text.toString().trim()
            val number = ui.txtPlayerBNumber.text.toString().toIntOrNull()
            val teamName = ui.txtTeamBName.text.toString().trim()

            if (name.isNotEmpty() && number != null && teamName.isNotEmpty()) {
                // Add player to Team B
                val newPlayer = createPlayer(name, number, teamName)
                teamBPlayers.add(newPlayer)
                Log.d("DEBUG", "Added player: $name (#$number) to $teamName")

                // Update UI for Team B
                ui.btnAddPlayerB.text = "Add Player to $teamName"
                if (teamBPlayers.size == 1) {
                    ui.lblTeamBPlayers.text = "$teamName:\n$name (#$number)"
                } else {
                    ui.lblTeamBPlayers.append("\n$name (#$number)")
                }

                // Clear input fields for Team B
                ui.txtPlayerBName.text.clear()
                ui.txtPlayerBNumber.text.clear()

                // Check if both teams have enough players to start the match
                checkIfReadyToStart()
            } else {
                Toast.makeText(this, "Please enter a valid name and number", Toast.LENGTH_SHORT).show()
            }
        }

        // Start match and save to Firestore
        ui.btnStartMatch.setOnClickListener {
            val teamAName = ui.txtTeamAName.text.toString().trim()
            val teamBName  = ui.txtTeamBName.text.toString().trim()
            val timestamp = com.google.firebase.Timestamp.now()
            val allPlayers = teamAPlayers + teamBPlayers
            val playerStats = mutableMapOf<String, Any>()

            // Check if both teams have names
            if (teamAName.isEmpty() || teamBName.isEmpty()) {
                Toast.makeText(this, "Please enter both team names", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate unique player IDs and store player stats
            allPlayers.forEach { player ->
                val playerID = generatePlayerID(player)
                playerStats[playerID] = player
            }

            // Create match data to save to Firestore
            val matchData = hashMapOf(
                "teamAName" to teamAName,
                "teamBName" to teamBName,
                "startedAt" to timestamp,
                "score" to mapOf(
                    "teamA" to mapOf("goals" to 0, "behinds" to 0),
                    "teamB" to mapOf("goals" to 0, "behinds" to 0)),
                "playerStats" to playerStats
                )

            // Create a matchId
            val matchId = "$teamAName vs $teamBName"
            Log.d("FIREBASE", "Saving match data with ID: $matchId")

            // Save match data to Firestore
            db.collection("matches")
                .document(matchId)
                .set(matchData)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "Match saved with ID: $matchId")
                    Toast.makeText(this, "Match Created.", Toast.LENGTH_SHORT).show()

                    // Navigate to MatchTrackingActivity
                    val intent = Intent(this, MatchTrackingActivity::class.java)
                    intent.putExtra("matchId", matchId)
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
                    Log.w("FIREBASE", "Failed to save match", exception)
                    Toast.makeText(this, "Failed to save match.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function that creates a Player object and assigns a unique ID
    private fun createPlayer(name: String, number: Int, team: String): Player {
        val tempPlayer = Player(
            id = "",
            name = name,
            number = number,
            team = team
        )
        return tempPlayer.copy(id = generatePlayerID(tempPlayer))
    }

    // Helper function to generate a unique player ID
    private fun generatePlayerID(player: Player): String {
        return "${player.name}_${player.number}_${player.team}"
    }

    // DEBUG: Prefill test data for debugging
    private fun prefillData() {
        // Create Player objects
        teamAPlayers.addAll(
            listOf(
                createPlayer("Charlie", 11, "Dolphins"),
                createPlayer("Alice", 12, "Dolphins"),
                createPlayer("Eve", 13, "Dolphins"),
                createPlayer("Grace", 14, "Dolphins")
            )
        )

        teamBPlayers.addAll(
            listOf(
                createPlayer("Daniel", 41, "Tigers"),
                createPlayer("Bob", 42, "Tigers"),
                createPlayer("Frank", 43, "Tigers"),
                createPlayer("Heidi", 44, "Tigers")
            )
        )

        // Set team names manually
        ui.txtTeamAName.setText("Dolphins")
        ui.txtTeamBName.setText("Tigers")

        // Update the team list UI manually
        ui.lblTeamAPlayers.text = "Dolphins:\n" + teamAPlayers.joinToString("\n") { "${it.name} (#${it.number})" }
        ui.lblTeamBPlayers.text = "Tigers:\n" + teamBPlayers.joinToString("\n") { "${it.name} (#${it.number})" }

        // Update button text manually
        ui.btnAddPlayerA.text = "Add Player to Dolphins"
        ui.btnAddPlayerB.text = "Add Player to Tigers"
    }

    // Check if both teams have enough players to start the match
    private fun checkIfReadyToStart() {
        if (teamAPlayers.size >= 2 && teamBPlayers.size >= 2) {
            Log.d("DEBUG", "Both teams have enough players to start the match")
        } else {
            Log.d("DEBUG", "Not enough players to start the match")
        }
    }
}