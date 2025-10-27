package dev.zorsh.zorshDeltarune

import dev.zorsh.zorshDeltarune.battle.*
import org.bukkit.plugin.java.JavaPlugin

class ZorshDeltarune : JavaPlugin() {

    private val battleManager = BattleManager()

    override fun onEnable() {
        logger.info("[ZorshDeltarune] Plugin enabled!")
    }

    override fun onDisable() {
        battleManager.destroyAllBattles()
        logger.info("[ZorshDeltarune] Plugin disabled!")
    }
}
