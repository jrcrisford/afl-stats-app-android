package au.edu.utas.jc101.aflstatsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private val players: List<Player>,
    private val onPlayerClicked: (Player) -> Unit) :
    RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPlayerNameNumber: TextView = view.findViewById(R.id.txtPlayerNameNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.txtPlayerNameNumber.text = "${player.name} (#${player.number})"
        holder.itemView.setOnClickListener {onPlayerClicked(player)}
    }

    override fun getItemCount(): Int {
        return players.size
    }
}