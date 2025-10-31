package dev.zorsh.zorshDeltarune.battle

import java.util.UUID

class BattleManager {
    companion object {
        private var battlesList = mutableMapOf<UUID, DeltaruneBattle>()

        @JvmStatic
        fun destroyAllBattles() {
            for ((_, battle) in battlesList) {
                battle.destroyBattle()
            }
            battlesList = mutableMapOf()
        }

        @JvmStatic
        fun startBattle(battle: DeltaruneBattle) {
            val uuid = UUID.randomUUID()
            battlesList[uuid] = battle
            for (dPlayer in battle.players) {
                dPlayer.myBattleUUID = uuid
            }
            battle.start(
                onEnded = {
                    battlesList.remove(uuid)
                }
            )
        }

        @JvmStatic
        fun hasBattle(uuid: UUID) = battlesList.containsKey(uuid)
    }
}