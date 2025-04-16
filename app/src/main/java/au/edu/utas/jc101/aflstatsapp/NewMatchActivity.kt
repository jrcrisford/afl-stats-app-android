package au.edu.utas.jc101.aflstatsapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityNewMatchBinding

data class Player(
    val name: String,
    val number: Int
)

class NewMatchActivity : AppCompatActivity() {
    private lateinit var ui: ActivityNewMatchBinding

    private val teamAPlayers = mutableListOf<Player>()
    private val teamBPlayers = mutableListOf<Player>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityNewMatchBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to NewMatchActivity")

        ui.btnAddPlayerA.setOnClickListener {
            val name = ui.txtPlayerAName.text.toString().trim()
            val number = ui.txtPlayerANumber.text.toString().toIntOrNull()
            val teamName = ui.txtTeamAName.text.toString()

            if (name.isNotEmpty() && number != null) {
                teamAPlayers.add(Player(name, number))
                ui.lblTeamAPlayers.text = "$teamName:"
                ui.btnAddPlayerA.text = "Add Player to $teamName"
                ui.lblTeamAPlayers.append("\n$name (#$number)")

                ui.txtPlayerAName.text.clear()
                ui.txtPlayerANumber.text.clear()
                checkIfReadyToStart()
            } else {
                Toast.makeText(this, "Please enter a valid name and number", Toast.LENGTH_SHORT).show()
            }
        }

        ui.btnAddPlayerB.setOnClickListener {
            val name = ui.txtPlayerBName.text.toString()
            val number = ui.txtPlayerBNumber.text.toString().toIntOrNull()
            val teamName = ui.txtTeamBName.text.toString()

            if (name.isNotEmpty() && number != null) {
                teamAPlayers.add(Player(name, number))
                ui.lblTeamBPlayers.text = "$teamName:"
                ui.btnAddPlayerB.text = "Add Player to $teamName"
                ui.lblTeamBPlayers.append("\n$name (#$number)")

                ui.txtPlayerBName.text.clear()
                ui.txtPlayerBNumber.text.clear()
                checkIfReadyToStart()
            } else {
                Toast.makeText(this, "Please enter a valid name and number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkIfReadyToStart() {
        if (teamAPlayers.size >= 2 && teamBPlayers.size >= 2) {
            Log.d("BUTTON", "Both teams have enough players to start the match")
        } else {
            Log.d("BUTTON", "Not enough players to start the match")
        }
    }
}