
package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.bettermodel.BattlePlayer
import org.joml.Quaternionf
import org.joml.Vector3f
import java.lang.reflect.Type

class PacketListenerEntityMetadata : PacketAdapter(
    ZorshDeltarune.instance,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.ENTITY_METADATA
) {
    override fun onPacketSending(event: PacketEvent) {
        val packet = event.packet
        val entityId = packet.integers.read(0)
        val player = event.player
        if (BattlePlayer.trackedModels.contains(entityId)) {
            if (
                !PacketManager.privateEntities[entityId].isNullOrEmpty() &&
                PacketManager.privateEntities[entityId]?.contains(player) == false
            ) {
                event.isCancelled = true
            } else {
                val metadata = packet.dataValueCollectionModifier.read(0)
                metadata.toList().forEach { data ->
                    if (data.index == 13) {
                        metadata.remove(data)
                        metadata += WrappedDataValue(
                            13,
                            WrappedDataWatcher.Registry.get(Quaternionf::class.java as Type),
                            Quaternionf(0f, 0f, 0f, 1f)
                        )
                        metadata += WrappedDataValue(
                            14,
                            WrappedDataWatcher.Registry.get(Quaternionf::class.java as Type),
                            data.rawValue
                        )
                    }
                    if (data.index == 12) {
                        metadata.remove(data)
                    }
                    metadata += WrappedDataValue(
                        12,
                        WrappedDataWatcher.Registry.get(Vector3f::class.java as Type),
                        Vector3f(0.01f, 1f, 1f)
                    )
                    if (data.index == 11) {
                        metadata.remove(data)
                        val pos = data.rawValue as Vector3f
                        metadata += WrappedDataValue(
                            11,
                            WrappedDataWatcher.Registry.get(Vector3f::class.java as Type),
                            Vector3f(pos.x * 0.02f, pos.y, pos.z)
                        )
                    }
                }
                packet.dataValueCollectionModifier.write(0, metadata)
            }
        }
    }
}