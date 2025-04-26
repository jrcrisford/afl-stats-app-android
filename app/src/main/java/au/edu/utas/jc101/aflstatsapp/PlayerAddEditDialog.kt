package au.edu.utas.jc101.aflstatsapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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
    private var selectedPhotoUri: Uri? = null
    private lateinit var  photoPreview: ImageView

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>

    /**
     * Shows the dialog for adding or editing a player.
     */
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_player, null)

        // Get UI components
        val edtName = dialogView.findViewById<EditText>(R.id.edtPlayerName)
        val edtNumber = dialogView.findViewById<EditText>(R.id.edtPlayerNumber)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSavePlayer)
        val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
        photoPreview = dialogView.findViewById(R.id.imgPlayerPhotoPreview)

        // If editing player, populate fields with existing data
        if (player != null) {
            edtName.setText(player.name)
            edtNumber.setText(player.number.toString())
            if (player.photoUri != null) {
                photoPreview.setImageURI(Uri.parse(player.photoUri))
                photoPreview.visibility = ImageView.VISIBLE
            }
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
                number = number,
                photoUri = selectedPhotoUri?.toString() ?: player.photoUri
            ) ?: Player(
                id = UUID.randomUUID().toString(), // Generate a unique ID
                name = name,
                number = number,
                team = "", // To be set later
                photoUri = selectedPhotoUri?.toString()
            )

            onPlayerSaved(savedPlayer)
            alertDialog.dismiss()
        }
        btnSelectPhoto.setOnClickListener {
            showPhotoOptions()
        }

        alertDialog.show()
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Take Photo", "Select from Gallery")
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setTitle("Choose Photo Source")
        alertBuilder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        alertBuilder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (context is Activity) {
            (context as Activity).startActivityForResult(cameraIntent, 1001) //COPLILOT
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (context is Activity) {
            (context as Activity).startActivityForResult(galleryIntent, 1002) //COPLILOT
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            1001 -> {
                val bitmap = data?.extras?.get("data") as? Bitmap //COPILOT
                if (bitmap != null) {
                    val uri = saveBitmap(bitmap)
                    if (uri != null) {
                        selectedPhotoUri = uri
                        photoPreview.setImageURI(uri)
                        photoPreview.visibility = ImageView.VISIBLE
                    }
                }
            }

            1002 -> {
                val uri = data?.data
                if (uri != null) {
                    selectedPhotoUri = uri
                    photoPreview.setImageURI(uri)
                    photoPreview.visibility = ImageView.VISIBLE
                }
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap): Uri? {
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Player Photo",
            null
        ) //COPILOT
        return  if (path != null) {
            Uri.parse(path)
        } else {
            null
        }
    }
}