package au.edu.utas.jc101.aflstatsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import au.edu.utas.jc101.aflstatsapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity()
{
    private lateinit var ui : ActivityMainBinding

    override fun onCreate(SavedInstanceState: Bundle?) {
        super.onCreate(SavedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.lblMainTitle.text = "Welcome to the AFL Stats App"
        Log.d("NAVIGATION", "Navigated to ActivityMain")

        val db = Firebase.firestore
        Log.d("FIREBASE", "Firebase connected: ${db.app.name}")

        // JSON import for match COPILOT
        val inputStream = assets.open("demo_match.json")
        val jsonStr = inputStream.bufferedReader().use { it.readText() }
        val jsonObj = JSONObject(jsonStr)
        val matchId = jsonObj.keys().next()
        val matchData = jsonObj.getJSONObject(matchId)

        db.collection("matches").document(matchId)
            .set(matchData.toMap())
            .addOnSuccessListener {
                Log.d("UPLOAD", "Match data imported successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("UPLOAD", "Failed to import match data", e)
            }

        // JSON import for teams COPILOT
        val teamInputStream = assets.open("demo_teams.json")
        val teamJsonStr = teamInputStream.bufferedReader().use { it.readText() }
        val teamJsonObj = JSONObject(teamJsonStr)

        for (teamName in teamJsonObj.keys()) {
            val teamData = teamJsonObj.getJSONObject(teamName)
            db.collection("teams").document(teamName)
                .set(teamData.toMap())
                .addOnSuccessListener {
                    Log.d("UPLOAD", "Team '$teamName' imported successfully!")
                }
                .addOnFailureListener { e ->
                    Log.e("UPLOAD", "Failed to import team '$teamName'", e)
                }
        }

        db.collection("matches").document(matchId)
            .set(matchData.toMap())
            .addOnSuccessListener {
                Log.d("UPLOAD", "Match data imported successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("UPLOAD", "Failed to import match data", e)
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

    // Helper extension functions COPILOT
    fun JSONObject.toMap(): Map<String, Any?> = keys().asSequence().associateWith { key ->
        when (val value = this[key]) {
            is JSONObject -> value.toMap()
            is JSONArray -> value.toList()
            JSONObject.NULL -> null
            else -> value
        }
    }

    fun JSONArray.toList(): List<Any?> = (0 until length()).map { i ->
        when (val value = get(i)) {
            is JSONObject -> value.toMap()
            is JSONArray -> value.toList()
            JSONObject.NULL -> null
            else -> value
        }
    }
}

