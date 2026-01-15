package dev.zorsh.zorshDeltarune

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.zorsh.zorshDeltarune.battle.BattleManager
import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import dev.zorsh.zorshDeltarune.commands.DeltaruneBattleCommand
import dev.zorsh.zorshDeltarune.listeners.DeltaruneListener
import dev.zorsh.zorshDeltarune.nms.*
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import kotlin.random.Random


class ZorshDeltarune : JavaPlugin() {

    companion object {
        lateinit var instance: ZorshDeltarune
        val protocolManager: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

        val random = Random(1488)

        var deltarunePlayer = mutableMapOf<UUID, DeltarunePlayer>()

        @JvmStatic
        fun getDPlayer(uuid: UUID): DeltarunePlayer? {
            return deltarunePlayer[uuid]
        }
    }

    override fun onEnable() {
        instance = this
        protocolManager.addPacketListener(PacketListenerEntityDestroy())
        protocolManager.addPacketListener(PacketListenerEntityMetadata())
        protocolManager.addPacketListener(PacketListenerSpawnEntity())
//        protocolManager.addPacketListener(PacketListenerUpdateAttributes())
//        protocolManager.addPacketListener(PacketListenerUpdateTime())
        server.pluginManager.registerEvents(DeltaruneListener(), this)
        getCommand("deltarunebattle")?.setExecutor(DeltaruneBattleCommand())
        getCommand("deltarunebattle")?.tabCompleter = DeltaruneBattleCommand()
        logger.info("[ZorshDeltarune] Plugin enabled!")
    }

    override fun onDisable() {
        logger.info("[ZorshDeltarune] Battles: ${BattleManager.getAllBattles()}")
        BattleManager.destroyAllBattles()
        protocolManager.removePacketListeners(instance)
        logger.info("[ZorshDeltarune] Plugin disabled!")
    }
}
