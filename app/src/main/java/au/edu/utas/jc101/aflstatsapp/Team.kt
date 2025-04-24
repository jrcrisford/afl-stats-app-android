package au.edu.utas.jc101.aflstatsapp

import java.io.Serializable

/**
 * Data class representing a team.
 *
 * @property name The name of the team.
 * @property players A mutable list of players in the team.
 */
data class Team(
    var name: String = "",
    var players: MutableList<Player> = mutableListOf()
) : Serializable

