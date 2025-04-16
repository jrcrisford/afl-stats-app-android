package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityNewMatchBinding
import com.google.firebase.firestore.FirebaseFirestore

// Player class containing player details and game stats
data class Player(
    val name: String,
    val number: Int,
    val team: String,
    val kicks: Int = 0,
    val handballs: Int = 0,
    val marks: Int = 0,
    val tackles: Int = 0,
    val goals: Int = 0,
    val behinds: Int = 0,
)

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

        // Add player to Team A and update UI
        ui.btnAddPlayerA.setOnClickListener {
            val name = ui.txtPlayerAName.text.toString().trim()
            val number = ui.txtPlayerANumber.text.toString().toIntOrNull()
            val teamName = ui.txtTeamAName.text.toString().trim()

            if (name.isNotEmpty() && number != null && teamName.isNotEmpty()) {
                // Add player to Team A
                teamAPlayers.add(Player(name, number, teamName))
                Log.d("PLAYER", "Added player: $name (#$number) to $teamName")

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
                teamBPlayers.add(Player(name, number, teamName))
                Log.d("PLAYER", "Added player: $name (#$number) to $teamName")

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
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE", "Failed to save match", e)
                    Toast.makeText(this, "Failed to save match.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Check if both teams have enough players to start the match
    private fun checkIfReadyToStart() {
        if (teamAPlayers.size >= 2 && teamBPlayers.size >= 2) {
            Log.d("BUTTON", "Both teams have enough players to start the match")
        } else {
            Log.d("BUTTON", "Not enough players to start the match")
        }
    }

    // Helper function to generate a unique player ID
    private fun generatePlayerID(player: Player): String {
        return "${player.team}_${player.number}_${player.name.hashCode()}"
    }
}