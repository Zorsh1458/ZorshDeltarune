package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.zorsh.zorshDeltarune.ZorshDeltarune

class PacketListenerEntityDestroy : PacketAdapter(
    ZorshDeltarune.instance,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.ENTITY_DESTROY
) {
    override fun onPacketSending(event: PacketEvent) {
        val packet = event.packet
        val player = event.player
        val listToRemove = packet.intLists.read(0)
        val newList = mutableListOf<Int>()
        for (entityId in listToRemove) {
            if (
                PacketManager.privateEntities[entityId].isNullOrEmpty() ||
                PacketManager.privateEntities[entityId]?.contains(player) == false
            ) {
                newList += entityId
            }
        }
        player.sendMessage(newList.toString())
        player.sendMessage(listToRemove.toString())
        player.sendMessage(PacketManager.privateEntities.toString())
        if (newList != listToRemove) {
            event.isCancelled = true
        }
//        packet.intLists.write(0, newList.toList())
//        event.packet = packet
    }
}