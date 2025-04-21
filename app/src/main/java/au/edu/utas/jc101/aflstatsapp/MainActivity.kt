package au.edu.utas.jc101.aflstatsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity()
{
    private lateinit var ui : ActivityMainBinding

    override fun onCreate(SavedInstanceState: Bundle?)
    {
        super.onCreate(SavedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.lblMainTitle.text = "Welcome to the AFL Stats App"

        Log.d("NAVIGATION", "Navigated to ActivityMain")

        val db = Firebase.firestore
        Log.d("FIREBASE", "Firebase connected: ${db.app.name}")

        val testData = hashMapOf(
            "message" to "Hello, World!",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("Test").document("testData")
            .set(testData)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Document successfully written to Firestore!")
            }
            .addOnFailureListener { e ->
                Log.w("FIREBASE", "Error writing document to Firestore", e)
            }

        ui.btnNewMatch.setOnClickListener {
            Log.d("DEBUG", "New Match button clicked")
            val intent = Intent(this, NewMatchActivity::class.java)
            startActivity(intent)
        }

        ui.btnViewStats.setOnClickListener {
            Log.d("DEBUG", "View Stats button clicked")
            val intent = Intent(this, PlayerStatsActivity::class.java)
            startActivity(intent)
        }

        ui.btnViewMatchHistory.setOnClickListener {
            Log.d("DEBUG", "View Match History button clicked")
            val intent = Intent(this, MatchHistoryActivity::class.java)
            startActivity(intent)
        }

        ui.btnTeamManagement.setOnClickListener {
            Log.d("DEBUG", "Team Management button clicked")
            val intent = Intent(this, TeamManagementActivity::class.java)
            startActivity(intent)
        }
    }
}

