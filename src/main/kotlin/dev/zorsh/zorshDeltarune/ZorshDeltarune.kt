package dev.zorsh.zorshDeltarune

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.zorsh.zorshDeltarune.battle.BattleManager
import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import dev.zorsh.zorshDeltarune.commands.DeltaruneBattleCommand
import dev.zorsh.zorshDeltarune.listeners.DeltaruneListener
import dev.zorsh.zorshDeltarune.nms.PacketListenerEntityDestroy
import dev.zorsh.zorshDeltarune.nms.PacketListenerSpawnEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


class ZorshDeltarune : JavaPlugin() {

    companion object {
        lateinit var instance: ZorshDeltarune
        val protocolManager: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

        var deltarunePlayer = mutableMapOf<Player, DeltarunePlayer>()

        @JvmStatic
        fun getDPlayer(player: Player): DeltarunePlayer {
            val dPlayer = deltarunePlayer[player] ?: DeltarunePlayer(player)
            if (deltarunePlayer[player] == null) {
                deltarunePlayer[player] = dPlayer
            }
            return dPlayer
        }
    }

    override fun onEnable() {
        instance = this
        protocolManager.addPacketListener(PacketListenerSpawnEntity())
        protocolManager.addPacketListener(PacketListenerEntityDestroy())
        server.pluginManager.registerEvents(DeltaruneListener(), this)
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
