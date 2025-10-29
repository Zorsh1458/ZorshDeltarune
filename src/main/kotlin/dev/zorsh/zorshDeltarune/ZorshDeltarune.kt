package dev.zorsh.zorshDeltarune

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.zorsh.zorshDeltarune.battle.BattleManager
import dev.zorsh.zorshDeltarune.commands.DeltaruneBattleCommand
import dev.zorsh.zorshDeltarune.nms.PacketListenerSpawnEntity
import org.bukkit.plugin.java.JavaPlugin


class ZorshDeltarune : JavaPlugin() {

    companion object {
        lateinit var instance: ZorshDeltarune
        val protocolManager: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }
    }

    override fun onEnable() {
        instance = this
        protocolManager.addPacketListener(PacketListenerSpawnEntity())
        getCommand("deltarunebattle")?.setExecutor(DeltaruneBattleCommand())
        getCommand("deltarunebattle")?.tabCompleter = DeltaruneBattleCommand()
        logger.info("[ZorshDeltarune] Plugin enabled!")
    }

    override fun onDisable() {
        BattleManager.destroyAllBattles()
        protocolManager.removePacketListeners(instance)
        logger.info("[ZorshDeltarune] Plugin disabled!")
    }
}
