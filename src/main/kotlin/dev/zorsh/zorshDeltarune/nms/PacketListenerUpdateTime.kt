package dev.zorsh.zorshDeltarune.nms

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.Bukkit

class PacketListenerUpdateTime : PacketAdapter(
    ZorshDeltarune.instance,
    ListenerPriority.NORMAL,
    PacketType.Play.Server.UPDATE_TIME
) {
    override fun onPacketSending(event: PacketEvent) {
        val player = event.player
        val previous = event.packet.longs.read(0)
        event.packet.longs.write(0, previous % 128 + ZorshDeltarune.random.nextInt() % 10000 * 24000)
        if (PacketManager.lockedTimeTracker.contains(player.uniqueId)) {
            val data = PacketManager.lockedTimeTracker[player.uniqueId]
            if (data != null && data.first >= Bukkit.getCurrentTick()) {
                event.packet.longs.write(0, data.second)
//                player.sendMessage("Overriding")
            }
        }
    }
}