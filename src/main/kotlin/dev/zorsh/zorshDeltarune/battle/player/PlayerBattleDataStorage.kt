package dev.zorsh.zorshDeltarune.battle.player

class PlayerBattleDataStorage(
    var optionsObjectNamesList: MutableList<String> = mutableListOf(),
    var optionsListSize: Pair<Int, Int> = 0 to 0,
    var optionsSelectorPosition: Pair<Int, Int> = 0 to 0
)