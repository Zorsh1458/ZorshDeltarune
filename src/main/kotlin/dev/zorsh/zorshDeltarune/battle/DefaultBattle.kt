package dev.zorsh.zorshDeltarune.battle

import kotlinx.coroutines.*
import org.bukkit.scheduler.BukkitRunnable

class DefaultBattle(players: List<DeltarunePlayer>, enemies: List<DeltaruneEnemy>) : DeltaruneBattle(players, enemies) {

    private var loopTask: BukkitRunnable? = null

    private var battleJob: Job? = null

    override fun destroyBattle() {
        if (loopTask?.isCancelled == false) {
            loopTask?.cancel()
        }
        if (battleJob?.isCancelled == false) {
            battleJob?.cancel()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun startBattle() {
        for (pl in players) {
            pl.lockInBattle(battleCenterLocation)
        }

        battleJob = GlobalScope.launch {
            for (enemy in enemies) {
                launch {
                    enemy.attack()
                }
            }
        }

        loopTask = object : BukkitRunnable() {
            override fun run() {
                if (players.all { !it.locked }) {
                    cancel()
                }
            }
        }
    }
}