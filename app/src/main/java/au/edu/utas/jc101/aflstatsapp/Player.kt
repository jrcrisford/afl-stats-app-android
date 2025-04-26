package au.edu.utas.jc101.aflstatsapp

import java.io.Serializable

/**
 * Data class representing a player in an AFL team and their match stats.
 *
 * @property id Unique identifier for the player.
 * @property name Player's name.
 * @property number Player's jersey number.
 * @property team Team the player belongs to.
 * @property kicks Number of kicks made by the player.
 * @property handballs Number of handballs made by the player.
 * @property marks Number of marks taken by the player.
 * @property tackles Number of tackles made by the player.
 * @property goals Number of goals scored by the player.
 * @property behinds Number of behinds scored by the player.
 * @property actionTimestamps List with timestamps of actions performed by the player.
 * @property photoUri URI of the player's photo.
 */
data class Player(
    val id: String,
    val name: String,
    val number: Int,
    val team: String,
    var kicks: Int = 0,
    var handballs: Int = 0,
    var marks: Int = 0,
    var tackles: Int = 0,
    var goals: Int = 0,
    var behinds: Int = 0,
    var actionTimestamps: MutableList<Map<String, Any>> = mutableListOf(),
    var photoUri: String? = null
) : Serializable
