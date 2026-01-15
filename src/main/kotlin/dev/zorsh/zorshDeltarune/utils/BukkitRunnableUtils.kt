package dev.zorsh.zorshDeltarune.utils

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

fun runLater(delay: Long, action: () -> Unit) {
    Bukkit.getScheduler().runTaskLater(ZorshDeltarune.instance, action, delay)
}

fun runSync(action: () -> Unit) {
    Bukkit.getScheduler().runTask(ZorshDeltarune.instance, action)
}

fun runRepeating(count: Int, action: (Int, BukkitRunnable) -> Unit) {
    object : BukkitRunnable() {
        var counter = 0

        override fun run() {
            if (counter >= count) {
                cancel()
            } else {
                try {
                    action(counter, this)
                } catch (ignored: Exception) {}
                counter++
            }
        }
    }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
}