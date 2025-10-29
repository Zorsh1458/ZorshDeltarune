package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.*
import dev.zorsh.zorshDeltarune.nms.PacketManager
import kotlinx.coroutines.*
import org.joml.Vector3d
import kotlin.random.Random

class TestEnemy(hitpoints: Int) : DeltaruneEnemy(hitpoints) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val manager = ZorshDeltarune.protocolManager

    private val random = Random(100)

    override suspend fun attack(onAttackEnds: () -> Unit) {
        scope.launch {
            repeat(100) { index ->
                launch {
                    testSpawnNMS(index)
                }
                delay(50)
            }
        }
    }

    private fun testSpawnNMS(i: Int) {
        PacketManager.spawnTextDisplay(
            myBattle.battleCenterLocation + Vector3d((i % 10) * 0.5 - 2.5, 2.0 ,2.0),
            "Test Text!",
            myBattle.players.map { it.player }
        ) { id ->
            scope.launch {
                delay(200)
                PacketManager.removeEntity(
                    id,
                    myBattle.players.map { it.player }
                )
            }
        }
    }
}