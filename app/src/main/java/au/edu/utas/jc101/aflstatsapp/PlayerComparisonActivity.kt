package au.edu.utas.jc101.aflstatsapp

import android.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityPlayerComparisonBinding

class PlayerComparisonActivity : AppCompatActivity() {
    private lateinit var ui: ActivityPlayerComparisonBinding
    private lateinit var player1: Player
    private lateinit var player2: Player
    private var selectedQuarter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlayerComparisonBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to PlayerComparisonActivity")

        // Get player data from intent
        player1 = intent.getParcelableExtra("player1", Player::class.java)!!
        player2 = intent.getParcelableExtra("player2", Player::class.java)!!

        Log.d("DEBUG", "Comparing Player 1: ${player1.name}, Player 2: ${player2.name}")

        val quarters = listOf("Full Match", "Q1", "Q2", "Q3", "Q4")
        val quarterAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, quarters)
        quarterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ui.spinnerQuarterFilter.adapter = quarterAdapter

        ui.spinnerQuarterFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedQuarter = position
                Log.d("DEBUG", "Selected quarter: $selectedQuarter")
                displayPlayerStats()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Display the loaded player stats
        displayPlayerStats()

        // Set up the return button to go back to the previous activity
        ui.btnReturnPlayerStats.setOnClickListener {
            Log.d("DEBUG", "Return to player stats button clicked")
            finish()
        }
    }

    /**
     * Displays the player stats in the UI and applies highlighting
     * to the stats based on the comparison between the two players.
     */
    private fun displayPlayerStats() {
        Log.d("DEBUG", "Displaying player stats for comparison")

        // Apply filtering based on selected quarter
        val filtered1 = if (selectedQuarter == 0) player1 else player1.copy(
            kicks = player1.actionTimestamps.count {
                it["action"] == "kick" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            handballs = player1.actionTimestamps.count {
                it["action"] == "handball" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            marks = player1.actionTimestamps.count {
                it["action"] == "mark" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            tackles = player1.actionTimestamps.count {
                it["action"] == "tackle" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            goals = player1.actionTimestamps.count {
                it["action"] == "goal" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            behinds = player1.actionTimestamps.count {
                it["action"] == "behind" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter }
        )

        val filtered2 = if (selectedQuarter == 0) player2 else player2.copy(
            kicks = player2.actionTimestamps.count {
                it["action"] == "kick" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            handballs = player2.actionTimestamps.count {
                it["action"] == "handball" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            marks = player2.actionTimestamps.count {
                it["action"] == "mark" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            tackles = player2.actionTimestamps.count {
                it["action"] == "tackle" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            goals = player2.actionTimestamps.count {
                it["action"] == "goal" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter },
            behinds = player2.actionTimestamps.count {
                it["action"] == "behind" && (it["quarter"]
                as? Long)?.toInt() == selectedQuarter }
        )

        // Set player names
        ui.txtPlayer1Name.text = "${filtered1.name} (${filtered1.number})"
        ui.txtPlayer2Name.text = "${filtered2.name} (${filtered2.number})"

        // Calculate values
        val disposals1 = filtered1.kicks + filtered1.handballs
        val disposals2 = filtered2.kicks + filtered2.handballs
        val totalScore1 = filtered1.goals * 6 + filtered1.behinds
        val totalScore2 = filtered2.goals * 6 + filtered2.behinds

        Log.d("DEBUG",
            "Player 1 - " +
                "Disposals: $disposals1, " +
                "Marks: ${filtered1.marks}, " +
                "Tackles: ${filtered1.tackles}, " +
                "Score: $totalScore1")
        Log.d("DEBUG",
            "Player 2 - " +
                "Disposals: $disposals2, " +
                "Marks: ${filtered2.marks}, " +
                "Tackles: ${filtered2.tackles}, " +
                "Score: $totalScore2")

        // Set stat values in the UI
        ui.txtPlayer1Disposals.text = disposals1.toString()
        ui.txtPlayer2Disposals.text = disposals2.toString()
        ui.txtPlayer1Marks.text = filtered1.marks.toString()
        ui.txtPlayer2Marks.text = filtered2.marks.toString()
        ui.txtPlayer1Tackles.text = filtered1.tackles.toString()
        ui.txtPlayer2Tackles.text = filtered2.tackles.toString()
        ui.txtPlayer1Score.text = "${filtered1.goals}.${filtered1.behinds} ($totalScore1)"
        ui.txtPlayer2Score.text = "${filtered2.goals}.${filtered2.behinds} ($totalScore2)"

        // Highlight the better player for each stat
        statHighlighting(ui.txtPlayer1Disposals, ui.txtPlayer2Disposals, disposals1, disposals2)
        statHighlighting(ui.txtPlayer1Marks, ui.txtPlayer2Marks, filtered1.marks, filtered2.marks)
        statHighlighting(ui.txtPlayer1Tackles, ui.txtPlayer2Tackles, filtered1.tackles, filtered2.tackles)
        statHighlighting(ui.txtPlayer1Score, ui.txtPlayer2Score, totalScore1, totalScore2)
    }

    /**
     * Highlights the better player's stat in green and the other player's stat in white.
     * If both stats are equal, both are highlighted in blue.
     */
    private fun statHighlighting(view1: TextView, view2: TextView, stat1: Int, stat2: Int) =
        if (stat1 > stat2) {
            view1.setTextColor(Color.GREEN)
            view2.setTextColor(Color.WHITE)
        } else if (stat2 > stat1) {
            view2.setTextColor(Color.GREEN)
            view1.setTextColor(Color.WHITE)
        } else {
            view1.setTextColor(Color.BLUE)
            view2.setTextColor(Color.BLUE)
        }
}