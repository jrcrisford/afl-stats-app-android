package au.edu.utas.jc101.aflstatsapp

import java.io.Serializable

data class Team(
    var name: String = "",
    var players: MutableList<Player> = mutableListOf()
) : Serializable

