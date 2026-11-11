package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.runLater
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.world.entity.ai.attributes.Attribute
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.lang.reflect.Type
import java.util.*

@Suppress("UNUSED", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class PacketManager {
    companion object {
        @Volatile
        private var counter = 0

        @Volatile
        var privateEntities = mutableMapOf<Int, Set<Player>>()

        @Volatile
        var savedEntities = mutableMapOf<Int, TextDisplay>()

        private val protocolManager: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

        @JvmStatic
        fun packetInfo(packetType: PacketType): String {
            val packet = PacketContainer(packetType)
            var res = "== FIELDS OF $packetType ==\n"
            res += "integers: [\n${packet.integers.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "floats: [\n${packet.float.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "booleans: [\n${packet.booleans.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "strings: [\n${packet.strings.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "structures: [\n${packet.structures.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "doubles: [\n${packet.doubles.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "bytes: [\n${packet.bytes.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "modifier: [\n${packet.modifier.fields.map { it.field.toGenericString() + "\n" }}\n"
            res += "uuiDs: [\n${packet.uuiDs.fields.map { it.field.toGenericString() + "\n" }}\n"
            return res
        }

        @JvmStatic
        fun setTransformation(
            entityId: Int,
            newTransformation: Transformation,
            players: List<Player>,
            interpolationDuration: Int = 1,
            teleportDuration: Int = 2,
        ) {
            val packet = getDisplayMetadataPacket(
                entityId,
                newTransformation,
                interpolationDuration,
                teleportDuration
            )

            for (player in players.filter { it.isOnline }) {
                protocolManager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun setTextDisplayMetadata(
            entityId: Int,
            newText: Component,
            newTransformation: Transformation,
            players: List<Player>,
            interpolationDuration: Int = 1,
            teleportDuration: Int = 2,
            opacity: Byte,
        ) {
            fun sendPacket(packet: PacketContainer) {
                for (player in players.filter { it.isOnline }) {
                    protocolManager.sendServerPacket(player, packet)
                }
            }

            if (savedEntities[entityId] != null) {

                savedEntities[entityId]?.transformation = newTransformation
                savedEntities[entityId]?.interpolationDuration = interpolationDuration
                savedEntities[entityId]?.teleportDuration = teleportDuration
                savedEntities[entityId]?.textOpacity = opacity
                savedEntities[entityId]?.text(newText)

                val ent = savedEntities[entityId]!!
                val packet = getTextDisplayMetadataPacketNew(entityId, WrappedDataWatcher.getEntityWatcher(ent))
                sendPacket(packet)
            } else {
                val packet = getTextDisplayMetadataPacket(
                    entityId,
                    newText,
                    newTransformation,
                    interpolationDuration,
                    teleportDuration,
                    opacity
                )
                sendPacket(packet)
            }
        }

        @JvmStatic
        fun getTextDisplayMetadataPacketNew(entityId: Int, entityWatcher: WrappedDataWatcher): PacketContainer {
            val metadata = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
            metadata.integers.write(0, entityId)
            metadata.dataValueCollectionModifier.write(0, entityWatcher.toDataValueCollection())
            return metadata
        }

        @JvmStatic
        fun getTextDisplayMetadataPacket(
            entityId: Int,
            newText: Component,
            newTransformation: Transformation,
            interpolationDuration: Int,
            teleportDuration: Int,
            opacity: Byte,
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
                    teleportDuration
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

                val serializer = GsonComponentSerializer.gson()

                metadataList += WrappedDataValue(
                    23,
                    WrappedDataWatcher.Registry.getChatComponentSerializer(),
                    WrappedChatComponent.fromJson(serializer.serialize(newText)).handle
                )

                metadataList += WrappedDataValue(
                    26,
                    WrappedDataWatcher.Registry.get(Byte::class.java as Type),
                    opacity
                )
            } catch (ignored: Exception) {
            }

            metadata.dataValueCollectionModifier.write(0, metadataList)

            return metadata
        }

        @JvmStatic
        fun getDisplayMetadataPacket(
            entityId: Int,
            newTransformation: Transformation,
            interpolationDuration: Int,
            teleportDuration: Int,
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
                    teleportDuration
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
            } catch (ignored: Exception) {
            }

            metadata.dataValueCollectionModifier.write(0, metadataList)

            return metadata
        }

        @JvmStatic
        fun playerLookAt(location: Location, players: List<Player>) {
            val packet = PacketContainer(PacketType.Play.Server.LOOK_AT)

            packet.doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z)

            for (player in players.filter { it.isOnline }) {
                protocolManager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun setAttribute(
            attribute: Holder<Attribute>,
            base: Double,
            entityId: Int,
            players: List<Player>
        ) {
            val packet = PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES)
            val data = ClientboundUpdateAttributesPacket.AttributeSnapshot(attribute, base, listOf())
            packet.integers.write(0, entityId)
            packet.modifier.write(1, listOf(data).toCollection(ArrayList()))

            for (player in players.filter { it.isOnline }) {
                protocolManager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun mountEntities(
            mountId: Int,
            entities: List<Int>,
            players: List<Player>
        ) {
            val packet = PacketContainer(PacketType.Play.Server.MOUNT)
            try {
                packet.integers.write(0, mountId)
                packet.integerArrays.write(
                    0,
                    entities.toIntArray()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            for (player in players.filter { it.isOnline }) {
                protocolManager.sendServerPacket(player, packet)
            }
        }

        @JvmStatic
        fun spawnHitbox(
            location: Location,
            scale: Double,
            players: List<Player>,
            afterSpawned: (Int, Int) -> Unit
        ) {
            val packetAnchor = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)
            val entityIdAnchor = 1000000 + counter
            counter = (counter + 1) % 10000
            packetAnchor.integers.write(0, entityIdAnchor)
            packetAnchor.uuiDs.write(0, UUID.randomUUID())
            packetAnchor.entityTypeModifier.write(0, EntityType.BLOCK_DISPLAY)

            packetAnchor.doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z)

            val packetShulker = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)
            val entityIdShulker = 1000000 + counter
            counter = (counter + 1) % 10000
            packetShulker.integers.write(0, entityIdShulker)
            packetShulker.uuiDs.write(0, UUID.randomUUID())
            packetShulker.entityTypeModifier.write(0, EntityType.SHULKER)

            packetShulker.doubles
                .write(0, location.x)
                .write(1, location.y)
                .write(2, location.z)

            val packet = PacketContainer(PacketType.Play.Server.MOUNT)
            try {
                packet.integers.write(0, entityIdAnchor)
                packet.integerArrays.write(
                    0,
                    intArrayOf(entityIdShulker)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val packetScale = PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES)
            try {
                val data = ClientboundUpdateAttributesPacket.AttributeSnapshot(
                    net.minecraft.world.entity.ai.attributes.Attributes.SCALE,
                    scale,
                    listOf()
                )
                packetScale.integers.write(0, entityIdShulker)
                packetScale.modifier.write(1, listOf(data).toCollection(ArrayList()))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            for (player in players.filter { it.isOnline }) {
                protocolManager.sendServerPacket(player, packetShulker)
                protocolManager.sendServerPacket(player, packetScale)
                protocolManager.sendServerPacket(player, packetAnchor)
                protocolManager.sendServerPacket(player, packet)
            }

            afterSpawned(entityIdAnchor, entityIdShulker)
        }

        @JvmStatic
        fun spawnShaderEffector(
            location: Location,
            players: List<Player>,
            afterSpawned: (FakeItemDisplay) -> Unit,
        ) {
            runLater(0L) {
                val ent = (location.world?.spawnEntity(location, EntityType.ITEM_DISPLAY)) as ItemDisplay
                ent.setItemStack(ItemStack.of(Material.STONE))
                ent.isPersistent = false
                val transform = Transformation(
                    Vector3f(0f),
                    AxisAngle4f(),
                    Vector3f(50f, 50f, 0.01f),
                    AxisAngle4f()
                )
                ent.transformation = transform
                ent.isGlowing = true
                ent.glowColorOverride = Color.fromARGB(255, 253, 0, 6)
                val entityId = ent.entityId
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    privateEntities.remove(entityId)
                }
                afterSpawned(FakeItemDisplay(entityId, transform, 0, 0, players))
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
                ent.teleportDuration = data.teleportDuration
                ent.transformation = data.transformation
                ent.interpolationDuration = data.interpolationDuration
                ent.brightness = Display.Brightness(15, 15)
                ent.isPersistent = false
                val entityId = ent.entityId
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    privateEntities.remove(entityId)
                }
                afterSpawned(
                    FakeItemDisplay(
                        entityId,
                        data.transformation,
                        data.teleportDuration,
                        data.interpolationDuration,
                        players
                    )
                )
            }
        }

        @JvmStatic
        fun spawnTextDisplay(
            location: Location,
            text: Component,
            players: List<Player>,
            data: FakeDisplayData,
            seeThrough: Boolean,
            alignment: TextDisplay.TextAlignment,
            lineWidth: Int,
            isShadowed: Boolean,
            afterSpawned: (FakeTextDisplay) -> Unit,
        ) {
            runLater(0L) {
                val ent = (location.world?.spawnEntity(location, EntityType.TEXT_DISPLAY)) as TextDisplay
                ent.text(text)
                ent.teleportDuration = data.teleportDuration
                ent.transformation = data.transformation
                ent.interpolationDuration = data.interpolationDuration
                ent.backgroundColor = Color.fromARGB(0)
                ent.brightness = Display.Brightness(15, 15)
                ent.lineWidth = lineWidth
                ent.textOpacity = data.opacity
                ent.isPersistent = false
                ent.isSeeThrough = seeThrough
                ent.alignment = alignment
                ent.isShadowed = isShadowed
                val entityId = ent.entityId
                savedEntities[entityId] = ent
                privateEntities[entityId] = players.toSet()
                runLater(1L) {
                    ent.remove()
                }
                runLater(2L) {
                    privateEntities.remove(entityId)
                }
                afterSpawned(
                    FakeTextDisplay(
                        entityId,
                        text,
                        data.transformation,
                        data.teleportDuration,
                        data.interpolationDuration,
                        players,
                        opacity = data.opacity
                    )
                )
            }
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

            for (player in players.filter { it.isOnline }) {
                protocolManager.sendServerPacket(player, packet)
            }

            savedEntities.remove(entityId)
        }
    }
}