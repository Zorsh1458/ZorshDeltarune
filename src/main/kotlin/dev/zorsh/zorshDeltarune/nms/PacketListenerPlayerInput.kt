package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.zorsh.zorshDeltarune.ZorshDeltarune

class PacketListenerPlayerInput : PacketAdapter(
    ZorshDeltarune.instance,
    ListenerPriority.NORMAL,
    PacketType.Play.Client.STEER_VEHICLE
) {
    override fun onPacketReceiving(event: PacketEvent) {
        val player = event.player
        val packet = event.packet
        val input = packet.structures.read(0)
        val forward = input.booleans.read(0)
        val backward = input.booleans.read(1)
        val left = input.booleans.read(2)
        val right = input.booleans.read(3)
        val space = input.booleans.read(4)
        val shift = input.booleans.read(5)
        ZorshDeltarune.instance.logger.info("!!! ${player.name} pressed jump ${ZorshDeltarune.getDPlayer(player.uniqueId)} - FROM PACKET")
    }
}