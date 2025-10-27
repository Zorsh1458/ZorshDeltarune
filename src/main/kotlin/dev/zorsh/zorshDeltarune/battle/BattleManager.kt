package dev.zorsh.zorshDeltarune.battle

class BattleManager {

    private var battlesList = emptyList<DeltaruneBattle>()

    fun destroyAllBattles() {
        for (battle in battlesList) {
            battle.destroyBattle()
        }
        battlesList = emptyList()
    }

    fun startBattle(battle: DeltaruneBattle) {
        battlesList += battle
        battle.start(
            onEnded = {
                battlesList -= battle
            }
        )
    }
}