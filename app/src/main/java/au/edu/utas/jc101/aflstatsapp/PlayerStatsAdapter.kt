package au.edu.utas.jc101.aflstatsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerStatsAdapter(private val players: List<Player>) : RecyclerView.Adapter<PlayerStatsAdapter.PlayerStatViewHolder>() {

    class PlayerStatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerName: TextView = view.findViewById(R.id.txtPlayerNameNumber)
        val disposals: TextView = view.findViewById(R.id.txtDisposals)
        val marks: TextView = view.findViewById(R.id.txtMarks)
        val tackles: TextView = view.findViewById(R.id.txtTackles)
        val score: TextView = view.findViewById(R.id.txtScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerStatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_stat_item, parent, false)
        return PlayerStatViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerStatViewHolder, position: Int) {
        val player = players[position]
        val disposals = player.kicks + player.handballs
        val totalScore = player.goals * 6 + player.behinds

        holder.playerName.text = "${player.name} (${player.number})"
        holder.disposals.text = "Disposals: $disposals"
        holder.marks.text = "Marks: ${player.marks}"
        holder.tackles.text = "Tackles: ${player.tackles}"
        holder.score.text = "Score: ${player.goals}.${player.behinds} (${totalScore})"
    }

    override fun getItemCount(): Int {
        return players.size
    }
}