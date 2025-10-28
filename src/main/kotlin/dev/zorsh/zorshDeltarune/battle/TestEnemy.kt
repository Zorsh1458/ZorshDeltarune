package dev.zorsh.zorshDeltarune.battle

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import dev.zorsh.zorshDeltarune.ZorshDeltarune
import kotlinx.coroutines.delay
import org.bukkit.entity.EntityType
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TestEnemy(hitpoints: Int) : DeltaruneEnemy(hitpoints) {

    private val manager = ZorshDeltarune.protocolManager

    private val random = Random(100)

    override suspend fun attack(onAttackEnds: () -> Unit) {
        repeat(500) { index ->
            testSpawnNMS(index)
            delay(5)
        }
    }

    private fun testSpawnNMS(i: Int) {
        val packet = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)
        val entityId = 10000 + random.nextInt() % 10000
        packet.integers.write(0, entityId)
        packet.uuiDs.write(0, UUID.randomUUID())
        packet.entityTypeModifier.write(0, EntityType.PIG)

        val location = myBattle.battleCenterLocation

        val x = location.x
        val z = location.z
        val angle = i.toDouble() * 3.1415 / 180
        val radius = 5

        packet.doubles
            .write(0, x + cos(angle) * radius)
            .write(1, location.y)
            .write(2, z + sin(angle) * radius)

        for (dplayer in myBattle.players) {
            manager.sendServerPacket(dplayer.player, packet)
        }
    }
}