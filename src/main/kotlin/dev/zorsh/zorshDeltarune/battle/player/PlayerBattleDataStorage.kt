package dev.zorsh.zorshDeltarune.battle.player

import dev.zorsh.zorshDeltarune.battle.enemy.DeltaruneEnemy

class PlayerBattleDataStorage(
    var optionsObjectNamesList: MutableList<String> = mutableListOf(),
    var optionsListSize: Pair<Int, Int> = 0 to 0,
    var optionsSelectorPosition: Pair<Int, Int> = 0 to 0,

    var selectedEnemyIndex: Int = 0,
    var selectedEnemy: DeltaruneEnemy? = null
)