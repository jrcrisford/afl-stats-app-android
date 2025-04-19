package au.edu.utas.jc101.aflstatsapp

import java.io.Serializable

// Data class representing a player and their statistics
data class Player(
    val id: String,                                                         // Unique identifier for the player
    val name: String,                                                       // Player's name
    val number: Int,                                                        // Player's jersey number
    val team: String,                                                       // Team the player belongs to
    var kicks: Int = 0,                                                     // Number of kicks made by the player
    var handballs: Int = 0,                                                 // Number of handballs made by the player
    var marks: Int = 0,                                                     // Number of marks taken by the player
    var tackles: Int = 0,                                                   // Number of tackles made by the player
    var goals: Int = 0,                                                     // Number of goals scored by the player
    var behinds: Int = 0,                                                   // Number of behinds scored by the player
    var actionTimestamps: MutableList<Map<String, Any>> = mutableListOf()   // List with timestamps of actions performed by the player
) : Serializable
