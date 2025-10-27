package dev.zorsh.zorshDeltarune

import dev.zorsh.zorshDeltarune.battle.*
import org.bukkit.plugin.java.JavaPlugin

class ZorshDeltarune : JavaPlugin() {

    companion object {
        lateinit var instance: ZorshDeltarune
    }

    private val battleManager = BattleManager()

    override fun onEnable() {
        instance = this
        logger.info("[ZorshDeltarune] Plugin enabled!")
    }

    override fun onDisable() {
        battleManager.destroyAllBattles()
        logger.info("[ZorshDeltarune] Plugin disabled!")
    }
}
