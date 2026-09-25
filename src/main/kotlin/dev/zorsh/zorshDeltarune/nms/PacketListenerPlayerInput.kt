package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.InputHolder

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
        val sprint = input.booleans.read(6)
        player.sendMessage("""
            New input from ${player.name}:
                L: $left
                R: $right
                F: $forward
                B: $backward
                J: $space
                Sn: $shift
                Sp: $sprint
        """.trimIndent())
//        ZorshDeltarune.getDPlayer(player.uniqueId)?.updateInputs(InputHolder(
//            left,
//            right,
//            forward,
//            backward,
//            space,
//            shift,
//            sprint
//        ))
    }
}