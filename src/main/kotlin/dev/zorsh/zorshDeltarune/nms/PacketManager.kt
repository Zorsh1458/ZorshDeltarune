package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.runLater
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import java.util.*

class PacketManager {
    companion object {
        private var counter = 0

        var privateEntities = mutableMapOf<Int, Set<Player>>()

        @JvmStatic
        fun getProtocolManager() = ZorshDeltarune.protocolManager

        @JvmStatic
        fun spawnTextDisplay(
            location: Location,
            text: String,
            players: List<Player>,
            afterSpawned: (Int) -> Unit
        ) {
            val task = runLater(0L) {
                val ent = (location.world?.spawnEntity(location, EntityType.TEXT_DISPLAY)) as TextDisplay
                ent.text = text
                val entityId = ent.entityId
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    for (player in players) {
                        privateEntities[entityId] = emptySet()
                    }
                }
                afterSpawned(entityId)
            }
        }

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
            packet.modifier.write(0,
                Class.forName("it.unimi.dsi.fastutil.ints.IntArrayList")
                .getConstructor(IntArray::class.java)
                .newInstance(intArrayOf(entityId))
            )

            val manager = getProtocolManager()
            for (player in players) {
                manager.sendServerPacket(player, packet)
            }
        }
    }
}