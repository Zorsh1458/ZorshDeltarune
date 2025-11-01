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

fun runRepeating(count: Int, action: (Int) -> Unit) {
    object : BukkitRunnable() {
        var counter = 0

        override fun run() {
            if (counter >= count) {
                cancel()
            } else {
                try {
                    action(counter)
                } catch (ignored: Exception) {}
                counter++
            }
        }
    }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
}