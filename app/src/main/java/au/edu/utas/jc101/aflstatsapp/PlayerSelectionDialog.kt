package au.edu.utas.jc101.aflstatsapp

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/**
 * A reusable dialog that allows a user to select two players for comparison of their stats.
 *
 * @param context The context in which the dialog is displayed.
 * @param players The list of players to choose from.
 * @param onPlayerSelected Callback function to be called when two players are selected.
 */
class PlayerSelectionDialog (
    private val context: Context,
    private val players: List<Player>,
    private val onPlayerSelected: (Player, Player) -> Unit
) {
    /**
     * Displays player selection dialog.
     */
    fun show() {
        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_players, null)

        // Find views in the dialog
        val spinnerPlayer1 = dialogView.findViewById<Spinner>(R.id.spinnerPlayer1)
        val spinnerPlayer2 = dialogView.findViewById<Spinner>(R.id.spinnerPlayer2)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmSelection)

        // Prepare the adapter for the player spinners
        val playerNames = players.map { "${it.name} (${it.number})" }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, playerNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerPlayer1.adapter = adapter
        spinnerPlayer2.adapter = adapter

        // Create the alert dialog
        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // Confirm button click logic
        btnConfirm.setOnClickListener {
            val pos1 = spinnerPlayer1.selectedItemPosition
            val pos2 = spinnerPlayer2.selectedItemPosition

            if (pos1 != pos2) {
                val player1 = players[pos1]
                val player2 = players[pos2]
                onPlayerSelected(player1, player2)
                alertDialog.dismiss()
            } else {
                // Warn user if the same player is selected twice
                Toast.makeText(context, "Please select two different players", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        alertDialog.show()
    }
}