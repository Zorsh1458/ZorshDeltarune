package dev.zorsh.zorshDeltarune.battle

import org.bukkit.Bukkit
import org.bukkit.Location

class DefaultBattle(players: List<DeltarunePlayer>, enemies: List<DeltaruneEnemy>) : DeltaruneBattle(players, enemies) {

    val battleCenterLocation = Location(Bukkit.getWorld("Moon"), 0.0, 100.0, 0.0)

    override fun destroyBattle() {
        TODO("Not yet implemented")
    }

    override fun startBattle() {
        for (pl in players) {
            pl.lockInBattle(battleCenterLocation)
        }
    }
}