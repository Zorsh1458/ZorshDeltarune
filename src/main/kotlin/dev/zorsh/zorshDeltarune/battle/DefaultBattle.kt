package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import kotlinx.coroutines.*
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class DefaultBattle(players: List<DeltarunePlayer>, enemies: List<DeltaruneEnemy>) : DeltaruneBattle(players, enemies) {

    private var loopTask: BukkitTask? = null

    private var battleJob: Job? = null

    override fun destroyBattle() {
        if (loopTask?.isCancelled == false) {
            loopTask?.cancel()
        }
        if (battleJob?.isCancelled == false) {
            battleJob?.cancel()
        }
    }

    override fun startBattle() {
        for (pl in players) {
            pl.lockInBattle(battleCenterLocation)
        }

        battleJob = scope.launch {
            val jobs = mutableListOf<Job>()
            for (enemy in enemies) {
                jobs += launch {
                    enemy.attack()
                }
            }
            jobs.joinAll()
            delay(200L)
            end()
        }

        loopTask = object : BukkitRunnable() {
            override fun run() {
                if (players.all { !it.locked }) {
                    if (battleJob?.isCancelled == false) {
                        battleJob?.cancel()
                    }
                    cancel()
                }
            }
        }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }
}