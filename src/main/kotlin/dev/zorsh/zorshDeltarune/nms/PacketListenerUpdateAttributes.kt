package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot

class PacketListenerUpdateAttributes : PacketAdapter(
    ZorshDeltarune.instance,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.UPDATE_ATTRIBUTES
)  {
//    override fun onPacketSending(event: PacketEvent) {
//        val packet = event.packet
//        val entityId = packet.integers.read(0)
//        val player = event.player
//        if (player.name == "Zorsh" && entityId == player.entityId) {
//            val data = (packet.modifier.read(1) as ArrayList<*>).first() as AttributeSnapshot
//            player.sendMessage("${data.modifiers}")
////            val list = packet.modifier.read(1) as ArrayList<*>
////            val newList = list.toArray()
////            list.forEachIndexed { index, t ->
////                val snapshot = t as AttributeSnapshot
////                if (snapshot.attribute.registeredName == "minecraft:scale") {
////                    val newData = AttributeSnapshot(snapshot.attribute, 2.0, snapshot.modifiers)
////                    newList[index] = newData
////                }
////            }
////            packet.modifier.write(1, newList.toCollection(ArrayList()))
//        }
//    }
}