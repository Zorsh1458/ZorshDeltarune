package dev.zorsh.zorshDeltarune.utils

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun Player.hideFromEveryone() {
    for (pl in Bukkit.getOnlinePlayers()) {
        if (pl != this) {
            pl.hidePlayer(ZorshDeltarune.instance, this)
        }
    }
}

fun Player.showToEveryone() {
    for (pl in Bukkit.getOnlinePlayers()) {
        if (pl != this) {
            pl.showPlayer(ZorshDeltarune.instance, this)
        }
    }
}