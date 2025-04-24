package au.edu.utas.jc101.aflstatsapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.UUID

/**
 * A reusable dialog for adding or editing a player.
 *
 * @param context The context in which the dialog is displayed.
 * @param player The player to edit, or null if creating a new player.
 * @param onPlayerSaved Callback function to be called when the player is saved.
 */
class PlayerAddEditDialog (
    private val context: Context,
    private val player: Player?, // null if creating a new player
    private val onPlayerSaved: (Player) -> Unit //Callback with saved player
) {
    /**
     * Shows the dialog for adding or editing a player.
     */
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_player, null)

        // Get UI components
        val edtName = dialogView.findViewById<EditText>(R.id.edtPlayerName)
        val edtNumber = dialogView.findViewById<EditText>(R.id.edtPlayerNumber)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSavePlayer)

        // If editing player, populate fields with existing data
        if (player != null) {
            edtName.setText(player.name)
            edtNumber.setText(player.number.toString())
        }

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(if (player == null) "Add Player" else "Edit Player")
            .create()

        // Set up the save button
        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val numberText = edtNumber.text.toString().trim()

            // Validate fields, both must be filled
            if (name.isEmpty() || numberText.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate number, must be a valid integer
            val number = numberText.toIntOrNull()
            if (number == null) {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a new updated player object
            val savedPlayer = player?.copy(
                name = name,
                number = number
            ) ?: Player(
                id = UUID.randomUUID().toString(), // Generate a unique ID
                name = name,
                number = number,
                team = "" // To be set later
            )

            onPlayerSaved(savedPlayer)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}