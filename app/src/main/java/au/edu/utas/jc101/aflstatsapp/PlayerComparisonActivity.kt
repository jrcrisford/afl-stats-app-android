package au.edu.utas.jc101.aflstatsapp

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityPlayerComparisonBinding

class PlayerComparisonActivity : AppCompatActivity() {
    private lateinit var ui: ActivityPlayerComparisonBinding
    private lateinit var player1: Player
    private lateinit var player2: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlayerComparisonBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //TODO Get player data from intent
        player1 = Player(
            id = "p1",
            name = "Charlie",
            number = 11,
            team = "Dolphins",
            kicks = 10,
            handballs = 5,
            marks = 3,
            tackles = 2,
            goals = 2,
            behinds = 1,
            actionTimestamps = mutableListOf()
        )

        player2 = Player(
            id = "p2",
            name = "Sam",
            number = 22,
            team = "Sharks",
            kicks = 8,
            handballs = 7,
            marks = 4,
            tackles = 3,
            goals = 1,
            behinds = 2,
            actionTimestamps = mutableListOf()
        )

        displayPlayerStats()
        ui.btnReturnPlayerStats.setOnClickListener {
            finish()
        }
    }

    private fun displayPlayerStats() {
        // Set player names
        ui.txtPlayer1Name.text = "${player1.name} (${player1.number})"
        ui.txtPlayer2Name.text = "${player2.name} (${player2.number})"

        // Calculate values
        val disposals1 = player1.kicks + player1.handballs
        val disposals2 = player2.kicks + player2.handballs
        val totalScore1 = player1.goals * 6 + player1.behinds
        val totalScore2 = player2.goals * 6 + player2.behinds

        // Set text elements
        ui.txtPlayer1Disposals.text = disposals1.toString()
        ui.txtPlayer2Disposals.text = disposals2.toString()
        ui.txtPlayer1Marks.text = player1.marks.toString()
        ui.txtPlayer2Marks.text = player2.marks.toString()
        ui.txtPlayer1Tackles.text = player1.tackles.toString()
        ui.txtPlayer2Tackles.text = player2.tackles.toString()
        ui.txtPlayer1Score.text = "${player1.goals}.${player1.behinds} ($totalScore1)"
        ui.txtPlayer2Score.text = "${player2.goals}.${player2.behinds} ($totalScore2)"

        statHighlighting(ui.txtPlayer1Disposals, ui.txtPlayer2Disposals, disposals1, disposals2)
        statHighlighting(ui.txtPlayer1Marks, ui.txtPlayer2Marks, player1.marks, player2.marks)
        statHighlighting(ui.txtPlayer1Tackles, ui.txtPlayer2Tackles, player1.tackles, player2.tackles)
        statHighlighting(ui.txtPlayer1Score, ui.txtPlayer2Score, totalScore1, totalScore2)
    }

    private fun statHighlighting(view1: TextView, view2: TextView, stat1: Int, stat2: Int) =
        if (stat1 > stat2) {
            view1.setTextColor(Color.GREEN)
            view2.setTextColor(Color.BLACK)
        } else if (stat2 > stat1) {
            view2.setTextColor(Color.GREEN)
            view1.setTextColor(Color.BLACK)
        } else {
            view1.setTextColor(Color.BLUE)
            view2.setTextColor(Color.BLUE)
        }
}