package au.edu.utas.jc101.aflstatsapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import au.edu.utas.jc101.aflstatsapp.databinding.DialogAddEditPlayerBinding
import java.util.UUID

class PlayerAddEditDialog (
    private val context: Context,
    private val player: Player?, // null if creating a new player
    private val onPlayerSaved: (Player) -> Unit
) {
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_player, null)

        val edtName = dialogView.findViewById<EditText>(R.id.edtPlayerName)
        val edtNumber = dialogView.findViewById<EditText>(R.id.edtPlayerNumber)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSavePlayer)

        if (player != null) {
            edtName.setText(player.name)
            edtNumber.setText(player.number.toString())
        }

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(if (player == null) "Add Player" else "Edit Player")
            .create()

        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val numberText = edtNumber.text.toString().trim()

            if (name.isEmpty() || numberText.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val number = numberText.toIntOrNull()
            if (number == null) {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPlayer = player?.copy(
                name = name,
                number = number
            ) ?: Player(
                id = UUID.randomUUID().toString(),
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