package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.*
import dev.zorsh.zorshDeltarune.nms.PacketManager
import kotlinx.coroutines.*
import org.joml.Vector3d
import kotlin.random.Random

class TestEnemy(hitpoints: Int) : DeltaruneEnemy(hitpoints) {

    private val manager = ZorshDeltarune.protocolManager

    private val random = Random(100)

    private val spawnedEntities = mutableSetOf<FakeTextDisplay>()

    override suspend fun attack(onAttackEnds: () -> Unit) = coroutineScope {
        repeat(100) { index ->
            repeat(50) {
                launch {
                    testSpawnNMS(index)
                }
            }
            delay(50)
        }
    }

    override fun die() {
        try {
            for (ent in spawnedEntities.toList()) {
                ent.destroy()
            }
            spawnedEntities.clear()
        } catch (ignored: Exception) {}
    }

    private fun testSpawnNMS(i: Int) {
        val primary = Vector3d(random.nextDouble() * 0.4 - 0.2, random.nextDouble() * 0.4 - 0.2, 0.0)
        val loc = myBattle.battleCenterLocation + Vector3d(0.0, 1.0 ,2.0) + primary
        loc.yaw = 180f
        PacketManager.spawnTextDisplay(
            loc,
            "⏺",
            myBattle.players.map { it.player }
        ) { entity ->
            spawnedEntities += entity
            entity.holder = spawnedEntities
            CoroutineScope(Dispatchers.IO).launch {
                repeat(20) {
                    entity.teleport(entity.location + primary + Vector3d(random.nextDouble() * 0.2 - 0.1, random.nextDouble() * 0.2 - 0.1, 0.0))
                    delay(50)
                }
                entity.destroy()
            }
        }
    }
}