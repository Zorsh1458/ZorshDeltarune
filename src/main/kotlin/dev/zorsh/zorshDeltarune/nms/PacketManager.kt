package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.runLater
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec3
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import java.lang.reflect.Type

class PacketManager {
    companion object {
        @Volatile
        private var counter = 0

        @Volatile
        var privateEntities = mutableMapOf<Int, Set<Player>>()

        val protocolManager: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

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
        fun setTransformation(entityId: Int, newTransformation: Transformation, players: List<Player>) {
            val packet = getDisplayMetadataPacket(
                entityId,
                newTransformation
            )

            for (player in players) {
                protocolManager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun getDisplayMetadataPacket(
            entityId: Int,
            newTransformation: Transformation,
            interpolationDuration: Int = 1,
        ): PacketContainer {
            // Le packet: https://minecraft.wiki/w/Java_Edition_protocol#Set_Entity_Metadata
            // Les index: https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Text_Display

            val metadata = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
            metadata.integers.write(0, entityId)

            val metadataList: MutableList<WrappedDataValue> = mutableListOf()

            try {
                metadataList += WrappedDataValue(
                    8,
                    WrappedDataWatcher.Registry.get(Integer::class.java as Type),
                    0
                )

                metadataList += WrappedDataValue(
                    9,
                    WrappedDataWatcher.Registry.get(Integer::class.java as Type),
                    interpolationDuration
                )

                metadataList += WrappedDataValue(
                    10,
                    WrappedDataWatcher.Registry.get(Integer::class.java as Type),
                    interpolationDuration
                )

                metadataList += WrappedDataValue(
                    11,
                    WrappedDataWatcher.Registry.get(Vector3f::class.java as Type),
                    newTransformation.translation
                )

                metadataList += WrappedDataValue(
                    12,
                    WrappedDataWatcher.Registry.get(Vector3f::class.java as Type),
                    newTransformation.scale
                )

                metadataList += WrappedDataValue(
                    13,
                    WrappedDataWatcher.Registry.get(Quaternionf::class.java as Type),
                    newTransformation.leftRotation
                )

                metadataList += WrappedDataValue(
                    14,
                    WrappedDataWatcher.Registry.get(Quaternionf::class.java as Type),
                    newTransformation.rightRotation
                )
            } catch (ignored: Exception) {}

            metadata.dataValueCollectionModifier.write(0, metadataList)

            return metadata
        }

//        @JvmStatic
//        fun getTextDisplayMetadataPacket(
//            entityId: Int,
//            text: String? = "",
//            lineWidth: Int = 1000,
//            backgroundColor: Int = 0,
//            InterpolationDuration: Int = 1,
//            billboardConstraints: Byte = 0,
//            brightness: Display.Brightness = Display.Brightness(15, 15),
//            viewRange: Float = 1f,
//            alignment: Int = 0,
//            isSeeThrough: Boolean = false,
//        ): PacketContainer {
//            // Le packet: https://minecraft.wiki/w/Java_Edition_protocol#Set_Entity_Metadata
//            // Les index: https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_metadata#Text_Display
//
//            val metadata = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
//            metadata.integers.write(0, entityId)
//
//            val metadataList: MutableList<WrappedDataValue> = mutableListOf()
//
//            // Index 9: Interpolation duration
//            metadataList += WrappedDataValue(
//                    9,
//                    WrappedDataWatcher.Registry.get(Int::class.java as Type),
//                    InterpolationDuration
//                )
//
//
//            // Index 12: Scale
//            val scale: Vector3f = Vector3f(2.0f, 2.0f, 2.0f)
//            metadataList.add(WrappedDataValue(12, WrappedDataWatcher.Registry.getVectorSerializer(), scale))
//
//            // Index 15: Billboard constraints
//            metadataList.add(
//                WrappedDataValue(
//                    15,
//                    WrappedDataWatcher.Registry.get(Byte::class.java as Type),
//                    billboardConstraints
//                )
//            ) // 0 = FIXED, 1 = VERTICAL, 2 = HORIZONTAL, 3 = CENTER
//
//            // Index 16: Brightness override
//            val brightnessValue = (brightness.blockLight shl 4) or (brightness.skyLight shl 20)
//            metadataList.add(
//                WrappedDataValue(
//                    16,
//                    WrappedDataWatcher.Registry.get(Int::class.java as Type),
//                    brightnessValue
//                )
//            )
//
//            // Index 17: View range
//            metadataList.add(
//                WrappedDataValue(
//                    17,
//                    WrappedDataWatcher.Registry.get(Float::class.java as Type),
//                    viewRange
//                )
//            ) // 1.0f par défaut
//
//            // Index 23: Text
//            metadataList.add(
//                WrappedDataValue(
//                    23,
//                    WrappedDataWatcher.Registry.getChatComponentSerializer(false),
//                    WrappedChatComponent.fromJson(text).handle
//                )
//            )
//
//            // Index 24: Line width
//            metadataList.add(WrappedDataValue(24, WrappedDataWatcher.Registry.get(Int::class.java as Type), lineWidth))
//
//            // Index 25: Background color
//            metadataList.add(
//                WrappedDataValue(
//                    25,
//                    WrappedDataWatcher.Registry.get(Int::class.java as Type),
//                    backgroundColor
//                )
//            ) // 0x40000000 par défaut
//
//            // Index 27: Bitmask
//            var bitmask: Byte = 0
//            if (isSeeThrough) {
//                bitmask = (bitmask.toInt() or 0x02).toByte()
//            }
//            if (alignment == 1 || alignment == 3) {
//                bitmask = (bitmask.toInt() or 0x08).toByte() // Aligné à gauche
//            } else if (alignment == 2) {
//                bitmask = (bitmask.toInt() or 0x10).toByte() // Aligné à droite
//            } // Sinon c'est le milieu et il n'y a rien à faire de plus
//
//            metadataList.add(WrappedDataValue(27, WrappedDataWatcher.Registry.get(Byte::class.java as Type), bitmask))
//
//            metadata.dataValueCollectionModifier.write(0, metadataList)
//
//            return metadata
//        }

        @JvmStatic
        fun playerLookAt(location: Location, players: List<Player>) {
            val packet = PacketContainer(PacketType.Play.Server.LOOK_AT)

            packet.doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z)

            for (player in players) {
                protocolManager.sendServerPacket(player, packet)
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

            for (player in players) {
                protocolManager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun spawnItemDisplay(
            location: Location,
            item: ItemStack,
            players: List<Player>,
            data: FakeDisplayData,
            afterSpawned: (FakeItemDisplay) -> Unit,
        ) {
            runLater(0L) {
                val ent = (location.world?.spawnEntity(location, EntityType.ITEM_DISPLAY)) as ItemDisplay
                ent.setItemStack(item)
                ent.teleportDuration = 1
                ent.transformation = data.transformation
                ent.interpolationDuration = 1
                ent.brightness = Display.Brightness(15, 15)
                val entityId = ent.entityId
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    privateEntities.remove(entityId)
                }
                afterSpawned(FakeItemDisplay(entityId, location, data.transformation, players))
            }
        }

        @JvmStatic
        fun spawnTextDisplay(
            location: Location,
            text: Component,
            players: List<Player>,
            data: FakeDisplayData,
            afterSpawned: (FakeTextDisplay) -> Unit,
        ) {
            runLater(0L) {
                val ent = (location.world?.spawnEntity(location, EntityType.TEXT_DISPLAY)) as TextDisplay
                ent.text(text)
                ent.teleportDuration = 2
                ent.transformation = data.transformation
                ent.interpolationDuration = 2
                ent.backgroundColor = Color.fromARGB(0)
                ent.brightness = Display.Brightness(15, 15)
                ent.lineWidth = 10000
                val entityId = ent.entityId
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    privateEntities.remove(entityId)
                }
                afterSpawned(FakeTextDisplay(entityId, location, data.transformation, players))
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

            for (player in players) {
                protocolManager.sendServerPacket(player, packet)
            }
            return entityId
        }

        @JvmStatic
        fun removeEntity(entityId: Int, players: List<Player>) {
            val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
            try {
                packet.modifier.writeDefaults()
                packet.modifier.write(
                    0,
                    IntArrayList(intArrayOf(entityId))
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            for (player in players) {
                protocolManager.sendServerPacket(player, packet)
            }
        }
    }
}