package au.edu.utas.jc101.aflstatsapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

        Log.d("NAVIGATION", "Navigated to PlayerComparisonActivity")

        // Get player data from intent
        player1 = intent.getParcelableExtra("player1", Player::class.java)!!
        player2 = intent.getParcelableExtra("player2", Player::class.java)!!

        Log.d("DEBUG", "Comparing Player 1: ${player1.name}, Player 2: ${player2.name}")

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

        // Set player names
        ui.txtPlayer1Name.text = "${player1.name} (${player1.number})"
        ui.txtPlayer2Name.text = "${player2.name} (${player2.number})"

        // Calculate values
        val disposals1 = player1.kicks + player1.handballs
        val disposals2 = player2.kicks + player2.handballs
        val totalScore1 = player1.goals * 6 + player1.behinds
        val totalScore2 = player2.goals * 6 + player2.behinds

        Log.d("DEBUG", "Player 1 - Disposals: $disposals1, Marks: ${player1.marks}, Tackles: ${player1.tackles}, Score: $totalScore1")
        Log.d("DEBUG", "Player 2 - Disposals: $disposals2, Marks: ${player2.marks}, Tackles: ${player2.tackles}, Score: $totalScore2")

        // Set stat values in the UI
        ui.txtPlayer1Disposals.text = disposals1.toString()
        ui.txtPlayer2Disposals.text = disposals2.toString()
        ui.txtPlayer1Marks.text = player1.marks.toString()
        ui.txtPlayer2Marks.text = player2.marks.toString()
        ui.txtPlayer1Tackles.text = player1.tackles.toString()
        ui.txtPlayer2Tackles.text = player2.tackles.toString()
        ui.txtPlayer1Score.text = "${player1.goals}.${player1.behinds} ($totalScore1)"
        ui.txtPlayer2Score.text = "${player2.goals}.${player2.behinds} ($totalScore2)"

        // Highlight the better player for each stat
        statHighlighting(ui.txtPlayer1Disposals, ui.txtPlayer2Disposals, disposals1, disposals2)
        statHighlighting(ui.txtPlayer1Marks, ui.txtPlayer2Marks, player1.marks, player2.marks)
        statHighlighting(ui.txtPlayer1Tackles, ui.txtPlayer2Tackles, player1.tackles, player2.tackles)
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