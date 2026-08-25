package dev.zorsh.zorshDeltarune.battle

import java.util.UUID

class BattleManager {
    companion object {
        private var battlesList = mutableMapOf<UUID, INeverlandBattle>()

        @JvmStatic
        fun destroyAllBattles() {
            for ((_, battle) in battlesList) {
                battle.destroyBattle()
            }
            battlesList = mutableMapOf()
        }

        @JvmStatic
        fun startBattle(battle: INeverlandBattle) {
            val uuid = UUID.randomUUID()
            battlesList[uuid] = battle
            battle.setBattleUUID(uuid)
            for (dPlayer in battle.getPlayers()) {
                dPlayer.myBattleUUID = uuid
            }
            battle.startBattle {
                battlesList.remove(uuid)
            }
        }

        @JvmStatic
        fun hasBattle(uuid: UUID) = battlesList.containsKey(uuid)

        @JvmStatic
        fun getBattle(uuid: UUID) = battlesList[uuid]

        @JvmStatic
        fun getAllBattles() = battlesList.map { it.key }
    }
}