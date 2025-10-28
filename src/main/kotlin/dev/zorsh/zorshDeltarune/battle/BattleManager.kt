package dev.zorsh.zorshDeltarune.battle

class BattleManager {
    companion object {
        private var battlesList = emptyList<DeltaruneBattle>()

        @JvmStatic
        fun destroyAllBattles() {
            for (battle in battlesList) {
                battle.destroyBattle()
            }
            battlesList = emptyList()
        }

        @JvmStatic
        fun startBattle(battle: DeltaruneBattle) {
            battlesList += battle
            battle.start(
                onEnded = {
                    battlesList -= battle
                }
            )
        }
    }
}