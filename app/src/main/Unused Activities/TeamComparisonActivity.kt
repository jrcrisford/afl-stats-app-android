package au.edu.utas.jc101.aflstatsapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityTeamComparisonBinding

class TeamComparisonActivity : AppCompatActivity() {
    private lateinit var ui: ActivityTeamComparisonBinding
    private lateinit var players: List<Player>
    private lateinit var teamAName: String
    private lateinit var teamBName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityTeamComparisonBinding.inflate(layoutInflater)
        setContentView(ui.root)

        Log.d("NAVIGATION", "Navigated to TeamComparisonActivity")

        // Get players and team names from intent
        players = intent.getParcelableArrayListExtra("players", Player::class.java) ?: listOf()
        teamAName = intent.getStringExtra("teamAName") ?: "Team A"
        teamBName = intent.getStringExtra("teamBName") ?: "Team B"

        Log.d("DEBUG", "Loaded players: ${players.size} total for comparison between [$teamAName] and [$teamBName]")

        // Display the loaded team stats
        displayTeamStats()

        // Set up the return button to go back to the previous activity
        ui.btnReturnPlayerStats.setOnClickListener {
            Log.d("NAVIGATION", "Return to PlayerStatsActivity triggered")
            finish()
        }
    }

    /**
     * Displays an aggregated view of the team stats in the UI and applies
     * highlighting to the better stat for each team.
     */
    private fun displayTeamStats() {
        Log.d("DEBUG", "Displaying team stats for comparison")

        // Split players into two teams
        val teamAPlayers = players.filter { it.team == teamAName }
        val teamBPlayers = players.filter { it.team == teamBName }

        // Calculate values for Team A
        val disposalsA = teamAPlayers.sumOf { it.kicks + it.handballs }
        val marksA = teamAPlayers.sumOf { it.marks }
        val tacklesA = teamAPlayers.sumOf { it.tackles }
        val scoreA = teamAPlayers.sumOf { it.goals * 6 + it.behinds }

        // Calculate values for Team B
        val disposalsB = teamBPlayers.sumOf { it.kicks + it.handballs }
        val marksB = teamBPlayers.sumOf { it.marks }
        val tacklesB = teamBPlayers.sumOf { it.tackles }
        val scoreB = teamBPlayers.sumOf { it.goals * 6 + it.behinds }

        Log.d("DEBUG", "Team A [$teamAName] - Disposals: $disposalsA, Marks: $marksA, Tackles: $tacklesA, Score: $scoreA")
        Log.d("DEBUG", "Team B [$teamBName] - Disposals: $disposalsB, Marks: $marksB, Tackles: $tacklesB, Score: $scoreB")

        // Set team names
        ui.txtTeamAName.text = teamAName
        ui.txtTeamBName.text = teamBName

        // Set stat values in the UI
        ui.txtTeamADisposals.text = disposalsA.toString()
        ui.txtTeamBDisposals.text = disposalsB.toString()
        ui.txtTeamAMarks.text = marksA.toString()
        ui.txtTeamBMarks.text = marksB.toString()
        ui.txtTeamATackles.text = tacklesA.toString()
        ui.txtTeamBTackles.text = tacklesB.toString()
        ui.txtTeamAScore.text = formatScore(teamAPlayers)
        ui.txtTeamBScore.text = formatScore(teamBPlayers)

        // Highlight the better team for each stat
        statHighlighting(ui.txtTeamADisposals, ui.txtTeamBDisposals, disposalsA, disposalsB)
        statHighlighting(ui.txtTeamAMarks, ui.txtTeamBMarks, marksA, marksB)
        statHighlighting(ui.txtTeamATackles, ui.txtTeamBTackles, tacklesA, tacklesB)
        statHighlighting(ui.txtTeamAScore, ui.txtTeamBScore, scoreA, scoreB)
    }

    /**
     * Highlights the better team's stat in green and the other team's stat in white.
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

    /**
     * Formats the score for display, converting goals and behinds into a string
     * in the Goals.Behinds (Total) format.
     */
    private fun formatScore(players: List<Player>): String {
        val goals = players.sumOf { it.goals }
        val behinds = players.sumOf { it.behinds }
        val total = goals * 6 + behinds
        return "$goals.$behinds ($total)"
    }
}