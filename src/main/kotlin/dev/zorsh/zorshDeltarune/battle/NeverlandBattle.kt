package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.color
import dev.zorsh.zorshDeltarune.utils.fontText
import dev.zorsh.zorshDeltarune.utils.minus
import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.runLater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.title.Title.title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3d
import org.joml.Vector3f
import java.time.Duration
import java.util.UUID
import kotlin.math.ceil

class NeverlandBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) : INeverlandBattle {

    lateinit var battleUUID: UUID

    private var onEndedAction = {}

    private val battleCanvas = BattleCanvas(players.mapNotNull { it.player }, this)

    private var loopTask: BukkitTask? = null

    private var battleJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    private val shulkerHitboxes = mutableSetOf<Int>()
    private val spawnedEntities = mutableSetOf<FakeDisplay>()

    private var theSoul: FakeTextDisplay? = null

    object BattleLocation {
        val TEST = Location(Bukkit.getWorld("world"), 8.0, 100.0, 8.1)
        val UNDER_STATION = Location(Bukkit.getWorld("moon"), 952.0, 99.6, 1101.0)
    }

    private val battleCenterLocation = BattleLocation.UNDER_STATION

    var playersTurn = false

    private fun newShaderEffector(
        loc: Location,
        playerToShow: List<Player> = players.mapNotNull { it.player },
    ) {
        PacketManager.spawnShaderEffector(
            loc,
            playerToShow,
        ) { entity ->
            spawnedEntities += entity
            entity.holder = spawnedEntities
        }
    }

    private fun newHitboxEntity(
        loc: Location,
        scale: Double,
        playerToShow: List<Player> = players.mapNotNull { it.player },
    ) {
        PacketManager.spawnHitbox(loc, scale, playerToShow) { anchor, shulker ->
            shulkerHitboxes += anchor
            shulkerHitboxes += shulker
        }
    }

    override fun destroyBattle() {
        for (pl in players) {
            try {
                pl.freeFromBattle(battleUUID)
            } catch (_: Exception) {
            }
        }
        val toDestroy = spawnedEntities.toList()
        for (ent in toDestroy) {
            try {
                ent.destroy()
            } catch (_: Exception) {
            }
        }
        spawnedEntities.clear()
        if (loopTask?.isCancelled == false) {
            loopTask?.cancel()
        }
        if (battleJob?.isCancelled == false) {
            battleJob?.cancel()
        }
        for (ent in shulkerHitboxes) {
            try {
                PacketManager.removeEntity(ent, getBattlePlayers().mapNotNull { it.player })
            } catch (_: Exception) {
            }
        }
        shulkerHitboxes.clear()
        scope.cancel()
        battleCanvas.myCanvas.clear()
    }

    override fun setUUID(uuid: UUID) {
        battleUUID = uuid
    }

    override fun getBattleInitialPlayers() = players

    override fun getBattlePlayers() = players.filter { it.player?.isOnline == true && it.myBattleUUID == battleUUID }

    override fun getBattleEnemies() = enemies

    override fun startBattle(onEnded: () -> Unit) {
        onEndedAction = onEnded

        for (pl in players) {
            pl.updatePlayer()
        }

        Bukkit.broadcast(Component.text("Debug: Starting battle $battleUUID"))
        Bukkit.broadcast(Component.text("Debug: Players: ${getBattlePlayers().map { it.player?.name }}"))

        for (pl in getBattlePlayers()) {
            pl.player?.showTitle(
                title(
                    fontText("\uD701", "#000000", "space:default"),
                    Component.text(""),
                    Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ZERO)
                )
            )
            runLater(3) {
                if (pl.player != null) {
                    Bukkit.dispatchCommand(
                        Bukkit.getServer().consoleSender,
                        "sendshaderdata ${pl.player!!.name} 1"
                    )
                }
            }
            pl.lockInBattle(battleCenterLocation)
            pl.player?.playSound(pl.player!!, "encounter", 1f, 1f)
            runLater(20) {
                pl.player?.playSound(pl.player!!, "weaponpull", 1f, 1f)
            }
            runLater(30) {
                pl.player?.playSound(pl.player!!, "battle", 1f, 1f)
            }
        }

        runLater(2) {
            prepareBattle()
            battleCanvas.initCanvas()
            battleCanvas.setupLayout()
        }

        runLater(40) {
            CoroutineScope(Dispatchers.IO).launch {
                val job = scope.launch {
                    repeat(3) {
                        battleCanvas.showPlayerOptions()
                        delay(3000)
                        battleCanvas.hidePlayerOptions()
                        battleBoxOpen()
                        delay(500)
                        unlockSouls()
                        delay(5000)
                        lockSouls()
                        battleBoxClose()
                        delay(500)
                    }
                }
                battleJob = job
                job.join()
                endBattle()
            }
        }

//        runLater(20) {
//            CoroutineScope(Dispatchers.IO).launch {
//                delay(250)
//                val job = scope.launch {
//                    repeat(5) {
//                        delay(100L)
//                        showPlayersOptions()
//                        delay(5000L)
//                        playersTurn = false
//                        hidePlayersOptions()
//                        battleBoxOpen()
//                        unlockSouls()
//                        val jobs = mutableListOf<Job>()
//                        for (enemy in enemies) {
//                            jobs += launch {
//                                enemy.attack()
//                            }
//                        }
//                        jobs.joinAll()
//                        delay(200L)
//                        lockSouls()
//                        battleBoxClose()
//                        delay(100L)
//                    }
//                }
//                battleJob = job
//                job.join()
//                endBattle()
//            }
//
//            loopTask = object : BukkitRunnable() {
//                override fun run() {
//                    if (players.all { !it.locked }) {
//                        if (battleJob?.isCancelled == false) {
//                            battleJob?.cancel()
//                        }
//                        cancel()
//                    }
//                }
//            }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
//        }
    }

    override fun endBattle() {
        destroyBattle()
        onEndedAction()
    }

    private fun prepareBattle() {
        val loc = battleCenterLocation
        loc.yaw = 0f
        loc.pitch = -90f

        runLater(3) {
            val sprite = CanvasSprite.SOUL
            PacketManager.spawnTextDisplay(
                loc,
                sprite.toTextValue().color(ShaderTextColor.pure("#ff0000").value),
                getBattlePlayers().mapNotNull { it.player },
                data = FakeDisplayData(
                    Transformation(
                        Vector3f(0f, 20f / 16f / 8f, 0f),
                        AxisAngle4f(),
                        Vector3f(0f, 0f, 1f),
                        AxisAngle4f()
                    ),
                    opacity = 251.toByte()
                ),
                seeThrough = false,
                alignment = TextDisplay.TextAlignment.CENTER,
                lineWidth = 1000,
                isShadowed = false
            ) { ent ->
                spawnedEntities += ent
                theSoul = ent
            }
        }
    }

    private fun unlockSouls() {
        val soul = theSoul ?: return
        val scaling = CanvasSprite.SOUL.getSizeRatios()
        theSoul?.changeOnlyTransformation(
            Transformation(
                soul.transformation.translation,
                soul.transformation.leftRotation,
                Vector3f(2.5f * scaling.first / 8f, 2.5f * scaling.second / 8f, 1f),
                soul.transformation.rightRotation
            )
        )
        getBattlePlayers().forEach { battlePlayer ->
            battlePlayer.unlockSoul(battleCanvas, getBattlePlayers().mapNotNull { dp -> dp.player }.filter { pl -> pl.uniqueId != battlePlayer.uuid })
        }
    }

    private fun lockSouls() {
        val soul = theSoul ?: return
        theSoul?.changeOnlyTransformation(
            Transformation(
                soul.transformation.translation,
                soul.transformation.leftRotation,
                Vector3f(0f, 0f, 1f),
                soul.transformation.rightRotation
            )
        )
        getBattlePlayers().forEach {
            it.lockSoul()
        }
    }

    private fun battleBoxOpen() {
        val availableSizes = enemies.map { it.askBoxSize() }
        var sizeX = 60f
        var sizeZ = 60f
        if (availableSizes.isNotEmpty()) {
            val rand = availableSizes.toSet().random()
            sizeX = rand.first
            sizeZ = rand.second
        }
        battleCanvas.openBattleBox(0f, 20f, sizeX, sizeZ)
        val scale = 1 / 16.0 / 8.0
        val playerWidth = 0.3
        val soulWidth = 9.0 * scale
        val shulkScaleZ = ceil(sizeX * scale)
        val shulkScaleX = ceil(sizeZ * scale)
        val loc1 =
            battleCenterLocation + Vector3d(0.0, 0.0, sizeZ * scale + shulkScaleZ * 0.5 + playerWidth - soulWidth)
        newHitboxEntity(loc1, shulkScaleZ)
        val loc2 =
            battleCenterLocation - Vector3d(0.0, 0.0, sizeZ * scale + shulkScaleZ * 0.5 + playerWidth - soulWidth)
        newHitboxEntity(loc2, shulkScaleZ)
        val loc3 =
            battleCenterLocation + Vector3d(sizeX * scale + shulkScaleX * 0.5 + playerWidth - soulWidth, 0.0, 0.0)
        newHitboxEntity(loc3, shulkScaleX)
        val loc4 =
            battleCenterLocation - Vector3d(sizeX * scale + shulkScaleX * 0.5 + playerWidth - soulWidth, 0.0, 0.0)
        newHitboxEntity(loc4, shulkScaleX)
    }

    private fun battleBoxClose() {
        for (id in shulkerHitboxes) {
            PacketManager.removeEntity(id, players.mapNotNull { it.player })
        }
        shulkerHitboxes.clear()
        battleCanvas.closeBattleBox()
    }

    private suspend fun showPlayersOptions() {
        repeat(2) {
            for (dPlayer in players) {
                // Lift players interface
                for (entity in dPlayer.perPlayerEntities) {
                    val transform = entity.transformation
                    entity.changeTransformation(
                        Transformation(
                            transform.translation + Vector3f(0f, 0.44f, 0f),
                            transform.leftRotation,
                            transform.scale,
                            transform.rightRotation
                        )
                    )
                }
            }
            delay(50)
        }
        delay(150)
        for (dPlayer in players) {
            dPlayer.actionStage = PlayerActionStage.SELECT_BUTTON
            val buttonEntity = dPlayer.playerButtonTexts[dPlayer.playerSelectedButton]
            buttonEntity.changeTransformation(
                Transformation(
                    buttonEntity.transformation.translation,
                    AxisAngle4f(),
                    Vector3f(1f),
                    AxisAngle4f()
                )
            )
        }
        delay(50)
        playersTurn = true
    }

    private suspend fun hidePlayersOptions() {
        playersTurn = false
//        for (enemy in enemies.filterIsInstance<SpritedEnemy>()) {
//            enemy.mySprite.glowTo.clear()
//        }
        for (dPlayer in players) {
            runLater(2L) {
                dPlayer.clearMenu()
                dPlayer.menuSelectorHeart?.hide()
                val infoEnt = dPlayer.battleInfoText
                infoEnt?.changeOnlyTransformation(
                    Transformation(
                        infoEnt.transformation.translation,
                        infoEnt.transformation.leftRotation,
                        Vector3f(1.8f, 2f, 1f),
                        infoEnt.transformation.rightRotation
                    )
                )
            }

            val index = dPlayer.playerSelectedButton
            val buttonEntity = dPlayer.playerButtonTexts[index]
            val transform = buttonEntity.transformation
            buttonEntity.changeTransformation(
                Transformation(
                    transform.translation,
                    transform.leftRotation,
                    Vector3f(0f, transform.scale.y, 1f),
                    transform.rightRotation
                )
            )
        }
        delay(150)
        repeat(2) {
            for (dPlayer in players) {
                for (entity in dPlayer.perPlayerEntities) {
                    val transform = entity.transformation
                    entity.changeTransformation(
                        Transformation(
                            transform.translation + Vector3f(0f, -0.44f, 0f),
                            transform.leftRotation,
                            transform.scale,
                            transform.rightRotation
                        )
                    )
                }
            }
            delay(50)
        }
    }

    private fun proceedPlayerSubmit(dPlayer: DeltarunePlayer) {
        if (playersTurn) {
            when (dPlayer.actionStage) {
                PlayerActionStage.SELECT_BUTTON -> {
                    val index = dPlayer.playerSelectedButton
                    val buttonEntity = dPlayer.playerButtonTexts[index]
                    val transform = buttonEntity.transformation
                    buttonEntity.changeTransformation(
                        Transformation(
                            transform.translation,
                            transform.leftRotation,
                            Vector3f(0f, transform.scale.y, 1f),
                            transform.rightRotation
                        )
                    )
                    val ent = dPlayer.battleInfoText
                    ent?.changeOnlyTransformation(
                        Transformation(
                            ent.transformation.translation,
                            ent.transformation.leftRotation,
                            Vector3f(0f),
                            ent.transformation.rightRotation
                        )
                    )
                    when (dPlayer.playerSelectedButton) {
                        0 -> {
                            dPlayer.actionStage = PlayerActionStage.FIGHT_SELECT_ENEMY
                            showEnemiesSelection(dPlayer)
                        }

                        1 -> {
                            dPlayer.actionStage = PlayerActionStage.ACT_SELECT_ACT
                        }

                        2 -> {
                            dPlayer.actionStage = PlayerActionStage.ITEM_SELECT_ITEM
                        }

                        3 -> {
                            dPlayer.actionStage = PlayerActionStage.MERCY_SELECT_ENEMY
                        }

                        else -> {
                            dPlayer.actionStage = PlayerActionStage.NONE
                        }
                    }
                }

                PlayerActionStage.FIGHT_SELECT_ENEMY -> {
                    dPlayer.actionStage = PlayerActionStage.NONE
                    dPlayer.clearMenu()
                    dPlayer.menuSelectorHeart?.hide()
                }

                else -> {}
            }
        }
    }

    private fun showEnemiesSelection(dPlayer: DeltarunePlayer) {
        dPlayer.menuSelectorHeart?.setPosition(0, 0)
        dPlayer.menuSelectorHeart?.setBounds(1, enemies.size)
        val loc = battleCenterLocation
        loc.yaw = 180f
        loc.pitch = -90f
        var offset = 0f
        for (enemy in enemies) {
            val updating = Component.text()
            updating.append(Component.text(" ".repeat(64) + '\n'))
            updating.append(enemy.name)
//            newTextDisplay(
//                loc,
//                updating.style(Style.style(TextDecoration.BOLD)).shadowColor(ShadowColor.shadowColor(0, 0, 64, 255))
//                    .build(),
//                playerToShow = listOfNotNull(dPlayer.player),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(0f, -3f - offset, 0.011f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1.8f, 2f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    ),
//                    interpolationDuration = 0
//                ),
//                lineWidth = 320,
//                alignment = TextDisplay.TextAlignment.LEFT,
//                isShadowed = true,
//                mountTo = true
//            ) { txtEntity ->
//                dPlayer.moveMenuTexts += txtEntity
//            }
            offset += 0.7f
        }
        dPlayer.menuSelectorHeart?.show()
//        runInfinite(1) { _, task ->
//            if (enemies.isNotEmpty() && dPlayer.player != null && dPlayer.actionStage == PlayerActionStage.FIGHT_SELECT_ENEMY && dPlayer.menuSelectorHeart != null && dPlayer.locked && playersTurn) {
//                val enemy = enemies.getOrNull(dPlayer.menuSelectorHeart!!.myY)
//            } else {
//                task.cancel()
//            }
//        }
    }
}