package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.*
import dev.zorsh.zorshDeltarune.nms.PacketManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.EntityType
import org.joml.Vector3d
import kotlin.random.Random

class TestEnemy(hitpoints: Int) : DeltaruneEnemy(hitpoints) {

    private val manager = ZorshDeltarune.protocolManager

    private val random = Random(100)

    override suspend fun attack(onAttackEnds: () -> Unit) {
        coroutineScope {
            repeat(500) { index ->
                launch {
                    testSpawnNMS(index)
                }
                delay(50)
            }
        }
    }

    private suspend fun testSpawnNMS(i: Int) {
        val id = PacketManager.spawnNewEntity(
            myBattle.battleCenterLocation + Vector3d((i % 10) * 0.5 - 2.5, 2.0 ,2.0),
            EntityType.PIG,
            myBattle.players.map { it.player }
        )
        ZorshDeltarune.instance.logger.info("[SPAWNING INDEX / ID]: $i / $id")
        delay(75)
        PacketManager.removeEntity(
            id,
            myBattle.players.map { it.player }
        )
    }
}