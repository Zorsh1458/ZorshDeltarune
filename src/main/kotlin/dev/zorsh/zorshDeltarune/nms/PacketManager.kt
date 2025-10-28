package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

class PacketManager {
    companion object {
        private var counter = 0

        @JvmStatic
        fun getProtocolManager() = ZorshDeltarune.protocolManager

        @JvmStatic
        fun spawnNewEntity(location: Location, type: EntityType, players: List<Player>): Int {
            val packet = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)
            val entityId = 1000000 + counter
            counter = (counter + 1) % 10000
            packet.integers.write(0, entityId)
            packet.uuiDs.write(0, UUID.randomUUID())
            packet.entityTypeModifier.write(0, type)

            packet.doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z)

            val manager = getProtocolManager()
            for (player in players) {
                manager.sendServerPacket(player, packet)
            }
            return entityId
        }

        @JvmStatic
        fun removeEntity(entityId: Int, players: List<Player>) {
            val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
            packet.modifier.writeDefaults()
            packet.modifier.write(0, IntArrayList intArrayOf(entityId))

            val manager = getProtocolManager()
            for (player in players) {
                manager.sendServerPacket(player, packet)
            }
        }
    }
}