package au.edu.utas.jc101.aflstatsapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private val players: List<Player>,
    private val onPlayerClicked: (Player) -> Unit,
    private val onPlayerLongClicked: (Player) -> Unit) :
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
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Player")
                .setMessage("Are you sure you want to delete: \n${player.name}?")
                .setPositiveButton("Yes") { _, _ -> onPlayerLongClicked(player) }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int {
        return players.size
    }
}