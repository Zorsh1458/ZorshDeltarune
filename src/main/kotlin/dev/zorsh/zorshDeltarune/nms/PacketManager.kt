package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.InternalStructure
import com.comphenix.protocol.events.PacketContainer
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.runLater
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import java.awt.TextComponent
import java.util.*

class PacketManager {
    companion object {
        private var counter = 0

        var privateEntities = mutableMapOf<Int, Set<Player>>()

        @JvmStatic
        fun getProtocolManager() = ZorshDeltarune.protocolManager

        @JvmStatic
        fun packetInfo(packetType: PacketType): String {
            val packet = PacketContainer(packetType)
            var res = "== FIELDS OF $packetType ==\n"
            res += "integers: [\n${packet.integers.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "strings: [\n${packet.strings.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "structures: [\n${packet.structures.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "doubles: [\n${packet.doubles.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "bytes: [\n${packet.bytes.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "modifier: [\n${packet.modifier.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "uuiDs: [\n${packet.uuiDs.fields.map { it.field.toGenericString() + "\n" }}\n"
            return res
        }

        @JvmStatic
        fun playerLookAt(location: Location, players: List<Player>) {
            val packet = PacketContainer(PacketType.Play.Server.LOOK_AT)

            packet.doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z)

            val manager = getProtocolManager()
            for (player in players) {
                manager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun teleportEntity(
            entityId: Int,
            location: Location,
            delta: Vec3,
            players: List<Player>,
        ) {
            val packet = PacketContainer(PacketType.Play.Server.ENTITY_POSITION_SYNC)
            packet.integers.write(0, entityId)

            packet.modifier.writeDefaults()
            packet.modifier
                .write(0, entityId)
                .write(
                    1,
                    PositionMoveRotation(
                        Vec3(location.x, location.y, location.z),
                        delta,
                        location.yaw,
                        location.pitch
                    )
                )

            val manager = getProtocolManager()
            for (player in players) {
                manager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun spawnTextDisplay(
            location: Location,
            text: String,
            players: List<Player>,
            afterSpawned: (FakeTextDisplay) -> Unit,
        ) {
            runLater(0L) {
                val ent = (location.world?.spawnEntity(location, EntityType.TEXT_DISPLAY)) as TextDisplay
                ent.text(Component.text(text))
                ent.teleportDuration = 1
                val entityId = ent.entityId
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    privateEntities.remove(entityId)
                }
                afterSpawned(FakeTextDisplay(entityId, location, players))
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
            packet.modifier.write(
                0,
                IntArrayList(intArrayOf(entityId))
            )

            val manager = getProtocolManager()
            for (player in players) {
                manager.sendServerPacket(player, packet)
            }
        }
    }
}