package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.zorsh.zorshDeltarune.ZorshDeltarune

class PacketListenerSpawnEntity : PacketAdapter(
    ZorshDeltarune.instance,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.SPAWN_ENTITY
) {
    override fun onPacketSending(event: PacketEvent) {
        val packet = event.packet
        val entityId = packet.integers.read(0)
        val player = event.player
        if (
            !PacketManager.privateEntities[entityId].isNullOrEmpty() &&
            PacketManager.privateEntities[entityId]?.contains(player) == false
            ) {
            event.isCancelled = true
        }
    }
}