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
    private var selectedTeam: String = "Both"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlayerStatsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to PlayerStatsActivity")

        db = FirebaseFirestore.getInstance()

        ui.playerStatsRecyclerView.layoutManager = LinearLayoutManager(this)

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