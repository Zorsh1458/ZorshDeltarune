package dev.zorsh.zorshDeltarune.utils

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.scheduler.BukkitRunnable

fun runLater(delay: Long, action: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            action()
        }
    }.runTaskLater(ZorshDeltarune.instance, delay)
}