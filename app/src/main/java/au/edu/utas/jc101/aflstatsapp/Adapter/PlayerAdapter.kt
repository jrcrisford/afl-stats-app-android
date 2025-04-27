package au.edu.utas.jc101.aflstatsapp

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * PlayerAdapter is a RecyclerView adapter for displaying a list of players.
 *
 * @param players List of players to display.
 * @param onPlayerClicked Lambda function to handle player click events.
 * @param onPlayerLongClicked Lambda function to handle player long click events.
 */
class PlayerAdapter(
    private val players: List<Player>,
    private val onPlayerClicked: (Player) -> Unit,
    private val onPlayerLongClicked: (Player) -> Unit) :
    RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    /**
     * ViewHolder for player items.
     *
     * @param view The view for the player item.
     */
    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPlayerNameNumber: TextView = view.findViewById(R.id.txtPlayerNameNumber)
    }

    /**
     * Inflates the layout for each item.
     *
     * @param parent The parent view group.
     * @param viewType The view type of the new ViewHolder.
     * @return A new PlayerViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        // Copilot created this function
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
        return PlayerViewHolder(view)
    }

    /**
     * Binds the player data to the corresponding item view holder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        // Copilot assisted with debugging this
        val player = players[position]
        holder.txtPlayerNameNumber.text = "${player.name} (#${player.number})"
        holder.txtPlayerNameNumber.setTextColor(Color.WHITE)

        // Handle click for editing player
        holder.itemView.setOnClickListener {onPlayerClicked(player)}

        // Handle long click for deleting player with a confirmation dialog
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

    /**
     * Returns the total number of players in the list.
     *
     * @return The size of the players list.
     */
    override fun getItemCount(): Int {
        return players.size
    }
}